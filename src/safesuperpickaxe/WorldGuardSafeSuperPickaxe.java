package safesuperpickaxe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;










import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;

public class WorldGuardSafeSuperPickaxe extends JavaPlugin implements Listener{

	private static String metadataKey="usingsspa";
	private static String successfulChangeMessage="§7[§aSafeSuperPickaxe§7]§aSuperPickaxe ";
	private List<Material> unbreakables=new ArrayList<Material>();
	private Consumer lbconsumer = null;
	private boolean logblock = false;
	private List<Integer> pickaxes=null;
	private boolean blockDrops=false;

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		final PluginManager pm=getServer().getPluginManager();
		final Plugin plugin=pm.getPlugin("LogBlock");
		if (plugin!=null)lbconsumer=((LogBlock)plugin).getConsumer();
		else logblock=false;
		this.getServer().getPluginManager().registerEvents(this,this);
		List<Integer> unbreakableIds=this.getConfig().getIntegerList("unbreakables");
		for(Integer i:unbreakableIds){
			Material m=Material.getMaterial(i.intValue());
			if(m!=null){
				unbreakables.add(m);
			}
		}
		pickaxes=this.getConfig().getIntegerList("pickaxes");
		blockDrops=this.getConfig().getBoolean("blockdrops");
		logblock=(lbconsumer!=null);
	}
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("sspa")){
			if(sender instanceof Player){
				Player player=(Player)sender;
				if(sender.hasPermission("sspa.use")){
					if(args.length==0){
						toggleUsing(player);
						return true;
					}
					else if(args.length==1){
						if(args[0].equalsIgnoreCase("on")){
							setUsing(player,true);
							return true;
						}
						if(args[0].equalsIgnoreCase("off")){
							setUsing(player,false);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	public void setUsing(Player player, boolean value){
		player.setMetadata(metadataKey,new FixedMetadataValue(this,value));
		if(value)player.sendMessage(successfulChangeMessage+"enabled.");
		else player.sendMessage(successfulChangeMessage+"disabled.");
	}
	public void toggleUsing(Player player){
		setUsing(player,!isUsing(player));
	}
	/**
	 * public boolean setUsing(Player player, String argument){
	 *
	 *	
	 *	player.setMetadata(metadataKey,new FixedMetadataValue(this,true));
	 *	return false;
	 *}
	 */
	public boolean isUsing(Player player){
		List<MetadataValue> values = player.getMetadata(metadataKey);  
		for(MetadataValue value : values){
			if(value.getOwningPlugin().getDescription().getName().equals(this.getDescription().getName())){
				return value.asBoolean();
			}
		}
		return false;
	}

	@Override
	public void onDisable(){

	}

	private WorldGuardPlugin getWorldGuard() {//COPY-PASTED from the Worldguard API website, removed other comments.
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null;
		}
		return (WorldGuardPlugin) plugin;
	}


	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		Player player=event.getPlayer();
		if (event.useItemInHand()==Result.DENY){
			return;
		}
		Action action=event.getAction();
		if(action==null)return;
		if(action==Action.LEFT_CLICK_BLOCK){

			Block block=event.getClickedBlock();
			if(pickaxes==null)return;
			if(pickaxes.contains(player.getItemInHand().getTypeId())){
				if(!unbreakables.contains(block.getType())){
					if(isUsing(player)){
						if(getWorldGuard().canBuild(player, block)){
							performSuperPicking(player, block);
							event.setCancelled(true);
						}
						else {
							player.sendMessage("§4You don't have permission for this area.");
						}
					}
				}
			}
		}
		return;
	}
	private void performSuperPicking(Player player,Block block){
		if(logblock)this.lbconsumer.queueBlockBreak(player.getName(),block.getState());
		if(this.blockDrops){
			Collection<ItemStack> drops=block.getDrops();
			World w=block.getWorld();
			Location l=block.getLocation();
			for(ItemStack i: drops){
				w.dropItem(l,i);
			}
		}
		block.setType(Material.AIR);
	}
}
