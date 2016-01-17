package scripts.BADFlawlessCowKiller.framework.flawlesscowkiller;

import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Combat;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.ext.Filters;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.script.Script;
import scripts.BADFlawlessCowKiller.api.antiban.BADAntiBan;
import scripts.BADFlawlessCowKiller.api.areas.BADAreas;
import scripts.BADFlawlessCowKiller.api.banking.BADBanking;
import scripts.BADFlawlessCowKiller.api.battle.BADBattle;
import scripts.BADFlawlessCowKiller.api.clicking.BADClicking;
import scripts.BADFlawlessCowKiller.api.conditions.BADConditions;
import scripts.BADFlawlessCowKiller.api.transportation.BADTransportation;
import scripts.BADFlawlessCowKiller.framework.gui.HideGUI;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Objects;


public class FlawlessCowKillerCore extends Script {
	
	private HideGUI UI = new HideGUI();
	private BADAntiBan AB = new BADAntiBan();
	
	private boolean kill = false;
	private boolean bank_hides = false;
	private boolean bank_bones = false;
	private boolean bank_beef = false;
	private boolean use_food;
	private int hides_collected = 0;
	private int bones_collected = 0;
	private int beef_collected = 0;
	private String food;

	private BADClicking CLICKING = new BADClicking();
	private BADTransportation TRANSPORT = new BADTransportation();
	private BADBattle CMB = new BADBattle();
	private BADBanking BANKER = new BADBanking();
	private RSArea PASTURE_AREA;
	private RSTile PASTURE_GATE;
	private boolean execute;
	
    public void run() { 
    	
    	while(!UI.getReady()) {
    		General.sleep(250);
    	}
    	execute = true;
    	setGuiVariables();
    	AB.setHoverSkill(Skills.SKILLS.HITPOINTS);
    	
        while(execute) {
        	State state = state();
        	if (state != null) {
	            switch (state) {
	            	case WITHDRAW_FOOD:
	            		withdrawFood();
	            		break;
	                case WALK_TO_LOOTZ:
	                	// walk to area
	                	walkToLootz();
	                    break;
	                case WALK_TO_BANK:
	                	if (!TRANSPORT.dpathnavWalk(BADAreas.LUMBRIDGE_CASTLE_BANK_AREA.getRandomTile())) {
	                		TRANSPORT.webWalking(BADAreas.LUMBRIDGE_CASTLE_BANK_AREA.getRandomTile());
	                	}
	                	break;
	                case GET_THE_LOOTZ:
	                	pickupLoot();
	                	break;
	                case DEPOSIT_ITEMS:
	                	// deposit items
	                	BANKER.depositAll();
	                    break;
	                case WALKING:
	                	// call antiban
	                	AB.handleWait();
	                	break;
	                case KILL:
	                	if (!CMB.searchForTarget("Cow", true)) {
	                		CMB.searchForTarget("Calf", true);
	                	};
	                	break;
	                case COMBAT:
	                	if (use_food) {
	                		eat();
	                	}
	                	AB.handleWait();
	                	break;
	                case SOMETHING_WENT_WRONG:
	                	execute = false;
	                	break;
	            }
            }
            // control cpu usage
            General.sleep(100,  250);
        }
    }

    private State state() {
    	
    	if (outOfFood() && use_food) {
    		if (!Banking.isInBank()) {
    			return State.WALK_TO_BANK;
    		} 
    		
    		if (Banking.isInBank()) {
    			return State.WITHDRAW_FOOD;
    		}
    	}
    	// handle combat
    	if (Combat.isUnderAttack()) {
    		return State.COMBAT;
    	}
    	
    	
    	if (Inventory.isFull() && needsBank()) {
    		// bank
    		if (Banking.isInBank()) {
    			// deposit
    			return State.DEPOSIT_ITEMS;
    		} else {
    			// walk to bank
    			return State.WALK_TO_BANK;
    		}
    	} else if (Player.getAnimation() == -1 && !Player.isMoving()) {
    		RSGroundItem[] items = null;
    		
    		if (bank_hides && GroundItems.findNearest("cowhide").length > 0) {
    			items = GroundItems.findNearest("cowhide");
    		} else if (items == null && bank_bones && GroundItems.findNearest("bones").length > 0) {
    			items = GroundItems.findNearest("bones");
    		} else if (items == null && bank_beef && GroundItems.findNearest("raw beef").length > 0) {
    			items = GroundItems.findNearest("raw beef");
    		}
    		
    		// check for items
    		if (items != null && items.length > 0) {
	    		// find loots
    			// ensure item is at least within range
    			if (items[0].getPosition().distanceToDouble(Player.getPosition()) < 10 && TRANSPORT.validateWalk(items[0].getPosition(), false)) {
    				
	    			if (items[0].getPosition().distanceToDouble(Player.getPosition()) > 5) {
	    				if (!items[0].isOnScreen()) {
	    					Camera.turnToTile(items[0].getPosition());
	    				}
	    				TRANSPORT.dpathnavWalk(items[0].getPosition());
	    			}
    				return State.GET_THE_LOOTZ;
    			}
	    		
    		}
    		
    		RSNPC[] npc = NPCs.findNearest("Cow", "Calf");
    		// check for combat
    		if (NPCs.findNearest("Cow", "Calf").length > 0 && kill){
    			if (npc[0].getPosition().distanceToDouble(Player.getPosition()) < 8 && TRANSPORT.validateWalk(npc[0].getPosition(), false)) {
    				return State.KILL;
    			}
    		}
    		
			return State.WALK_TO_LOOTZ;
    	
    	} else if (Player.isMoving()) {
    		return State.WALKING;
    	} else if (!Player.isMoving()) {
    		return State.WALK_TO_LOOTZ;
    	}

    	return State.SOMETHING_WENT_WRONG;
        
    }

   enum State {
        WALK_TO_BANK,
        WALK_TO_LOOTZ,
        GET_THE_LOOTZ,
        DEPOSIT_ITEMS,
        KILL,
        COMBAT,
        SOMETHING_WENT_WRONG,
        WITHDRAW_FOOD,
        WALKING,
        WAITING
    }
   
   private void pickupLoot() {
   	int count = Inventory.getAll().length;

       // pickup loot
   	if (bank_hides) {
	   		if (CLICKING.getGroundItemByName("cowhide")) {
	   			Timing.waitCondition(BADConditions.gainedItem(count), General.random(2500, 4500));
	   			hides_collected++;
	   		};
		}
   	if (bank_bones) {
	   		if (CLICKING.getGroundItemByName("bones")) {
	   			Timing.waitCondition(BADConditions.gainedItem(count), General.random(2500, 4500));
	   			bones_collected++;
	   		};
		}
		if (bank_beef) {
	   		if (CLICKING.getGroundItemByName("raw beef")) {
	   			Timing.waitCondition(BADConditions.gainedItem(count), General.random(2500, 4500));
	   			beef_collected++;
	   		};
		}
   }
   
   private boolean outOfFood() {
	   return Inventory.find(Filters.Items.actionsContains("Eat")).length < 1;
   }
   
   private boolean needsBank() {
	   return bank_hides || bank_beef || bank_bones || (use_food && outOfFood());
   }
   
   private void walkToLootz() {
   	if (TRANSPORT.webWalking(PASTURE_GATE)) {
		RSObject[] gate = Objects.find(5, Filters.Objects.actionsContains("Open"));
		if (gate.length > 0) {
			if (gate[0].click("Open")) {
				Timing.waitCondition(BADConditions.noClosedGatesNear(), 3000);
			}
		}
		TRANSPORT.dpathnavWalk(PASTURE_AREA.getRandomTile());
	}
   }
   
   private void setGuiVariables() {
	   bank_hides = UI.getBankCowhides();
	   bank_beef = UI.getBankBeef();
	   bank_bones = UI.getBankBones();
	   kill = UI.getKillCows();
	   food = UI.getFoodName();
	   use_food = UI.getUseFood();
	   
	   PASTURE_AREA = getLocation();
   }
   
	public int maxhp() {
		return Combat.getMaxHP();
	}
	
	public int hp() {
		return Combat.getHP();
	}
	
	public float healthPercent() {
	    float perc = (float)hp()/(float)maxhp();
		return perc*100;
	}
   
	public boolean eat() {
		if (healthPercent() < AB.abc.INT_TRACKER.NEXT_EAT_AT.next()) {
			if (eatFood()) {
				println("Ate food");
				AB.abc.INT_TRACKER.NEXT_EAT_AT.reset();
				return true;
			};
		}
		
		return false;
	}
	
	public boolean eatFood() {
		if (Inventory.find(food).length > 0) {
			if (Inventory.find(food)[0].hover()) {
				if (Inventory.find(food)[0].click("Eat")) {
					return true;
				}
			}
		}
		
		return false;
	}
   
   private RSArea getLocation() {
	   if (UI.getLocationNorth()) {
		   PASTURE_GATE = new RSTile(3177, 3314, 0);
		   return BADAreas.NORTH_PASTURE_AREA;
	   }
	   
	   if (UI.getLocationMiddle()) {
		   PASTURE_GATE = new RSTile(3198, 3281, 0);
		   return BADAreas.MIDDLE_PASTURE_AREA;
	   }
	   
	   if (UI.getLocationEast()) {
		   PASTURE_GATE = new RSTile(3250, 3263, 0);
		   return BADAreas.EAST_PASTURE_AREA;
	   }
	   
	   return PASTURE_AREA;
   }
   
   private void withdrawFood() {
		if (Banking.openBank()){
			if (BANKER.depositAll() > 0 || Inventory.getAll().length == 0) {
				if (BANKER.withdraw(food, 14)) {
					
				} else {
					if (Banking.find(food).length < 1){
						use_food = false;
					}
				};
			}
		}
   }
   
   public long getHides() {
	   return hides_collected;
   }
   
   public long getBones() {
	   return bones_collected;
   }
   
   public long getBeef() {
	   return beef_collected;
   }
}
