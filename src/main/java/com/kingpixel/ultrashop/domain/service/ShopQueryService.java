package com.kingpixel.ultrashop.domain.service;

import com.kingpixel.cobbleutils.Model.EconomyUse;
import com.kingpixel.cobbleutils.Model.ItemChance;
import com.kingpixel.cobbleutils.Model.conditions.util.ConditionUtils;
import com.kingpixel.cobbleutils.api.EconomyApi;
import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.ultrashop.ShopContext;
import com.kingpixel.ultrashop.UltraShop;
import com.kingpixel.ultrashop.api.model.ProductView;
import com.kingpixel.ultrashop.domain.model.ActionShop;
import com.kingpixel.ultrashop.domain.model.Product;
import com.kingpixel.ultrashop.domain.model.UserInfo;
import com.kingpixel.ultrashop.domain.model.shop.Shop;
import com.kingpixel.ultrashop.domain.model.shop.config.ConditionsConfig;
import com.kingpixel.ultrashop.infrastructure.config.LangConfig;
import com.kingpixel.ultrashop.infrastructure.config.ShopConfig;
import com.kingpixel.ultrashop.presentation.gui.PlaceholderReplacer;
import com.kingpixel.ultrashop.presentation.gui.ShopProducts;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Query service providing read-only models and computed data for custom UIs and external mods.
 */
public final class ShopQueryService {

  private ShopQueryService() {
  }

  /**
   * Retrieves the active products of a shop for a specific player (respects rotations and scopes).
   */
  public static List<Product> getActiveProducts(Shop shop, @Nullable ServerPlayerEntity player) {
    if (shop == null) return List.of();
    String modId = ShopContext.get().findModId(shop);
    return ShopProducts.activeProducts(shop, modId, player);
  }

  /**
   * Builds a full {@link ProductView} ready for UI rendering.
   */
  public static ProductView getProductView(Product product, @Nullable ServerPlayerEntity player, Shop shop) {
    ShopContext ctx = ShopContext.get();
    String modId = ctx.findModId(shop);
    ShopConfig config = ctx.getConfigs().get(modId);
    LangConfig lang = ctx.getLang();

    Map<EconomyUse, BigDecimal> buyPrices = getBuyPrices(product, player, shop, 1);
    Map<EconomyUse, BigDecimal> sellPrices = getSellPrices(product, shop, 1);
    float discount = getDiscount(product, player, shop);

    boolean buyable = canBuy(product, player, shop);
    boolean sellable = canSell(product, player, shop);
    boolean affordable = canAfford(product, player, shop, 1);

    Long remainingStock = getRemainingStock(product, player);
    Integer remainingLimit = getRemainingLimit(product, player);
    long cooldownRemaining = getCooldownSeconds(product, player);

    String finalDisplay = product.getDisplay() != null ? product.getDisplay() : product.getProduct();
    ItemChance itemChance = new ItemChance(finalDisplay, 0);
    String title = product.getDisplayname() != null ? product.getDisplayname() : itemChance.getTitle();

    String playerBalance = PlaceholderReplacer.buildBalanceString(product, shop, player);

    List<String> loreTemplate = lang != null && lang.getInfoProduct() != null
      ? new ArrayList<>(lang.getInfoProduct())
      : new ArrayList<>();
    List<String> filteredLore = PlaceholderReplacer.filterLore(loreTemplate, product, player, shop, config, ActionShop.BUY);
    List<String> resolvedLore = filteredLore.stream()
      .map(line -> PlaceholderReplacer.replace(line, product, player, shop, 1, config, playerBalance))
      .toList();

    ItemStack displayStack = itemChance.getItemStack();

    return ProductView.builder()
      .product(product)
      .buyPrices(buyPrices)
      .sellPrices(sellPrices)
      .discountPercent(discount)
      .canBuy(buyable)
      .canSell(sellable)
      .canAfford(affordable)
      .remainingStock(remainingStock)
      .remainingLimit(remainingLimit)
      .cooldownRemainingSeconds(cooldownRemaining)
      .displayStack(displayStack)
      .displayName(title)
      .lore(resolvedLore)
      .build();
  }

  /**
   * Builds {@link ProductView} instances for all active products of a shop.
   */
  public static List<ProductView> getProductViews(Shop shop, @Nullable ServerPlayerEntity player) {
    if (shop == null) return List.of();
    List<Product> products = getActiveProducts(shop, player);
    return products.stream()
      .map(product -> getProductView(product, player, shop))
      .toList();
  }

  /**
   * Calculates total buy prices for a product and amount.
   */
  public static Map<EconomyUse, BigDecimal> getBuyPrices(Product product, @Nullable ServerPlayerEntity player,
                                                          Shop shop, int amount) {
    if (product == null || !product.isBuyable()) return Map.of();
    ShopConfig config = ShopContext.get().getConfigs().get(ShopContext.get().findModId(shop));
    return PriceCalculator.getBuyPrices(product, player, amount, shop, config);
  }

  /**
   * Calculates total sell prices for a product and amount.
   */
  public static Map<EconomyUse, BigDecimal> getSellPrices(Product product, Shop shop, int amount) {
    if (product == null || !product.isSellable()) return Map.of();
    return PriceCalculator.getSellPrices(product, amount, shop);
  }

  /**
   * Calculates discount percentage for a player on a product.
   */
  public static float getDiscount(Product product, @Nullable ServerPlayerEntity player, Shop shop) {
    if (product == null || player == null || shop == null) return 0f;
    ShopConfig config = ShopContext.get().getConfigs().get(ShopContext.get().findModId(shop));
    return PriceCalculator.getDiscount(product, player, shop, config);
  }

  /**
   * Checks whether a player can buy a product (checks permissions, conditions, stock, limits, and cooldowns).
   */
  public static boolean canBuy(Product product, @Nullable ServerPlayerEntity player, Shop shop) {
    if (product == null || !product.isBuyable()) return false;
    if (player == null) return true;
    if (shop != null && !canAccessShop(shop, player)) return false;
    if (!PriceCalculator.hasPermission(product, player)) return false;
    if (product.getConditions() != null && !product.getConditions().isEmpty()
        && !ConditionUtils.check(product.getConditions(), player)) {
      return false;
    }
    ShopContext ctx = ShopContext.get();
    if (product.hasStockControl() && ctx.getRepositories() != null && ctx.getRepositories().getStockRepository() != null) {
      long remaining = ctx.getRepositories().getStockRepository().getRemaining(
        player.getUuid(), product.getUuid(), product.getStockMode(), product.getStockAmount()
      );
      if (remaining <= 0) return false;
    }
    if (ctx.getRepositories() != null && ctx.getRepositories().getUserRepository() != null) {
      UserInfo userInfo = ctx.getRepositories().getUserRepository().findByUuid(player.getUuid());
      if (userInfo != null && !userInfo.canBuy(product)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Checks whether a player can sell a product.
   */
  public static boolean canSell(Product product, @Nullable ServerPlayerEntity player, Shop shop) {
    if (product == null || !product.isSellable()) return false;
    if (player == null) return true;
    ShopContext ctx = ShopContext.get();
    String modId = ctx.findModId(shop);
    ShopConfig config = ctx.getConfigs().get(modId);
    return PriceCalculator.canSell(product, player, shop, config);
  }

  /**
   * Checks whether a player has enough currency to buy a product for the given quantity.
   */
  public static boolean canAfford(Product product, @Nullable ServerPlayerEntity player, Shop shop, int amount) {
    if (player == null || product == null) return false;
    Map<EconomyUse, BigDecimal> buyPrices = getBuyPrices(product, player, shop, amount);
    for (Map.Entry<EconomyUse, BigDecimal> entry : buyPrices.entrySet()) {
      if (!EconomyApi.hasEnoughMoney(player.getUuid(), entry.getValue(), entry.getKey(), false)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Retrieves remaining stock for a product, or {@code null} if stock control is disabled.
   */
  @Nullable
  public static Long getRemainingStock(Product product, @Nullable ServerPlayerEntity player) {
    if (product == null || !product.hasStockControl()) return null;
    ShopContext ctx = ShopContext.get();
    if (ctx.getRepositories() == null || ctx.getRepositories().getStockRepository() == null) return null;
    UUID uuid = player != null ? player.getUuid() : null;
    return ctx.getRepositories().getStockRepository().getRemaining(
      uuid, product.getUuid(), product.getStockMode(), product.getStockAmount()
    );
  }

  /**
   * Retrieves remaining limit count for a player, or {@code null} if no limit is configured.
   */
  @Nullable
  public static Integer getRemainingLimit(Product product, @Nullable ServerPlayerEntity player) {
    if (product == null || product.getMax() == null || product.getMax() <= 0 || player == null) return null;
    ShopContext ctx = ShopContext.get();
    if (ctx.getRepositories() == null || ctx.getRepositories().getUserRepository() == null) return null;
    UserInfo userInfo = ctx.getRepositories().getUserRepository().findByUuid(player.getUuid());
    int current = userInfo != null ? userInfo.getActualProductLimit(product) : 0;
    return Math.max(0, product.getMax() - current);
  }

  /**
   * Retrieves remaining cooldown in seconds for a player, or 0 if ready to purchase.
   */
  public static long getCooldownSeconds(Product product, @Nullable ServerPlayerEntity player) {
    if (product == null || product.getCooldown() == null || player == null) return 0L;
    ShopContext ctx = ShopContext.get();
    if (ctx.getRepositories() == null || ctx.getRepositories().getUserRepository() == null) return 0L;
    UserInfo userInfo = ctx.getRepositories().getUserRepository().findByUuid(player.getUuid());
    if (userInfo == null) return 0L;
    long cooldownExpiry = userInfo.getProductCooldown(product);
    return Math.max(0L, (cooldownExpiry - System.currentTimeMillis()) / 1000L);
  }

  /**
   * Checks whether a player is authorized to access a shop (maintenance, permissions, open conditions).
   */
  public static boolean canAccessShop(Shop shop, @Nullable ServerPlayerEntity player) {
    if (shop == null) return false;
    if (player == null) return true;
    ShopContext ctx = ShopContext.get();
    String modId = ctx.findModId(shop);
    if (shop.isMaintenance() && !PermissionApi.hasPermission(player, modId + ".admin", 2)
        && !PermissionApi.hasPermission(player, UltraShop.MOD_ID + ".admin", 2)) {
      return false;
    }
    if (!PermissionApi.hasPermission(player, shop.getPermission(modId), 4)) {
      return false;
    }
    ConditionsConfig conditionsCfg = shop.getConditionsConfig();
    var openConditions = conditionsCfg != null ? conditionsCfg.getOpenConditions() : null;
    return openConditions == null || openConditions.isEmpty() || ConditionUtils.check(openConditions, player);
  }

  /**
   * Searches products across all shops belonging to a mod matching the query.
   */
  public static List<Product> searchProducts(String query, String modId, @Nullable ServerPlayerEntity player) {
    if (query == null || query.isBlank()) return List.of();
    String normalized = query.trim().toLowerCase();
    List<Shop> shops = ShopContext.get().getTypedShops(modId);
    List<Product> matches = new ArrayList<>();

    for (Shop shop : shops) {
      if (!canAccessShop(shop, player)) continue;
      for (Product product : ShopProducts.allConfiguredProducts(shop)) {
        if (player != null && product.getVisibilityConditions() != null && !product.getVisibilityConditions().isEmpty()
            && !ConditionUtils.check(product.getVisibilityConditions(), player)) {
          continue;
        }
        String id = product.getProduct().toLowerCase();
        String display = product.getDisplay() != null ? product.getDisplay().toLowerCase() : "";
        String name = product.getDisplayname() != null ? product.getDisplayname().toLowerCase() : "";
        if (id.contains(normalized) || display.contains(normalized) || name.contains(normalized)) {
          matches.add(product);
        }
      }
    }
    return Collections.unmodifiableList(matches);
  }
}
