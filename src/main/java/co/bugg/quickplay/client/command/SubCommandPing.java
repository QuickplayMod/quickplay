package co.bugg.quickplay.client.command;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.http.Request;
import co.bugg.quickplay.http.response.WebResponse;
import co.bugg.quickplay.util.Message;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.List;

/**
 * Sub command for the "help" message
 */
public class SubCommandPing extends ASubCommand {

    /**
     * Constructor
     * @param parent Parent command
     */
    public SubCommandPing(ACommand parent) {
        super(
                parent,
                "ping",
                "Ping the Quickplay servers",
                "",
                false,
                false,
                0.0
        );
    }

    @Override
    public void run(String[] args) {
        try {
            Request req = Quickplay.INSTANCE.requestFactory.newPingRequest();
            WebResponse res = req.execute();
            if(res.ok) {
                Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentText("Ping request success")));
            } else {
                Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentText("Ping request failed (Response not ok)")));
            }
        } catch(Exception e) {
            e.printStackTrace();
            Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentText("Ping request failed (Exception caught)")));
        }
    }

    @Override
    public List<String> getTabCompletions(String[] args) {
        return new ArrayList<>();
    }
}
