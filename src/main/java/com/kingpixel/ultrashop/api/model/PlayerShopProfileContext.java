package com.kingpixel.ultrashop.api.model;

import com.kingpixel.cobbleutils.Model.EconomyUse;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Read-only profile context representing a player's shop balances, limits, and permissions.
 */
@Data
@Builder
public class PlayerShopProfileContext {

  @NotNull
  private final UUID playerUuid;

  @NotNull
  private final String playerName;

  @NotNull
  private final Map<EconomyUse, BigDecimal> balances;

  @NotNull
  private final Map<String, BigDecimal> dailySellEarnings;

  private final boolean canSellAll;
}
