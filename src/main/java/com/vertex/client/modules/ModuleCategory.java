package com.vertex.client.modules;

public enum ModuleCategory {
    Combat("f"),
    Movement("w"),
    Render("E"),
    Display("q"),
    Player("r"),
    Misc("v");

    public final String icon;

    ModuleCategory(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name();
    }

    public String getIconGlyph() {
        return icon;
    }
}