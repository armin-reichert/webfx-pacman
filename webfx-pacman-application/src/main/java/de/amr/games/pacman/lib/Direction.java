/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * The move directions inside the world.
 * 
 * @author Armin Reichert
 */
public enum Direction {

	LEFT(-1, 0), RIGHT(1, 0), UP(0, -1), DOWN(0, 1);

	private static final Direction[] OPPOSITE = { RIGHT, LEFT, DOWN, UP };

	public static Stream<Direction> stream() {
		return Stream.of(values());
	}

	public static List<Direction> shuffled() {
		List<Direction> dirs = Arrays.asList(values());
		Collections.shuffle(dirs);
		return dirs;
	}

	private final Vector2i vector;

	private Direction(int x, int y) {
		vector = new Vector2i(x, y);
	}

	public Vector2i vector() {
		return vector;
	}

	public Direction opposite() {
		return OPPOSITE[ordinal()];
	}

	public Direction succAntiClockwise() {
		switch (this) {
		case UP:
			return LEFT;
		case LEFT:
			return DOWN;
		case DOWN:
			return RIGHT;
		case RIGHT:
			return UP;
		default:
			throw new IllegalStateException();
		}
	}

	public Direction succClockwise() {
		switch (this) {
		case UP:
			return RIGHT;
		case RIGHT:
			return DOWN;
		case DOWN:
			return LEFT;
		case LEFT:
			return UP;
		default:
			throw new IllegalStateException();
		}
	}

	public boolean isVertical() {
		return this == UP || this == DOWN;
	}

	public boolean isHorizontal() {
		return this == LEFT || this == RIGHT;
	}

	public boolean sameOrientation(Direction other) {
		return isHorizontal() && other.isHorizontal() || isVertical() && other.isVertical();
	}
}