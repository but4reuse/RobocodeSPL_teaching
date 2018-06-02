package jab;

import jab.gun.*;
import jab.module.*;
import jab.movement.*;
import jab.radar.*;
import jab.selectEnemy.*;
import jab.targeting.*;

import java.awt.Color;

/**
 * Module Bot 1.0.0
 * 
 * @author jab
 */
public class ModuleBot extends Module {

	protected void initialize() {
		// TODO Customize the colors here
		setBodyColor(Color.BLACK);
		setGunColor(Color.BLACK);
		setRadarColor(Color.BLACK);
		setScanColor(Color.BLUE);
		setBulletColor(Color.RED);
	}

	protected void selectBehavior() {
		radar = selectedRadar;
		movement = selectedMovement;
		targeting = selectedTargeting;
		selectEnemy = selectedSelectEnemy;
		gun = selectedGun;
	}
}
