package com.lerdorf.fancymobs;

import java.util.Collection;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

public class Drop {
	public ItemStack item;
	public int minCount = 0;
	public int maxCount = 1;
	public float probability = 0.5f;
	
	public Drop(ItemStack item, float probability, int minCount, int maxCount) {
		this.item = item;
		this.probability = probability;
		this.minCount = minCount;
		this.maxCount = maxCount;
	}
	
	public static ItemStack getFromLoot(String loot) {
		NamespacedKey key = NamespacedKey.fromString(loot);
		LootTable table = Bukkit.getLootTable(key);
		if (table != null) {
		    // You just need a world & location for the context
		    World world = Bukkit.getWorlds().getFirst(); // or any valid world
		    LootContext context = new LootContext.Builder(new Location(world, 0, 0, 0))
		        .build();

		    Collection<ItemStack> items = table.populateLoot(new Random(), context);

		    ItemStack first = items.stream().findFirst().orElse(null);
		    if (first != null) {
		        return first;
		    }
		}
		return null;
	}
	
	public Drop(String loot, float probability, int minCount, int maxCount) {
		this.probability = probability;
		this.minCount = minCount;
		this.maxCount = maxCount;
		
		item = getFromLoot(loot);
		

	}
	
	public ItemStack getDrop(int looting) {
		int amount = minCount;
		for (int i = minCount; i <= (maxCount > 1 ? maxCount + looting : maxCount); i++) {
			if (Math.random() > probability + 0.01f*looting) {
				amount = i;
				break;
			}
		}
		if (amount == 0) return null;
		else {
			ItemStack result = item.clone();
			result.setAmount(amount);
			return result;
		}
	}
}
