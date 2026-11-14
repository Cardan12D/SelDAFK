package dev.knyui.client;

import dev.knyui.KnyUi;
import dev.knyui.client.ui.KnyScreen;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/** Forge-bus события клиента: открытие меню по клавише и красивый тултип с редкостью. */
@Mod.EventBusSubscriber(modid = KnyUi.MODID, value = Dist.CLIENT)
public final class ClientForgeEvents {
    private ClientForgeEvents() {}

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        while (ClientEvents.OPEN_KEY.consumeClick()) {
            if (mc.player != null && mc.screen == null) mc.setScreen(new KnyScreen());
        }
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent e) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(e.getItemStack().getItem());
        Rarity r = Rarity.of(id);
        if (r == null) return;
        List<Component> tt = e.getToolTip();
        int at = Math.min(1, tt.size());
        tt.add(at, Component.translatable("knyui.tooltip.demon_slayers_only").withStyle(ChatFormatting.GREEN));
        tt.add(at + 1, Component.translatable("knyui.tooltip.rarity").withStyle(ChatFormatting.GRAY)
            .append(Component.translatable("knyui.rarity." + r.key).withStyle(s -> s.withColor(r.color & 0xFFFFFF))));
    }

    @SubscribeEvent
    public static void tooltipColors(RenderTooltipEvent.Color e) {
        Rarity r = Rarity.of(ForgeRegistries.ITEMS.getKey(e.getItemStack().getItem()));
        if (r == null) return;
        int rgb = r.color & 0xFFFFFF;
        e.setBorderStart(0xFF000000 | rgb);
        e.setBorderEnd(0xAA000000 | rgb);
        e.setBackgroundStart(0xF0070D12);
        e.setBackgroundEnd(0xF0070D12);
    }
}
