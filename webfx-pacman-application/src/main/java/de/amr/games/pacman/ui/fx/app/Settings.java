/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameVariant;
import javafx.scene.input.KeyCode;

import java.util.HashMap;
import java.util.Map;
import dev.webfx.platform.util.collection.Collections;

/**
 * @author Armin Reichert
 */
public class Settings {

	private static final Map<Direction, KeyCode> KEYS_NUMPAD = Collections.mapOf(//
			Direction.UP, KeyCode.NUMPAD8, //
			Direction.DOWN, KeyCode.NUMPAD5, //
			Direction.LEFT, KeyCode.NUMPAD4, //
			Direction.RIGHT, KeyCode.NUMPAD6);

	private static final Map<Direction, KeyCode> KEYS_CURSOR = Collections.mapOf(//
			Direction.UP, KeyCode.UP, //
			Direction.DOWN, KeyCode.DOWN, //
			Direction.LEFT, KeyCode.LEFT, //
			Direction.RIGHT, KeyCode.RIGHT);

	private static Map<Direction, KeyCode> keyMap(String name) {
		switch (name) {
		case "numpad":
			return KEYS_NUMPAD;
		case "cursor":
			return KEYS_CURSOR;
		default:
			throw new IllegalArgumentException("Unknown keymap name: " + name);
		}
	}

	public boolean fullScreen;
	public GameVariant variant;
	public float zoom;
	public Map<Direction, KeyCode> keyMap;

	public Settings() {
		this(new HashMap<>(0));
	}

	public Settings(Map<String, String> pm) {
		fullScreen = false;
		variant = GameVariant.PACMAN;
		zoom = 2;
		keyMap = keyMap("cursor");
		merge(pm);
	}

	public void merge(Map<String, String> pm) {
		if (pm.containsKey("fullScreen")) {
			fullScreen = Boolean.valueOf(pm.get("fullScreen"));
		}
		if (pm.containsKey("variant")) {
			variant = GameVariant.valueOf(pm.get("variant"));
		}
		if (pm.containsKey("zoom")) {
			zoom = Float.valueOf(pm.get("zoom"));
		}
		if (pm.containsKey("keys")) {
			keyMap = keyMap(pm.get("keys"));
		}
	}

	@Override
	public String toString() {
		return "Settings [fullScreen=" + fullScreen + ", variant=" + variant + ", zoom=" + zoom + "]";
	}
}