package br.com.finalcraft.betteritemrestrict.config;

import br.com.finalcraft.betteritemrestrict.restrictdata.RestrictedItem;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ConfigManager {
	public static Multimap<Material, RestrictedItem> usage = HashMultimap.create();
	public static Multimap<Material, RestrictedItem> ownership = HashMultimap.create();
	public static Multimap<Material, RestrictedItem> world = HashMultimap.create();

	public static void initialize(JavaPlugin instance){

		FileConfiguration config = instance.getConfig();
		instance.reloadConfig();

		usage.clear();
		ownership.clear();
		world.clear();

		// load config
		copyAsset(instance, "config.yml");
		instance.reloadConfig();

		int ownershipCount = 0;
		int usageCount = 0;
		int worldCount = 0;
		for(String bannedItem : config.getStringList("Ownership")) {
			try {
				RestrictedItem ri = RestrictedItem.deserialize(bannedItem);
				ownership.put(ri.material, ri);
				ownershipCount++;
			} catch (IllegalArgumentException e) {
				instance.getLogger().warning(e.getMessage());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		for(String bannedItem : config.getStringList("Usage")) {
			try {
				RestrictedItem ri = RestrictedItem.deserialize(bannedItem);
				if (!ownership.get(ri.material).contains(ri)) {
					usage.put(ri.material, ri);
				}
				usageCount++;
			} catch (IllegalArgumentException e) {
				instance.getLogger().warning(e.getMessage());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}


		for(String bannedItem : config.getStringList("World")) {
			try {
				RestrictedItem ri = RestrictedItem.deserialize(bannedItem);
				world.put(ri.material, ri);
				worldCount++;
			} catch (IllegalArgumentException e) {
				instance.getLogger().warning(e.getMessage());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		instance.getLogger().info("Loaded " + usageCount + " usage, " + ownershipCount + " ownership, and " + worldCount + " world restrictions.");
	}

	public static File copyAsset(JavaPlugin instance, String assetName) {
		File file = new File(instance.getDataFolder(), assetName);
		file.getParentFile().mkdirs();
		if (!file.exists()) {
			try {
				Files.copy(getAsset(instance, assetName), file.getAbsoluteFile().toPath(), new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
			} catch (IOException var4) {
				throw new RuntimeException(var4);
			}
		}

		return file;
	}

	public static InputStream getAsset(JavaPlugin instance, String assetName) {
		return instance.getResource("assets/" + instance.getName().toLowerCase() + "/" + assetName);
	}

}
