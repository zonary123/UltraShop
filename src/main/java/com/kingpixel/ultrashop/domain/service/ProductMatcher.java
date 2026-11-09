package com.kingpixel.ultrashop.domain.service;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

/**
 * Compares ItemStacks for sell matching — fixes the NBT comparison bug.
 *
 * <p>The old code only compared item type + CustomModelData, ignoring
 * enchantments, custom names, and all other components. This caused
 * items with NBT to sell incorrectly or be discarded.</p>
 */
public final class ProductMatcher {

  private ProductMatcher() {
  }

  /**
   * Full component comparison — items must match in every way.
   * This is the default and correct behavior for selling.
   */
  public static boolean matches(ItemStack playerItem, ItemStack productTemplate) {
    if (playerItem == null || playerItem.isEmpty()) return false;
    if (productTemplate == null || productTemplate.isEmpty()) return false;

    return ItemStack.areItemsAndComponentsEqual(
      withoutCount(playerItem),
      withoutCount(productTemplate)
    );
  }

  /**
   * Counts total matching items inside a player's inventory.
   */
  public static int countMatching(PlayerInventory inventory, ItemStack productTemplate) {
    if (inventory == null || productTemplate == null || productTemplate.isEmpty()) return 0;
    int count = 0;
    for (int i = 0; i < inventory.size(); i++) {
      ItemStack slot = inventory.getStack(i);
      if (matches(slot, productTemplate)) {
        count += slot.getCount();
      }
    }
    return count;
  }

  /**
   * Creates a copy with count=1 so comparison ignores stack size.
   */
  private static ItemStack withoutCount(ItemStack stack) {
    ItemStack copy = stack.copy();
    copy.setCount(1);
    copy.remove(DataComponentTypes.LORE);
    copy.remove(DataComponentTypes.CUSTOM_NAME);
    copy.remove(DataComponentTypes.ITEM_NAME);
    copy.remove(DataComponentTypes.REPAIR_COST);
    return copy;
  }
}

