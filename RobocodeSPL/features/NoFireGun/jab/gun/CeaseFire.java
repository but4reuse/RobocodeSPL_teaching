package jab.gun;

import jab.module.Gun;
import jab.module.Module;

public class CeaseFire extends Gun {
	public CeaseFire(Module bot) {
		super(bot);
	}

	public void fire(){
		bot.bulletPower= 0;
	}
}
