package com.kingpixel.ultrashop.api;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.ultrashop.ShopContext;
import com.kingpixel.ultrashop.UltraShop;
import com.kingpixel.ultrashop.api.ui.ShopUiProvider;
import com.kingpixel.ultrashop.api.ui.ShopUiRegistry;
import com.kingpixel.ultrashop.domain.model.ActionShop;
import com.kingpixel.ultrashop.domain.model.Product;
import com.kingpixel.ultrashop.domain.model.shop.Shop;
import com.kingpixel.ultrashop.domain.service.TransactionService;
import com.kingpixel.ultrashop.infrastructure.config.ConfigLoader;
import com.kingpixel.ultrashop.infrastructure.config.ShopConfig;
import com.kingpixel.ultrashop.migrate.V1ToV2Migrator;
import com.kingpixel.ultrashop.presentation.command.CommandTree;
import com.kingpixel.ultrashop.presentation.gui.NavigationContext;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Public API for UltraShop — main facade for addons and integrations.
 */
public final class ShopApi {

  private ShopApi() {
  }

  /**
   * Register a shop system for a mod. Called during command registration.
   */
  public static void register(ShopOptionsApi options, CommandDispatcher<ServerCommandSource> dispatcher) {
    if (options.getUiProvider() != null) {
      ShopUiRegistry.register(options.getModId(), options.getUiProvider());
    }
    Path shopDir = CobbleUtils.getPath().resolve(options.getPath()).resolve("shop");
    V1ToV2Migrator.migrateIfNeeded(shopDir);
    ConfigLoader.load(options);
    ShopContext.get().startDashboard();
    CommandTree.register(options, dispatcher);
  }

  /**
   * Reload shops for a specific mod.
   */
  public static void reload(ShopOptionsApi options) {
    ConfigLoader.load(options);
  }

  /**
   * Get the config for a specific mod.
   */
  public static ShopConfig getConfig(String modId) {
    return ShopContext.get().getConfigs().get(modId);
  }

  /**
   * Get the main UltraShop config.
   */
  public static ShopConfig getMainConfig() {
    return ShopContext.get().getMainConfig();
  }

  /**
   * Get all shops for a mod.
   */
  public static List<Shop> getShops(String modId) {
    return ShopContext.get().getTypedShops(modId);
  }

  /**
   * Find a shop by id.
   */
  public static Shop getShop(String modId, String shopId) {
    return ShopContext.get().getTypedShops(modId).stream()
      .filter(s -> s.getId().equals(shopId))
      .findFirst().orElse(null);
  }

  /**
   * Register a custom UI provider for a specific mod.
   */
  public static void registerUiProvider(String modId, ShopUiProvider provider) {
    ShopUiRegistry.register(modId, provider);
  }

  /**
   * Unregister a custom UI provider for a specific mod.
   */
  public static void unregisterUiProvider(String modId) {
    ShopUiRegistry.unregister(modId);
  }

  /**
   * Get the active UI provider for a specific mod.
   */
  public static ShopUiProvider getUiProvider(String modId) {
    return ShopUiRegistry.get(modId);
  }

  /**
   * Override the global default UI provider.
   */
  public static void setDefaultUiProvider(ShopUiProvider provider) {
    ShopUiRegistry.setDefaultProvider(provider);
  }

  /**
   * Opens the main menu for a player using UltraShop's configuration.
   */
  public static void openMainMenu(ServerPlayerEntity player) {
    openMainMenu(player, UltraShop.MOD_ID);
  }

  /**
   * Opens the main menu for a player for a specific modId.
   */
  public static void openMainMenu(ServerPlayerEntity player, String modId) {
    ShopConfig config = getConfig(modId);
    ShopUiRegistry.get(modId).openMainMenu(player, config, modId);
  }

  /**
   * Opens a specific shop for a player by modId and shopId.
   */
  public static void openShop(ServerPlayerEntity player, String modId, String shopId) {
    openShop(player, modId, shopId, true);
  }

  /**
   * Opens a specific shop for a player by modId and shopId with close button option.
   */
  public static void openShop(ServerPlayerEntity player, String modId, String shopId, boolean withClose) {
    Shop shop = getShop(modId, shopId);
    if (shop != null) {
      openShop(player, modId, shop, withClose);
    }
  }

  /**
   * Opens a specific shop for a player.
   */
  public static void openShop(ServerPlayerEntity player, Shop shop) {
    openShop(player, shop, true);
  }

  /**
   * Opens a specific shop for a player with close button option.
   */
  public static void openShop(ServerPlayerEntity player, Shop shop, boolean withClose) {
    String modId = ShopContext.get().findModId(shop);
    openShop(player, modId, shop, withClose);
  }

  /**
   * Opens a specific shop for a player with explicit modId and close button option.
   */
  public static void openShop(ServerPlayerEntity player, String modId, Shop shop, boolean withClose) {
    ShopConfig config = getConfig(modId);
    NavigationContext nav = new NavigationContext();
    nav.push(shop);
    ShopUiRegistry.get(modId).openShop(player, shop, nav, config, withClose);
  }

  /**
   * Opens the buy or sell interface for a player.
   */
  public static void openBuySell(ServerPlayerEntity player, Product product, Shop shop, ActionShop action) {
    openBuySell(player, product, shop, action, 1);
  }

  /**
   * Opens the buy or sell interface for a player with custom initial amount.
   */
  public static void openBuySell(ServerPlayerEntity player, Product product, Shop shop, ActionShop action, int amount) {
    String modId = ShopContext.get().findModId(shop);
    ShopConfig config = getConfig(modId);
    NavigationContext nav = new NavigationContext();
    nav.push(shop);
    ShopUiRegistry.get(modId).openBuySell(player, nav, product, amount, action, config, true);
  }

  /**
   * Opens the transaction history for a player.
   */
  public static void openTransactions(ServerPlayerEntity player, UUID targetUuid, String targetName, String modId) {
    ShopConfig config = getConfig(modId);
    ShopUiRegistry.get(modId).openTransactions(player, targetUuid, targetName, config, modId);
  }

  /**
   * Opens the search menu with a query for a mod.
   */
  public static void openSearch(ServerPlayerEntity player, String modId, String query) {
    ShopUiRegistry.get(modId).openSearch(player, query, modId);
  }

  /**
   * Opens the stats overview menu for a mod.
   */
  public static void openStats(ServerPlayerEntity player, String modId) {
    ShopConfig config = getConfig(modId);
    ShopUiRegistry.get(modId).openStats(player, config, modId);
  }

  /**
   * Opens the inventory bulk-sell GUI for a player.
   */
  public static void openSellGui(ServerPlayerEntity player) {
    ShopUiRegistry.getDefaultProvider().openSellGui(player);
  }

  /**
   * Executes a purchase transaction programmatically for a player.
   */
  public static boolean buy(ServerPlayerEntity player, Product product, Shop shop, int amount) {
    String modId = ShopContext.get().findModId(shop);
    ShopConfig config = getConfig(modId);
    return TransactionService.buy(player, product, shop, amount, config);
  }

  /**
   * Executes a sell transaction programmatically for a player.
   */
  public static void sell(ServerPlayerEntity player, Product product, Shop shop, int amount) {
    String modId = ShopContext.get().findModId(shop);
    ShopConfig config = getConfig(modId);
    TransactionService.sell(player, product, shop, amount, config);
  }

  /**
   * Sell all matching items from a player's inventory.
   */
  public static void sellAll(ServerPlayerEntity player, List<ItemStack> itemStacks) {
    TransactionService.sellAll(player, itemStacks);
  }

  /**
   * Dynamically registers a shop in memory and rebuilds the sell index.
   */
  public static void registerShop(String modId, Shop shop) {
    ShopContext.get().addTypedShop(modId, shop);
    ShopContext.get().getSellIndex().rebuild(ShopContext.get().getTypedShops());
  }

  /**
   * Dynamically removes a shop in memory and rebuilds the sell index.
   */
  public static boolean removeShop(String modId, String shopId) {
    boolean removed = ShopContext.get().removeTypedShop(modId, shopId);
    if (removed) {
      ShopContext.get().getSellIndex().rebuild(ShopContext.get().getTypedShops());
    }
    return removed;
  }

  /**
   * Returns an unmodifiable list of all registered shops across all mods.
   */
  public static List<Shop> getAllShops() {
    List<Shop> all = new ArrayList<>();
    ShopContext.get().getTypedShops().values().forEach(all::addAll);
    return Collections.unmodifiableList(all);
  }
}
