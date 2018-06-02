package jab.module;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Bot info
 * 
 * @author jab
 */
public class BotInfo extends Point2D.Double implements java.io.Serializable {

	private static final long serialVersionUID = 5026775048692868786L;

	public boolean teammate;
	public boolean leader;
	public boolean droid;
	public String name;
	public double bearingRadians;
	public double headingRadians;
	public double previousHeadingRadians;
	public double distance;
	public double velocity;
	public double energy;
	public double previousEnergy;
	public int timeSinceLastScan;
	public int timeScanned;

	public Rectangle2D.Double getBotRectangle() {
		Rectangle2D.Double rectangle = new Rectangle2D.Double();
		rectangle.x = this.x - 16;
		rectangle.y = this.y - 16;
		rectangle.width = rectangle.height = 32;
		return rectangle;
	}
}
