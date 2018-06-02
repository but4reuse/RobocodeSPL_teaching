package jab.module;

import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * Bullet info enemy
 * 
 * @author jab
 */
public class BulletInfoEnemy extends Point2D.Double implements Serializable {

	private static final long serialVersionUID = 5287201696698043952L;

	public String fromName;
	public boolean isFriendFire;
	public boolean isToRemove;
	public double power;
	public double headingRadians;
	public double velocity;

}
