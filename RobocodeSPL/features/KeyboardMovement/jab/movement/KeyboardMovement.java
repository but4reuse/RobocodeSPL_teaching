package jab.movement;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import jab.module.Module;
import jab.module.Movement;

public class KeyboardMovement extends Movement {

	/**
	 * Credits Interactive - a sample robot by Flemming N. Larsen.
	 */
	public KeyboardMovement(Module bot) {
		super(bot);
	}

	// Move direction: 1 = move forward, 0 = stand still, -1 = move backward
	int moveDirection;

	// Turn direction: 1 = turn right, 0 = no turning, -1 = turn left
	int turnDirection;

	// Amount of pixels/units to move
	double moveAmount;

	public void move() {
		// Sets the robot to move forward, backward or stop moving depending
		// on the move direction and amount of pixels to move
		bot.setAhead(moveAmount * moveDirection);

		// Decrement the amount of pixels to move until we reach 0 pixels
		// This way the robot will automatically stop if the mouse wheel
		// has stopped it's rotation
		moveAmount = Math.max(0, moveAmount - 1);

		// Sets the robot to turn right or turn left (at maximum speed) or
		// stop turning depending on the turn direction
		bot.setTurnRight(45 * turnDirection); // degrees
	}

	public void listenInput(InputEvent e) {
		if (e instanceof KeyEvent) {
			if (((KeyEvent) e).getID() == KeyEvent.KEY_PRESSED)
				switch (((KeyEvent) e).getKeyCode()) {
				case KeyEvent.VK_UP:
					moveDirection = 1;
					moveAmount = Double.POSITIVE_INFINITY;
					break;

				case KeyEvent.VK_DOWN:
					moveDirection = -1;
					moveAmount = Double.POSITIVE_INFINITY;
					break;

				case KeyEvent.VK_RIGHT:
					turnDirection = 1;
					break;

				case KeyEvent.VK_LEFT:
					turnDirection = -1;
					break;
				}
			else if (((KeyEvent) e).getID() == KeyEvent.KEY_RELEASED)
				switch (((KeyEvent) e).getKeyCode()) {
				case KeyEvent.VK_UP:
				case KeyEvent.VK_DOWN:
					// Arrow up and down keys: move direction = stand still
					moveDirection = 0;
					break;

				case KeyEvent.VK_RIGHT:
				case KeyEvent.VK_LEFT:
					// Arrow right and left keys: turn direction = stop turning
					turnDirection = 0;
					break;
				}

		}
	}

}