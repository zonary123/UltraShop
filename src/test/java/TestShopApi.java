import com.kingpixel.cobbleutils.Model.EconomyUse;
import com.kingpixel.ultrashop.api.action.ShopActionResult;
import com.kingpixel.ultrashop.api.model.PlayerShopProfileContext;
import com.kingpixel.ultrashop.api.model.ProductView;
import com.kingpixel.ultrashop.api.model.ShopView;
import com.kingpixel.ultrashop.api.model.TransactionView;
import com.kingpixel.ultrashop.api.ui.ShopUiProvider;
import com.kingpixel.ultrashop.api.ui.ShopUiRegistry;
import com.kingpixel.ultrashop.domain.model.ActionShop;
import com.kingpixel.ultrashop.domain.model.Product;
import com.kingpixel.ultrashop.domain.model.ShopType;
import com.kingpixel.ultrashop.infrastructure.config.ShopConfig;
import com.kingpixel.ultrashop.presentation.gui.NavigationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TestShopApi {

  @AfterEach
  void tearDown() {
    ShopUiRegistry.unregister("test_mod");
    ShopUiRegistry.setDefaultProvider(null);
  }

  @Test
  @DisplayName("ShopActionResult represents success state and metadata")
  void testActionResultSuccess() {
    Product product = new Product();
    product.setProduct("minecraft:diamond");

    Map<EconomyUse, BigDecimal> prices = Map.of(new EconomyUse("vault", "dollar"), BigDecimal.valueOf(100));

    ShopActionResult result = ShopActionResult.success(product, 2, ActionShop.BUY, prices, "Purchased 2 diamonds");

    assertTrue(result.isSuccess());
    assertEquals(ShopActionResult.Status.SUCCESS, result.getStatus());
    assertEquals(2, result.getProcessedAmount());
    assertEquals(2, result.getRequestedAmount());
    assertEquals(ActionShop.BUY, result.getAction());
    assertEquals(1, result.getPrices().size());
    assertEquals("Purchased 2 diamonds", result.getMessage());
  }

  @Test
  @DisplayName("ShopActionResult represents failure state correctly")
  void testActionResultFailure() {
    Product product = new Product();
    product.setProduct("minecraft:emerald");

    ShopActionResult result = ShopActionResult.failure(
      ShopActionResult.Status.NOT_ENOUGH_MONEY,
      "Insufficient balance",
      product,
      5,
      ActionShop.BUY
    );

    assertFalse(result.isSuccess());
    assertEquals(ShopActionResult.Status.NOT_ENOUGH_MONEY, result.getStatus());
    assertEquals(0, result.getProcessedAmount());
    assertEquals(5, result.getRequestedAmount());
    assertEquals("Insufficient balance", result.getMessage());
  }

  @Test
  @DisplayName("ShopUiRegistry dispatches custom provider and falls back to default")
  void testShopUiRegistry() {
    ShopUiProvider defaultProvider = ShopUiRegistry.getDefaultProvider();
    assertNotNull(defaultProvider);

    // Initial query should return default
    assertEquals(defaultProvider, ShopUiRegistry.get("test_mod"));

    // Register custom provider
    final boolean[] opened = {false};
    ShopUiProvider customProvider = new ShopUiProvider() {
      @Override
      public void openMainMenu(ServerPlayerEntity player, ShopConfig config, String modId) {
        opened[0] = true;
      }

      @Override
      public void openShop(ServerPlayerEntity player, com.kingpixel.ultrashop.domain.model.shop.Shop shop,
                           NavigationContext nav, ShopConfig config, boolean withClose) {
      }
    };

    ShopUiRegistry.register("test_mod", customProvider);
    assertEquals(customProvider, ShopUiRegistry.get("test_mod"));
    assertEquals(defaultProvider, ShopUiRegistry.get("other_mod"));

    // Unregister should revert to default
    ShopUiRegistry.unregister("test_mod");
    assertEquals(defaultProvider, ShopUiRegistry.get("test_mod"));
  }

  @Test
  @DisplayName("ShopView correctly holds shop metadata and active product views")
  void testShopView() {
    ShopView view = ShopView.builder()
      .id("pokemon_balls")
      .modId("ultrashop")
      .name("Poke Balls")
      .title("&aPoke Ball Emporium")
      .type(ShopType.NORMAL)
      .displayStack(null)
      .canAccess(true)
      .maintenance(false)
      .productsCount(5)
      .activeProducts(List.of())
      .subShopIds(List.of())
      .subShops(List.of())
      .build();

    assertEquals("pokemon_balls", view.getId());
    assertEquals("ultrashop", view.getModId());
    assertEquals("Poke Balls", view.getName());
    assertEquals(ShopType.NORMAL, view.getType());
    assertTrue(view.isCanAccess());
    assertFalse(view.isMaintenance());
    assertEquals(5, view.getProductsCount());
  }

  @Test
  @DisplayName("TransactionView models auditing history records")
  void testTransactionView() {
    UUID uuid = UUID.randomUUID();
    TransactionView tx = TransactionView.builder()
      .playerUuid(uuid)
      .playerName("AshKetchum")
      .shopId("potions")
      .productId("cobblemon:hyper_potion")
      .action(ActionShop.BUY)
      .amount(3)
      .value(BigDecimal.valueOf(1500))
      .currency("dollar")
      .formattedPrice("$1,500.00")
      .timestamp(1700000000000L)
      .build();

    assertEquals(uuid, tx.getPlayerUuid());
    assertEquals("AshKetchum", tx.getPlayerName());
    assertEquals("potions", tx.getShopId());
    assertEquals(ActionShop.BUY, tx.getAction());
    assertEquals(3, tx.getAmount());
    assertEquals(BigDecimal.valueOf(1500), tx.getValue());
    assertEquals("$1,500.00", tx.getFormattedPrice());
  }

  @Test
  @DisplayName("PlayerShopProfileContext tracks multi-economy balances")
  void testPlayerShopProfileContext() {
    UUID uuid = UUID.randomUUID();
    EconomyUse ecoDollar = new EconomyUse("vault", "dollar");
    EconomyUse ecoPoints = new EconomyUse("impactor", "points");

    PlayerShopProfileContext profile = PlayerShopProfileContext.builder()
      .playerUuid(uuid)
      .playerName("Red")
      .balances(Map.of(ecoDollar, BigDecimal.valueOf(5000), ecoPoints, BigDecimal.valueOf(120)))
      .dailySellEarnings(Map.of("dollar", BigDecimal.valueOf(350)))
      .canSellAll(true)
      .build();

    assertEquals(uuid, profile.getPlayerUuid());
    assertEquals("Red", profile.getPlayerName());
    assertEquals(2, profile.getBalances().size());
    assertEquals(BigDecimal.valueOf(5000), profile.getBalances().get(ecoDollar));
    assertEquals(BigDecimal.valueOf(350), profile.getDailySellEarnings().get("dollar"));
    assertTrue(profile.isCanSellAll());
  }

  @Test
  @DisplayName("ProductView models availability, stock and pricing")
  void testProductView() {
    Product product = new Product();
    product.setProduct("cobblemon:rare_candy");

    ProductView view = ProductView.builder()
      .product(product)
      .buyPrices(Map.of())
      .sellPrices(Map.of())
      .discountPercent(10.0f)
      .canBuy(true)
      .canSell(false)
      .canAfford(true)
      .remainingStock(25L)
      .remainingLimit(5)
      .cooldownRemainingSeconds(0)
      .displayStack(null)
      .displayName("Rare Candy")
      .lore(List.of("Instantly raises a Pokemon's level by 1"))
      .build();

    assertEquals(product, view.getProduct());
    assertEquals(10.0f, view.getDiscountPercent());
    assertTrue(view.isCanBuy());
    assertFalse(view.isCanSell());
    assertTrue(view.isCanAfford());
    assertEquals(25L, view.getRemainingStock());
    assertEquals(5, view.getRemainingLimit());
    assertEquals(0, view.getCooldownRemainingSeconds());
    assertEquals("Rare Candy", view.getDisplayName());
    assertEquals(1, view.getLore().size());
  }
}
