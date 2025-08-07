package com.lerdorf.fancymobs;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;

public class SpawnCondition {
	public static final int onGround = 0;
	public static final int air = 1;
	public static final int onWater = 2;
	public static final int onLava = 3;
	public static final int inWater = 4;
	public static final int inLava = 5;
	public static final int specificBiomes = 6;
	public static final int dark = 7;
	public static final int pitchDark = 8;
	public static final int specificFloorTypes = 9;
	public static final int specificDimensions = 10;
	
	public int[] conditions;
	public Biome[] biomes;
	public Material[] floorTypes;
	public World.Environment[] dimensions;
	
	public SpawnCondition(int[] conditions, Biome[] biomes, Material[] floorTypes, World.Environment[] dimensions) {
		this.conditions = conditions;
		this.biomes = biomes;
		this.floorTypes = floorTypes;
		this.dimensions = dimensions;
	}
	
	public boolean canSpawn(Location loc) {
		
		for (int c : conditions) {
			switch (c) {
			case onGround:
				if (!(loc.getBlock().isPassable() && loc.clone().add(0, -1, 0).getBlock().isSolid())) {
					return false;
				}
				break;
			case air: 
				if (!loc.getBlock().isPassable()) {
					return false;
				}
				break;
			case onWater:
				if (!(loc.getBlock().isPassable() && !loc.getBlock().isLiquid() && loc.clone().add(0, -1, 0).getBlock().getType() == Material.WATER))
					return false;
				break;
			case inWater:
				if (!(loc.getBlock().isLiquid() && loc.getBlock().getType() == Material.WATER))
					return false;
				break;
			case onLava:
				if (!(loc.getBlock().isPassable() && !loc.getBlock().isLiquid() && loc.clone().add(0, -1, 0).getBlock().getType() == Material.LAVA))
					return false;
				break;
			case inLava:
				if (!(loc.getBlock().isLiquid() && loc.getBlock().getType() == Material.LAVA))
					return false;
				break;
			case specificBiomes:
				boolean inBiome = false;
				for (Biome biome : biomes) {
					if (loc.getBlock().getBiome().equals(biome)) {
						inBiome = true;
						break;
					}
				}
				if (!inBiome)
					return false;
				break;
			case specificFloorTypes:
				boolean onFloor = false;
				for (Material floor : floorTypes) {
					if (loc.getBlock().getType().equals(floor)) {
						onFloor = true;
						break;
					}
				}
				if (!onFloor)
					return false;
				break;
			case dark:
				if (loc.getBlock().getLightLevel() >= 8)
					return false;
				break;
			case pitchDark:
				if (loc.getBlock().getLightLevel() >= 1)
					return false;
				break;
			case specificDimensions:
				boolean inDimension = false;
				for (World.Environment dim : dimensions) {
					if (loc.getWorld().getEnvironment().equals(dim)) {
						inDimension = true;
						break;
					}
				}
				if (!inDimension)
					return false;
				break;
			}
				
		}
		
		return false;
	}
	
}
