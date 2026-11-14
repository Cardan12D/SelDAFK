package dev.knyui.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.knyui.KnyUi;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Mod-bus события клиента: регистрация клавиши. */
@Mod.EventBusSubscriber(modid = KnyUi.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {}

    public static final KeyMapping OPEN_KEY =
        new KeyMapping("key.knyui.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.knyui");

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) { e.register(OPEN_KEY); }
}
