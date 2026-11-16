package com.kingpixel.ultrashop.domain.service;

import com.kingpixel.cobbleutils.Model.EconomyUse;
import com.kingpixel.cobbleutils.Model.ItemChance;
import com.kingpixel.cobbleutils.api.EconomyApi;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.ultrashop.ShopContext;
import com.kingpixel.ultrashop.UltraShop;
import com.kingpixel.ultrashop.api.action.ShopActionResult;
import com.kingpixel.ultrashop.api.action.ShopActionResult.Status;
import com.kingpixel.ultrashop.api.event.ShopEvents;
import com.kingpixel.ultrashop.api.event.ShopPreTransactionEvent;
import com.kingpixel.ultrashop.domain.model.ActionShop;
import com.kingpixel.ultrashop.domain.model.Product;
import com.kingpixel.ultrashop.domain.model.Transaction;
import com.kingpixel.ultrashop.domain.model.UserInfo;
import com.kingpixel.ultrashop.domain.model.shop.Shop;
import com.kingpixel.ultrashop.domain.model.shop.ShopReference;
import com.kingpixel.ultrashop.infrastructure.config.ShopConfig;
import com.kingpixel.ultrashop.infrastructure.index.SellProductIndex;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles buy, sell, and sellAll transactions with proper thread safety.
 *
 * <p>CRITICAL: Inventory modifications MUST run on the server thread.
 * Price calculations can run async.</p>
 */
public final class TransactionService {

  private static final String PLACEHOLDER_AMOUNT = "%amount%";
  private static final String PLACEHOLDER_PRICE = "%price%";
  private static final Map<UUID, Long> sellLock = new ConcurrentHashMap<>();

  private TransactionService() {
  }

  /**
   * Purchases a product for a player, charging all economies defined in the product's effective prices.
   * Provided for backwards compatibility where stock reservation is not handled externally.
   *
   * @param player the player performing the purchase
   * @param product the product being purchased
   * @param shop the reference to the shop hosting the product
   * @param amount the quantity of the product to purchase
   * @param config the active shop configuration
   * @return true if the purchase was completed successfully, false otherwise
   */
  public static boolean buy(ServerPlayerEntity player, Product product, ShopReference shop, int amount,
                             ShopConfig config) {
    return buy(player, product, shop, amount, config, false);
  }

  /**
   * Purchases a product for a player, charging all economies defined in the product's effective prices.
   * If stockAlreadyReserved is true, the stock has already been checked and decremented in the repository.
   *
   * @param player the player performing the purchase
   * @param product the product being purchased
   * @param shop the reference to the shop hosting the product
   * @param amount the quantity of the product to purchase
   * @param config the active shop configuration
   * @param stockAlreadyReserved whether the product's stock has already been checked and decremented in the repository
   * @return true if the purchase was completed successfully, false otherwise
   */
  public static boolean buy(ServerPlayerEntity player, Product product, ShopReference shop, int amount,
                             ShopConfig config, boolean stockAlreadyReserved) {
    return buyWithResult(player, product, shop, amount, config, stockAlreadyReserved).isSuccess();
  }

  /**
   * Purchases a product for a player and returns a detailed {@link ShopActionResult}.
   */
  public static ShopActionResult buyWithResult(ServerPlayerEntity player, Product product, ShopReference shop, int amount,
                                               ShopConfig config, boolean stockAlreadyReserved) {
    ShopContext ctx = ShopContext.get();
    synchronized (ctx.getTransactionLock(player.getUuid())) {
      ShopPreTransactionEvent preEvent = ShopEvents.firePreTransaction(player, product, shop, amount, ActionShop.BUY);
      if (preEvent.isCanceled()) {
        String reason = preEvent.getCancelReason() != null && !preEvent.getCancelReason().isBlank()
          ? preEvent.getCancelReason()
          : "Transaction canceled by event";
        if (preEvent.getCancelReason() != null && !preEvent.getCancelReason().isBlank()) {
          PlayerUtils.sendMessage(player, preEvent.getCancelReason(), ctx.getLang().getPrefix(), TypeMessage.CHAT);
        }
        if (stockAlreadyReserved) {
          releaseStock(player, product, amount, ctx);
        }
        return ShopActionResult.failure(Status.EVENT_CANCELED, reason, product, amount, ActionShop.BUY);
      }

      ItemChance itemChance = buildItemChance(product);
      ItemStack itemStack = itemChance.getItemStack();

      if (!hasInventorySpace(player, product, amount, itemStack, ctx)) {
        if (stockAlreadyReserved) {
          releaseStock(player, product, amount, ctx);
        }
        String msg = ctx.getLang() != null ? ctx.getLang().getMessageNotEnoughSpace() : "Inventory full";
        return ShopActionResult.failure(Status.INVENTORY_FULL, msg, product, amount, ActionShop.BUY);
      }

      Map<EconomyUse, BigDecimal> buyPrices = PriceCalculator.getBuyPrices(product, player, amount, shop, config);
      if (!canAffordBuy(player, itemChance, itemStack, amount, buyPrices, ctx)) {
        if (stockAlreadyReserved) {
          releaseStock(player, product, amount, ctx);
        }
        String msg = ctx.getLang() != null ? ctx.getLang().getMessageNotEnoughMoney() : "Not enough money";
        return ShopActionResult.builder()
          .status(Status.NOT_ENOUGH_MONEY)
          .message(msg)
          .product(product)
          .requestedAmount(amount)
          .action(ActionShop.BUY)
          .prices(buyPrices)
          .build();
      }

      UserInfo userInfo = ctx.getRepositories().getUserRepository().findByUuid(player.getUuid());
      if (userInfo != null && !userInfo.canBuy(product)) {
        long time = Math.max(0, (userInfo.getProductCooldown(product) - System.currentTimeMillis()) / 1000);
        String limitMsg = ctx.getLang().getMessageYouCantBuyNow()
          .replace("%limit%", String.valueOf(product.getMax()))
          .replace("%time%", String.valueOf(time));
        PlayerUtils.sendMessage(player, limitMsg, ctx.getLang().getPrefix(), TypeMessage.CHAT);
        if (stockAlreadyReserved) {
          releaseStock(player, product, amount, ctx);
        }
        Status status = time > 0 ? Status.ON_COOLDOWN : Status.LIMIT_REACHED;
        return ShopActionResult.builder()
          .status(status)
          .message(limitMsg)
          .product(product)
          .requestedAmount(amount)
          .action(ActionShop.BUY)
          .cooldownRemainingSeconds(time)
          .remainingLimit(product.getMax())
          .build();
      }

      if (!stockAlreadyReserved) {
        if (!tryConsumeStock(player, product, amount, ctx)) {
          String msg = ctx.getLang() != null ? ctx.getLang().getMessageOutOfStock() : "Out of stock";
          return ShopActionResult.failure(Status.OUT_OF_STOCK, msg, product, amount, ActionShop.BUY);
        }
      }

      try {
        chargeBuyPrices(player, buyPrices);
        ItemChance.giveReward(player, itemChance, amount);
      } catch (Exception e) {
        releaseStock(player, product, amount, ctx);
        refundBuyPrices(player, buyPrices);
        UltraShop.LOGGER.error("Error completing buy transaction: " + e.getMessage());
        return ShopActionResult.failure(Status.ERROR, e.getMessage(), product, amount, ActionShop.BUY);
      }

      persistProductLimit(player, product, amount, ctx);
      saveTransactions(player, product, shop, amount, buyPrices, ActionShop.BUY, config, ctx);

      StringBuilder allBuySb = new StringBuilder();
      for (Map.Entry<EconomyUse, BigDecimal> entry : buyPrices.entrySet()) {
        allBuySb.append(EconomyApi.formatMoney(entry.getValue(), entry.getKey())).append(" ");
      }
      String successMsg = ctx.getLang().getMessageSimpleBuy()
        .replace("%product%", itemStack.getName().getString())
        .replace(PLACEHOLDER_AMOUNT, String.valueOf(amount))
        .replace(PLACEHOLDER_PRICE, allBuySb.toString().trim());

      PlayerUtils.sendMessage(player, successMsg, ctx.getLang().getPrefix(), TypeMessage.CHAT);

      ShopEvents.firePostTransaction(player, product, shop, amount, ActionShop.BUY, true);

      return ShopActionResult.builder()
        .status(Status.SUCCESS)
        .message(successMsg)
        .product(product)
        .requestedAmount(amount)
        .processedAmount(amount)
        .action(ActionShop.BUY)
        .prices(buyPrices)
        .build();
    }
  }

  /**
   * Sells a specific quantity of a product from a player's inventory, applying product, shop, and global limits.
   *
   * @param player the player performing the sale
   * @param product the product being sold
   * @param shop the reference to the shop hosting the product
   * @param amount the maximum quantity of the product to sell
   * @param config the active shop configuration
   */
  public static void sell(ServerPlayerEntity player, Product product, ShopReference shop, int amount,
                          ShopConfig config) {
    sellWithResult(player, product, shop, amount, config);
  }

  /**
   * Sells a specific quantity of a product and returns a detailed {@link ShopActionResult}.
   */
  public static ShopActionResult sellWithResult(ServerPlayerEntity player, Product product, ShopReference shop, int amount,
                                                ShopConfig config) {
    ShopContext ctx = ShopContext.get();
    synchronized (ctx.getTransactionLock(player.getUuid())) {
      if (!PriceCalculator.canSell(product, player, shop, config)) {
        UltraShop.LOGGER.warn("Blocked exploit sell attempt: {} tried to sell {} (sell > buy)",
          player.getGameProfile().getName(), product.getProduct());
        String msg = ctx.getLang().getMessageBuyPriceLessThanSell();
        PlayerUtils.sendMessage(player, msg, ctx.getLang().getPrefix(), TypeMessage.CHAT);
        return ShopActionResult.failure(Status.CANNOT_SELL, msg, product, amount, ActionShop.SELL);
      }

      ItemStack productTemplate = product.getItemStack();
      Map<EconomyUse, BigDecimal> sellPerUnit = PriceCalculator.getSellPricesPerUnit(product, shop);

      UserInfo userInfo = ctx.getRepositories().getUserRepository().findByUuid(player.getUuid());
      if (userInfo == null) {
        userInfo = new UserInfo(player.getUuid(), player.getGameProfile().getName());
      }

      SellLimitService.LimitCheckResult limitResult = SellLimitService.evaluateSellLimit(
        userInfo, product, shop, config, sellPerUnit, amount
      );

      if (limitResult.blocked()) {
        notifySellBlocked(player, limitResult, ctx);
        Status st = "product".equals(limitResult.blockReasonType())
          ? Status.LIMIT_REACHED
          : Status.DAILY_SELL_LIMIT_EXCEEDED;
        return ShopActionResult.failure(st, "Sell limit reached: " + limitResult.blockReasonType(), product, amount, ActionShop.SELL);
      }

      int allowedAmount = limitResult.allowedAmount();

      ShopPreTransactionEvent preEvent = ShopEvents.firePreTransaction(player, product, shop, allowedAmount, ActionShop.SELL);
      if (preEvent.isCanceled()) {
        String reason = preEvent.getCancelReason() != null && !preEvent.getCancelReason().isBlank()
          ? preEvent.getCancelReason()
          : "Transaction canceled by event";
        if (preEvent.getCancelReason() != null && !preEvent.getCancelReason().isBlank()) {
          PlayerUtils.sendMessage(player, preEvent.getCancelReason(), ctx.getLang().getPrefix(), TypeMessage.CHAT);
        }
        return ShopActionResult.failure(Status.EVENT_CANCELED, reason, product, allowedAmount, ActionShop.SELL);
      }

      final int[] sold = {0};
      int remaining = allowedAmount;

      PlayerInventory inventory = player.getInventory();
      for (int i = 0; i < inventory.size() && remaining > 0; i++) {
        ItemStack slot = inventory.getStack(i);
        if (ProductMatcher.matches(slot, productTemplate)) {
          int stackCount = slot.getCount();
          int toRemove = Math.min(stackCount, remaining);
          slot.decrement(toRemove);
          sold[0] += toRemove;
          remaining -= toRemove;
        }
      }

      if (sold[0] > 0) {
        StringBuilder allSellSb = new StringBuilder();
        Map<EconomyUse, BigDecimal> totals = new LinkedHashMap<>();
        for (Map.Entry<EconomyUse, BigDecimal> entry : sellPerUnit.entrySet()) {
          BigDecimal total = entry.getValue().multiply(BigDecimal.valueOf(sold[0]));
          EconomyApi.addMoney(player.getUuid(), total, entry.getKey());
          allSellSb.append(EconomyApi.formatMoney(total, entry.getKey())).append(" ");
          totals.put(entry.getKey(), total);
        }

        String msg = ctx.getLang().getMessageSimpleSell()
          .replace("%product%", productTemplate.getName().getString())
          .replace(PLACEHOLDER_AMOUNT, String.valueOf(sold[0]))
          .replace(PLACEHOLDER_PRICE, allSellSb.toString().trim());

        PlayerUtils.sendMessage(player, msg, ctx.getLang().getPrefix(), TypeMessage.CHAT);

        saveTransactions(player, product, shop, sold[0], totals, ActionShop.SELL, config, ctx);
        ShopEvents.firePostTransaction(player, product, shop, sold[0], ActionShop.SELL, true);

        ctx.getAsyncContext().runAsync(() -> {
          UserInfo uInfo = ctx.getRepositories().getUserRepository().findByUuid(player.getUuid());
          if (uInfo == null) {
            uInfo = new UserInfo(player.getUuid(), player.getGameProfile().getName());
          }
          SellLimitService.applyEarningsAndLimits(uInfo, product, shop, config, sold[0], totals);
          ctx.getRepositories().getUserRepository().save(uInfo);
        });

        return ShopActionResult.builder()
          .status(Status.SUCCESS)
          .message(msg)
          .product(product)
          .requestedAmount(amount)
          .processedAmount(sold[0])
          .action(ActionShop.SELL)
          .prices(totals)
          .build();
      } else {
        String msg = ctx.getLang().getMessageNotSell();
        PlayerUtils.sendMessage(player, msg, ctx.getLang().getPrefix(), TypeMessage.CHAT);
        return ShopActionResult.failure(Status.NOT_ENOUGH_ITEMS, msg, product, amount, ActionShop.SELL);
      }
    }
  }

  /**
   * Sells all matching items from the player's inventory using the pre-built sell index.
   *
   * @param player the player performing the sale
   * @param itemStacks the list of item stacks in the player's inventory to search for sellable products
   */
  public static void sellAll(ServerPlayerEntity player, List<ItemStack> itemStacks) {
    if (itemStacks == null || itemStacks.isEmpty()) return;
    if (sellLock.containsKey(player.getUuid())) return;

    sellLock.put(player.getUuid(), System.currentTimeMillis());
    ShopContext ctx = ShopContext.get();

    ctx.runOnServer(() -> {
      try {
        processSellAll(player, itemStacks, ctx, ctx.getMainConfig());
      } finally {
        sellLock.remove(player.getUuid());
      }
    });
  }

  /**
   * Sells all matching items and returns a detailed {@link ShopActionResult}.
   */
  public static ShopActionResult sellAllWithResult(ServerPlayerEntity player, List<ItemStack> itemStacks) {
    if (itemStacks == null || itemStacks.isEmpty()) {
      return ShopActionResult.failure(Status.NOT_ENOUGH_ITEMS, "No items to sell", null, 0, ActionShop.SELL);
    }
    if (sellLock.containsKey(player.getUuid())) {
      return ShopActionResult.failure(Status.ERROR, "Sell lock active", null, 0, ActionShop.SELL);
    }

    sellLock.put(player.getUuid(), System.currentTimeMillis());
    ShopContext ctx = ShopContext.get();
    try {
      return processSellAll(player, itemStacks, ctx, ctx.getMainConfig());
    } finally {
      sellLock.remove(player.getUuid());
    }
  }

  private static ShopActionResult processSellAll(ServerPlayerEntity player, List<ItemStack> itemStacks,
                                                 ShopContext ctx, ShopConfig config) {
    try {
      long start = System.currentTimeMillis();
      SellProductIndex index = ctx.getSellIndex();
      Map<EconomyUse, BigDecimal> earnings = new LinkedHashMap<>();
      List<SellAction> actions = collectSellActionsWithLimit(player, itemStacks, index, config, earnings, ctx);

      if (actions.isEmpty()) {
        Map<EconomyUse, BigDecimal> dummyEarnings = new HashMap<>();
        List<SellAction> potentialActions = collectSellActions(player, itemStacks, index, config, dummyEarnings);
        if (!potentialActions.isEmpty()) {
          UserInfo userInfo = ctx.getRepositories().getUserRepository().findByUuid(player.getUuid());
          if (userInfo == null) {
            userInfo = new UserInfo(player.getUuid(), player.getGameProfile().getName());
          }

          boolean productLimitHit = false;
          Product limitProduct = null;
          boolean shopLimitHit = false;
          ShopReference limitShop = null;
          String limitCurrency = "";
          BigDecimal currentLimit = BigDecimal.ZERO;

          for (SellAction action : potentialActions) {
            if (action.product().getSellMax() != null && action.product().getSellUuid() != null) {
              if (!userInfo.canSellProduct(action.product())) {
                productLimitHit = true;
                limitProduct = action.product();
                break;
              }
            }
            if (action.shop().getDailySellLimits() != null && !action.shop().getDailySellLimits().isEmpty()) {
              userInfo.checkShopDailySellReset(action.shop().getId(), action.shop().getDailySellResetCooldown());
              for (Map.Entry<EconomyUse, BigDecimal> entry : PriceCalculator.getSellPricesPerUnit(action.product(), action.shop()).entrySet()) {
                String currency = entry.getKey().getCurrency();
                BigDecimal limit = action.shop().getDailySellLimits().get(currency);
                if (limit != null && limit.compareTo(BigDecimal.ZERO) > 0) {
                  BigDecimal current = userInfo.getShopDailySellEarnings(action.shop().getId(), currency);
                  if (current.compareTo(limit) >= 0) {
                    shopLimitHit = true;
                    limitShop = action.shop();
                    limitCurrency = currency;
                    currentLimit = limit;
                    break;
                  }
                }
              }
              if (shopLimitHit) break;
            }
          }

          if (productLimitHit && limitProduct != null) {
            long time = Math.max(0, (userInfo.getProductSellCooldown(limitProduct) - System.currentTimeMillis()) / 1000);
            String limitMsg = ctx.getLang().getMessageYouCantSellNow()
              .replace("%limit%", String.valueOf(limitProduct.getSellMax()))
              .replace("%time%", String.valueOf(time));
            PlayerUtils.sendMessage(player, limitMsg, ctx.getLang().getPrefix(), TypeMessage.CHAT);
            return ShopActionResult.failure(Status.LIMIT_REACHED, limitMsg, limitProduct, 0, ActionShop.SELL);
          } else if (shopLimitHit && limitShop != null) {
            String msg = ctx.getLang().getMessageShopDailySellLimitReached()
              .replace("%limit%", currentLimit.toPlainString())
              .replace("%currency%", limitCurrency);
            PlayerUtils.sendMessage(player, msg, ctx.getLang().getPrefix(), TypeMessage.CHAT);
            return ShopActionResult.failure(Status.DAILY_SELL_LIMIT_EXCEEDED, msg, null, 0, ActionShop.SELL);
          } else {
            String globalCurrency = "";
            BigDecimal globalLimit = BigDecimal.ZERO;
            if (config.getDailySellLimits() != null) {
              for (String currency : config.getDailySellLimits().keySet()) {
                BigDecimal limit = config.getDailySellLimits().get(currency);
                if (limit != null && limit.compareTo(BigDecimal.ZERO) > 0) {
                  BigDecimal current = userInfo.getDailySellEarnings(currency);
                  if (current.compareTo(limit) >= 0) {
                    globalCurrency = currency;
                    globalLimit = limit;
                    break;
                  }
                }
              }
            }
            String msg = ctx.getLang().getMessageDailySellLimitReached()
              .replace("%limit%", globalLimit.toPlainString())
              .replace("%currency%", globalCurrency);
            PlayerUtils.sendMessage(player, msg, ctx.getLang().getPrefix(), TypeMessage.CHAT);
            return ShopActionResult.failure(Status.DAILY_SELL_LIMIT_EXCEEDED, msg, null, 0, ActionShop.SELL);
          }
        } else {
          String msg = ctx.getLang().getMessageNotSell();
          PlayerUtils.sendMessage(player, msg, ctx.getLang().getPrefix(), TypeMessage.CHAT);
          return ShopActionResult.failure(Status.NOT_ENOUGH_ITEMS, msg, null, 0, ActionShop.SELL);
        }
      }

      int totalCount = 0;
      for (SellAction action : actions) {
        action.itemStack.decrement(action.amount);
        totalCount += action.amount;
      }

      StringBuilder allSell = rewardSellAll(player, ctx, earnings);

      PlayerUtils.sendMessage(player,
        ctx.getLang().getMessageSell().replace("%sell%", allSell.toString()),
        ctx.getLang().getPrefix(), TypeMessage.CHAT);

      saveSellAllTransactions(player, actions, config, ctx);

      ctx.getAsyncContext().runAsync(() -> {
        UserInfo uInfo = ctx.getRepositories().getUserRepository().findByUuid(player.getUuid());
        if (uInfo == null) {
          uInfo = new UserInfo(player.getUuid(), player.getGameProfile().getName());
        }

        for (SellAction action : actions) {
          SellLimitService.applyEarningsAndLimits(uInfo, action.product(), action.shop(), config, action.amount(), action.totals());
        }

        ctx.getRepositories().getUserRepository().save(uInfo);
      });

      logSellAllTiming(config, start);

      return ShopActionResult.builder()
        .status(Status.SUCCESS)
        .message(allSell.toString())
        .processedAmount(totalCount)
        .action(ActionShop.SELL)
        .prices(earnings)
        .build();
    } catch (Exception e) {
      UltraShop.LOGGER.error("Error in sellAll: " + e.getMessage());
      return ShopActionResult.failure(Status.ERROR, e.getMessage(), null, 0, ActionShop.SELL);
    }
  }

  public static void removeSellLock(UUID uuid) {
    sellLock.remove(uuid);
  }

  private static ItemChance buildItemChance(Product product) {
    return ItemChance.builder()
      .item(product.getProduct())
      .chance(0D)
      .build();
  }

  private static boolean hasInventorySpace(ServerPlayerEntity player, Product product, int amount,
                                           ItemStack itemStack, ShopContext ctx) {
    if (product.getProduct().startsWith("command:") || product.getProduct().startsWith("pokemon:")
      || product.getProduct().contains("|")) {
      return true;
    }
    int maxStack = itemStack.getMaxCount();
    int slotsNeeded = (int) Math.ceil((double) amount / maxStack);
    long emptySlots = player.getInventory().main.stream().filter(ItemStack::isEmpty).count();
    if (emptySlots >= slotsNeeded) {
      return true;
    }
    PlayerUtils.sendMessage(player,
      ctx.getLang().getMessageNotEnoughSpace()
        .replace(PLACEHOLDER_AMOUNT, String.valueOf(amount))
        .replace("%slots%", String.valueOf(slotsNeeded)),
      ctx.getLang().getPrefix(), TypeMessage.CHAT);
    return false;
  }

  private static boolean canAffordBuy(ServerPlayerEntity player, ItemChance itemChance, ItemStack itemStack,
                                      int amount, Map<EconomyUse, BigDecimal> buyPrices, ShopContext ctx) {
    for (Map.Entry<EconomyUse, BigDecimal> entry : buyPrices.entrySet()) {
      if (!EconomyApi.hasEnoughMoney(player.getUuid(), entry.getValue(), entry.getKey(), false)) {
        PlayerUtils.sendMessage(player,
          ctx.getLang().getMessageNotEnoughMoney()
            .replace("%product%", itemChance.getTitle())
            .replace(PLACEHOLDER_AMOUNT, String.valueOf(amount))
            .replace("%pack%", String.valueOf(itemStack.getCount()))
            .replace(PLACEHOLDER_PRICE, EconomyApi.formatMoney(entry.getValue(), entry.getKey())),
          ctx.getLang().getPrefix(), TypeMessage.CHAT);
        return false;
      }
    }
    return true;
  }

  private static boolean tryConsumeStock(ServerPlayerEntity player, Product product, int amount, ShopContext ctx) {
    Integer stockAmount = product.getStockAmount();
    if (!product.hasStockControl() || stockAmount == null) {
      return true;
    }
    boolean consumed = ctx.getRepositories().getStockRepository().tryConsume(
      player.getUuid(),
      product.getUuid(),
      product.getStockMode(),
      amount,
      stockAmount
    );
    if (consumed) {
      return true;
    }
    long remaining = ctx.getRepositories().getStockRepository().getRemaining(
      player.getUuid(),
      product.getUuid(),
      product.getStockMode(),
      stockAmount
    );
    PlayerUtils.sendMessage(player,
      ctx.getLang().getMessageNotEnoughStock().replace("%remaining%", String.valueOf(remaining)),
      ctx.getLang().getPrefix(), TypeMessage.CHAT);
    return false;
  }

  private static void chargeBuyPrices(ServerPlayerEntity player, Map<EconomyUse, BigDecimal> buyPrices) {
    for (Map.Entry<EconomyUse, BigDecimal> entry : buyPrices.entrySet()) {
      EconomyApi.removeMoney(player.getUuid(), entry.getValue(), entry.getKey());
    }
  }

  private static void refundBuyPrices(ServerPlayerEntity player, Map<EconomyUse, BigDecimal> buyPrices) {
    for (Map.Entry<EconomyUse, BigDecimal> entry : buyPrices.entrySet()) {
      EconomyApi.addMoney(player.getUuid(), entry.getValue(), entry.getKey());
    }
  }

  private static void releaseStock(ServerPlayerEntity player, Product product, int amount, ShopContext ctx) {
    if (!product.hasStockControl() || product.getStockAmount() == null) {
      return;
    }
    ctx.getAsyncContext().runAsync(() -> {
      ctx.getRepositories().getStockRepository().release(
        player.getUuid(),
        product.getUuid(),
        product.getStockMode(),
        amount
      );
    });
  }

  private static void persistProductLimit(ServerPlayerEntity player, Product product, int amount, ShopContext ctx) {
    if (product.getUuid() == null || product.getMax() == null) {
      return;
    }
    ctx.getAsyncContext().runAsync(() -> {
      UserInfo userInfo = ctx.getRepositories().getUserRepository().findByUuid(player.getUuid());
      if (userInfo == null) {
        userInfo = new UserInfo(player.getUuid(), player.getGameProfile().getName());
      }
      userInfo.addProductLimit(product, amount);
      ctx.getRepositories().getUserRepository().save(userInfo);
    });
  }

  private static void saveTransactions(ServerPlayerEntity player, Product product, ShopReference shop, int amount,
                                       Map<EconomyUse, BigDecimal> totals, ActionShop action, ShopConfig config,
                                       ShopContext ctx) {
    if (config == null || !config.isSaveTransactions()) {
      return;
    }
    ctx.getAsyncContext().runAsync(() -> {
      for (Map.Entry<EconomyUse, BigDecimal> entry : totals.entrySet()) {
        ctx.getRepositories().getTransactionRepository().save(Transaction.builder()
          .playerUuid(player.getUuid())
          .playerName(player.getGameProfile().getName())
          .shopId(shop.getId())
          .productId(product.getProduct())
          .action(action)
          .amount(amount)
          .value(entry.getValue())
          .currency(entry.getKey().getCurrency())
          .timestamp(System.currentTimeMillis())
          .build());
      }
    });
  }

  private static List<SellAction> collectSellActions(ServerPlayerEntity player, List<ItemStack> itemStacks,
                                                     SellProductIndex index, ShopConfig config,
                                                     Map<EconomyUse, BigDecimal> earnings) {
    List<SellAction> actions = new ArrayList<>();
    for (ItemStack itemStack : itemStacks) {
      if (!itemStack.isEmpty()) {
        SellAction action = findSellAction(player, itemStack, index, config, earnings);
        if (action != null) {
          actions.add(action);
        }
      }
    }
    return actions;
  }

  private static SellAction findSellAction(ServerPlayerEntity player, ItemStack itemStack, SellProductIndex index,
                                           ShopConfig config, Map<EconomyUse, BigDecimal> earnings) {
    List<SellProductIndex.SellEntry> entries = index.findSellable(itemStack, player);
    for (SellProductIndex.SellEntry entry : entries) {
      if (PriceCalculator.canSell(entry.product(), player, entry.shop(), config)) {
        Map<EconomyUse, BigDecimal> perUnit = PriceCalculator.getSellPricesPerUnit(entry.product(), entry.shop());
        if (!perUnit.isEmpty()) {
          return createSellAction(itemStack, entry.shop(), entry.product(), perUnit, earnings);
        }
      }
    }
    return null;
  }

  private static SellAction createSellAction(ItemStack itemStack, Shop shop, Product product,
                                             Map<EconomyUse, BigDecimal> perUnit,
                                             Map<EconomyUse, BigDecimal> earnings) {
    int count = itemStack.getCount();
    Map<EconomyUse, BigDecimal> totals = new LinkedHashMap<>();
    for (Map.Entry<EconomyUse, BigDecimal> entry : perUnit.entrySet()) {
      BigDecimal total = entry.getValue().multiply(BigDecimal.valueOf(count));
      totals.put(entry.getKey(), total);
      earnings.merge(entry.getKey(), total, BigDecimal::add);
    }
    return new SellAction(itemStack, shop, product, count, totals);
  }



  private static StringBuilder rewardSellAll(ServerPlayerEntity player, ShopContext ctx,
                                             Map<EconomyUse, BigDecimal> earnings) {
    StringBuilder allSell = new StringBuilder();
    earnings.forEach((economy, price) -> {
      allSell.append(ctx.getLang().getFormatSell()
          .replace(PLACEHOLDER_PRICE, EconomyApi.formatMoney(price, economy)))
        .append("\n");
      EconomyApi.addMoney(player.getUuid(), price, economy);
    });
    return allSell;
  }

  private static void saveSellAllTransactions(ServerPlayerEntity player, List<SellAction> actions,
                                              ShopConfig config, ShopContext ctx) {
    if (config == null || !config.isSaveTransactions()) {
      return;
    }
    for (SellAction action : actions) {
      saveTransactions(player, action.product(), action.shop(), action.amount(), action.totals(), ActionShop.SELL, config, ctx);
    }
  }

  /**
   * Collects sell actions for the provided item stacks while enforcing product, shop, and global sell limits.
   *
   * @param player the player performing the sale
   * @param itemStacks the list of item stacks being sold
   * @param index the sell product index to match item stacks to products
   * @param config the active shop configuration
   * @param earnings a map to accumulate the total earnings from this sell batch per economy type
   * @param ctx the active shop context
   * @return a list of SellAction objects representing the allowed sales within limits
   */
  private static List<SellAction> collectSellActionsWithLimit(ServerPlayerEntity player, List<ItemStack> itemStacks,
                                                              SellProductIndex index, ShopConfig config,
                                                              Map<EconomyUse, BigDecimal> earnings, ShopContext ctx) {
    UserInfo dbUser = ctx.getRepositories().getUserRepository().findByUuid(player.getUuid());
    final UserInfo userInfo = dbUser != null ? dbUser : new UserInfo(player.getUuid(), player.getGameProfile().getName());

    SellLimitService.BatchTracker tracker = new SellLimitService.BatchTracker(userInfo, config);
    List<SellAction> actions = new ArrayList<>();

    for (ItemStack itemStack : itemStacks) {
      if (itemStack.isEmpty()) continue;

      List<SellProductIndex.SellEntry> entries = index.findSellable(itemStack, player);
      for (SellProductIndex.SellEntry entry : entries) {
        if (!PriceCalculator.canSell(entry.product(), player, entry.shop(), config)) continue;

        Map<EconomyUse, BigDecimal> perUnit = PriceCalculator.getSellPricesPerUnit(entry.product(), entry.shop());
        if (perUnit.isEmpty()) continue;

        int allowedCount = tracker.calculateAllowedAmount(entry.product(), entry.shop(), perUnit, itemStack.getCount());
        if (allowedCount > 0) {
          Map<EconomyUse, BigDecimal> totals = tracker.recordSale(entry.product(), entry.shop(), allowedCount, perUnit, earnings);
          actions.add(new SellAction(itemStack, entry.shop(), entry.product(), allowedCount, totals));
        }
        break;
      }
    }
    return actions;
  }

  private static void notifySellBlocked(ServerPlayerEntity player, SellLimitService.LimitCheckResult result, ShopContext ctx) {
    String msg = switch (result.blockReasonType()) {
      case "product" -> ctx.getLang().getMessageYouCantSellNow()
        .replace("%limit%", result.limitValue().toPlainString())
        .replace("%time%", String.valueOf(result.cooldownRemainingSeconds()));
      case "shop" -> ctx.getLang().getMessageShopDailySellLimitReached()
        .replace("%limit%", result.limitValue().toPlainString())
        .replace("%currency%", result.currency());
      case "global" -> ctx.getLang().getMessageDailySellLimitReached()
        .replace("%limit%", result.limitValue().toPlainString())
        .replace("%currency%", result.currency());
      default -> null;
    };
    if (msg != null) {
      PlayerUtils.sendMessage(player, msg, ctx.getLang().getPrefix(), TypeMessage.CHAT);
    }
  }

  private static void logSellAllTiming(ShopConfig config, long start) {
    if (config != null && config.isDebug()) {
      UltraShop.LOGGER.info("SellAll took " + (System.currentTimeMillis() - start) + "ms");
    }
  }

  private record SellAction(ItemStack itemStack, Shop shop, Product product, int amount,
                            Map<EconomyUse, BigDecimal> totals) {
  }
}

