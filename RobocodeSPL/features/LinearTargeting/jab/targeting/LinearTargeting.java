package jab.targeting;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import robocode.util.Utils;
import jab.module.Module;
import jab.module.Targeting;

/**
 * A method of targeting which assumes that the target will come to a stop when
 * they reach a wall and that he always move at the same speed
 * 
 * http://robowiki.net/wiki/Linear_Targeting
 */
public class LinearTargeting extends Targeting {

	public LinearTargeting(Module bot) {
		super(bot);
	}

	public void target() {
		if (bot.enemy != null) {
			double myX = bot.getX();
			double myY = bot.getY();
			double enemyX = bot.enemy.x;
			double enemyY = bot.enemy.y;
			double enemyHeading = bot.enemy.headingRadians;
			double enemyVelocity = bot.enemy.velocity;

			double deltaTime = 0;
			double battleFieldHeight = bot.getBattleFieldHeight(), battleFieldWidth = bot.getBattleFieldWidth();
			double predictedX = enemyX, predictedY = enemyY;
			while ((++deltaTime) * (20.0 - 3.0 * bot.bulletPower) < Point2D.Double.distance(myX, myY, predictedX,
					predictedY)) {
				predictedX += Math.sin(enemyHeading) * enemyVelocity;
				predictedY += Math.cos(enemyHeading) * enemyVelocity;
				if (predictedX < 18.0 || predictedY < 18.0 || predictedX > battleFieldWidth - 18.0
						|| predictedY > battleFieldHeight - 18.0) {
					predictedX = Math.min(Math.max(18.0, predictedX), battleFieldWidth - 18.0);
					predictedY = Math.min(Math.max(18.0, predictedY), battleFieldHeight - 18.0);
					break;
				}
			}
			double theta = Utils.normalAbsoluteAngle(Math.atan2(predictedX - bot.getX(), predictedY - bot.getY()));
			bot.setTurnGunRightRadians(Utils.normalRelativeAngle(theta - bot.getGunHeadingRadians()));

			toPaintX = (int) predictedX;
			toPaintY = (int) predictedY;
		}
	}

	int toPaintX = 0;
	int toPaintY = 0;

	public void onPaint(Graphics2D g) {
		if (bot.enemy != null) {
			g.setColor(Color.red);
			g.drawOval(toPaintX - 4, toPaintY - 4, 8, 8);
			g.drawLine(toPaintX - 6, toPaintY, toPaintX + 6, toPaintY);
			g.drawLine(toPaintX, toPaintY - 6, toPaintX, toPaintY + 6);
			g.drawLine((int) bot.getX(), (int) bot.getY(), toPaintX, toPaintY);
		}
	}
}
