package sample;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SIRSController {
    SIRSModel model = new SIRSModel();
    SIRSDisplay display = new SIRSDisplay();

    public SIRSController() {
        display.setModel(model);
    }
    private int rows = 50, cols = 50;
    public void createModel(int rows, int cols) {
        this.rows = rows; this.cols = cols;
        model.setUpGrid(rows, cols);
    }

    public void updateChanges(int id) {
        try {
            for (int i = istart[id]; i < iend[id]; i++) {
                for (int j = jstart[id]; j < jend[id]; j++) {
                    model.update(i, j);
                }
            }
        }catch(Exception e) {

        }
        setUpdate(1);
    }

    int[] istart = new int[9],
            iend = new int[9],
            jstart = new int[9],
            jend = new int[9];
    int id = 0;
    Lock lock = new ReentrantLock();
    int updated = 0;
    //multi-thread changes
    public void run() {
        update(true);
        setUpdate(-updated);
        int index = 0;
        id = 0;
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
                    updateChanges(id++);
                });
                t.start();
            }
        }
        SIRSModel.State[][] temp = model.grid;
        model.grid = model.buffer;
        model.buffer = temp;
    }

    public synchronized void setUpdate(int inc) {
        updated += inc;
    }

    public synchronized void update(boolean bool) {
        updating = bool;
    }

    boolean running, paused, reset;
    boolean updating;
    public void beginSimulation() {
        running = true;
        paused = false;
        model.init();
        while (running) {
            if(updated == 9) {
                update(false);
            }
            if(!paused && !updating) {
                run();
            }
        }
    }

    public void pauseSimulation() {
        paused = true;
    }
    public void continueSimulation() {
        paused = false;
    }

    public void clearSimulation() {
        paused = true;
        running = false;
        model.setUpGrid(rows, cols);
    }
}
