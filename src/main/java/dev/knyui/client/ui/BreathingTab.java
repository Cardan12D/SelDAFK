package dev.knyui.client.ui;

import dev.knyui.data.Content;
import dev.knyui.data.KnyData;
import dev.knyui.net.ClientState;
import dev.knyui.net.KnyNet;
import dev.knyui.net.Packets;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Слева — эмблема дыхания и слоты выбора, справа — карточки стилей с разблокировкой за очки. */
public final class BreathingTab extends Tab {
    private static final int LW = 128, CW = 112, CH = 152, GAP = 10, SLOT = 30;
    private final HScroll scroll = new HScroll();
    private int selected = 0;
    private boolean draggingBar;

    public BreathingTab(Anim anim) { super(anim); }

    @Override public String key() { return "breathing"; }
    @Override public int accent() { return Content.BREATHINGS.get(selected).color(); }

    private Rect cardsArea(Rect r) { return new Rect(r.x() + LW + 12, r.y(), r.w() - LW - 12, r.h()); }
    private Rect card(Rect area, int i) { return new Rect(area.x() + i * (CW + GAP) - Math.round(scroll.cur), area.y() + 6, CW, CH); }
    private Rect slot(Rect r, int i) { return new Rect(r.x() + (LW - (3 * SLOT + 16)) / 2 + i * (SLOT + 8), r.y() + 128, SLOT, SLOT); }
    private Rect button(Rect c) { return new Rect(c.x() + 10, c.y() + CH - 32, CW - 20, 22); }

    @Override
    public void render(GuiGraphics g, Font f, Rect r, int mx, int my, float a) {
        KnyData d = ClientState.data;
        Content.Breathing br = Content.BREATHINGS.get(selected);
        int col = br.color();

        // ----- левая часть: эмблема -----
        float pe = appear(0);
        int ex = r.x() + LW / 2, ey = r.y() + 56;
        float pulse = 0.5f + 0.5f * (float) Math.sin(System.nanoTime() / 1e9 * 2.0);
        Draw.glow(g, ex, ey, 62, col, (0.12f + 0.08f * pulse) * a * pe);
        Draw.circleGradient(g, ex, ey, 42, Draw.alpha(0xFFFFFF, 0.10f * a * pe), Draw.alpha(col, 0.30f * a * pe));
        Draw.icon(g, "breath_" + br.id(), ex - 28, ey - 28, 56, a * pe);
        Draw.textC(g, f, Component.translatable("knyui.breathing." + br.id()), ex, r.y() + 106, 1.15f, Draw.alpha(col, a * pe));

        for (int i = 0; i < Content.BREATHINGS.size(); i++) {
            Content.Breathing b = Content.BREATHINGS.get(i);
            Rect s = slot(r, i);
            boolean sel = i == selected;
            float h = anim.approach("bs" + i, (sel ? 1f : 0f) + (s.in(mx, my) ? 0.5f : 0f), 14f);
            int bc = Draw.lerp(Draw.alpha(0x2B3A44, a), Draw.alpha(b.color(), a), Math.min(1f, h));
            Draw.rrect(g, s.x() - 1, s.y() - 1, s.w() + 2, s.h() + 2, 5, bc);
            Draw.rrect(g, s.x(), s.y(), s.w(), s.h(), 4, Draw.alpha(0x0E181E, 0.95f * a));
            Draw.icon(g, "breath_" + b.id(), s.x() + 4, s.y() + 4, 22, a * (0.45f + 0.55f * Math.min(1f, h)));
        }

        // ----- правая часть: карточки стилей -----
        Rect area = cardsArea(r);
        List<Content.Style> styles = br.styles();
        scroll.tick(anim.dt, styles.size() * (CW + GAP) - GAP, area.w());
        g.enableScissor(area.x() - 4, area.y() - 8, area.right() + 4, area.bottom());
        for (int i = 0; i < styles.size(); i++) {
            Content.Style st = styles.get(i);
            Rect c = card(area, i);
            if (c.right() < area.x() - 20 || c.x() > area.right() + 20) continue;
            float p = appear(i + 1);
            float al = a * p;
            boolean hover = c.in(mx, my) && mx >= area.x() && mx < area.right();
            float h = anim.approach("bc" + i, hover ? 1f : 0f, 14f);
            int x = c.x(), y = c.y() - Math.round(h * 3f) + Math.round((1f - p) * 16f);
            int cx = x + CW / 2;
            boolean owned = d.unlocked.contains(st.id());

            Draw.rrect(g, x - 1, y - 1, CW + 2, CH + 2, 7, Draw.lerp(Draw.alpha(0x22343E, al), Draw.alpha(col, al), 0.45f + 0.55f * h));
            Draw.rrect(g, x, y, CW, CH, 6, Draw.alpha(0x0A1319, 0.97f * al));
            g.fillGradient(x + 2, y + 2, x + CW - 2, y + CH - 2, Draw.alpha(col, (0.20f + 0.10f * h) * al), Draw.alpha(col, 0f));

            Draw.textC(g, f, Component.translatable("knyui.style.title", i + 1), cx, y + 10, 1.25f, Draw.alpha(col, al));
            float ty = Draw.wrapC(g, f, Component.translatable("knyui.style." + st.id() + ".name"), cx, y + 30, CW - 14, 0.95f, Draw.alpha(0xFFFFFF, al));
            Draw.wrapC(g, f, Component.translatable("knyui.style." + st.id() + ".desc"), cx, ty + 3, CW - 18, 0.66f, Draw.alpha(0x93A0A6, al));

            g.fill(x + 10, y + CH - 58, x + CW - 10, y + CH - 57, Draw.alpha(0xFFFFFF, 0.25f * al));
            Draw.textC(g, f, Component.translatable("knyui.style.cost", st.cost()), cx, y + CH - 52, 0.85f, Draw.alpha(0xFFFFFF, al));

            Rect b = new Rect(x + 10, y + CH - 32, CW - 20, 22);
            if (owned) {
                Draw.textC(g, f, Component.translatable("knyui.style.unlocked"), cx, y + CH - 25, 0.9f, Draw.alpha(0x66BB6A, al));
            } else if (st.needsTraining() && !d.trained.contains(st.id())) {
                Draw.wrapC(g, f, Component.translatable("knyui.style.need_training"), cx, y + CH - 33, CW - 16, 0.85f, Draw.alpha(0xFF3B30, al));
            } else {
                boolean can = d.points >= st.cost();
                float bh = anim.approach("bb" + i, can && b.in(mx, my) ? 1f : 0f, 16f);
                Draw.rrect(g, b.x(), b.y(), b.w(), b.h(), 6, Draw.lerp(Draw.alpha(0x16232B, al), Draw.alpha(col, al), 0.15f + 0.65f * bh));
                Component label = can ? Component.translatable("knyui.style.unlock") : Component.translatable("knyui.style.no_points");
                Draw.textC(g, f, label, cx, b.y() + 7, 0.9f, can ? Draw.alpha(0xFFFFFF, al) : Draw.alpha(0xFF6B5E, al));
            }
        }
        g.disableScissor();
        scroll.draw(g, bar(area), col, a);
    }

    @Override
    public boolean click(double mx, double my, int btn, Rect r) {
        if (btn != 0) return false;
        for (int i = 0; i < Content.BREATHINGS.size(); i++) {
            if (slot(r, i).in(mx, my)) {
                if (i != selected) { selected = i; scroll.target = 0; scroll.cur = 0; shownAt = System.nanoTime(); Draw.clickSound(); }
                return true;
            }
        }
        Rect area = cardsArea(r);
        if (scroll.hit(bar(area), mx, my)) { draggingBar = true; scroll.dragTo(bar(area), mx); return true; }
        KnyData d = ClientState.data;
        List<Content.Style> styles = Content.BREATHINGS.get(selected).styles();
        for (int i = 0; i < styles.size(); i++) {
            Content.Style st = styles.get(i);
            Rect c = card(area, i);
            if (!d.unlocked.contains(st.id()) && button(c).in(mx, my) && mx >= area.x() && mx < area.right()) {
                Draw.clickSound();
                KnyNet.CHANNEL.sendToServer(new Packets.UnlockStyle(st.id()));
                return true;
            }
        }
        return false;
    }

    @Override public boolean scroll(double mx, double my, double d, Rect r) { scroll.wheel(d); return true; }

    @Override
    public boolean drag(double mx, double my, Rect r) {
        if (!draggingBar) return false;
        scroll.dragTo(bar(cardsArea(r)), mx);
        return true;
    }

    @Override public void onShow() { super.onShow(); draggingBar = false; }
    @Override public void release() { draggingBar = false; }
}
