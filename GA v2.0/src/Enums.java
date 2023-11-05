public class Enums {
}

enum INDIVIDUAL_TYPE {boolArray, intArray, tspIntArray, fttxIntArray}

enum FITNESS_FUNC {MostBitsOn, LeastBitsOn, QuadEquationBoolArray, Tsp, FttxNVP}

enum SELECTION {Roulette, Tournament}

enum CROSSOVER {SinglePoint_Simple, nPoint_Simple, Uniform_Simple, PMX_Tsp, SinglePoint_Fttx}

enum MUTATION {Uniform_Bool, SinglePoint_Bool, Exchange_Tsp, Inversion_Tsp, Arithmetic_Fttx}

enum MIN_MAX {Min, Max}