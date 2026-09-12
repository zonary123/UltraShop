package com.kingpixel.ultrashop.domain.model.shop;

import com.kingpixel.cobbleutils.Model.EconomyUse;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Minimal capability surface that both legacy and typed shops expose
 * for pricing, discounts, and placement logic.
 */
public interface ShopReference {

  /** Stable identifier for the shop (file name without {@code .json}). */
  String getId();

  /** Default currencies accepted by this shop, in declaration order. */
  LinkedHashSet<EconomyUse> getEconomies();

  /** Whether products without an explicit slot are auto-arranged. */
  boolean isAutoPlace();

  /** Shop-wide discount percentage applied to all products. */
  float getGlobalDiscount();

  /** Permission-keyed discount overrides ({@code "group.vip" -> 2.0f}). */
  Map<String, Float> getDiscounts();

  /** Shop-level daily sell limits. */
  default Map<String, BigDecimal> getDailySellLimits() {
    return Map.of();
  }

  /** Shop-level daily sell reset cooldown. */
  default String getDailySellResetCooldown() {
    return "24h";
  }
}

