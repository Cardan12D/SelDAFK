package dev.knyui;

import dev.knyui.net.KnyNet;
import dev.knyui.server.KnyEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import dev.knyui.data.KnyCaps;

@Mod(KnyUi.MODID)
public class KnyUi {
    public static final String MODID = "knyui";

    public KnyUi() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(KnyNet::register));
        bus.addListener(KnyCaps::register);
        MinecraftForge.EVENT_BUS.register(KnyEvents.class);
    }
}
