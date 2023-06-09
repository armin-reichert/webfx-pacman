/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.PacManIntro;
import de.amr.games.pacman.controller.PacManIntro.State;
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.app.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.GhostAnimationsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.PacAnimationsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.SpritesheetPacManGame;

import static de.amr.games.pacman.lib.Globals.TS;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghost are presented one after another, then Pac-Man is chased by the ghosts, turns the card and hunts the ghost
 * himself.
 * 
 * @author Armin Reichert
 */
public class PacManIntroScene extends GameScene2D {

	private static final String QUOTE = "\"";

	private PacManIntro intro;
	private PacManIntro.Context ic;
	private Signature signature;

	public PacManIntroScene(PacManGames2dUI ui) {
		super(ui);
		signature = new Signature();
		overlay.getChildren().add(signature.root());
	}

	@Override
	public void init() {
		var ss = (SpritesheetPacManGame) ui().spritesheet();

		setCreditVisible(true);
		setScoreVisible(true);

		signature.setNameFont(ui().theme().font("font.handwriting", 9));
		signature.hide();

		intro = new PacManIntro();
		intro.addStateChangeListener((oldState, newState) -> {
			if (oldState == PacManIntro.State.SHOWING_POINTS) {
				signature.show(t(5.5), t(32.0));
			}
		});
		ic = intro.context();

		ic.pacMan.setAnimations(new PacAnimationsPacManGame(ic.pacMan, ss));
		ic.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimationsPacManGame(ghost, ss)));
		ic.blinking.reset();

		intro.changeState(State.START);
	}

	@Override
	public void update() {
		intro.update();
	}

	@Override
	public void end() {
		ui().soundHandler().stopVoice();
		signature.hide();
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.anyPressed(PacManGames2d.KEY_ADD_CREDIT, PacManGames2d.KEY_ADD_CREDIT_NUMPAD)) {
			ui().addCredit();
		} else if (Keyboard.anyPressed(PacManGames2d.KEY_START_GAME, PacManGames2d.KEY_START_GAME_NUMPAD)) {
			ui().startGame();
		} else if (Keyboard.pressed(PacManGames2d.KEY_SELECT_VARIANT)) {
			ui().switchGameVariant();
		} else if (Keyboard.pressed(PacManGames2d.KEY_PLAY_CUTSCENES)) {
			ui().startCutscenesTest();
		}
	}

	@Override
	public void drawSceneContent() {
		var timer = intro.state().timer();
		drawGallery();
		switch (intro.state()) {
		case SHOWING_POINTS: {
			drawPoints();
		}
			break;

		case CHASING_PAC: {
			drawPoints();
			drawBlinkingEnergizer();
			drawGuys(flutter(timer.tick()));
			drawMidwayCopyright(t(4), t(32));
		}
			break;

		case CHASING_GHOSTS: {
			drawPoints();
			drawGuys(0);
			drawMidwayCopyright(t(4), t(32));
		}
			break;

		case READY_TO_PLAY: {
			drawPoints();
			drawGuys(0);
			drawMidwayCopyright(t(4), t(32));
		}
			break;

		default: {
			// nothing to do
		}
		}
		drawLevelCounter(t(24), t(34), game().levelCounter());
	}

	@Override
	protected void drawSceneInfo() {
		drawTileGrid(TILES_X, TILES_Y);
	}

	// TODO inspect in MAME what's really going on here
	private int flutter(long time) {
		return time % 5 < 2 ? 0 : -1;
	}

	private void drawGallery() {
		var ss = (SpritesheetPacManGame) ui().spritesheet();

		int tx = ic.leftTileX;
		if (ic.titleVisible) {
			drawText("CHARACTER / NICKNAME", ArcadeTheme.PALE, sceneFont(), t(tx + 3), t(6));
		}
		for (int id = 0; id < 4; ++id) {
			if (!ic.ghostInfo[id].pictureVisible) {
				continue;
			}
			int ty = 7 + 3 * id;
			drawSpriteOverBoundingBox(ss.ghostFacingRight(id), t(tx) + 4, t(ty));
			if (ic.ghostInfo[id].characterVisible) {
				var text = "-" + ic.ghostInfo[id].character;
				var color = ui().theme().color(String.format("ghost.%d.color.normal.dress", id));
				drawText(text, color, sceneFont(), t(tx + 3), t(ty + 1));
			}
			if (ic.ghostInfo[id].nicknameVisible) {
				var text = QUOTE + ic.ghostInfo[id].ghost.name() + QUOTE;
				var color = ui().theme().color(String.format("ghost.%d.color.normal.dress", id));
				drawText(text, color, sceneFont(), t(tx + 14), t(ty + 1));
			}
		}
	}

	private void drawBlinkingEnergizer() {
		if (ic.blinking.on()) {
			g.setFill(ui().theme().color("pacman.maze.foodColor"));
			g.fillOval(s(t(ic.leftTileX)), s(t(20)), s(TS), s(TS));
		}
	}

	private void drawGuys(int shakingAmount) {
		if (shakingAmount == 0) {
			ic.ghosts().forEach(ghost -> drawGhostSprite(ghost));
		} else {
			drawGhostSprite(ic.ghost(0));
			drawGhostSprite(ic.ghost(3));
			// shaking ghosts effect, not quite as in original game
			g.save();
			g.translate(shakingAmount, 0);
			drawGhostSprite(ic.ghost(1));
			drawGhostSprite(ic.ghost(2));
			g.restore();
		}
		drawPacSprite(ic.pacMan);
	}

	private void drawPoints() {
		var font6 = ui().theme().font("font.arcade", s(6));
		int tx = ic.leftTileX + 6;
		int ty = 25;
		g.setFill(ui().theme().color("pacman.maze.foodColor"));
		g.fillRect(s(t(tx) + 4), s(t(ty - 1) + 4), s(2), s(2));
		if (ic.blinking.on()) {
			g.fillOval(s(t(tx)), s(t(ty + 1)), s(TS), s(TS));
		}
		drawText("10", ArcadeTheme.PALE, sceneFont(), t(tx + 2), t(ty));
		drawText("PTS", ArcadeTheme.PALE, font6, t(tx + 5), t(ty));
		drawText("50", ArcadeTheme.PALE, sceneFont(), t(tx + 2), t(ty + 2));
		drawText("PTS", ArcadeTheme.PALE, font6, t(tx + 5), t(ty + 2));
	}
}