package dev.knyui.client.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public abstract class Tab {
    protected final Anim anim;
    protected long shownAt = System.nanoTime();

    protected Tab(Anim anim) { this.anim = anim; }

    public abstract String key();
    public abstract int accent();
    public abstract void render(GuiGraphics g, Font f, Rect r, int mx, int my, float a);

    public Component title() { return Component.translatable("knyui.tab." + key()); }
    public void onShow() { shownAt = System.nanoTime(); }
    public boolean click(double mx, double my, int btn, Rect r) { return false; }
    public boolean scroll(double mx, double my, double d, Rect r) { return false; }
    public boolean drag(double mx, double my, Rect r) { return false; }
    public void release() {}

    /** 0..1 — анимация появления n-го элемента (каскадом). */
    protected float appear(int index) {
        float t = (System.nanoTime() - shownAt) / 1e9f - index * 0.07f;
        return Anim.easeOut(Anim.clamp(t / 0.3f, 0f, 1f));
    }

    protected static Rect bar(Rect r) { return new Rect(r.x(), r.y() + r.h() - 8, r.w(), 4); }
}
