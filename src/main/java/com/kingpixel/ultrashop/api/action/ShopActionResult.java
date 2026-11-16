package com.kingpixel.ultrashop.api.action;

import com.kingpixel.cobbleutils.Model.EconomyUse;
import com.kingpixel.ultrashop.domain.model.ActionShop;
import com.kingpixel.ultrashop.domain.model.Product;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Result model for shop buy and sell operations executed programmatically.
 */
@Data
@Builder
public class ShopActionResult {

  public enum Status {
    SUCCESS,
    NOT_ENOUGH_MONEY,
    OUT_OF_STOCK,
    INVENTORY_FULL,
    ON_COOLDOWN,
    LIMIT_REACHED,
    NOT_ENOUGH_ITEMS,
    CANNOT_BUY,
    CANNOT_SELL,
    DAILY_SELL_LIMIT_EXCEEDED,
    SHOP_CLOSED,
    EVENT_CANCELED,
    ERROR
  }

  private final Status status;
  private final String message;
  @Nullable
  private final Product product;
  private final int requestedAmount;
  private final int processedAmount;
  private final ActionShop action;
  private final Map<EconomyUse, BigDecimal> prices;
  private final long cooldownRemainingSeconds;
  @Nullable
  private final Long remainingStock;
  @Nullable
  private final Integer remainingLimit;

  public boolean isSuccess() {
    return status == Status.SUCCESS;
  }

  public static ShopActionResult success(Product product, int amount, ActionShop action, Map<EconomyUse, BigDecimal> prices, String message) {
    return ShopActionResult.builder()
      .status(Status.SUCCESS)
      .message(message)
      .product(product)
      .requestedAmount(amount)
      .processedAmount(amount)
      .action(action)
      .prices(prices != null ? prices : Map.of())
      .cooldownRemainingSeconds(0)
      .build();
  }

  public static ShopActionResult failure(Status status, String message, @Nullable Product product, int requestedAmount, ActionShop action) {
    return ShopActionResult.builder()
      .status(status)
      .message(message)
      .product(product)
      .requestedAmount(requestedAmount)
      .processedAmount(0)
      .action(action)
      .prices(Map.of())
      .cooldownRemainingSeconds(0)
      .build();
  }
}
