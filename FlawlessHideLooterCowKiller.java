package scripts;

import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Combat;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSTile;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;
import org.tribot.api2007.GroundItems;


import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCs;



@ScriptManifest(authors = {"botsallday"}, category = "Money Making", name = "FlawlessHideLooterCowKiller")

public class FlawlessHideLooterCowKiller extends Script implements Painting {
	
	private HideGUI ui = new HideGUI();
	private AntiBan ab = new AntiBan();
	
	private boolean kill = false;
	private boolean bank_hides = false;
	private boolean bank_bones = false;
	private boolean bank_beef = false;
	private int hides_collected = 0;
	private int bones_collected = 0;
	private int beef_collected = 0;
	private static final long startTime = System.currentTimeMillis();
    Font font = new Font("Verdana", Font.BOLD, 14);

	private Clicking clicking = new Clicking();
	private Transportation transport = new Transportation();
	private Battle cmb = new Battle();
	private Banker banker = new Banker();
	private RSArea bank_area = transport.getAreaFromCoords(3207, 3209, 3216, 3219, 2);
	private RSArea pasture_area = transport.getAreaFromCoords(3248, 3262, 3280, 3294, 0);
	private boolean execute;
	
    public void run() { 
    	
    	while(!ui.getReady()) {
    		General.sleep(250);
    	}
    	
    	execute = true;
    	
    	setGuiVariables();
    	
    	ab.setHoverSkill(Skills.SKILLS.HITPOINTS);
    	General.useAntiBanCompliance(true);
    	
        while(execute) {
        	
            State state = state();
            println("State");
            println(state);
            if (state != null) {
                switch (state) {
                    case WALK_TO_LOOTZ:
                    	// walk to area
                    	if (transport.webWalking(new RSTile(3250, 3263, 0))) {
                    		transport.dpathnavWalk(pasture_area.getRandomTile());
                		}
                        break;
                    case WALK_TO_BANK:
                    	if (!transport.dpathnavWalk(bank_area.getRandomTile())) {
                    		transport.webWalking(bank_area.getRandomTile());
                    	}
                    	break;
                    case GET_THE_LOOTZ:
                    	int count = Inventory.getAll().length;

                    	Condition gainedItem = new Condition() {
                            @Override
                            public boolean active() {
                                // control cpu usage
                                General.sleep(50, 150);
                                // ensure we have an item
                                return Inventory.getAll().length > count;
                            }
                        };

                        // pickup loot
                    	if (bank_hides) {
                    		if (clicking.getGroundItemByName("cowhide")) {
                    			Timing.waitCondition(gainedItem, General.random(2500, 4500));
                    			hides_collected++;
                    		};
                		}
                    	if (bank_bones) {
                    		if (clicking.getGroundItemByName("bones")) {
                    			Timing.waitCondition(gainedItem, General.random(2500, 4500));
                    			bones_collected++;
                    		};
                		}
                		if (bank_beef) {
                    		if (clicking.getGroundItemByName("raw beef")) {
                    			Timing.waitCondition(gainedItem, General.random(2500, 4500));
                    			beef_collected++;
                    		};
                		}
                    	break;
                    case DEPOSIT_ITEMS:
                    	// deposit items
                    	banker.depositAll();
                        break;
                    case WALKING:
                    	// call antiban
                    	ab.handleWait();
                    	break;
                    case KILL:
                    	if (!cmb.searchForTarget("Cow", true)) {
                    		cmb.searchForTarget("Calf", true);
                    	};
                    	break;
                    case COMBAT:
                    	ab.handleWait();
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
    		if (items != null) {
	    		// find loots
	    		if (items.length > 0) {
	    			// ensure item is at least within range
	    			if (items[0].getPosition().distanceToDouble(Player.getPosition()) < 10) {
	    				
		    			if (items[0].getPosition().distanceToDouble(Player.getPosition()) > 5) {
		    				if (!items[0].isOnScreen()) {
		    					Camera.turnToTile(items[0].getPosition());
		    				}
		    				transport.dpathnavWalk(items[0].getPosition());
		    			}
	    				return State.GET_THE_LOOTZ;
	    			}
	    		
	    		}
    		}
    		
    		RSNPC[] npc = NPCs.findNearest("Cow", "Calf");
    		// check for combat
    		if (NPCs.findNearest("Cow", "Calf").length > 0 && kill){
    			if (npc[0].getPosition().distanceToDouble(Player.getPosition()) < 8) {
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
        WALKING,
    }
   
   private boolean needsBank() {
	   return bank_hides || bank_beef || bank_bones;
   }
   
   private void setGuiVariables() {
	   bank_hides = ui.getBankCowhides();
	   bank_beef = ui.getBankBeef();
	   bank_bones = ui.getBankBones();
	   kill = ui.getKillCows();
   }
   
   public void onPaint(Graphics g) {
       // set variables for display
       long run_time = System.currentTimeMillis() - startTime;
       
       g.setFont(font);
       g.setColor(new Color(0, 0, 0));

       g.drawString("Run Time: " + Timing.msToString(run_time), 140, 360);

       g.drawString("Items - ", 140, 380);

       g.drawString("Hides: " + hides_collected, 160, 410);
       g.drawString("Bones: " + bones_collected, 160, 430);
       g.drawString("Beef: " + beef_collected, 160, 450);

//       g.drawString("Items per hour: "+ (hides_collected+bones_collected+beef_collected), 160, 380);
//       g.drawString("Profit per hour"+(hides_collected+bones_collected+beef_collected)*75, 160, 400);

   }
}
