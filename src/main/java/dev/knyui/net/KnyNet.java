package dev.knyui.net;

import dev.knyui.KnyUi;
import dev.knyui.data.KnyCaps;
import dev.knyui.server.StatEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class KnyNet {
    private KnyNet() {}
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(KnyUi.MODID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, Packets.Sync.class, Packets.Sync::encode, Packets.Sync::decode, Packets.Sync::handle);
        CHANNEL.registerMessage(id++, Packets.SpendStat.class, Packets.SpendStat::encode, Packets.SpendStat::decode, Packets.SpendStat::handle);
        CHANNEL.registerMessage(id++, Packets.UnlockStyle.class, Packets.UnlockStyle::encode, Packets.UnlockStyle::decode, Packets.UnlockStyle::handle);
        CHANNEL.registerMessage(id++, Packets.ActivatePassive.class, Packets.ActivatePassive::encode, Packets.ActivatePassive::decode, Packets.ActivatePassive::handle);
    }

    /** Применить статы к атрибутам и отправить клиенту актуальные данные. */
    public static void syncAndApply(ServerPlayer p) {
        p.getCapability(KnyCaps.CAP).ifPresent(d -> {
            StatEffects.apply(p, d);
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), new Packets.Sync(d));
        });
    }
}
