package advancedbot;

import cz.cuni.pogamut.Client.Agent;
import cz.cuni.pogamut.exceptions.PogamutException;
import cz.cuni.pogamut.introspection.PogProp;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import cz.cuni.pogamut.MessageObjects.*;
import cz.cuni.pogamut.exceptions.ConnectException;

//import newbotdb.*;
import java.sql.*;
//import advancedBot.nasty;

/**
 * This bot will show you how to shoot down your opponets, it's extension
 * of the SimpleBot ... it just checks whether it sees an opponent and 
 * if so - shoot him down.
 * 
 * @author Horatko
 */
public class Main extends Agent {

    public Main() {
    }
    
    NavPoint chosenNavigationPoint = null;
    
    Player enemy = null;

    /**
     * Main method of the bot's brain - we're going to do some thinking about
     * the situation we're in (how it's unfair to be the bot in the gloomy world
     * of UT2004 :-).
     * <p>
     * Check out the javadoc for this class - there you find a basic concept
     * of this bot.
     */
    protected void doLogic() {
        // marking next iteration
        log.fine("doLogic iteration");

        // do we have enemy to shoot down?
        if (enemy != null) {
            // yes we do - can we still see him?
            enemy = memory.getSeePlayer(enemy.UnrealID);
            if (enemy != null) {
                // change to the best weapon
                body.changeWeapon(memory.getBetterWeapon(memory.getAgentLocation(), enemy.location));
                // shoot him
                body.shoot(enemy);
                // run towards it (in stupid way...)
                body.runToTarget(enemy);
                return;
            } else {
                // enemy lost, stop shooting
                body.stopShoot();
            }                   
        }
        
        // okey, so we've lost our enemy or we didn't have one from previous logic iteration
        // can we see another one?
        enemy = memory.getSeeEnemy();
        if (enemy != null) {
            // change to the best weapon
            body.changeWeapon(memory.getBetterWeapon(memory.getAgentLocation(), enemy.location));
            // we can see the enemy - let's shoot it!
            body.shoot(enemy);
            // run towards it (in stupid way...)
            body.runToTarget(enemy);
            return;
        }
        
        // no enemy spotted ... just run randomly
        
        // if don't have any navigation point chosen
        if (chosenNavigationPoint == null) {
            // let's pick one at random
            chosenNavigationPoint = memory.getKnownNavPoints().get(
                    random.nextInt(memory.getKnownNavPoints().size()));
            log.info("navpoint="+chosenNavigationPoint.location);
            //chosenNavigationPoint.location.x = 698.0;
            //chosenNavigationPoint.location.y = -94.0;
            //chosenNavigationPoint.location.z = -111.0;
                   //(698.0,-94.0,-111.0);
            //newbot(chosenNavigationPoint.location.toString());
            try { nasty(chosenNavigationPoint.location.toString()); } catch (Exception K) { System.out.println("Hello World!"); }
            //try { nasty.jacob(chosenNavigationPoint.location.toString()); } catch (Exception K) { System.out.println("Hello World!"); }
        }
        // here we're sure the chosenNavigationPoint is not null
        // call method iteratively to get to the navigation point
        if (!gameMap.safeRunToLocation(chosenNavigationPoint.location)) {
            // if safeRunToLocation() returns false it means
            if (Triple.distanceInSpace(memory.getAgentLocation(), chosenNavigationPoint.location) < 100) {
                // 1) we're at the navpoint
                log.info("I've successfully arrived at navigation point!");
            } else {
                // 2) something bad happens
                log.info("Darn the path is broken :(");
            }
            // nullify chosen navigation point and chose it during the
            // next iteration of the logic
            chosenNavigationPoint = null;
        }
    }

    /**
     * NOTE: this method MUST REMAIN DEFINED + MUST REMAIN EMPTY, due to technical reasons.
     */
    public static void main(String[] Args) {
    }

    //public void nasty () { System.out.println("Hello World!"); }
  //public void nasty(String[] location) throws Exception {
  public void nasty(String location) throws Exception {

      Class.forName("org.sqlite.JDBC");
      Connection conn = DriverManager.getConnection("jdbc:sqlite:/Program Files/Pogamut 2/PogamutPlatform/projects/AdvancedBot/src/advancedbot/sample.db");
      Statement stat = conn.createStatement();
      //stat.executeUpdate("drop table if exists people;");
      //stat.executeUpdate("create table people (name, occupation);");
      PreparedStatement prep = conn.prepareStatement(
          "insert into people values (?, ?);");

      //prep.setString(1, location.toString());
      prep.setString(1, location);
      prep.setString(2, "politics");
      prep.addBatch();

      conn.setAutoCommit(false);
      prep.executeBatch();
      conn.setAutoCommit(true);

      ResultSet rs = stat.executeQuery("select * from people;");
      while (rs.next()) {
          log.info("name = " + rs.getString("name"));
          log.info("job = " + rs.getString("occupation"));
      }
      rs.close();

      conn.close();
  }
  
}


