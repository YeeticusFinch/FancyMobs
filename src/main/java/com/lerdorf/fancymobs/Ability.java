package com.lerdorf.fancymobs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import kr.toxicity.model.api.animation.AnimationIterator;
import kr.toxicity.model.api.animation.AnimationModifier;
import kr.toxicity.model.api.tracker.TrackerModifier;
import kr.toxicity.model.api.util.FunctionUtil;
import kr.toxicity.model.api.util.function.FloatConstantSupplier;

public class Ability {

	public static final int RED_LASER = 0;
	public static final int THROW_SICKLE = 1;
	public static final int SIT = 2;
	public static final int SPRINT = 3;
	public static final int SLEEP = 4;
	public static final int ROAR = 5;
	public static final int SHAKE = 6;
	public static final int FLY = 7;
	public static final int GLIDE = 8;
	public static final int DIVE = 9;

	int id;
	String anim;
	long cooldown;
	long lastShot = 0;
	net.kyori.adventure.sound.Sound sound;

	public Ability(int id, String anim, long cooldown) {
		this.id = id;
		this.anim = anim;
		this.cooldown = cooldown;
	}
	
	public Ability(int id, String anim, long cooldown, net.kyori.adventure.sound.Sound sound) {
		this.id = id;
		this.anim = anim;
		this.cooldown = cooldown;
		this.sound = sound;
	}

	public boolean act(FancyMob mob, LivingEntity target, boolean force) {
		if (System.currentTimeMillis() - lastShot < cooldown)
			return false;
		lastShot = System.currentTimeMillis();
		double distance = -1;
		if (mob.entity != null && mob.entity.isValid() && target != null && target.isValid()) {
			distance = mob.entity.getEyeLocation().distance(target.getLocation());
		}
		if ((mob.entity.getScoreboardTags().contains("sitting") || mob.entity.getScoreboardTags().contains("sleeping")) && mob.getPassengers().size() > 0) {
			if (mob.entity.getScoreboardTags().contains("sitting")) {
				mob.entity.removePotionEffect(PotionEffectType.SLOWNESS);
				mob.entity.removeScoreboardTag("sitting");
				mob.tracker.stopAnimation("sitting");
			}
			if (mob.entity.getScoreboardTags().contains("sleeping")) {
				mob.entity.removePotionEffect(PotionEffectType.SLOWNESS);
				mob.entity.removeScoreboardTag("sleeping");
				mob.tracker.stopAnimation("sleeping");
			}
		}
		switch (id) {
		case RED_LASER: {
			if (force || (distance > 5 && distance < 60 && !mob.flying)) {
				mob.entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 2*20, 10, true,  false));
				mob.tracker.animate(anim);
				Bukkit.getScheduler().runTaskLater(FancyMobs.plugin, () -> {
					mob.entity.getWorld().playSound(mob.entity.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1.5f);
					shootLaser(mob, mob.entity.getEyeLocation(),
							target.getLocation().toVector().subtract(mob.entity.getEyeLocation().toVector()).normalize()
									.multiply(0.4),
							new FancyParticle(Particle.DUST, 1, 0, 0, 0, 0.1f, new DustOptions(Color.RED, 1)), 60, true,
							0.6);
					Bukkit.getScheduler().runTaskLater(FancyMobs.plugin, () -> {
						mob.tracker.stopAnimation(anim);
					}, 5);
				}, 5);
				return true;
			}
			break;
		}
		case THROW_SICKLE: {
			if (force || (distance > 3 && distance < 30 && !mob.flying)) {
				mob.entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 2*20, 10, true,  false));
				mob.tracker.animate(anim);
				Bukkit.getScheduler().runTaskLater(FancyMobs.plugin, () -> {
					mob.entity.getWorld().playSound(mob.entity.getEyeLocation(), Sound.ENTITY_ARROW_SHOOT, 1, 1f);
					ItemStack sickle = new ItemStack(Material.RAW_IRON);
					ItemMeta meta = sickle.getItemMeta();
					meta.setItemModel(NamespacedKey.fromString("wep:weapons/sickle/golden_sickle"));
					sickle.setItemMeta(meta);
					throwItem(sickle, mob, mob.entity.getEyeLocation(),
							target.getEyeLocation().toVector().subtract(mob.entity.getEyeLocation().toVector()).normalize()
									.multiply(0.75),
							new FancyParticle(Particle.DUST, 1, 0, 0, 0, 0.1f, new DustOptions(Color.YELLOW, 1)), 30,
							0.4f);
					Bukkit.getScheduler().runTaskLater(FancyMobs.plugin, () -> {
						mob.tracker.stopAnimation(anim);
					}, 5);
				}, 9);
				return true;
			}
			break;
		}
		case SIT: {
			if (force || ((target == null || !target.isValid()) && !mob.flying)) {
				if (!mob.entity.getScoreboardTags().contains("sitting")) {
					Collection<PotionEffect> newEffects = new ArrayList<>();
					newEffects.add(new PotionEffect(PotionEffectType.SLOWNESS, 20*20, 255, true, false));
					mob.entity.addPotionEffects(newEffects);
					mob.entity.addScoreboardTag("sitting");
					mob.tracker.animate(anim);
					Bukkit.getScheduler().runTaskLater(FancyMobs.plugin, () -> {
						if (mob.entity.getScoreboardTags().contains("sitting")) {
							mob.entity.removePotionEffect(PotionEffectType.SLOWNESS);
							mob.entity.removeScoreboardTag("sitting");
							mob.tracker.stopAnimation(anim);
						}
					}, 20*20);
					return true;
				}
			} else {
				if (mob.entity.getScoreboardTags().contains("sitting")) {
					mob.entity.removePotionEffect(PotionEffectType.SLOWNESS);
					mob.entity.removeScoreboardTag("sitting");
					mob.tracker.stopAnimation(anim);
					return true;
				}
			}
			break;
		}
		case SPRINT: {
			if (force || (distance > 3 && distance < 30 && !mob.entity.getScoreboardTags().contains("sprinting") && !mob.flying)) {
				Collection<PotionEffect> newEffects = new ArrayList<>();
				newEffects.add(new PotionEffect(PotionEffectType.SPEED, 20*20, 3, true, false));
				mob.entity.addPotionEffects(newEffects);
				
				var adapter = mob.tracker.registry().adapter();
				TrackerModifier modifier = mob.tracker.modifier();
				var damageTickProvider = FunctionUtil.throttleTickFloat(adapter::damageTick);
				 var walkSupplier = FunctionUtil.throttleTickBoolean(() -> adapter.onWalk() || damageTickProvider.getAsFloat() > 0.25 || mob.tracker.bones().stream().anyMatch(e -> {
			            var hitBox = e.getHitBox();
			            return hitBox != null && hitBox.onWalk();
			        }));
			        var walkSpeedSupplier = modifier.damageAnimation() ? FunctionUtil.throttleTickFloat(() -> adapter.walkSpeed() + 4F * (float) Math.sqrt(damageTickProvider.getAsFloat())) : FloatConstantSupplier.ONE;
			        mob.tracker.animate(anim, new AnimationModifier(walkSupplier, 6, 0, AnimationIterator.Type.LOOP, walkSpeedSupplier));
				//mob.tracker.animate(anim);
				
				mob.entity.addScoreboardTag("sprinting");
				Bukkit.getScheduler().runTaskLater(FancyMobs.plugin, () -> {
					mob.entity.removeScoreboardTag("sprinting");
					mob.tracker.stopAnimation(anim);
				}, 20*20);
				return true;
			}
			break;
		}
		case SHAKE: {
			if (force || (target == null && !mob.entity.getScoreboardTags().contains("shaking") && !mob.flying)) {
				Location loc = mob.entity.getLocation();
				mob.tracker.animate(anim);
				Collection<PotionEffect> newEffects = new ArrayList<>();
				newEffects.add(new PotionEffect(PotionEffectType.SLOWNESS, 20*2, 5, true, false));
				mob.entity.addPotionEffects(newEffects);
				Util.playSound(sound, loc);
				mob.entity.addScoreboardTag("shaking");
				Bukkit.getScheduler().runTaskLater(FancyMobs.plugin, () -> {
					mob.tracker.stopAnimation(anim);
					mob.entity.removeScoreboardTag("shaking");
				}, 20*2);
				return true;
			}
			break;
		}
		case ROAR: {
			if (force || (!mob.entity.getScoreboardTags().contains("roaring") && !mob.flying)) {
				Location loc = mob.entity.getLocation();
				mob.tracker.animate(anim);
				Collection<PotionEffect> newEffects = new ArrayList<>();
				newEffects.add(new PotionEffect(PotionEffectType.SLOWNESS, 20*3, 50, true, false));
				mob.entity.addPotionEffects(newEffects);
				Util.playSound(sound, loc);
				mob.entity.addScoreboardTag("roaring");
				Bukkit.getScheduler().runTaskLater(FancyMobs.plugin, () -> {
					mob.tracker.stopAnimation(anim);
					mob.entity.removeScoreboardTag("roaring");
				}, 20*3);
				return true;
			}
			break;
		}
		case FLY: {
			if (force || !mob.entity.getScoreboardTags().contains("flying")) {
				Collection<PotionEffect> newEffects = new ArrayList<>();
				newEffects.add(new PotionEffect(PotionEffectType.SLOW_FALLING, 20*90, 3, true, false));
				mob.entity.addPotionEffects(newEffects);
				
				//mob.entity.setGravity(false);
				
				 var walkSupplier = FunctionUtil.throttleTickBoolean(() -> true);
			        var walkSpeedSupplier = FloatConstantSupplier.ONE;
			        mob.tracker.animate(anim, new AnimationModifier(walkSupplier, 6, 0, AnimationIterator.Type.LOOP, walkSpeedSupplier));
				//mob.tracker.animate(anim);
				mob.flyAnim = anim;
				mob.entity.addScoreboardTag("flying");
				mob.flying = true;
				Bukkit.getScheduler().runTaskLater(FancyMobs.plugin, () -> {
					mob.entity.removeScoreboardTag("flying");
					mob.tracker.stopAnimation(anim);
					//mob.entity.setGravity(true);
					mob.flying = false;
					mob.entity.removePotionEffect(PotionEffectType.SLOW_FALLING);
					if (mob.gliding) {
						mob.gliding = false;
						mob.tracker.stopAnimation(mob.glideAnim);
						mob.entity.removeScoreboardTag("gliding");
					}
					if (mob.diving) {
						mob.diving = false;
						mob.tracker.stopAnimation(mob.diveAnim);
						mob.entity.removeScoreboardTag("diving");
					}
				}, (int)(20*(30+Math.random()*60)));
				return true;
			}
			break;
		}
		case GLIDE: {
			if (force || (!mob.entity.getScoreboardTags().contains("gliding") && mob.entity.getScoreboardTags().contains("flying"))) {
				//Collection<PotionEffect> newEffects = new ArrayList<>();
				//newEffects.add(new PotionEffect(PotionEffectType.SPEED, 20*20, 3, true, false));
				//mob.entity.addPotionEffects(newEffects);
				
				//mob.entity.setGravity(false);
				
				var walkSupplier = FunctionUtil.throttleTickBoolean(() -> true);
		        var walkSpeedSupplier = FloatConstantSupplier.ONE;
		        mob.tracker.animate(anim, new AnimationModifier(walkSupplier, 6, 0, AnimationIterator.Type.LOOP, walkSpeedSupplier));
				//mob.tracker.animate(anim);
				mob.glideAnim = anim;
				mob.entity.addScoreboardTag("gliding");
				mob.gliding = true;
				Bukkit.getScheduler().runTaskLater(FancyMobs.plugin, () -> {
					mob.entity.removeScoreboardTag("gliding");
					mob.tracker.stopAnimation(anim);
					mob.gliding = false;
				}, (int)(20*(Math.random()*60)));
				return true;
			}
			break;
		}
		case DIVE:
		{
			if (force || (target != null && !mob.entity.getScoreboardTags().contains("diving") && mob.entity.getScoreboardTags().contains("flying"))) {
				//Collection<PotionEffect> newEffects = new ArrayList<>();
				//newEffects.add(new PotionEffect(PotionEffectType.SPEED, 20*20, 3, true, false));
				//mob.entity.addPotionEffects(newEffects);
				
				//mob.entity.setGravity(false);
				
				var walkSupplier = FunctionUtil.throttleTickBoolean(() -> true);
		        var walkSpeedSupplier = FloatConstantSupplier.ONE;
		        mob.tracker.animate(anim, new AnimationModifier(walkSupplier, 6, 0, AnimationIterator.Type.LOOP, walkSpeedSupplier));
				//mob.tracker.animate(anim);
				mob.diveAnim = anim;
				mob.entity.addScoreboardTag("diving");
				mob.diving = true;
				Bukkit.getScheduler().runTaskLater(FancyMobs.plugin, () -> {
					mob.entity.removeScoreboardTag("diving");
					mob.tracker.stopAnimation(anim);
					mob.diving = false;
				}, (int)(20*(10)));
				return true;
			}
			break;
		}
		}
		return false;
	}

	boolean hitBlock(FancyMob mob, Location loc) {
		switch (id) {
		case RED_LASER: {
			loc.getWorld().spawnParticle(Particle.FLASH, loc, 1, 0, 0, 0, 0);
			loc.getWorld().playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 2);
			return true;
		}
		case THROW_SICKLE: {
			loc.getWorld().playSound(loc, Sound.ITEM_TRIDENT_RETURN, 1, 2);
			return true;
		}
		}
		return true;
	}

	boolean hitEntity(FancyMob mob, LivingEntity entity) {
		switch (id) {
		case RED_LASER: {
			entity.damage(5, mob.entity);
			entity.setVelocity(entity.getVelocity().add(
					mob.entity.getEyeLocation().subtract(entity.getLocation()).toVector().normalize().multiply(2)));
			entity.getWorld().playSound(entity, Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1, 1);
			break;
		}
		case THROW_SICKLE: {
			entity.damage(9, mob.entity);
			entity.setVelocity(entity.getVelocity()
					.subtract(mob.entity.getEyeLocation().subtract(entity.getLocation()).toVector().normalize()));
			entity.getWorld().playSound(entity, Sound.ITEM_TRIDENT_RETURN, 1, 2);
			return true;
		}
		}
		return false;
	}

	public void throwItem(ItemStack item, FancyMob mob, Location loc, Vector step, FancyParticle particle, double range,
			double radius) {
		loc.add(step);

		Quaternionf rot = new Quaternionf().rotateX((float) Math.toRadians(90)) // rotate 90° on X axis
				.rotateZ((float) Math.toRadians(-45)); // then 45° on Y axis

		// Convert to AxisAngle
		AxisAngle4f axisAngle = new AxisAngle4f().set(rot);

		Vector offset = new Vector(0, -0.2f, 0);
		float rightOffset = 0.2f;
		float forwardOffset = 0.4f;

		ItemDisplay display = loc.getWorld().spawn(loc.clone().add(step.clone().multiply(forwardOffset).add(offset)),
				ItemDisplay.class, entity -> {
					// customize the entity!
					entity.setItemStack(item);
					entity.setTransformation(
							new Transformation(new Vector3f(), axisAngle, new Vector3f(1f, 1f, 1f), new AxisAngle4f()));

				});

		Collection<LivingEntity> nearbyEntities = loc.getWorld().getNearbyLivingEntities(
				loc.clone().add(step.clone().normalize().multiply(range / 2)), range / 2, range / 2, range / 2,
				entity -> !entity.equals(mob.entity));

		new BukkitRunnable() {
			int ticks = 0;
			int lifetime = 1000;
			double distance = 0;
			Location point = loc;
			ArrayList<UUID> hitEntities = new ArrayList<UUID>();

			@Override
			public void run() {

				point = point.add(step);
				particle.spawn(point.clone().subtract(step.clone().multiply(0.5f)));
				particle.spawn(point);
				point.setRotation(point.getYaw()+20, point.getPitch());
				display.teleport(point);
				display.setRotation(display.getYaw()+20, display.getPitch());

				for (LivingEntity le : nearbyEntities) {
					if (intersectsSegmentAABB(point.toVector().subtract(step), point.toVector(), le.getBoundingBox()) && !hitEntities.contains(le.getUniqueId())) {
						hitEntities.add(le.getUniqueId());
						boolean stop = hitEntity(mob, le);
						if (stop) {
							display.remove();
							cancel();
							return;
						}
					}
				}

				Block block = point.getBlock();
				if (!block.isPassable() && block.getBoundingBox().contains(point.toVector())) {
					point = getClosestPoint(loc.toVector(), block.getBoundingBox()).toLocation(loc.getWorld())
							.subtract(step.clone().normalize().multiply(0.5f));
					boolean stop = hitBlock(mob, point);
					if (stop) {
						display.remove();
						cancel();
						return;
					}
				}

				if (ticks > lifetime || distance > range) {
					display.remove();
					cancel();
					return;
				}
				ticks++;
			}
		}.runTaskTimer(FancyMobs.plugin, 0L, 1L);
	}

	public void shootLaser(FancyMob mob, Location loc, Vector step, FancyParticle particle, double range,
			boolean hitscan, double radius) {
		loc.add(step);

		Collection<LivingEntity> nearbyEntities = loc.getWorld().getNearbyLivingEntities(
				loc.clone().add(step.clone().normalize().multiply(range / 2)), range / 2, range / 2, range / 2,
				entity -> !entity.equals(mob.entity));

		if (hitscan) {
			
			Location hit = raycastForBlocks(loc.clone(), step.clone().normalize().multiply(range));

			Location point = loc.clone();
			
			for (LivingEntity le : nearbyEntities) {
				//if (!hitEntities.contains(le.getUniqueId())) {
				//	hitEntities.add(le.getUniqueId());
				if (intersectsSegmentAABB(point.clone().subtract(step).toVector(), point.toVector(), le.getBoundingBox())) {
					boolean stop = hitEntity(mob, le);
					if (stop) {
						hit = getClosestPoint(point.clone().subtract(step).toVector(), le.getBoundingBox()).toLocation(point.getWorld());
						break;
					}
				}
				//}
			}
			
			double dist = hit.distance(loc);
			Vector halfStep = step.clone().normalize().multiply(0.5);
			for (float i = 0; i < dist; i += step.length()/2) {
				particle.spawn(point.add(halfStep));
			}
			
			hitBlock(mob, hit);
			
		} else {

			new BukkitRunnable() {
				int ticks = 0;
				int lifetime = 1000;
				double distance = 0;
				Location point = loc;
				ArrayList<UUID> hitEntities = new ArrayList<UUID>();

				@Override
				public void run() {

					point = point.add(step);
					particle.spawn(point);

					for (LivingEntity le : nearbyEntities) {
						if (intersectsSegmentAABB(point.clone().subtract(step).toVector(), point.toVector(), le.getBoundingBox()) && !hitEntities.contains(le.getUniqueId())) {
							hitEntities.add(le.getUniqueId());
							boolean stop = hitEntity(mob, le);
							if (stop) {
								cancel();
								return;
							}
						}
					}

					Block block = point.getBlock();
					if (!block.isPassable() && block.getBoundingBox().contains(point.toVector())) {
						point = getClosestPoint(loc.toVector(), block.getBoundingBox()).toLocation(loc.getWorld())
								.subtract(step.clone().normalize().multiply(0.5f));

						cancel();
						return;
					}

					if (ticks > lifetime || distance > range) {
						cancel();
						return;
					}
					ticks++;
				}
			}.runTaskTimer(FancyMobs.plugin, 0L, 1L);
		}
	}

	public Location raycastForBlocks(Location loc, Vector target) {
		Location result = loc.clone();

		double inc = 0.9;

		for (int i = 0; i < target.length() / inc; i++) {
			Block block = result.getBlock();
			if (!block.isPassable() && block.getBoundingBox().contains(result.toVector())) {
				return getClosestPoint(loc.toVector(), block.getBoundingBox()).toLocation(loc.getWorld())
						.subtract(target.clone().normalize().multiply(0.5f));
			}
			result = result.add(target.clone().normalize().multiply(0.9));
		}

		result.add(target.clone().normalize().multiply(target.length() - inc * ((int) (target.length() / inc))));

		return result;
	}

	public static Vector getClosestPoint(Vector point, BoundingBox box) {
		double x = clamp(point.getX(), box.getMinX(), box.getMaxX());
		double y = clamp(point.getY(), box.getMinY(), box.getMaxY());
		double z = clamp(point.getZ(), box.getMinZ(), box.getMaxZ());
		return new Vector(x, y, z);
	}

	private static double clamp(double val, double min, double max) {
		return Math.max(min, Math.min(max, val));
	}

	public boolean intersectsSegmentAABB(Vector start, Vector end, BoundingBox box) {
		Vector dir = end.clone().subtract(start);
		Vector invDir = new Vector(dir.getX() == 0 ? Double.POSITIVE_INFINITY : 1.0 / dir.getX(),
				dir.getY() == 0 ? Double.POSITIVE_INFINITY : 1.0 / dir.getY(),
				dir.getZ() == 0 ? Double.POSITIVE_INFINITY : 1.0 / dir.getZ());

		double tMin = 0.0;
		double tMax = 1.0;

		Vector min = new Vector(box.getMinX(), box.getMinY(), box.getMinZ());
		Vector max = new Vector(box.getMaxX(), box.getMaxY(), box.getMaxZ());

		// Iterate over X, Y, Z manually
		double[] startComponents = { start.getX(), start.getY(), start.getZ() };
		double[] invComponents = { invDir.getX(), invDir.getY(), invDir.getZ() };
		double[] minComponents = { min.getX(), min.getY(), min.getZ() };
		double[] maxComponents = { max.getX(), max.getY(), max.getZ() };

		for (int i = 0; i < 3; i++) {
			double startComponent = startComponents[i];
			double inv = invComponents[i];
			double t1 = (minComponents[i] - startComponent) * inv;
			double t2 = (maxComponents[i] - startComponent) * inv;

			double tNear = Math.min(t1, t2);
			double tFar = Math.max(t1, t2);

			tMin = Math.max(tMin, tNear);
			tMax = Math.min(tMax, tFar);

			if (tMax < tMin) {
				return false;
			}
		}

		return true;
	}

}
