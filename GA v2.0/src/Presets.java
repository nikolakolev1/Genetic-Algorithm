public class Presets {
    /**
     * Preset naming scheme:
     * preset +
     * Individual type (Bol, Int, Tsp) +              | Binary, Integer, TSP int[]
     * Selection type (Rol, Tor) +                    | Roulette, Tournament
     * Fitness func (Mbo, Lbo, Qde, Tsp) +            | MostBitsOn, LeastBitsOn, QuadraticEquation, TSP
     * Crossover (Sin, Npo, Unf, Pmx) +               | Single point, n point, Uniform, PMX
     * Mutation (Unf, Sin, Exc, Inv) +                | Uniform, Single point, Exchange, Inversion
     * Elitism (Elt, Noe) +                           | Elitism, No elitism
     * Minimization/Maximization (Min, Max) +         | Minimization, Maximization
     * Population size (Sml, Med, Lrg, Gen, Qde, Tsp) | Small, Medium, Large, Many generations, Quadratic eq., TSP
     */
    public static void preset(String letterCode) throws Exception {
        INDIVIDUAL_TYPE ind;
        SELECTION sel;
        FITNESS_FUNC fit;
        CROSSOVER cross;
        MUTATION mutate;
        boolean elite;
        MIN_MAX minMax;

        switch (letterCode.substring(0, 3)) {
            case ("Bol") -> ind = INDIVIDUAL_TYPE.boolArray;
            case ("Int") -> ind = INDIVIDUAL_TYPE.intArray;
            case ("Tsp") -> ind = INDIVIDUAL_TYPE.tspIntArray;
            default -> throw new Exception("Error with the preset");
        }
        switch (letterCode.substring(3, 6)) {
            case ("Rol") -> sel = SELECTION.Roulette;
            case ("Tor") -> sel = SELECTION.Tournament;
            default -> throw new Exception("Error with the preset");
        }
        switch (letterCode.substring(6, 9)) {
            case ("Mbo") -> fit = FITNESS_FUNC.MostBitsOn;
            case ("Lbo") -> fit = FITNESS_FUNC.LeastBitsOn;
            case ("Qde") -> fit = FITNESS_FUNC.QuadEquationBoolArray;
            case ("Tsp") -> fit = FITNESS_FUNC.Tsp;
            default -> throw new Exception("Error with the preset");
        }
        switch (letterCode.substring(9, 12)) {
            case ("Sin") -> cross = CROSSOVER.SinglePoint_Simple;
            case ("Npo") -> cross = CROSSOVER.nPoint_Simple;
            case ("Unf") -> cross = CROSSOVER.Uniform_Simple;
            case ("Pmx") -> cross = CROSSOVER.PMX_Tsp;
            default -> throw new Exception("Error with the preset");
        }
        switch (letterCode.substring(12, 15)) {
            case ("Unf") -> mutate = MUTATION.Uniform_Bool;
            case ("Sin") -> mutate = MUTATION.SinglePoint_Bool;
            case ("Exc") -> mutate = MUTATION.Exchange_Tsp;
            case ("Inv") -> mutate = MUTATION.Inversion_Tsp;
            default -> throw new Exception("Error with the preset");
        }
        switch (letterCode.substring(15, 18)) {
            case ("Noe") -> elite = false;
            case ("Elt") -> elite = true;
            default -> throw new Exception("Error with the preset");
        }
        switch (letterCode.substring(18, 21)) {
            case ("Min") -> minMax = MIN_MAX.Min;
            case ("Max") -> minMax = MIN_MAX.Max;
            default -> throw new Exception("Error with the preset");
        }

        GA.setSwitches(ind, sel, fit, cross, mutate, elite, minMax);

        switch (letterCode.substring(21, 24)) {
            case ("Sml") -> smallPopulation();
            case ("Med") -> mediumPopulation();
            case ("Lrg") -> largePopulation();
            case ("Gen") -> manyGensPopulation();
            case ("Qde") -> qdePopulation();
            case ("Tsp") -> tspPopulation();
            default -> throw new Exception("Error with the preset");
        }
    }

    private static void smallPopulation() {
        GA.setSettings(5, 12, 4, 5, 0.95, 0.03);
    }

    private static void mediumPopulation() {
        GA.setSettings(20, 80, 4, 20, 0.95, 0.03);
    }

    private static void largePopulation() {
        GA.setSettings(100, 400, 10, 50, 0.95, 0.03);
    }

    private static void manyGensPopulation() {
        GA.setSettings(100, 400, 10, 100, 0.95, 0.03);
    }

    private static void qdePopulation() {
        int BITS = Equation.NUMBERS_TO_FIND_COUNT * 5;
        GA.setSettings(BITS, 80, 4, 20, 0.95, 0.03);
    }

    private static void tspPopulation() {
        int size = 3;

        if (TSP.filename != null) {
            size = GA.BITS;
        } else {
            while (true) {
                double trigSide = size - 1;
                double temp = trigSide * ((trigSide / 2) + 0.5);
                if (temp == TSP.costArray.length) break;
                else size++;
            }
        }

        GA.setSettings(size, 160, 10, 75, 0.95, 0.5);
    }
}