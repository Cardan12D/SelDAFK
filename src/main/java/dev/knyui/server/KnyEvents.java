package dev.knyui.server;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.knyui.data.Content;
import dev.knyui.data.KnyCaps;
import dev.knyui.data.KnyData;
import dev.knyui.net.KnyNet;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Регистрируется в KnyUi через MinecraftForge.EVENT_BUS.register(KnyEvents.class). */
public final class KnyEvents {
    private KnyEvents() {}

    @SubscribeEvent
    public static void attach(AttachCapabilitiesEvent<Entity> e) {
        if (e.getObject() instanceof Player && !e.getObject().getCapability(KnyCaps.CAP).isPresent()) {
            e.addCapability(KnyCaps.ID, new KnyCaps.Provider());
        }
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone e) {
        e.getOriginal().reviveCaps();
        e.getOriginal().getCapability(KnyCaps.CAP).ifPresent(old ->
            e.getEntity().getCapability(KnyCaps.CAP).ifPresent(n -> n.copyFrom(old)));
        e.getOriginal().invalidateCaps();
    }

    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e) { sync(e.getEntity()); }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e) { sync(e.getEntity()); }
    @SubscribeEvent public static void dim(PlayerEvent.PlayerChangedDimensionEvent e) { sync(e.getEntity()); }

    private static void sync(Player p) {
        if (p instanceof ServerPlayer sp) KnyNet.syncAndApply(sp);
    }

    // ---- отладочные команды (нужен оп): /knyui points 20, level 300, rank 3, train water.2, reset ----
    @SubscribeEvent
    public static void commands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("knyui").requires(s -> s.hasPermission(2))
            .then(Commands.literal("points").then(Commands.argument("n", IntegerArgumentType.integer(0, 9999))
                .executes(c -> edit(c, d -> d.points = IntegerArgumentType.getInteger(c, "n")))))
            .then(Commands.literal("level").then(Commands.argument("n", IntegerArgumentType.integer(1, 9999))
                .executes(c -> edit(c, d -> d.level = IntegerArgumentType.getInteger(c, "n")))))
            .then(Commands.literal("rank").then(Commands.argument("n", IntegerArgumentType.integer(0, Content.RANK_COUNT - 1))
                .executes(c -> edit(c, d -> d.rank = IntegerArgumentType.getInteger(c, "n")))))
            .then(Commands.literal("train").then(Commands.argument("style", StringArgumentType.word())
                .executes(c -> edit(c, d -> d.trained.add(StringArgumentType.getString(c, "style"))))))
            .then(Commands.literal("reset").executes(c -> edit(c, d -> d.copyFrom(new KnyData())))));
    }

    private static int edit(CommandContext<CommandSourceStack> c, Consumer<KnyData> f) throws CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        p.getCapability(KnyCaps.CAP).ifPresent(f);
        KnyNet.syncAndApply(p);
        c.getSource().sendSuccess(() -> Component.literal("OK"), false);
        return 1;
    }
}
