package Enums;

import Main.Individual;
import Problems.Equation;
import Problems.FTTx;
import Problems.TSP;

import java.util.Arrays;
import java.util.HashMap;

import static Main.GA.BITS;

public enum FITNESS_FUNC {
    MostBitsOn {
        @Override
        public MIN_MAX getMinMax() {
            return MIN_MAX.Max;
        }

        @Override
        public double fitness(Individual individual) {
            boolean[] ind = individual.individualB;

            double fitness = 0.0;

            for (boolean bit : ind) {
                if (bit) fitness++;
            }

            return fitness;
        }
    },

    LeastBitsOn {
        @Override
        public MIN_MAX getMinMax() {
            return MIN_MAX.Max;
        }

        @Override
        public double fitness(Individual individual) {
            boolean[] ind = individual.individualB;


            double fitness = 0.0;

            for (boolean bit : ind) {
                if (!bit) fitness++;
            }

            return fitness;
        }
    },

    QuadEquationBoolArray {
        @Override
        public MIN_MAX getMinMax() {
            return MIN_MAX.Min;
        }

        @Override
        public double fitness(Individual individual) throws Exception {
            boolean[] ind = individual.individualB;

            if (!Equation.evaluateParameters())
                throw new Exception("Set appropriate parameters for the quadratic equation");

            int numberLengthBits = BITS / Equation.NUMBERS_TO_FIND.length;
            int[] numbers = new int[Equation.NUMBERS_TO_FIND.length];

            for (int i = 0; i < Equation.NUMBERS_TO_FIND.length; i++) {
                int from = i * numberLengthBits;
                int to = from + numberLengthBits;
                numbers[i] = Equation.binaryToDecimal(Arrays.copyOfRange(ind, from, to));
            }

            double difference = 0.0;

            HashMap<Double, Double> results = Equation.quadraticEquationSolver(numbers);
            for (Double key : results.keySet()) {
                difference += Math.abs(Equation.valuesAtPoints.get(key) - results.get(key));
            }

            return difference;
        }
    },

    Tsp {
        @Override
        public MIN_MAX getMinMax() {
            return MIN_MAX.Min;
        }

        @Override
        public double fitness(Individual individual) {
            int[] ind = individual.individualI;

            double fitness = 0.0;

            for (int i = 0; i < ind.length - 1; i++) {
                fitness += TSP.costMatrix[ind[i]][ind[i + 1]];
            }
            fitness += TSP.costMatrix[ind[ind.length - 1]][ind[0]];

            return fitness;
        }
    },

    FTTxNVP {
        @Override
        public MIN_MAX getMinMax() {
            return MIN_MAX.Max;
        }

        @Override
        public double fitness(Individual individual) {
            int[] ind = individual.individualI;

            return FTTx.npv(ind);
        }
    },

    SequentialCovering {
        @Override
        public MIN_MAX getMinMax() {
            return MIN_MAX.Max;
        }

        @Override
        public double fitness(Individual individual) {
            boolean[] ind = individual.individualB;

            return Problems.SequentialCovering.fitness(ind);
        }
    },

    AocDay5 {
        @Override
        public MIN_MAX getMinMax() {
            return MIN_MAX.Min;
        }

        @Override
        public double fitness(Individual individual) {
            int[] ind = individual.individualI;

            long l = Problems.AocDay5.convertIntArrToLong(ind);

            if (Problems.AocDay5.checkRangesContain(l)) return Problems.AocDay5.mapSeedToLocation(l);
            else return Double.MAX_VALUE;
        }
    };

    // ------------------------------------- Methods -------------------------------------
    public MIN_MAX getMinMax() throws Exception {
        throw new Exception("Not implemented");
    }

    public double fitness(Individual individual) throws Exception {
        throw new Exception("Not implemented");
    }
}