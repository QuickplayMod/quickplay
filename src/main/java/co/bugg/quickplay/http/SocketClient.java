package co.bugg.quickplay.http;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.actions.serverbound.InitializeClientAction;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.util.ServerUnavailableException;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Date;

public class SocketClient extends WebSocketClient {

    public static final int pingTime = 30000;
    private Date lastPing;
    private boolean connected = false;

    public SocketClient(URI uri) {
        super(uri);

        // Loop for a Quickplay server disconnection
        Quickplay.INSTANCE.threadPool.submit(() -> {
            while(Quickplay.INSTANCE.socket == this) {
                try {
                    Thread.sleep(SocketClient.pingTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.checkForTimeout();
            }
        });
    }

    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        super.onWebsocketPing(conn, f);
        this.lastPing = new Date();
    }

    void checkForTimeout() {
        if(this.lastPing == null || this.lastPing.getTime() < new Date().getTime() - SocketClient.pingTime * 2) {
            this.reconnect();
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to Quickplay backend.");
        this.connected = true;
        this.lastPing = new Date();

        try {
            this.sendAction(new InitializeClientAction());
        } catch (ServerUnavailableException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.messageBuffer.push(new Message(
                    new QuickplayChatComponentTranslation("quickplay.failedToConnect")
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)),
                    true
            ));
        }
    }

    @Override
    public void onMessage(String message) {
        // Unused, see onMessage(ByteBuffer)
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        try {
            final Action action = Action.from(bytes);
            action.run();
        } catch (BufferUnderflowException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.threadPool.submit(() -> Quickplay.INSTANCE.sendExceptionRequest(e));
        }
    }

    /**
     * Send an action to the server. Does nothing if action is null.
     * @param action The action to send to the server.
     * @throws ServerUnavailableException If the client is not connected to the server to send this Action.
     */
    public void sendAction(Action action) throws ServerUnavailableException {
        if(action == null) {
            return;
        }
        try {
            this.send(action.build());
        } catch(WebsocketNotConnectedException e) {
            throw new ServerUnavailableException();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        this.connected = false;
    }

    @Override
    public void onError(Exception ex) {
        // Don't clutter the logs if we've already established that the connection was lost.
        if(this.connected) {
            System.out.println("Lost connection to Quickplay backend!");
            ex.printStackTrace();
        }
        Quickplay.INSTANCE.threadPool.submit(this::reconnect);
    }
}
