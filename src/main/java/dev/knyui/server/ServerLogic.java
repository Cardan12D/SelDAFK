package dev.knyui.server;

import dev.knyui.data.Content;
import dev.knyui.data.KnyCaps;
import dev.knyui.net.KnyNet;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Вся проверка действий игрока — на сервере. Клиент только просит. */
public final class ServerLogic {
    private ServerLogic() {}

    public static void spendStat(ServerPlayer p, int stat, int delta) {
        if (stat < 0 || stat > 2 || (delta != 1 && delta != -1)) return;
        p.getCapability(KnyCaps.CAP).ifPresent(d -> {
            int v = d.getStat(stat);
            if (delta > 0) {
                if (d.points <= 0 || v >= Content.MAX_STAT) return;
                d.points--; d.setStat(stat, v + 1);
            } else {
                if (v <= Content.BASE_STAT) return;
                d.points++; d.setStat(stat, v - 1);
            }
            KnyNet.syncAndApply(p);
        });
    }

    public static void unlockStyle(ServerPlayer p, String id) {
        Content.Style s = Content.findStyle(id);
        if (s == null) return;
        p.getCapability(KnyCaps.CAP).ifPresent(d -> {
            if (d.unlocked.contains(id)) return;
            if (s.needsTraining() && !d.trained.contains(id)) {
                p.displayClientMessage(Component.translatable("knyui.msg.need_training"), true);
                return;
            }
            if (d.points < s.cost()) {
                p.displayClientMessage(Component.translatable("knyui.msg.no_points"), true);
                return;
            }
            d.points -= s.cost();
            d.unlocked.add(id);
            KnyNet.syncAndApply(p);
        });
    }

    public static void activatePassive(ServerPlayer p, String id) {
        Content.Passive ps = Content.findPassive(id);
        if (ps == null) return;
        p.getCapability(KnyCaps.CAP).ifPresent(d -> {
            if (d.rank < ps.minRank()) {
                p.displayClientMessage(Component.translatable("knyui.msg.low_rank"), true);
                return;
            }
            int ticks = ps.minutes() * 60 * 20;
            // Заглушки эффектов — замените на свою логику (или вызов процедур оригинального мода).
            switch (id) {
                case "slayer_mark" -> {
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, ticks, 3, false, false, true));
                    p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ticks, 1, false, false, true));
                    p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, ticks, 1, false, false, true));
                    p.heal(p.getMaxHealth());
                }
                case "constant_focus" -> {
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, ticks, 1, false, false, true));
                    p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, ticks, 0, false, false, true));
                    p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, ticks, 1, false, false, true));
                }
                default -> { return; }
            }
            p.displayClientMessage(Component.translatable("knyui.msg.activated",
                Component.translatable("knyui.passive." + id + ".name")), true);
        });
    }
}
