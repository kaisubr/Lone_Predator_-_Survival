package sample;

import javafx.scene.Group;

/**
 * Lone Predator - Survival, file created in sample.sprites by Kailash Sub.
 */
public class EnergyFuel extends SupplyItem{
    public EnergyFuel(double x, double y, Group root, String tag) {
        super(x, y, root, tag, "F");
    }

    public void updatePlayerInformation(Player player) {
        if (SupplyItem.isCollision(player, this)) {
            System.out.println("Collision with energy " + true);
            //increase energy
            player.energyAcquired((int) Math.ceil(Math.random() * 25));
            System.out.println("[EnergyFuel] Item is being destroyed, tag will update to "+ (Main.currentEnergyItemTag + 1));
            destroy(tag);
            Main.currentEnergyItemTag++;
        }
    }
}
