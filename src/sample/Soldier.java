package sample;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * Lone Predator - Survival, file created in sample by Kailash Sub.
 */
public abstract class Soldier {
    public static final double PLAYER_BOUNDS = 50;
    public static final double LARGER_PLAYER_BOUNDS = 100;
    public static List<Soldier> soldiersCatalog = new ArrayList<>();
    public int selfIndex;
    public boolean isEnemy;
    public boolean alive;
    public ImageView playerSprite;
    public Label floatingHealthLabel;
    public static final int FLOATING_PADDING = 16;
    public Group root; public Stage primaryStage; public Scene mScene;
    double health = 100, energy = 100, ammo = 25;
    public static final String AMMO_FILLER_VALUE = "Ammo remaining: ";
    public static final String ENERGY_FILLER_VALUE = "Energy remaining: ";
    public static final String HEALTH_FILLER_VALUE = "Health remaining: ";
    public static final int speed = 2;
    private java.util.Timer t;
    public Soldier hitMeLast;
    public int damageCount = 0;
    public int missedBullets = 0;

    public Soldier(Group root, Stage primaryStage, Scene mScene, double startX, double startY, String imageResource) throws FileNotFoundException {
        playerSprite = ImageViewBuilder.create().image(new Image(getClass().getResourceAsStream(imageResource))).build();
        this.root = root; this.primaryStage = primaryStage; this.mScene = mScene;

        playerSprite.setFitHeight(32);
        playerSprite.setFitWidth(32);
        playerSprite.setPreserveRatio(true);
        playerSprite.setX(startX); playerSprite.setY(startY);

        floatingHealthLabel = new Label(String.valueOf((int) health));
        floatingHealthLabel.setPrefWidth(32);
        floatingHealthLabel.setAlignment(Pos.CENTER);
        floatingHealthLabel.setTextAlignment(TextAlignment.CENTER);
        floatingHealthLabel.setLayoutX(playerSprite.getX());
        floatingHealthLabel.setLayoutY(playerSprite.getY() - FLOATING_PADDING);
        floatingHealthLabel.setFont(new Font("Fira Sans", 12));

        root.getChildren().addAll(playerSprite, floatingHealthLabel);

        System.out.println("[BUILD] new soldier created at X " + playerSprite.getX() + ", Y " + playerSprite.getY() + " h/w " + playerSprite.getFitHeight() + ", set to index " + (soldiersCatalog.size()));
        t = new java.util.Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (Main.gameStarted && alive) {
                    if (!isEnemy) {
                        energyAcquired(-0.01);
                    }

                    if (health < 100) {
                        //health regen
                        healthAcquired(3);
                    }
                }
            }
        }, 0, 500);

        soldiersCatalog.add(this);
        selfIndex = soldiersCatalog.size() - 1;

        alive = true;
    }

    public ImageView getRawSprite() {
        return playerSprite;
    }

    /**
     * Rotates the sprite to the point mx, my
     * @param mx x coor
     * @param my y coor
     * @return theta value for turn
     */
    public double rotateToPoint(double mx, double my) {
        double originX = playerSprite.getX();
        double originY = playerSprite.getY();
        double dx = mx - originX;
        double dy = my - originY; //?
        double theta = Math.toDegrees(Math.atan2(dy,dx)) + 90;
        playerSprite.setRotate(theta);
        //System.out.println("x " + mx + ", y " + my + " is point. PLAYER is " + originX + ", " + originY + ". \n \t dx = " + dx + ". dy = " + dy + ". theta = " + theta);

        return theta;
    }

    public double getAngleBetween(double ax, double ay, double bx, double by) {
        double originX = ax;
        double originY = ay;
        double dx = bx - originX;
        double dy = by - originY;
        return Math.toDegrees(Math.atan2(dy,dx)) + 90;
    }

    public void moveBy(int dx, int dy) {
        playerSprite.setX(playerSprite.getX() + dx);
        playerSprite.setY(playerSprite.getY() + dy);

        floatingHealthLabel.setLayoutX(playerSprite.getX());
        floatingHealthLabel.setLayoutY(playerSprite.getY() - FLOATING_PADDING);
        if (dx != 0 || dy != 0 ) energy -= 0.025;

        if ((playerSprite.getX() >= (Main.WIDTH - 10) || playerSprite.getY() >= (Main.HEIGHT - 10) || playerSprite.getX() <= 10 || playerSprite.getY() <= 10)) {
            //out of bounds, lose health
            healthAcquired(-1);
        }
    }

    public void repaint() {
        playerSprite.relocate(playerSprite.getX(), playerSprite.getY());
    }

    public void fireBullet(double angle, Soldier expectedTarget, double[] mouseCoorNoSuchTarget) {
        if (ammo > 0) {
            ammo--;
            Bullet b = new Bullet(angle, this);
            if (expectedTarget != null) {
                //reset according to new point
                double targetX = expectedTarget.getRawSprite().getX(), targetY = Main.HEIGHT - expectedTarget.getRawSprite().getY();
                double myX = this.getRawSprite().getX(), myY = Main.HEIGHT - this.getRawSprite().getY();
                //if target above, above - below, else, above - below
                Double distance = (targetX == myX)? ((targetY > myY)? (targetY - myY) : (myY - targetY)) :
                        (java.awt.geom.Point2D.distance(
                                this.getRawSprite().getX(), this.getRawSprite().getY(),
                                expectedTarget.getRawSprite().getX(), expectedTarget.getRawSprite().getY()));
                b.beginCourse(expectedTarget, distance);
            } else {
                if (mouseCoorNoSuchTarget == null) throw new NullPointerException("You must pass mouse coordinates if expected target is not specified.");

                //reset according to new point
                double targetX = mouseCoorNoSuchTarget[0], targetY = Main.HEIGHT - mouseCoorNoSuchTarget[1];
                double myX = this.getRawSprite().getX(), myY = Main.HEIGHT - this.getRawSprite().getY();

                //if target above, above - below, else, above - below
                Double distance = (targetX == myX)? ((targetY > myY)? (targetY - myY) : (myY - targetY)) :
                        (java.awt.geom.Point2D.distance(
                                this.getRawSprite().getX(), this.getRawSprite().getY(),
                                targetX, mouseCoorNoSuchTarget[1]));

                b.beginCourse(null, distance);
            }

            onAmmoChanged(ammo);
        }
    }

    public void ammoAcquired(int howMuch) {
        ammo += howMuch;
        onAmmoChanged(ammo);
    }

    public void energyAcquired(double howMuch) {
        energy += howMuch;
        onEnergyChanged(energy);
    }

    public void healthAcquired(int howMuch){
        health += howMuch;
        health = (health >= 100)? 100 : health;
        health = (health < 0)? 0 : health;

        onHealthChanged(health);
        Platform.runLater(() -> {
            floatingHealthLabel.setText(String.valueOf((int) health));
            if (howMuch < 0) {
                ColorAdjust colorAdjustInit = new ColorAdjust();
                colorAdjustInit.setHue(360);
                colorAdjustInit.setBrightness(1);

                //apply now.
                getRawSprite().setEffect(colorAdjustInit);

                ColorAdjust colorAdjustEnd = new ColorAdjust();
                colorAdjustEnd.setHue(0);
                colorAdjustInit.setBrightness(0);

                final Timeline timeline = new Timeline();
                final KeyValue colorAdjustValue = new KeyValue(getRawSprite().effectProperty(), colorAdjustEnd);
                final KeyFrame keyFrame = new KeyFrame(Duration.millis(200), colorAdjustValue);
                timeline.getKeyFrames().add(keyFrame);
                timeline.play();
            }
        });
    }

    public void destroy() {
        root.getChildren().removeAll(getRawSprite(), floatingHealthLabel);
        ammo = 0;
        t.cancel();
        alive = false;

    }

    public abstract void onEnergyChanged(double energy);
    public abstract void onAmmoChanged(double ammo);
    public abstract void onHealthChanged(double health);
}
