package jab.gun;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import jab.module.Gun;
import jab.module.Module;
import robocode.Bullet;

/**
 * Credits Interactive - a sample robot by Flemming N. Larsen. - Button 1: Fire
 * a bullet with power = 1 - Button 2: Fire a bullet with power = 2 - Button 3:
 * Fire a bullet with power = 3
 */
public class MouseClickGun extends Gun {

	public MouseClickGun(Module bot) {
		super(bot);
	}

	public void fire() {
		if (bot.bulletPower > 0 && bot.getGunHeat() == 0) {
			Bullet b = bot.setFireBullet(bot.bulletPower);
			bot.registerBullet(b);
		}
	}

	public void listenInput(InputEvent e) {
		if (e instanceof MouseEvent)

			if (e.getID() == MouseEvent.MOUSE_PRESSED)
				if (((MouseEvent) e).getButton() == MouseEvent.BUTTON3)
					bot.bulletPower = 3;
				else if (((MouseEvent) e).getButton() == MouseEvent.BUTTON2)
					bot.bulletPower = 2;
				else
					bot.bulletPower = 1;

		if (e.getID() == MouseEvent.MOUSE_RELEASED)
			bot.bulletPower = 0;
	}

}