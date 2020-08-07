package co.bugg.quickplay.http;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
import co.bugg.quickplay.actions.serverbound.InitializeClientAction;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class SocketClient extends WebSocketClient {

    public SocketClient(URI uri) {
        super(uri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {

        this.sendAction(new InitializeClientAction());
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

    public void sendAction(Action action) {
        if(action == null) {
            return;
        }
        this.send(action.build());
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }
}
