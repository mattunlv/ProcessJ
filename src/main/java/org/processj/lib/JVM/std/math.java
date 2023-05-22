package std;

import java.lang.StrictMath;

public class math {
    
    public static final double PI = 3.14159265358979323846;
    
    public static final double M_E = 2.7182818284590452354;
    public static final double M_LOG2E = 1.4426950408889634074;
    public static final double M_LOG10E = 0.43429448190325182765;
    public static final double M_LN2 = 0.69314718055994530942;
    public static final double M_LN10 = 2.30258509299404568402;
    public static final double M_PI = 3.14159265358979323846;
    public static final double M_PI_2 = 1.57079632679489661923;
    public static final double M_PI_4 = 0.78539816339744830962;
    public static final double M_1_PI = 0.31830988618379067154;
    public static final double M_2_PI = 0.63661977236758134308;
    public static final double M_2_SQRTPI = 1.12837916709551257390;
    public static final double M_SQRT2 = 1.41421356237309504880;
    public static final double M_SQRT1_2 = 0.70710678118654752440;
    
    public static double acos(double x) {
        return StrictMath.acos(x);
    }
    
    public static double asin(double x) {
        return StrictMath.asin(x);
    }
    
    public static double atan(double x) {
        return StrictMath.atan(x);
    }
    
    public static double atan2(double y, double x) {
        return StrictMath.atan2(y, x);
    }
    
    public static double cos(double x) {
        return StrictMath.cos(x);
    }
    
    public static double cosh(double x) {
        return StrictMath.cosh(x);
    }
    
    public static double sin(double x) {
        return StrictMath.sin(x);
    }
    
    public static double sinh(double x) {
        return StrictMath.sinh(x);
    }
    
    public static double tanh(double x) {
        return StrictMath.tanh(x);
    }
    
    public static double exp(double x) {
        return StrictMath.exp(x);
    }
    
    public static double log(double x) {
        return StrictMath.log(x);
    }
    
    public static double log10(double x) {
        return StrictMath.log10(x);
    }
    
    public static double pow(double x, double y) {
        return StrictMath.pow(x, y);
    }
    
    public static double sqrt(double x) {
        return StrictMath.sqrt(x);
    }
    
    public static double cbrt(double x) {
        return StrictMath.cbrt(x);
    }
    
    public static double ceil(double x) {
        return StrictMath.ceil(x);
    }
    
    public static double fabs(double x) {
        return StrictMath.abs(x);
    }
    
    public static long abs(long x) {
        return StrictMath.abs(x);
    }
    
    public static int abs(int x) {
        return StrictMath.abs(x);
    }
    
    public static double floor(double x) {
        return StrictMath.floor(x);
    }
    
    public static double toRadians(double ang) {
        return ang / 180.0 * PI;
    }
    
    public static double toDegrees(double ang) {
        return ang * 180.0 / PI;
    }
}
