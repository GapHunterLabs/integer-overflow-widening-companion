class Calc {

    // Flagged: int * int computed first, then widened -- overflows for
    // large values (e.g. a=100000, b=100000 gives a wrong negative
    // result instead of 10,000,000,000).
    long computeUnsafe(int a, int b) {
        long result = a * b;
        return result;
    }

    // Not flagged: widening cast on one operand forces 64-bit arithmetic.
    long computeSafe(int a, int b) {
        long result = (long) a * b;
        return result;
    }
}
