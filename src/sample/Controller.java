package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.ScrollEvent;
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

               Thread t = new Thread(() -> {
                   //sirs.beginSimulation();
                   //sirs.testStart();
                   sirs.sync();
               });
               t.start();
            }
            else if(paused) {
                sirs.continueSimulation();
                paused = false;
            }
            else
                System.out.println("already playing");
        }
        else if(actionEvent.getSource() == pause) {
            paused = true;
            sirs.pauseSimulation();
        }
        else if(actionEvent.getSource() == reset) {
            sirs.clearSimulation();
            isReset = true;
            sirs.createModel(height, width);
        }
    }

    public void init() {
        sirs.setCanvas(canvas);
        width = (int)canvas.getWidth();
        height = (int)canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        sirs.createModel(width, height); //h: 450, w: 600
        updateSlider();
    }

    public void stop() {
        sirs.clearSimulation();
        sirs.stop();
    }

    @FXML
    Slider slider_infection;
    @FXML
    Slider slider_recovery;
    @FXML
    Slider slider_immunity_loss;
    @FXML
    Label label_infection_rate;
    @FXML
    Label label_recovery_rate;
    @FXML
    Label label_immunity_loss_rate;

    public void updateSlider() {
        sirs.model.updateRate("infection", .3);
        sirs.model.updateRate("recovery", .2);
        sirs.model.updateRate("immunityLoss", .3);

        slider_infection.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                sirs.model.updateRate("infection", new_val.doubleValue()/100);
                label_infection_rate.setText((int)new_val.doubleValue() + "%");
            }
        });
        slider_recovery.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                sirs.model.updateRate("recovery", new_val.doubleValue()/100);
                label_recovery_rate.setText((int)new_val.doubleValue() + "%");
            }
        });
        slider_immunity_loss.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                sirs.model.updateRate("immunityLoss", new_val.doubleValue()/100);
                label_immunity_loss_rate.setText(String.format("%.2f", new_val.doubleValue()));
            }
        });
    }
}
