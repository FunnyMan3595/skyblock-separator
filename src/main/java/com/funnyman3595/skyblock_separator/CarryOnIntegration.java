package com.funnyman3595.skyblock_separator;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

public class CarryOnIntegration {
    public static CompoundTag saveFor(Player player) {
    	return CarryOnDataManager.getCarryData(player).getNbt();
    }
    
    public static void loadFor(Player player, CompoundTag saveData) {
    	CarryOnDataManager.setCarryData(player, new CarryOnData(saveData));
    }
}
