package com.lerdorf.fancymobs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

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
		if (sound != null)
			if (loc.getNearbyPlayers(30*sound.volume()).size() > 0)
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute in " + Util.getDimension(loc.getWorld()) + " positioned " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + " run playsound " + sound.name().asString() + " NEUTRAL @a ~ ~ ~ " + sound.volume() + " " + sound.pitch());
	}
}
