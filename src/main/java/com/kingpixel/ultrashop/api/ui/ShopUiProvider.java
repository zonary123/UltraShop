package com.kingpixel.ultrashop.api.ui;

import com.kingpixel.ultrashop.domain.model.ActionShop;
import com.kingpixel.ultrashop.domain.model.Product;
import com.kingpixel.ultrashop.domain.model.shop.Shop;
import com.kingpixel.ultrashop.infrastructure.config.ShopConfig;
import com.kingpixel.ultrashop.presentation.gui.NavigationContext;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

/**
 * Service Provider Interface (SPI) for presenting shop user interfaces.
 *
 * <p>Addon mods can implement this interface to replace default GooeyLibs menus
 * with custom UI implementations, custom layouts, or alternative screen libraries.</p>
 */
public interface ShopUiProvider {

  /**
   * Opens the main category/shop selection menu for a player.
   *
   * @param player the player viewing the menu
   * @param config the shop configuration
   * @param modId  the mod identifier
   */
  void openMainMenu(ServerPlayerEntity player, ShopConfig config, String modId);

  /**
   * Opens a specific shop menu for a player.
   *
   * @param player    the player viewing the shop
   * @param shop      the shop being opened
   * @param nav       the navigation breadcrumb context
   * @param config    the shop configuration
   * @param withClose whether the close button should be shown
   */
  void openShop(ServerPlayerEntity player, Shop shop, NavigationContext nav, ShopConfig config, boolean withClose);

  /**
   * Opens the buy or sell quantity selection / confirmation menu.
   *
   * @param player     the player performing the transaction
   * @param nav        the navigation breadcrumb context
   * @param product    the product being bought or sold
   * @param amount     the quantity to buy or sell
   * @param actionShop the transaction type (BUY or SELL)
   * @param config     the shop configuration
   * @param withClose  whether the close button should be shown
   */
  default void openBuySell(ServerPlayerEntity player, NavigationContext nav, Product product,
                           int amount, ActionShop actionShop, ShopConfig config, boolean withClose) {
    ShopUiRegistry.getDefaultProvider().openBuySell(player, nav, product, amount, actionShop, config, withClose);
  }

  /**
   * Opens the transaction history menu for a player.
   *
   * @param player     the player viewing the history
   * @param targetUuid the UUID of the target player whose history is viewed
   * @param targetName the name of the target player
   * @param config     the shop configuration
   * @param modId      the mod identifier
   */
  default void openTransactions(ServerPlayerEntity player, UUID targetUuid, String targetName,
                                ShopConfig config, String modId) {
    ShopUiRegistry.getDefaultProvider().openTransactions(player, targetUuid, targetName, config, modId);
  }

  /**
   * Opens the product search result menu for a player.
   *
   * @param player the player searching products
   * @param query  the search query
   * @param modId  the mod identifier
   */
  default void openSearch(ServerPlayerEntity player, String query, String modId) {
    ShopUiRegistry.getDefaultProvider().openSearch(player, query, modId);
  }

  /**
   * Opens the revenue and product statistics menu for a player.
   *
   * @param player the player viewing statistics
   * @param config the shop configuration
   * @param modId  the mod identifier
   */
  default void openStats(ServerPlayerEntity player, ShopConfig config, String modId) {
    ShopUiRegistry.getDefaultProvider().openStats(player, config, modId);
  }

  /**
   * Opens the inventory bulk-sell GUI for a player.
   *
   * @param player the player opening the sell GUI
   */
  default void openSellGui(ServerPlayerEntity player) {
    ShopUiRegistry.getDefaultProvider().openSellGui(player);
  }
}
