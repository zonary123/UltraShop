package com.kingpixel.ultrashop.api.event;

import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.ultrashop.ShopContext;
import com.kingpixel.ultrashop.UltraShop;
import com.kingpixel.ultrashop.domain.model.ActionShop;
import com.kingpixel.ultrashop.domain.model.Product;
import com.kingpixel.ultrashop.domain.model.shop.Shop;
import com.kingpixel.ultrashop.domain.model.shop.ShopReference;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Central event bus for UltraShop lifecycle and transaction events.
 *
 * <p>Addons can register listeners here to intercept or be notified of shop events.</p>
 */
public final class ShopEvents {

  private static final List<Consumer<ShopOpenEvent>> OPEN_LISTENERS = new CopyOnWriteArrayList<>();
  private static final List<Consumer<ShopPreTransactionEvent>> PRE_TRANSACTION_LISTENERS = new CopyOnWriteArrayList<>();
  private static final List<Consumer<ShopPostTransactionEvent>> POST_TRANSACTION_LISTENERS = new CopyOnWriteArrayList<>();

  private ShopEvents() {
  }

  /**
   * Registers a listener to be called when a player opens a shop.
   */
  public static void onShopOpen(Consumer<ShopOpenEvent> listener) {
    if (listener != null) {
      OPEN_LISTENERS.add(listener);
    }
  }

  /**
   * Registers a listener to be called immediately before a transaction executes.
   */
  public static void onPreTransaction(Consumer<ShopPreTransactionEvent> listener) {
    if (listener != null) {
      PRE_TRANSACTION_LISTENERS.add(listener);
    }
  }

  /**
   * Registers a listener to be called after a transaction completes.
   */
  public static void onPostTransaction(Consumer<ShopPostTransactionEvent> listener) {
    if (listener != null) {
      POST_TRANSACTION_LISTENERS.add(listener);
    }
  }

  /**
   * Dispatches the shop open event.
   *
   * @return true if opening is permitted, false if canceled
   */
  public static boolean fireShopOpen(ServerPlayerEntity player, Shop shop, String modId) {
    if (OPEN_LISTENERS.isEmpty()) return true;
    ShopOpenEvent event = new ShopOpenEvent(player, shop, modId);
    for (Consumer<ShopOpenEvent> listener : OPEN_LISTENERS) {
      try {
        listener.accept(event);
        if (event.isCanceled()) {
          if (event.getCancelReason() != null && !event.getCancelReason().isBlank()) {
            PlayerUtils.sendMessage(player, event.getCancelReason(),
              ShopContext.get().getLang().getPrefix(), TypeMessage.CHAT);
          }
          return false;
        }
      } catch (Exception e) {
        UltraShop.LOGGER.error("Error in ShopOpenEvent listener: {}", e.getMessage(), e);
      }
    }
    return true;
  }

  /**
   * Dispatches the pre-transaction event.
   */
  public static ShopPreTransactionEvent firePreTransaction(ServerPlayerEntity player, Product product,
                                                           ShopReference shop, int amount, ActionShop action) {
    ShopPreTransactionEvent event = new ShopPreTransactionEvent(player, product, shop, amount, action);
    for (Consumer<ShopPreTransactionEvent> listener : PRE_TRANSACTION_LISTENERS) {
      try {
        listener.accept(event);
        if (event.isCanceled()) {
          return event;
        }
      } catch (Exception e) {
        UltraShop.LOGGER.error("Error in ShopPreTransactionEvent listener: {}", e.getMessage(), e);
      }
    }
    return event;
  }

  /**
   * Dispatches the post-transaction event.
   */
  public static void firePostTransaction(ServerPlayerEntity player, Product product,
                                         ShopReference shop, int amount, ActionShop action, boolean success) {
    if (POST_TRANSACTION_LISTENERS.isEmpty()) return;
    ShopPostTransactionEvent event = new ShopPostTransactionEvent(player, product, shop, amount, action, success);
    for (Consumer<ShopPostTransactionEvent> listener : POST_TRANSACTION_LISTENERS) {
      try {
        listener.accept(event);
      } catch (Exception e) {
        UltraShop.LOGGER.error("Error in ShopPostTransactionEvent listener: {}", e.getMessage(), e);
      }
    }
  }
}
