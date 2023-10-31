import java.util.ArrayList;
import java.io.*;

/**
 * Class for techno-economic analysis.
 *
 * @author Fernando Otero
 * @version 1.0
 */
public class FTTx
{
    // problem parameters

    /**
     * Number of areas.
     */
    private int noOfAreas;

    /**
     * Length of the study period.
     */
    private int period;

    /**
     * Rental charges per household.
     */
    private double rental;

    /**
     * CAPEX charges. They only occur once per area in
     * the lifetime of the investment.
     */
    private double capex;

    /**
     * OPEX charges.
     */
    private double opex;

    /**
     * The interest rate that will be used for the
     * NPV calculations.
     */
    private double interest;

    /**
     * The deployment plan. Each cell in the array
     * represents an area and each value the roll-out
     * year for that area.
     */
    private int[] plan;

    /**
     * Population (number of households) per area.
     */
    private Integer[] households;

    /**
     * Imitator percentages per area.
     */
    private Double[] imitator;

    /**
     * Default constructor.
     */
    public FTTx() {
        noOfAreas = 3;
        period = 10;
        rental = 2;
        capex = 500;
        opex = 200;
        interest = 0.01;

        plan = new int[noOfAreas];
        households = new Integer[noOfAreas];
        imitator = new Double[noOfAreas];

        plan[0] = 0;
        plan[1] = 2;
        plan[2] = 1;

        households[0] = 100;
        households[1] = 100;
        households[2] = 1000;

        imitator[0] = 0.2;
        imitator[1] = 0.5;
        imitator[2] = 0.2;
    }

    /**
     * Returns the NPV value.
     * 
     * @return the NPV value.
     */
    public double npv() {
        return 0;
    }

    /**
     * Loads a csv file in memory, skipping the first line (column names).
     * 
     * @ param filename the file to load.
     */
    public void load(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        ArrayList<Integer> col1 = new ArrayList<>();
        ArrayList<Double> col2 = new ArrayList<>();
        String line = null;
        // skip the first line (column names)
        reader.readLine();
        
        while ((line = reader.readLine()) != null) {
            String[] split = line.split(",");
            
            col1.add(Integer.valueOf(split[0]));
            col2.add(Double.valueOf(split[1]));
        }
        noOfAreas = col1.size();
        households = col1.toArray(new Integer[noOfAreas]);
        imitator = col2.toArray(new Double[noOfAreas]);
    }
}