package dev.knyui.client.ui;

import net.minecraft.client.gui.GuiGraphics;

/** Горизонтальная прокрутка карточек с плавностью + полоса прокрутки. */
public final class HScroll {
    public float target, cur;
    private int contentW, viewW, thumbW = 24;

    public void tick(float dt, int contentW, int viewW) {
        this.contentW = contentW; this.viewW = viewW;
        target = Anim.clamp(target, 0, max());
        cur += (target - cur) * Math.min(1f, dt * 14f);
        if (Math.abs(target - cur) < 0.05f) cur = target;
    }

    public float max() { return Math.max(0, contentW - viewW); }
    public void wheel(double delta) { target = Anim.clamp(target - (float) delta * 42f, 0, max()); }

    public void draw(GuiGraphics g, Rect bar, int accent, float a) {
        Draw.rrect(g, bar.x(), bar.y() + 1, bar.w(), 2, 1, Draw.alpha(0xFFFFFF, 0.16f * a));
        thumbW = Math.max(28, (int) (bar.w() * viewW / (float) Math.max(contentW, viewW)));
        int tx = bar.x() + (max() > 0 ? (int) ((bar.w() - thumbW) * (cur / max())) : 0);
        Draw.rrect(g, tx, bar.y(), thumbW, 4, 2, Draw.alpha(accent, a));
    }

    public boolean hit(Rect bar, double mx, double my) {
        return mx >= bar.x() && mx < bar.right() && my >= bar.y() - 5 && my < bar.y() + 9;
    }

    public void dragTo(Rect bar, double mx) {
        if (max() <= 0) return;
        float frac = (float) ((mx - bar.x() - thumbW / 2.0) / Math.max(1, bar.w() - thumbW));
        target = Anim.clamp(frac, 0f, 1f) * max();
    }
}
