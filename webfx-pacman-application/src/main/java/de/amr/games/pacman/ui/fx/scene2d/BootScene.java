/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.ui.fx.app.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import javafx.geometry.Rectangle2D;

import static de.amr.games.pacman.lib.Globals.RND;

/**
 * @author Armin Reichert
 */
public class BootScene extends GameScene2D {

	public BootScene(PacManGames2dUI ui) {
		super(ui);
	}

	@Override
	public void init() {
		setRoundedCorners(false);
		setScoreVisible(false);
	}

	@Override
	public void render() {
		double start = 1.0; // seconds
		var timer = state().timer();

		if (timer.tick() == 1) {
			drawSceneBackground();
		}

		else if (timer.betweenSeconds(start, start + 1) && timer.tick() % 4 == 0) {
			paintRandomHexCodes();
		}

		else if (timer.betweenSeconds(start + 1, start + 2.5) && timer.tick() % 4 == 0) {
			paintRandomSprites();
		}

		else if (timer.atSecond(start + 2.5)) {
			paintGrid(16);
		}

		else if (timer.atSecond(start + 3)) {
			gameController().terminateCurrentState();
		}
	}

	@Override
	protected void drawSceneContent() {
		// not used here
	}

	private void paintRandomHexCodes() {
		drawSceneBackground();
		g.setFill(ArcadeTheme.PALE);
		g.setFont(sceneFont());
		for (int row = 0; row < TILES_Y; ++row) {
			for (int col = 0; col < TILES_X; ++col) {
				var hexCode = Integer.toHexString(RND.nextInt(16));
				g.fillText(hexCode, s(t(col)), s(t(row + 1)));
			}
		}
	}

	private void paintRandomSprites() {
		drawSceneBackground();
		for (int row = 0; row < TILES_Y / 2; ++row) {
			if (RND.nextInt(100) > 10) {
				var region1 = randomSpritesheetTile();
				var region2 = randomSpritesheetTile();
				var splitX = TILES_X / 8 + RND.nextInt(TILES_X / 4);
				for (int col = 0; col < TILES_X / 2; ++col) {
					var region = col < splitX ? region1 : region2;
					drawSprite(region, region.getWidth() * col, region.getHeight() * row);
				}
			}
		}
	}

	private Rectangle2D randomSpritesheetTile() {
		var source = ui().spritesheet().source();
		var raster = ui().spritesheet().raster();
		double x = RND.nextDouble() * (source.getWidth() - raster);
		double y = RND.nextDouble() * (source.getHeight() - raster);
		return new Rectangle2D(x, y, raster, raster);
	}

	private void paintGrid(int raster) {
		drawSceneBackground();
		var numRows = TILES_Y / 2;
		var numCols = TILES_X / 2;
		g.setStroke(ArcadeTheme.PALE);
		g.setLineWidth(s(2.0));
		for (int row = 0; row <= numRows; ++row) {
			g.strokeLine(0, s(row * raster), s(WIDTH_UNSCALED), s(row * raster));
		}
		for (int col = 0; col <= numCols; ++col) {
			g.strokeLine(s(col * raster), 0, s(col * raster), s(HEIGHT_UNSCALED));
		}
	}
}