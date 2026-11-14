package dev.knyui.net;

import dev.knyui.data.KnyData;
import dev.knyui.server.ServerLogic;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public final class Packets {
    private Packets() {}

    /** Сервер -> клиент: полные данные игрока. */
    public record Sync(KnyData data) {
        public static void encode(Sync m, FriendlyByteBuf b) { m.data.write(b); }
        public static Sync decode(FriendlyByteBuf b) { return new Sync(KnyData.read(b)); }
        public static void handle(Sync m, Supplier<NetworkEvent.Context> c) {
            c.get().enqueueWork(() -> ClientState.data = m.data);
            c.get().setPacketHandled(true);
        }
    }

    /** Клиент -> сервер: +1 / -1 к характеристике (0 сила, 1 ловкость, 2 выносливость). */
    public record SpendStat(int stat, int delta) {
        public static void encode(SpendStat m, FriendlyByteBuf b) { b.writeVarInt(m.stat); b.writeVarInt(m.delta); }
        public static SpendStat decode(FriendlyByteBuf b) { return new SpendStat(b.readVarInt(), b.readVarInt()); }
        public static void handle(SpendStat m, Supplier<NetworkEvent.Context> c) {
            ServerPlayer p = c.get().getSender();
            c.get().enqueueWork(() -> { if (p != null) ServerLogic.spendStat(p, m.stat, m.delta); });
            c.get().setPacketHandled(true);
        }
    }

    /** Клиент -> сервер: разблокировать стиль дыхания. */
    public record UnlockStyle(String id) {
        public static void encode(UnlockStyle m, FriendlyByteBuf b) { b.writeUtf(m.id); }
        public static UnlockStyle decode(FriendlyByteBuf b) { return new UnlockStyle(b.readUtf(64)); }
        public static void handle(UnlockStyle m, Supplier<NetworkEvent.Context> c) {
            ServerPlayer p = c.get().getSender();
            c.get().enqueueWork(() -> { if (p != null) ServerLogic.unlockStyle(p, m.id); });
            c.get().setPacketHandled(true);
        }
    }

    /** Клиент -> сервер: активировать пассивную способность (метка, концентрация). */
    public record ActivatePassive(String id) {
        public static void encode(ActivatePassive m, FriendlyByteBuf b) { b.writeUtf(m.id); }
        public static ActivatePassive decode(FriendlyByteBuf b) { return new ActivatePassive(b.readUtf(64)); }
        public static void handle(ActivatePassive m, Supplier<NetworkEvent.Context> c) {
            ServerPlayer p = c.get().getSender();
            c.get().enqueueWork(() -> { if (p != null) ServerLogic.activatePassive(p, m.id); });
            c.get().setPacketHandled(true);
        }
    }
}
