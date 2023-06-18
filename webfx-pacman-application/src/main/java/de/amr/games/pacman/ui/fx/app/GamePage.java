package de.amr.games.pacman.ui.fx.app;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.PacManGames2d;
import de.amr.games.pacman.ui.fx.input.PacMouseSteering;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneConfiguration;
import de.amr.games.pacman.ui.fx.scene2d.GameScene2D;
import de.amr.games.pacman.ui.fx.scene2d.HelpMenu;
import de.amr.games.pacman.ui.fx.scene2d.HelpMenuFactory;
import de.amr.games.pacman.ui.fx.util.FlashMessageView;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.oneOf;

public class GamePage {

    public static final Duration MENU_FADING_DELAY = Duration.seconds(1.5);

    private final PacManGames2dUI ui;
    private final FlashMessageView flashMessageView = new FlashMessageView();
    private final StackPane root = new StackPane();
    private final BorderPane layoutPane = new BorderPane();
    private final BorderPane rootPane = new BorderPane();
    private final Canvas canvas = new Canvas();
    private final Pane popupLayer = new Pane();
    private final HelpMenuFactory helpMenuFactory = new HelpMenuFactory();
    private final HelpMenu helpMenu = new HelpMenu();
    private final Pane helpButton = new Pane();

    private GameScene2D gameScene2D;
    private double scaling = 1.0;

    public GamePage(PacManGames2dUI ui) {
        this.ui = ui;

        root.getChildren().addAll(layoutPane, popupLayer, flashMessageView);

        //TODO in desktop version, corners are black, in GWT they are transparent (bug?) what is wanted here
        rootPane.setBackground(ResourceManager.coloredBackground(Color.BLACK));
        rootPane.setBorder(roundedBorder(ArcadeTheme.PALE, 20, 10));
        rootPane.setCenter(canvas);
        rootPane.heightProperty().addListener((py, ov, nv) -> scale(scaling));

        layoutPane.setBackground(ui.theme().background("wallpaper.background"));
        layoutPane.setCenter(rootPane);

        helpButton.setOnMouseClicked(e -> { e.consume(); showHelpMenu(); });
        helpButton.setVisible(false);

        popupLayer.setOnMouseClicked(this::handleMouseClick);
        popupLayer.getChildren().addAll(helpButton, helpMenu);

        root.setOnKeyPressed(this::handleKeyPressed);
        new PacMouseSteering(this, popupLayer, () -> ui.game().level().map(GameLevel::pac).orElse(null));

        //layoutPane.setBorder(roundedBorder(Color.YELLOW, 10, 3));
        //popupLayer.setBorder(roundedBorder(Color.GREEN, 10, 3));

        scale(scaling);
    }

    private static Border roundedBorder(Color color, double cornerRadius, double width) {
        return new Border(
            new BorderStroke(color, BorderStrokeStyle.SOLID, new CornerRadii(cornerRadius), new BorderWidths(width)));
    }

    private void handleMouseClick(MouseEvent mouseEvent) {
        root.requestFocus();
        if (mouseEvent.isConsumed()) {
            return;
        }
        if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            var config = sceneConfiguration();
            if (gameScene2D == config.introScene()
                    || gameScene2D == config.creditScene() && ui.game().credit() == 0
                    || gameScene2D == config.playScene() && ui.game().level().get().isDemoLevel()) {
                ui.addCredit(); // simulate key press "5" (add credit)
            } else if (gameScene2D == config.creditScene() /* credit > 0 */) {
                ui.startGame(); // simulate key press "1" (start game)
            }
        }
    }

    private void showHelpMenu() {
        helpMenuFactory.setFont(ui.theme().font("font.monospaced", Math.max(6, 14 * scaling)));
        helpMenu.show(currentHelpMenu(), MENU_FADING_DELAY);
        helpMenu.setTranslateX(10 * scaling);
        helpMenu.setTranslateY(30 * scaling);
    }

    private Pane currentHelpMenu() {
        var game = ui.game();
        var gameState = GameController.it().state();
        Pane menu = null;
        if (gameState == GameState.INTRO) {
            menu = helpMenuFactory.menuIntro();
        } else if (gameState == GameState.CREDIT) {
            menu = helpMenuFactory.menuCredit();
        } else if (oneOf(gameState, GameState.READY, GameState.HUNTING, GameState.PACMAN_DYING, GameState.GHOST_DYING)) {
            if (game.level().isPresent()) {
                menu = game.level().get().isDemoLevel() ? helpMenuFactory.menuDemoLevel() : helpMenuFactory.menuPlaying();
            }
        }
        return menu;
    }

    public void update() {
        if (gameScene2D != null) {
            gameScene2D.update();
        }
    }

    public void render() {
        if (gameScene2D != null) {
            gameScene2D.render();
        }
        flashMessageView.update();
    }

    Parent root() {
        return root;
    }

    public void setGameScene(GameScene gameScene) {
        gameScene2D = (GameScene2D) gameScene;
        gameScene2D.setCanvas(canvas);
        scale(scaling);
        if (isPlayScene(gameScene)) {
            root.addEventHandler(KeyEvent.KEY_PRESSED, ui.keyboardPlayerSteering);
        } else {
            root.removeEventHandler(KeyEvent.KEY_PRESSED, ui.keyboardPlayerSteering);
        }
        root.requestFocus();
        updateHelpButton();
    }

    private boolean isPlayScene(GameScene gameScene) {
        var config = sceneConfiguration();
        return gameScene == config.playScene();
    }

    private GameSceneConfiguration sceneConfiguration() {
        return ui.game().variant() == GameVariant.MS_PACMAN ? ui.configMsPacMan : ui.configPacMan;
    }

    private void updateHelpButton() {
        double size = Math.ceil(10 * scaling);
        String key = ui.game().variant() == GameVariant.MS_PACMAN ? "mspacman.helpButton.icon" : "pacman.helpButton.icon";
        var icon = new ImageView(ui.theme().image(key));
        icon.setFitHeight(size);
        icon.setFitWidth(size);
        helpButton.getChildren().setAll(icon);
        helpButton.setCursor(Cursor.HAND);
        helpButton.setTranslateX(popupLayer.getWidth() - 20 * scaling);
        helpButton.setTranslateY(8 * scaling);
        helpButton.setVisible(sceneConfiguration().bootScene() != gameScene2D);
    }

    public void scale(double scaling) {
        this.scaling = scaling;

        double w = Math.round( (GameScene2D.WIDTH_UNSCALED  + 30) * scaling );
        double h = Math.round( (GameScene2D.HEIGHT_UNSCALED + 15) * scaling );

        if (h < 80) {
            Logger.info("Cannot scale down further");
            return;
        }

        rootPane.setMinSize(w, h);
        rootPane.setPrefSize(w, h);
        rootPane.setMaxSize(w, h);
        popupLayer.setMinSize(w, h);
        popupLayer.setPrefSize(w, h);
        popupLayer.setMaxSize(w, h);

        double borderWidth = Math.max(5, Math.ceil(h / 60));
        double cornerRadius = Math.ceil(15 * scaling);
        rootPane.setBorder(roundedBorder(ArcadeTheme.PALE, cornerRadius, borderWidth));

        if (gameScene2D != null) {
            gameScene2D.setScaling(scaling);
        }
        updateHelpButton();
        Logger.info("Scaled game page: scaling: {} height: {} border: {}", scaling, h, borderWidth);
    }

    public double getScaling() {
        return scaling;
    }

    public FlashMessageView flashMessageView() {
        return flashMessageView;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    protected void handleKeyPressed(KeyEvent keyEvent) {
        Keyboard.accept(keyEvent);
        handleKeyboardInput();
        if (ui.currentGameScene() != null) {
            ui.currentGameScene().handleKeyboardInput();
        }
        Keyboard.clearState();
    }

    protected void handleKeyboardInput() {
        if (Keyboard.pressed(PacManGames2d.KEY_SHOW_HELP)) {
            showHelpMenu();
        } else if (Keyboard.pressed(PacManGames2d.KEY_AUTOPILOT)) {
            ui.toggleAutopilot();
        } else if (Keyboard.pressed(PacManGames2d.KEY_BOOT)) {
            if (GameController.it().state() != GameState.BOOT) {
                ui.reboot();
            }
        } else if (Keyboard.pressed(PacManGames2d.KEY_DEBUG_INFO)) {
            Ufx.toggle(PacManGames2d.PY_SHOW_DEBUG_INFO);
        } else if (Keyboard.pressed(PacManGames2d.KEY_IMMUNITIY)) {
            ui.toggleImmunity();
        } else if (Keyboard.pressed(PacManGames2d.KEY_PAUSE)) {
            ui.togglePaused();
        } else if (Keyboard.pressed(PacManGames2d.KEY_PAUSE_STEP) || Keyboard.pressed(PacManGames2d.KEY_SINGLE_STEP)) {
            ui.oneSimulationStep();
        } else if (Keyboard.pressed(PacManGames2d.KEY_TEN_STEPS)) {
            ui.tenSimulationSteps();
        } else if (Keyboard.pressed(PacManGames2d.KEY_SIMULATION_FASTER)) {
            ui.changeSimulationSpeed(5);
        } else if (Keyboard.pressed(PacManGames2d.KEY_SIMULATION_SLOWER)) {
            ui.changeSimulationSpeed(-5);
        } else if (Keyboard.pressed(PacManGames2d.KEY_SIMULATION_NORMAL)) {
            ui.resetSimulationSpeed();
        } else if (Keyboard.pressed(PacManGames2d.KEY_QUIT)) {
            if (GameController.it().state() != GameState.BOOT && GameController.it().state() != GameState.INTRO) {
                ui.restartIntro();
            }
        } else if (Keyboard.pressed(PacManGames2d.KEY_TEST_LEVELS)) {
            ui.startLevelTestMode();
        } else if (Keyboard.pressed(PacManGames2d.KEY_FULLSCREEN)) {
            ui.stage.setFullScreen(true);
        }
    }
}