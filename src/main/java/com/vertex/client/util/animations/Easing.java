package com.vertex.client.util.animations;

import java.util.function.Function;


public enum Easing {
    LINEAR(x -> x),
    SMOOTH_STEP(x -> x * x * (3 - 2 * x)),
    QUARTIC_OUT(x -> 1 - Math.pow(1 - x, 4)),
    QUARTIC_IN(x -> Math.pow(x, 4)),
    CUBIC_OUT(x -> 1 - Math.pow(1 - x, 3)),
    SINE_OUT(x -> Math.sin(x * Math.PI / 2));

    private final Function<Double, Double> function;

    Easing(Function<Double, Double> function) {
        this.function = function;
    }

    public double apply(double x) {
        return function.apply(clamp01(x));
    }

    public float apply(float x) {
        return (float) apply((double) x);
    }


    public float ease(float t, float b, float c, float d) {
        double progress = d == 0 ? 0 : clamp01(t / d);
        return (float) (b + c * function.apply(progress));
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
