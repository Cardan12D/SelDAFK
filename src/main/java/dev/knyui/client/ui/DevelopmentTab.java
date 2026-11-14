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

/** Пассивные способности (Метка истребителя и т.д.): карточки с прокруткой и замком по рангу. */
public final class DevelopmentTab extends Tab {
    private static final int CW = 118, CH = 158, GAP = 10;
    private final HScroll scroll = new HScroll();
    private boolean draggingBar;

    public DevelopmentTab(Anim anim) { super(anim); }

    @Override public String key() { return "development"; }
    @Override public int accent() { return 0xFFE53935; }

    private Rect card(Rect r, int i) {
        return new Rect(r.x() + i * (CW + GAP) - Math.round(scroll.cur), r.y() + 6, CW, CH);
    }

    @Override
    public void render(GuiGraphics g, Font f, Rect r, int mx, int my, float a) {
        KnyData d = ClientState.data;
        List<Content.Passive> list = Content.PASSIVES;
        scroll.tick(anim.dt, list.size() * (CW + GAP) - GAP, r.w());

        g.enableScissor(r.x() - 4, r.y() - 8, r.right() + 4, r.bottom());
        for (int i = 0; i < list.size(); i++) {
            Content.Passive ps = list.get(i);
            Rect c = card(r, i);
            if (c.right() < r.x() - 20 || c.x() > r.right() + 20) continue;
            float p = appear(i);
            float al = a * p;
            boolean locked = d.rank < ps.minRank();
            boolean hover = c.in(mx, my) && mx >= r.x() && mx < r.right();
            float h = anim.approach("dp" + i, hover ? 1f : 0f, 14f);
            int x = c.x(), y = c.y() - Math.round(h * 3f) + Math.round((1f - p) * 16f);
            int col = ps.color();
            int cx = x + CW / 2;

            Draw.rrect(g, x - 1, y - 1, CW + 2, CH + 2, 7, Draw.lerp(Draw.alpha(0x22343E, al), Draw.alpha(col, al), 0.40f + 0.60f * h));
            Draw.rrect(g, x, y, CW, CH, 6, Draw.alpha(0x0A1319, 0.97f * al));
            g.fillGradient(x + 2, y + CH / 2, x + CW - 2, y + CH - 2, Draw.alpha(col, 0f), Draw.alpha(col, (0.22f + 0.12f * h) * al));

            // подпись минимального ранга + линия
            g.fill(x + 8, y + 11, x + 36, y + 12, Draw.alpha(0xFFFFFF, 0.30f * al));
            Draw.text(g, f, Component.translatable("knyui.rank." + ps.minRank()), x + 42, y + 7, 0.8f, Draw.alpha(0xA8B4BA, al));

            // иконка
            Draw.circleGradient(g, cx, y + 46, 25, Draw.alpha(0x34444E, al), Draw.alpha(0x1A262D, al));
            Draw.icon(g, "passive_" + ps.id(), cx - 17, y + 29, 34, al);

            float ty = y + 76;
            ty = Draw.wrapC(g, f, Component.translatable("knyui.passive." + ps.id() + ".name"), cx, ty, CW - 14, 1.0f, Draw.alpha(col, al));
            Draw.textC(g, f, Component.translatable("knyui.passive.duration", ps.minutes()), cx, ty + 2, 0.78f, Draw.alpha(0xFFFFFF, al));
            Draw.textC(g, f, Component.translatable("knyui.passive.type"), cx, ty + 12, 0.78f, Draw.alpha(0xFFFFFF, al));
            Draw.wrapC(g, f, Component.translatable("knyui.passive." + ps.id() + ".desc"), cx, ty + 25, CW - 16, 0.66f, Draw.alpha(0x93A0A6, al));

            if (locked) {
                Draw.rrect(g, x, y, CW, CH, 6, Draw.alpha(0x000000, 0.74f * al));
                Draw.icon(g, "icon_lock", cx - 19, y + CH / 2 - 42, 38, al);
                Draw.wrapC(g, f, Component.translatable("knyui.passive.locked"), cx, y + CH / 2 + 2, CW - 18, 0.85f, Draw.alpha(0xFFFFFF, al));
            }
        }
        g.disableScissor();
        scroll.draw(g, bar(r), accent(), a);
    }

    @Override
    public boolean click(double mx, double my, int btn, Rect r) {
        if (btn != 0) return false;
        if (scroll.hit(bar(r), mx, my)) { draggingBar = true; scroll.dragTo(bar(r), mx); return true; }
        KnyData d = ClientState.data;
        for (int i = 0; i < Content.PASSIVES.size(); i++) {
            Content.Passive ps = Content.PASSIVES.get(i);
            if (card(r, i).in(mx, my) && mx >= r.x() && mx < r.right() && d.rank >= ps.minRank()) {
                Draw.clickSound();
                KnyNet.CHANNEL.sendToServer(new Packets.ActivatePassive(ps.id()));
                return true;
            }
        }
        return false;
    }

    @Override public boolean scroll(double mx, double my, double d, Rect r) { scroll.wheel(d); return true; }

    @Override
    public boolean drag(double mx, double my, Rect r) {
        if (!draggingBar) return false;
        scroll.dragTo(bar(r), mx);
        return true;
    }

    @Override public void onShow() { super.onShow(); draggingBar = false; }
    @Override public void release() { draggingBar = false; }
}
