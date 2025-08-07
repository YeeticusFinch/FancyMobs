package com.lerdorf.fancymobs;

import org.bukkit.World;

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
}
