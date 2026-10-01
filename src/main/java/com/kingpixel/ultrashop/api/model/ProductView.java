package com.kingpixel.ultrashop.api.model;

import com.kingpixel.cobbleutils.Model.EconomyUse;
import com.kingpixel.ultrashop.domain.model.Product;
import lombok.Builder;
import lombok.Data;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Read-only view model representing a product prepared for rendering in custom UIs.
 * Contains calculated pricing, availability, limits, stock, and display information for a specific player.
 */
@Data
@Builder
public class ProductView {

  /**
   * The underlying domain product.
   */
  @NotNull
  private final Product product;

  /**
   * Calculated total buy prices per economy (with player discounts applied).
   */
  @NotNull
  private final Map<EconomyUse, BigDecimal> buyPrices;

  /**
   * Calculated total sell prices per economy.
   */
  @NotNull
  private final Map<EconomyUse, BigDecimal> sellPrices;

  /**
   * Active discount percentage applied to this product for the player.
   */
  private final float discountPercent;

  /**
   * Whether the player is permitted and eligible to buy this product (permission, stock, limit, cooldown).
   */
  private final boolean canBuy;

  /**
   * Whether the player is permitted and eligible to sell this product.
   */
  private final boolean canSell;

  /**
   * Whether the player currently has enough funds across all required economies to purchase.
   */
  private final boolean canAfford;

  /**
   * Remaining stock available for purchase, or {@code null} if stock control is not enabled.
   */
  @Nullable
  private final Long remainingStock;

  /**
   * Remaining purchases allowed under personal max limit, or {@code null} if no limit is set.
   */
  @Nullable
  private final Integer remainingLimit;

  /**
   * Seconds remaining on purchase cooldown, or 0 if ready to buy.
   */
  private final long cooldownRemainingSeconds;

  /**
   * The resolved Minecraft ItemStack representing this product for GUI display.
   */
  @NotNull
  private final ItemStack displayStack;

  /**
   * The resolved title / display name.
   */
  @NotNull
  private final String displayName;

  /**
   * The resolved lore lines with placeholders replaced.
   */
  @NotNull
  private final List<String> lore;
}
