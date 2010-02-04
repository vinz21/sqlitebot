#pragma once
#include <BWAPI.h>
#include <BWTA.h>
#include <Arbitrator.h>
#include <WorkerManager.h>
#include <SupplyManager.h>
#include <BuildManager.h>
#include <BuildOrderManager.h>
#include <TechManager.h>
#include <UpgradeManager.h>
#include <BaseManager.h>
#include <ScoutManager.h>
#include <DefenseManager.h>
#include <InformationManager.h>
#include <UnitGroupManager.h>
#include <EnhancedUI.h>
class BasicAIModule : public BWAPI::AIModule
{
public:
  virtual void onStart();
  virtual void onFrame();
  virtual void onUnitShow(BWAPI::Unit* unit);
  virtual void onUnitHide(BWAPI::Unit* unit);
  virtual void onUnitMorph(BWAPI::Unit* unit);
  virtual void onUnitRenegade(BWAPI::Unit* unit);
  virtual void onUnitDestroy(BWAPI::Unit* unit);

  virtual void checkIdle();
  virtual void checkResources();
  virtual void groupAdd(BWAPI::Unit* unit);
  virtual void groupMove();

  bool enableScout;

    class GroupData
    {
      public:
	    int unitID;
		int groupID;  //group 0 = home defense
		int groupType; //0=scout,1=defend,2=attack,3=harass
		BWAPI::Unit* unitPointer;
	    bool move;
		//BWAPI::Position* currentTargetPosition;
		double currentTargetDistance;
    };

  std::list<GroupData*> mainGroup;
  std::list<GroupData*> homeGroup;

  virtual bool onSendText(std::string text);
  void showStats(); //not part of BWAPI::AIModule
  void showPlayers();
  void showForces();
  bool analyzed;
  std::map<BWAPI::Unit*,BWAPI::UnitType> buildings;
  Arbitrator::Arbitrator<BWAPI::Unit*,double> arbitrator;
  WorkerManager* workerManager;
  SupplyManager* supplyManager;
  BuildManager* buildManager;
  TechManager* techManager;
  UpgradeManager* upgradeManager;
  BaseManager* baseManager;
  ScoutManager* scoutManager;
  BuildOrderManager* buildOrderManager;
  DefenseManager* defenseManager;
  InformationManager* informationManager;
  UnitGroupManager* unitGroupManager;
  EnhancedUI* enhancedUI;
  bool showManagerAssignments;

};