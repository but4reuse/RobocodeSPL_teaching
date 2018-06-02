package jab.targeting;

import jab.module.Module;
import jab.module.Targeting;

public class HeadOnTargeting extends Targeting {

	public HeadOnTargeting(Module bot) {
		super(bot);
	}

	public void target() {
		double absoluteBearing = bot.getHeadingRadians() + bot.enemy.bearingRadians;
		//System.out.println("HT Bearing: " +bot.enemy.bearingRadians + "   AbsoluteBearing: "+absoluteBearing);
		bot.setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(absoluteBearing - bot.getGunHeadingRadians()));
	}
}
