package sample;

import java.util.Random;

public class SIRSModel {
    public void updateRate(String type, double value) {
        switch(type) {
            case "infection":
                infectionRate = value; infectionRate /= iterations;
                break;
            case "recovery":
                recoveryRate = value; recoveryRate /= iterations;
                break;
            case "immunityLoss":
                immunityLossRate = value; immunityLossRate /= iterations;
                break;
        }
        System.out.println("infection: " + infectionRate);
        System.out.println("recovery: " + recoveryRate);
        System.out.println("immunity loss: " + immunityLossRate);
    }

    public enum State { NON_INFECTED, INFECTED, RECOVERED, INFECTED_DEAD, NON_INFECTED_DEAD };
    private Random rand = new Random();

    public int radius = 10; //manhattan distance or layers?
    public State[][] grid;
    public State[][] buffer;
    public double iterations = 60; //40 <= iterations <= 100
    public double infectionRate;// = .8 / iterations;
    public double recoveryRate;// = .5 / iterations;
    public double immunityLossRate;// = .3 / iterations;
    public int initial = 1;
    private int durationStart = 7;
    private int durationLen;
    private int immunityStart = 14;
    private int immunityLen;

    private int population;
    private int infected;
    private int nonInfected;
    private int recovered;

    private int infectedDeaths;
    private int nonInfectedDeaths;
    private int recoveredDeaths;

    private int births; // TODO: simulate life span and eras?

    private int rows = 50, cols = 50;
    public void setUpGrid() {
        population = rows*cols;
        nonInfected = population;
        reset();

        grid = new State[rows][cols];
        buffer = new State[rows][cols];
        for(int i = 0; i < rows; i++)
            for(int j = 0; j < cols; j++) {
                grid[i][j] = State.NON_INFECTED;
                buffer[i][j] = State.NON_INFECTED;
            }
    }

    public void reset() {
        infected = 0;
        recovered = 0;

        nonInfectedDeaths = 0;
        infectedDeaths = 0;
        recoveredDeaths = 0;

        births = 0;
    }

    public void setUpGrid(int r, int c) {
        rows = r; cols = c;
        setUpGrid();
    }

    public void swap() {
        State[][] temp = grid;
        grid = buffer;
        buffer = temp;
    }

    public void init() {
        int radius = 3;
        //selected random point
        int i = rand.nextInt(grid.length);
        int j = rand.nextInt(grid[0].length);
        grid[i][j] = State.INFECTED;
        updateInfected();
    }

    public void update(int i, int j) {
        State person = grid[i][j];
        if(person == null) return;
        switch(person) {
            //if non-infected, calculate chance of infection (or die)?
            case NON_INFECTED:
                //search neighboring cells and calculate chance repeatedly (with weights based on distance)
                if(search(i, j, "layer")) {
                    buffer[i][j] = State.INFECTED;
                    updateInfected();
                }
                else
                    buffer[i][j] = State.NON_INFECTED;
                break;
            //if infected, calculate chance to recover //or die?
            case INFECTED:
                //inverse relation to (iterations or duration)
                double recoveryChance = rand.nextDouble();
                if(recoveryChance < recoveryRate) { //recovered
                    buffer[i][j] = State.RECOVERED;
                    updateRecovered();
                }
                else
                    buffer[i][j] = State.INFECTED;
                break;

            //if recovered, calculate chance of losing immunity
            case RECOVERED:
                //reduces immunity after a certain number of iterations
                double immunityLossChance = rand.nextDouble();
                if(immunityLossChance < immunityLossRate) {//immunityDecay) {
                    buffer[i][j] = State.NON_INFECTED;
                    updateImmunityLoss();
                }
                else
                    buffer[i][j] = State.RECOVERED;
                break;
            default:
                buffer[i][j] = State.NON_INFECTED;
        }
    }

    private boolean search(int i, int j, String type) {
        boolean infected = false;
        type = type.toLowerCase();
        //these methods will break if person is infected or exhausted search
        //potentially updates infected
        if(type.equals("manhattan")) {
            infected = manhattanDistance(i, j);
        }
        else if(type.equals("layer"))
            infected = layerDistance(i, j);
        return infected;
    }

    private boolean manhattanDistance(int r, int c) {
        //parse layered neighbors
        for(int i = -radius; i < radius; i++) {
            for(int j = -radius; j < radius; j++) {
                if(i == 0 && j == 0) continue; //skip self
                int row = i+r; int col = c+j;
                //check if neighbor is inbounds
                if(inBounds(row, col) && Math.abs(r+c) <= radius) {
                    //check if neighbor is sick and calculate infection chance
                    if(grid[row][col] == State.INFECTED) {
                        //TODO: Think of diminishing weights as layers increase
                        double distance;
                        //distance = 1.0/(Math.abs(i) + Math.abs(j));
                        distance = 1.0;
                        double infectionChance = rand.nextDouble() * distance;
                        if(infectionChance < infectionRate) {
                            return true; //break out of loop, reduce computation
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean layerDistance(int r, int c) {
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
                        double distance;
                        //distance = 1.0/(Math.abs(i) + Math.abs(j));
                        distance = 1.0;
                        double infectionChance = rand.nextDouble() * distance;
                        if(infectionChance < infectionRate) {
                            return true; //break out of loop, reduce computation
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean inBounds(int i, int j) {
        if(i < 0 || i >= grid.length)
            return false;
        if(j < 0 || j >= grid[0].length)
            return false;
        return true;
    }

    public int getPopulation() {
        return population;
    }
    public int getInfected() { return infected; }
    public int getNonInfected() { return nonInfected; }
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
        infected--;
        recovered++;
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
