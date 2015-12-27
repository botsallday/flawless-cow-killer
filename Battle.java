package scripts;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Combat;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.types.RSNPC;

public class Battle {
	
	private Transportation transport;
	
	Battle() {
		transport = new Transportation();
	}
	
	
	public boolean searchForTarget(String name, boolean attack) {
		RSNPC[] npc = NPCs.findNearest("Cow", "Calf");
		
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
		}
		
		return false;
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
