public class Presets {
    /**
     * <h2>Preset naming scheme:</h2>
     * Ind. type + selection + fitness + crossover + mutation + elitism + min/max + pop. size
     * <ol>
     *     <li>Individual type (Bol, Int, Tsp, Ftx)          | Binary, Integer, TSP int[], FTTx int[]</li>
     *     <li>Selection type (Rol, Tor)                     | Roulette, Tournament</li>
     *     <li>Fitness func (Mbo, Lbo, Qde, Tsp, Nvp)        | MostBitsOn, LeastBitsOn, QuadraticEq, TSP, FTTx nvp</li>
     *     <li>Crossover (Sin, Npo, Unf, Pmx, Ftx)           | Single point (bool), n Point, Uniform, PMX, Single point (int)</li>
     *     <li>Mutation (Unf, Sin, Exc, Inv, Ari)            | Uniform, Single point, Exchange, Inversion, Arithmetic</li>
     *     <li>Elitism (Elt, Noe)                            | Elitism, No elitism</li>
     *     <li>Minimization/Maximization (Min, Max)          | Minimization, Maximization</li>
     *     <li>Pop. size (Sml, Med, Lrg, Gen, Qde, Tsp, Ftx) | Small, Medium, Large, Many gen, Quadratic eq., TSP, FTTx</li>
     * </ol>
     * <p>
     *     <b>Example:</b> BolTorMboSinUnfEltMaxMed <br>
     *     <b>Example:</b> BolTorLboSinUnfEltMaxMed <br>
     *     <b>Example:</b> BolTorQdeSinUnfEltMaxQde <br>
     *     <b>Example:</b> TspTorTspPmxExcNoeMaxTsp <br>
     *     <b>Example:</b> FtxTorNvpFtxAriNoeMaxFtx
     * </p>
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
            case ("Ftx") -> ind = INDIVIDUAL_TYPE.fttxIntArray;
            default -> throw new Exception("Error with the preset (individual type)");
        }
        switch (letterCode.substring(3, 6)) {
            case ("Rol") -> sel = SELECTION.Roulette;
            case ("Tor") -> sel = SELECTION.Tournament;
            default -> throw new Exception("Error with the preset (selection type)");
        }
        switch (letterCode.substring(6, 9)) {
            case ("Mbo") -> fit = FITNESS_FUNC.MostBitsOn;
            case ("Lbo") -> fit = FITNESS_FUNC.LeastBitsOn;
            case ("Qde") -> fit = FITNESS_FUNC.QuadEquationBoolArray;
            case ("Tsp") -> fit = FITNESS_FUNC.Tsp;
            case ("Nvp") -> fit = FITNESS_FUNC.FttxNVP;
            default -> throw new Exception("Error with the preset (fitness function)");
        }
        switch (letterCode.substring(9, 12)) {
            case ("Sin") -> cross = CROSSOVER.SinglePoint_Simple;
            case ("Npo") -> cross = CROSSOVER.nPoint_Simple;
            case ("Unf") -> cross = CROSSOVER.Uniform_Simple;
            case ("Pmx") -> cross = CROSSOVER.PMX_Tsp;
            case ("Ftx") -> cross = CROSSOVER.SinglePoint_Fttx;
            default -> throw new Exception("Error with the preset (crossover)");
        }
        switch (letterCode.substring(12, 15)) {
            case ("Unf") -> mutate = MUTATION.Uniform_Bool;
            case ("Sin") -> mutate = MUTATION.SinglePoint_Bool;
            case ("Exc") -> mutate = MUTATION.Exchange_Tsp;
            case ("Inv") -> mutate = MUTATION.Inversion_Tsp;
            case ("Ari") -> mutate = MUTATION.Arithmetic_Fttx;
            default -> throw new Exception("Error with the preset (mutation)");
        }
        switch (letterCode.substring(15, 18)) {
            case ("Noe") -> elite = false;
            case ("Elt") -> elite = true;
            default -> throw new Exception("Error with the preset (elitism)");
        }
        switch (letterCode.substring(18, 21)) {
            case ("Min") -> minMax = MIN_MAX.Min;
            case ("Max") -> minMax = MIN_MAX.Max;
            default -> throw new Exception("Error with the preset (min/max)");
        }

        GA.setSwitches(ind, sel, fit, cross, mutate, elite, minMax);

        switch (letterCode.substring(21, 24)) {
            case ("Sml") -> smallPopulation();
            case ("Med") -> mediumPopulation();
            case ("Lrg") -> largePopulation();
            case ("Gen") -> manyGensPopulation();
            case ("Qde") -> qdePopulation();
            case ("Tsp") -> tspPopulation();
            case ("Ftx") -> ftxPopulation();
                default -> throw new Exception("Error with the preset");
        }
    }

    private static void smallPopulation() {
        GA.setSettings(5, 12, 3, 5, 0.95, 0.03);
    }

    private static void mediumPopulation() {
        GA.setSettings(20, 32, 4, 10, 0.95, 0.03);
    }

    private static void largePopulation() {
        GA.setSettings(100, 400, 10, 40, 0.95, 0.03);
    }

    private static void manyGensPopulation() {
        GA.setSettings(100, 400, 10, 100, 0.95, 0.03);
    }

    private static void qdePopulation() {
        int BITS = Equation.NUMBERS_TO_FIND.length * 5;
        GA.setSettings(BITS, 32, 4, 20, 0.95, 0.03);
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

        GA.setSettings(size, 160, 8, 200, 0.9, 0.25);
    }

    private static void ftxPopulation() throws Exception {
        if (FTTx.noOfAreas == 0) throw new Exception("FTTx not initialised");
        else GA.setSettings(FTTx.noOfAreas, 280, 10, 600, 0.9, 0.5);
    }
}