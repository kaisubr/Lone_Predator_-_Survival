package sample;


import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;
import javafx.util.Duration;

import java.awt.geom.Point2D;
import java.util.TimerTask;

/**
 * Lone Predator - Survival, file created in sample by Kailash Sub.
 */
public class Bullet {
    public static final double MAXIMUM_RANGE = 250;
    double mAngle;
    Rectangle bullet;
    Soldier soldier;

    public Bullet(double angle, Soldier soldier) {
        if (angle >= 0 && angle <= 90) mAngle = 90 - angle;
        if (angle >= 90 && angle <= 180) mAngle = 360 - (angle - 90);
        if (angle >= 180 && angle <= 270) mAngle = 180 + (270 - angle);
        if (angle >= -90 && angle <= 0) mAngle = 180 - (angle - 270);

        bullet = RectangleBuilder.create()
                .x(soldier.getRawSprite().getX())
                .y(soldier.getRawSprite().getY())
                .width(7)
                .height(7)
                .fill(Color.color(0, 0.502, 0.502))
                .arcWidth(10).arcHeight(10)
                .build();
        soldier.root.getChildren().add(bullet);
        System.out.println(mAngle);
        this.soldier = soldier;
    }

    public Rectangle getRawBullet() {
        return bullet;
    }

    /**
     * If the bullet hit the expected target, it returns true. If expectedTarget is null, will search through all soldiers
     * for contact.
     * @param expectedTarget can be null.
     * @return if target was hit.
     */

    final Timeline timeline = new Timeline();

    public void beginCourse(Soldier expectedTarget, final Double distance) {
        final Object[] hitTarget = {null, expectedTarget};

        //fire bullet
        //rate is 2 pixels/second
        double theta = mAngle;
        double radius = ((distance < MAXIMUM_RANGE))? distance : MAXIMUM_RANGE;
        double soldX = soldier.getRawSprite().getX(); double soldY = soldier.getRawSprite().getY();
        double xDisplacement = Math.cos(Math.toRadians(theta)) * radius;
        double yDisplacement = Math.sin(Math.toRadians(theta)) * radius;

        final KeyValue valueX = new KeyValue(bullet.xProperty(), soldX + xDisplacement, Interpolator.EASE_IN);
        final KeyValue valueY = new KeyValue(bullet.yProperty(), soldY - yDisplacement, Interpolator.EASE_IN);
        final KeyFrame frame = new KeyFrame(Duration.millis(1000), valueX, valueY);
        timeline.getKeyFrames().add(frame);

        timeline.play();


        timeline.setOnFinished(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(200), bullet);
            ft.setFromValue(1.0);
            ft.setToValue(0);
            ft.setCycleCount(1);
            ft.setAutoReverse(false);
            ft.play();

            hitTarget[0] = false;

            double bounds = (soldier.isEnemy)? Soldier.PLAYER_BOUNDS : Soldier.LARGER_PLAYER_BOUNDS;

            if (expectedTarget != null) {
                double targetX = expectedTarget.getRawSprite().getX(), targetY = Main.HEIGHT - expectedTarget.getRawSprite().getY();
                double bX = bullet.getX(), bY = Main.HEIGHT - bullet.getY(); System.out.println(bX + ", " + bY + " has targeted target " + targetX + ", " + targetY);
                double localDist = (targetX == bX)? ((targetY > bY)? (targetY - bY) : (bY - targetY)) :
                        (java.awt.geom.Point2D.distance(targetX, targetY, bX, bY));

                if (localDist <= bounds) {
                    hitTarget[0] = true;
                    hitTarget[1] = expectedTarget;
                }
            } else {
                //search every soldier for contact
                //check if it hit someone
                System.out.println("Target is not locked.");

                for (int i = 0; i < Soldier.soldiersCatalog.size(); i++) {
                    double targetX = Soldier.soldiersCatalog.get(i).getRawSprite().getX() + 16,
                            targetY = Main.HEIGHT - (Soldier.soldiersCatalog.get(i).getRawSprite().getY() - 16);
                    double bX = bullet.getX(), bY = Main.HEIGHT - bullet.getY();

                    double localDist = (targetX == bX)? ((targetY > bY)? (targetY - bY) : (bY - targetY)) :
                            (java.awt.geom.Point2D.distance(targetX, targetY, bX, bY));

                    System.out.println("Searching index " + i);
                    System.out.println("local distance calc " + localDist);

                    if (localDist <= bounds) {
                        hitTarget[0] = true;
                        hitTarget[1] = Soldier.soldiersCatalog.get(i);
                    }
                }
            }

            handleContact(hitTarget); //will not handle repeated contact
        });

    }

    private void handleContact(Object[] hitTarget) {
        if ((boolean) hitTarget[0]) {
            System.out.println("HIT on target index " + ((Soldier) hitTarget[1]).selfIndex);
            ((Soldier) hitTarget[1]).hitMeLast = soldier; //soldier hit target
            soldier.damageCount++;
            ((Soldier) hitTarget[1]).healthAcquired(-7);
            System.out.println(((Soldier) hitTarget[1]).health);
            //contact, missedbullet reset to 0
            soldier.missedBullets = 0;
        } else {
            System.out.println("Soldier " + soldier.selfIndex + ": NO HIT FOUND, MISSED BULLET UP ONE FROM " + soldier.missedBullets);
            soldier.missedBullets++;
        }
    }
}
