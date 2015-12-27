package scripts;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSItemDefinition;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.GroundItems;


public class Clicking {
	Clicking() {
		
	}
	
    public boolean collectAnimableObject(RSObject obj, String option) {
        if (!Inventory.isFull()) {
            if (obj.isOnScreen() && obj.isClickable()) {
                if (DynamicClicking.clickRSObject(obj, option)) {
                    // wait until we finish picking the obj
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            // control cpu usage
                            General.sleep(250, 500);
                            // ensure we have deposited items
                            return Player.getAnimation() == -1;
                        }
                    }, General.random(500, 750));
                    
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean collectGroundObject(RSObject obj, String option) {
        if (!Inventory.isFull()) {
            if (obj.isOnScreen() && obj.isClickable()) {
                if (DynamicClicking.clickRSObject(obj, option)) {
                    // wait until we finish picking the obj
                    Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            // control cpu usage
                            General.sleep(250, 500);
                            // ensure we have deposited items
                            return Player.getAnimation() == -1;
                        }
                    }, General.random(500, 750));
                    
                    return true;
                }
            }
        }
        
        return false;
    }
    
	public boolean getGroundItemByName(String item) {
		// get ground items
		RSGroundItem[] groundItems = GroundItems.findNearest(item);
		// get definition
		RSItemDefinition definition = groundItems[0].getDefinition();
		
	    if (definition != null) {
	        String name = definition.getName();
	        if (name != null) {
	        	// take item from stack
	            if (groundItems[0].click("Take " + name)) {
	            	// count inventory items
	            	final int count = Inventory.getAll().length;
	            	
	            	// wait condition
	            	Timing.waitCondition(new Condition() {
	                    @Override
	                    public boolean active() {
	                        return Inventory.getAll().length > count;
	                    }
	                }, General.random(3000, 6000));
	            	
	            	return true;
	            }
	        }
	    }
	    
	    return false;
	}
}
