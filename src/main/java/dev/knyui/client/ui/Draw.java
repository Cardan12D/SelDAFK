package dev.knyui.client.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.knyui.KnyUi;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

/** Мини-библиотека рисования: скруглённые прямоугольники, круги, градиенты, текст с масштабом. */
public final class Draw {
    private Draw() {}

    private static final Map<String, ResourceLocation> TEX = new HashMap<>();

    public static ResourceLocation tex(String name) {
        return TEX.computeIfAbsent(name, n -> new ResourceLocation(KnyUi.MODID, "textures/gui/" + n + ".png"));
    }

    // ---------- цвета ----------
    public static int alpha(int rgb, float a) {
        int al = Math.max(0, Math.min(255, Math.round(a * 255f)));
        return (al << 24) | (rgb & 0xFFFFFF);
    }

    public static int mulA(int argb, float a) { return alpha(argb, ((argb >>> 24) / 255f) * a); }

    public static int lerp(int c1, int c2, float t) {
        t = Anim.clamp(t, 0f, 1f);
        int a = (int) (((c1 >>> 24) & 255) + (((c2 >>> 24) & 255) - ((c1 >>> 24) & 255)) * t);
        int r = (int) (((c1 >> 16) & 255) + (((c2 >> 16) & 255) - ((c1 >> 16) & 255)) * t);
        int g = (int) (((c1 >> 8) & 255) + (((c2 >> 8) & 255) - ((c1 >> 8) & 255)) * t);
        int b = (int) ((c1 & 255) + ((c2 & 255) - (c1 & 255)) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // ---------- фигуры ----------
    public static void rrect(GuiGraphics g, int x, int y, int w, int h, int r, int c) {
        r = Math.min(r, Math.min(w, h) / 2);
        if (r <= 0) { g.fill(x, y, x + w, y + h, c); return; }
        g.fill(x, y + r, x + w, y + h - r, c);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(r * r - dy * dy));
            g.fill(x + inset, y + i, x + w - inset, y + i + 1, c);
            g.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, c);
        }
    }

    public static void circle(GuiGraphics g, int cx, int cy, int r, int c) {
        for (int dy = -r; dy <= r; dy++) {
            int hw = (int) Math.round(Math.sqrt((double) r * r - (double) dy * dy));
            g.fill(cx - hw, cy + dy, cx + hw, cy + dy + 1, c);
        }
    }

    public static void circleGradient(GuiGraphics g, int cx, int cy, int r, int top, int bottom) {
        for (int dy = -r; dy <= r; dy++) {
            int hw = (int) Math.round(Math.sqrt((double) r * r - (double) dy * dy));
            g.fill(cx - hw, cy + dy, cx + hw, cy + dy + 1, lerp(top, bottom, (dy + r) / (2f * r)));
        }
    }

    /** Мягкое свечение: несколько колец с малой прозрачностью. */
    public static void glow(GuiGraphics g, int cx, int cy, int r, int rgb, float a) {
        for (int i = 0; i < 4; i++) circle(g, cx, cy, r - i * (r / 5), alpha(rgb, a / 4f));
    }

    // ---------- текстуры ----------
    public static void icon(GuiGraphics g, String name, int x, int y, int size, float a) {
        RenderSystem.enableBlend();
        g.setColor(1f, 1f, 1f, Anim.clamp(a, 0f, 1f));
        g.blit(tex(name), x, y, size, size, 0, 0, 64, 64, 64, 64);
        g.setColor(1f, 1f, 1f, 1f);
    }

    // ---------- текст ----------
    public static void text(GuiGraphics g, Font f, Component c, float x, float y, float scale, int color) {
        if ((color >>> 24) < 8) return;
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(f, c, 0, 0, color, false);
        g.pose().popPose();
    }

    public static void textC(GuiGraphics g, Font f, Component c, float cx, float y, float scale, int color) {
        if ((color >>> 24) < 8) return;
        g.pose().pushPose();
        g.pose().translate(cx, y, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(f, c, -f.width(c) / 2, 0, color, false);
        g.pose().popPose();
    }

    /** Перенос по словам, центрирование. Возвращает y после последней строки. */
    public static float wrapC(GuiGraphics g, Font f, Component c, float cx, float y, float maxW, float scale, int color) {
        List<FormattedCharSequence> lines = f.split(c, Math.max(10, (int) (maxW / scale)));
        for (FormattedCharSequence line : lines) {
            if ((color >>> 24) >= 8) {
                g.pose().pushPose();
                g.pose().translate(cx, y, 0);
                g.pose().scale(scale, scale, 1f);
                g.drawString(f, line, -f.width(line) / 2, 0, color, false);
                g.pose().popPose();
            }
            y += 9.5f * scale;
        }
        return y;
    }

    public static void clickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
