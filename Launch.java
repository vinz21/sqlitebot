package advancedbot; // replace with your package name

import cz.cuni.pogamut.Client.AgentLauncher;
import java.net.URI;
import java.net.URISyntaxException;

public class Launch
{
    public static void main(String[] args) throws URISyntaxException
    {
        String uri;
        if(args.length > 0)
        {
            uri = args[0];
        }
        else
        {
            uri = "ut://localhost:3000";
        }

        URI gameBots = new URI(uri);

        Main mybot = new Main();
        AgentLauncher launcher = new AgentLauncher(mybot, gameBots, true);

        launcher.launch("Player_Jeremy"); // launches bot - replace Player_xxx with your bot's name
    }
}