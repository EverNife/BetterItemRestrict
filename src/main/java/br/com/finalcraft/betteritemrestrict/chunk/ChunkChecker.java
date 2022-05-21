package br.com.finalcraft.betteritemrestrict.chunk;

import br.com.finalcraft.betteritemrestrict.BetterItemRestrict;
import br.com.finalcraft.betteritemrestrict.config.ConfigManager;
import br.com.finalcraft.betteritemrestrict.restrictdata.RestrictedItem;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;


public class ChunkChecker extends Thread {
	final private Chunk chunk;
	final private int yMax;

	public ChunkChecker(Chunk chunk) {
		super("BetterItemRestrict-ChunkChecker");
		this.chunk = chunk;
		this.yMax = chunk.getWorld().getMaxHeight();
	}

	@Override
	public void run() {
		final List<Block> toBeRemoved = new ArrayList<>();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 0; y < yMax; y++) {
					final Block block = chunk.getBlock(x, y, z);
					if (block.getType() != Material.AIR){ // don't need to check air :/
						for (RestrictedItem ri : ConfigManager.world.get(block.getType())) {
							if (ri.isRestricted(block)) {
								toBeRemoved.add(block);
								break;
							}
						}
					}
				}
			}
		}

		if (!toBeRemoved.isEmpty()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					for (Block block : toBeRemoved) {
						BetterItemRestrict.instance.getLogger().info("Removing "+block.getType()+" at "+ BetterItemRestrict.locationToString(block.getLocation()));
						block.setType(Material.AIR);
					}
				}
			}.runTask(BetterItemRestrict.instance);
		}
	}
}
