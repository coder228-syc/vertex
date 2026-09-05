package com.vertex.client.modules.setting;

import java.util.function.Supplier;

public class SliderSetting extends Setting {

    public boolean dragging = false;
    private Number value;
    private final double min;
    private final double max;
    private final double increment;
    private final Number defaultValue;
    private String desc = "";

    public SliderSetting(String name, Number value, Number min, Number max, Number increment) {
        this(name, value, min, max, increment, () -> true);
    }

    public SliderSetting(String name, Number value, Number min, Number max, Number increment, Supplier<Boolean> visible) {
        this.name = name;
        this.value = value;
        this.defaultValue = value;
        this.min = min.doubleValue();
        this.max = max.doubleValue();
        this.increment = increment.doubleValue();
        setVisible(visible);
    }

    public Number get() {
        return value;
    }

    public void set(double value) {
        this.value = value;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getIncrement() {
        return increment;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void resetToDefault() {
        this.value = defaultValue;
    }
}
