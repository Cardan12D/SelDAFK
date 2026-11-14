package dev.knyui.server;

import dev.knyui.data.Content;
import dev.knyui.data.KnyData;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/** Превращает статы в атрибуты игрока. Формулы — здесь, меняйте под баланс. */
public final class StatEffects {
    private StatEffects() {}

    private static final UUID STR = UUID.fromString("7f1b0c3a-0001-4a7e-9a10-6b6e79756901");
    private static final UUID AGI_SPEED = UUID.fromString("7f1b0c3a-0002-4a7e-9a10-6b6e79756902");
    private static final UUID AGI_ATK = UUID.fromString("7f1b0c3a-0003-4a7e-9a10-6b6e79756903");
    private static final UUID END = UUID.fromString("7f1b0c3a-0004-4a7e-9a10-6b6e79756904");

    public static double damageBonus(int str) { return (str - Content.BASE_STAT) * 0.25; }
    public static double speedPercent(int agi) { return (agi - Content.BASE_STAT) * 1.0; }
    public static double healthBonus(int end) { return (end - Content.BASE_STAT) * 2.0; }

    public static void apply(Player p, KnyData d) {
        set(p, Attributes.ATTACK_DAMAGE, STR, "kny_strength", damageBonus(d.strength), AttributeModifier.Operation.ADDITION);
        set(p, Attributes.MOVEMENT_SPEED, AGI_SPEED, "kny_agility_speed", speedPercent(d.agility) / 100.0, AttributeModifier.Operation.MULTIPLY_BASE);
        set(p, Attributes.ATTACK_SPEED, AGI_ATK, "kny_agility_attack", (d.agility - Content.BASE_STAT) * 0.05, AttributeModifier.Operation.ADDITION);
        set(p, Attributes.MAX_HEALTH, END, "kny_endurance", healthBonus(d.endurance), AttributeModifier.Operation.ADDITION);
        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
    }

    private static void set(Player p, Attribute a, UUID id, String name, double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = p.getAttribute(a);
        if (inst == null) return;
        inst.removeModifier(id);
        if (amount != 0) inst.addPermanentModifier(new AttributeModifier(id, name, amount, op));
    }
}
