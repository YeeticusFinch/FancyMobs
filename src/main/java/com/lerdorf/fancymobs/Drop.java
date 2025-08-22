package com.lerdorf.fancymobs;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;

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
		World world = Bukkit.getWorlds().get(0);
		Location loc = world.getSpawnLocation().clone();
		loc.setY(world.getMaxHeight() - 2);

		// Place a temporary barrel
		Block block = loc.getBlock();
		block.setType(Material.BARREL);

		// Get a console sender (or your plugin command sender)
		ConsoleCommandSender sender = Bukkit.getConsoleSender();

		// Build the loot command
		String lootTable = loot; // your loot table
		String cmd = "loot insert " + block.getX() + " " + block.getY() + " " + block.getZ() + " loot " 
		             + lootTable;

		// Dispatch the command
		Bukkit.dispatchCommand(sender, cmd);

		// Access the inventory immediately
		if (block.getState() instanceof Container container) {
		    Inventory inv = container.getInventory();
		    ItemStack firstItem = null;
		    for (ItemStack item : inv.getContents()) {
		        if (item != null && item.getType() != Material.AIR) {
		            firstItem = item.clone();
		            break;
		        }
		    }

		    container.getInventory().clear();
		    
		    // Schedule cleanup
		    Bukkit.getScheduler().runTaskLater(FancyMobs.plugin, () -> block.setType(Material.AIR), 5L);

		    return firstItem;
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
		for (int i = minCount; i <= (maxCount > 1 ? (maxCount + looting) : maxCount); i++) {
			if (Math.random() > probability + 0.01f*looting) {
				amount = i;
				break;
			}
		}
		//Bukkit.broadcastMessage("Min = " + minCount + ", Max = " + maxCount + ", Probability = " + probability + ", Rolled = " + amount);
		if (amount == 0) return null;
		else {
			ItemStack result = item.clone();
			result.setAmount(amount);
			return result;
		}
	}
}
