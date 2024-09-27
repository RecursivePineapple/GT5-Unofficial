package gregtech.api.util;

import gtPlusPlus.core.util.math.MathUtils;

public class IntFraction {
    
    public long numerator = 1, denominator = 1;

    public IntFraction() {
        
    }

    public IntFraction(long numerator, long denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public double toDouble() {
        return numerator / (double) denominator;
    }

    public float toFloat() {
        return numerator / (float) denominator;
    }

    public IntFraction clone() {
        return new IntFraction(numerator, denominator);
    }

    public void mul(long n) {
        this.numerator *= n;
    }

    public void div(long n) {
        this.denominator *= n;
    }

    public void reduce() {
        long gcd = MathUtils.gcd(this.numerator, this.denominator);

        if (gcd > 1) {
            this.numerator /= gcd;
            this.denominator /= gcd;
        }
    }

    public int apply(int x) {
        return (int) (x * numerator / denominator);
    }

    public long apply(long x) {
        return x * numerator / denominator;
    }
}
