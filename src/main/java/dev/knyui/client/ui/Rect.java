package dev.knyui.client.ui;

public record Rect(int x, int y, int w, int h) {
    public int right() { return x + w; }
    public int bottom() { return y + h; }
    public boolean in(double px, double py) { return px >= x && px < x + w && py >= y && py < y + h; }
}
