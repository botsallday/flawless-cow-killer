package scripts.BADFlawlessCowKiller.framework.paint;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import org.tribot.api.General;
import org.tribot.api.Timing;

public class BADPaint {
    Font font = new Font("Verdana", Font.BOLD, 14);
    private static final long startTime = System.currentTimeMillis();
    
	public void paint(Graphics g, long hides_collected, long bones_collected, long beef_collected) {
       // set variables for display
       long run_time = System.currentTimeMillis() - startTime;
       
       g.setFont(font);
       g.setColor(new Color(0, 0, 0));

       g.drawString("Run Time: " + Timing.msToString(run_time), 140, 360);


       g.drawString("Items - ", 140, 380);

       g.drawString("Hides: " + hides_collected, 160, 410);
       g.drawString("Bones: " + bones_collected, 160, 430);
       g.drawString("Beef: " + beef_collected, 160, 450);

	}
}
