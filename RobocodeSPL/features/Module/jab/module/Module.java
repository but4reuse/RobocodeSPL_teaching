package jab.module;

import robocode.*;
import robocode.util.Utils;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

/**
 * Module 1.0.0
 * 
 * @author jab
 */
public abstract class Module extends TeamRobot {

	public static Rectangle2D.Double battleField;
	public final double BOT_WIDTH = 36;

	public static String[] enemyNumAssignation;
	public static int totalNumOfEnemies;

	// Bot's parts
	public Radar radar;
	public Targeting targeting;
	public Movement movement;
	public Gun gun;
	public SelectEnemy selectEnemy;
	public Vector<Special> specials = new Vector<Special>();

	// The power of the next bullet
	public double bulletPower;

	// The current BotInfo
	public BotInfo enemy = null;

	// A Hash-table of all the scanned Enemies
	public Hashtable<String, BotInfo> botsInfo = new Hashtable<String, BotInfo>();

	// A Vector of all the fired bullets
	// public Vector<BulletInfo> bullets = new Vector<BulletInfo>();
	public Vector<BulletInfoEnemy> enemyBullets = new Vector<BulletInfoEnemy>();

	// Team leader
	public static boolean teamLeader = false;

	// Debug
	private static int debugOption;

	public void run() {
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		// Creating the custom event EnemyFires
		addCustomEvent(new Condition("EnemyFires") {
			public boolean test() {
				return (enemy != null && enemy.previousEnergy > enemy.energy
						&& enemy.previousEnergy - enemy.energy <= robocode.Rules.MAX_BULLET_POWER
						&& !Utils.isNear((enemy.previousEnergy - enemy.energy),
								robocode.Rules.getBulletDamage(bulletPower))
						&& enemy.distance > 55);
			};
		});

		if (battleField == null) {
			battleField = new Rectangle2D.Double(BOT_WIDTH / 2, BOT_WIDTH / 2, getBattleFieldWidth() - BOT_WIDTH,
					getBattleFieldHeight() - BOT_WIDTH);
			totalNumOfEnemies = getOthers() - (getTeammates() == null ? 0 : getTeammates().length);
			enemyNumAssignation = new String[totalNumOfEnemies];
		}
		initialize();

		while (true) {
			updateEnemyPositions();
			updateEnemyBullets();
			selectBehavior();
			executeBehavior();
			broadCastMyInfo();
		}
	}

	protected abstract void selectBehavior();

	protected abstract void initialize();

	private void updateEnemyPositions() {
		Rectangle2D.Double walkableBattleField = new Rectangle2D.Double(BOT_WIDTH / 2 - 3, BOT_WIDTH / 2 - 3,
				getBattleFieldWidth() - BOT_WIDTH + 6, getBattleFieldHeight() - BOT_WIDTH + 6);
		Enumeration<BotInfo> enemies = botsInfo.elements();
		while (enemies.hasMoreElements()) {
			BotInfo botInfo = enemies.nextElement();
			double newX = Math.sin(botInfo.headingRadians) * botInfo.velocity + botInfo.x;
			double newY = Math.cos(botInfo.headingRadians) * botInfo.velocity + botInfo.y;
			Point2D.Double newPos = new Point2D.Double(newX, newY);
			if (walkableBattleField.contains(newPos)) {
				botInfo.x = newX;
				botInfo.y = newY;
				botInfo.distance = botInfo.distance(getX(), getY());
				botInfo.bearingRadians = getBearing(botInfo);
			}
		}
	}

	private void executeBehavior() {
		selectEnemy.select();
		radar.scan();
		gun.fire();
		targeting.target();
		movement.move();
		Iterator<Special> i = specials.iterator();
		while (i.hasNext())
			i.next().doIt();
		execute();
	}

	private void listenEvent(Event e) {
		if (selectEnemy != null) {
			// MessageEvents could be received before behavior initialization
			selectEnemy.listen(e);
			radar.listen(e);
			gun.listen(e);
			targeting.listen(e);
			movement.listen(e);
			Iterator<Special> i = specials.iterator();
			while (i.hasNext())
				i.next().listen(e);
		}
	}

	private void listenInputEvent(InputEvent e) {
		if (selectEnemy != null)
			selectEnemy.listenInput(e);
		if (radar != null)
			radar.listenInput(e);
		if (gun != null)
			gun.listenInput(e);
		if (targeting != null)
			targeting.listenInput(e);
		if (movement != null)
			movement.listenInput(e);
		Iterator<Special> i = specials.iterator();
		while (i.hasNext()) {
			Special special = i.next();
			if (special != null)
				special.listenInput(e);
		}
	}

	public void registerBullet(Bullet bullet) {
		if (bullet != null) {
			// BulletInfo bulletInfo = new BulletInfo();
			// bulletInfo.bullet = bullet;
			// bulletInfo.toName = enemy.name;
			// bulletInfo.targeting = targeting.getClass().getSimpleName();
			// bulletInfo.timeFire = (int) getTime();
			// bullets.add(bulletInfo);

			BulletInfoEnemy friendFire = new BulletInfoEnemy();
			friendFire.isFriendFire = true;
			friendFire.isToRemove = false;
			friendFire.fromName = getName();
			friendFire.headingRadians = bullet.getHeadingRadians();
			friendFire.velocity = bullet.getVelocity();
			friendFire.power = bullet.getPower();
			friendFire.x = bullet.getX();
			friendFire.y = bullet.getY();
			try {
				this.broadcastMessage(friendFire);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void updateEnemyBullets() {
		Enumeration<BulletInfoEnemy> i = enemyBullets.elements();
		while (i.hasMoreElements()) {
			BulletInfoEnemy bullet = i.nextElement();
			bullet.x = /*-1 */Math.sin(bullet.headingRadians) * bullet.velocity + bullet.x;
			bullet.y = /*-1 */Math.cos(bullet.headingRadians) * bullet.velocity + bullet.y;
			if (!battleField.contains(bullet)) {
				enemyBullets.remove(bullet);
			}
		}
	}

	public void activate(Special special) {
		if (!specials.contains(special))
			specials.add(special);
	}

	public void deactivate(Special special) {
		specials.remove(special);
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		if (!isTeammate(e.getName())) {
			assignNumToEnemy(e.getName());
		}

		BotInfo scanned = botsInfo.get(e.getName());
		if (scanned == null) {
			scanned = new BotInfo();
		}
		scanned.teammate = isTeammate(e.getName());
		scanned.name = e.getName();
		scanned.bearingRadians = e.getBearingRadians();
		scanned.previousHeadingRadians = scanned.headingRadians;
		scanned.headingRadians = e.getHeadingRadians();
		scanned.distance = e.getDistance();
		scanned.x = getX() + e.getDistance() * Math.sin(getHeadingRadians() + e.getBearingRadians());
		scanned.y = getY() + e.getDistance() * Math.cos(getHeadingRadians() + e.getBearingRadians());
		scanned.velocity = e.getVelocity();
		scanned.previousEnergy = scanned.energy;
		scanned.energy = e.getEnergy();
		scanned.timeSinceLastScan = (int) e.getTime() - scanned.timeScanned;
		scanned.timeScanned = (int) e.getTime();
		if (botsInfo.get(e.getName()) == null) {
			// Scanned for the first time
			if (scanned.energy > 190) {
				scanned.leader = true;
				if (scanned.energy > 210) {
					scanned.droid = true;
				}
			} else if (scanned.energy > 110) {
				scanned.droid = true;
			}
		}
		botsInfo.put(e.getName(), scanned);

		if (!scanned.teammate) {
			try {
				broadcastMessage((BotInfo) scanned.clone());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		listenEvent(e);
	}

	// Handling the custom event
	public void onCustomEvent(CustomEvent e) {
		Condition condition = e.getCondition();
		if (condition.getName().equals("EnemyFires")) {
			BulletInfoEnemy enemyBullet = new BulletInfoEnemy();
			enemyBullet.fromName = enemy.name;
			enemyBullet.x = enemy.x;
			enemyBullet.y = enemy.y;
			enemyBullet.power = enemy.previousEnergy - enemy.energy;
			enemyBullet.headingRadians = Utils.normalAbsoluteAngle(Math.atan2(enemy.x - getX(), enemy.y - getY()));
			enemyBullet.velocity = robocode.Rules.getBulletSpeed(enemyBullet.power);
			enemyBullet.isFriendFire = false;
			enemyBullet.isToRemove = false;
			enemyBullets.add(enemyBullet);
		}
		listenEvent(e);
	}

	public void onHitByBullet(HitByBulletEvent e) {
		listenEvent(e);
	}

	public void onHitRobot(HitRobotEvent e) {
		listenEvent(e);
	}

	public void onHitWall(HitWallEvent e) {
		listenEvent(e);
	}

	public void onBulletHit(BulletHitEvent e) {
		listenEvent(e);

		// Send to teammates to be removed from enemyBullets
		Bullet bullet = e.getBullet();
		BulletInfoEnemy friendFire = new BulletInfoEnemy();
		friendFire.isFriendFire = true;
		friendFire.isToRemove = true;
		friendFire.fromName = getName();
		friendFire.headingRadians = bullet.getHeadingRadians();
		friendFire.velocity = bullet.getVelocity();
		friendFire.power = bullet.getPower();
		friendFire.x = bullet.getX();
		friendFire.y = bullet.getY();
		try {
			this.broadcastMessage(friendFire);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void onBulletHitBullet(BulletHitBulletEvent e) {
		listenEvent(e);

		// Send to teammates to be removed from enemyBullets
		Bullet bullet = e.getBullet();
		BulletInfoEnemy friendFire = new BulletInfoEnemy();
		friendFire.isFriendFire = true;
		friendFire.isToRemove = true;
		friendFire.fromName = getName();
		friendFire.headingRadians = bullet.getHeadingRadians();
		friendFire.velocity = bullet.getVelocity();
		friendFire.power = bullet.getPower();
		friendFire.x = bullet.getX();
		friendFire.y = bullet.getY();
		try {
			this.broadcastMessage(friendFire);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void onBulletMissed(BulletMissedEvent e) {
		listenEvent(e);
	}

	public void onRobotDeath(RobotDeathEvent e) {
		listenEvent(e);
		botsInfo.remove(e.getName());
		selectEnemy.select();
	}

	public void onWin(WinEvent e) {
		listenEvent(e);
	}

	public void onDeath(DeathEvent e) {
		listenEvent(e);
	}

	public void onSkippedTurn(SkippedTurnEvent e) {
		System.out.println("SKIPPED TURN!!!!!!!");
		listenEvent(e);
	}

	public void onKeyPressed(KeyEvent e) {
		int key = e.getKeyCode() - 48;
		if (key >= 0 && key <= 6) {
			debugOption = key;
		}
		listenInputEvent(e);
	}

	public void onKeyReleased(KeyEvent e) {
		listenInputEvent(e);
	}

	public void onMouseMoved(MouseEvent e) {
		listenInputEvent(e);
	}

	public void onMousePressed(MouseEvent e) {
		listenInputEvent(e);
	}

	public void onMouseReleased(MouseEvent e) {
		listenInputEvent(e);
	}

	public void onPaint(Graphics2D g) {
		g.setColor(Color.white);
		Enumeration<BotInfo> enemies = botsInfo.elements();
		while (enemies.hasMoreElements()) {
			BotInfo enemy = enemies.nextElement();
			g.draw(enemy.getBotRectangle());
		}
		if (enemy != null) {
			g.setColor(Color.red);
			g.draw(botsInfo.get(enemy.name).getBotRectangle());
		}

		// Enumeration<BulletInfoEnemy> friendFire = enemyBullets.elements();
		// while (friendFire.hasMoreElements()){
		// BulletInfoEnemy bul = friendFire.nextElement();
		// if (bul.isFriendFire)
		// g.drawOval((int)bul.x-10, (int)bul.y-10, 20, 20);
		// }

		g.setColor(Color.white);
		g.draw(battleField);
		g.drawString(
				"Debug option= " + debugOption
						+ "      0: All      1: SelectEnemy      2: Radar      3: Gun      4: Targeting      5: Movement      6: Specials",
				15, 15);
		switch (debugOption) {
		case 0:
			selectEnemy.onPaint(g);
			radar.onPaint(g);
			gun.onPaint(g);
			targeting.onPaint(g);
			movement.onPaint(g);
			Iterator<Special> i = specials.iterator();
			while (i.hasNext())
				i.next().onPaint(g);
			break;
		case 1:
			selectEnemy.onPaint(g);
			break;
		case 2:
			radar.onPaint(g);
			break;
		case 3:
			gun.onPaint(g);
			break;
		case 4:
			targeting.onPaint(g);
			break;
		case 5:
			movement.onPaint(g);
			break;
		case 6:
			Iterator<Special> it = specials.iterator();
			while (it.hasNext())
				it.next().onPaint(g);
			break;
		}
	}

	public void onMessageReceived(MessageEvent e) {
		if (e.getMessage() instanceof BotInfo) {
			BotInfo botInfo = (BotInfo) e.getMessage();
			if (!isTeammate(botInfo.name)) {
				assignNumToEnemy(botInfo.name);
			}
			botInfo.bearingRadians = getBearing(botInfo);
			botInfo.distance = botInfo.distance(new Point2D.Double(getX(), getY()));
			botsInfo.put(botInfo.name, botInfo);
		} else if (e.getMessage() instanceof BulletInfoEnemy) {
			BulletInfoEnemy bullet = (BulletInfoEnemy) e.getMessage();
			if (!bullet.isToRemove) {
				enemyBullets.add(bullet);
			} else {
				Enumeration<BulletInfoEnemy> enumeration = enemyBullets.elements();
				while (enumeration.hasMoreElements()) {
					BulletInfoEnemy b = enumeration.nextElement();
					if (b.isFriendFire && bullet.isFriendFire && b.fromName.equals(bullet.fromName)
							&& b.power == bullet.power && b.headingRadians == bullet.headingRadians
							&& b.distance(bullet) < 50) {
						enemyBullets.remove(b);
					}
				}
			}
		}
		listenEvent(e);
	}

	private void broadCastMyInfo() {
		BotInfo me = new BotInfo();
		me.teammate = true;
		me.name = getName();
		if (this.getTime() < 25 && this.getEnergy() > 190) {
			teamLeader = true;
		}
		me.leader = teamLeader;
		me.headingRadians = getHeadingRadians();
		me.x = getX();
		me.y = getY();
		me.velocity = getVelocity();
		me.energy = getEnergy();
		me.timeScanned = (int) getTime();
		// botsInfo.put(getName(), me);
		try {
			broadcastMessage(me);
		} catch (IOException ex) {
			out.println(ex);
		}
	}

	private void assignNumToEnemy(String enemyName) {
		for (int i = 0; i < enemyNumAssignation.length; i++) {
			if (enemyNumAssignation[i] == null) {
				enemyNumAssignation[i] = enemyName;
				break;
			}
		}
	}

	public int getEnemyAssignedNum(String enemyName) {
		for (int i = 0; i < enemyNumAssignation.length; i++) {
			if (enemyNumAssignation[i].equals(enemyName))
				return i;
		}
		return 0;
	}

	public int getCurrentRoundScannedEnemies() {
		int counter = 0;
		Enumeration<BotInfo> botsEnum = botsInfo.elements();
		while (botsEnum.hasMoreElements()) {
			if (!botsEnum.nextElement().teammate) {
				counter++;
			}
		}
		return counter;
	}

	public int getCurrentNumberOfEnemies() {
		int counter = 0;
		Enumeration<BotInfo> enemies = botsInfo.elements();
		while (enemies.hasMoreElements()) {
			BotInfo botInfo = enemies.nextElement();
			if (!isTeammate(botInfo.name)) {
				counter++;
			}
		}
		return counter;
	}

	public int getCurrentNumberOfTeamMates() {
		int counter = 0;
		Enumeration<BotInfo> enemies = botsInfo.elements();
		while (enemies.hasMoreElements()) {
			BotInfo botInfo = enemies.nextElement();
			if (isTeammate(botInfo.name)) {
				counter++;
			}
		}
		return counter;
	}

	public int getCurrentNumberDroidEnemies() {
		int counter = 0;
		Enumeration<BotInfo> enemies = botsInfo.elements();
		while (enemies.hasMoreElements()) {
			BotInfo botInfo = enemies.nextElement();
			if (botInfo.droid && !botInfo.teammate) {
				counter++;
			}
		}
		return counter;
	}

	public BotInfo getEnemiesLeader() {
		Enumeration<BotInfo> enemies = botsInfo.elements();
		while (enemies.hasMoreElements()) {
			BotInfo botInfo = enemies.nextElement();
			if (botInfo.leader && !isTeammate(botInfo.name)) {
				return botInfo;
			}
		}
		return null;
	}

	public BotInfo getTeamLeader() {
		Enumeration<BotInfo> enemies = botsInfo.elements();
		while (enemies.hasMoreElements()) {
			BotInfo botInfo = enemies.nextElement();
			if (botInfo.leader && isTeammate(botInfo.name)) {
				return botInfo;
			}
		}
		return null;
	}

	public boolean isTheSameBot(String name1, String name2) {
		if (name1.endsWith(")") && name2.endsWith(")")) {
			name1 = name1.substring(0, name1.lastIndexOf("("));
			name2 = name2.substring(0, name2.lastIndexOf("("));
			if (name1.equals(name2)) {
				return true;
			}
		}
		return false;
	}

	public int getNumberOfEnemies() {
		return getOthers() - (getTeammates() == null ? 0 : getTeammates().length);
	}

	public int getNumberOfTeamMates() {
		return (getTeammates() == null ? 0 : getTeammates().length);
	}

	public double getBearing(Point2D.Double botInfo) {
		double thetaFireTime = Utils.normalAbsoluteAngle(Math.atan2(botInfo.x - getX(), botInfo.y - getY()));
		return Utils.normalRelativeAngle(thetaFireTime - getHeadingRadians());
	}
}
