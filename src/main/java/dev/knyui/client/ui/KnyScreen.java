package dev.knyui.client.ui;

import dev.knyui.client.ClientEvents;
import dev.knyui.data.Content;
import dev.knyui.data.KnyData;
import dev.knyui.net.ClientState;
import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Главный экран «Персонаж»: боковая панель + вкладки (Развитие / Дыхание / Характеристики). */
public final class KnyScreen extends Screen {
    private static final int SIDEBAR_W = 96;
    private static final int[] BLOB_COLORS = {0x1E88E5, 0xFFB300, 0x00BCD4};

    private final Anim anim = new Anim();
    private final List<Tab> tabs = new ArrayList<>();
    private final Rect[] tabHeads = new Rect[3];
    private int active = 2; // открываем сразу «Характеристики»
    private Rect panel = new Rect(0, 0, 10, 10), content = new Rect(0, 0, 10, 10);

    public KnyScreen() { super(Component.literal("Kimetsu UI")); }

    @Override
    protected void init() {
        tabs.clear();
        tabs.add(new DevelopmentTab(anim));
        tabs.add(new BreathingTab(anim));
        tabs.add(new StatsTab(anim));
        int pw = Math.min(width - 16, 470), ph = Math.min(height - 16, 262);
        panel = new Rect((width - pw) / 2, (height - ph) / 2, pw, ph);
        content = new Rect(panel.x() + SIDEBAR_W + 14, panel.y() + 66, pw - SIDEBAR_W - 28, ph - 76);
        tabs.get(active).onShow();
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        anim.tick();
        KnyData d = ClientState.data;
        Minecraft mc = Minecraft.getInstance();
        float open = anim.approach("open", 1f, 10f);

        // фон + плавающие блики
        g.fillGradient(0, 0, width, height, Draw.alpha(0x000A12, 0.62f * open), Draw.alpha(0x001A24, 0.82f * open));
        float t = System.nanoTime() / 1e9f;
        for (int k = 0; k < 3; k++) {
            int bx = (int) (width * (0.2f + 0.3f * k) + Math.sin(t * 0.3f + k * 2f) * 40f);
            int by = (int) (height * (0.3f + 0.2f * k) + Math.cos(t * 0.25f + k) * 30f);
            Draw.glow(g, bx, by, 90 + k * 14, BLOB_COLORS[k], 0.10f * open);
        }

        // «выезд» панели при открытии
        g.pose().pushPose();
        float s = 0.93f + 0.07f * open;
        float pcx = panel.x() + panel.w() / 2f, pcy = panel.y() + panel.h() / 2f;
        g.pose().translate(pcx, pcy, 0);
        g.pose().scale(s, s, 1f);
        g.pose().translate(-pcx, -pcy, 0);
        float a = open;

        Draw.rrect(g, panel.x() - 1, panel.y() - 1, panel.w() + 2, panel.h() + 2, 11, Draw.alpha(0x2B5566, a));
        Draw.rrect(g, panel.x(), panel.y(), panel.w(), panel.h(), 10, Draw.alpha(0x070F14, 0.96f * a));

        renderSidebar(g, d, mc, a);
        renderHeader(g, d, mx, my, a);

        // содержимое вкладки с анимацией переключения
        float tp = anim.approach("tab", 1f, 9f, 1f);
        int off = Math.round((1f - tp) * 22f);
        g.pose().pushPose();
        g.pose().translate(off, 0, 0);
        tabs.get(active).render(g, font, content, mx - off, my, a * tp);
        g.pose().popPose();

        g.pose().popPose();
    }

    private void renderSidebar(GuiGraphics g, KnyData d, Minecraft mc, float a) {
        int x = panel.x(), y = panel.y();
        Draw.rrect(g, x, y, SIDEBAR_W, panel.h(), 10, Draw.alpha(0x03080B, 0.85f * a));
        g.fill(x + SIDEBAR_W, y + 8, x + SIDEBAR_W + 1, y + panel.h() - 8, Draw.alpha(0xFFFFFF, 0.08f * a));

        int cx = x + SIDEBAR_W / 2, cy = y + 44;
        Draw.glow(g, cx, cy, 40, 0xFFB300, 0.20f * a);
        Draw.circle(g, cx, cy, 28, Draw.alpha(0xFFB300, a));
        Draw.circle(g, cx, cy, 26, Draw.alpha(0x0B141A, a));
        if (mc.player != null) {
            RenderSystem.enableBlend();
            g.setColor(1f, 1f, 1f, a);
            var skin = mc.player.getSkinTextureLocation();
            g.blit(skin, cx - 18, cy - 18, 36, 36, 8, 8, 8, 8, 64, 64);   // лицо
            g.blit(skin, cx - 18, cy - 18, 36, 36, 40, 8, 8, 8, 64, 64);  // слой шапки
            g.setColor(1f, 1f, 1f, 1f);
        }

        String name = mc.player != null ? mc.player.getName().getString() : "";
        Draw.textC(g, font, Component.literal(name), cx, cy + 36, 1.0f, Draw.alpha(0xFFFFFF, a));
        int rank = Math.max(0, Math.min(d.rank, Content.RANK_COUNT - 1));
        Draw.textC(g, font, Component.translatable("knyui.ui.rank", Component.translatable("knyui.rank." + rank)),
            cx, cy + 49, 0.8f, Draw.alpha(0xC8D2D8, a));
        g.fill(x + 16, cy + 66, x + SIDEBAR_W - 16, cy + 67, Draw.alpha(0xFFFFFF, 0.12f * a));
        Draw.textC(g, font, Component.translatable("knyui.ui.level", d.level), cx, cy + 74, 0.85f, Draw.alpha(0xFFFFFF, a));
        Draw.textC(g, font, Component.translatable("knyui.ui.close"), cx, y + panel.h() - 16, 0.7f, Draw.alpha(0x7C8A91, a));
    }

    private void renderHeader(GuiGraphics g, KnyData d, int mx, int my, float a) {
        int pts = Math.round(anim.approach("pts", d.points, 12f, d.points));
        Draw.text(g, font, Component.translatable("knyui.ui.points", pts), content.x(), panel.y() + 14, 1.5f, Draw.alpha(0xFFFFFF, a));

        int ty = panel.y() + 44, x = content.x();
        for (int i = 0; i < tabs.size(); i++) {
            Component title = tabs.get(i).title();
            int w = font.width(title);
            Rect head = new Rect(x - 4, ty - 4, w + 8, 18);
            tabHeads[i] = head;
            float h = anim.approach("th" + i, head.in(mx, my) ? 1f : 0f, 14f);
            int color = Draw.lerp(Draw.alpha(0x7C8A91, a), Draw.alpha(0xFFFFFF, a), i == active ? 1f : h * 0.8f);
            Draw.text(g, font, title, x, ty, 1.0f, color);
            if (i == active) {
                float ux = anim.approach("ux", x, 16f, x);
                float uw = anim.approach("uw", w, 16f, w);
                Draw.rrect(g, Math.round(ux), ty + 12, Math.round(uw), 2, 1, Draw.alpha(tabs.get(i).accent(), a));
            }
            x += w + 24;
        }
    }

    private void switchTab(int i) {
        if (i == active) return;
        active = i;
        anim.set("tab", 0f);
        tabs.get(i).onShow();
        Draw.clickSound();
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0) {
            for (int i = 0; i < tabHeads.length; i++) {
                if (tabHeads[i] != null && tabHeads[i].in(mx, my)) { switchTab(i); return true; }
            }
            if (anim.approach("tab", 1f, 9f, 1f) > 0.85f && tabs.get(active).click(mx, my, btn, content)) return true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        tabs.get(active).release();
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (tabs.get(active).drag(mx, my, content)) return true;
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (tabs.get(active).scroll(mx, my, delta, content)) return true;
        return super.mouseScrolled(mx, my, delta);
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (ClientEvents.OPEN_KEY.matches(key, scan)) { onClose(); return true; }
        return super.keyPressed(key, scan, mods);
    }
}
