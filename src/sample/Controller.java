package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

public class Controller {
    SIRSController sirs = new SIRSController();
    @FXML
    Canvas canvas;
    @FXML
    Button play, pause, reset;

    private int width, height;
    private boolean isReset = true, paused = false;
    public void onAction(ActionEvent actionEvent) {
        if(actionEvent.getSource() == play) {
            if(isReset) {
               isReset = false;
               paused = false;
               sirs.createModel(height, width);
               Thread t = new Thread(() -> {
                   sirs.beginSimulation();
               });
               t.start();
            }
            else if(paused) {
                sirs.continueSimulation();
            }
        }
        else if(actionEvent.getSource() == pause) {
            paused = true;
            sirs.pauseSimulation();
        }
        else if(actionEvent.getSource() == reset) {
            sirs.clearSimulation();
            isReset = true;
        }
    }

    public void init() {
        sirs.setCanvas(canvas);
        width = (int)canvas.getWidth();
        height = (int)canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        sirs.createModel(height, width);
    }

    public void stop() {
        sirs.clearSimulation();
        sirs.stop();
    }
}
