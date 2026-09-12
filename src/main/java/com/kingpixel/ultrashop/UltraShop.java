package com.kingpixel.ultrashop;

import com.kingpixel.cobbleutils.util.UtilsFile;
import com.kingpixel.cobbleutils.util.UtilsLogger;
import com.kingpixel.ultrashop.api.ShopApi;
import com.kingpixel.ultrashop.api.ShopOptionsApi;
import com.kingpixel.ultrashop.domain.model.shop.CategoryShop;
import com.kingpixel.ultrashop.domain.model.shop.NormalShop;
import com.kingpixel.ultrashop.domain.model.shop.RotationShop;
import com.kingpixel.ultrashop.domain.model.shop.Shop;
import com.kingpixel.ultrashop.domain.scheduler.Scheduler;
import com.kingpixel.ultrashop.domain.service.TransactionService;
import com.kingpixel.ultrashop.infrastructure.persistence.json.JsonUserRepository;
import com.kingpixel.ultrashop.infrastructure.persistence.mongodb.MongoUserRepository;
import com.kingpixel.ultrashop.infrastructure.serialization.scheduler.SchedulerJsonAdapter;
import com.kingpixel.ultrashop.infrastructure.serialization.shop.ShopTypeAdapterFactory;
import com.kingpixel.ultrashop.presentation.gui.edit.ChatInputManager;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import org.apache.logging.log4j.Logger;

/**
 * UltraShop v2 — Minimal bootstrap.
 * All state lives in {@link ShopContext}, async in UtilsAsync, I/O in UtilsFile.
 *
 * @author Carlos Varas Alonso
 */
public class UltraShop implements ModInitializer {

  public static final String MOD_ID = "ultrashop";
  public static final String MOD_NAME = "UltraShop";
  public static final String PATH = "ultrashop/";
  public static final Logger LOGGER = UtilsLogger.getLogger(MOD_ID);

  @Override
  public void onInitialize() {
    ShopTypeAdapterFactory shopAdapterFactory = new ShopTypeAdapterFactory();
    UtilsFile.registerAdapter(Shop.class, shopAdapterFactory);
    UtilsFile.registerAdapter(NormalShop.class, shopAdapterFactory);
    UtilsFile.registerAdapter(RotationShop.class, shopAdapterFactory);
    UtilsFile.registerAdapter(CategoryShop.class, shopAdapterFactory);
    UtilsFile.registerAdapter(Scheduler.class, new SchedulerJsonAdapter());

    ShopContext.get().init();
    registerEvents();
  }

  private void registerEvents() {
    ShopOptionsApi defaultOptions = ShopOptionsApi.builder()
      .modId(MOD_ID)
      .path(PATH)
      .build();

    LifecycleEvent.SERVER_STOPPING.register(event -> {
      ShopContext.get().getDataShop().write();
      ShopContext.get().shutdown();
    });

    CommandRegistrationEvent.EVENT.register((dispatcher, commandRegistryAccess, registrationEnvironment) ->
      ShopApi.register(defaultOptions, dispatcher));

    PlayerEvent.PLAYER_JOIN.register(player -> ShopContext.get().getAsyncContext().runAsync(() -> {
      var repo = ShopContext.get().getRepositories();
      if (repo != null) {
        if (repo.getUserRepository() instanceof JsonUserRepository jsonRepo) {
          jsonRepo.findByPlayer(player);
        } else if (repo.getUserRepository() instanceof MongoUserRepository mongoRepo) {
          mongoRepo.findByPlayer(player);
        }
      }
    }));

    PlayerEvent.PLAYER_QUIT.register(player -> {
      TransactionService.removeSellLock(player.getUuid());
      ChatInputManager.clear(player.getUuid());
      var repo = ShopContext.get().getRepositories();
      if (repo != null) {
        repo.getUserRepository().remove(player.getUuid());
      }
    });

    ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
      if (ChatInputManager.hasPending(sender.getUuid())) {
        return !ChatInputManager.handleChat(sender, message.getContent().getString());
      }
      return true;
    });
  }
}
