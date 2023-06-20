/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.util;

import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;

/**
 * Game clock with modifiable frame rate.
 * 
 * @author Armin Reichert
 */
public class GameClock {

	private static void snooze() {
		// rest
	}

	public final IntegerProperty targetFrameratePy = new SimpleIntegerProperty(this, "targetFramerate", 30) {
		@Override
		protected void invalidated() {
			updateClock();
		}
	};
	public final BooleanProperty pausedPy = new SimpleBooleanProperty(this, "paused", false);
	public final BooleanProperty timeMeasuredPy = new SimpleBooleanProperty(this, "timeMeasured", false);

	private Runnable onTick = GameClock::snooze;
	private Runnable onRender = GameClock::snooze;
	private Timeline timeline;
	private long updateCount;
	private long ticksPerSec;
	private long countTicksStartTime;
	private long ticks;

	public GameClock() {
	}

	public GameClock(Runnable onTick, Runnable onRender) {
		this.onTick = onTick;
		this.onRender = onRender;
		createClock();
	}

	public void setOnTick(Runnable onTick) {
		this.onTick = onTick;
	}

	public void setOnRender(Runnable onRender) {
		this.onRender = onRender;
	}

	private void createClock() {
		int targetFPS = targetFrameratePy.get();
		var tick = new KeyFrame(Duration.seconds(1.0 / targetFPS), e -> executeSingleStep(!isPaused()));
		timeline = new Timeline(targetFPS, tick);
		timeline.setCycleCount(Animation.INDEFINITE);
	}

	private void updateClock() {
		boolean running = timeline.getStatus() == Status.RUNNING;
		if (running) {
			timeline.stop();
		}
		createClock();
		if (running) {
			start();
		}
	}

	public void start() {
		timeline.play();
	}

	public void stop() {
		timeline.stop();
	}

	public boolean isRunning() {
		return timeline.getStatus() == Status.RUNNING;
	}

	public boolean isPaused() {
		return pausedPy.get();
	}

	public long getUpdateCount() {
		return updateCount;
	}

	public long getFPS() {
		return ticksPerSec;
	}

	public void setTimeMeasured(boolean measured) {
		timeMeasuredPy.set(measured);
	}

	public void executeSteps(int n, boolean updateEnabled) {
		for (int i = 0; i < n; ++i) {
			executeSingleStep(updateEnabled);
		}
	}

	public void executeSingleStep(boolean updateEnabled) {
		long tickTime = System.nanoTime();
		if (updateEnabled) {
			runPhase(onTick, "Update phase: {} milliseconds");
			updateCount++;
		}
		runPhase(onRender, "Render phase: {} milliseconds");
		++ticks;
		computeFrameRate(tickTime);
	}

	private void runPhase(Runnable phase, String logMessage) {
		if (timeMeasuredPy.get()) {
			double startNanos = System.nanoTime();
			phase.run();
			double durationNanos = System.nanoTime() - startNanos;
			Logger.info(logMessage, durationNanos / 1e6);
		} else {
			phase.run();
		}
	}

	private void computeFrameRate(long time) {
		if (time - countTicksStartTime > 1e9) {
			ticksPerSec = ticks;
			ticks = 0;
			countTicksStartTime = time;
		}
	}
}