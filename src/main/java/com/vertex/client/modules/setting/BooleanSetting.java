package com.vertex.client.modules.setting;

import java.util.function.Supplier;

public class BooleanSetting extends Setting {

    private boolean value;

    public BooleanSetting(String name, boolean value) {
        this.name = name;
        this.value = value;
        setVisible(() -> true);
    }

    public BooleanSetting(String name, boolean value, Supplier<Boolean> visible) {
        this.name = name;
        this.value = value;
        setVisible(visible);
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }
}
