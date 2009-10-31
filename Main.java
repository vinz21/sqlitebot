package advancedbot;

import cz.cuni.pogamut.Client.Agent;
import cz.cuni.pogamut.Client.RcvMsgEvent;
import cz.cuni.pogamut.Client.RcvMsgListener;
import cz.cuni.pogamut.MessageObjects.*;
import cz.cuni.pogamut.introspection.PogProp;
import cz.cuni.pogamut.exceptions.ConnectException;
import cz.cuni.pogamut.exceptions.PogamutException;

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This bot currently is copied code from the hunter bot example,
 * plus a strafing fuction from the amis/loque bot.
 *
 * The part which is mine are the functions dbWrite(database INSERT) and SetNavpoint(database SELECT)
 * which demonstrate the beginning utilization of a sqlite database to persist the bots memory/events
 * for better analysis and planning by the bot in gameplay
 */

public class Main extends Agent {

   String sqliteClass = "org.sqlite.JDBC";
   //String sqliteDBPath = "jdbc:sqlite:c:/sqlitebot/sample.db";
   String sqliteDBPath = "jdbc:sqlite:/sqlitebot/sample.db";  //unix

   //below bot values populated from db table lookup function initBot
   int botId = 0;
   String botName = null;
   int botSkill = 0;
   String botSkin = null;
   int botTeam = 0;
   
   boolean killBot = false;

   boolean manualSpawn = false;
   //behavior = normal,camp,navlog,hide,tide,general
   String behavior = "general";

   String botSquad = null;

   //end initBot settings

   int navStep = 0;
   boolean seeTargetValue;
   int pickupDistance = 50;
   Double weaponWaitTime = 0.5;
   boolean statePursue = false;
   int lastSeenWaitTime = 30;

   ArrayList<String> squadArray = new ArrayList<String> ();
   ArrayList<String> playerArray = new ArrayList<String> ();
   ArrayList<Integer> weaponArray = new ArrayList<Integer> ();

   String lastSeenLocation = null;
   String botCommand = "none";
   String botCommandTarget = "none";
   String botCommandLocation = "none";
   Double botCommandRadius = 0.0;

   @PogProp NavPoint chosenNavigationPoint = null;
   @PogProp NavPoint lastNavigationPoint = null;

   //int navSize = memory.getKnownNavPoints().size();
   @PogProp int navCount = 0;

   int hideFromNav = -1; //used with 'hide' behavior

   // Create a hash table
   Map navMap = new HashMap();
   //navMap = new TreeMap();        // sorted map
   Map timerMap = new HashMap();
   Map nameMap = new HashMap();
   Map playerSeeWaitMap = new HashMap();
   Map squadMap = new HashMap();

   @PogProp int botYFocus = 0;
   @PogProp double botYMin = 0.0;
   @PogProp double botYMax = 0.0;

   Player enemy = null;
   double gameTime = 0.0;
   double turnTime = 0.0;
   double playerSeeWait = 0.0;
   double dbInsertLastSeenWait = 0.0;
   double refreshMapsWait = 0.0;

   public boolean shouldIdle = false;
   boolean idleState = false;
   double idleTime = 0.0;

   boolean strafingRight = true;

   int tripFlag = 0;
   boolean initFlag = true;

   //@PogProp boolean turn;
   //@PogProp boolean hitWall;

   @PogProp int frags = 0;
   @PogProp int deaths = 0;
   /** how low the health level should be to start collecting healht */
   @PogProp
   //public int healthSearchLevel = 90;
   public int healthEvadeLevel = 60;  //try 60
   /** choosen item for the state seeItem */
   protected Item choosenItem = null;
   /** choose med kits for the stateMedKit */
   protected ArrayList<Item> choosenMedKits = null;

   protected ArrayList<Item> choosenItems = null;

   @PogProp
   public boolean useAStar = false;
    /** last enemy which disappeared from agent's view */
    private Player lastEnemy = null;
    /** walking mystic properties - prevent bot from continuous jumping - he will jump only once */
    private boolean jumped;
    /**
     * Stores last unreachable item - item that bot chose and was not able to go to. <br>
     * This setting should prevent bot from stucks.
     */
    protected Item previousChoosenItem = null;

    @PogProp
    public boolean shouldRearm = true;
    @PogProp
    public boolean shouldEngage = true;
    public boolean shouldEngageClose = true;
    @PogProp
    public boolean shouldPursue = true;
    @PogProp
    public boolean shouldCollectItems = true;

    public boolean shouldHide = false;
    public boolean shouldSleep = false;

    Weapon weaponGet = null;
    Weapon weaponGetPickup = null;
    String runType = null;

    Health healthGet = null;
    Ammo ammoGet = null;

   /** Creates a new instance of agent. */
   public Main() {
       super();
       /**
        * set level of logging - see logging documentation, now you will see only more relevant things
        */
       log.setLevel(Level.INFO);
       platformLog.setLevel(Level.INFO);
   }

   @Override
   protected void postPrepareAgent() {

       try { initBot(); } catch (Exception K) { System.out.println("error:initBot"); }

       body.initializer.setName(botName);
       // this affects the bot's skill level ... 6 == GODLIKE, 0 == total newbie
       //good default = 4
       body.initializer.setBotSkillLevel(botSkill);
       body.initializer.setSkin(botSkin);
       //testing teams/squads
       body.initializer.setTeam(botTeam);

       if (manualSpawn) {
           body.initializer.setManualSpawn(true);
           //Triple startLocation = new Triple (2817.0,4612.0,50.0);
           Triple startLocation = new Triple (100,100,50);
           body.initializer.setLocation(startLocation);
           Triple startRotation = new Triple (65280,31232,0);
           body.initializer.setRotation(startRotation);
       }

       //body.initializer.setTeam(1);
    // Add key/value pairs to the map
    //navMap.put("x", new Integer(1));
    //navMap.put("y", new Integer(2));
    //navMap.put("z", new Integer(3));

    for (int i=0; i<memory.getKnownNavPoints().size(); i++) {
         navMap.put(Integer.toString(memory.getKnownNavPoints().get(i).ID), new Integer (Integer.toString(i)));
         //log.info(i+":"+Integer.toString(memory.getKnownNavPoints().get(i).ID)+":"+memory.getKnownNavPoints().get(i).UnrealID);
    }

      if (lastNavigationPoint == null) {
        log.info("navpoint=null");
        lastNavigationPoint = memory.getKnownNavPoints().get(0);
        //chosenNavigationPoint = memory.getKnownNavPoints().get(0);
      }
       
    // Get number of entries in map
    //int size = navMap.size();        // 2
    //log.info("size:"+size);
    //createNavArray();
 
/*
      Class.forName(sqliteClass);
      Connection conn = DriverManager.getConnection(sqliteDBPath);
      Statement stat = conn.createStatement();

      String sql = null;
      sql = "select from_navpoint_id from navpoint where from_navpoint_id = and map_level =  limit 1;";

      log.info("sql:"+sql);
      ResultSet rs = stat.executeQuery(sql);

      boolean flagFound = false;
      while (rs.next()) {
             flagFound = true;
      }

      rs.close();
      conn.close();
 */
}

    /**
     * Main method of the bot's brain - we're going to do some thinking about
     * the situation we're in (how it's unfair to be the bot in the gloomy world
     * of UT2004 :-).
     * <p>
     * Check out the javadoc for this class - there you find a basic concept
     * of this bot.
     */
    @Override
     protected void doLogic() {
       // marking next iteration
       //log.info("doLogic iteration");

       //kill bot if no table rows(slots) available
       if (botName == null) { log.info("no bots available"); System.exit(0); }

       if (initFlag) {
         initFlag = false;
         //for navlog each navpoint can see itself
         if (behavior.equals("navlog")) {
             for (int i=0; i<memory.getKnownNavPoints().size(); i++) {
               try { dbWriteNavpoint(memory.getGameInfo().level.toString(),memory.getKnownNavPoints().get(i).ID,memory.getKnownNavPoints().get(i).ID); } catch (Exception K) { System.out.println("error:dbWriteNavpoint:"+i); }
             }
          }
        }

       //botCommand = "none";
       //refresh memory hashmaps from database lookup every n seconds
       if (gameTime > refreshMapsWait) {
         refreshMapsWait = gameTime+3;
         try { refreshInfo(); } catch (Exception K) { System.out.println("error:refreshMaps"); }
       }

       //log.info("botCommand:"+botCommand.toString());

       if (botCommand.equals("halt")) {
                     //log.info("enemyFacinghalt:"+enemyFacing(enemy));
            if (memory.isShooting()) {
              stateStopShooting();
            }
           return;
       }

        // navtest begin

       seeTargetValue = seeTarget();

        //hunt/engage if you are healthy enough or enemy too close, otherwise hide
        boolean enemyClose = false;
        if (memory.getAgentLocation() != null && enemy != null && enemy.location != null) {
          Double enemyFacingValue =  enemyFacing(enemy);
          //FIX if (enemyFacingValue > 0.5) {
          if (enemyFacingValue > 0) {
              int minEngageDistance = Math.round(random.nextFloat() * 200) + 1600;  //was +400,+800
              if (Triple.distanceInSpace(memory.getAgentLocation(), enemy.location) < minEngageDistance) {
                  enemyClose = true;
              }
          }
          else { log.info("enemy facing away:"+enemyFacingValue); }
        }

        //log.info("enemyClose:"+enemyClose+":health:"+memory.getAgentHealth());
        if (!enemyClose && (memory.getAgentHealth() < healthEvadeLevel)) {
            shouldHide = true;
            shouldEngage = false;
            shouldPursue = false;
            weaponGet = null;
        }
        else {
            shouldHide = false;
            //shouldEngage = true;
            //shouldPursue = true;
        }

        // IF-THEN RULES:
        // 1) see enemy and has better weapon? -> switch to better weapon
        if (shouldRearm && seeTargetValue && hasBetterWeapon()) {
            stateChangeToBetterWeapon();
            //return; 10-30
        }

     
        //if (weaponGetPickup != null) { log.info("weaponGetPickup3:"+chosenNavigationPoint.toString());}

        //log.info("loadedWeapon:"+memory.hasLoadedWeapon()+":seeTargetValue:"+seeTargetValue+":shoulEngage:"+shouldEngage);
        // 2) do you see enemy?         -> go to PURSUE (start shooting / hunt the enemy)
        //if (shouldEngage && memory.getTarget() && memory.hasAnyLoadedWeapon()) {
        //FIX - not go after weapon if far away and other closer threats?
        if (shouldEngage && seeTargetValue && memory.hasLoadedWeapon()) {
            gameMap.resetPath();
            stateEngage();
            //if (shouldHide) { navigateBot();}
            return;
        }

         // 3) are you shooting?        -> go to STOP_SHOOTING (stop shooting, you've lost your target)
        if (memory.isShooting()) {
            stateStopShooting();
            //return; 10-30
        }

       // are you being shot?  -> go to HIT (turn around - try to find your enemy)
        if (memory.isBeingDamaged() && enemy == null) {
            stateHit();
            //return;
        }

        //        if (weaponGetPickup != null) { log.info("weaponGetPickup4:"+chosenNavigationPoint.toString());}

        //if (enemy != null) { log.info("enemy:"+enemy.toString()); }
        //if (lastEnemy != null) { log.info("lastEnemy:"+lastEnemy.toString()); }
        // have you got enemy to pursue? -> go to the last position of enemy
        if (shouldEngageClose && (lastEnemy != null) && (shouldPursue) && (memory.hasAnyLoadedWeapon())
                && weaponGetPickup == null) { //&& ammoGet == null ?
            //log.info("lastEnemyLoc:"+lastEnemy.location)
            statePursue();
            navigateBot();
            return;
        }

        // 6) are you walking?          -> go to WALKING       (check WAL)
        if (memory.isColliding()) {
            log.info("collision");
            //strafingRight = !strafingRight;
            //stateWalking();
            //return;
        }

        //if (weaponGetPickup != null) { log.info("weaponGetPickup5:"+chosenNavigationPoint.toString());}

        // 7) do you see item?          -> go to GRAB_ITEM        (pick the most suitable item and run for)
        /*if (shouldCollectItems && seeAnyReachableItemAndWantIt()) {
            stateSeeItem();
            return;
        }*/

        // are you hurt?                        -> get yourself some medKit
        //if (memory.getAgentHealth() < healthSearchLevel && canRunAlongMedKit()) {
        //    stateMedKit();
        //    return;
        //}

   //if (weaponGetPickup != null && healthGet == null && ammoGet == null) {
        if (weaponGetPickup == null) {
         getReachableWeapons();
        }
        if (weaponGetPickup != null) {
            log.info("weaponGetPickup:"+weaponGetPickup.weaponType.toString());
            //dropped item/pickup - no established navpoint
            chosenNavigationPoint = memory.getKnownNavPoints().get(0);
            chosenNavigationPoint.location = weaponGetPickup.location;
        }

        //if (weaponGetPickup != null) { log.info("weaponGetPickup2:"+chosenNavigationPoint.UnrealID.toString());}
        //FIX - for holding several weapontypes
        if (ammoGet == null && !memory.hasLoadedWeapon() && weaponGetPickup == null) {
          canRunAlongItemsAmmo();
        }
        if (ammoGet != null && weaponGetPickup == null) {
            log.info("ammoGet:"+ammoGet.getAmmoType().toString());
            chosenNavigationPoint = ammoGet.navPoint;
        }

        //if (weaponGetPickup != null) { log.info("weaponGetPickup6:"+chosenNavigationPoint.toString());}
        if (healthGet == null && shouldHide && weaponGetPickup == null && ammoGet == null) {
          canRunAlongItemsHealth();
        }
        if (healthGet != null && weaponGetPickup == null) {
            log.info("healthGet:"+healthGet.typeOfHealth.toString());
            chosenNavigationPoint = healthGet.navPoint;
        }

        //        if (weaponGetPickup != null) { log.info("weaponGetPickup7:"+chosenNavigationPoint.toString());}
        if (weaponGet == null && healthGet == null && !shouldHide && weaponGetPickup == null && ammoGet == null && statePursue == false) {
          //gameMap.resetPath();
          canRunAlongWeapons();
        }
        if (weaponGet != null && weaponGetPickup == null && statePursue == false) {
            log.info("weaponGet:"+weaponGet.weaponType.toString());
            chosenNavigationPoint = weaponGet.navPoint;
        }

        //random idle
        if (shouldIdle) {
            if (idleState == false) {

              int minIdleWait = 20;  //min number of seconds to wait between idles
              if (gameTime > idleTime+minIdleWait) {
                log.info("gameTime:"+gameTime+":idleTime:"+idleTime);
                int myRandIdle = random.nextInt(3)+1;  //1 in x chance of entering idle
                log.info("randIdle:"+myRandIdle);
                if (myRandIdle == 1) {
                  idleState = true;
                  idleTime = gameTime;
                  return;
                }
                //return;
              }
            }

            //idleState = true
            int idleLength = 5; //number of idle seconds
            if (gameTime > idleTime+idleLength) {
                idleState = false;
            }
            else {
                int myRandTurn = random.nextInt(90)-45;  //random looking left/right
                body.turnHorizontal(myRandTurn);
                //additional random idle movement/feet ??
                return;
            }
        }
        
 // navtest end

     // no enemy spotted ... just run randomly
     navigateBot();

}

protected void navigateBot() {

        //log.info("navigateBot");
        // if don't have any navigation point chosen
        if (chosenNavigationPoint == null) {
            // let's pick one at random
            try { setNavpoint(behavior); } catch (Exception K) { System.out.println("error:setNavpoint"); }
            //chosenNavigationPoint = memory.getKnownNavPoints().get(random.nextInt(memory.getKnownNavPoints().size()));
            //log.fine("navpoint_logic="+chosenNavigationPoint.UnrealID);
            //log.fine("map_id_logic="+chosenNavigationPoint.getID());
       }
        // here we're sure the chosenNavigationPoint is not null
        // call method iteratively to get to the navigation point

        //log.info("debugMain");
        if (tripFlag == 0) {
            //tripFlag = 1;

        //if (chosenNavigationPoint == lastNavigationPoint) { return; }
        //if (Triple.distanceInSpace(memory.getAgentLocation(), chosenNavigationPoint.location) < pickupDistance) { return; }
//log.info("debugMain2"+chosenNavigationPoint.location.toString());
        //if (weaponGet != null) {log.info("debugMain2:"+weaponGet.weaponType.toString()); }
        //if (healthGet != null) {log.info("debugMain2:"+healthGet.UnrealID.toString()); }
        //if (ammoGet != null) {log.info("debugMain2:"+ammoGet.UnrealID.toString()); }
        //if (weaponGetPickup != null) {log.info("debugMain2WPickup:"+weaponGetPickup.weaponType.toString()); }
        if ((runType != null) && runType.equals("runto")) {
          log.info("runto:"+chosenNavigationPoint.UnrealID.toString());


          if (Triple.distanceInSpace(memory.getAgentLocation(), chosenNavigationPoint.location) < pickupDistance) {
               //(!chosenNavigationPoint.isVisible())) {
              runType = null;
              weaponGetPickup = null;
              return;
          }
          body.runToLocation(chosenNavigationPoint.location);
        return;
        }

        if (Triple.distanceInSpace(memory.getAgentLocation(), chosenNavigationPoint.location) < pickupDistance) {
            pathSuccess();
            return;
        }

        log.info("chosenPoint:"+chosenNavigationPoint.ID+":"+chosenNavigationPoint.UnrealID);
        log.info("chosenLocation:"+chosenNavigationPoint.location.toString());

        //chosenNavigationPoint = gameMap.nearestNavPoint(chosenNavigationPoint.location);
        if (!gameMap.safeRunToLocationNav(chosenNavigationPoint)) {
        //if (!gameMap.safeRunToLocation(chosenNavigationPoint.location)) {
            //log.info("debugMain4"+chosenNavigationPoint.location.toString());
            
        //if (!gameMap.safeRunToLocationNav(chosenNavigationPoint)) {
            // if safeRunToLocation() returns false it means
            //log.info("chosen_dist:"+Triple.distanceInSpace(memory.getAgentLocation(), chosenNavigationPoint.location));
            //FIX - remove below if since checking above?
            if (Triple.distanceInSpace(memory.getAgentLocation(), chosenNavigationPoint.location) < pickupDistance) {
                pathSuccess();
            } else {
                // 2) something bad happens
                log.info("Darn the path is broken :(:"+Triple.distanceInSpace(memory.getAgentLocation(), chosenNavigationPoint.location));
                //body.runToLocation(lastNavigationPoint.location);

                gameMap.resetPath();
                statePursue = false;

                if (memory.getSeeAnyNavPoint()) {
                  body.runToLocation(memory.getSeeNavPoint().location);
                  //FIX - assume nearest Navpoint path is not blocked
                  //body.runToNavPoint(gameMap.nearestNavPoint(memory.getAgentLocation()));
                  //runType = "runto";
                }
                else {
                  body.turnHorizontal(180);
                }
                //if we're stuck trying to get a weapon, forget about for a while(default timer length=40 sec)
                /*if (weaponGet != null) {
                  timerMap.put(Integer.toString(weaponGet.ID), new Double (Double.toString(gameTime)));
                  weaponGet = null;
                }*/
                
                //FIX - timer getting taken to often with broken paths
                /*if (healthGet != null) {
                  timerMap.put(Integer.toString(healthGet.ID), new Double (Double.toString(gameTime)));
                  healthGet = null;
                }*/

                //Triple campFocusLocation = new Triple (-1520.0,1450.0,-207.0);
                //body.turnToLocation(campFocusLocation);

                // nullify chosen navigation point and chose it during the
                // next iteration of the logic
                lastNavigationPoint = chosenNavigationPoint;
                chosenNavigationPoint = null;
            }

        }
//log.info("debugMain3");
        } //tripFlag


}

protected void pathSuccess() {
    // 1) we're at the navpoint
    log.info("station:"+chosenNavigationPoint.UnrealID.toString());
    gameMap.resetPath();

    //if we're stuck trying to get a weapon, forget about for a while(default timer length=40 sec)
    if (weaponGet != null) {
      timerMap.put(Integer.toString(weaponGet.ID), new Double (Double.toString(gameTime)));
      weaponGet = null;
    }
    if (healthGet != null) {
      timerMap.put(Integer.toString(healthGet.ID), new Double (Double.toString(gameTime)));
      healthGet = null;
    }
    if (ammoGet != null) {
      timerMap.put(Integer.toString(ammoGet.ID), new Double (Double.toString(gameTime)));
      ammoGet = null;
    }

    lastNavigationPoint = chosenNavigationPoint;
    chosenNavigationPoint = null;
}

     /**
     * changes to better weapon that he posseses
     */
    @SuppressWarnings("static-access")
    protected void stateChangeToBetterWeapon() {
        log.log(Level.INFO, "Decision is: CHANGE WEAPON");
        if (memory.getAgentLocation() == null || memory.getSeeEnemy() == null || memory.getSeeEnemy().location == null) {
            return;
        }
        AddWeapon weapon = memory.getBetterWeapon(memory.getAgentLocation(), memory.getSeeEnemy().location);
        if (weapon != null) {
            sleep(200,100);
            body.changeWeapon(weapon);
        }
    }

    /**
     * has better weapon - this magic check goes through weapons in inventory and according to their characteristics
     * decides which is the best - that means which effectiveDistance is lowest and which maximal distance is big enough
     * to reach enemy.
     * </p>
     * <p>
     * Note!: Both effective and maximal distance are guessed and therefore could not work exactly
     * </p>
     */
        protected boolean hasBetterWeapon() {
        Player thisEnemy = memory.getSeeEnemy();
        if (memory.getAgentLocation() == null || thisEnemy == null || thisEnemy.location == null) {
            return false;
        }
        AddWeapon weapon = memory.getBetterWeapon(memory.getAgentLocation(), thisEnemy.location);
        // platformLog.info("Better weapon : " + weapon + "\nWeapons: " + memory.getAllWeapons().toString());
        if (weapon == null) {
            return false;
        } else {
            return true;
        }
    }
    /**
     * Fired when bot see any enemy.
     * <ol>
     * <li> if have enemyID - checks whether the same enemy is visible, if not, drop him (and stop shooting)
     * <li> if doesn't have enemyID - pick one of the enemy for pursuing
     * <li> if not shooting at enemyID - start shooting
     * <li> if out of ammo - switch to another weapon
     * <li> if enemy is reachable - run to him
     * <li> if enemy is not reachable - stand still (kind a silly, right? :-)
     * </ol>
     */
    protected void stateEngage() {
        log.log(Level.INFO, "Decision is: ENGAGE");

        /*
        // 1) if have enemyID - checks whether the same enemy is visible, if not, drop ID (and stop shooting)
        if (enemy != null) {
            lastEnemy = enemy;
            enemy = memory.getSeePlayer(enemy.ID); // refresh information about the enemy,
            // note that even though we've got pointer to the message of the enemy seen, it's still a certain message
            // from a specific time - when new message arrives it's written as a new message
            if (enemy == null) {
                if (memory.isShooting()) {
                    body.stopShoot();
                } // stop shooting, we've lost target
                return;
            }
        }

        // 2) if doesn't have enemy - pick one of the enemy for pursuing
        if (enemy == null) {
            enemy = memory.getSeeEnemy();
            if (enemy == null) {
                body.stop();
                body.stopShoot();
                return;
            }
        }
*/

        log.info("enemy:"+enemy.name.toString());

        AddWeapon weapon = null;
        // 3) if out of ammo - switch to another weapon
        if ((!memory.hasLoadedWeapon()) && memory.hasAnyLoadedWeapon()) {
            platformLog.info("no ammo - switching weapon " + memory.hasLoadedWeapon() + " " + memory.getAnyWeapon() + "\nCurrent Weapon:" + memory.getCurrentWeapon() + "\nWeapons : " + memory.getAllWeapons().toString());
            weapon = memory.getAnyWeapon();
            if ((weapon != null) && ((memory.getCurrentWeapon() == null) || ((memory.getCurrentWeapon() != null) && (!weapon.weaponType.equals(memory.getCurrentWeapon().weaponType))))) {
                platformLog.info("no ammo - switching weapon: " + weapon);
                body.changeWeapon(weapon);
            } else {

            }
        }

        // 4) if not shooting at enemyID - start shooting
        double distance = (Triple.distanceInSpace(memory.getAgentLocation(), enemy.location));
        if (memory.getCurrentWeapon() != null && memory.getCurrentWeapon().maxDist > distance) {// it is worth shooting
            platformLog.info("Would like to shoot at enemy!!!");
            if (!memory.isShooting()) {
                sleep(200,100);
                body.shoot(enemy);
                //log.info("start shooting");
            } else // to turn to enemy - shoot will not turn to enemy during shooting
            {
                body.turnToTarget(enemy);
                //log.info("start turning");
            }
        }

        if (shouldEngageClose) { stateEngageClose(); }

      }


 protected void stateEngageClose() {
        //if (shouldHide) { return; } //don't close distance, follow hide navigation
        // 5) if enemy is far - run to him
        //int decentDistance = Math.round(random.nextFloat() * 800) + 400;
        int decentDistance = Math.round(random.nextFloat() * 800) + 4000; //was +2000

        if (memory.getAgentLocation() != null && enemy != null && enemy.location != null &&
                Triple.distanceInSpace(memory.getAgentLocation(), enemy.location) < decentDistance) {
            //if (memory.isMoving()) {
            if (1 == 1) {
                //body.stop();

            // compute new agent location (to where to run to)
            // note: resolves strafing and pick-ups
            Triple newAgentLocation = getStrafeAroundLocation (enemy);
            // strafe to the new location and look at the new focal point..
            body.strafeToLocation(newAgentLocation, enemy.location);
            }
            else { log.info("not moving"); }
        } else {
            log.info("runToTargetDistance:"+decentDistance);
            body.runToTarget(enemy);
            jumped = false;
        }
 }


    /**
     * Fired when bot loose enemy from his view <br>
     * He just stops shooting and no more wastes his ammo
     */
    protected void stateStopShooting() {
        log.log(Level.INFO, "Decision is: STOP_SHOOTING");
        body.stopShoot();
    }

     /**
     * Fired when bot is damaged, it has those options:
     * <ol>
     * <li> He has idea where to turn to from to DAM message
     * <li> He got no idea at all -> turns around
     * </ol>
     */
    protected void stateHit() {
        if (turnTime == 0.0) {
            turnTime = gameTime;
        }
        double waitTimeHit = 0.0;
        //only perform turn if within last waitTimeHit seconds
        if (turnTime >= (gameTime - waitTimeHit)) {
            log.log(Level.INFO, "Decision is: HIT");
            body.turnHorizontal(180);
                int myRand = random.nextInt(3)+1;

                if (myRand == 1) {
                  log.info("dodge right");
                  Triple dodgeDirection = new Triple (0.0,1.0,0.0);
                  body.dodge(dodgeDirection);
                }
                if (myRand == 2) {
                  log.info("dodge left");
                  Triple dodgeDirection = new Triple (0.0,-1.0,0.0);
                  body.dodge(dodgeDirection);
                }
                if (myRand == 3) {
                  log.info("jump");
                  body.jump();
                }
        }
        else {
            log.log(Level.INFO, "HIT ignored");
            if (turnTime+3.0 < (gameTime - waitTimeHit)) {
              turnTime = 0.0;
            }
        }
    }

        /**
     * State pursue is for pursuing enemy who was for example lost behind a corner.
     * How it works?:
     * <ol>
     * <li> initialize properties
     * <li> obtain path to the enemy
     * <li> follow the path - if it reaches the end - set lastEnemy to null - bot would have seen him before or lost him once for all
     * </ol>
     */
    protected void statePursue() {
        log.log(Level.INFO, "Decision is: PURSUE");
        //statePursue = true;
        /*if (memory.getSeeAnyReachableNavPoint()) {
          runType = "runto";
          chosenNavigationPoint = memory.getSeeReachableNavPoint();
        }*/

        if (!statePursue) {
            statePursue = true;
            gameMap.resetPath();

            chosenNavigationPoint = gameMap.nearestNavPoint(lastEnemy.location);

            //chosenNavigationPoint = memory.getKnownNavPoints().get(0);
            chosenNavigationPoint.location = lastEnemy.location;
            log.info("pursue navpoint:"+chosenNavigationPoint.UnrealID.toString());
            log.info("pursue location:"+chosenNavigationPoint.location.toString());
        }

        //if (!this.gameMap.safeRunToLocation(lastEnemy.location)) {
        if (Triple.distanceInSpace(memory.getAgentLocation(), chosenNavigationPoint.location) < pickupDistance + 100) {
          log.info("Ended at the enemy position or failed - > STOP THE CHASE.");
          previousChoosenItem = choosenItem;
          lastEnemy = null;
          statePursue = false;
        }
        return;
    }

    /**
     * Fired when bot is moving, checks few accidents than can happen to him
     * <ol>
     * <li> Wall collision
     * <li> Fell of the bot
     * <li> Bump to another actor of the game
     * </ol>
     */
        protected void stateWalking() {
        log.log(Level.INFO, "Decision is: WALKING");

        if (memory.isColliding()) {
            if (!jumped) {
                body.doubleJump();
                jumped = true;
            } else {
                body.stop();
                jumped = false;
            }
        }
        if (memory.isFalling()) {
            body.sendGlobalMessage("I am flying like a bird:D!");
            log.info("I'm flying like an angel to the sky ... it's so high ...");
        }
        if (memory.isBumpingToAnotherActor()) {
            body.stop();
        }
    }

   /**
     * choose weapon according to the one he is currently holding
     * <ol>
     * <li> has melee and see ranged => pick up ranged
     * <li> has ranged and see melee => pick up melee
     * <li> pick up first weapon he sees
     * </ol>
     *
     * @return the choosen one weapon
     */
    private Weapon chooseWeapon() {
        ArrayList<Weapon> weapons = memory.getSeeReachableWeapons();
        for (Weapon weapon : weapons) {
            // 0) has no weapon in hands
            if (memory.getCurrentWeapon() == null) {
                return weapon;
            }
            // 1) weapon is ranged, bot has melee
            if ((memory.getCurrentWeapon().melee) && !weapon.isMelee() && !memory.hasWeaponOfType(weapon.weaponType)) {
                return weapon;
            }
            // 2) weapon is melee, bot has ranged
            if (!memory.getCurrentWeapon().melee && weapon.isMelee() && !memory.hasWeaponOfType(weapon.weaponType)) {
                return weapon;
            }
        }
        Weapon chosen = memory.getSeeReachableWeapon();
        if (!memory.hasWeaponOfType(chosen.weaponType)) {
            return chosen;
        }
        return null;
    }

    /**
     * Reasoning about what to do with seen item <br>
     * the easiest way of handeling it will be just to take it every time, but what should we do
     * when there are many of items laying in front of agent?
     * <ol>
     * <li> choose weapon - choose the type he is lacking (melee/ranged)
     * <li> choose armor
     * <li> choose health - if the health is bellow normal maximum
     * <li> choose ammo - if it is suitable for possessed weapons
     * <li> ignore the item
     * </ol>
     */

    private Item chooseItem() {
        if (memory.getSeeAnyReachableExtra()) {
            return memory.getSeeExtra();
        }

        // 1) choose weapon - choose the type he is lacking (melee/ranged)
        if (memory.getSeeAnyReachableWeapon()) {
            return chooseWeapon();
        }
        // 2) choose armor
        if (memory.getSeeAnyReachableArmor()) {
            return memory.getSeeReachableArmor();
        }
        // 3) choose health - if the health is bellow normal maximum or the item is boostable
        if (memory.getSeeAnyReachableHealth()) {
            Health health = memory.getSeeReachableHealth();
            if (memory.getAgentHealth() < 199) {
                return health;
            }
            if (health.boostable) // if the health item is boostable, grab it anyway:)
            {
                return health;
            }
        }
        // 4) choose ammo - if it is suitable for possessed weapons
        if ((memory.getSeeAnyReachableAmmo()) &&
                (memory.isAmmoSuitable(memory.getSeeReachableAmmo()))) {
            return memory.getSeeReachableAmmo();
        }
        // 5) ignore the item
        return null;
    }

    /**
     * sees reachable item and wants it
     * @return true if there is an item which is useful for agent
     */
    private boolean seeAnyReachableItemAndWantIt() {
        if (choosenItem == null) {
        if (memory.getSeeAnyReachableItem()) {
            choosenItem = chooseItem();
            if (choosenItem != null) {
                //log.info("NEW ITEM CHOSEN: " + choosenItem);
                //log.info("LAST CHOOSEN ITEM: " + previousChoosenItem);
            }
        } else {
            choosenItem = null;
        }
        }
        
        if ((choosenItem != null) && (!choosenItem.equals(previousChoosenItem))) //&& (Triple.distanceInSpace(memory.getAgentLocation(), choosenItem.location) > 20))
        {
            return true;
        } else {
            return false;
        }
    }


    /**
     * run along the path to choosen item
     */
    protected void stateSeeItem() {
        log.log(Level.INFO, "Decision is: SEE_ITEM --- Running for: " + choosenItem.toString());
        if (!gameMap.safeRunToLocation(choosenItem.location)) {         // unable to reach the choosen item
            log.info("unable to REACH the choosen item");
            previousChoosenItem = choosenItem;
            choosenItem = null;
        }
        jumped = false;
    }
       /**
     * checks whether there are any medkit items around and if there are
     * checks if the agent is not standing on the first one in the choosenMedKits
     * <p>
     * (bot got stucked because nearestHealth returns Healths according to inventory spots
     * not to the current situation, so the bot with low health got stucked on the inventory spot)
     * <p>
     * @return true if bot can run along med kits - initialize them before that
     */
    protected boolean canRunAlongMedKit() {
        if (choosenMedKits == null) {
            choosenMedKits = gameMap.nearestHealth(4, 8);
        }
        // no medkits to run to around the agent - restricted AStar - see nearestHealth
        if (choosenMedKits.isEmpty()) {
            choosenMedKits = null;
            return false;
        }
        // bot is too close to the object - possibly standing at the only one
        if (Triple.distanceInSpace(choosenMedKits.get(0).location, memory.getAgentLocation()) < 15) {
            // there are many - remove the first one - seeItem has highest priority, so bot should
            // pick up the item anyway and otherwise will not get stucked at the inventory spot of
            // the item
            if (choosenMedKits.size() > 2) {
                choosenMedKits.remove(0);
            } else {
                choosenItem = null;
                return false;
            }
        }
        return true;
    }

    protected boolean canRunAlongWeapons() {

        ArrayList<Weapon> weaponList = new ArrayList<Weapon>();
        ArrayList<Weapon> weaponListGood = new ArrayList<Weapon>();

        weaponList = memory.getKnownWeapons();

        for (Weapon thisWeapon : weaponList) {
            //log.info("thisWeapon:"+thisWeapon.getWeaponType().toString());
            //move on if we have this weapon already
            if (memory.hasWeaponOfType(thisWeapon.getWeaponType())) { continue; }
            //log.info("thisWeapon:"+thisWeapon.getWeaponType().toString());
            //log.info("thisWeaponOrdinal:"+thisWeapon.getWeaponType().ordinal());
            double weaponTime = gameTime;
            boolean keyFlag = false;
            if (timerMap.containsKey(Integer.toString(thisWeapon.ID))) {
              keyFlag = true;
              //log.info("containsKey");
              Object lookup = timerMap.get(Integer.toString(thisWeapon.ID));
              weaponTime = Double.parseDouble(lookup.toString());
              //expired
              if (gameTime >= weaponTime+weaponWaitTime) { timerMap.remove(Integer.toString(thisWeapon.ID)); keyFlag = false; }
            }
            else {
              //log.info("nokey");
              if (Triple.distanceInSpace(memory.getAgentLocation(),thisWeapon.location) < pickupDistance) {
              //log.info("addkey:"+gameTime+":"+thisWeapon.ID);
              timerMap.put(Integer.toString(thisWeapon.ID), new Double (Double.toString(gameTime)));
              }
            }

            //if (gameTime <= weaponTime+60) { continue; }
            if (keyFlag) { continue; }

            //log.info("gameTime:"+gameTime+":weaponTime:"+weaponTime);

            //move on if weapon not present at pickup
/*            if (Triple.distanceInSpace(memory.getAgentLocation(),thisWeapon.location) < 15) { continue; }

                  Object lookup = navMap.get(Integer.toString(navID));
      int navLookup = Integer.parseInt(lookup.toString());
      return navLookup;

  */

            //log.info("getWeapon:"+thisWeapon.getWeaponType().toString());
            weaponListGood.add(thisWeapon);
            //gameMap.safeRunToLocation(thisWeapon.location);
            //chosenNavigationPoint.location = thisWeapon.location;
            //return true;
        }

        //get closest available weapon
        //Weapon weaponGet = null;
        double minWeaponDistance = 30000.0;
        for (Weapon thisWeapon : weaponListGood) {
            double weaponDistance = Triple.distanceInSpace(memory.getAgentLocation(),thisWeapon.location);
            if (weaponDistance < minWeaponDistance) {
                minWeaponDistance = weaponDistance;
                weaponGet = thisWeapon;
            }
        }

        if (weaponGet != null) {
          //log.info("getWeapon:"+weaponGet.getWeaponType().toString());
          //chosenNavigationPoint = memory.getKnownNavPoints().get(0);
          //chosenNavigationPoint.location = weaponGet.location;
          return true;
          /*if(gameMap.safeRunToLocation(weaponGet.location)) {
              if (Triple.distanceInSpace(memory.getAgentLocation(),weaponGet.location) < 50) {
                weaponGet = null;
              }
            return true;
           }
           */

        }

        return false;
    }
/*
        if (choosenItems == null) {
            choosenItems = gameMap.nearestItems(MessageType.WEAPON,3);
        }
        // no medkits to run to around the agent - restricted AStar - see nearestHealth
        if (choosenItems.isEmpty()) {
            choosenItems = null;
            return false;
        }
        // bot is too close to the object - possibly standing at the only one
        if (Triple.distanceInSpace(choosenItems.get(0).location, memory.getAgentLocation()) < 15) {
            // there are many - remove the first one - seeItem has highest priority, so bot should
            // pick up the item anyway and otherwise will not get stucked at the inventory spot of
            // the item
            if (choosenItems.size() > 2) {
                choosenItems.remove(0);
            } else {
                choosenItem = null;
                return false;
            }
        }
        return true;
 */

    protected boolean canRunAlongItemsHealth() {

    //probably can have just one function for multiple item types but cut&paste for now
        log.info("tryGetHealth");
        ArrayList<Health> healthList = new ArrayList<Health>();
        ArrayList<Health> healthListGood = new ArrayList<Health>();

        healthList = memory.getKnownHealths();

        for (Health thisHealth : healthList) {
            //log.info("thisWeapon:"+thisWeapon.getWeaponType().toString());
            //move on if we have this weapon already
            //if (memory.hasWeaponOfType(thisWeapon.getWeaponType())) { continue; }
            //log.info("thisWeapon:"+thisWeapon.getWeaponType().toString());
            double healthTime = gameTime;
            boolean keyFlag = false;
            if (timerMap.containsKey(Integer.toString(thisHealth.ID))) {
              keyFlag = true;
              //log.info("containsKey");
              Object lookup = timerMap.get(Integer.toString(thisHealth.ID));
              healthTime = Double.parseDouble(lookup.toString());
              //expired
              if (gameTime >= healthTime+40) { timerMap.remove(Integer.toString(thisHealth.ID)); keyFlag = false; }
            }
            else {
              //log.info("nokey");
              if (Triple.distanceInSpace(memory.getAgentLocation(),thisHealth.location) < pickupDistance) {
              //log.info("addkey:"+gameTime+":"+thisWeapon.ID);
              timerMap.put(Integer.toString(thisHealth.ID), new Double (Double.toString(gameTime)));
              }
            }

            //if (gameTime <= weaponTime+60) { continue; }
            if (keyFlag) { continue; }

            //log.info("gameTime:"+gameTime+":weaponTime:"+weaponTime);

            //move on if weapon not present at pickup
/*            if (Triple.distanceInSpace(memory.getAgentLocation(),thisWeapon.location) < 15) { continue; }

                  Object lookup = navMap.get(Integer.toString(navID));
      int navLookup = Integer.parseInt(lookup.toString());
      return navLookup;

  */

            //log.info("getWeapon:"+thisWeapon.getWeaponType().toString());
            healthListGood.add(thisHealth);
            log.info("addHealth");
            //gameMap.safeRunToLocation(thisWeapon.location);
            //chosenNavigationPoint.location = thisWeapon.location;
            //return true;
        }
                
        //get closest available weapon
        //Weapon weaponGet = null;
        double minHealthDistance = 30000.0;
        for (Health thisHealth : healthListGood) {
            double healthDistance = Triple.distanceInSpace(memory.getAgentLocation(),thisHealth.location);
            if (healthDistance < minHealthDistance) {
                minHealthDistance = healthDistance;
                healthGet = thisHealth;
            }
        }


        if (healthGet != null) {
          //log.info("getHealth:"+healthGet.getWeaponType().toString());
          //chosenNavigationPoint = memory.getKnownNavPoints().get(0);
          //chosenNavigationPoint = healthGet.navPoint;
          log.info("gotHealth");
          //chosenNavigationPoint.location = healthGet.location;
          return true;
          /*if(gameMap.safeRunToLocation(weaponGet.location)) {
              if (Triple.distanceInSpace(memory.getAgentLocation(),weaponGet.location) < 50) {
                weaponGet = null;
              }
            return true;
           }
           */

        }

        return false;
    }

protected boolean canRunAlongItemsAmmo() {

//probably can have just one function for multiple item types but cut&paste for now
    log.info("tryGetAmmo");
    ArrayList<Ammo> ammoList = new ArrayList<Ammo>();
    ArrayList<Ammo> ammoListGood = new ArrayList<Ammo>();

    ammoList = memory.getKnownAmmos();

    for (Ammo thisAmmo : ammoList) {
        double ammoTime = gameTime;
        boolean keyFlag = false;
        if (timerMap.containsKey(Integer.toString(thisAmmo.ID))) {
          keyFlag = true;
          //log.info("containsKey");
          Object lookup = timerMap.get(Integer.toString(thisAmmo.ID));
          ammoTime = Double.parseDouble(lookup.toString());
          //expired
          if (gameTime >= ammoTime+40) { timerMap.remove(Integer.toString(thisAmmo.ID)); keyFlag = false; }
        }
        else {
          //log.info("nokey");
          if (Triple.distanceInSpace(memory.getAgentLocation(),thisAmmo.location) < pickupDistance) {
          timerMap.put(Integer.toString(thisAmmo.ID), new Double (Double.toString(gameTime)));
          }
        }

        if (keyFlag) { continue; }

        ammoListGood.add(thisAmmo);
        log.info("addAmmo");
    }

    //get closest available ammo
    double minAmmoDistance = 30000.0;
    for (Ammo thisAmmo : ammoListGood) {
        double ammoDistance = Triple.distanceInSpace(memory.getAgentLocation(),thisAmmo.location);
        if (ammoDistance < minAmmoDistance) {
            minAmmoDistance = ammoDistance;
            ammoGet = thisAmmo;
        }
    }

    if (ammoGet != null) {
      //log.info("getAmmo:"+ammoGet.getWeaponType().toString());
      //chosenNavigationPoint = ammoGet.navPoint;
      log.info("gotAmmo");
      return true;
    }

    return false;
}


    protected boolean getReachableWeapons() {

        ArrayList<Weapon> weaponList = new ArrayList<Weapon>();
        ArrayList<Weapon> weaponListGood = new ArrayList<Weapon>();

        //log.info("debugGet2");
        weaponList = memory.getSeeReachableWeapons();
        if (weaponList.isEmpty()) { return false; }

        //log.info("debugGet1");
        for (Weapon thisWeapon : weaponList) {
            //move on if we have this weapon already
            //FIX - check weather want weapon or not - greedy for now
            //if (memory.hasWeaponOfType(thisWeapon.getWeaponType())) { continue; }

            //log.info("thisWeapon:"+thisWeapon.getWeaponType().toString());
            //log.info("autotrace:"+memory.getAutoTrace(thisWeapon.ID));
            //don't go for weapon if too far away
            double weaponDistance = Triple.distanceInSpace(memory.getAgentLocation(),thisWeapon.location);
            if (weaponDistance < 1000) {
              weaponListGood.add(thisWeapon);
            }
        }

        //get closest available weapon
        //Weapon weaponGet = null;
        double minWeaponDistance = 30000.0;
        for (Weapon thisWeapon : weaponListGood) {
            double weaponDistance = Triple.distanceInSpace(memory.getAgentLocation(),thisWeapon.location);
            if (weaponDistance < minWeaponDistance) {
                minWeaponDistance = weaponDistance;
                weaponGetPickup = thisWeapon;
                //log.info("thisWeaponGet:"+weaponGet.getWeaponType().toString());
            }
        }

        if (weaponGetPickup != null) {
          //body.runToLocation(weaponGet.location);
          runType = "runto";

          //log.info("getWeaponPickup:"+weaponGetPickup.getWeaponType().toString());
          //chosenNavigationPoint = memory.getKnownNavPoints().get(0);
          //chosenNavigationPoint.location = weaponGetPickup.location;
          return true;
          /*if(gameMap.safeRunToLocation(weaponGet.location)) {
              if (Triple.distanceInSpace(memory.getAgentLocation(),weaponGet.location) < 50) {
                weaponGet = null;
              }
            return true;
           }
           */
        }

        return false;
    }

    /**
     * runs along healths of strength at least 8 to recover health
     */
    protected void stateMedKit() {
        log.log(Level.INFO, "Decision is: RUN_MED_KITS:"+memory.getAgentHealth());
        gameMap.runAroundItemsInTheMap(choosenMedKits, useAStar);
    }

    protected void findItem() {
        log.log(Level.INFO, "Decision is: findItem");
        gameMap.runAroundItemsInTheMap(choosenItems, useAStar);
    }

   /*========================================================================*/

    /**
     * Computes the best location that should be used as strafing destination
     * while dancing around given enemy during a combat. Agent and enemy weapons
     * are considered to choose the location. Nearby health packs, vials and
     * armors are picked up along the way.
     *
     * <h4>Pogamut troubles</h4>
     *
     * How about using autotrace rays for scanning the ground around? Usage of
     * one autotrace ray pointed to the direction where the agent is aiming
     * might help prevent rocketry suicides. Well, it could, if the autotrace
     * were working as it was supposed to. For now, it causes more trouble than
     * it helps.
     *
     * <h4>Future</h4>
     *
     * What about foraging nearby healths? Check the perimeter and pick them up.
     * This could be done easily by comparing the calculated strafing vector
     * with the vectors of nearby reachable items. Should the angle between the
     * vectors be small enough, strafe to the item instead of the calculated
     * strafing point.
     *
     * <p>There is one pitfall to this however: The closer the items are to the
     * agent, the bigger might their angle-between-the-vectors be. Paradoxicaly:
     * the closer the item is, the more the angle starts to raise. And the vial
     * gets to be more attractive. In results, comparing the vectors only is
     * not good enough. Distance must be taken into consideration and tweaked
     * into a reasonable condition with the vectors angle.</p>
     *
     * @param enemy Enemy, which to dance around.
     * @return Strafing location to where to strafe to while wrestling.
     */
    protected Triple getStrafeAroundLocation (Player enemy)
    {
        // this is used for debugging purposes
        //if (main._DEBUGLocation != null) return main._DEBUGLocation;

        //double desiredEnemyDistance = (random.nextFloat() * 400) + 150;
        //double strafingAmount = (random.nextFloat() * 70) + 80;
        double desiredEnemyDistance = (random.nextFloat() * 200) + 400;
        double strafingAmount = (random.nextFloat() * 140) + 160;
        //double desiredEnemyDistance = 200;
        //double strafingAmount = 100;

        //random strafe direction change?
        int myRand = random.nextInt(3)+1;
        if (myRand == 1) { strafingRight = !strafingRight; }


/*        // primary fire mode
        if (!alternateFire)
        {
            desiredEnemyDistance = currentWeaponInfo.priIdealCombatRange;
            strafingAmount = currentWeaponInfo.priStrafingAmount;
        }
        // alternate fire mode
        else
        {
            desiredEnemyDistance = currentWeaponInfo.altIdealCombatRange;
            strafingAmount = currentWeaponInfo.altStrafingAmount;
        }
 */
        // get agent location from memory
        //Triple agentLocation = memory.self.getLocation ();
        Triple agentLocation = memory.getAgentLocation();

        // get location and velocity of enemy
        Triple enemyLocation = enemy.location;
        Triple enemyVelocity = enemy.velocity;

        // update the enemy location by its velocity
        enemyLocation = Triple.add(
            enemyLocation,
            //Triple.multiplyByNumber(enemyVelocity, 1/main.logicFrequency)
            Triple.multiplyByNumber(enemyVelocity, 1/5.0)
        );

        // compute planar direction to the enemy
        // howto: substract the two locations, remove z-axis, normalize
        Triple enemyDirection = Triple.subtract(enemyLocation, agentLocation);
        // remove z-axis
        enemyDirection.z = 0;
        // and normalize it
        enemyDirection = enemyDirection.normalize ();

        // compute distance to the enemy
        double enemyDistance = Triple.distanceInSpace(enemyLocation, agentLocation);

        // compute orthogonal direction to the enemy
        Triple enemyOrthogonal = new Triple (enemyDirection.y, -enemyDirection.x, 0);

        // decide, how much to move forward
        double moveForward = enemyDistance - desiredEnemyDistance;

        // decide, how much and where to strafe
        double moveStrafe = strafingRight ? strafingAmount : -strafingAmount;
        //log.info("strafing:"+moveStrafe);
        // decide where to move..
        Triple moveDirection = moveDirection = Triple.add (
            // move forward/backward..
            Triple.multiplyByNumber (enemyDirection, moveForward),
            // and strafe to side along the way
            Triple.multiplyByNumber (enemyOrthogonal, moveStrafe)
        );

        // finally, add moving vector to current agent location
        return Triple.add(agentLocation, moveDirection);
    }

    @Override
    @SuppressWarnings("static-access")
    public void receiveMessage(RcvMsgEvent e) {
        // DO NOT DELETE! Otherwise things will screw up! Agent class itself is also using this listener...
        super.receiveMessage(e);

        if (e.getMessage().type.toString().equals("NAV_POINT")) { return; }
        if (e.getMessage().type.toString().equals("DELETE_FROM_BATCH")) { return; }
        if (e.getMessage().type.toString().equals("WEAPON")) { return; }
        if (e.getMessage().type.toString().equals("CHANGE_WEAPON")) { return; }
        if (e.getMessage().type.toString().equals("CHANGED_WEAPON")) { return; }
        if (e.getMessage().type.toString().equals("ITEM")) { return; }
        if (e.getMessage().type.toString().equals("AMMO")) { return; }
        if (e.getMessage().type.toString().equals("MOVER")) { return; }
        //if (e.getMessage().type.toString().equals("BEGIN")) { return; }
        if (e.getMessage().type.toString().equals("END")) { return; }
        if (e.getMessage().type.toString().equals("GAME_STATUS")) { return; }
        if (e.getMessage().type.toString().equals("SELF")) { return; }
        if (e.getMessage().type.toString().equals("HEALTH")) { return; }
        if (e.getMessage().type.toString().equals("ADD_ITEM")) { return; }
        if (e.getMessage().type.toString().equals("ADD_AMMO")) { return; }
        if (e.getMessage().type.toString().equals("ADD_HEALTH")) { return; }
        if (e.getMessage().type.toString().equals("ADD_SPECIAL")) { return; }
        if (e.getMessage().type.toString().equals("ADD_WEAPON")) { return; }
        if (e.getMessage().type.toString().equals("ADRENALINE_GAINED")) { return; }
        if (e.getMessage().type.toString().equals("ARMOR")) { return; }
        if (e.getMessage().type.toString().equals("SPECIAL")) { return; }
        if (e.getMessage().type.toString().equals("HEAR_NOISE")) { return; }
        if (e.getMessage().type.toString().equals("HEAR_PICKUP")) { return; }
        if (e.getMessage().type.toString().equals("PLAYER")) { return; }
        if (e.getMessage().type.toString().equals("PATH")) { return; }

        if (e.getMessage().type.toString().equals("SEE_PLAYER")) { return; }

        if (!(e.getMessage().type.toString().equals("BEGIN"))) {
            getLogger().info("message: " + e.getMessage().type.toString());
        }
        // Take care of frags and deaths.
        switch (e.getMessage().type) {
            case PLAYER_KILLED:
              PlayerKilled pk;

              pk = (PlayerKilled) e.getMessage();

              /*if (pk.killerID == getMemory().getAgentID()) {
                frags += 1;
                //getLogger().info("pk: "+pk.killerID+":"+lastEnemy.ID+":"+pk.ID+":"+memory.getAgentID());
                try { dbWrite(memory.getGameInfo().level.toString(),chosenNavigationPoint.getID(),memory.getKnownNavPoints().indexOf(chosenNavigationPoint),chosenNavigationPoint.location.toString(),chosenNavigationPoint.UnrealID.toString(),memory.getAgentLocation().toString(),gameTime, 1); } catch (Exception K) { System.out.println("error:dbWrite"); }
              }*/

              //frags += 1;

              //make sure killer was this bot
              if (pk.killerID == memory.getAgentID()) {

                frags += 1;
                //getLogger().info("pk: "+pk.killerID+":"+lastEnemy.ID+":"+pk.ID+":"+memory.getAgentID());
                //try { dbWrite(memory.getGameInfo().level.toString(),chosenNavigationPoint.getID(),memory.getKnownNavPoints().indexOf(chosenNavigationPoint),chosenNavigationPoint.location.toString(),chosenNavigationPoint.UnrealID.toString(),memory.getAgentLocation().toString(),gameTime, 1); } catch (Exception K) { System.out.println("error:dbWrite"); }

                //to correct bot pursuing after dead enemy disappears
                //log.info("got'EM");
                lastEnemy = null;
                statePursue = false;

                try { dbInsertKillScore(enemy.name.toString(),enemy.location.toString(),pk.damageType,botName,memory.getAgentLocation().toString()); } catch (Exception K) { System.out.println("error:dbInsertKillScore"); }
              }

              break;
            case BOT_KILLED:
              BotKilled bk;

              bk = (BotKilled) e.getMessage();
              //if (bk.killerID == getMemory().getAgentID()) {
                deaths += 1;

                String enemyName = "";
                if (enemy != null) { enemyName = enemy.name.toString(); }
                //getLogger().info("bk: " + bk.killerID);
                Object lookup = nameMap.get(Integer.toString(bk.killerID));
                String killerName = "";
                if (lookup != null) {
                    //getLogger().info("bk: " + lookup.toString());
                    killerName = lookup.toString();
                    
                    if (behavior.equals("tide")) {
                      try { dbSwitchSquad(enemyName); } catch (Exception K) { System.out.println("error:dbSwitchSquad"); }
                    }
                }
                
                //will log killerLocation if same as current enemy(could be other sniper,etc)
                String killerLocation = "";
                if (!enemyName.equals("") && killerName.equals(enemyName)) { killerLocation = enemy.location.toString(); }
                //log.info("bk: "+bk.UnrealID.toString());
                //getLogger().info("location:"+memory.getAgentLocation().toString());

                try { dbInsertKillScore(botName,memory.getAgentLocation().toString(),bk.damageType,killerName,killerLocation); } catch (Exception K) { System.out.println("error:dbInsertKillScore"); }

                //probably should avoid places killed, but trying returning to the scene of the crime for now
                //try { dbWrite(memory.getGameInfo().level.toString(),chosenNavigationPoint.getID(),memory.getKnownNavPoints().indexOf(chosenNavigationPoint),chosenNavigationPoint.location.toString(),chosenNavigationPoint.UnrealID.toString(),memory.getAgentLocation().toString(),gameTime, 1); } catch (Exception K) { System.out.println("error:dbWrite"); }
              //}
              
              //bot process dies if manualSpawn is true
              if (manualSpawn) {
                try { exitBot(); } catch (Exception K) { System.out.println("error:exitBot"); }
                if (killBot) { System.exit(0); }
              }
 
              break;
            case GLOBAL_CHAT:
              GlobalChat gc;

              gc = (GlobalChat) e.getMessage();
              getLogger().info("Message: " + gc.string);
              break;
            case BEGIN:
              BeginMessage begin;

              begin = (BeginMessage) e.getMessage();
              //getLogger().info("Message: " + begin.time);
              gameTime = begin.time;
              break;
            case WALL_COLLISION:
              strafingRight = !strafingRight;
              int myWCRand = random.nextInt(3)+1;
              if (myWCRand == 1) { body.jump(); }
              if (myWCRand == 2) { body.doubleJump(); }
              break;
            case INCOMMING_PROJECTILE:
                sleep(200,100);

                int myRand = random.nextInt(5)+1;
                if (myRand == 1) {
                  log.info("dodge right");
                  Triple dodgeDirection = new Triple (0.0,1.0,0.0);
                  body.dodge(dodgeDirection);
                }
                if (myRand == 2) {
                  log.info("dodge left");
                  Triple dodgeDirection = new Triple (0.0,-1.0,0.0);
                  body.dodge(dodgeDirection);
                }
                if (myRand == 3) {
                  log.info("jump");
                  body.jump();
                }

              break;
            case SPAWN:
              gameMap.resetPath();
              runType = null;
              weaponGetPickup = null;
              weaponGet = null;
              healthGet = null;
              ammoGet = null;
              break;
        }
    }


    /**
     * NOTE: this method MUST REMAIN DEFINED + MUST REMAIN EMPTY, due to technical reasons.
     */
    public static void main(String[] Args) {
    }

/*  public void dbWrite(String map_level, int nav_ID, int ID, String location, String UnrealID, String eventLocation, double eventTime, int eventWeight) throws Exception {

      Class.forName(sqliteClass);
      Connection conn = DriverManager.getConnection(sqliteDBPath);
      Statement stat = conn.createStatement();
      //stat.executeUpdate("drop table if exists obs;");
      //stat.executeUpdate("create table people (name, occupation);");
      PreparedStatement prep = conn.prepareStatement(
          "insert into obs(row_entry_date,map_level,map_id,navpoint_id,location,unreal_id,event_location,event_time,event_weight) values (datetime('now'),?,?,?,?,?,?,?,?);");

      log.fine("navpoint_ID="+ID);

      prep.setString(1, map_level);
      prep.setInt(2, nav_ID);
      prep.setInt(3, ID);
      prep.setString(4, location);
      prep.setString(5, UnrealID);
      prep.setString(6, eventLocation);
      prep.setDouble(7, eventTime);
      prep.setDouble(8, eventWeight);

      prep.addBatch();

      conn.setAutoCommit(false);
      prep.executeBatch();
      conn.setAutoCommit(true);

      conn.close();
  }
*/

  public void dbWriteNavpoint(String map_level, int from_navpoint_id, int to_navpoint_id) throws Exception {

      Class.forName(sqliteClass);
      Connection conn = DriverManager.getConnection(sqliteDBPath);
      Statement stat = conn.createStatement();

      log.info("dbWriteNavpoint:"+map_level+":"+from_navpoint_id+":"+to_navpoint_id);

      PreparedStatement prep = conn.prepareStatement(
          "insert into navpoint(row_entry_date,map_level,from_navpoint_id,to_navpoint_id,visibility) values (datetime('now'),?,?,?,1);");

      prep.setString(1, map_level);
      prep.setInt(2, from_navpoint_id);
      prep.setInt(3, to_navpoint_id);

      prep.addBatch();

      conn.setAutoCommit(false);
      prep.executeBatch();
      conn.setAutoCommit(true);

/*
      String sql = "insert into navpoint(row_entry_date,map_level,from_navpoint_id,to_navpoint_id,visibility) values (datetime('now'),'"+map_level+"',"+from_navpoint_id+","+to_navpoint_id+",1);";
      log.info("sql:"+sql);

      conn.setAutoCommit(false);
      stat.executeQuery(sql);
      conn.setAutoCommit(true);
 */

      conn.close();
  }

public boolean dbLoggedNavpoint(String map_level,int from_navpoint_id) throws Exception {

      //checks to see if this navpoint was logged on the database earlier or not

      Class.forName(sqliteClass);
      Connection conn = DriverManager.getConnection(sqliteDBPath);
      Statement stat = conn.createStatement();

      String sql = null;
      sql = "select from_navpoint_id from navpoint where from_navpoint_id = "+from_navpoint_id+" and map_level = '"+map_level+"' limit 1;";

      log.info("sql:"+sql);
      ResultSet rs = stat.executeQuery(sql);

      boolean flagFound = false;
      while (rs.next()) {
             flagFound = true;
      }

      rs.close();
      conn.close();

      return flagFound;

      }

public void setNavpoint(String behavior) throws Exception {

      //set an initial location default if null
      //FIX - more needed for earlier, navlog?
      /*if (lastNavigationPoint == null) {
        log.info("navpoint=null");
        lastNavigationPoint = memory.getKnownNavPoints().get(0);
        chosenNavigationPoint = memory.getKnownNavPoints().get(0);
      }
      */
    
      Class.forName(sqliteClass);
      Connection conn = DriverManager.getConnection(sqliteDBPath);
      Statement stat = conn.createStatement();
      String sql = null;

      String map_level = memory.getGameInfo().level;
      //use locations within past # seconds, else randomSearch
      double recentTime = gameTime - lastSeenWaitTime;  //120 sec earlier
      if (recentTime < 0) { recentTime = 0; }

      if (behavior.equals("normal")) {
        sql = "select navpoint_id,event_location from obs where row_entry_date > strftime('%Y-%m-%d %H:%M:%S','now','-2 minute') and event_time > "+recentTime+" and event_weight = 1 and map_level = '"+map_level+"' order by row_entry_date desc limit 1;";
      }
      if (behavior.equals("general") || behavior.equals("tide")) {
        if (botCommand.equals("none")) {
          sql = "select location from last_seen where 1=2;";
        }

        if (botCommand.equals("hunt")) {
            if (botCommandTarget.equals("")) {
              //hunt any nearest enemy target
              sql = "select unit_name,location,gametime from last_seen where row_entry_date > strftime('%Y-%m-%d %H:%M:%S','now','-2 minute') and gametime > "+recentTime+" and map_level = '"+map_level+"' and report_by_squad like '"+botSquad+"%' group by unit_name order by row_entry_date;";
            }
            else {
              //hunt specified target
              sql = "select location,gametime from last_seen where row_entry_date > strftime('%Y-%m-%d %H:%M:%S','now','-2 minute') and gametime > "+recentTime+" and map_level = '"+map_level+"' and unit_name = '"+botCommandTarget+"' and report_by_squad like '"+botSquad+"%' order by row_entry_date desc limit 1;";
            }
        }
        if (botCommand.equals("follow")) {

          //don't follow ourself if we are the target/lead
          if (botCommandTarget.equals(botName)) {
            sql = "select location from last_seen where 1=2;";
          }
          else {
            //FIX - need to add gametime,maplevel-init state?
            sql = "select location from bot where name = '"+botCommandTarget+"';";
          }
        }
      }
      else if (behavior.equals("camp")) {
         log.info("camp");
         sql = "select navpoint_id,event_location from obs where event_weight = 2 and map_level = '"+map_level+"' order by row_entry_date desc limit 3;";
      }
      else if (behavior.equals("hide")) {
         //log.info("hide");

         //default hide from last position if no enemy
         if (hideFromNav == -1 || enemy == null) { hideFromNav = knownNavLkp(lastNavigationPoint.ID); }

         //get a fix on the latest enemy position if available
         //log.info("debug0:"+memory.getSeeAnyEnemy()+":"+memory.getSeeAnyPlayer());
         if (seeTargetValue) {
             //lastEnemy = enemy;
             //enemy = memory.getSeePlayer(enemy.ID);
             /*if (memory.getSeeAnyPlayer()) {
               enemy = memory.getSeePlayer();
             }
             if (memory.getSeeAnyEnemy()) {
               enemy = memory.getSeeEnemy()r;
             }
              */

           hideFromNav = findClosestNavpoint(enemy.location);
           log.info("enemy:"+enemy.name.toString()+":"+memory.getKnownNavPoints().get(hideFromNav).UnrealID.toString());
         }

         sql = "select to_navpoint_id from navpoint where map_level = '"+map_level+"' and from_navpoint_id = "+memory.getKnownNavPoints().get(hideFromNav).ID+";";

         log.info("sql:"+sql);
         ResultSet rs = stat.executeQuery(sql);

         //tried but failed to assign using getArray
         //http://publib.boulder.ibm.com/infocenter/idshelp/v10/index.jsp?topic=/com.ibm.jdbc_pg.doc/jdbc126.htm
         //http://forums.sun.com/thread.jspa?threadID=523675

         //rs.next();

         ArrayList<Integer> ArrayToGet = new ArrayList<Integer> ();
         while (rs.next()) {
             //log.info("visList:"+rs.getInt(1));
             ArrayToGet.add(rs.getInt(1));
         }
         //ArrayToGet = (ArrayList<Integer>) rs.getArray(1)
         //for (int j=0; j<ArrayToGet.size(); j++) {
           //log.info("integer element = "+ArrayToGet.get(j).toString());
         //}

         //Object testVal = navMap.get("1");
         //log.info("testval:"+testVal.toString());

         //get navpoints from visible set
         //int myRandHide = random.nextInt(ArrayToGet.size());
         //int thisNavpoint = ArrayToGet.get(myRandHide);

         //get navpoints from non-visible set
         int hideNavFound = 0;
         int thisNavpoint = 0;

         if (!ArrayToGet.isEmpty()) {

           while (hideNavFound == 0) {
             int myRandHide = random.nextInt(memory.getKnownNavPoints().size());
             thisNavpoint = memory.getKnownNavPoints().get(myRandHide).ID;
             //log.info("thisNavpoint:"+thisNavpoint);
             if (ArrayToGet.contains(thisNavpoint)) {
               //log.info("navpoint is visible:"+memory.getKnownNavPoints().get(myRandHide).UnrealID.toString());
             }
             else { log.info("navpoint not visible:"+memory.getKnownNavPoints().get(myRandHide).UnrealID.toString()); hideNavFound = 1; }
           }
         }
         else {
           int myRandHide = random.nextInt(memory.getKnownNavPoints().size());
           thisNavpoint = memory.getKnownNavPoints().get(myRandHide).ID; 
         }

         //log.info("hide_navpoint:"+thisNavpoint);
         int intLookup = knownNavLkp(thisNavpoint);
         log.info("hide_navpoint:"+thisNavpoint+":"+intLookup);
         chosenNavigationPoint = memory.getKnownNavPoints().get(intLookup);

         //for (int i=0; i<memory.getKnownNavPoints().size(); i++) {
         //for (int i=0; i<10; i++) {
             //log.info(i+":"+memory.getKnownNavPoints().get(i).ID);
            //if (memory.getKnownNavPoints().get(i).ID == thisNavpoint) {
              //log.info("hide_navpoint:"+thisNavpoint+":"+i);
              //chosenNavigationPoint = memory.getKnownNavPoints().get(i);
            //}
         //}
        //}

      rs.close();
      conn.close();

      return;

      }
      else if (behavior.equals("navlog")) {

          if ((botYFocus == 4) || (lastNavigationPoint == null)) {

            botYFocus = 0;

            int navSize = memory.getKnownNavPoints().size();
            //int navSize = 130;

            if (navCount >= navSize) { navCount = 0; }  //navCount = 128;

            chosenNavigationPoint = memory.getKnownNavPoints().get(navCount);
            log.info("navpoint("+navCount+"/"+navSize+"):"+chosenNavigationPoint.UnrealID);

            navCount++;
            //skip catalogging jumppads since we can't sit on them to inventory
            while (memory.getKnownNavPoints().get(navCount).UnrealID.contains("JumpPad")) { navCount++; }
            //while (dbLoggedNavpoint(memory.getGameInfo().level.toString(),memory.getKnownNavPoints().get(navCount).ID)) { log.info("skipping navpoint:"+navCount); navCount++; }
            return;
          }
          else {

            chosenNavigationPoint = lastNavigationPoint;
            double botY = memory.getAgentRotation().y;

            if (botYFocus == 0) { botYMin = 0.0; botYMax = 2000.0; }
            if (botYFocus == 1) { botYMin = 16000.0; botYMax = 18000.0; }
            if (botYFocus == 2) { botYMin = 32000.0; botYMax = 34000.0; }
            if (botYFocus == 3) { botYMin = 48000.0; botYMax = 50000.0; }

            if ((botY < botYMin) || (botY > botYMax)) {
               body.turnHorizontal(5);
               return;
            }
            else {
              log.info("rotation:"+memory.getAgentRotation().y);
              ArrayList<NavPoint> testNavigationPoint = memory.getSeeNavPoints();
              for (int i=0; i<=testNavigationPoint.size()-1; i++) {
                if (testNavigationPoint.get(i).type.toString().equals("NAV_POINT")) {
                   log.info("navVis("+botYFocus+"/"+i+")"+testNavigationPoint.get(i).UnrealID);
                   //log.info(chosenNavigationPoint.getID()+":"+testNavigationPoint.get(i).getID());
                   try { dbWriteNavpoint(memory.getGameInfo().level.toString(),chosenNavigationPoint.getID(),testNavigationPoint.get(i).getID()); } catch (Exception K) { System.out.println("error:dbWriteNavpoint"); }
                }
              }
            }
          
            botYFocus++;

            return;
          }
        }
      //}

      log.info("sql:"+sql);
      ResultSet rs = stat.executeQuery(sql);

      String thisLocation = null;
      NavPoint testNavPoint = null;
      //below initialize line required or bot errors out
      testNavPoint = memory.getKnownNavPoints().get(0);
      chosenNavigationPoint = memory.getKnownNavPoints().get(0);

      if (botCommand.equals("hunt")) {
      double minEnemyDistance = 30000.0; //set to absolute large as possible

      while (rs.next()) {
          //assign rs vars out all at once - code didn't seem to like/allow assigning after rs_2 called
          String lastSeenName = rs.getString("unit_name");
          thisLocation = rs.getString("location");
          Double thisRowTime = rs.getDouble("gametime");

          //log.info("lastSeenName:"+lastSeenName);

          Double lastSeenKilledTime = 0.0;
 
          //killed check
          String sql_2 = "select gametime from kill_score where killed_name='"+lastSeenName+"' order by row_entry_date desc limit 1;";
          //log.info("sql_2:"+sql_2);
          ResultSet rs_2 = stat.executeQuery(sql_2);
          while (rs_2.next()) {
            lastSeenKilledTime = rs_2.getDouble("gametime");
            //log.info("lastSeenKilledTime:"+lastSeenKilledTime);
          }
 
          //log.info("lastSeenKilledTime:"+lastSeenKilledTime+":thisRowTime:"+thisRowTime);
          if (lastSeenKilledTime > thisRowTime) { continue; }


          //passed kill check
          String[] temp = null;
          temp = thisLocation.split(",");
          testNavPoint.location.x = Double.valueOf(temp[0]);
          testNavPoint.location.y = Double.valueOf(temp[1]);
          testNavPoint.location.z = Double.valueOf(temp[2]);

          double enemyDistance = Triple.distanceInSpace(memory.getAgentLocation(),testNavPoint.location);

          if (enemyDistance < minEnemyDistance) {
            minEnemyDistance = enemyDistance;
            chosenNavigationPoint.location = testNavPoint.location;
          }
      }
      }

      if (botCommand.equals("follow")) {
      
      while (rs.next()) {
        thisLocation = rs.getString("location");

          String[] temp = null;
          temp = thisLocation.split(",");
          testNavPoint.location.x = Double.valueOf(temp[0]);
          testNavPoint.location.y = Double.valueOf(temp[1]);
          testNavPoint.location.z = Double.valueOf(temp[2]);

          chosenNavigationPoint.location = testNavPoint.location;
          }
         //log.info("chosenNavPointLoc:"+chosenNavigationPoint.location.toString());
      }

      rs.close();
      conn.close();


      //default random, if database not populated
      if (thisLocation == null) {
          //chosenNavigationPoint = memory.getKnownNavPoints().get(navStep);
          //navStep++;
          //if (navStep == 2) {navStep=0;}
          chosenNavigationPoint = memory.getKnownNavPoints().get(random.nextInt(memory.getKnownNavPoints().size()));
          log.info("randomLocation:"+chosenNavigationPoint.UnrealID.toString());
      }
      else {

          log.info("thisLocation="+testNavPoint.location.toString());

/*          String[] temp = null;
          temp = thisLocation.split(",");

          chosenNavigationPoint.location.x = Double.valueOf(temp[0]);
          chosenNavigationPoint.location.y = Double.valueOf(temp[1]);
          chosenNavigationPoint.location.z = Double.valueOf(temp[2]);
*/

          chosenNavigationPoint = gameMap.nearestNavPoint(chosenNavigationPoint.location);
      //chosenNavigationPoint.location.x = 698.0;
      //chosenNavigationPoint.location.y = -94.0;
      //chosenNavigationPoint.location.z = -111.0;
      }

  }

public boolean initBot() throws Exception {

      Class.forName(sqliteClass);
      Connection conn = DriverManager.getConnection(sqliteDBPath);
      Statement stat = conn.createStatement();
      String sql = null;

      ///////////////////////////////////////////////
      //read bot table
      sql = "select row_id,name,skill,skin,team from bot where available = 1 limit 1;";
      //log.info("sql:"+sql);
      ResultSet rs = stat.executeQuery(sql);

      boolean flagFound = false;
      while (rs.next()) {
          flagFound = true;

          botId = rs.getInt(1);
          botName = rs.getString(2);
          botSkill = rs.getInt(3);
          botSkin = rs.getString(4);
          botTeam = rs.getInt(5);

          log.info("botName:"+botName);
      }

      if (!flagFound) { return false; }

      ///////////////////////////////////////////////
      //read unit_to_squad table
      //assume bot can only be member of one squad at a time
      sql = "select squad_name from unit_to_squad where unit_name = '"+botName+"';";
      //log.info("sql:"+sql);
      rs = stat.executeQuery(sql);

      while (rs.next()) {
          botSquad = rs.getString(1);
      }

      //botSquad defaults to 'none' if not set
      if (botSquad == null) { botSquad = "none"; }

      if (!botSquad.equals("none")) {
          //read squad table
          sql = "select skin from squad where squad_name = '"+botSquad+"';";
          //log.info("sql:"+sql);
          rs = stat.executeQuery(sql);

          while (rs.next()) {
              botSkin = rs.getString(1);
          }
      }

      ///////////////////////////////////////////////
      //read system table
      sql = "select manual_spawn,behavior from system_settings;";
      //log.info("sql:"+sql);
      rs = stat.executeQuery(sql);

      while (rs.next()) {
          if (rs.getInt(1) == 1) { manualSpawn = true; }
          behavior = rs.getString(2);
      }

      //close rs
      rs.close();

      ///////////////////////////////////////////////
      //remove available slot for this bot
      PreparedStatement prep = conn.prepareStatement(
      "update bot set available = 0 where row_id = ?;");

      prep.setInt(1, botId);

      prep.addBatch();
      conn.setAutoCommit(false);
      prep.executeBatch();
      conn.setAutoCommit(true);
      conn.close();
     
      return true;
}

public void exitBot() throws Exception {

      Class.forName(sqliteClass);
      Connection conn = DriverManager.getConnection(sqliteDBPath);
      Statement stat = conn.createStatement();

      //make available slot for this bot
      PreparedStatement prep = conn.prepareStatement(
      "update bot set available = 1 where row_id = ?;");

      prep.setInt(1, botId);

      prep.addBatch();
      conn.setAutoCommit(false);
      prep.executeBatch();
      conn.setAutoCommit(true);
      conn.close();

      //System.exit(0);
      killBot = true;
}


/* public int findClosestNavpoint(Triple thisLocation) {
 * finds closest getKnownNavPoints navpoint_id to a given location
 * can probably replace with gamemap.nearestnav
 */

public int findClosestNavpoint(Triple thisLocation) {

       double minDistance = 10000.00; //should be larger than most node internode distances
       int minNav = 0;
       for (int i=0; i<memory.getKnownNavPoints().size(); i++) {       
         double navDistance = Triple.distanceInSpace(thisLocation, memory.getKnownNavPoints().get(i).location);
         if (navDistance < minDistance) { minDistance = navDistance; minNav = i; }
         //log.info("enemy:"+enemy.name.toString()+":"+navDistance+":"+memory.getKnownNavPoints().get(i).UnrealID.toString());
       }
       //log.info("minDistance:"+minDistance+":"+memory.getKnownNavPoints().get(minNav).UnrealID.toString());
      return minNav;

}

public int knownNavLkp(int navID) {
      Object lookup = navMap.get(Integer.toString(navID));
      int navLookup = Integer.parseInt(lookup.toString());
      return navLookup;
}

public void sleep(int amount, int extra) {

            if (!shouldSleep) { return; }

            int myRandSleep = random.nextInt(extra);
            try {
                Thread.sleep(amount+myRandSleep);
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
}

/* seeTarget() allows us to filter bot for table lookup enemies */

public boolean seeTarget() {

    //lastEnemy = enemy;
     //enemy = memory.getSeePlayer(enemy.ID);
     //enemy = null;
     //if (memory.getSeeAnyPlayer()) {

    /*if (enemy == null) {

     enemy = memory.getSeePlayer(enemy.ID);
     if (enemy != null) {
       //log.info("seeTarget:player");
       //putting this to name to hash for BOT_KILLED message later
       nameMap.put(Integer.toString(enemy.ID), new String (enemy.name));
     }
     //if (memory.getSeeAnyEnemy()) {
     if (enemy == null) {
       enemy = memory.getSeeEnemy();
     }
*/

    //log.info("seeTarget");
    // 1) if have enemyID - checks whether the same enemy is visible, if not, drop ID (and stop shooting)
    
        if (enemy != null) {

            //lastEnemy = enemy;
            
            //FIX - putting this to name to hash for BOT_KILLED message later
            nameMap.put(Integer.toString(enemy.ID), new String (enemy.name));

            enemy = memory.getSeePlayer(enemy.ID); // refresh information about the enemy,
            // note that even though we've got pointer to the message of the enemy seen, it's still a certain message
            // from a specific time - when new message arrives it's written as a new message
            if (enemy == null) {
                //if (memory.isShooting()) {
                    body.stopShoot();
                //} // stop shooting, we've lost target
                log.info("seeTarget: no player/enemy");
                return false;
            }
        }


        // 2) if doesn't have enemy - pick one of the enemy for pursuing
        if (enemy == null) {
            enemy = memory.getSeeEnemy();

            if (enemy != null) {
              //ignore squad members
              if (squadArray.contains(enemy.name.toString())) { enemy = null; }
            }

            if (enemy == null) {
                body.stop();
                body.stopShoot();
                log.info("seeTarget: no enemy");
                return false;
            }

        }

/*        ArrayList<Player> playerList = new ArrayList<Player>();
        ArrayList<Player> playerListGood = new ArrayList<Player>();
        playerList = memory.getSeePlayers();

        for (Player thisPlayer : playerList) {
          //FIX - get closest enemy in this list later
          if (!squadArray.contains(thisPlayer.name.toString())) {
              //enemy = thisPlayer;
              playerListGood.add(thisPlayer);
              //break;
          }
        }

        //get closest enemy/player
        double minPlayerDistance = 30000.0;
        for (Player thisPlayer : playerListGood) {
            double playerDistance = Triple.distanceInSpace(memory.getAgentLocation(),thisPlayer.location);
            if (playerDistance < minPlayerDistance) {
                minPlayerDistance = playerDistance;
                enemy = thisPlayer;
            }
        }
*/
        
        /*if (enemy == null) {
            body.stop();
            body.stopShoot();
            log.info("seeTarget: no enemy");
            return false;
        }*/


     //if (enemy != null) {
        log.info("seeTarget: enemy:"+enemy.name.toString());

        //nameMap.put(Integer.toString(enemy.ID), new String (enemy.name));
        //enemy = memory.getSeePlayer(enemy.ID); // refresh information about the enemy

        //for players(bot=0), register with hash lookup and last_seen table
        if (playerArray != null) {
        if (playerArray.contains(enemy.name.toString())) {
            Object lookup = playerSeeWaitMap.get(enemy.name);
            if (lookup == null) {
                playerSeeWaitMap.put(new String (enemy.name), Double.toString(gameTime));
                //log.info("null:gametime:"+gameTime+":playerSeeWait:"+playerSeeWait);
            }
            if (lookup != null) {
                playerSeeWait = Double.parseDouble(lookup.toString());
                //log.info("gametime:"+gameTime+":playerSeeWait:"+playerSeeWait);
                if (gameTime > playerSeeWait) {
                    playerSeeWait = gameTime+3;
                    playerSeeWaitMap.put(new String (enemy.name), Double.toString(playerSeeWait));
                    try { dbInsertLastSeen(enemy.location.toString(),enemy.name.toString()); } catch (Exception K) { System.out.println("error:dbInsertLastSeen"); }
                }
            }
        }
        }

        //if name on our squad then don't log last_seen or shoot
        /*FIX - ? - believe the below section is handled above
         if (squadArray.contains(enemy.name.toString())) {
            //log.info("enemy on our squad:"+enemy.name.toString());
            enemy = null;
            log.info("seeTarget: my squad");
            return false;
        }*/

        //true = shoot
        lastEnemy = enemy;
        return true;

    //}
    //else {     log.info("seeTarget: no enemy2"); return false; }
}

public boolean refreshInfo() throws Exception {

      Class.forName(sqliteClass);
      Connection conn = DriverManager.getConnection(sqliteDBPath);
      Statement stat = conn.createStatement();
      String sql = null;

      ///////////////////////////////////////////////
      //read unit_to_squad table
      //assume bot can only be member of one squad at a time
      sql = "select squad_name from unit_to_squad where unit_name = '"+botName+"';";
      //log.info("sql:"+sql);
      ResultSet rs = stat.executeQuery(sql);

      while (rs.next()) {
          botSquad = rs.getString(1);
      }

      //read squad table
      //for behavior=tide no attack same squad or other bots in general
      sql = "select unit_name from unit_to_squad where squad_name = '"+botSquad+"' or bot = 1;";
      //log.info("sql:"+sql);
      rs = stat.executeQuery(sql);

     squadArray.clear();
     while (rs.next()) {
         squadArray.add(rs.getString(1));
     }

      //read players
      sql = "select unit_name from unit_to_squad where bot = 0;";
      //log.info("sql:"+sql);
      rs = stat.executeQuery(sql);

     playerArray.clear();
     while (rs.next()) {
         playerArray.add(rs.getString(1));
     }

      ///////////////////////////////////////////////
      //read commands

      //assume bot can only be member of one squad at a time
      sql = "select command,target,location,radius from command where squad_name='"+botSquad+"' and status=0 and level=2 order by priority limit 1;";
      //log.info("sql:"+sql);
      rs = stat.executeQuery(sql);

      //defaults cleared
      botCommand = "none";
      botCommandTarget = "none";
      botCommandLocation = "none";
      botCommandRadius = 0.0;

      while (rs.next()) {
          botCommand = rs.getString(1);
          botCommandTarget = rs.getString(2);
          botCommandLocation = rs.getString(3);
          botCommandRadius = rs.getDouble(4);
      }

      ///////////////////////////////////////////////
      //read reactive commands
      //assume bot can only be member of one squad at a time
      sql = "select command from command where squad_name='"+botSquad+"' and status=0 and level=1 order by priority;";
      //log.info("sql:"+sql);
      rs = stat.executeQuery(sql);

      while (rs.next()) {
          String thisReactive = rs.getString(1);
          if (thisReactive.equals("no_engage")) { shouldEngage = false; }
          if (thisReactive.equals("engage")) { shouldEngage = true; }
          
          if (thisReactive.equals("no_engage_close")) { shouldEngageClose = false; }
          if (thisReactive.equals("engage_close")) { shouldEngageClose = true; }

          if (thisReactive.equals("no_pursue")) { shouldPursue = false; }
          if (thisReactive.equals("pursue")) { shouldPursue = true; }

      }

      ///////////////////////////////////////////////
      //update bot location

      sql = "update bot set location = '"+memory.getAgentLocation().toString()+"' where name = '"+botName+"';";
      rs = stat.executeQuery(sql);

      //close rs
      rs.close();
      return true;
}

public void dbInsertLastSeen(String eventLocation, String seenName) throws Exception {

  //a quick check to make sure not inserting same row from recent memory
  //double testDistance = Triple.distanceInSpace(thisLocation, memory.getKnownNavPoints().get(i).location);
  if (eventLocation.equals(lastSeenLocation) && gameTime < dbInsertLastSeenWait) { return; }
  lastSeenLocation = eventLocation;
  dbInsertLastSeenWait = gameTime+30;
  //FIX - seenNameWaitMap (name,time)

  Class.forName(sqliteClass);
  Connection conn = DriverManager.getConnection(sqliteDBPath);
  Statement stat = conn.createStatement();
  PreparedStatement prep = conn.prepareStatement(
      "insert into last_seen(row_entry_date,gametime,map_level,location,unit_name,bot,report_by_unit,report_by_squad) values (datetime('now'),?,?,?,?,?,?,?);");

  prep.setDouble(1, gameTime);
  prep.setString(2, memory.getGameInfo().level.toString());
  prep.setString(3, eventLocation);
  prep.setString(4, seenName);
  prep.setInt(5, 0);
  prep.setString(6, botName);
  prep.setString(7, botSquad);

  prep.addBatch();
  conn.setAutoCommit(false);
  prep.executeBatch();
  conn.setAutoCommit(true);
  conn.close();
}

public void dbInsertKillScore(String killedName, String killedLocation, String damageType, String killerName, String killerLocation) throws Exception {

  Class.forName(sqliteClass);
  Connection conn = DriverManager.getConnection(sqliteDBPath);
  Statement stat = conn.createStatement();
  PreparedStatement prep = conn.prepareStatement(
      "insert into kill_score(row_entry_date,gametime,map_level,killed_name,killed_location,damage_type,killer_name,killer_location) values (datetime('now'),?,?,?,?,?,?,?);");

  prep.setDouble(1, gameTime);
  prep.setString(2, memory.getGameInfo().level.toString());
  prep.setString(3, killedName);
  prep.setString(4, killedLocation);
  prep.setString(5, damageType);
  prep.setString(6, killerName);
  prep.setString(7, killerLocation);

  prep.addBatch();
  conn.setAutoCommit(false);
  prep.executeBatch();
  conn.setAutoCommit(true);
  conn.close();

}

public boolean dbSwitchSquad(String enemyName) throws Exception {

      Class.forName(sqliteClass);
      Connection conn = DriverManager.getConnection(sqliteDBPath);
      Statement stat = conn.createStatement();
      String sql = null;

      ///////////////////////////////////////////////
      //read unit_to_squad table
      //assume bot can only be member of one squad at a time
      sql = "select squad_name from unit_to_squad where unit_name = '"+enemyName+"';";
      //log.info("sql:"+sql);
      ResultSet rs = stat.executeQuery(sql);

      String botSquadEnemy = "none";
      while (rs.next()) {
          botSquadEnemy = rs.getString(1);
      }

    ///////////////////////////////////////////////
      //update bot squad

      sql = "update unit_to_squad set squad_name = '"+botSquadEnemy+"' where unit_name = '"+botName+"';";
      rs = stat.executeQuery(sql);

      //close rs
      rs.close();
      return true;
}

protected Double enemyFacing (Player enemy) {

    //assumes seen enemy in front of agent on a flat-plane
    //1=facing us,0.5=perpendicular,0=back to us
    Double diff = (Math.abs(enemy.rotation.y - memory.getAgentRotation().y)/65535)*2;
    return diff;
    /*
  Triple enemyFacing = Triple.rotationAsVector(enemy.rotation);
  enemyFacing = enemyFacing.normalize();

  Triple enemyDirection = Triple.subtract(enemy.location, memory.getAgentLocation());
  
  Double orientation = Triple.multiScalar(enemyFacing, enemyDirection);
  return orientation;
     */
/*
//enemy.location - memory.getAgentLocation();

        // compute planar direction to the enemy
        // howto: substract the two locations, remove z-axis, normalize
        Triple enemyDirection = Triple.subtract(enemyLocation, agentLocation);
        // remove z-axis
        enemyDirection.z = 0;
        // and normalize it
        enemyDirection = enemyDirection.normalize ();
*/

/*
 A, B;
vector aFacing,aToB;

// What direction is A facing in?
aFacing=Normal(Vector(A.Rotation));
// Get the vector from A to B
aToB=B.Location-A.Location;

orientation = aFacing dot aToB;
*/
}



}


