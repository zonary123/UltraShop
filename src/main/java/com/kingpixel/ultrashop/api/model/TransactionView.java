package com.kingpixel.ultrashop.api.model;

import com.kingpixel.ultrashop.domain.model.ActionShop;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Read-only view model representing a recorded shop transaction.
 */
@Data
@Builder
public class TransactionView {

  @NotNull
  private final UUID playerUuid;

  @NotNull
  private final String playerName;

  @NotNull
  private final String shopId;

  @NotNull
  private final String productId;

  @NotNull
  private final ActionShop action;

  private final int amount;

  @NotNull
  private final BigDecimal value;

  @NotNull
  private final String currency;

  @Nullable
  private final String formattedPrice;

  private final long timestamp;
}
