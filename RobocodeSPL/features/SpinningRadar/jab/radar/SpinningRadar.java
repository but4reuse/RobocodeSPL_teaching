package jab.radar;

import jab.module.Module;
import jab.module.Radar;

/**
 * A simple spin of the radar
 * 
 * http://robowiki.net/wiki/One_on_One_Radar
 */
public class SpinningRadar extends Radar {

	public SpinningRadar(Module bot) {
		super(bot);
	}

	public void scan() {
		bot.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	}

}
