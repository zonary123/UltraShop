package com.kingpixel.ultrashop.api.ui;

import com.kingpixel.ultrashop.presentation.gui.DefaultShopUiProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for {@link ShopUiProvider} instances per mod.
 * Allows addon mods to register custom UI providers and override default interfaces.
 */
public final class ShopUiRegistry {

  private static final ShopUiProvider DEFAULT_PROVIDER = new DefaultShopUiProvider();
  private static volatile ShopUiProvider globalDefaultProvider = DEFAULT_PROVIDER;
  private static final Map<String, ShopUiProvider> PROVIDERS = new ConcurrentHashMap<>();

  private ShopUiRegistry() {
  }

  /**
   * Registers a custom UI provider for a specific modId.
   *
   * @param modId    the mod identifier
   * @param provider the UI provider implementation
   */
  public static void register(String modId, ShopUiProvider provider) {
    if (modId == null || provider == null) return;
    PROVIDERS.put(modId, provider);
  }

  /**
   * Unregisters a UI provider for a specific modId.
   *
   * @param modId the mod identifier
   */
  public static void unregister(String modId) {
    if (modId != null) {
      PROVIDERS.remove(modId);
    }
  }

  /**
   * Retrieves the UI provider for a modId, or the global default if none is registered.
   *
   * @param modId the mod identifier
   * @return the registered UI provider, or the default provider
   */
  public static ShopUiProvider get(String modId) {
    if (modId != null) {
      ShopUiProvider provider = PROVIDERS.get(modId);
      if (provider != null) {
        return provider;
      }
    }
    return globalDefaultProvider;
  }

  /**
   * Overrides the global default UI provider for all mods that do not specify their own.
   *
   * @param provider the new global default provider
   */
  public static void setDefaultProvider(ShopUiProvider provider) {
    globalDefaultProvider = provider != null ? provider : DEFAULT_PROVIDER;
  }

  /**
   * Gets the active global default provider.
   *
   * @return the global default UI provider
   */
  public static ShopUiProvider getDefaultProvider() {
    return globalDefaultProvider;
  }
}
