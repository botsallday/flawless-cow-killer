package scripts.BADFlawlessCowKiller.flawlesscowkiller;

import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.api.General;
import org.tribot.script.interfaces.Painting;
import org.tribot.script.interfaces.Starting;

import scripts.BADFlawlessCowKiller.framework.flawlesscowkiller.FlawlessCowKillerCore;
import scripts.BADFlawlessCowKiller.framework.paint.BADPaint;

import java.awt.Graphics; 

@ScriptManifest(authors = {"botsallday"}, category = "Money Making", name = "FlawlessHideLootingCowKilla")

public class FlawlessCowKiller extends Script implements Painting, Starting {
    
	private BADPaint painter = new BADPaint();
	private FlawlessCowKillerCore core = new FlawlessCowKillerCore();
    
    public void run() {
		// execute the script
		core.run();
    }
   
    public void onPaint(Graphics g) {
    	painter.paint(g, core.getHides(), core.getBones(), core.getBeef(), core.getState());
    }

	@Override
	public void onStart() {
    	General.useAntiBanCompliance(true);
	}
}