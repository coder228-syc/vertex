package com.vertex.client.util.animations;

public class Animation {

    private long duration;
    private float startValue;
    private float endValue;
    private float value;
    private long startTime;
    private boolean running;
    private Easing easing;

    public Animation(long duration, float startValue, Easing easing) {
        this.duration = Math.max(1L, duration);
        this.startValue = startValue;
        this.endValue = startValue;
        this.value = startValue;
        this.easing = easing != null ? easing : Easing.LINEAR;
        this.running = false;
    }

    public Animation(long duration, float startValue) {
        this(duration, startValue, Easing.LINEAR);
    }

    public Animation(long duration, Easing easing) {
        this(duration, 0f, easing);
    }

    public Animation(long duration) {
        this(duration, 0f, Easing.LINEAR);
    }

    public void animateTo(float target) {
        this.startValue = this.value;
        this.endValue = target;
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }

    public void setValue(float value) {
        this.value = value;
        this.startValue = value;
        this.endValue = value;
        this.running = false;
    }

    public void reset(float value) {
        setValue(value);
    }

    public float update() {
        return getValue();
    }

    public float update(boolean forward) {
        float target = forward ? 1f : 0f;
        if (Float.compare(endValue, target) != 0 || !running) {
            animateTo(target);
        }
        return getValue();
    }

    public float update(float target) {
        if (Float.compare(endValue, target) != 0 || !running) {
            animateTo(target);
        }
        return getValue();
    }

    public float getValue() {
        if (!running) return value;

        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= duration) {
            value = endValue;
            running = false;
            return value;
        }

        float progress = (float) elapsed / (float) duration;
        float eased = easing.apply(progress);
        value = startValue + (endValue - startValue) * eased;
        return value;
    }

    public boolean isDone() {
        if (!running) return true;
        getValue();
        return !running;
    }

    public boolean isRunning() {
        return running;
    }

    public void setDuration(long duration) {
        this.duration = Math.max(1L, duration);
    }

    public void setEasing(Easing easing) {
        this.easing = easing != null ? easing : Easing.LINEAR;
    }

    public float getEndValue() {
        return endValue;
    }

    public float getStartValue() {
        return startValue;
    }
}