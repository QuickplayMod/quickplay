package co.bugg.quickplay.http;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.actions.serverbound.InitializeClientAction;
import co.bugg.quickplay.actions.serverbound.MigrateKeybindsAction;
import co.bugg.quickplay.actions.serverbound.ServerJoinedAction;
import co.bugg.quickplay.actions.serverbound.SetClientSettingsAction;
import co.bugg.quickplay.config.AConfiguration;
import co.bugg.quickplay.config.ConfigKeybinds;
import co.bugg.quickplay.elements.ElementController;
import co.bugg.quickplay.util.Message;
import co.bugg.quickplay.util.QuickplayChatComponentTranslation;
import co.bugg.quickplay.util.ServerChecker;
import co.bugg.quickplay.util.ServerUnavailableException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import java.io.FileNotFoundException;
import java.io.IOException;
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
                // Only try to re-connect if not disabled.
                if(!Quickplay.INSTANCE.isEnabled) {
                    return;
                }
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
            this.sendAction(new SetClientSettingsAction(Quickplay.INSTANCE.settings));

            final String currentIp = ServerChecker.getCurrentIP();
            if(currentIp != null) {
                Quickplay.INSTANCE.socket.sendAction(new ServerJoinedAction(currentIp, null));
            }
        } catch (ServerUnavailableException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.messageBuffer.push(new Message(
                    new QuickplayChatComponentTranslation("quickplay.failedToConnect")
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
            ));
            // Failed to connect to Quickplay backend -- Load gamelist from cache
            try {
                Quickplay.INSTANCE.elementController = ElementController.loadCache();
            } catch (FileNotFoundException fileNotFoundException) {
                System.out.println("Failed to load cached elements.");
                fileNotFoundException.printStackTrace();
            }
        }

        // If keybinds need to be converted on socket connect, then send a migrate keybinds action.
        if(ConfigKeybinds.checkForConversionNeeded("keybinds.json")) {
            try {
                // Get the array of keybinds from the file keybinds.json
                final String contents = AConfiguration.getConfigContents("keybinds.json");
                final JsonElement base = new Gson().fromJson(contents, JsonElement.class);
                // checkForConversionNeeded asserts that none of the items on the following line return null.
                final JsonArray arr = base.getAsJsonObject().get("keybinds").getAsJsonArray();
                final Action action = new MigrateKeybindsAction(arr);
                try {
                    this.sendAction(action);
                } catch(ServerUnavailableException e) {
                    e.printStackTrace();
                    Quickplay.INSTANCE.messageBuffer.push(new Message(
                            new QuickplayChatComponentTranslation("quickplay.keybinds.migratingFailed")
                                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED))
                            , true));
                    Quickplay.INSTANCE.keybinds = new ConfigKeybinds(true);
                    Quickplay.INSTANCE.keybinds.save();
                }
            } catch (JsonSyntaxException | IOException e) {
                e.printStackTrace();
            }
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
            if(action == null) {
                return;
            }
            if(Quickplay.INSTANCE.isInDebugMode) {
                System.out.println("DEBUG > " + action.getClass().getName() + " received.");
            }
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
        if(Quickplay.INSTANCE.isInDebugMode) {
            System.out.println("DEBUG > " + action.getClass().getName() + " sent.");
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
