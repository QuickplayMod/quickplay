package co.bugg.quickplay.util;

import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Written by Aaron1998ish on 11/12/2017.
 *
 * Useful methods for retrieving data on scoreboards on Hypixel
 *
 */
public class ScoreboardUtils {

    /**
     * Filters scores from a scoreboards objective and returns the values in a list
     *
     * Output was designed around Hypixels scoreboard setup.
     *
     * @param scoreboard the target scoreboard
     * @return returns an empty list if no scores were found.
     */
    public static List<String> getSidebarScores(Scoreboard scoreboard) {
        List<String> found = new ArrayList<>();

        ScoreObjective sidebar = scoreboard.getObjectiveInDisplaySlot(1);
        if (sidebar != null) {
            List<Score> scores = new ArrayList<>(scoreboard.getScores());


            /*Scores retrieved here do not care for ordering, this is done by the Scoreboard its self.
              We'll need to do this our selves in this case.

              This will appear backwars in chat, but remember that the scoreboard reverses this order
              to ensure highest scores go first.
             */
            scores.sort(Comparator.comparingInt(Score::getScorePoints));

            found = scores.stream()
                    .filter(score -> score.getObjective().getName().equals(sidebar.getName()))
                    .map(score -> score.getPlayerName() + getSuffixFromContainingTeam(scoreboard, score.getPlayerName()))
                    .collect(Collectors.toList());

        }
        return found;
    }

    /**
     * Filters through Scoreboard teams searching for a team
     * that contains the last part of our scoreboard message.
     *
     *
     * @param scoreboard The target scoreboard
     * @param member The message we're searching for inside a teams member collection.
     * @return If no team was found, an empty suffix is returned
     */
    private static String getSuffixFromContainingTeam(Scoreboard scoreboard, String member) {
        String suffix = null;
        for (ScorePlayerTeam team : scoreboard.getTeams()) {
            if (team.getMembershipCollection().contains(member)) {
                suffix = team.getColorSuffix();
                break;
            }
        }
        return (suffix == null ? "" : suffix);
    }
}
