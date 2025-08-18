package com.lerdorf.fancymobs;

import com.sk89q.worldedit.*;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;

import java.util.EnumSet;
import java.util.Set;
import java.util.Arrays;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Biome;
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
import org.bukkit.event.world.ChunkLoadEvent;
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
import kr.toxicity.model.api.event.DismountModelEvent;
import kr.toxicity.model.api.event.MountModelEvent;
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
						new Attack("attack_1", 8, new HashMap<>() {
								{
									put(Attack.SLOWNESS, 2d);
								}
							}, 6, 900
						),
						new Attack("attack_2", 6, new HashMap<>() {
							{
								//put(Attack.SLOWNESS, 2d);
							}
						}, 5, 500
					)
				},
				new Ability[] {
						new Ability(Ability.SIT, "sitting", 2000)
				},
				new SpawnCondition(new int[] {SpawnCondition.onGround, SpawnCondition.specificFloorTypes, SpawnCondition.specificDimensions}, null, new Material[] {Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT}, new Environment[] {Environment.NORMAL}),
				null
				));

		mobRegistry.put("suchomimmus", new FancyMob("Suchomimmus", 35, 0.3f, 0.6f, "suchomimmus.generic", EntityType.POLAR_BEAR,
				FancyMob.NEUTRAL, new HashMap<>() {
					{
						put(Attribute.ARMOR, 5d);
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
						new Ability(Ability.SPRINT, "sprinting", 5000)
				},
				new SpawnCondition(new int[] {SpawnCondition.onGround, SpawnCondition.specificFloorTypes, SpawnCondition.specificDimensions}, null, new Material[] {Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT}, new Environment[] {Environment.NORMAL}),
				new Tameable(new Material[] {Material.BEEF, Material.PORKCHOP, Material.CHICKEN, Material.MUTTON, Material.RABBIT}, new ItemStack(Material.SADDLE), true, "suchomimmus.saddle")
				));
		

		mobRegistry.put("ogre", new FancyMob("Ogre", 30, 0.2f, 1f, "ogre", EntityType.HUSK,
				FancyMob.NEUTRAL, new HashMap<>() {
					{
						put(Attribute.ARMOR, 5d);
						//put(Attribute.WATER_MOVEMENT_EFFICIENCY, 0.5);
					}
				},
				new PotionEffect[] {
						//new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 3, true, false)
					},
				Sound.sound(Key.key("yeet:ogre"), Source.HOSTILE, 1.0f, 1.0f),
				Sound.sound(Key.key("yeet:ogre_hurt"), Source.HOSTILE, 1.0f, 1.0f),
				Sound.sound(Key.key("yeet:ogre_hurt"), Source.HOSTILE, 1.0f, 1.0f),
				40,
				new Attack[] { 
						new Attack("attack", 7, new HashMap<>() {
							{
								put(Attack.SWEEP, 0.5);
							}
						}, 4, 600
					)
				},
				new Ability[] {
						//new Ability(Ability.SIT, "sitting", 2000),
				},
				new SpawnCondition(new int[] {SpawnCondition.onGround, SpawnCondition.specificDimensions, SpawnCondition.dark, SpawnCondition.specificBiomes}, new Biome[] {Biome.DRIPSTONE_CAVES, Biome.LUSH_CAVES}, new Material[] {Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT}, new Environment[] {Environment.NORMAL}),
				null
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
		
		loadSchematic("warrior24_factory.schem");

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
	
	@EventHandler
	public void onMountModel(MountModelEvent event) {
		Entity passenger = event.entity();
		LivingEntity le = (LivingEntity)event.getTracker().sourceEntity();
		FancyMob fm = getFancyMob(le);
		fm.mount(passenger);
	}
	
	@EventHandler
	public void onDismountMountModel(DismountModelEvent event) {
		Entity passenger = event.entity();
		LivingEntity le = (LivingEntity)event.getTracker().sourceEntity();
		FancyMob fm = getFancyMob(le);
		fm.dismount(passenger);
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
				for (org.bukkit.World world : Bukkit.getWorlds()) {
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
	
	private Clipboard clipboard;
	private final Set<Biome> coldBiomes = new HashSet<>(Set.of(
		    Biome.SNOWY_PLAINS,
		    Biome.SNOWY_TAIGA,
		    Biome.SNOWY_BEACH,
		    Biome.SNOWY_SLOPES,
		    Biome.GROVE,
		    Biome.ICE_SPIKES,
		    Biome.FROZEN_PEAKS,
		    Biome.JAGGED_PEAKS,
		    Biome.FROZEN_RIVER
		));
	
	private void loadSchematic(String fileName) {
        try {
            File schemFile = new File("plugins/WorldEdit/schematics/" + fileName);
            ClipboardFormat format = ClipboardFormats.findByFile(schemFile);
            try (ClipboardReader reader = format.getReader(new FileInputStream(schemFile))) {
                clipboard = reader.read();
                getLogger().info("Schematic loaded: " + fileName);
            }
        } catch (Exception e) {
            getLogger().severe("Failed to load schematic: " + e.getMessage());
            e.printStackTrace();
        }
    }
	
	@EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) return; // only generate in new chunks
        if (clipboard == null) return;
        if (Math.random() >= 0.02) return; // 2% chance per chunk

        Chunk chunk = event.getChunk();
        org.bukkit.World bukkitWorld = chunk.getWorld();

        // Skip Nether/End
        if (bukkitWorld.getEnvironment() != Environment.NORMAL) return;

        // Pick a random spot in this chunk
        int blockX = (chunk.getX() << 4) + (int) (Math.random() * 16);
        int blockZ = (chunk.getZ() << 4) + (int) (Math.random() * 16);

        // Find surface Y and check biome
        int surfaceY = bukkitWorld.getHighestBlockYAt(blockX, blockZ);
        Biome surfaceBiome = bukkitWorld.getBiome(blockX, surfaceY, blockZ);

        if (!coldBiomes.contains(surfaceBiome)) return;

        // Pick an underground Y position
        int pasteY = (int) (Math.random() * 20) + 20; // random Y between 20–40

        // Make sure it's not in air or water
        Material mat = bukkitWorld.getBlockAt(blockX, pasteY, blockZ).getType();
        if (mat.isAir() || mat == Material.WATER || mat == Material.LAVA) return;

        pasteStructure(bukkitWorld, blockX, pasteY, blockZ);
    }
	
	private void pasteStructure(org.bukkit.World bukkitWorld, int x, int y, int z) {
	    try {
	        World adaptedWorld = BukkitAdapter.adapt(bukkitWorld);

	        // Paste blocks
	        try (EditSession editSession = WorldEdit.getInstance().newEditSession(adaptedWorld)) {
	            ClipboardHolder holder = new ClipboardHolder(clipboard);
	            holder.createPaste(editSession)
	                    .to(BlockVector3.at(x, y, z))
	                    .ignoreAirBlocks(false)
	                    .copyEntities(true)
	                    .build();
	            editSession.flushSession();
	        }

	        getLogger().info("Spawned structure with entities at " + x + ", " + y + ", " + z);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}


}