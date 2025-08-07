package com.lerdorf.fancymobs;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.Multimap;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;

import kr.toxicity.model.api.*;
import kr.toxicity.model.api.bone.RenderedBone;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;

public class FancyMobs extends JavaPlugin implements Listener, TabExecutor {

	private File configFile;
	private Map<String, Object> configValues;
	
	boolean enforceResourcepack = true;
	String resourcepackUpdateScript = "/home/carl/KraftySMPTest/update-resource-pack.sh";

	public static Plugin plugin;

	public HashMap<String, FancyMob> mobRegistry = new HashMap<>();
	public HashMap<UUID, FancyMob> fancyMobs = new HashMap<>();

	public void loadMobRegistry() {
		mobRegistry.put("warrior25", new FancyMob("Warrior25", 100, 0.2f, 1, "warrior25.generic", EntityType.IRON_GOLEM,
				FancyMob.HOSTILE, new HashMap<>() {
					{
						put(Attribute.ARMOR, 10d);
					}
				},
				new PotionEffect[] {
						new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3, true, false),
						new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 2, true, false) 
					},
				Sound.sound(Key.key("yeet:gulag"), Source.HOSTILE, 1.0f, 1.0f),
				Sound.sound(Key.key("yeet:soviet_hurt"), Source.HOSTILE, 1.0f, 1.0f),
				Sound.sound(Key.key("yeet:soviet_death"), Source.HOSTILE, 1.0f, 1.0f),
				80,
				new Attack[] { 
						new Attack("attack", 10, new HashMap<>() {
							{
								put(Attack.KNOCKBACK, 4d);
							}
						}), 
						new Attack("attackL", 8, new HashMap<>() {
						{
							put(Attack.KNOCKBACK, -4d);
							put(Attack.SWEEP, 0.6d);
						}
						}) 
				},
				new Ability[] {
						new Ability(Ability.RED_LASER, "block", 3500),
						new Ability(Ability.THROW_SICKLE, "throw", 2000),
						new Ability(Ability.SPRINT, "sprint", 20000)
				},
				null, null
				));
		mobRegistry.put("barinasuchus", new FancyMob("Barinasuchus", 40, 0.2f, 0.7f, "barinasuchus", EntityType.POLAR_BEAR,
				FancyMob.NEUTRAL, new HashMap<>() {
					{
						put(Attribute.ARMOR, 15d);
						put(Attribute.WATER_MOVEMENT_EFFICIENCY, 0.5);
					}
				},
				new PotionEffect[] {
						new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 3, true, false)
					},
				Sound.sound(Key.key("yeet:barina_idle"), Source.NEUTRAL, 1.0f, 1.0f),
				Sound.sound(Key.key("yeet:barina_hurt"), Source.NEUTRAL, 1.0f, 1.0f),
				Sound.sound(Key.key("yeet:barina_death"), Source.NEUTRAL, 1.0f, 1.0f),
				70,
				new Attack[] { 
						new Attack("bite", 8, new HashMap<>() {
							{
								put(Attack.SLOWNESS, 2d);
							}
						}, 5, 600
					)
				},
				new Ability[] {
						new Ability(Ability.SIT, "sitting", 2000)
				},
				new SpawnCondition(new int[] {SpawnCondition.onGround, SpawnCondition.specificFloorTypes, SpawnCondition.specificDimensions}, null, new Material[] {Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT}, new Environment[] {Environment.NORMAL}),
				null
				));

		mobRegistry.put("suchomimmus", new FancyMob("Suchomimmus", 40, 0.2f, 0.7f, "suchomimmus.generic", EntityType.WOLF,
				FancyMob.NEUTRAL, new HashMap<>() {
					{
						put(Attribute.ARMOR, 15d);
						put(Attribute.WATER_MOVEMENT_EFFICIENCY, 0.5);
					}
				},
				new PotionEffect[] {
						new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 3, true, false)
					},
				Sound.sound(Key.key("yeet:spino_idle"), Source.NEUTRAL, 1.0f, 1.0f),
				Sound.sound(Key.key("yeet:spino_hurt"), Source.NEUTRAL, 1.0f, 1.0f),
				Sound.sound(Key.key("yeet:spino_death"), Source.NEUTRAL, 1.0f, 1.0f),
				40,
				new Attack[] { 
						new Attack("bite", 7, new HashMap<>() {
							{
								put(Attack.WEAKNESS, 2d);
							}
						}, 5, 600
					)
				},
				new Ability[] {
						new Ability(Ability.SIT, "sitting", 2000),
						new Ability(Ability.SLEEP, "sleeping", 20000),
						new Ability(Ability.ROAR, "roaring", 5000, Sound.sound(Key.key("yeet:spino_roar"), Source.NEUTRAL, 1.0f, 0.7f)),
						new Ability(Ability.SHAKE, "shaking", 10000, Sound.sound(Key.key("minecraft:entity.wolf.shake"), Source.NEUTRAL, 1.0f, 0.7f)),
				},
				new SpawnCondition(new int[] {SpawnCondition.onGround, SpawnCondition.specificFloorTypes, SpawnCondition.specificDimensions}, null, new Material[] {Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT}, new Environment[] {Environment.NORMAL}),
				new Tameable(new Material[] {Material.BEEF, Material.PORKCHOP, Material.CHICKEN, Material.MUTTON, Material.RABBIT}, new ItemStack(Material.SADDLE), true, "suchomimmus.saddle")
				));
		
	}

	public void loadConfig() {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // <-- use block style
		options.setIndent(2);
		options.setPrettyFlow(true);

		File pluginFolder = this.getDataFolder();
		if (!pluginFolder.exists())
			pluginFolder.mkdirs();

		configFile = new File(pluginFolder, "config.yml");

		Yaml yaml = new Yaml(options);

		// If file doesn't exist, create it with defaults
		if (!configFile.exists()) {
			configValues = new HashMap<>();
			// configValues.put("requireBothHandsEmpty", requireBothHandsEmpty);
			saveConfig(); // Save default config
		}

		try {
			String yamlAsString = Files.readString(configFile.toPath());
			configValues = (Map<String, Object>) yaml.load(yamlAsString);
			if (configValues == null)
				configValues = new HashMap<>();
		} catch (Exception e) {
			e.printStackTrace();
			configValues = new HashMap<>();
		}

		// Now parse and update values
		
		try {
			if (configValues.containsKey("enforceResourcepack"))
				enforceResourcepack = (boolean)configValues.get("enforceResourcepack");
		} catch (Exception e) {
			
		}
		configValues.put("enforceResourcepack", enforceResourcepack);
		
		try {
			if (configValues.containsKey("resourcepackUpdateScript"))
				resourcepackUpdateScript = (String)configValues.get("resourcepackUpdateScript");
		} catch (Exception e) {
			
		}
		configValues.put("resourcepackUpdateScript", resourcepackUpdateScript);
		

		saveConfig(); // Ensure config is up to date
	}

	public void saveConfig() {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // <-- use block style
		options.setIndent(2);
		options.setPrettyFlow(true);

		Yaml yaml = new Yaml(options);
		try (FileWriter writer = new FileWriter(configFile)) {
			yaml.dump(configValues, writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onEnable() {
		plugin = this;
		getServer().getPluginManager().registerEvents(this, this);

		this.getCommand("fm").setExecutor(this);

		loadConfig();
		saveConfig();

		loadMobRegistry();

		startSpawnLoop();

		  new BukkitRunnable() {
		  int c = 0;
		  @Override public void run() {
			  if (Bukkit.getOnlinePlayers().size() > 0) {
				  c++;
				  if (c%10==0) {
					  for (FancyMob mob : fancyMobs.values())
						  mob.tick_10();
				  }
			  }
		  } }.runTaskTimer(this, 0L, 1L); // Run every 1 tick


		getLogger().info("FancyMobs enabled!");
	}

	@Override
	public void onDisable() {
		getLogger().info("FancyMobs disabled!");
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) throws ExecutionException, InterruptedException {
		sendResourcepack(event.getPlayer());
	}
	
	public void sendResourcepack(Player player) throws InterruptedException, ExecutionException {
		if (!enforceResourcepack) return;
		ResourcePackInfo packInfo = ResourcePackInfo.resourcePackInfo()
				.uri(URI.create("https://fancy.lerdorf.com/build.zip"))
				.computeHashAndBuild().get();
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> player.sendResourcePacks(packInfo), 5);
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();
		if (entity instanceof Interaction interaction) {
			FancyMob fm = getFancyMobFromInteraction(interaction);
			if (fm != null) {
				fm.rightClick(event);
			}
		} else if (entity instanceof LivingEntity le) {
			if (le.getScoreboardTags().contains("fm_mob")) {
				getFancyMob(le).rightClick(event);;
			}
		}
	}
	
	public FancyMob getFancyMobFromInteraction(Interaction interaction) {
		Location loc = interaction.getLocation();
		Collection<LivingEntity> nearbyFancyMobs = loc.getWorld().getNearbyLivingEntities(
				loc, 10, 10, 10,
				e -> e.getScoreboardTags().contains("fm_mob"));
		for (LivingEntity fm : nearbyFancyMobs) {
			FancyMob fancyMob = getFancyMob(fm);
			for (RenderedBone bone : fancyMob.tracker.bones()) {
				if (bone.getHitBox() != null && bone.getHitBox().source() != null && bone.getHitBox().source().getUniqueId() == interaction.getUniqueId()) {
					return fancyMob;
				}
			}
			
		}
		return null;
	}
	
	public void startSpawnLoop() {
		new BukkitRunnable() {
			@Override
			public void run() {
				for (World world : Bukkit.getWorlds()) {
					for (Player player : world.getPlayers()) {
						if (Math.random() < 0.05) { // 5% chance to spawn something per check
							boolean spawned = getRandomNearbySpawn(player.getLocation());
						}
					}
				}
			}
		}.runTaskTimer(this, 200L, 200L); // Every 10 seconds
	}
	
	public boolean getRandomNearbySpawn(Location center) {
		int radius = 15;
		FancyMob[] mobs = new FancyMob[mobRegistry.size()];
		mobs = mobRegistry.values().toArray(mobs);
		for (int i = 0; i < 10; i++) {
			int dx = (int) (Math.random() * radius * 2) - radius;
			int dz = (int) (Math.random() * radius * 2) - radius;
			Location tryLoc = center.clone().add(dx, 0, dz);
			tryLoc.setY(tryLoc.getWorld().getHighestBlockYAt(tryLoc) + 1);
			for (int j = 0; j < mobs.length; j++) {
				FancyMob mob = mobs[(int)(Math.random()*mobs.length)];
				if (mob.spawnCondition != null)
				{
					if (mob.canSpawnHere(tryLoc)) {
						// Spawn
						mob.clone().spawn(tryLoc);
						return  true;
					}
					if (mob.canSpawnHere(tryLoc.clone().add(0, 1, 0))) {
						// Spawn
						mob.clone().spawn(tryLoc.clone().add(0, 1, 0));
						return  true;
					}
					if (mob.canSpawnHere(tryLoc.clone().add(0, -1, 0))) {
						// Spawn
						mob.clone().spawn(tryLoc.clone().add(0, -1, 0));
						return  true;
					}
					if (mob.canSpawnHere(tryLoc.clone().add(0, Math.random()*50, 0))) {
						// Spawn
						mob.clone().spawn(tryLoc.clone().add(0, Math.random()*50, 0));
						return  true;
					}
				}
			}
		}
		return false;
	}

	private List<String> filterPrefix(List<String> options, String prefix) {
		return options.stream().filter(opt -> opt.toLowerCase().startsWith(prefix.toLowerCase())).sorted().toList();
	}

	public String getName(LivingEntity le) {
		return PlainTextComponentSerializer.plainText().serialize(le.customName());
	}

	public FancyMob getFancyMob(LivingEntity le) {
		if (fancyMobs.containsKey(le.getUniqueId())) {
			return fancyMobs.get(le.getUniqueId());
		} else {
			FancyMob mob = mobRegistry.get(getName(le).toLowerCase());
			if (mob != null) {
				FancyMob newMob = mob.clone();
				newMob.setup(le);
				fancyMobs.put(le.getUniqueId(), newMob);
				return newMob;
			} else {
				return null;
			}
		}
	}

	@EventHandler
	public void onEntityAttack(EntityDamageByEntityEvent event) {
		Entity damager = event.getDamager();

		if (damager instanceof LivingEntity living && living.getScoreboardTags().contains("fm_mob")) {
			FancyMob fancyMob = getFancyMob(living);
			if (fancyMob != null) {
				fancyMob.attack(event);
			}
		}
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof LivingEntity le && le.getScoreboardTags().contains("fm_mob")) {
				FancyMob mob = getFancyMob(le);
				if (mob != null)
					mob.death();
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		try {
			if (event.getEntity() instanceof LivingEntity le && le.getScoreboardTags().contains("fm_mob")) {
				if (le.getHealth() - event.getDamage() < 0.1) {
					//FancyMob mob = getFancyMob(le);
					//if (mob != null)
					//	mob.death();
				} else {
					FancyMob mob = getFancyMob(le);
					if (mob != null)
						mob.hurt();
				}
			}
		} catch (Exception ex) {

		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("fm")) {
			if (args.length == 1) {
				// Suggest first-level subcommands like 'spawn'
				return filterPrefix(List.of("spawn"), args[0]);
			} else if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
				// Suggest available mob names from mobRegistry
				return filterPrefix(new ArrayList<>(mobRegistry.keySet()), args[1]);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
			try {
				// Copy the files from build to expanded_warfare, zip the new resourcepack and update the sha1 hash
				ProcessBuilder pb = new ProcessBuilder(resourcepackUpdateScript);
				Process pc = pb.start();
				Bukkit.getScheduler().runTaskLater(FancyMobs.plugin, () -> {
					for (Player p : Bukkit.getOnlinePlayers())
						try {
							sendResourcepack(p);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}, 60);
			} catch (Exception e) {
				sender.sendMessage("Failed to modify resourcepack: " + e.getLocalizedMessage());
			}
		} 
		else if (args.length > 1) {
			Location loc = null;
			Player player = null;
			if (sender instanceof Player p) {
				loc = p.getLocation();
				player = p;
				if (args[0].equalsIgnoreCase("model")) {
					ItemStack item = player.getEquipment().getItemInMainHand();
					if (item == null) {
						player.sendMessage("Must be holding an item");
						return false;
					}
					ItemMeta meta = item.getItemMeta();
					meta.setItemModel(NamespacedKey.fromString(args[1]));
					item.setItemMeta(meta);
					return true;
				}
			} else if (sender instanceof BlockCommandSender blockSender) {
				loc = blockSender.getBlock().getLocation().add(0.5, 1.5, 0.5);
			}
			if (args[0].equalsIgnoreCase("spawn")) {
				// Spawn a FancyMob
				// This should tab-complete for the mob registry
				String mobName = args[1].toLowerCase();
				FancyMob fancyMob = mobRegistry.get(mobName).clone();
				fancyMob.spawn(loc);
				if (player != null)
					player.sendMessage(ChatColor.AQUA + "Spawning " + fancyMob.name);
				return true;
			}
		}
		return false;
	}

}