package com.funnyman3595.skyblock_separator;

import java.util.Map;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public class CuriosIntegration {
    public static ICuriosItemHandler getCuriosHandler(Player player) {
    	return CuriosApi.getCuriosHelper().getCuriosHandler(player).orElse(null);
    }

    public static ListTag saveFor(Player player) {
    	return getCuriosHandler(player).saveInventory(true);
    }
    
    public static void loadFor(Player player, ListTag saveData) {
    	ICuriosItemHandler handler = getCuriosHandler(player);
    	Map<String, ICurioStacksHandler> oldCurios = handler.getCurios();
    	handler.reset();
    	handler.loadInventory(saveData);
    	Map<String, ICurioStacksHandler> newCurios = handler.getCurios();

    	for (ICurioStacksHandler stackHandler : oldCurios.values()) {
    		stackHandler.clearCachedModifiers();
    	}
    	for (ICurioStacksHandler stackHandler : newCurios.values()) {
    		stackHandler.clearCachedModifiers();
    	}
    }
}
