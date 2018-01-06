package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.awt.*;
import java.io.FileNotFoundException;
import java.util.Optional;

public class Main extends Application {

    private Group root;
    private Scene mScene;
    private Player player;
    public static boolean gameStarted = false;
    public static Messenger messenger;
    public final static double WIDTH = 1200, HEIGHT = 750;
    public Stage mPrimaryStage;
    public static TextArea logger;
    public static int NUMBER_OF_BOTS = 5;

    @Override
    public void start(Stage primaryStage) throws Exception{
        root = new Group();
        this.mPrimaryStage = primaryStage;
        mPrimaryStage.setTitle("Lone Predator: Survival");
        mScene = new Scene(root, WIDTH, HEIGHT);

        // ask for number of bots
        NUMBER_OF_BOTS = dialogueNumberBots();

        //build player
        player = new Player(root, primaryStage, mScene, 100, 100);

        Label energyLabel = new Label(Soldier.ENERGY_FILLER_VALUE + "100%");
        Label healthLabel = new Label(Soldier.HEALTH_FILLER_VALUE + "100%");
        Label ammoLabel = new Label("Ammo remaining: 25");
        Label itemsLabel = new Label("You have no items");

        energyLabel.setTranslateX(20);  energyLabel.setTranslateY(20);
        healthLabel.setTranslateX(20);  healthLabel.setTranslateY(40);
        ammoLabel.setTranslateX(20);    ammoLabel.setTranslateY(60);
        itemsLabel.setTranslateX(20);   itemsLabel.setTranslateY(80);

        root.getChildren().addAll(energyLabel, healthLabel, ammoLabel, itemsLabel);

        player.setEnergyLabel(energyLabel);
        player.setHealthLabel(healthLabel);
        player.setAmmoLabel(ammoLabel);
        player.setItemsLabel(itemsLabel);

        Label messageLabel = new Label(); root.getChildren().add(messageLabel);
        messenger = new Messenger(messageLabel);

        mPrimaryStage.setScene(mScene);
        mPrimaryStage.setX(0); mPrimaryStage.setY(0);
        mPrimaryStage.show();

        Group logRoot = new Group();
        Scene logScene = new Scene(logRoot, 400 , 500);
        logger = new TextArea("The match has started.\nUse W, A, S, and D (or the arrow keys) to move, and use J to fire. Use K to stop moving.");
        logger.setEditable(false);
        logger.setWrapText(true);
        javafx.scene.text.Font jfxft = new javafx.scene.text.Font("Fira Sans Bold", 13);
        logger.setFont(jfxft);
        logger.setTranslateX(0); logger.setTranslateY(0);
        logger.setPrefWidth(400);
        logger.setPrefHeight(500);
        logRoot.getChildren().add(logger);
        Stage logStage = new Stage(); logStage.setScene(logScene);
        logStage.setX(mPrimaryStage.getWidth());
        logStage.setY(0);
        logStage.show();

        mPrimaryStage.requestFocus();

        player.getRawSprite().requestFocus();

        //add enemies
        try {
            for (int i = 0; i < NUMBER_OF_BOTS; i++) {
                new Enemy(root, mPrimaryStage, mScene, (int) (Math.random() * (WIDTH - 20)), (int) (Math.random() * (HEIGHT - 20)));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        runGame();
    }

    private String contentText = "How many bots would you like to fight against?";
    private int dialogueNumberBots() {
        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Number of bots");
        dialog.setHeaderText("Number of bots");
        dialog.setContentText(contentText);
        Optional<String> result = dialog.showAndWait();
        try {
            if (result.isPresent()) {
                int res = Integer.valueOf(result.get());
                if (res <= 1000) return res; else {
                    contentText = "Bot count may not exceed 1000, try again:";
                    return dialogueNumberBots();
                }
            }
        } catch (NumberFormatException e) {
            contentText = "Please enter a valid integer between 0 and 100:";
            e.printStackTrace();
            return dialogueNumberBots();
        }

        contentText = "I couldn't read that result, try again:";
        return dialogueNumberBots(); //result was not present
    }

    static boolean NORTH, SOUTH, EAST, WEST; static boolean stopAll;
    boolean fire; double angle; int fireTimeout = 0; final int RECOMMENDED_FIRE_TIMEOUT = 5;
    double mx, my;
    static int currentAmmoBoxTag = 0;
    static int currentEnergyItemTag = 0;

    private void runGame() {
        gameStarted = true;

        messenger.sendMessage("Be the last one standing.");

        mScene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case UP: stopAll = false; NORTH = true; SOUTH = false; EAST = false; WEST = false; break;
                case W: stopAll = false; NORTH = true; SOUTH = false; EAST = false; WEST = false;break;
                case DOWN: stopAll = false; SOUTH = true; NORTH = false; EAST = false; WEST = false;break;
                case S: stopAll = false; SOUTH = true; NORTH = false; EAST = false; WEST = false; break;
                case LEFT: stopAll = false; WEST = true; NORTH = false; SOUTH = false; EAST = false;break;
                case A: stopAll = false; WEST = true; NORTH = false; SOUTH = false; EAST = false;break;
                case RIGHT: stopAll = false; EAST = true; NORTH = false; SOUTH = false; WEST = false;break;
                case D: stopAll = false; EAST = true; NORTH = false; SOUTH = false; WEST = false;break;

                case J: fire = true; break;
                case K: stopAll = true; break;
            }
        });

        mScene.setOnMouseClicked(e -> {
            fire = true;
        });

        mScene.setOnMouseMoved(e -> {
            mx = e.getX();
            my = e.getY();
            angle = player.rotateToPoint(mx, my);
        });

        final int[] frames = {0};

        AnimationTimer at = new AnimationTimer() {
            @Override
            public void handle(long now) {
                frames[0]++;

                if (NUMBER_OF_BOTS <= 0) {
                    gameStarted = false;
                    messenger.sendMessage("You have won!");
                    newLogMessage("You have won!");
                } else {
                    System.out.println(NUMBER_OF_BOTS + " to go!!");
                }

                if (!gameStarted) {
                    //someone won, tally up for star player
                    int highestDamage = 0;
                    Soldier starPlayer = null;
                    for (int i = 0; i < Soldier.soldiersCatalog.size(); i++) {
                        if (Soldier.soldiersCatalog.get(i).damageCount > highestDamage) {
                            highestDamage = Soldier.soldiersCatalog.get(i).damageCount;
                            starPlayer = Soldier.soldiersCatalog.get(i);
                        }
                    }

                    if (starPlayer != null) Main.newLogMessage("Star player is " + ((starPlayer.selfIndex > 0)? ("Bot " + starPlayer.selfIndex): "Player") +
                            " with the highest accurate shots: " + highestDamage);

                    this.stop();
                    return;
                }

                //movement checker
                int dx = 0, dy = 0;

//NORTH = false; SOUTH = false; EAST = false; WEST = false;
                if (NORTH) {
                    dy -= Soldier.speed;
                    SOUTH = false; EAST = false; WEST = false;
                }

                if (SOUTH) {
                    dy += Soldier.speed;
                    NORTH = false; EAST = false; WEST = false;
                }

                if (EAST) {
                    dx += Soldier.speed;
                    NORTH = false; SOUTH = false; WEST = false;
                }

                if (WEST) {
                    dx -= Soldier.speed;
                    NORTH = false; SOUTH = false; EAST = false;
                }

                if (stopAll) {
                    dx = 0; dy = 0;
                    NORTH = false; SOUTH = false; EAST = false; WEST = false;
                }

                player.moveBy(dx, dy);

                //player clicked "J" to fire
                if ((fire) && (fireTimeout == 0)) {
                    fireTimeout = RECOMMENDED_FIRE_TIMEOUT;
                    System.out.println("pew");
                    player.fireBullet(angle, null, new double[]{mx, my});
                } else {
                    //System.out.println("Can't fire, firetimeout = " + fireTimeout);
                }

                if (dx != 0 || dy != 0) {
                    //player moved.
                    angle = player.rotateToPoint(mx, my);
                }

                //ammo box if ammo box count is empty
                AmmoBox boxInfo = (AmmoBox) SupplyItem.getBoxInfoByTag(String.valueOf("A" + currentAmmoBoxTag));
                if (boxInfo == null) { //that means AmmoBox updated the currentAmmoBoxTag after destroying itself
                    new AmmoBox(Math.random() * (WIDTH - 100), Math.random() * (HEIGHT - 100), root, String.valueOf("A" + currentAmmoBoxTag));
                    //("New ammobox created at " + a.rawLabel.getLayoutX() + ", " + a.rawLabel.getLayoutY() + " using new tag " + currentAmmoBoxTag);
                } else {
                    //box exists at boxInfo[1]
                    //("Box already exists under tag " + currentAmmoBoxTag);
                    boxInfo.updatePlayerInformation(player);
                }

                //add energy drink if energy drink count is empty
                EnergyFuel drinkInfo = (EnergyFuel) SupplyItem.getBoxInfoByTag(String.valueOf("E" + currentEnergyItemTag));
                if (drinkInfo == null) {
                    new EnergyFuel(Math.random() * (WIDTH - 100), Math.random() * (HEIGHT - 100), root, String.valueOf("E" + currentEnergyItemTag));
                } else {
                    drinkInfo.updatePlayerInformation(player);
                }

                //request focus to player
                player.getRawSprite().requestFocus();


                //reset all values
                fire = false; fireTimeout--;
                if (fireTimeout <= 0) fireTimeout = 0;
            }
        };

        at.start();
    }

    public static void newLogMessage(String message) {
        logger.appendText("\n\n" + message);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
