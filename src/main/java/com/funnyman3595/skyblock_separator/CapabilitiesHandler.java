package com.funnyman3595.skyblock_separator;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

@Mod.EventBusSubscriber(modid=SkyblockSeparator.MODID)
public class CapabilitiesHandler {
	private static final ResourceLocation SEPARATED_INVENTORY = SkyblockSeparator.resourceLocation("separated_inventory");
	
	@Mod.EventBusSubscriber(modid=SkyblockSeparator.MODID, bus=MOD)
	private static final class Setup {
		@SubscribeEvent
		public static void registerCapabilities(RegisterCapabilitiesEvent event) {
			event.register(SeparatedInventory.class);
		}
	}
	
	@SubscribeEvent
	public static void attachPlayerCapability(AttachCapabilitiesEvent<Entity> event) {
		if (!(event.getObject() instanceof Player)) return;
		
		event.addCapability(SEPARATED_INVENTORY, new SeparatedInventory());
	}
	
	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event) {
		event.getOriginal().reviveCaps();
		SeparatedInventory old = SeparatedInventory.get(event.getOriginal());
		SeparatedInventory clone = SeparatedInventory.get(event.getEntity());
		clone.deserializeNBT(old.serializeNBT());
		event.getOriginal().invalidateCaps();
	}
}
