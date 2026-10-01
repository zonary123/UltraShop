package com.kingpixel.ultrashop.api.event;

import com.kingpixel.ultrashop.domain.model.ActionShop;
import com.kingpixel.ultrashop.domain.model.Product;
import com.kingpixel.ultrashop.domain.model.shop.ShopReference;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Event fired after a buy or sell transaction has successfully completed.
 */
@Getter
public class ShopPostTransactionEvent {
  private final ServerPlayerEntity player;
  private final Product product;
  private final ShopReference shop;
  private final int amount;
  private final ActionShop action;
  private final boolean success;

  public ShopPostTransactionEvent(ServerPlayerEntity player, Product product, ShopReference shop,
                                  int amount, ActionShop action, boolean success) {
    this.player = player;
    this.product = product;
    this.shop = shop;
    this.amount = amount;
    this.action = action;
    this.success = success;
  }
}
