package Enums;

public enum MIN_MAX {
    Min {
        @Override
        public boolean compareFitness(double current, double contender) {
            return current < contender;
        }
    },

    Max {
        @Override
        public boolean compareFitness(double current, double contender) {
            return current > contender;
        }
    };


    // ------------------------------------- Methods -------------------------------------
    // returns true if the contender is better than the current
    public boolean compareFitness(double current, double contender) throws Exception {
        throw new Exception("Not implemented");
    }
}