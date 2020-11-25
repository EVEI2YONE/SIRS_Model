package sample;

import javafx.scene.paint.Color;

public class SIRSDisplay {
    SIRSModel model;
    private Color
        nonInfected,
        infected,
        recovered,
        infectedDeath,
        nonInfectedDeath,
        recoveredDeath;

    public void setModel(SIRSModel model) {
        this.model = model;
    }
}
