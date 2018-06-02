package jab.movement;

import jab.module.Module;
import jab.module.Movement;

/**
 * No movement
 * 
 * @author jab
 */
public class Quiet extends Movement {

	public Quiet(Module bot) {
		super(bot);
	}

	public void move() {
		bot.setAhead(0.0001);
	}

}