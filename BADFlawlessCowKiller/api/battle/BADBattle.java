package scripts.BADFlawlessCowKiller.api.battle;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Combat;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.types.RSNPC;

import scripts.BADFlawlessCowKiller.api.antiban.BADAntiBan;
import scripts.BADFlawlessCowKiller.api.transportation.BADTransportation;

public class BADBattle {
	
	private BADTransportation transport;
	private BADAntiBan AB;
	
	public BADBattle() {
		transport = new BADTransportation();
		AB = new BADAntiBan();
	}
	
	
	public boolean searchForTarget(String name, boolean attack) {
		RSNPC[] npc = NPCs.findNearest(name);
		// antiban compliance
		AB.handleSwitchObjectCombatDelay();
		if (npc.length > 0 && transport.validateWalk(npc[0].getPosition(), true)) {
			RSNPC target = findTarget(npc);
			if (!target.isInCombat() || target.isInteractingWithMe()) {
				if (attack) {
					target.click("Attack");
	            	Timing.waitCondition(new Condition() {
	                    @Override
	                    public boolean active() {
	                        return Combat.isUnderAttack();
	                    }
	                }, General.random(3000, 7000));
				}
				
				return true;
			}
		} else {
			// if we didn't find an enemy then we must be waiting on one to spawn
			AB.handleNewObjectCombatDelay();
		}
		
		return false;
	}
	
	public boolean handleHoverNext(String target) {
		RSNPC[] npcs = NPCs.findNearest(target);
		
		if (npcs.length > 0) {
			return AB.handleHoverNextNPC(findHoverNextTarget(npcs));
		}
		
		return false;

	}
	
	private RSNPC findHoverNextTarget(RSNPC[] npcs) {
		for (int i = 0; i < npcs.length; i++) {
			if (!npcs[i].isInCombat() && !npcs[i].isInteractingWithMe() && npcs[i].isOnScreen()) {
				return npcs[i];
			}
		}
		
		return npcs[0];
	}
	
	public RSNPC findTarget(RSNPC[] targets) {
		for (int i = 0; i < targets.length && i < 5; i++) {
			if (!targets[i].isInCombat() || targets[i].isInteractingWithMe()) {
				return targets[i];
			}
		}
		
		return targets[0];
	}

}
