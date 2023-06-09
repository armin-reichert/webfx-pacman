/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.event;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public class GhostEvent extends GameEvent {

	public final Ghost ghost;

	public GhostEvent(GameModel game, byte type, Ghost ghost) {
		super(game, type, ghost.tile());
		checkNotNull(ghost);
		this.ghost = ghost;
	}

	@Override
	public String toString() {
		return "GhostEvent{" +
				"ghost=" + ghost +
				'}';
	}
}