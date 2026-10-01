package com.kingpixel.ultrashop.presentation.gui;

import com.kingpixel.ultrashop.api.ui.ShopUiProvider;
import com.kingpixel.ultrashop.domain.model.ActionShop;
import com.kingpixel.ultrashop.domain.model.Product;
import com.kingpixel.ultrashop.domain.model.shop.Shop;
import com.kingpixel.ultrashop.infrastructure.config.ShopConfig;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

/**
 * Default GooeyLibs implementation of {@link ShopUiProvider}.
 */
public final class DefaultShopUiProvider implements ShopUiProvider {

  @Override
  public void openMainMenu(ServerPlayerEntity player, ShopConfig config, String modId) {
    MainMenuBuilder.open(player, config, modId);
  }

  @Override
  public void openShop(ServerPlayerEntity player, Shop shop, NavigationContext nav, ShopConfig config, boolean withClose) {
    ShopMenuBuilder.openShop(player, shop, nav, config, withClose);
  }

  @Override
  public void openBuySell(ServerPlayerEntity player, NavigationContext nav, Product product,
                          int amount, ActionShop actionShop, ShopConfig config, boolean withClose) {
    BuyAndSellMenuBuilder.open(player, nav, product, amount, actionShop, config, withClose);
  }

  @Override
  public void openTransactions(ServerPlayerEntity player, UUID targetUuid, String targetName,
                               ShopConfig config, String modId) {
    TransactionMenuBuilder.open(player, targetUuid, targetName, config, modId);
  }

  @Override
  public void openSearch(ServerPlayerEntity player, String query, String modId) {
    SearchMenuBuilder.open(player, query, modId);
  }

  @Override
  public void openStats(ServerPlayerEntity player, ShopConfig config, String modId) {
    StatsMenuBuilder.open(player, config, modId);
  }

  @Override
  public void openSellGui(ServerPlayerEntity player) {
    SellGuiBuilder.open(player);
  }
}
