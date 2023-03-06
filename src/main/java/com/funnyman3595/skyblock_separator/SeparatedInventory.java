package com.funnyman3595.skyblock_separator;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import de.melanx.skyblockbuilder.world.IslandPos;

public class SeparatedInventory implements ICapabilitySerializable<CompoundTag> {
	public static final String MAIN_NBT_KEY = "separated_inventory";
	public static final String LAST_ISLAND_NBT_KEY = "last_island";
	public Map<IslandPos, ListTag> inventories = new HashMap<IslandPos, ListTag>();
	public Map<IslandPos, ListTag> enderInventories = new HashMap<IslandPos, ListTag>();
	public IslandPos lastIsland = null;
	
	public static SeparatedInventory get(Player player) {
		return (SeparatedInventory) player.getCapability(SkyblockSeparator.separatedInventory)
			.orElseThrow(SeparatedInventoryNotFoundException::new);
	}
	
	public static void swapIsland(Player player, IslandPos from, IslandPos to) {
		if (from == null || !from.equals(get(player).lastIsland)) {
			// Something has gone badly wrong here, so we do nothing, and hope it gets better.
		    return;
		}
		saveIsland(player, from);
		loadIsland(player, to);
		get(player).lastIsland = to;
	}
	
	public static void saveIsland(Player player, IslandPos island) {
		ListTag inventory = new ListTag();
		player.getInventory().save(inventory);
		ListTag enderInventory = player.getEnderChestInventory().createTag();
		get(player).inventories.put(island, inventory);
		get(player).enderInventories.put(island, enderInventory);
	}
	
	public static void loadIsland(Player player, IslandPos island) {
		player.getInventory().load(get(player).inventories.getOrDefault(island, new ListTag()));
		player.getEnderChestInventory().fromTag(get(player).enderInventories.getOrDefault(island, new ListTag()));
	}
	
	@Override
	public CompoundTag serializeNBT() {
		var tag = new CompoundTag();
		var list = new ListTag();
		inventories.forEach((island, inventory) -> {
			var separatedInventory = new CompoundTag();
			separatedInventory.put("island", island.toTag());
			separatedInventory.put("inventory", inventory);
			separatedInventory.put("ender_inventory", enderInventories.get(island));
			list.add(separatedInventory);
		});
		tag.put(MAIN_NBT_KEY, list);
		if (lastIsland != null) {
			tag.put(LAST_ISLAND_NBT_KEY, lastIsland.toTag());
		}
		
		return tag;
	}
	
	@Override
	public void deserializeNBT(CompoundTag tag) {
		var list = tag.getList(MAIN_NBT_KEY, Tag.TAG_COMPOUND);
		inventories.clear();
		enderInventories.clear();
		for (Tag rawTag : list) {
			CompoundTag inventoryTag = (CompoundTag) rawTag;
			inventories.put(IslandPos.fromTag(inventoryTag.getCompound("island")),
					(ListTag) inventoryTag.getList("inventory", Tag.TAG_COMPOUND));
			enderInventories.put(IslandPos.fromTag(inventoryTag.getCompound("island")),
					(ListTag) inventoryTag.getList("ender_inventory", Tag.TAG_COMPOUND));
		}
		CompoundTag lastIslandTag = tag.getCompound(LAST_ISLAND_NBT_KEY);
		if (!lastIslandTag.isEmpty()) {
			lastIsland = IslandPos.fromTag(lastIslandTag);
		}
	}

	private final LazyOptional<SeparatedInventory> capabilityOptional = LazyOptional.of(() -> this);
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
		return capability == SkyblockSeparator.separatedInventory ? capabilityOptional.cast() : LazyOptional.empty();
	}
	
	public static class SeparatedInventoryNotFoundException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public SeparatedInventoryNotFoundException() {
			super("No separated inventory found on player.");
		}
	}
}
