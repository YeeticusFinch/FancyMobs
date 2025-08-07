package com.lerdorf.fancymobs;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Tameable {

	public Material[] tameMaterial = null;
	public ItemStack saddleItem;
	public boolean saddleable;
	public boolean tamed = false;
	
	public Tameable(Material[] tameMaterial, ItemStack saddleItem, boolean saddleable) {
		this.tameMaterial = tameMaterial;
		this.saddleItem = saddleItem;
		this.saddleable = saddleable;
	}

	public void tryTame(Player player) {
		// TODO Auto-generated method stub
		ItemStack item = player.getEquipment().getItemInMainHand();
		if (item != null) {
			if (isTameMaterial(item)) {
				item.setAmount(item.getAmount()-1);
				
			}
		}
	}
	
	boolean isTameMaterial(ItemStack item) {

		for (Material mat : tameMaterial) {
			if (mat == item.getType()) {
				return true;
			}
		}
		return false;
	}
	
}
