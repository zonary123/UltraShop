package com.kingpixel.ultrashop.api.event;

import com.kingpixel.ultrashop.domain.model.shop.Shop;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Event fired when a player attempts to open a shop.
 * Can be canceled to prevent the shop menu from opening.
 */
@Getter
public class ShopOpenEvent {
  private final ServerPlayerEntity player;
  private final Shop shop;
  private final String modId;
  @Setter
  private boolean canceled = false;
  @Setter
  private String cancelReason = null;

  public ShopOpenEvent(ServerPlayerEntity player, Shop shop, String modId) {
    this.player = player;
    this.shop = shop;
    this.modId = modId;
  }

  /**
   * Cancels the opening of the shop and displays a message to the player.
   *
   * @param reason message sent to player explaining why the shop did not open
   */
  public void cancel(String reason) {
    this.canceled = true;
    this.cancelReason = reason;
  }
}
