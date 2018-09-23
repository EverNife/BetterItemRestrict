package net.kaikk.mc.itemrestrict.bukkit;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.kaikk.mc.itemrestrict.ChunkIdentifier;

public class BetterItemRestrict extends JavaPlugin {
	private static BetterItemRestrict instance;
	private Config config;
	private Executor executor = Executors.newSingleThreadExecutor();
	private Set<ChunkIdentifier> checkedChunks = new HashSet<>();

	@Override
	public void onEnable() {
		instance = this;
		config = new Config(this);

		this.getServer().getPluginManager().registerEvents(new EventListener(this), this);

		// commands executor
		CommandExec ce = new CommandExec(this);
		for (String command : this.getDescription().getCommands().keySet()) {
			this.getCommand(command).setExecutor(ce);
		}

		// scan players inventories every 10 ticks
		new BukkitRunnable() {
			@Override
			public void run() {
				for(Player p : Bukkit.getOnlinePlayers()) {
					inventoryCheck(p);
				}
			}
		}.runTaskTimer(this, 200L, 10L);
	}

	public Config config() {
		return config;
	}

	public static BetterItemRestrict instance() {
		return instance;
	}

	public RestrictedItem restricted(Block block) {
		for (RestrictedItem ri : config.ownership.get(block.getType())) {
			if (ri.isRestricted(block)) {
				return ri;
			}
		}
		for (RestrictedItem ri : config.usage.get(block.getType())) {
			if (ri.isRestricted(block)) {
				return ri;
			}
		}

		return null;
	}

	public RestrictedItem restricted(ItemStack itemStack) {
		for (RestrictedItem ri : config.ownership.get(itemStack.getType())) {
			if (ri.isRestricted(itemStack)) {
				return ri;
			}
		}
		for (RestrictedItem ri : config.usage.get(itemStack.getType())) {
			if (ri.isRestricted(itemStack)) {
				return ri;
			}
		}

		return null;
	}

	public RestrictedItem usageRestricted(Block block) {
		for (RestrictedItem ri : config.usage.get(block.getType())) {
			if (ri.isRestricted(block)) {
				return ri;
			}
		}

		return null;
	}

	public RestrictedItem usageRestricted(ItemStack itemStack) {
		for (RestrictedItem ri : config.usage.get(itemStack.getType())) {
			if (ri.isRestricted(itemStack)) {
				return ri;
			}
		}

		return null;
	}

	public RestrictedItem ownershipRestricted(ItemStack itemStack) {
		for (RestrictedItem ri : config.ownership.get(itemStack.getType())) {
			if (ri.isRestricted(itemStack)) {
				return ri;
			}
		}

		return null;
	}

	public void checkChunk(Chunk chunk) {
		if (!this.config().world.isEmpty() && this.checkedChunks.add(new ChunkIdentifier(chunk))) {
			this.executor.execute(new ChunkChecker(chunk));
		}
	}


	public boolean check(HumanEntity player, ItemStack itemStack) {
		if (itemStack == null || itemStack.getType() == Material.AIR) {
			return false;
		}

		if (player.hasPermission("betteritemrestrict.bypass") || player.hasPermission("betteritemrestrict.bypass."+itemStack.getType())) {
			return false;
		}

		RestrictedItem ri = this.restricted(itemStack);
		if (ri==null) {
			return false;
		}

		this.notify(player, ri);
		return true;
	}

	public boolean check(HumanEntity player, Block block) {
		if (block == null || block.getType() == Material.AIR) {
			return false;
		}

		if (player.hasPermission("betteritemrestrict.bypass") || player.hasPermission("betteritemrestrict.bypass."+block.getType())) {
			return false;
		}

		RestrictedItem ri = this.restricted(block);
		if (ri==null) {
			return false;
		}

		this.notify(player, ri);
		return true;
	}

	public boolean check(HumanEntity player, Block block, ItemStack itemStack) {
		if (player.hasPermission("betteritemrestrict.bypass")) {
			return false;
		}

		RestrictedItem ri;
		if (block!=null && block.getType() != Material.AIR && !player.hasPermission("betteritemrestrict.bypass."+block.getType())) {
			ri = this.restricted(block);
			if (ri!=null) {
				this.notify(player, ri);
				return true;
			}
		}

		if (itemStack!=null && itemStack.getType() != Material.AIR && !player.hasPermission("betteritemrestrict.bypass."+itemStack.getType())) {
			ri = this.restricted(itemStack);
			if (ri!=null) {
				this.notify(player, ri);
				return true;
			}
		}

		return false;
	}

	public boolean ownershipCheck(HumanEntity player, ItemStack itemStack) {
		if (itemStack == null || itemStack.getType() == Material.AIR) {
			return false;
		}

		if (player.hasPermission("betteritemrestrict.bypass") || player.hasPermission("betteritemrestrict.bypass."+itemStack.getType())) {
			return false;
		}

		RestrictedItem ri = this.ownershipRestricted(itemStack);
		if (ri==null) {
			return false;
		}

		this.notify(player, ri);
		return true;
	}

	public void inventoryCheck(HumanEntity player) {
		if (player.hasPermission("betteritemrestrict.bypass")) {
			return;
		}

		final ItemStack[] inv = player.getInventory().getContents();
		for (int i = 0; i < inv.length; i++) {
			if (this.ownershipCheck(player, inv[i])) {
				this.getLogger().info("Removendo "+inv[i]+" i:"+i+" do jogador "+player.getName());
				player.getInventory().setItem(i, null);

			}
		}
	}

	public void notify(HumanEntity player, RestrictedItem restrictedItem) {
		this.getLogger().info(player.getName()+" @ "+locationToString(player.getLocation())+" tried to own/use "+restrictedItem);

		for (Player player2 : Bukkit.getServer().getOnlinePlayers()) {
			if (player2.hasPermission("betteritemrestrict.notify")) {
				player2.sendMessage(ChatColor.ITALIC + "" + ChatColor.GRAY + player.getName() + " @ " + player.getLocation() + " in " + player.getWorld() + ", tried to own/use " + restrictedItem.label);
			}
		}
		if (player instanceof CommandSender) {
			CommandSender commandSender = ((CommandSender) player);
			commandSender.sendMessage("   §c§l✘ Item Banido ✘");
			commandSender.sendMessage("   §c§l[§4§l" + restrictedItem.label + "§c§l]");
			commandSender.sendMessage("   §c§l➸ §c" + restrictedItem.reason);
			commandSender.sendMessage("");
			player.getWorld().playSound(player.getLocation(), Sound.ITEM_BREAK, 10, 1);
		}
	}

	public static String locationToString(Location location) {
		return "[" + location.getWorld().getName() + ", " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "]";
	}
}
