package com.funnyman3595.skyblock_separator;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;

import de.melanx.skyblockbuilder.config.ConfigHandler;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.data.Team;
import de.melanx.skyblockbuilder.world.IslandPos;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.TickEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
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
    
    public static Capability<SeparatedInventory> separatedInventory = CapabilityManager.get(new CapabilityToken<>() { });

    public SkyblockSeparator()
    {
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onTick);
    }

	public static ResourceLocation resourceLocation(String path) {
		return new ResourceLocation(MODID, path);
	}

    private void registerCommands(final RegisterCommandsEvent event)
    {
    	event.getDispatcher().register(LiteralArgumentBuilder.<CommandSourceStack>literal("atisland")
    		.executes(context -> atisland(context.getSource().getPlayerOrException()) ? 1 : 0));
    }
    
    public boolean atisland(Player player) {
    	if (player.getLevel().dimension() != Level.OVERWORLD) {
    		return false;
    	}
    	
    	IslandPos island = getIsland(player.getLevel(), player.position());
    	SkyblockSavedData skyblock = SkyblockSavedData.get(player.getLevel());
    	Team team = skyblock.getTeamFromPlayer(player);
    	
    	LOGGER.info("Player " + player.getScoreboardName() + " currently on island " + island.toTag().toString() + ", home island is " + team.getIsland().toTag().toString());
    	
    	return team.getIsland().equals(island);
    }
    
    public IslandPos getIsland(Level level, Vec3 pos) {
    	if (level.dimension() != Level.OVERWORLD) {
    		return null;
    	}
    	double x = pos.x();
    	double z = pos.z();
    	int islandDistance = ConfigHandler.World.islandDistance;
    	double islandX = (x / islandDistance) + 0.5;
    	double islandZ = (z / islandDistance) + 0.5;
    	return new IslandPos(level, (int) islandX, (int) islandZ);
    }
    
    public double realMod(double num, double modulus) {
    	double naiveMod = num % modulus;
    	if (naiveMod < 0) {
    		return naiveMod + modulus;
    	}
    	return naiveMod;
    }

    private void onTick(final TickEvent.PlayerTickEvent event)
    {
    	if (event.player.getLevel().dimension() != Level.OVERWORLD) {
    		return;
    	}
    	
    	// Avoid crashing if the capability hasn't been added yet.
    	// (e.g. on death)
    	try {
    		SeparatedInventory.get(event.player);
    	} catch (SeparatedInventory.SeparatedInventoryNotFoundException e) {
    		return;
    	}
    	
    	int islandDistance = ConfigHandler.World.islandDistance;
    	double islandX = realMod((event.player.getX() / islandDistance) + 0.5, 1.0);
    	double islandZ = realMod((event.player.getZ() / islandDistance) + 0.5, 1.0);
    	double boundedIslandX = Math.min(Math.max(islandX, 0.01), 0.99);
    	double boundedIslandZ = Math.min(Math.max(islandZ, 0.01), 0.99);
    	if (islandX != boundedIslandX || islandZ != boundedIslandZ) {
    		Vec3 currentPos = event.player.position();
    		double boundedX = currentPos.x + (boundedIslandX - islandX) * islandDistance;
    		double boundedZ = currentPos.z + (boundedIslandZ - islandZ) * islandDistance;
    		event.player.setPos(new Vec3(boundedX, currentPos.y, boundedZ));
    		event.player.setDeltaMovement(0, 0, 0);
    		event.player.sendSystemMessage(Component.literal("You have reached the island border."));
    	}
    	
    	IslandPos newIsland = getIsland(event.player.getLevel(), event.player.getPosition(0));
    	SeparatedInventory separatedInventory = SeparatedInventory.get(event.player);
    	if (separatedInventory.lastIsland == null) {
    		separatedInventory.lastIsland = newIsland;
    		return;
    	} else if (separatedInventory.lastIsland.equals(newIsland)) {
    		return;
    	}
    	
    	LOGGER.info("Swapping player " + event.player.getScoreboardName() + " from island " + separatedInventory.lastIsland.toTag().toString() + " to " + newIsland.toTag().toString());
    	SeparatedInventory.swapIsland(event.player, separatedInventory.lastIsland, newIsland);
    }
}
