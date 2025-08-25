package com.lerdorf.fancymobs;

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.player.Player;

import org.bukkit.craftbukkit.entity.CraftCat;
import org.bukkit.craftbukkit.entity.CraftFox;
import org.bukkit.craftbukkit.entity.CraftIronGolem;
import org.bukkit.craftbukkit.entity.CraftOcelot;
import org.bukkit.craftbukkit.entity.CraftParrot;
import org.bukkit.craftbukkit.entity.CraftPolarBear;
import org.bukkit.craftbukkit.entity.CraftWolf;
import org.bukkit.craftbukkit.entity.CraftAxolotl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.axolotl.*;

public class Hostiler {
	public static void activate(Entity entity) {
	    if (!(entity instanceof org.bukkit.entity.LivingEntity le) || !entity.isValid()) return;

	    switch (le.getType()) {
	        case IRON_GOLEM -> {
	            CraftIronGolem craft = (CraftIronGolem) entity;
	            IronGolem nms = craft.getHandle();

	            // Add nearest-player targeting
	            nms.goalSelector.addGoal(1, new MeleeAttackGoal(nms, 1.0D, true));
	            nms.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(nms, Player.class, true));
	            break;
	        }
	        case CAT -> {
	            CraftCat craft = (CraftCat) entity;
	            Cat nms = craft.getHandle();
	            nms.goalSelector.addGoal(1, new MeleeAttackGoal(nms, 2.0D, true));
	            nms.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(nms, Player.class, true));
	            break;
	        }
	        case OCELOT -> {
	        	CraftOcelot craft = (CraftOcelot) entity;
	            Ocelot nms = craft.getHandle();
	            nms.goalSelector.addGoal(1, new MeleeAttackGoal(nms, 2.0D, true));
	            nms.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(nms, Player.class, true));
	            break;
	        }
	        case FOX -> {
	        	CraftFox craft = (CraftFox) entity;
	            Fox nms = craft.getHandle();
	            nms.goalSelector.addGoal(1, new MeleeAttackGoal(nms, 1.8D, true));
	            nms.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(nms, Player.class, true));
	            break;
	        }
	        case AXOLOTL -> {
	        	CraftAxolotl craft = (CraftAxolotl) entity;
	            Axolotl nms = craft.getHandle();
	            nms.goalSelector.addGoal(1, new MeleeAttackGoal(nms, 1.8D, true));
	            nms.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(nms, Player.class, true));
	            break;
	        }
	        case POLAR_BEAR -> {
	        	CraftPolarBear craft = (CraftPolarBear) entity;
	            PolarBear nms = craft.getHandle();
	            nms.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(nms, Player.class, true));
	            break;
	        }
	        case WOLF -> {
	        	CraftWolf craft = (CraftWolf) entity;
	            Wolf nms = craft.getHandle();
	            nms.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(nms, Player.class, true));
	            break;
	        }
	        case PARROT -> {
	            var craft = (CraftParrot) entity;
	            Parrot nms = craft.getHandle();
	            nms.goalSelector.addGoal(1, new MeleeAttackGoal(nms, 1.0D, true));
	            nms.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(nms, Player.class, true));
	            break;
	        }
	        default -> { /* not supported */ }
	    }
	}

}