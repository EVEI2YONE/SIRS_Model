package sample;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SIRSDisplay {
    SIRSModel model;
    GraphicsContext g;
    private Color
        nonInfected = Color.LIGHTGREEN,
        infected = Color.RED,
        recovered = Color.BLUE,
        infectedDeath = Color.BLACK,
        nonInfectedDeath = Color.DARKGREEN,
        recoveredDeath = Color.CYAN;

    public void setModel(SIRSModel model) {
        this.model = model;
    }

    private synchronized void draw(int i, int j, Color col) {
        g.setFill(col);
        g.fillRect(i, j, 1, 1);
    }
    private void display(int index) {
        if(model == null) return;
        for(int i = istart[index]; i < iend[index]; i++) {
            for(int j = jstart[index]; j < jend[index]; j++) {
                SIRSModel.State person = model.grid[i][j];
                if(person == null) continue;
                switch(person) {
                    case INFECTED:
                        draw(i, j, infected);
                        break;
                    case NON_INFECTED:
                        draw(i, j, nonInfected);
                        break;
                    case RECOVERED:
                        draw(i, j, recovered);
                        break;
                }
            }
        }
        update();
    }

    int[] istart = new int[9],
            iend = new int[9],
            jstart = new int[9],
            jend = new int[9];
    int id = 0;
    int updated = 0;
    //multi-thread changes
    public void show() {
        displaying = true;
        updated = 0;
        id = 0;
        int index = 0;
        int rows = model.grid.length;
        int rowLen = rows/3;
        int cols = model.grid[0].length;
        int colLen = cols/3;
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                istart[index] = i * (rows/3);
                iend[index] = istart[index] + rowLen;
                if(i == 2) {
                    iend[index] = rows;
                }

                jstart[index] = j*(cols/3);
                jend[index] = jstart[index] + colLen;
                if(j == 2) {
                    jend[index] = cols;
                }
                index++;
                Thread t = new Thread(() -> {
                    display(updateID());
                });
                t.start();
            }
        }
    }

    public synchronized int updateID() {
        id++;
        return id-1;
    }

    private boolean displaying;
    public boolean isDisplaying() { return displaying; }
    public synchronized void update() {
        updated++;
        if(updated == 9)
            displaying = false;
    }

    public void setCanvas(GraphicsContext gr) {
        g = gr;
    }

    private boolean running = true;
    public void stop() {
        running = false;
    }
}
