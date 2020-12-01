package controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    controllers.SIRSController sirs = new controllers.SIRSController();
    @FXML
    Canvas canvas;
    @FXML
    Button play, pause, reset;
    private int width, height;
    private boolean isReset = true, paused = false;
    public void onAction(ActionEvent actionEvent) {
        if(actionEvent.getSource() == play) {
            if(isReset) {
                sirs.model.setInitial(getInitial());
                sirs.clearSimulation();
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
            sirs.seconds = 0;
        }
        if(actionEvent.getSource() == pause) {
            paused = true;
            sirs.pauseSimulation();
        }
    }

    @FXML
    Label label_time_elapsed;
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sirs.setCanvas(canvas);
        sirs.setLabels(label_susceptible, label_infected, label_recovered, label_time_elapsed);
        width = (int)canvas.getWidth();
        height = (int)canvas.getHeight();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        sirs.createModel(width, height); //h: 450, w: 600
        setUpStats();
        setUpTextFields();
        setUpSliders();
    }

    public void stop() {
        sirs.clearSimulation();
        sirs.stop();
    }

    @FXML
    Label label_susceptible,
          label_infected,
          label_recovered;

    public  void setUpStats() {
        label_susceptible.setText(sirs.model.getSusceptible() + "");
        label_infected.setText(sirs.model.getInfected() + "");
        label_recovered.setText(sirs.model.getRecovered() + "");
    }

    @FXML
    TextArea textArea_initial;

    private boolean strippingNewLines = false;
    public void setUpTextFields() {
        textArea_initial.textProperty().addListener(e -> {
            if(strippingNewLines) return;
            try {
                strippingNewLines = true;
                String t = textArea_initial.getText();
                t = t.replaceAll("\\n", "");
                textArea_initial.setText(t);
                int initial = Integer.parseInt(textArea_initial.getText());
                sirs.model.setInitial(initial);
            } catch(Exception ex) {
                System.out.println("Invalid initial input");
            }
            strippingNewLines = false;
        });
    }

    public int getInitial() {
        int initial;
        try {
            strippingNewLines = true;
            String t = textArea_initial.getText();
            t = t.replaceAll("\\n", "");
            textArea_initial.setText(t);
            initial = Integer.parseInt(textArea_initial.getText());
            sirs.model.setInitial(initial);
        } catch(Exception ex) {
            System.out.println("Invalid initial input");
            initial = 0;
        }
        return initial;
    }

    @FXML
    Slider slider_radius,
           slider_infection,
           slider_recovery,
           slider_immunity_loss,
           slider_time;
    @FXML
    Label label_radius,
          label_infection_rate,
          label_recovery_rate,
          label_immunity_loss_rate,
          label_time;

    public void setUpSliders() {
        sirs.model.setTimeNormalizer(.50);
        sirs.model.setRadius(1);
        sirs.model.updateRate("infection", .3);
        sirs.model.updateRate("recovery", .2);
        sirs.model.updateRate("immunityLoss", .3);

        slider_radius.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                sirs.model.setRadius((int)new_val.doubleValue());
                label_radius.setText((int)new_val.doubleValue() + "");
            }
        });
        slider_time.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                sirs.model.setTimeNormalizer(new_val.doubleValue()/100.0);
                label_time.setText((int)new_val.doubleValue() + "%");
            }
        });

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
                label_immunity_loss_rate.setText((int)new_val.doubleValue() + "%");
            }
        });
    }
}
