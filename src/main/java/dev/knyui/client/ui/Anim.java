package dev.knyui.client.ui;

import java.util.HashMap;
import java.util.Map;

/** Простая система анимаций: плавное приближение значений к цели (не зависит от FPS). */
public final class Anim {
    private final Map<String, Float> values = new HashMap<>();
    private long last = System.nanoTime();
    public float dt = 0.016f;

    /** Вызывать один раз за кадр в начале render(). */
    public void tick() {
        long n = System.nanoTime();
        dt = Math.min(0.1f, (n - last) / 1e9f);
        last = n;
    }

    public float approach(String key, float target, float speed) { return approach(key, target, speed, 0f); }

    public float approach(String key, float target, float speed, float init) {
        float cur = values.getOrDefault(key, init);
        cur += (target - cur) * Math.min(1f, dt * speed);
        if (Math.abs(target - cur) < 0.001f) cur = target;
        values.put(key, cur);
        return cur;
    }

    public void set(String key, float v) { values.put(key, v); }

    public static float clamp(float x, float a, float b) { return Math.max(a, Math.min(b, x)); }
    public static float easeOut(float t) { float u = 1f - t; return 1f - u * u * u; }
}
