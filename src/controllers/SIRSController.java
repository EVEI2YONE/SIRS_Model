package controllers;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import models.SIRSDisplay;
import models.SIRSModel;

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
        model.init();
        prepRegions();
        prepTestRegion();
        show();
    }

    int rowsections = 1;
    int colsections = 2;
    int size = (rowsections + colsections)+1;
    int[] istart = new int[size],
            iend = new int[size],
            jstart = new int[size],
            jend = new int[size];
    public void prepRegions() {
        int index = 0;
        int rows = model.grid.length;
        int rowLen = rows/rowsections;
        int cols = model.grid[0].length;
        int colLen = cols/colsections;
        for(int i = 0; i < rowsections; i++) {
            for(int j = 0; j < colsections; j++) {
                istart[index] = i * (rows/rowsections);
                iend[index] = istart[index] + rowLen;
                if(i == rowsections-1) {
                    iend[index] = rows;
                }
                jstart[index] = j*(cols/colsections);
                jend[index] = jstart[index] + colLen;
                if(j == colsections-1) {
                    jend[index] = cols;
                }
                index++;
            }
        }
    }
    public void prepTestRegion() {
        int s = size-1;
        istart[s] = 0;
        iend[s] = rows;
        jstart[s] = 0;
        jend[s] = cols;
    }

    public void parseGrid(int index, String type) {
        type = type.toLowerCase();
        try {
            //parse subgrid
            for (int i = istart[index]; i < iend[index]; i++) {
                for (int j = jstart[index]; j < jend[index]; j++) {
                    if(display.g == null) return;
                    if(type.equals("update"))
                        model.update(i, j);
                    else if(type.equals("display"))
                        display.show(i, j);
                        //display.draw(i, j);
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
            System.out.println("Error updating changes");
        }
        if(type.equals("update"))
            setUpdate(1);
        else if(type.equals("display"))
            setDisplay(1);
    }

    int id = 0;
    public void show() {
        id = 0;
        displaying = true;
        displayed = 0;
        parseGrid(size-1, "display");
    }
    public void run() { //h: 450, w: 600 -> 150 x 200
        id = 0;
        updating = true;
        updated = 0;
        for(int i = 0; i < size-1; i++) {
            Thread t = new Thread(() -> {
                parseGrid(updateID(), "update");
            });
            t.start();
        }
    }

    public synchronized int updateID() {
        id++;
        return id-1;
    }
    public synchronized void setUpdate(int inc) {
        updated += inc;
    }
    public synchronized void setDisplay(int dis) {
        displayed += dis;
    }

    public void pauseSimulation() {
        paused = true;
    }
    public void continueSimulation() {
        if(!paused) return; //don't launch another thread while it's running
        paused = false;
        Thread t = new Thread(() -> {
            beginSimulation();
        });
        t.start();
    }

    public void clearSimulation() {
        paused = false;
        display.setCanvas(null);
        //let updating thread force into displaying state
        while(running && updating) {}
        //let 2nd main thread exit
        running = false;
        //wait for displaying state to end thread
        try { Thread.sleep(5); } catch(Exception ex){}
        display.setCanvas(g);
        createModel(rows, cols);
        resetValues();
    }
    private void resetValues() {
        thread_updating  = false; thread_displaying  = false;
        running = false; paused = false; reset = false;
        updated = 0; displayed = 0;
        updating  = false; displaying  = false;
    }

    public void stop() {
        display.stop();
    }

    Canvas c;
    GraphicsContext g;
    public void setCanvas(Canvas canvas) {
        c = canvas;
        display.setCanvas(canvas.getGraphicsContext2D());
        g = canvas.getGraphicsContext2D();
    }

    public void normalizeTime() {
        //every second is a day

    }

    private long timeElapsed = 0;
    long frameLimit = 30, start, end, time;
    long nanoSeconds = 1000000000;
    long frameCap = nanoSeconds/frameLimit/2;

    boolean thread_updating, thread_displaying;

    boolean running, paused, reset;
    int updated = 0, displayed = 0;
    boolean updating, displaying;

    int frames = 0;
    int seconds = 0;
    public void beginSimulation() {
        normalizeTime();
        running = true;
        int population = model.getPopulation();
        while(running && !paused) {
            if(model.getInfected() == population) {
                System.out.println("EVERYONE IS INFECTED");
                running = false;
            }
            else if(model.getInfected() == 0) {
                System.out.println("EVERYONE HAS RECOVERED");
                running = false;
            }
            start = System.nanoTime();
            //60 frames per second, i think...
            if (timeElapsed >= frameCap) {
                Platform.runLater(() -> {
                    updateStats();
                });
                if(frames >= frameLimit) {
                    frames -= frameLimit;
                    seconds++;
                }
                timeElapsed -= frameCap;
                if(timeElapsed <= frameCap) {
                    if (!thread_updating && !thread_displaying) {
                        Platform.runLater(() -> {
                            thread_updating = true;
                            parseGrid(size - 1, "update");
                            model.swap();
                            thread_updating = false;
                        });
                    }
                    if (!thread_displaying) {
                        Platform.runLater(() -> {
                            thread_displaying = true;
                            parseGrid(size - 1, "display");
                            thread_displaying = false;
                        });
                    }
                }
                frames++;
            }
            end = System.nanoTime();
            time = end-start;
            timeElapsed += time;
        }
    }

    Label susceptible, infected, recovered, time_elapsed;
    public void setLabels(Label label_susceptible, Label label_infected, Label label_recovered, Label label_time_elapsed) {
        susceptible = label_susceptible;
        infected = label_infected;
        recovered = label_recovered;
        time_elapsed = label_time_elapsed;
    }
    public void updateStats(){
        susceptible.setText(String.format("%d", model.getSusceptible()));
        infected.setText(String.format("%d", model.getInfected()));
        recovered.setText(String.format("%d", model.getRecovered()));
        time_elapsed.setText(String.format("%d s", (int) (seconds)));
    }
}
