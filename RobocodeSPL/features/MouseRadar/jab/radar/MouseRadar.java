package jab.radar;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import robocode.util.Utils;

import jab.module.Module;
import jab.module.Radar;

/**
 * Credits Interactive - a sample robot by Flemming N. Larsen.
 */
public class MouseRadar extends Radar {

	public MouseRadar(Module bot) {
		super(bot);
	}

	int aimX, aimY;

	public void scan() {
		double angle = Utils.normalAbsoluteAngle(Math.atan2(aimX - bot.getX(), aimY - bot.getY()));
		bot.setTurnRadarRightRadians(Utils.normalRelativeAngle(angle - bot.getRadarHeadingRadians()));
	}

	public void listenInput(InputEvent e) {
		if (e instanceof MouseEvent) {
			aimX = ((MouseEvent) e).getX();
			aimY = ((MouseEvent) e).getY();
		}
	}
}