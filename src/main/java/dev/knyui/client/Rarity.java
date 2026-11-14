package dev.knyui.client;

import java.util.Set;
import net.minecraft.resources.ResourceLocation;

/** Редкость мечей оригинального мода (id вида kimetsunoyaiba:nichirinsword_*). Правьте списки под себя. */
public enum Rarity {
    COMMON("common", 0xFFB0B0B0),
    RARE("rare", 0xFF4FC3F7),
    EPIC("epic", 0xFFB36BFF),
    LEGENDARY("legendary", 0xFFFFB300);

    public final String key;
    public final int color;

    Rarity(String key, int color) { this.key = key; this.color = color; }

    private static final Set<String> LEGENDARY_SET = Set.of(
        "yoriichi", "tomioka", "rengoku", "kocho", "kanroji", "iguro", "shinazugawa", "tokito", "uzui",
        "senior_super", "golden", "moon");
    private static final Set<String> EPIC_SET = Set.of("tanjiro", "zenitsu", "inosuke", "genya", "kanae", "kanawo", "kaigaku");
    private static final Set<String> RARE_SET = Set.of("flame", "water", "wind", "thunder", "mist", "cherry_blossom", "black");

    /** null — предмет не меч Нитирин, тултип не трогаем. */
    public static Rarity of(ResourceLocation id) {
        if (id == null || !"kimetsunoyaiba".equals(id.getNamespace())) return null;
        String p = id.getPath();
        if (!p.startsWith("nichirinsword")) return null;
        String name = p.equals("nichirinswordmoon") ? "moon" : p.substring(Math.min(p.length(), "nichirinsword_".length()));
        if (LEGENDARY_SET.contains(name)) return LEGENDARY;
        if (EPIC_SET.contains(name)) return EPIC;
        if (RARE_SET.contains(name)) return RARE;
        return COMMON;
    }
}
