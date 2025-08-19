package com.lerdorf.fancymobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import kr.toxicity.model.api.BetterModel;
import kr.toxicity.model.api.bone.BoneName;
import kr.toxicity.model.api.bone.RenderedBone;
import kr.toxicity.model.api.nms.HitBox;
import kr.toxicity.model.api.nms.HitBoxListener;
import kr.toxicity.model.api.tracker.EntityTracker;
import kr.toxicity.model.api.tracker.EntityTrackerRegistry;
import kr.toxicity.model.api.tracker.ModelScaler;
import kr.toxicity.model.api.util.function.BonePredicate;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;

public class FancyMob {
	public static final int PEACEFUL = 0;
	public static final int NEUTRAL = 1;
	public static final int HOSTILE = 2;
	
	public String name = "FancyMob";
	public int maxHp = 20;
	public float moveSpeed = 0.1f;
	public float scale = 1;
	public int minXp = 0;
	public int maxXp = 1;
	
	public EntityTracker tracker;
	public String modelName;
	public LivingEntity entity;
	
	public EntityType baseType;
	
	public int alignment = 0;

	public Sound ambientSound;
	public Sound hurtSound;
	public Sound deathSound;
	
	public int aggroRange = 64;
	
	Attack[] attacks;
	public Ability[] abilities;
	
	HashMap<Attribute, Double> attributes = new HashMap<>();
	PotionEffect[] effects;
	Drop[] drops;
	
	SpawnCondition spawnCondition;
	
	Tameable tameable;
	
	public FancyMob(String name, int maxHp, float moveSpeed, float scale, String modelName, EntityType baseType, int alignment, HashMap<Attribute, Double> attributes, PotionEffect[] effects, Sound ambientSound, Sound hurtSound, Sound deathSound, int aggroRange, Attack[] attacks, Ability[] abilities, SpawnCondition spawnCondition, Tameable tameable, Drop[] drops, int minXp, int maxXp) {
		this.name = name;
		this.maxHp = maxHp;
		this.moveSpeed = moveSpeed;
		this.scale = scale;
		this.modelName = modelName;
		this.baseType = baseType;
		this.alignment = alignment;
		this.attributes = attributes;
		this.effects = effects;
		this.ambientSound = ambientSound;
		this.hurtSound = hurtSound;
		this.deathSound = deathSound;
		this.attacks = attacks;
		this.abilities = abilities;
		this.aggroRange = aggroRange;
		this.spawnCondition = spawnCondition;
		this.tameable = tameable;
		this.drops = drops;
		this.minXp = minXp;
		this.maxXp = maxXp;
	}
	
	public FancyMob clone() {
		return new FancyMob(name, maxHp, moveSpeed, scale, modelName, baseType, alignment, attributes, effects, ambientSound, hurtSound, deathSound, aggroRange, attacks, abilities, spawnCondition, tameable, drops, minXp, maxXp);
	}
	
	public void setup(LivingEntity le) {
	    this.entity = le;

	    // Get the registry for this entity
	    BetterModel.registry(le).ifPresent(registry -> {
	        // Only attach if a tracker already exists (don’t create a new one here)
	        registry.trackers().stream()
	            .filter(t -> t.sourceEntity().getUniqueId().equals(le.getUniqueId()))
	            .findFirst()
	            .ifPresent(t -> this.tracker = t);
	    });
	}
	
	long lastAttack = 0;
	
	public void attack(EntityDamageByEntityEvent event) {
		if (attacks != null && attacks.length > 0) {
			Attack attack = attacks[(int)(Math.random()*attacks.length)];
			if (System.currentTimeMillis() - lastAttack < attack.cooldown) {
				event.setCancelled(true);
				return;
			} 
			lastAttack = System.currentTimeMillis();
			tracker.animate(attack.anim);
			if (Math.abs(attack.damage+1) > 0.1)
				event.setDamage(attack.damage);
		} else
			tracker.animate("attack");
	}
	
	public void hurt() {
		//entity.getWorld().playSound(entity, hurtSound.name().asString(), hurtSound.volume(), hurtSound.pitch());
		Location loc = entity.getLocation();
		Util.playSound(hurtSound, loc);
		tracker.animate("hurt");
	}
	
	public void death() {
		//entity.getWorld().playSound(entity, deathSound.name().asString(), deathSound.volume(), deathSound.pitch());
		Location loc = entity.getLocation();
		Util.playSound(deathSound, loc);
		//entity.setInvisible(true);
		//entity.setVisibleByDefault(false);
	}
	
	public String getAlignmentString() {
		switch (alignment) {
		case HOSTILE:
			return "hostile";
		case NEUTRAL:
			return "neutral";
		case PEACEFUL:
			return "passive";
		}
		return "passive";
	}
	
	
	
	long lastAmbient = 0;
	
	public void tick_10() {
		if (entity != null && entity.isValid()) {
			Entity target = null;
			try {
				//entity.getTargetEntity(aggroRange, true);
				if (entity instanceof Mob mob) {
					target = mob.getTarget();
				}
			} catch (Exception e) {
				
			}
			if (target != null && target.isValid()) {
				Vector dir = target.getLocation().toVector().subtract(entity.getLocation().toVector());
				if (dir.dot(new Vector(0, 1, 0)) > 0.6 && Math.random() < 0.15) {
					entity.setJumping(true);
				}
				if (Math.random() < 0.5 && attacks.length > 0 && target instanceof LivingEntity le) {
					Attack attack = attacks[(int)(Math.random()*attacks.length)];
					if (attack.reach > -1 && System.currentTimeMillis() - lastAttack > attack.cooldown && target.getLocation().distance(entity.getEyeLocation()) < attack.reach && Math.abs(attack.damage+1) > 0.1) {
						lastAttack = System.currentTimeMillis();
						tracker.animate(attack.anim);
						le.damage(attack.damage, entity);
					}
				}
			}
			if (Math.random() < 0.1 && abilities != null && abilities.length > 0) {
				try {
					abilities[(int)(Math.random()*abilities.length)].act(this, (LivingEntity)target, false);
				} catch (Exception e) {
					System.out.println(e.getLocalizedMessage());
				}
			}
			
			if (Math.random() < 0.1 && ambientSound != null && System.currentTimeMillis() - lastAmbient > 1500) {
				lastAmbient = System.currentTimeMillis();
				Location loc = entity.getLocation();
				Util.playSound(ambientSound, loc);
			}
		}
	}
	
	public void spawn(Location loc) {
		entity = (LivingEntity) loc.getWorld().spawnEntity(loc, baseType);
		//entity.setCustomName(name);
		entity.customName(Component.text(name));
		entity.setCustomNameVisible(false);

		entity.addScoreboardTag("fm_mob");
		entity.setSilent(true);
		
		// Set Max Health and Current Health
		entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHp);
		entity.setHealth(maxHp);
		
		entity.getAttribute(Attribute.FOLLOW_RANGE).setBaseValue(aggroRange);

		// Set Movement Speed (if applicable to this entity)
		AttributeInstance speedAttr = entity.getAttribute(Attribute.MOVEMENT_SPEED);
		if (speedAttr != null) {
			speedAttr.setBaseValue(moveSpeed);
		}
		
		if (attributes != null && attributes.size() > 0) {
			for (Attribute attr : attributes.keySet()) {
				if (entity.getAttribute(attr) != null)
					entity.getAttribute(attr).setBaseValue(attributes.get(attr));
			}
		}
		
		for (PotionEffect effect : effects)
			entity.addPotionEffect(effect, true);
		entity.addPotionEffects(Arrays.asList(effects));

		// Alignment logic placeholder (can expand with AI changes later)
		switch (alignment) {
			case HOSTILE:
				Hostiler.activate(entity);
				break;
			case NEUTRAL:
				break;
			case PEACEFUL:
				break;
		}

		// Register model and set scale
		tracker = BetterModel.model(modelName).map(r -> r.getOrCreate(entity)).orElse(null);
		if (tracker != null) {
			tracker.scaler(ModelScaler.value(scale));

			HitBoxListener damageListener = HitBoxListener.builder()
			        .damage((hitBox, event, damage) -> {
			            entity.damage(damage, event.getCausingEntity());
			            return true;
			        })
			        .build();

			    // Remove only existing damage hitboxes (bones named or tagged "hitbox")
			    tracker.bones().stream()
			        .filter(bone -> bone.getHitBox() != null && isDamageHitboxBone(bone))
			        .forEach(bone -> bone.getHitBox().removeHitBox());

			    // Recreate damage hitboxes only on bones that qualify
			    BonePredicate pred = BonePredicate.of(BonePredicate.State.TRUE, bone -> isDamageHitboxBone(bone));
			    tracker.createHitBox(pred, damageListener);
		}
	}
	
	public List<UUID> passengers = new ArrayList<UUID>();
	
	public List<UUID> getPassengers() {
	    return passengers;
	}
	
	private boolean isDamageHitboxBone(RenderedBone bone) {
	    String name = bone.getName().name();
	    return /*name.equalsIgnoreCase("hitbox") ||*/ name.startsWith("b_") || name.startsWith("ob_");
	}

	public boolean canSpawnHere(Location tryLoc) {
		if (spawnCondition == null)
			return false;
		return spawnCondition.canSpawn(tryLoc);
	}
	
	public void mount(Entity passenger) {
		passengers.add(passenger.getUniqueId());
		if (abilities != null) {
			for (Ability a : abilities) {
				if (a.id == Ability.SLEEP || a.id == Ability.SIT) {
					a.act(this, null, false);
				}
			}
		}
	}
	
	public void dismount(Entity passenger) {
		passengers.remove(passenger.getUniqueId());
	}

	public void rightClick(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		
		if (tameable != null) {
			if (!tameable.tamed) {
				switch (tameable.tryTame(player)) {
				case -1: // Wrong item
					break;
				case 0: // Right item, unsuccessful
					event.getRightClicked().getWorld().spawnParticle(Particle.SMOKE, event.getRightClicked().getLocation(), 20, 0.4f, 0.4f, 0.4f, 0.1f);
					break;
				case 1: // Tamed!
					event.getRightClicked().getWorld().spawnParticle(Particle.HEART, event.getRightClicked().getLocation(), 20, 0.4f, 0.4f, 0.4f, 0.1f);
					break;
				}
			} else if (tameable.saddleable && !tameable.saddled) {
				if (tameable.isCorrectSaddle(player.getEquipment().getItemInMainHand())) {
					Location loc = entity.getLocation();
					double health = entity.getHealth();
					entity.remove();
					entity = null;
					modelName = tameable.saddleModel;
					player.getEquipment().getItemInMainHand().setAmount(player.getEquipment().getItemInMainHand().getAmount()-1);
					
					Bukkit.getScheduler().runTaskLater(FancyMobs.plugin, () -> {
						Bukkit.getLogger().warning("Setting " + name + " model to " + modelName);
						spawn(loc);
						entity.setHealth(health);
						entity.teleport(loc);
						tameable.saddled = true;
					}, 5);
				}
			}
		} 
		
	}

	public int getDroppedExp() {
		// TODO Auto-generated method stub
		
		return (int)(Math.random()*(maxXp-minXp)+minXp);
	}
}
