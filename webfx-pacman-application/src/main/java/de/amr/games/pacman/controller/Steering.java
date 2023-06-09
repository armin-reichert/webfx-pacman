/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.controller;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Creature;

/**
 * @author Armin Reichert
 */
public abstract class Steering {

	public static final Steering NONE = new Steering() {
		@Override
		public void steer(GameLevel level, Creature guy) {
		}
	};

	private boolean enabled;

	public void init() {
		// implement if needed
	}

	public abstract void steer(GameLevel level, Creature guy);

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}