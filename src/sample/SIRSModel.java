package sample;

import java.util.Random;

public class SIRSModel {

    public enum State { NON_INFECTED, INFECTED, RECOVERED, INFECTED_DEAD, NON_INFECTED_DEAD };
    private Random rand = new Random();

    public int radius = 2; //manhattan distance or layers?
    public State[][] grid;
    public State[][] buffer;
    public double infectionRate = .05;
    private int durationStart = 7;
    private int durationLen;
    private int immunityStart = 14;
    private int immunityLen;

    private int infected;
    private int nonInfected;
    private int recovered;

    private int infectedDeaths;
    private int nonInfectedDeaths;
    private int recoveredDeaths;

    private int births; // TODO: simulate life span and eras?

    public void init() {
        //selected random point
        int i = rand.nextInt(grid.length);
        int j = rand.nextInt(grid[0].length);
        buffer[i][j] = State.INFECTED;
        updateInfected();
    }

    public void update(int i, int j) {
        State person = grid[i][j];
        switch(person) {
            //if infected, calculate chance to recover //or die?
            case INFECTED:
                durationLen = rand.nextInt(7);
                double recoveryRate = 1/(durationStart + durationLen);
                //inverse relation to (iterations or duration)
                if(rand.nextDouble() < recoveryRate) { //recovered
                    buffer[i][j] = State.RECOVERED;
                    updateRecovered();
                }
                break;
            //if non-infected, calculate chance of infection (or die)?
            case NON_INFECTED:
                //search neighboring cells and calculate chance repeatedly (with weights based on distance)
                search(i, j, "layer");
                break;
            //if recovered, calculate chance of losing immunity
            case RECOVERED:
                //reduces immunity after a certain number of iterations
                //immunity decay
                immunityLen = rand.nextInt(7);
                double immunityDecay = 1 / (immunityStart + immunityLen);
                if(rand.nextDouble() < immunityDecay) {
                    buffer[i][j] = State.NON_INFECTED;
                    updateImmunityLoss();
                }
                break;
        }
    }

    private void search(int i, int j, String type) {
        type = type.toLowerCase();
        //these methods will break if person is infected or exhausted search
        //potentially updates infected
        if(type.equals("manhattan")) {
            manhattanDistance(i, j);
        }
        else if(type.equals("layer"))
            layerDistance(i, j);
    }

    private void manhattanDistance(int i, int j) {

    }

    private void layerDistance(int r, int c) {
        //parse layered neighbors
        for(int i = -radius; i < radius; i++) {
            for(int j = -radius; j < radius; j++) {
                if(i == 0 && j == 0) continue; //skip self
                int row = i+r; int col = c+j;
                //check if neighbor is inbounds
                if(inBounds(row, col)) {
                    //check if neighbor is sick and calculate infection chance
                    if(grid[row][col] == State.INFECTED) {
                        //TODO: Think of diminishing weights as layers increase
                        double distance = 1.0/(Math.abs(i) + Math.abs(j));
                        if(rand.nextDouble() * distance < infectionRate) {
                            buffer[r][c] = State.INFECTED;
                            return; //break out of loop, reduce computation
                        }
                    }
                    else //be sure to update, just in case of residual logic
                        buffer[r][c] = State.NON_INFECTED;
                }
            }
        }
    }

    private boolean inBounds(int i, int j) {
        if(i < 0 || i >= grid.length)
            return false;
        if(j < 0 || j >= grid[0].length)
            return false;
        return true;
    }

    private int rows = 50, cols = 50;
    public void setUpGrid() {
        grid = new State[rows][cols];
        buffer = new State[rows][cols];
    }

    public void setUpGrid(int r, int c) {
        rows = r; cols = c;
        setUpGrid();
    }

    public int getInfected() { return infected; }
    public int getRecovered() { return recovered; }
    public int getDeaths() {
        return nonInfectedDeaths+infectedDeaths+recoveredDeaths;
    }

    //non-infected -> infected
    private synchronized void updateInfected() {
        nonInfected--;
        infected++;
    }
    //infected -> recovered
    private synchronized void updateRecovered() {
        recovered++;
        infected--;
    }
    //recovered -> non-infected
    private synchronized void updateImmunityLoss() {
        recovered--;
        nonInfected++;
    }
    private void nonInfectedDeath(int deaths) {
        nonInfectedDeaths += deaths;
    }
    private void infectedDeath(int deaths) {
        infectedDeaths += deaths;
    }
    private void recoveredDeath(int deaths) {
        recoveredDeaths += deaths;
    }
}
