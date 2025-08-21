package com.lerdorf.fancymobs;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Tameable {

	public Material[] tameMaterial = null;
	public ItemStack[] tameItems = null;
	public ItemStack saddleItem;
	public boolean saddleable;
	public boolean tamed = false;
	public float tameChance = 0.3f;
	public String saddleModel;
	public boolean saddled = false;
	
	public Tameable(Material[] tameMaterial, ItemStack saddleItem, boolean saddleable, String saddleModel) {
		this.tameMaterial = tameMaterial;
		this.saddleItem = saddleItem;
		this.saddleable = saddleable;
		this.saddleModel = saddleModel;
	}
	
	public Tameable(Material[] tameMaterial, ItemStack saddleItem, boolean saddleable, String saddleModel, float tameChance) {
		this.tameMaterial = tameMaterial;
		this.saddleItem = saddleItem;
		this.saddleable = saddleable;
		this.tameChance = tameChance;
		this.saddleModel = saddleModel;
	}

	public Tameable(ItemStack[] tameItems, ItemStack saddleItem, boolean saddleable, String saddleModel) {
		this.tameItems = tameItems;
		this.saddleItem = saddleItem;
		this.saddleable = saddleable;
		this.saddleModel = saddleModel;
	}

	public int tryTame(Player player) {
		// TODO Auto-generated method stub
		ItemStack item = player.getEquipment().getItemInMainHand();
		if (item != null) {
			if (isTameMaterial(item)) {
				item.setAmount(item.getAmount()-1);
				if (Math.random() < tameChance) {
					tamed = true;
					return 1;
				} else {
					return 0;
				}
			}
		}
		return -1;
	}
	
	public boolean isCorrectSaddle(ItemStack item) {
		if (item != null && saddleItem != null && saddleable) {
			if (item.getType() == saddleItem.getType()) {
				if ((item.getItemMeta().getItemModel() != null) == (saddleItem.getItemMeta().getItemModel() != null)) {
					if (item.getItemMeta().getItemModel() != null && item.getItemMeta().getItemModel().getNamespace().equalsIgnoreCase(saddleItem.getItemMeta().getItemModel().getNamespace())) {
						return true;
					} else if (item.getItemMeta().getItemModel() == null) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	boolean isTameMaterial(ItemStack item) {

		if (tameMaterial != null && tameMaterial.length > 0) {
			for (Material mat : tameMaterial) {
				if (mat == item.getType()) {
					return true;
				}
			}
		}
		if (tameItems != null && tameItems.length > 0) {
			for (ItemStack match : tameItems) {
				if (match.getType() == item.getType() && (match.hasItemMeta() == item.hasItemMeta() && (match.hasItemMeta() == false || (match.getItemMeta().getItemModel().asMinimalString().equalsIgnoreCase(item.getItemMeta().getItemModel().asMinimalString()))))) {
					return true;
				}
			}
		}
		return false;
	}
	
}
