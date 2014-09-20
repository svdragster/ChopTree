package de.svdragster.choptree;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.Enchantment;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.inventory.ItemType;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockFace;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.BlockDestroyHook;
import net.canarymod.hook.player.ConnectionHook;
import net.canarymod.plugin.PluginListener;

public class ChopTreeListener implements PluginListener {

	String VERSION = new ChopTree().getVersion();
	
	@HookHandler
	public void onBlockDestroy(BlockDestroyHook hook) {
		Block block = hook.getBlock();
		Player player = hook.getPlayer();
		if (isLog(block)) {
			Item hand = player.getItemHeld();
			if (hand != null && isAxe(hand)) {
				if (player.hasPermission("choptree.chop")) {
					checkSurrounding(block, new ArrayList<Block>(), player);
				}
			}
		}
	}
	
	public boolean isAxe(Item item) {
		ItemType type = item.getType();
		if (type.equals(ItemType.WoodAxe) || type.equals(ItemType.StoneAxe) || type.equals(ItemType.IronAxe) || type.equals(ItemType.DiamondAxe) || type.equals(ItemType.GoldAxe)) {
			return true;
		}
		return false;
	}
	
	public boolean isLog(Block block) {
		if (block.getTypeId() == 17 || block.getTypeId() == 162) { // 17 are the old logs, 162 the new logs introduced in Minecraft version 1.7
			return true;
		}
		return false;
	}
	
	// Checks if this is a valid blocktype
	public boolean isValid(Block block) {
		if (isLog(block) || isLeaf(block) || block.isAir() || block.getType().equals(BlockType.Vines)) {
			return true;
		}
		return false;
	}
	
	// Checks if this log is from a tree, and not from a house 
	public boolean isTreeLog(Block block) {
		Block top = block.getFacingBlock(BlockFace.TOP);
		Block north = block.getFacingBlock(BlockFace.NORTH);
		Block east = block.getFacingBlock(BlockFace.EAST);
		Block south = block.getFacingBlock(BlockFace.SOUTH);
		Block west = block.getFacingBlock(BlockFace.WEST);
		if ((isValid(top)) && (isValid(north)) && (isValid(east)) && (isValid(south)) && (isValid(west))) {
			return true;
		}
		return false;
	}
	
	public boolean isLeaf(Block block) {
		if (block.getTypeId() == 18 || block.getTypeId() == 161) {
			return true;
		}
		return false;
	}
	
	// Checks if there is at least one leaf around one of the blocks
	public boolean hasLeaves(ArrayList<Block> blocks) {
		for (int i=0; i<blocks.size(); i++) {
			Block block = blocks.get(i);
			Block top = block.getFacingBlock(BlockFace.TOP);
			Block north = block.getFacingBlock(BlockFace.NORTH);
			Block east = block.getFacingBlock(BlockFace.EAST);
			Block south = block.getFacingBlock(BlockFace.SOUTH);
			Block west = block.getFacingBlock(BlockFace.WEST);
			if (isLeaf(top) || isLeaf(north) || isLeaf(east) || isLeaf(south) || isLeaf(west)) {
				return true;
			}
		}
		return false;
	}
	
	public void Chop(ArrayList<Block> toChop, Player player) {
		if (hasLeaves(toChop)) {
			for (int i=0; i<toChop.size(); i++) {
				Block block = toChop.get(i);
				block.dropBlockAsItem(true);
			}
			Item hand = player.getItemHeld();
			int damage = hand.getDamage();
			int maxdamage = hand.getBaseItem().getMaxDamage();
			int toAdd = toChop.size();
			Enchantment[] enchantments = hand.getEnchantments();
			if (enchantments != null) {
				for (int i=0; i<enchantments.length; i++) {
					Enchantment ench = enchantments[i];
					if (ench.getType().equals(Enchantment.Type.Unbreaking)) {
						toAdd = toAdd / ench.getLevel(); //On average, lifetime of a tool is (Level+1) times as long.
					}
				}
			}
			int totalDamage = damage + toAdd;
			if (totalDamage >= maxdamage) {
				player.getInventory().setSlot(hand.getSlot(), (Item) null);
			} else {
				hand.setDamage(totalDamage);
			}
		}
	}
	
	// Checks if there is a log around block
	public void checkSurrounding(Block block, ArrayList<Block> checked, Player player) {
		int count = 0;
		Block front = block.getRelative(1, 0, 0);
		Block frontright = block.getRelative(1, 0, 1);
		Block right = block.getRelative(0, 0, 1);
		Block bottomright = block.getRelative(-1, 0, 1);
		Block bottom = block.getRelative(-1, 0, 0);
		Block bottomleft = block.getRelative(-1, 0, -1);
		Block left= block.getRelative(0, 0, -1);
		Block frontleft = block.getRelative(1, 0, -1);
		Block top = block.getRelative(0, 1, 0);
		Block down = block.getRelative(0, -1, 0);
		if ((!checked.contains(front)) && isLog(front) && isTreeLog(front)) {
			checked.add(front);
			checkSurrounding(front, checked, player);
			count++;
		}
		if ((!checked.contains(frontright)) && isLog(frontright) && isTreeLog(frontright)) {
			checked.add(frontright);
			checkSurrounding(frontright, checked, player);
			count++;
		}
		if ((!checked.contains(right)) && isLog(right) && isTreeLog(right)) {
			checked.add(right);
			checkSurrounding(right, checked, player);
			count++;
		}
		if ((!checked.contains(bottomright)) && isLog(bottomright) && isTreeLog(bottomright)) {
			checked.add(bottomright);
			checkSurrounding(bottomright, checked, player);
			count++;
		}
		if ((!checked.contains(bottom)) && isLog(bottom) && isTreeLog(bottom)) {
			checked.add(bottom);
			checkSurrounding(bottom, checked, player);
			count++;
		}
		if ((!checked.contains(bottomleft)) && isLog(bottomleft) && isTreeLog(bottomleft)) {
			checked.add(bottomleft);
			checkSurrounding(bottomleft, checked, player);
			count++;
		}
		if ((!checked.contains(left)) && isLog(left) && isTreeLog(left)) {
			checked.add(left);
			checkSurrounding(left, checked, player);
			count++;
		}
		if ((!checked.contains(frontleft)) && isLog(frontleft) && isTreeLog(frontleft)) {
			checked.add(frontleft);
			checkSurrounding(frontleft, checked, player);
			count++;
		}
		if ((!checked.contains(top)) && isLog(top) && isTreeLog(top)) {
			checked.add(top);
			checkSurrounding(top, checked, player);
			count++;
		}
		if ((!checked.contains(down)) && isLog(down) && isTreeLog(down)) {
			checked.add(down);
			checkSurrounding(down, checked, player);
			count++;
		}
		if (count < 1) {
			Chop(checked, player);
		}
	}
	
	@HookHandler
	  public void onLogin(ConnectionHook hook) {
		  if (hook.getPlayer().hasPermission("choptree.checkforupdates")) {
				try {
					String result = sendGet(hook.getPlayer().getName());
					if ((result != null) && (!result.isEmpty())) {
						hook.getPlayer().message(result);
						hook.getPlayer()
								.message("Or you can check the forum post.");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	  }
	  
	  public String sendGet(String playername) throws Exception { 
			String MYIDSTART = "svdragster>";
			String MYIDEND = "<svdragster";
			String url = "http://svdragster.dtdns.net/checkupdate.php?version=" + VERSION
					+ "&plugin=choptree&motd=" + playername;

			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			con.setRequestMethod("GET");

			con.setRequestProperty("User-Agent", "canary_minecraft");

			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));

			StringBuffer response = new StringBuffer();
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			String result = response.toString();
			if ((result.contains(MYIDSTART)) && (result.contains(MYIDEND))) {
				int endPos = result.indexOf(MYIDEND);
				result = "§6[ChopTree] §2Update available at: §f"
						+ result.substring(MYIDSTART.length(), endPos);
			}
			return result;
		}
	
}
