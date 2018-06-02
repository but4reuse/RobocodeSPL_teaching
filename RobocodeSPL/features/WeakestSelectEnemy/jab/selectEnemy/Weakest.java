package jab.selectEnemy;

import java.util.Iterator;

import jab.module.BotInfo;
import jab.module.Module;
import jab.module.SelectEnemy;

/**
 * Select the enemy with less energy
 * 
 * @author jab
 */
public class Weakest extends SelectEnemy {

	public Weakest(Module bot) {
		super(bot);
	}

	public void select() {
		Iterator<BotInfo> iterator= bot.botsInfo.values().iterator();
		double minEnergy= Double.MAX_VALUE;
		BotInfo selected=null;
		while (iterator.hasNext()){
			BotInfo e= iterator.next();
			if (minEnergy>e.energy){
				selected=e;
				minEnergy=e.energy;
			}				
		}
		bot.enemy=selected;
	}

}
