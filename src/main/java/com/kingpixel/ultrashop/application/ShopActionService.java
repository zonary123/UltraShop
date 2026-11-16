package com.kingpixel.ultrashop.application;

import com.kingpixel.ultrashop.ShopContext;
import com.kingpixel.ultrashop.api.action.ShopActionResult;
import com.kingpixel.ultrashop.domain.model.Product;
import com.kingpixel.ultrashop.domain.model.shop.ShopReference;
import com.kingpixel.ultrashop.domain.service.TransactionService;
import com.kingpixel.ultrashop.infrastructure.config.ShopConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * Headless action service providing atomic player buy and sell operations.
 */
public final class ShopActionService {

  private static final ShopActionService INSTANCE = new ShopActionService();

  private ShopActionService() {
  }

  public static ShopActionService getInstance() {
    return INSTANCE;
  }

  /**
   * Executes a purchase transaction programmatically for a player.
   */
  public ShopActionResult buy(ServerPlayerEntity player, Product product, ShopReference shop, int amount) {
    String modId = ShopContext.get().findModId(shop);
    ShopConfig config = ShopContext.get().getConfigs().get(modId);
    return TransactionService.buyWithResult(player, product, shop, amount, config, false);
  }

  /**
   * Executes a purchase transaction programmatically with explicit configuration.
   */
  public ShopActionResult buy(ServerPlayerEntity player, Product product, ShopReference shop, int amount, ShopConfig config) {
    return TransactionService.buyWithResult(player, product, shop, amount, config, false);
  }

  /**
   * Executes a sell transaction programmatically for a player.
   */
  public ShopActionResult sell(ServerPlayerEntity player, Product product, ShopReference shop, int amount) {
    String modId = ShopContext.get().findModId(shop);
    ShopConfig config = ShopContext.get().getConfigs().get(modId);
    return TransactionService.sellWithResult(player, product, shop, amount, config);
  }

  /**
   * Executes a sell transaction programmatically with explicit configuration.
   */
  public ShopActionResult sell(ServerPlayerEntity player, Product product, ShopReference shop, int amount, ShopConfig config) {
    return TransactionService.sellWithResult(player, product, shop, amount, config);
  }

  /**
   * Sells all matching items from the provided item stacks.
   */
  public ShopActionResult sellAll(ServerPlayerEntity player, List<ItemStack> itemStacks) {
    return TransactionService.sellAllWithResult(player, itemStacks);
  }
}
