package com.nisovin.magicspells.util;

import org.bukkit.Material;
import org.bukkit.inventory.*;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataType;

import com.nisovin.magicspells.MagicSpells;

public class ItemUtil {

	public static void addFakeEnchantment(ItemMeta meta) {
		if (meta == null) return;
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addEnchant(Enchantment.FROST_WALKER, -1, true);
	}

	public static boolean hasFakeEnchantment(ItemMeta meta) {
		return meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)
			&& meta.hasEnchant(Enchantment.FROST_WALKER)
			&& meta.getEnchantLevel(Enchantment.FROST_WALKER) == 65535;
	}

	public static boolean hasDurability(Material type) {
		String name = type.name().toUpperCase();
		return
				name.contains("HELMET") ||
				name.contains("CHESTPLATE") ||
				name.contains("LEGGINGS") ||
				name.contains("BOOTS") ||
				name.contains("PICKAXE") ||
				name.contains("SHOVEL") ||
				name.contains("AXE") ||
				name.contains("HOE") ||
				name.contains("SWORD") ||
				name.contains("FISHING_ROD") ||
				name.contains("CARROT_ON_A_STICK") ||
				name.contains("FLINT_AND_STEEL") ||
				name.contains("BOW") ||
				name.contains("CROSSBOW") ||
				name.contains("TRIDENT") ||
				name.contains("ELYTRA") ||
				name.contains("SHIELD") ||
				name.contains("WARPED_FUNGUS_ON_A_STICK") ||
				name.contains("SHEARS");
	}

	public static int getDurability(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (!(meta instanceof Damageable)) return 0;
		return ((Damageable) meta).getDamage();
	}

	public static int getCustomModelData(ItemMeta meta) {
		if (meta == null) return 0;
		if (meta.hasCustomModelData()) return meta.getCustomModelData();
		return 0;
	}

	public static Recipe createCookingRecipe(String type, NamespacedKey namespaceKey, String group, ItemStack result, Material ingredient, float experience, int cookingTime) {
		CookingRecipe<?> recipe = null;

		switch (type.toLowerCase()) {
			case "smoking" -> recipe = new SmokingRecipe(namespaceKey, result, ingredient, experience, cookingTime);
			case "campfire" -> recipe = new CampfireRecipe(namespaceKey, result, ingredient, experience, cookingTime);
			case "blasting" -> recipe = new BlastingRecipe(namespaceKey, result, ingredient, experience, cookingTime);
		}

		if (recipe != null) recipe.setGroup(group);
		return recipe;
	}

	public static Recipe createStonecutterRecipe(NamespacedKey namespaceKey, String group, ItemStack result, Material ingredient) {
		StonecuttingRecipe recipe = new StonecuttingRecipe(namespaceKey, result, ingredient);
		recipe.setGroup(group);
		return recipe;
	}

	private static NamespacedKey createKey(String key) {
		return new NamespacedKey(MagicSpells.getInstance(), key);
	}

	public static String getPersistentString(ItemStack item, String key) {
		return item.getItemMeta().getPersistentDataContainer().get(createKey(key), PersistentDataType.STRING);
	}

	public static void setPersistentString(ItemStack item, String key, String value) {
		ItemMeta meta = item.getItemMeta();
		meta.getPersistentDataContainer().set(createKey(key), PersistentDataType.STRING, value);
		item.setItemMeta(meta);
	}

	public static void removePersistentString(ItemStack item, String key) {
		ItemMeta meta = item.getItemMeta();
		meta.getPersistentDataContainer().remove(createKey(key));
		item.setItemMeta(meta);
	}

}
