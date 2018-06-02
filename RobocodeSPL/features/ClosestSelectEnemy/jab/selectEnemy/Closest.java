package jab.selectEnemy;

import java.util.Iterator;

import jab.module.BotInfo;
import jab.module.Module;
import jab.module.SelectEnemy;

/**
 * Select the closest enemy
 * 
 * @author jab
 */
public class Closest extends SelectEnemy {

	public Closest(Module bot) {
		super(bot);
	}

	public void select() {
		Iterator<BotInfo> iterator = bot.botsInfo.values().iterator();
		double minDistance = Double.MAX_VALUE;
		BotInfo selected = null;
		while (iterator.hasNext()) {
			BotInfo botInfo = iterator.next();
			if ((!botInfo.teammate) && minDistance > botInfo.distance) {
				selected = botInfo;
				minDistance = botInfo.distance;
			}
		}
		bot.enemy = selected;
	}

}
