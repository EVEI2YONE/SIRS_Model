package models;

import java.util.Random;

public class SIRSModel {
    public void updateRate(String type, double value) {
        switch(type) {
            case "infection":
                infectionRate = value; normInfection = infectionRate/timeNormalizer;
                break;
            case "recovery":
                recoveryRate = value; normRecovery = recoveryRate/timeNormalizer;
                break;
            case "immunityLoss":
                immunityLossRate = value; normImmunityLoss = immunityLossRate/timeNormalizer;
                break;
        }
    }

    public enum State { NON_INFECTED, INFECTED, RECOVERED, INFECTED_DEAD, NON_INFECTED_DEAD, EMPTY, BORDER, TRANSPORTATION };
    private Random rand = new Random();

    public int radius = 1; //manhattan distance or layers?
    public State[][] grid, buffer;
    public String searchType = "layer"; //"manhattan" or "layer"
    private double
        timeNormalizer,// = 40, //40 <= timeNormalizer <= 1000
        infectionRate,
        recoveryRate,
        immunityLossRate;
    private double
        normInfection,
        normRecovery,
        normImmunityLoss;

    private int
        initial = 1,
        durationStart = 7,
        durationLen,
        immunityStart = 14,
        immunityLen;

    private int
        population,
        infected,
        susceptible,
        recovered;

    private int
        infectedDeaths,
        nonInfectedDeaths,
        recoveredDeaths;

    private int births; // TODO: simulate life span and eras?

    private int rows = 50, cols = 50;
    public void setUpGrid() {
        population = rows*cols;
        susceptible = population;
        reset();

        grid = new State[rows][cols];
        buffer = new State[rows][cols];
        for(int i = 0; i < rows; i++)
            for(int j = 0; j < cols; j++) {
                grid[i][j] = State.NON_INFECTED;
                buffer[i][j] = State.INFECTED;
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
        //selected random point
        for(int k = 0; k < initial; k++) {
            int i = rand.nextInt(grid.length);
            int j = rand.nextInt(grid[0].length);
            grid[i][j] = State.INFECTED;
            updateInfected();
        }
    }

    public void update(int i, int j) {
        State person = grid[i][j];
        if(person == null) return;
        switch(person) {
            //if non-infected, calculate chance of infection (or die)?
            case NON_INFECTED:
                //search neighboring cells and calculate chance repeatedly (with weights based on distance)
                if(search(i, j, searchType)) {
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
                if(recoveryChance < normRecovery) { //recovered
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
                if(immunityLossChance < normImmunityLoss) {//immunityDecay) {
                    buffer[i][j] = State.NON_INFECTED;
                    updateImmunityLoss();
                }
                else
                    buffer[i][j] = State.RECOVERED;
                break;
            default:
                buffer[i][j] = grid[i][j];
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
                if(inBounds(row, col) && Math.abs(i)+Math.abs(j) <= radius) {
                    //check if neighbor is sick and calculate infection chance
                    if(grid[row][col] == State.INFECTED) {
                        //TODO: Think of diminishing weights as layers increase
                        double distance;
                        //distance = 1.0/(Math.abs(i) + Math.abs(j));
                        distance = 1.0;
                        double infectionChance = rand.nextDouble() * distance;
                        if(infectionChance < normInfection) {
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
                        if(infectionChance < normInfection) {
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

    public void setInitial(int initial) {
        this.initial = initial;
    }
    public void setRadius(int rad) {
        radius = rad;
    }

    public void setTimeNormalizer(double timeRatio) { //100% = 40, 0% = 1000);
        timeNormalizer = 20 + fade(1-timeRatio)*(1000-20);
        normInfection = infectionRate/timeNormalizer;
        normRecovery = recoveryRate/timeNormalizer;
        normImmunityLoss = immunityLossRate/timeNormalizer;
    }
    private double fade(double ratio) { return 6*Math.pow(ratio, 5.0)-15*Math.pow(ratio,4.0)+10*Math.pow(ratio,3.0); };
    private double normalizeRate(double val) {
        return val / timeNormalizer;
    }

    public int getPopulation() {
        return population;
    }
    public int getInfected() { return infected; }
    public int getSusceptible() { return susceptible; }
    public int getRecovered() { return recovered; }
    public int getDeaths() {
        return nonInfectedDeaths+infectedDeaths+recoveredDeaths;
    }

    //non-infected -> infected
    private synchronized void updateInfected() {
        susceptible--;
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
        susceptible++;
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
