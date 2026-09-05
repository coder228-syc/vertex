package com.vertex.client.modules.setting;

import java.util.function.Supplier;

public class ColorSetting extends Setting {

    private final String name;
    private int color;
    private final int defaultValue;
    private Supplier<Boolean> visible;
    public boolean expanded;

    public ColorSetting(String name, int color) {
        this.name = name;
        this.color = color;
        this.defaultValue = color;
        setVisible(() -> true);
    }

    public ColorSetting(String name, int color, Supplier<Boolean> visible) {
        this.name = name;
        this.color = color;
        this.defaultValue = color;
        setVisible(visible);
    }

    @Override
    public boolean isVisible() {
        return visible.get();
    }

    @Override
    public void setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
    }

    @Override
    public String getName() {
        return name;
    }

    public int get() {
        return color;
    }

    public void set(int color) {
        this.color = color;
    }

    public int getDefaultValue() {
        return defaultValue;
    }

    public void resetToDefault() {
        this.color = defaultValue;
    }
}
