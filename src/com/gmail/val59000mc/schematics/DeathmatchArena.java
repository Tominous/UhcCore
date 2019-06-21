package com.gmail.val59000mc.schematics;

import com.gmail.val59000mc.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DeathmatchArena {
	private Location loc;
	private File arenaSchematic;
	private boolean enable;
	private List<Location> teleportSpots;
	private boolean built;
	protected static int width, length, height;
	
	public DeathmatchArena(Location loc){
		this.loc = loc;
		this.built = false;
		this.teleportSpots = new ArrayList<Location>();
		this.enable = GameManager.getGameManager().getConfiguration().getEndWithDeathmatch();
		teleportSpots.add(loc);
		checkIfSchematicCanBePasted(); 
	}
	
	private void checkIfSchematicCanBePasted() {
		if(GameManager.getGameManager().getConfiguration().getWorldEditLoaded()){
			arenaSchematic = SchematicHandler.getSchematicFile("arena");
        	if(!arenaSchematic.exists()){
        		if(enable){
        			enable = false;
        			Bukkit.getLogger().severe("[UHC Core] Arena schematic not found in 'plugins/UhcCore/arena.schematic'. There will be no deathmatch arena");
        			GameManager.getGameManager().getConfiguration().disableEndWithDeathmatch();
        		}      
        	}
		}else{
			GameManager.getGameManager().getConfiguration().disableEndWithDeathmatch();
			enable = false;
		}
	}

	public void build(){
		if(enable){
			if(!built){
				
				ArrayList<Integer> dimensions;
				try {
					dimensions = SchematicHandler.pasteSchematic(loc, arenaSchematic);
					DeathmatchArena.height = dimensions.get(0);
					DeathmatchArena.length = dimensions.get(1);
					DeathmatchArena.width = dimensions.get(2);
					built = true;
				} catch (Exception e) {
					Bukkit.getLogger().severe("[UHC Core] An error ocurred while pasting the arena");
					built = false;
				}
			}  
				
			if(built){
				calculateTeleportSpots();
			}else{
				Bukkit.getLogger().severe("[UHC Core] Disabling end with deathmatch feature");
				GameManager.getGameManager().getConfiguration().disableEndWithDeathmatch();
			}
		}
	}

	public Location getLoc() {
		return loc;
	}

	public boolean isBuilt() {
		return built;
	}

	public int getMaxSize() {
		return Math.max(DeathmatchArena.length, DeathmatchArena.width);
	}
	
	public void calculateTeleportSpots(){
		List<Location> spots = new ArrayList<Location>();
		int x = loc.getBlockX(),
			y = loc.getBlockY(),
			z = loc.getBlockZ();
		
		Material spotMaterial = GameManager.getGameManager().getConfiguration().getDeathmatchTeleportSpotBLock();
		
		for(int i = x - width ; i < x + width ; i++ ){
			for(int j = y - height ; j < y + height ; j++ ){
				for(int k = z - length ; k < z + length ; k++ ){
					Block block = loc.getWorld().getBlockAt(i, j, k);
					if(block.getType().equals(spotMaterial)){
						spots.add(block.getLocation().clone().add(0.5, 1, 0.5));
						Bukkit.getLogger().info("[UHC Core] Arena teleport spot found at "+i+" "+(j+1)+" "+k);
					}
				}
			}
		}
		
		if(spots.isEmpty()){
			Bukkit.getLogger().info("[UHC Core] No Arena teleport spot found, defaulting to schematic origin");
		}else{
			teleportSpots = spots;
		}
	}
	
	public List<Location> getTeleportSpots(){
		return teleportSpots;
	}

	public void loadChunks() {
		if(enable){
			World world = getLoc().getWorld();
			for(Location loc : teleportSpots){
				world.loadChunk(loc.getChunk());
			}
		}
	}
}
