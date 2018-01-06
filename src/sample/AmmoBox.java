package sample;

import javafx.scene.Group;

/**
 * Lone Predator - Survival, file created in sample by Kailash Sub.
 */
public class AmmoBox extends SupplyItem {
    public AmmoBox(double x, double y, Group root, String tag) {
        super(x, y, root, tag, "A");
    }

    public void updatePlayerInformation(Player player) {
        if (SupplyItem.isCollision(player, this)) {
            System.out.println("Collision " + true);
            //increase ammo
            player.ammoAcquired((int) Math.ceil(Math.random() * 25));
            System.out.println("[AmmoBox] Item is being destroyed, tag will update to "+ (Main.currentAmmoBoxTag + 1));
            destroy(tag);
            Main.currentAmmoBoxTag++;
        }
    }
}
