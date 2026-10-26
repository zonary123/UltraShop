package com.kingpixel.ultrashop.domain.service;

import com.kingpixel.cobbleutils.Model.EconomyUse;
import com.kingpixel.ultrashop.domain.model.Product;
import com.kingpixel.ultrashop.domain.model.UserInfo;
import com.kingpixel.ultrashop.domain.model.shop.ShopReference;
import com.kingpixel.ultrashop.infrastructure.config.ShopConfig;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for evaluating and tracking product, shop, and global sell limits.
 */
public final class SellLimitService {

  private SellLimitService() {
  }

  public record LimitCheckResult(
    int allowedAmount,
    boolean blocked,
    String blockReasonType,
    String currency,
    BigDecimal limitValue,
    long cooldownRemainingSeconds
  ) {
    public static LimitCheckResult allowed(int amount) {
      return new LimitCheckResult(amount, false, "", "", BigDecimal.ZERO, 0);
    }

    public static LimitCheckResult productBlocked(int max, long cooldownSeconds) {
      return new LimitCheckResult(0, true, "product", "", BigDecimal.valueOf(max), cooldownSeconds);
    }

    public static LimitCheckResult shopBlocked(String currency, BigDecimal limit) {
      return new LimitCheckResult(0, true, "shop", currency, limit, 0);
    }

    public static LimitCheckResult globalBlocked(String currency, BigDecimal limit) {
      return new LimitCheckResult(0, true, "global", currency, limit, 0);
    }
  }

  /**
   * Evaluates the allowed sell amount for a single product.
   */
  public static LimitCheckResult evaluateSellLimit(
    UserInfo userInfo,
    Product product,
    ShopReference shop,
    @Nullable ShopConfig config,
    Map<EconomyUse, BigDecimal> sellPerUnit,
    int requestedAmount
  ) {
    int allowed = requestedAmount;

    // 1. Product Sell Limit
    if (product.getSellMax() != null && product.getSellUuid() != null) {
      int currentSold = userInfo.getActualProductSellLimit(product);
      int remaining = product.getSellMax() - currentSold;
      if (remaining <= 0) {
        long time = Math.max(0, (userInfo.getProductSellCooldown(product) - System.currentTimeMillis()) / 1000);
        return LimitCheckResult.productBlocked(product.getSellMax(), time);
      }
      allowed = Math.min(allowed, remaining);
    }

    // 2. Shop Daily Limits
    if (shop.getDailySellLimits() != null && !shop.getDailySellLimits().isEmpty()) {
      userInfo.checkShopDailySellReset(shop.getId(), shop.getDailySellResetCooldown());
      for (Map.Entry<EconomyUse, BigDecimal> entry : sellPerUnit.entrySet()) {
        String currency = entry.getKey().getCurrency();
        BigDecimal limit = shop.getDailySellLimits().get(currency);
        if (limit != null && limit.compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal current = userInfo.getShopDailySellEarnings(shop.getId(), currency);
          BigDecimal remainingEarnings = limit.subtract(current);
          if (remainingEarnings.compareTo(BigDecimal.ZERO) <= 0) {
            return LimitCheckResult.shopBlocked(currency, limit);
          }
          BigDecimal unitPrice = entry.getValue();
          if (unitPrice.compareTo(BigDecimal.ZERO) > 0) {
            int maxUnits = remainingEarnings.divide(unitPrice, 0, RoundingMode.DOWN).intValue();
            allowed = Math.min(allowed, maxUnits);
            if (allowed == 0) {
              return LimitCheckResult.shopBlocked(currency, limit);
            }
          }
        }
      }
    }

    // 3. Global Daily Limits
    if (config != null && config.getDailySellLimits() != null && !config.getDailySellLimits().isEmpty()) {
      userInfo.checkDailySellReset(config.getDailySellResetCooldown());
      for (Map.Entry<EconomyUse, BigDecimal> entry : sellPerUnit.entrySet()) {
        String currency = entry.getKey().getCurrency();
        BigDecimal limit = config.getDailySellLimits().get(currency);
        if (limit != null && limit.compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal current = userInfo.getDailySellEarnings(currency);
          BigDecimal remainingEarnings = limit.subtract(current);
          if (remainingEarnings.compareTo(BigDecimal.ZERO) <= 0) {
            return LimitCheckResult.globalBlocked(currency, limit);
          }
          BigDecimal unitPrice = entry.getValue();
          if (unitPrice.compareTo(BigDecimal.ZERO) > 0) {
            int maxUnits = remainingEarnings.divide(unitPrice, 0, RoundingMode.DOWN).intValue();
            allowed = Math.min(allowed, maxUnits);
            if (allowed == 0) {
              return LimitCheckResult.globalBlocked(currency, limit);
            }
          }
        }
      }
    }

    return LimitCheckResult.allowed(allowed);
  }

  /**
   * Applies the sales earnings and updates product, shop, and global daily limits on the user info.
   */
  public static void applyEarningsAndLimits(
    UserInfo userInfo,
    Product product,
    ShopReference shop,
    @Nullable ShopConfig config,
    int amount,
    Map<EconomyUse, BigDecimal> totals
  ) {
    if (product.getSellMax() != null && product.getSellUuid() != null) {
      userInfo.addDailyProductSellLimit(product, amount);
    }
    if (shop.getDailySellLimits() != null && !shop.getDailySellLimits().isEmpty()) {
      for (Map.Entry<EconomyUse, BigDecimal> entry : totals.entrySet()) {
        String currency = entry.getKey().getCurrency();
        if (shop.getDailySellLimits().containsKey(currency)) {
          userInfo.addShopDailySellEarnings(shop.getId(), currency, entry.getValue(), shop.getDailySellResetCooldown());
        }
      }
    }
    if (config != null && config.getDailySellLimits() != null && !config.getDailySellLimits().isEmpty()) {
      for (Map.Entry<EconomyUse, BigDecimal> entry : totals.entrySet()) {
        String currency = entry.getKey().getCurrency();
        if (config.getDailySellLimits().containsKey(currency)) {
          userInfo.addDailySellEarnings(currency, entry.getValue(), config.getDailySellResetCooldown());
        }
      }
    }
  }

  /**
   * Tracks remaining limits across a batch of sales (for sellAll).
   */
  public static class BatchTracker {
    private final UserInfo userInfo;
    private final ShopConfig config;
    private final Map<UUID, Integer> remainingProductLimits = new HashMap<>();
    private final Map<String, Map<String, BigDecimal>> remainingShopLimits = new HashMap<>();
    private final Map<String, BigDecimal> remainingGlobalLimits = new HashMap<>();

    public BatchTracker(UserInfo userInfo, ShopConfig config) {
      this.userInfo = userInfo;
      this.config = config;

      if (config != null) {
        userInfo.checkDailySellReset(config.getDailySellResetCooldown());
        if (config.getDailySellLimits() != null) {
          for (Map.Entry<String, BigDecimal> entry : config.getDailySellLimits().entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
              BigDecimal current = userInfo.getDailySellEarnings(entry.getKey());
              BigDecimal remaining = entry.getValue().subtract(current);
              remainingGlobalLimits.put(entry.getKey(), remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO);
            }
          }
        }
      }
    }

    public int calculateAllowedAmount(Product product, ShopReference shop, Map<EconomyUse, BigDecimal> perUnit, int requestedAmount) {
      int allowed = requestedAmount;

      // 1. Product Sell Limit
      if (product.getSellMax() != null && product.getSellUuid() != null) {
        if (remainingProductLimits.containsKey(product.getSellUuid())) {
          allowed = Math.min(allowed, remainingProductLimits.get(product.getSellUuid()));
        } else {
          if (userInfo.canSellProduct(product)) {
            int remaining = product.getSellMax() - userInfo.getActualProductSellLimit(product);
            remainingProductLimits.put(product.getSellUuid(), remaining);
            allowed = Math.min(allowed, remaining);
          } else {
            allowed = 0;
          }
        }
      }

      if (allowed <= 0) return 0;

      // 2. Shop Daily Limits
      if (shop.getDailySellLimits() != null && !shop.getDailySellLimits().isEmpty()) {
        Map<String, BigDecimal> shopLimits = remainingShopLimits.computeIfAbsent(shop.getId(), id -> {
          userInfo.checkShopDailySellReset(shop.getId(), shop.getDailySellResetCooldown());
          Map<String, BigDecimal> limits = new HashMap<>();
          for (Map.Entry<String, BigDecimal> limitEntry : shop.getDailySellLimits().entrySet()) {
            if (limitEntry.getValue().compareTo(BigDecimal.ZERO) > 0) {
              BigDecimal current = userInfo.getShopDailySellEarnings(shop.getId(), limitEntry.getKey());
              BigDecimal remaining = limitEntry.getValue().subtract(current);
              limits.put(limitEntry.getKey(), remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO);
            }
          }
          return limits;
        });

        for (Map.Entry<EconomyUse, BigDecimal> ecoEntry : perUnit.entrySet()) {
          String currency = ecoEntry.getKey().getCurrency();
          BigDecimal remainingLimit = shopLimits.get(currency);
          if (remainingLimit != null) {
            BigDecimal unitPrice = ecoEntry.getValue();
            if (unitPrice.compareTo(BigDecimal.ZERO) > 0) {
              int maxUnits = remainingLimit.divide(unitPrice, 0, RoundingMode.DOWN).intValue();
              allowed = Math.min(allowed, maxUnits);
            } else {
              allowed = 0;
            }
          }
        }
      }

      if (allowed <= 0) return 0;

      // 3. Global Daily Limits
      if (config != null && config.getDailySellLimits() != null && !config.getDailySellLimits().isEmpty()) {
        for (Map.Entry<EconomyUse, BigDecimal> ecoEntry : perUnit.entrySet()) {
          String currency = ecoEntry.getKey().getCurrency();
          BigDecimal remainingLimit = remainingGlobalLimits.get(currency);
          if (remainingLimit != null) {
            BigDecimal unitPrice = ecoEntry.getValue();
            if (unitPrice.compareTo(BigDecimal.ZERO) > 0) {
              int maxUnits = remainingLimit.divide(unitPrice, 0, RoundingMode.DOWN).intValue();
              allowed = Math.min(allowed, maxUnits);
            } else {
              allowed = 0;
            }
          }
        }
      }

      return Math.max(0, allowed);
    }

    public Map<EconomyUse, BigDecimal> recordSale(
      Product product,
      ShopReference shop,
      int allowedCount,
      Map<EconomyUse, BigDecimal> perUnit,
      Map<EconomyUse, BigDecimal> globalEarnings
    ) {
      if (product.getSellMax() != null && product.getSellUuid() != null) {
        remainingProductLimits.put(product.getSellUuid(), remainingProductLimits.get(product.getSellUuid()) - allowedCount);
      }

      Map<EconomyUse, BigDecimal> totals = new LinkedHashMap<>();
      for (Map.Entry<EconomyUse, BigDecimal> ecoEntry : perUnit.entrySet()) {
        String currency = ecoEntry.getKey().getCurrency();
        BigDecimal total = ecoEntry.getValue().multiply(BigDecimal.valueOf(allowedCount));
        totals.put(ecoEntry.getKey(), total);
        globalEarnings.merge(ecoEntry.getKey(), total, BigDecimal::add);

        if (shop.getDailySellLimits() != null && !shop.getDailySellLimits().isEmpty()) {
          Map<String, BigDecimal> shopLimits = remainingShopLimits.get(shop.getId());
          if (shopLimits != null) {
            BigDecimal remainingLimit = shopLimits.get(currency);
            if (remainingLimit != null) {
              shopLimits.put(currency, remainingLimit.subtract(total));
            }
          }
        }

        BigDecimal remainingLimit = remainingGlobalLimits.get(currency);
        if (remainingLimit != null) {
          remainingGlobalLimits.put(currency, remainingLimit.subtract(total));
        }
      }
      return totals;
    }
  }
}
