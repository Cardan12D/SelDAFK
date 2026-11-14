package dev.knyui.client.ui;

import dev.knyui.data.Content;
import dev.knyui.data.KnyData;
import dev.knyui.net.ClientState;
import dev.knyui.net.KnyNet;
import dev.knyui.net.Packets;
import dev.knyui.server.StatEffects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/** Сила / Ловкость / Выносливость + кнопки -/+ (тратят очки). */
public final class StatsTab extends Tab {
    private static final String[] KEYS = {"strength", "agility", "endurance"};
    private static final int[] COL = {0xFFD32F2F, 0xFF43A047, 0xFFF9A825};

    public StatsTab(Anim anim) { super(anim); }

    @Override public String key() { return "stats"; }
    @Override public int accent() { return 0xFFF2B01E; }

    private static int colW(Rect r) { return r.w() / 3; }
    private static int centerX(Rect r, int i) { return r.x() + colW(r) * i + colW(r) / 2; }
    private static int ctrlY(Rect r) { return r.y() + 130; }
    private static Rect minus(Rect r, int i) { return new Rect(centerX(r, i) - 44, ctrlY(r) - 4, 18, 18); }
    private static Rect plus(Rect r, int i) { return new Rect(centerX(r, i) + 26, ctrlY(r) - 4, 18, 18); }

    @Override
    public void render(GuiGraphics g, Font f, Rect r, int mx, int my, float a) {
        KnyData d = ClientState.data;
        for (int i = 0; i < 3; i++) {
            float p = appear(i);
            float al = a * p;
            int cx = centerX(r, i);
            int cy = r.y() + 44 + Math.round((1f - p) * 14f);
            int col = COL[i];

            boolean hov = (mx - cx) * (mx - cx) + (my - cy) * (my - cy) < 40 * 40;
            float h = anim.approach("st" + i, hov ? 1f : 0f, 12f);
            int rad = 36 + Math.round(h * 3f);

            Draw.glow(g, cx, cy, rad + 14, col, (0.10f + 0.16f * h) * al);
            Draw.circleGradient(g, cx, cy, rad, Draw.alpha(0xFFFFFF, 0.16f * al), Draw.alpha(col, (0.55f + 0.25f * h) * al));
            Draw.icon(g, "icon_" + KEYS[i], cx - 20, cy - 20, 40, al);

            Draw.textC(g, f, Component.translatable("knyui.stat." + KEYS[i]), cx, cy + rad + 8, 1.25f, Draw.alpha(0xFFFFFF, al));
            Draw.wrapC(g, f, Component.translatable("knyui.stat." + KEYS[i] + ".desc"), cx, cy + rad + 25, colW(r) - 26, 0.75f, Draw.alpha(0xA8B4BA, al));

            int v = d.getStat(i);
            boolean canMinus = v > Content.BASE_STAT;
            boolean canPlus = d.points > 0 && v < Content.MAX_STAT;
            drawBtn(g, f, minus(r, i), "-", canMinus, mx, my, "m" + i, col, al);
            drawBtn(g, f, plus(r, i), "+", canPlus, mx, my, "p" + i, col, al);

            float shown = anim.approach("sv" + i, v, 14f, v);
            Draw.textC(g, f, Component.literal(String.valueOf(Math.round(shown))), cx, ctrlY(r) - 1, 1.5f, Draw.alpha(0xFFFFFF, al));

            Draw.textC(g, f, bonus(i, d), cx, ctrlY(r) + 24, 0.75f, Draw.alpha(col, 0.9f * al));
        }
    }

    private Component bonus(int i, KnyData d) {
        return switch (i) {
            case 0 -> Component.translatable("knyui.stat.bonus.strength", String.format("%.2f", StatEffects.damageBonus(d.strength)));
            case 1 -> Component.translatable("knyui.stat.bonus.agility", String.format("%.0f", StatEffects.speedPercent(d.agility)));
            default -> Component.translatable("knyui.stat.bonus.endurance", String.format("%.0f", StatEffects.healthBonus(d.endurance)));
        };
    }

    private void drawBtn(GuiGraphics g, Font f, Rect b, String label, boolean enabled, int mx, int my, String key, int col, float al) {
        float h = anim.approach("sb" + key, enabled && b.in(mx, my) ? 1f : 0f, 16f);
        if (h > 0.01f) Draw.rrect(g, b.x() - 1, b.y() - 1, b.w() + 2, b.h() + 2, 5, Draw.alpha(col, 0.35f * h * al));
        int color = enabled ? Draw.lerp(Draw.alpha(0xFFFFFF, al), Draw.alpha(0xFFFFFF, al), h) : Draw.alpha(0x5C666B, al);
        Draw.textC(g, f, Component.literal(label), b.x() + b.w() / 2f, b.y() + 2, 1.3f, color);
    }

    @Override
    public boolean click(double mx, double my, int btn, Rect r) {
        if (btn != 0) return false;
        KnyData d = ClientState.data;
        for (int i = 0; i < 3; i++) {
            if (minus(r, i).in(mx, my) && d.getStat(i) > Content.BASE_STAT) { send(i, -1); return true; }
            if (plus(r, i).in(mx, my) && d.points > 0 && d.getStat(i) < Content.MAX_STAT) { send(i, 1); return true; }
        }
        return false;
    }

    private void send(int stat, int delta) {
        Draw.clickSound();
        KnyNet.CHANNEL.sendToServer(new Packets.SpendStat(stat, delta));
    }
}
