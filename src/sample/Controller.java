package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;

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
               sirs.setCanvas(canvas);
               Thread t = new Thread(() -> {
                   sirs.beginSimulation();
               });
               t.start();
            }
            else if(paused) {
                sirs.continueSimulation();
                paused = false;
            }
            else
                System.out.println("already playing");
        }//TODO: FIX MULTITHREADING ISSUE WITH BUTTON COMBINATIONS
        if(actionEvent.getSource() == reset) {
            sirs.clearSimulation();
            isReset = true;
        }
        if(actionEvent.getSource() == pause) {
            paused = true;
            sirs.pauseSimulation();
        }
    }

    public void init() {
        sirs.setCanvas(canvas);
        width = (int)canvas.getWidth();
        height = (int)canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        sirs.createModel(width, height); //h: 450, w: 600
        setUpSliders();
        setUpTextFields();
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

    public void setUpSliders() {
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

    @FXML
    TextArea textArea_initial;

    //TODO: Figure out why textChange -> reset -> Thread error
    public void setUpTextFields() {
        textArea_initial.textProperty().addListener(e -> {
            try {
                int initial = Integer.parseInt(textArea_initial.getText());
                sirs.model.initial = initial;
            } catch(Exception ex) {
                System.out.println("Invalid initial input");
            }
        });
    }
}
