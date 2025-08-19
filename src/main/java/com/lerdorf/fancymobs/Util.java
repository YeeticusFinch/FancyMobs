package com.lerdorf.fancymobs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import net.kyori.adventure.sound.Sound;

public class Util {

	public static String getDimension(World world) {
		World.Environment env = world.getEnvironment();

		switch (env) {
		    case NORMAL:
		        // Overworld
		    	return "minecraft:overworld";
		    case NETHER:
		        // Nether
		    	return "minecraft:the_nether";
		    case THE_END:
		        // End
		    	return "minecraft:the_end";
		    default:
		    	return world.toString().strip().toLowerCase();
		}
	}
	
	public static void playSound(Sound sound, Location loc) {
		try {
		if (sound != null)
			if (loc.getNearbyPlayers(30*sound.volume()).size() > 0)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in " + Util.getDimension(loc.getWorld()) + " positioned " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + " run playsound " + sound.name().asString().toLowerCase().trim() + " neutral @a ~ ~ ~ " + sound.volume() + " " + sound.pitch());
	
		} catch (Exception e) {
			Bukkit.getLogger().warning(e.getLocalizedMessage());
		}
	}
	

	public static void setEquippable(ItemStack e, String model) {
		ItemMeta meta = e.getItemMeta();
		if (meta != null) {
			if (meta.hasEquippable()) {
				EquippableComponent equippable = meta.getEquippable();
				String ogModel = equippable.getModel().toString();
				if (ogModel.toLowerCase().contains(model)) return;
				equippable.setModel(NamespacedKey.fromString(model));
				meta.setEquippable(equippable);
				e.setItemMeta(meta);
				//FancyMagic.plugin.getLogger().info("Adding invisibility to " + meta.getItemName());
			} else {
				NBT.modifyComponents(e, components -> {
	                ReadWriteNBT equippable = components.getOrCreateCompound("minecraft:equippable");
	                
	                // Set the asset_id
	                equippable.setString("asset_id", model);
	                
	                // If this is a new equippable component, set defaults based on item type
	                if (!equippable.hasTag("slot")) {
	                    setDefaultEquippableData(equippable, e.getType());
	                }
	                
	            });
				//meta = e.getItemMeta();
				//e.setItemMeta(meta);
				//FancyMagic.plugin.getLogger().info("Adding invisibility to " + meta.getItemName() + " using NBTs");
			}
		}
		
	}

	private static void setDefaultEquippableData(ReadWriteNBT equippable, Material material) {
	    String materialName = material.name().toLowerCase();
	    
	    // Set slot based on material type
	    if (materialName.contains("helmet") || materialName.contains("cap")) {
	        equippable.setString("slot", "head");
	    } else if (materialName.contains("chestplate") || materialName.contains("tunic")) {
	        equippable.setString("slot", "chest");
	    } else if (materialName.contains("leggings") || materialName.contains("pants")) {
	        equippable.setString("slot", "legs");
	    } else if (materialName.contains("boots") || materialName.contains("shoes")) {
	        equippable.setString("slot", "feet");
	    } else {
	        // Default to chest if we can't determine
	        equippable.setString("slot", "chest");
	    }
	    
	    // Set equip sound based on material
	    String equipSound = getEquipSoundString(material);
	    if (equipSound != null) {
	        equippable.setString("equip_sound", equipSound);
	    }
	    
	    // Set default properties
	    equippable.setBoolean("dispensable", true);
	    equippable.setBoolean("swappable", true);
	    equippable.setBoolean("damage_on_hurt", true);
	}
	
	private static String getEquipSoundString(Material material) {
	    String name = material.name().toLowerCase();
	    
	    if (name.contains("leather")) {
	        return "minecraft:item.armor.equip_leather";
	    } else if (name.contains("chain")) {
	        return "minecraft:item.armor.equip_chain";
	    } else if (name.contains("iron")) {
	        return "minecraft:item.armor.equip_iron";
	    } else if (name.contains("diamond")) {
	        return "minecraft:item.armor.equip_diamond";
	    } else if (name.contains("gold")) {
	        return "minecraft:item.armor.equip_gold";
	    } else if (name.contains("netherite")) {
	        return "minecraft:item.armor.equip_netherite";
	    }
	    
	    return "minecraft:item.armor.equip_generic";
	}
	
	public static boolean modelContains(ItemStack item, String sub) {
		if (item == null)
			return false;
		String model = getModel(item);
		if (model != null && model.toLowerCase().contains(sub.toLowerCase()))
			return true;
		return false;
	}
	
	public static String getModel(ItemStack item) {
		if (!item.hasItemMeta())
			return null;
		ItemMeta meta = item.getItemMeta();
		if (!meta.hasEquippable()) {
			if (meta.getItemModel() == null)
				return null;
			return meta.getItemModel().toString();
		}
		EquippableComponent equippable = meta.getEquippable();
		String currentModel = equippable.getModel().toString();
		return currentModel;
	}
}
