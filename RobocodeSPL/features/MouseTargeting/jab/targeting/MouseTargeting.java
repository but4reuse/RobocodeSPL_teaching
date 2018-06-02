package jab.targeting;
 
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
 
import robocode.util.Utils;
import jab.module.Module;
import jab.module.Targeting;
 
/**
 * Credits
 * Interactive - a sample robot by Flemming N. Larsen.
 */
public class MouseTargeting extends Targeting {
 
	public MouseTargeting(Module bot) {
		super(bot);
	}
 
	int aimX, aimY;
 
	public void target() {
		double angle = Utils.normalAbsoluteAngle(Math.atan2(aimX - bot.getX(), aimY - bot.getY()));
		bot.setTurnGunRightRadians(Utils.normalRelativeAngle(angle - bot.getGunHeadingRadians()));
	}
 
	public void listenInput(InputEvent e){
		if (e instanceof MouseEvent){
			aimX = ((MouseEvent)e).getX();
			aimY = ((MouseEvent)e).getY();
		}
	}
 
	public void onPaint(Graphics2D g) {
		g.setColor(Color.RED);
		g.drawOval(aimX - 15, aimY - 15, 30, 30);
		g.drawLine(aimX, aimY - 4, aimX, aimY + 4);
		g.drawLine(aimX - 4, aimY, aimX + 4, aimY);
	}
}