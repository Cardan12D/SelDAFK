package dev.knyui.data;

import java.util.List;

/**
 * Весь игровой контент UI в одном месте. Названия/описания берутся из lang-файлов по id:
 *  knyui.breathing.<id>, knyui.style.<styleId>.name / .desc, knyui.passive.<id>.name / .desc, knyui.rank.<n>
 * Чтобы добавить новое дыхание/пассивку — добавьте запись сюда + строки в lang + иконку
 * textures/gui/breath_<id>.png или passive_<id>.png.
 */
public final class Content {
    private Content() {}

    public record Style(String id, int cost, boolean needsTraining) {}
    public record Breathing(String id, int color, List<Style> styles) {}
    public record Passive(String id, int color, int minutes, int minRank) {}

    public static final int RANK_COUNT = 12;
    public static final int BASE_STAT = 10;
    public static final int MAX_STAT = 100;

    public static final List<Breathing> BREATHINGS = List.of(
        new Breathing("water", 0xFF4FC3F7, List.of(new Style("water.1", 1, false), new Style("water.2", 1, true))),
        new Breathing("flame", 0xFFFF7043, List.of(new Style("flame.1", 1, false), new Style("flame.2", 1, true))),
        new Breathing("thunder", 0xFFFFEE58, List.of(new Style("thunder.1", 1, false), new Style("thunder.2", 1, true)))
    );

    public static final List<Passive> PASSIVES = List.of(
        new Passive("slayer_mark", 0xFFE53935, 30, 0),
        new Passive("constant_focus", 0xFF1E6BFF, 30, 2)
    );

    public static Style findStyle(String id) {
        for (Breathing b : BREATHINGS) for (Style s : b.styles()) if (s.id().equals(id)) return s;
        return null;
    }

    public static Passive findPassive(String id) {
        for (Passive p : PASSIVES) if (p.id().equals(id)) return p;
        return null;
    }
}
