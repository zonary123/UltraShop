package com.kingpixel.ultrashop.api.event;

import com.kingpixel.ultrashop.domain.model.ActionShop;
import com.kingpixel.ultrashop.domain.model.Product;
import com.kingpixel.ultrashop.domain.model.shop.ShopReference;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Event fired immediately before a buy or sell transaction is processed.
 * Can be canceled to prevent the transaction.
 */
@Getter
public class ShopPreTransactionEvent {
  private final ServerPlayerEntity player;
  private final Product product;
  private final ShopReference shop;
  private final int amount;
  private final ActionShop action;
  @Setter
  private boolean canceled = false;
  @Setter
  private String cancelReason = null;

  public ShopPreTransactionEvent(ServerPlayerEntity player, Product product, ShopReference shop,
                                 int amount, ActionShop action) {
    this.player = player;
    this.product = product;
    this.shop = shop;
    this.amount = amount;
    this.action = action;
  }

  /**
   * Cancels the transaction and displays a message to the player.
   *
   * @param reason message sent to player explaining why the transaction was blocked
   */
  public void cancel(String reason) {
    this.canceled = true;
    this.cancelReason = reason;
  }
}
