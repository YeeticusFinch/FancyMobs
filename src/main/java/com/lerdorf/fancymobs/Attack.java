package com.lerdorf.fancymobs;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import net.kyori.adventure.sound.Sound;

public class Attack {
	
	String anim;
	double damage = -1;
	double reach = -1;
	long cooldown = 100;
	Map<Integer, Double> props;
	Sound sound = null;
	
	public static final int SWEEP = 0;
	public static final int KNOCKBACK = 1;
	public static final int FIRE = 2;
	public static final int KNOCKUP = 3;
	public static final int SLOWNESS = 4;
	public static final int WEAKNESS = 5;
	public static final int VAMPIRE = 6;

	public Attack(String anim, double damage, Map<Integer, Double> props, double reach, long cooldown) {
		this.anim = anim;
		this.damage = damage;
		this.props = props;
		this.reach = reach;
		this.cooldown = cooldown;
	}
	
	public Attack(String anim, double damage, Map<Integer, Double> props) {
		this.anim = anim;
		this.damage = damage;
		this.props = props;
	}
	
	public Attack(String anim, double damage, Map<Integer, Double> props, double reach, long cooldown, @NotNull Sound sound) {
		this.anim = anim;
		this.damage = damage;
		this.props = props;
		this.reach = reach;
		this.cooldown = cooldown;
		this.sound = sound;
	}

	public void attack(LivingEntity source, LivingEntity target) {
		if (sound != null) {
			Util.playSound(sound, source.getLocation());
		}
		if (props != null && props.size() > 0) {
			for (int prop : props.keySet()) {
				double power = props.get(prop);
				switch (prop) {
				case SWEEP:
					for (Entity e : target.getNearbyEntities(2, 2, 2)) {
						if (e instanceof LivingEntity le && le.getUniqueId() != source.getUniqueId() && le.getUniqueId() != target.getUniqueId()) {
							le.damage(power*damage, source);
						}
					}
					break;
				case KNOCKBACK:
					target.setVelocity(target.getVelocity().add( target.getLocation().toVector().subtract(source.getLocation().toVector()).normalize().multiply(power) ));
					break;
				case FIRE:
					target.setFireTicks(target.getFireTicks() + (int)power);
					break;
				case KNOCKUP:
					target.setVelocity(target.getVelocity().setY(power));
					break;
				case SLOWNESS:
					target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, (int)(power*10+4)*20, (int)power));
					break;
				case WEAKNESS:
					target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, (int)(power*10+4)*20, (int)power));
					break;
				case VAMPIRE:
					source.heal(power*damage);
					break;
				}
			}
		}
	}
	
}
