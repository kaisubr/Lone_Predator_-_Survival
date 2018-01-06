package sample;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileNotFoundException;

/**
 * Lone Predator - Survival, file created in sample by Kailash Sub.
 */
public class Player extends Soldier {

    private Label energyLabel, healthLabel, ammoLabel, itemsLabel;
    private boolean youDiedFlag = false;

    public Player(Group root, Stage primaryStage, Scene mScene, double startX, double startY) throws FileNotFoundException {
        super(root, primaryStage, mScene, startX, startY, "sprites/player2.png");
        isEnemy = false;
    }

    @Override
    public void onEnergyChanged(double energy) {
        if ((int) energy < 0 && !youDiedFlag) {
            //you died.
            Main.messenger.sendMessage("You died since you ran out of energy.");
            this.alive = false;
            youDiedFlag = true;
            FadeTransition ft = new FadeTransition(Duration.millis(5000), this.getRawSprite());
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.play();
            System.out.println("fade trans");
            ft.setOnFinished((e) -> {
                this.destroy();
            });
        }

        switch ((int) energy) {
            case 50:
                Main.messenger.sendMessage("You have 50% energy left. Fuel is labeled with an \"F\".");
                break;
            case 25:
                Main.messenger.sendMessage("You are very low on energy. Fuel is labeled with an \"F\".");
                break;
            case 10:
                Main.messenger.sendMessage("Your energy is tremendously low.");
                break;
        }

        if (energyLabel == null) {
            throw new NullPointerException("setEnergyLabel(...) was not called.");
        } else {
            Platform.runLater(() -> {
                energyLabel.setText(Soldier.ENERGY_FILLER_VALUE + Math.floor(energy) + "%");
            });
        }
    }

    @Override
    public void onAmmoChanged(double ammo) {
        if (ammo == 0) {
            Main.messenger.sendMessage("You're out of ammo! Ammo boxes are labeled with an \"A\".");
        }
        if (ammoLabel == null) {
            throw new NullPointerException("setAmmoLabel(...) was not called.");
        } else {
            Platform.runLater(() -> {
                ammoLabel.setText(Soldier.AMMO_FILLER_VALUE + Math.floor(ammo));
            });
        }
    }

    @Override
    public void onHealthChanged(double health) {
        if (health <= 25 && health > 0) {
            Main.messenger.sendMessage("You're low on health! Escape and wait for your health to regenerate.");
        }

        if (health <= 0 && !youDiedFlag) {
            //you died.
            Main.messenger.sendMessage("You died.");
            this.alive = false;
            youDiedFlag = true;
            FadeTransition ft = new FadeTransition(Duration.millis(5000), this.getRawSprite());
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.play();
            System.out.println("fade trans");
            ft.setOnFinished((e) -> {
                this.destroy();
            });
        }

        if (healthLabel == null) {
            throw new NullPointerException("setAmmoLabel(...) was not called.");
        } else {
            Platform.runLater(() -> {
                healthLabel.setText(Soldier.HEALTH_FILLER_VALUE + Math.floor(health));
            });
        }
    }

    public void setEnergyLabel(Label energyLabel) {
        this.energyLabel = energyLabel;
    }

    public void setHealthLabel(Label healthLabel) {
        this.healthLabel = healthLabel;
    }

    public void setAmmoLabel(Label ammoLabel) {
        this.ammoLabel = ammoLabel;
    }

    public void setItemsLabel(Label itemsLabel) {
        this.itemsLabel = itemsLabel;
    }

}
