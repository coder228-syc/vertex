package com.vertex.client.clickgui.search;

public class SearchState {
    public String text = "";
    public boolean focused = false;
    public int cursorPosition = 0;
    public long lastCursorBlink = System.currentTimeMillis();
    public boolean cursorVisible = true;
    public int hoverIndex = -1;
    public int dropdownScroll = 0;

    public int barX;
    public int barY;
    public int barW;
    public int barH;
    public int dropX;
    public int dropY;
    public int dropW;
    public int dropH;
    public int visibleResultCount;

    public SearchState() {
    }
}
