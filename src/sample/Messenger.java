package sample;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Lone Predator - Survival, file created in sample by Kailash Sub.
 */
public class Messenger {
    Label messageLabel;

    public Messenger(Label messageLabel) {
        this.messageLabel = messageLabel;
        resetLabel();
    }

    /**
     * Resets label to the default position. Set the message before calling resetLabel so that the text is centered.
     */
    private void resetLabel() {
        messageLabel.setPrefWidth(Main.WIDTH);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setTranslateX(0);
        messageLabel.setTranslateY(150);
        messageLabel.setOpacity(0);
    }

    boolean messengerIsBusy = false;

    public void sendMessage(String message) {
        if (messengerIsBusy) return;

        messengerIsBusy = true;
        Thread t = new Thread(() -> {
            Platform.runLater(() -> {
                messageLabel.setText(message);
                System.out.println(messageLabel.getText());
                messageLabel.setFont(new Font("Fira Sans Bold", 20));
                resetLabel();
            });
        });

        t.start();

        //animate label
        FadeTransition ft0 = new FadeTransition(Duration.millis(500), messageLabel);
        ft0.setToValue(1.0);

        TranslateTransition tt = new TranslateTransition(Duration.millis(2000),  messageLabel);
        tt.setToY(100);
        tt.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition ft1 = new FadeTransition(Duration.millis(500), messageLabel);
        ft1.setDelay(Duration.millis(200));
        ft1.setToValue(0.0);

        ParallelTransition pt = new ParallelTransition();
        pt.getChildren().addAll(ft0, tt);

        SequentialTransition st = new SequentialTransition();
        st.getChildren().addAll(pt, ft1);
        st.play();

        st.setOnFinished(e -> {
            try {
                t.join();
                messengerIsBusy = false;
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });

    }

    public Label getMessageLabel() {
        return messageLabel;
    }
}
