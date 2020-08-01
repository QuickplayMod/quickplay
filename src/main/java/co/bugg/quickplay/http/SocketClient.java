package co.bugg.quickplay.http;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.actions.Action;
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
        this.send("Hello, world!");
        System.out.println("> Hello, world!");
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

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {

    }
}
