package sample;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.FileNotFoundException;

/**
 * Lone Predator - Survival, file created in sample by Kailash Sub.
 */
public class Enemy extends Soldier {

    Soldier target;
    AnimationTimer atMotion;
    private final static int NO_TARGETS_REMAINING = -1;
    boolean readHasDiedMessage = false;
    private double fireTimeoutLimit = 20;
    private int sameTargetCycle = 0;

    public Enemy(Group root, Stage primaryStage, Scene mScene, double startX, double startY) throws FileNotFoundException {
        super(root, primaryStage, mScene, startX, startY,
                "sprites/enemy" + ((int)(Math.random() * 3)) + ".png");
        isEnemy = true;
        target = findNewTarget();
        System.out.println(target.getRawSprite().getX());
        Soldier enemy = this;
        //follow target and minimize distance to 500 (range of bullet)
        final int[] dyNeeded = {0};

        final int[] fireTimeout = {12};
        atMotion = new AnimationTimer() {
            @Override
            public void handle(long now) {
                int dy = 0, dx = 0;

                //Target is no longer alive or number of missed hits is greater than 20, find new target
                if (!target.alive || missedBullets > 10) {
                    try {
                        Soldier newTarget = findNewTarget();
                        //give up, look for new target every 20 misses
                        if (newTarget != target) {
                            target = newTarget;
                        } else {
                            sameTargetCycle++;

                            missedBullets = 0;

                            //same target. every 2... faster shooting? moving?
                            if (sameTargetCycle == 2) {
                                fireTimeoutLimit = (fireTimeoutLimit >= 5 ? fireTimeoutLimit - 5 : 5);
                                Main.newLogMessage("Soldier " + selfIndex + " is growing stronger! New firetimeoutlimit is " + fireTimeoutLimit);
                                sameTargetCycle = 0;
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        //winner here.
                        Main.messenger.sendMessage("Bot " + selfIndex + " has won!");
                        Main.newLogMessage("Bot " + selfIndex + " has won!");
                        Main.gameStarted = false;
                        atMotion.stop();
                    }
                }

                if ((enemy.getRawSprite().getX() >= (Main.WIDTH - 20) ||
                        enemy.getRawSprite().getY() >= (Main.HEIGHT - 20) ||
                        enemy.getRawSprite().getX() <= 20 ||
                        enemy.getRawSprite().getY() <= 20)) {
                    //out of bounds, will lose health
                    enemy.rotateToPoint(target.getRawSprite().getX(), target.getRawSprite().getY());
                    dx = (enemy.getRawSprite().getX() <= 20)? 2 : -2;
                    dy = (enemy.getRawSprite().getY() <= 20)? 2 : -2;

                    moveBy(dx, dy);
                    return;
                }



                //reset according to new point
                double targetX = target.getRawSprite().getX(), targetY = Main.HEIGHT - target.getRawSprite().getY();
                double myX = enemy.getRawSprite().getX(), myY = Main.HEIGHT - enemy.getRawSprite().getY();
                //if target above, above - below, else, above - below
                double distance = (targetX == myX)? ((targetY > myY)? (targetY - myY) : (myY - targetY)) :
                        (java.awt.geom.Point2D.distance(
                                enemy.getRawSprite().getX(), enemy.getRawSprite().getY(),
                                target.getRawSprite().getX(), target.getRawSprite().getY()));

                if (health < 67 && distance <= Bullet.MAXIMUM_RANGE + Soldier.LARGER_PLAYER_BOUNDS + 20) { //the target is within firing range and we're at low health.
                    //escape
                    enemy.rotateToPoint(target.getRawSprite().getX(), target.getRawSprite().getY());
                    dy = (target.getRawSprite().getY() > enemy.getRawSprite().getY())? 2 : -2;
                    dx = (target.getRawSprite().getX() > enemy.getRawSprite().getX())? -2 : 2;
                    moveBy(dx, dy);
                    return;
                }

                //rotate
                double angle = enemy.rotateToPoint(targetX, target.getRawSprite().getY());

                if (distance > Bullet.MAXIMUM_RANGE){ //then move!
                    //slope between me and target
                    double slope = (targetY - myY)/(targetX - myX);
                    if (slope <= 1 || slope >= 1) {
                        dx = (targetX < myX)? -1 : 1; //if slope is less than 0 go left
                        dy = (targetY < myY)? - 1 : 1; //keep retrying until x's are aligned...?
                    } else if (slope == 1) {
                        dx = (targetX < myX)? -1 : 1;
                        dy = 0;
                    } else if (targetX == myX) { //has problems when run with distance & slope method
                        dx = 0;
                        dy = (targetY < myY)? -1 : 1;
                    }

                    moveBy(dx, -dy);
                } else {
                    if (fireTimeout[0] == 0) {
                        enemy.fireBullet(angle, target, null);
                        //enemies have unlimited ammo.
                        enemy.ammoAcquired(1);
                        fireTimeout[0] = (int) (Math.random() * fireTimeoutLimit);
                    }
                }

                fireTimeout[0] = (fireTimeout[0] <= 0)? 0 : fireTimeout[0] - 1;
            }
        };
        atMotion.start();
    }

    private Soldier findNewTarget() {
        double shortestDistance = Double.MAX_VALUE;
        int shortestDistanceIndex = -1;

        ImageView mySprite = this.getRawSprite();
        for (int i = 0; i < soldiersCatalog.size(); i++) {
            if (i == selfIndex) continue;
            if (!soldiersCatalog.get(i).alive) continue;

            ImageView targetSprite = soldiersCatalog.get(i).getRawSprite();
            double distance = java.awt.geom.Point2D.distance(
                    mySprite.getX(), mySprite.getY(),
                    targetSprite.getX(), targetSprite.getY()
            );
            if (distance < shortestDistance) {
                shortestDistance = distance;
                shortestDistanceIndex = i;
            }
        }

        if (shortestDistance == NO_TARGETS_REMAINING) {
            throw new ArrayIndexOutOfBoundsException("There are no targets remaining. We have a winner!");
        }

        System.out.println("[LOCK] Soldier " + selfIndex + " is now locked onto target at index " + shortestDistanceIndex);
        return soldiersCatalog.get(shortestDistanceIndex);
    }

    @Override
    public void onEnergyChanged(double energy) {
        /* unimplemented */
    }

    @Override
    public void onAmmoChanged(double ammo) {
        /* unimplemented */
    }

    @Override
    public void onHealthChanged(double health) {
        if (!readHasDiedMessage && health == 0) {
            String killedBy = (hitMeLast.selfIndex == 0)? "Player" : "Bot " + String.valueOf(hitMeLast.selfIndex);
            Main.newLogMessage(killedBy + " has killed " + ((selfIndex == 0)? "Player" : ("Bot " + selfIndex)) + "!");

            Main.NUMBER_OF_BOTS--;
            System.out.println("nob is now " + Main.NUMBER_OF_BOTS);

            this.destroy();
            atMotion.stop();
            readHasDiedMessage = true;
        }
    }
}
