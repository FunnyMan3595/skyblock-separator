package com.funnyman3595.skyblock_separator;

import com.mojang.logging.LogUtils;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SkyblockSeparator.MODID)
public class SkyblockSeparator
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "skyblock_separator";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public SkyblockSeparator()
    {
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onTick);
        MinecraftForge.EVENT_BUS.addListener(this::onTeleport);
    }

    private void registerCommands(final RegisterCommandsEvent event)
    {
    }

    private void onTick(final TickEvent.PlayerTickEvent event)
    {
    }

    private void onTeleport(final EntityTeleportEvent event)
    {
    }
}
