package com.vertex.client.util;

import net.minecraft.util.math.MathHelper;

public class MathUtil implements IMinecraft {

    public static double deltaTick() {
        return mc.getCurrentFps() > 0 ? 1.0 / mc.getCurrentFps() : 1.0;
    }

    public static float lerp(float end, float start, float multiple) {
        return (float) (end + (start - end) * MathHelper.clamp(deltaTick() * multiple, 0.0, 1.0));
    }

    public static double lerp(double end, double start, double multiple) {
        return end + (start - end) * MathHelper.clamp(deltaTick() * multiple, 0.0, 1.0);
    }

    public static Double interpolate(double old, double value, double interpolation) {
        return old + (value - old) * interpolation;
    }

    public static float interpolate(float old, float value, double interpolation) {
        return (float) (old + (value - old) * interpolation);
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return Math.round((float) (oldValue + (newValue - oldValue) * interpolationValue));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
