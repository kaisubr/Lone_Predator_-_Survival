package sample;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

/**
 * Lone Predator - Survival, file created in sample by Kailash Sub.
 */
public abstract class SupplyItem {
    public static List<Object[]> items = new ArrayList<>();
    public final String tag;
    private double x, y;
    private Group root;
    public Label rawLabel;
    public String type;

    static final private double padding = 10;

    public SupplyItem(double x, double y, Group root, String tag, String type) {
        this.x = x;
        this.y = y;
        this.root = root;
        this.type = type;
        this.tag = tag;

        rawLabel = new Label();
        rawLabel.setLayoutX(x);
        rawLabel.setLayoutY(y);
        rawLabel.setText(type);
        rawLabel.setFont(new Font("Fira Sans Bold", 16));
        rawLabel.setStyle("-fx-border-color: black; -fx-padding: " + padding + "px");

        root.getChildren().add(rawLabel);

        items.add(new Object[]{tag, this});
    }

    /**
     * @deprecated Please use {@link #getBoxInfoByTag(String)} ()} since it is much faster and more reliable.
     * @param type
     * @return
     */
    @Deprecated
    public static Object[] getBoxInfoByType(String type) {
        for (int i = 0; i < items.size(); i++) {
            if (((SupplyItem) items.get(i)[1]).type.equals(type)) {
                return new Object[]{true, items.get(i)};
            }
        }

        return new Object[]{false, null};
    }

    public static boolean isCollision(Player player, SupplyItem item) {
        if (java.awt.geom.Point2D.distance(player.getRawSprite().getX(), player.getRawSprite().getY(), item.rawLabel.getLayoutX(), item.rawLabel.getLayoutY()) <= 50) {
            return true;
        } else {
            return false;
        }
//        Bounds itemBounds = item.rawLabel.getBoundsInParent();
//
//        if (player.getRawSprite().getBoundsInParent().contains(itemBounds)) {
//            return true;
//        }
//        return false;
    }

    public void destroy(String tag) {
        root.getChildren().remove(rawLabel);
        for (int i = 0; i < items.size(); i++) {
            if (((SupplyItem) items.get(i)[1]).tag.equals(tag)) {
                items.remove(i);
            }
        }
    }

    public static SupplyItem getBoxInfoByTag(String currentAmmoBoxTag) {
        for (int i = 0; i < items.size(); i++) {
            if (((SupplyItem) items.get(i)[1]).tag.equals(currentAmmoBoxTag)) {
                return ((SupplyItem) items.get(i)[1]);
            }
        }

        return null;
    }
}
