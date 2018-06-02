package jab.gun;

import jab.module.Gun;
import jab.module.Module;
import robocode.Rules;
import robocode.Bullet;

public class Maximum extends Gun {

	public Maximum(Module bot) {
		super(bot);
	}

	public void fire() {
		if (bot.enemy != null) {
			double bulletPower = Math.min(Rules.MAX_BULLET_POWER, bot.getEnergy() - 0.01);
			if (bot.enemy.energy == 0) {
				bulletPower = 0;
			}
			bot.bulletPower = bulletPower;
			if (bot.getGunHeat() == 0) {
				Bullet b = bot.setFireBullet(bulletPower);
				bot.registerBullet(b);
			}
		}
	}
}
