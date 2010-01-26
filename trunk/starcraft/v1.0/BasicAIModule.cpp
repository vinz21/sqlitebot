//#include <vector>
#include "BasicAIModule.h"
using namespace BWAPI;

/*#include <WorkerManager.h>
#include "Util.h"
#include <algorithm>

using namespace std;
using namespace Util;
WorkerManager::WorkerManager(Arbitrator::Arbitrator<Unit*,double>* arbitrator)
{
  this->arbitrator        = arbitrator;
  this->baseManager       = NULL;
  this->buildOrderManager = NULL;
  this->lastSCVBalance    = 0;
  this->WorkersPerGas     = 3;
  this->mineralRate       = 0;
  this->gasRate           = 0;
  this->autoBuild         = false;
  this->autoBuildPriority = 80;
}
*/



//    int SizeX = 3;
//    int SizeY = 5;
//    std::vector<std::vector<int> > my_2d_int_array(SizeX, std::vector<int>(SizeY));

int sc_group[30][3];
//int sc_group[2][2]={{1,2},{3,4}};
//    int sc_group[10][10];
//	sc_group[1][1] = 123;

bool enableStart = true;

int countTroop = 0;
int attackStrength = 5;
int scoutStrength = 60;

Unit* myBase=NULL;
int baseRadius = 1200; //default 1200

Unit* troopGuy;

//BWAPI::Position mob;

void BasicAIModule::onStart()
{
sc_group[1][1] = 55;

  this->showManagerAssignments=false;
  if (Broodwar->isReplay()) return;
  // Enable some cheat flags
  Broodwar->enableFlag(Flag::UserInput);
  //Broodwar->enableFlag(Flag::CompleteMapInformation);
  BWTA::readMap();
  BWTA::analyze();
  this->analyzed=true;
  this->buildManager       = new BuildManager(&this->arbitrator);
  this->techManager        = new TechManager(&this->arbitrator);
  this->upgradeManager     = new UpgradeManager(&this->arbitrator);
  this->scoutManager       = new ScoutManager(&this->arbitrator);
  this->workerManager      = new WorkerManager(&this->arbitrator);
  this->buildOrderManager  = new BuildOrderManager(this->buildManager,this->techManager,this->upgradeManager,this->workerManager);
  this->baseManager        = new BaseManager();
  this->supplyManager      = new SupplyManager();
  this->defenseManager     = new DefenseManager(&this->arbitrator);
  this->informationManager = new InformationManager();
  this->unitGroupManager   = new UnitGroupManager();
  this->enhancedUI         = new EnhancedUI();

  this->supplyManager->setBuildManager(this->buildManager);
  this->supplyManager->setBuildOrderManager(this->buildOrderManager);
  this->techManager->setBuildingPlacer(this->buildManager->getBuildingPlacer());
  this->upgradeManager->setBuildingPlacer(this->buildManager->getBuildingPlacer());
  this->workerManager->setBaseManager(this->baseManager);
  this->workerManager->setBuildOrderManager(this->buildOrderManager);
  this->baseManager->setBuildOrderManager(this->buildOrderManager);
  
  BWAPI::Race race = Broodwar->self()->getRace();
  BWAPI::Race enemyRace = Broodwar->enemy()->getRace();
  BWAPI::UnitType workerType=*(race.getWorker());
    double minDist;
  BWTA::BaseLocation* natural=NULL;
  BWTA::BaseLocation* home=BWTA::getStartLocation(Broodwar->self());
  for(std::set<BWTA::BaseLocation*>::const_iterator b=BWTA::getBaseLocations().begin();b!=BWTA::getBaseLocations().end();b++)
  {
    if (*b==home) continue;
    double dist=home->getGroundDistance(*b);
    if (dist>0)
    {
      if (natural==NULL || dist<minDist)
      {
        minDist=dist;
        natural=*b;
      }
    }
  }
  this->buildOrderManager->enableDependencyResolver();
  //make the basic production facility
  if (race == Races::Zerg)
  {
    //send an overlord out if Zerg
    this->scoutManager->setScoutCount(1);

    //12 hatch
    this->buildOrderManager->build(12,workerType,80);
    this->baseManager->expand(natural,79);
    this->buildOrderManager->build(20,workerType,78);
    this->buildOrderManager->buildAdditional(1,UnitTypes::Zerg_Spawning_Pool,60);
    this->buildOrderManager->buildAdditional(3,UnitTypes::Zerg_Zergling,82);
  }
  else if (race == Races::Terran)
  {
    this->buildOrderManager->build(22,workerType,120);
	this->buildOrderManager->build(5,workerType,74);
    if (enemyRace == Races::Zerg)
    {
      this->buildOrderManager->buildAdditional(1,UnitTypes::Terran_Barracks,60);
      this->buildOrderManager->buildAdditional(9,UnitTypes::Terran_Marine,45);
      this->buildOrderManager->buildAdditional(1,UnitTypes::Terran_Refinery,42);
      this->buildOrderManager->buildAdditional(1,UnitTypes::Terran_Barracks,40);
      this->buildOrderManager->buildAdditional(1,UnitTypes::Terran_Academy,39);
      this->buildOrderManager->buildAdditional(9,UnitTypes::Terran_Medic,38);
      this->buildOrderManager->research(TechTypes::Stim_Packs,35);
      this->buildOrderManager->research(TechTypes::Tank_Siege_Mode,35);
      this->buildOrderManager->buildAdditional(3,UnitTypes::Terran_Siege_Tank_Tank_Mode,34);
      this->buildOrderManager->buildAdditional(2,UnitTypes::Terran_Science_Vessel,30);
	  this->buildOrderManager->research(TechTypes::Irradiate,30);
      this->buildOrderManager->upgrade(1,UpgradeTypes::Terran_Infantry_Weapons,20);
      this->buildOrderManager->build(3,UnitTypes::Terran_Missile_Turret,13);
      this->buildOrderManager->upgrade(3,UpgradeTypes::Terran_Infantry_Weapons,12);
      this->buildOrderManager->upgrade(3,UpgradeTypes::Terran_Infantry_Armor,12);
      this->buildOrderManager->build(1,UnitTypes::Terran_Engineering_Bay,11);
      this->buildOrderManager->buildAdditional(40,UnitTypes::Terran_Marine,10);
      this->buildOrderManager->build(6,UnitTypes::Terran_Barracks,8);
      this->buildOrderManager->build(2,UnitTypes::Terran_Engineering_Bay,7);
      this->buildOrderManager->buildAdditional(10,UnitTypes::Terran_Siege_Tank_Tank_Mode,5);
    }
    else
    {
	  if (enableStart) {
	  //medics at 1 to 5 troop ratio
      //this->buildOrderManager->buildAdditional(10,UnitTypes::Terran_Marine,100);
      //this->buildOrderManager->buildAdditional(2,UnitTypes::Terran_Medic,100);
      this->buildOrderManager->buildAdditional(3,UnitTypes::Terran_Medic,119);
	  this->buildOrderManager->buildAdditional(15,UnitTypes::Terran_Marine,118);

      /*this->buildOrderManager->buildAdditional(3,UnitTypes::Terran_Medic,117);
	  this->buildOrderManager->buildAdditional(15,UnitTypes::Terran_Marine,116);
      this->buildOrderManager->buildAdditional(3,UnitTypes::Terran_Medic,115);
	  this->buildOrderManager->buildAdditional(15,UnitTypes::Terran_Marine,114);
      this->buildOrderManager->buildAdditional(3,UnitTypes::Terran_Medic,113);
	  this->buildOrderManager->buildAdditional(15,UnitTypes::Terran_Marine,112);
	  this->buildOrderManager->buildAdditional(3,UnitTypes::Terran_Medic,111);
	  this->buildOrderManager->buildAdditional(15,UnitTypes::Terran_Marine,111);
	  */
	  this->buildOrderManager->buildAdditional(12,UnitTypes::Terran_Medic,73);
      this->buildOrderManager->buildAdditional(60,UnitTypes::Terran_Marine,73);

      //this->buildOrderManager->buildAdditional(1,UnitTypes::Terran_Barracks,86);
      this->buildOrderManager->buildAdditional(1,UnitTypes::Terran_Barracks,109);
	  this->buildOrderManager->buildAdditional(2,UnitTypes::Terran_Barracks,75);

      //this->buildOrderManager->buildAdditional(3,UnitTypes::Terran_Barracks,109);
      this->buildOrderManager->buildAdditional(1,UnitTypes::Terran_Academy,109);

	  this->buildOrderManager->buildAdditional(1,UnitTypes::Terran_Science_Facility,85);

	  this->buildOrderManager->buildAdditional(1,UnitTypes::Terran_Control_Tower,74);
	  this->buildOrderManager->buildAdditional(1,UnitTypes::Terran_Armory,74);
	  this->buildOrderManager->upgrade(3,UpgradeTypes::Terran_Ship_Plating,102);      

	  //this->buildOrderManager->buildAdditional(4,UnitTypes::Terran_Missile_Turret,79);  
	  this->buildOrderManager->buildAdditional(3,UnitTypes::Terran_Science_Vessel,74); 

      this->buildOrderManager->upgrade(1,UpgradeTypes::Terran_Infantry_Weapons,106);      
	  this->buildOrderManager->upgrade(3,UpgradeTypes::Terran_Infantry_Weapons,104);
	  this->buildOrderManager->upgrade(1,UpgradeTypes::Terran_Infantry_Armor,105);
	  this->buildOrderManager->upgrade(3,UpgradeTypes::Terran_Infantry_Armor,103);

	  this->buildOrderManager->upgrade(1,UpgradeTypes::U_238_Shells,104);

	  this->buildOrderManager->research(TechTypes::Healing,104);
	  this->buildOrderManager->upgrade(1,UpgradeTypes::Caduceus_Reactor,103);
	  this->buildOrderManager->research(TechTypes::Restoration,102);
	  this->buildOrderManager->research(TechTypes::Stim_Packs,101);

	  //science vessel - 2 units, research, upgrades
	  //attacks?
	  //attack target if idle? not for now since more vulnerable
	  }

	  /*
      this->buildOrderManager->buildAdditional(2,BWAPI::UnitTypes::Terran_Machine_Shop,70);
      this->buildOrderManager->buildAdditional(3,BWAPI::UnitTypes::Terran_Factory,60);
	  //this->buildOrderManager->research(TechTypes::Spider_Mines,55);
      //this->buildOrderManager->research(TechTypes::Tank_Siege_Mode,55);
      this->buildOrderManager->buildAdditional(20,BWAPI::UnitTypes::Terran_Vulture,40);
      //this->buildOrderManager->buildAdditional(20,BWAPI::UnitTypes::Terran_Siege_Tank_Tank_Mode,40);
	  this->buildOrderManager->upgrade(3,UpgradeTypes::Ion_Thrusters,40);
      this->buildOrderManager->upgrade(3,UpgradeTypes::Terran_Vehicle_Weapons,40);
	  this->buildOrderManager->upgrade(3,UpgradeTypes::Terran_Vehicle_Plating,40);
 */

	}
  }
  else if (race == Races::Protoss)
  {
    this->buildOrderManager->build(20,workerType,80);
    this->buildOrderManager->buildAdditional(10,UnitTypes::Protoss_Dragoon,70);
    this->buildOrderManager->buildAdditional(10,UnitTypes::Protoss_Zealot,70);
    this->buildOrderManager->upgrade(1,UpgradeTypes::Singularity_Charge,61);
    this->buildOrderManager->buildAdditional(20,UnitTypes::Protoss_Carrier,60);
  }
  this->workerManager->enableAutoBuild();
  this->workerManager->setAutoBuildPriority(40);
 
}


void BasicAIModule::onFrame()
{
  if (Broodwar->isReplay()) return;
  //JTC if (!this->analyzed) return;
  //Broodwar->sendText("Update");
  this->buildManager->update();
  this->buildOrderManager->update();
  this->baseManager->update();
  this->workerManager->update();
  this->techManager->update();
  this->upgradeManager->update();
  this->supplyManager->update();
  this->scoutManager->update();
  this->defenseManager->update();
  this->arbitrator.update();

  this->enhancedUI->update();

  //JTCif (Broodwar->getFrameCount()>24*50)
    //scoutManager->setScoutCount(1);

  //if (enableScout)
	//scoutManager->setScoutCount(1);

  std::set<Unit*> units=Broodwar->self()->getUnits();
  if (this->showManagerAssignments)
  {
    for(std::set<Unit*>::iterator i=units.begin();i!=units.end();i++)
    {
      if (this->arbitrator.hasBid(*i))
      {
        int x=(*i)->getPosition().x();
        int y=(*i)->getPosition().y();
        std::list< std::pair< Arbitrator::Controller<BWAPI::Unit*,double>*, double> > bids=this->arbitrator.getAllBidders(*i);
        int y_off=0;
        bool first = false;
        const char activeColor = '\x07', inactiveColor = '\x16';
        char color = activeColor;
        for(std::list< std::pair< Arbitrator::Controller<BWAPI::Unit*,double>*, double> >::iterator j=bids.begin();j!=bids.end();j++)
        {
          Broodwar->drawText(CoordinateType::Map,x,y+y_off,"%c%s: %d",color,j->first->getShortName().c_str(),(int)j->second);
          y_off+=15;
          color = inactiveColor;
        }
      }
    }
  }

  UnitGroup myPylonsAndGateways = SelectAll()(Pylon,Gateway)(HitPoints,"<=",200);
  for each(Unit* u in myPylonsAndGateways)
  {
    Broodwar->drawCircleMap(u->getPosition().x(),u->getPosition().y(),20,Colors::Red);
  }
  
  if ((Broodwar->getFrameCount() % 30) == 0) { checkIdle(); }
}

void BasicAIModule::onUnitDestroy(BWAPI::Unit* unit)
{
  this->arbitrator.onRemoveObject(unit);
  this->buildManager->onRemoveUnit(unit);
  this->techManager->onRemoveUnit(unit);
  this->upgradeManager->onRemoveUnit(unit);
  this->workerManager->onRemoveUnit(unit);
  this->scoutManager->onRemoveUnit(unit);
  this->defenseManager->onRemoveUnit(unit);
  this->informationManager->onUnitDestroy(unit);

  Broodwar->sendText("UnitDestroy:%s",unit->getType().getName().c_str());
  //replace our destroyed units
  if (unit->getPlayer()->getID() == Broodwar->self()->getID()) {

  if (!strcmp(unit->getType().getName().c_str(),"Terran Vulture")) 
    {
    this->buildOrderManager->buildAdditional(1,BWAPI::UnitTypes::Terran_Vulture,100);
    } 
  if (!strcmp(unit->getType().getName().c_str(),"Terran Marine")) 
    {
    this->buildOrderManager->buildAdditional(1,BWAPI::UnitTypes::Terran_Marine,100);
    } 
  if (!strcmp(unit->getType().getName().c_str(),"Terran Medic")) 
    {
    this->buildOrderManager->buildAdditional(1,BWAPI::UnitTypes::Terran_Medic,101);
    } 
  if (!strcmp(unit->getType().getName().c_str(),"Terran SCV")) 
    {
    this->buildOrderManager->buildAdditional(1,BWAPI::UnitTypes::Terran_SCV,100);
    }
  if (!strcmp(unit->getType().getName().c_str(),"Terran Science Vessel")) 
    {
		this->buildOrderManager->buildAdditional(1,BWAPI::UnitTypes::Terran_Science_Vessel,100);
    }

  if (!strcmp(unit->getType().getName().c_str(),"Terran Barracks")) 
    {
		this->buildOrderManager->buildAdditional(1,BWAPI::UnitTypes::Terran_Barracks,100);
    } 
  if (!strcmp(unit->getType().getName().c_str(),"Terran Command Center")) 
    {
		this->buildOrderManager->buildAdditional(1,BWAPI::UnitTypes::Terran_Command_Center,100);
    } 
  }
}

void BasicAIModule::onUnitShow(BWAPI::Unit* unit)
{
  this->informationManager->onUnitShow(unit);
  this->unitGroupManager->onUnitShow(unit);
  
  int thisTime = Broodwar->getFrameCount();
  Broodwar->sendText("A %s [%x] has been spotted at (%d,%d):time:%d",unit->getType().getName().c_str(),unit,unit->getPosition().x(),unit->getPosition().y(),thisTime);

  static int attackTime;

  if (thisTime < attackTime+24*5) { return; }
  
  //attackTime = thisTime;
  //Broodwar->sendText("Attack!");

  //if (!((*i)->getPlayer()->getID()==Broodwar->self()->getID()))
  
  //Broodwar->sendText("attackTime:%d:Neutral:%d",attackTime,unit->getType().isNeutral());

  //don't attack if low strength and seen enemy far away
  if (myBase==NULL) { return; }
  if (countTroop < attackStrength && ((unit)->getDistance(myBase) > baseRadius)) { return; }

  //attack if not self
  if (unit->getPlayer()->getID() != Broodwar->self()->getID()
	  && !(unit->getType().isNeutral())
	  && strcmp(unit->getType().getName().c_str(),"Special Power Generator")
	  ) 
    {
    Broodwar->sendText("Attack!");
    attackTime = thisTime;

	BWAPI::Position target;
	target.x() = unit->getPosition().x();
	target.y() = unit->getPosition().y();

	Unit* closestTarget=NULL;

	std::set<Unit*> units=Broodwar->self()->getUnits();

	for(std::set<Unit*>::iterator i=units.begin();i!=units.end();i++)
	  {
	  //ignore others units
	 if ((*i)->getPlayer()->getID() != Broodwar->self()->getID()) { continue; }

      //don't send workers,medics,flyers to attack
	  if (!((*i)->getType().isWorker()) &&
		  (strcmp((*i)->getType().getName().c_str(),"Terran Medic"))  &&
		  !(*i)->getType().isFlyer()
		  )
	    {
	    (*i)->attackMove(target);

        if (closestTarget==NULL || (*i)->getDistance(target)<(*i)->getDistance(closestTarget))
			{ closestTarget=*i; }
		//troopGuy = *i;
	    }

	}

	for(std::set<Unit*>::iterator i=units.begin();i!=units.end();i++)
	  {
	  //ignore others units
	 if ((*i)->getPlayer()->getID() != Broodwar->self()->getID()) { continue; }

	  //idle medics,sv can join attack
	  if ( !strcmp((*i)->getType().getName().c_str(),"Terran Medic")
		  //&& (*i)->isIdle()
		  ) {
		(*i)->attackMove(target);
		//(*i)->follow(troopGuy);
		//tried closest but maybe coming around corner problems
		//(*i)->rightClick(closestTarget->getPosition());
	  }

	  if ( !strcmp((*i)->getType().getName().c_str(),"Terran Science Vessel") 
		  && closestTarget!=NULL
		  ) {
		(*i)->rightClick(closestTarget->getPosition());
	  }
	}

  }
}

void BasicAIModule::onUnitHide(BWAPI::Unit* unit)
{
  this->informationManager->onUnitHide(unit);
  this->unitGroupManager->onUnitHide(unit);
}

void BasicAIModule::onUnitMorph(BWAPI::Unit* unit)
{
  this->unitGroupManager->onUnitMorph(unit);
}
void BasicAIModule::onUnitRenegade(BWAPI::Unit* unit)
{
  this->unitGroupManager->onUnitRenegade(unit);
}

bool BasicAIModule::onSendText(std::string text)
{
  UnitType type=UnitTypes::getUnitType(text);
  if (text=="debug")
  {
    this->showManagerAssignments=true;
    this->buildOrderManager->enableDebugMode();
    this->scoutManager->enableDebugMode();
    return true;
  }
  if (text=="expand")
  {
    this->baseManager->expand();
  }
  if (type!=UnitTypes::Unknown)
  {
    this->buildOrderManager->buildAdditional(1,type,300);
  }
  else
  {
    TechType type=TechTypes::getTechType(text);
    if (type!=TechTypes::Unknown)
    {
      this->techManager->research(type);
    }
    else
    {
      UpgradeType type=UpgradeTypes::getUpgradeType(text);
      if (type!=UpgradeTypes::Unknown)
      {
        this->upgradeManager->upgrade(type);
      }
      else
        Broodwar->printf("You typed '%s'!",text.c_str());
    }
  }
  return true;
}

void BasicAIModule::checkIdle()
{

	//Broodwar->sendText("checkIdle");

	int gasWorkers = 0;
	int mineralWorkers = 0;
	countTroop = 0;
	//int totalX = 0;
	//int totalY = 0;
    int totalIdle = 0;
	bool idleMob = false;

	Unit* hurt=NULL;
    
	std::set<Unit*> repairUnits;

//check our list of repairs already in progress to see if finished and remove from list

	/*
  for (std::set<Unit*>::const_iterator u=repairUnits.begin(); u!=repairUnits.end(); u++) {
	  if ((*u)->getType().getID()==(*m)->getType().getID()) { repairUnits.erase(
	  {

	  }
  }*/

	//workerUnit
	//vector<int> dyarray;
	//arrayRepair[1][1] = 55;


	for(std::set<Unit*>::const_iterator i=Broodwar->self()->getUnits().begin();i!=Broodwar->self()->getUnits().end();i++)
    {
		if ((*i)->getType().isWorker()) {
		  if ((*i)->isGatheringMinerals()) { mineralWorkers++; }
		  if ((*i)->isGatheringGas()) { gasWorkers++; }
		}
		//don't count workers,buildings with troops
		//else if (!(*i)->getType().isBuilding() && !(*i)->getType().isFlyer()) {
		else if (!(*i)->getType().isBuilding()) {
			countTroop++;

			if ((*i)->isIdle()) { totalIdle++; }

			//totalX += (*i)->getPosition().x();
			//totalY += (*i)->getPosition().y();

			//if ((*i)->getHitPoints() < (*i)->getInitialHitPoints()) 
			if ((*i)->getHitPoints() < (*i)->getType().maxHitPoints() ||
				(*i)->isBlind()				
				) 
				hurt = *i;
		}

	    if (!strcmp((*i)->getType().getName().c_str(),"Terran Medic") && hurt != NULL) {
			if ((hurt)->isBlind()) { (*i)->useTech(BWAPI::TechTypes::Restoration,hurt); }
			else { if (!(*i)->getType().isFlyer()) { (*i)->rightClick(hurt);} }
	    }
		if (!strcmp((*i)->getType().getName().c_str(),"Terran Science Vessel") && hurt != NULL) {
		  //try follow as right-click brings too much air counter-attack
	      (*i)->rightClick(hurt);
	    }

		//which unit base? more than one?
		if (!strcmp((*i)->getType().getName().c_str(),"Terran Command Center"))
		{
			myBase = *i;
		}

	}
	//JTC - FIX bug divide by zero troop at game start
	//int avgX = totalX/countTroop;
	//int avgY = totalY/countTroop;  

	//mob.x() = avgX;
	//mob.y() = avgY;

	if (countTroop > scoutStrength) { enableScout = true; }

	if (totalIdle == countTroop) { idleMob = true; }

	Broodwar->sendText("g:%d:m:%d:t:%d:i:%d:im:%d",gasWorkers,mineralWorkers,countTroop,totalIdle,idleMob);

    //send each worker to the mineral field that is closest to it
    for(std::set<Unit*>::const_iterator i=Broodwar->self()->getUnits().begin();i!=Broodwar->self()->getUnits().end();i++)
    {
      if ((*i)->getType().isWorker() && (*i)->isIdle())
      {
		
		//double mineralRate = this->workerManager->getMineralRate();
		//double gasRate = this->workerManager->getGasRate();

/*  for(map<Unit*,WorkerData>::iterator u = workers.begin(); u != workers.end(); u++)
  {
    Unit* i = u->first;
    if (u->second.resource!=NULL)
    {
		if (u->second.resource->getType()==UnitTypes::Resource_Mineral_Field)
        mineralRate+=8/180.0;
      else
        gasRate+=8/180.0;
    }
  }
*/

		  Broodwar->sendText("idle:gasWorkers:%d:mineral:%d",gasWorkers,mineralWorkers);
		//gas
		if (gasWorkers < 3) {
 Broodwar->sendText("debug0");
          Unit* closestGeyser=NULL;
          for(std::set<Unit*>::iterator m=Broodwar->getAllUnits().begin();m!=Broodwar->getAllUnits().end();m++)
		  {
			  //Broodwar->sendText("m:%d",(*m)->getType().isRefinery());
			  if ((*m)->getType().isRefinery() && (*m)->getPlayer()==Broodwar->self() && (*m)->isCompleted()) {
 Broodwar->sendText("debug1");

            if (closestGeyser==NULL || (*i)->getDistance(*m)<(*i)->getDistance(closestGeyser))
			{ closestGeyser=*m; Broodwar->sendText("debug2"); }
		  }
		  }
			if (closestGeyser!=NULL) { (*i)->rightClick(closestGeyser); }
		  
		}
		//minerals
		else {
          Unit* closestMineral=NULL;
          for(std::set<Unit*>::iterator m=Broodwar->getMinerals().begin();m!=Broodwar->getMinerals().end();m++)
          {
          if (closestMineral==NULL || (*i)->getDistance(*m)<(*i)->getDistance(closestMineral))
            closestMineral=*m;
          }
          if (closestMineral!=NULL)
            (*i)->rightClick(closestMineral);
		}
	  }

	  //if ((*i)->getType().isWorker() && !(*i)->isRepairing()) {
	  if ((*i)->getType().isWorker()) {


  	  //repair trumps others
          Unit* closestRepair=NULL;
   	  	  for(std::set<Unit*>::const_iterator m=Broodwar->self()->getUnits().begin();m!=Broodwar->self()->getUnits().end();m++)
    {
//check our list of repairs already in progress so we don't repeat assign to others
  bool alreadyRepairing = false;
  for (std::set<Unit*>::const_iterator u=repairUnits.begin(); u!=repairUnits.end(); u++) {
	  if ((*u)->getType().getID()==(*m)->getType().getID()) {
		  alreadyRepairing = true;
		  break;
	  }
  }
			  
  if (alreadyRepairing) { break; }

			  //for(std::set<Unit*>::iterator m=Broodwar->getAllUnits().begin();m!=Broodwar->getAllUnits().end();m++)
		
			  //Broodwar->sendText("rtype:%s:%d:%d",(*m)->getType().getName().c_str(),(*m)->getHitPoints(),(*m)->getInitialHitPoints());
			  //Broodwar->sendText("m:%d",(*m)->getType().isRefinery());
			  //repair buildings,detectors(sv,missilesilo),workers
			  if (((*m)->getType().isBuilding() || (*m)->getType().isDetector() || (*m)->getType().isWorker()) &&
				  //(*m)->getPlayer()==Broodwar->self() &&
				  //((*m)->getHitPoints() < (*m)->getInitialHitPoints())
				  ((*m)->getHitPoints() < (*m)->getType().maxHitPoints())
				  ) {

			//Broodwar->sendText("repair:%s",(*m)->getType().getName().c_str());
            if (closestRepair==NULL || (*i)->getDistance(*m)<(*i)->getDistance(closestRepair))
			{ closestRepair=*m; }
		  }
		  }  //for all our units
			if (closestRepair!=NULL) { (*i)->repair(closestRepair); repairUnits.insert(closestRepair); }
	  
	  } //if worker

    //base defense
    for(std::set<Unit*>::iterator m=Broodwar->getAllUnits().begin();m!=Broodwar->getAllUnits().end();m++)
	{
		//Broodwar->sendText("BA:%.2f:%s",(*m)->getDistance(myBase),(*m)->getType().getName().c_str());

		//if !worker,!self,!neutral,!power and enemy and close to base
		if ( (!(*i)->getType().isWorker())
			 && strcmp((*i)->getType().getName().c_str(),"Terran Medic") 
			 && strcmp((*i)->getType().getName().c_str(),"Terran Science Vessel")

		     && ((*m)->getPlayer()->getID() != Broodwar->self()->getID())
			 && !((*m)->getType().isNeutral())
			 && strcmp((*m)->getType().getName().c_str(),"Special Power Generator")
			 && strcmp((*m)->getType().getName().c_str(),"Vulture Spider Mine")
			)
		{
			//if within baseRadius, always attack - otherwise only attack if idle(not already attacking)
			if ((*m)->getDistance(myBase) < baseRadius)
			  {
			  Broodwar->sendText("BAC:%.2f:%s",(*m)->getDistance(myBase),(*m)->getType().getName().c_str());
			  (*i)->attackMove((*m)->getPosition());
			  }
			else if ((*i)->isIdle()) { (*i)->attackMove((*m)->getPosition()); }
		}
	}
	
	}  // for all self units

}