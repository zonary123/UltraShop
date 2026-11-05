package com.kingpixel.ultrashop.presentation.gui.edit;

import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.ultrashop.ShopContext;
import com.kingpixel.ultrashop.infrastructure.config.LangConfig;
import net.minecraft.server.network.ServerPlayerEntity;

import ca.landonjw.gooeylibs2.api.UIManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Manages chat-based text input for admin GUI editing.
 * When a player needs to type a value (e.g., product name, command),
 * the GUI closes and their next chat message is captured as input.
 */
public final class ChatInputManager {

  private static final Map<UUID, PendingInput> pendingInputs = new ConcurrentHashMap<>();

  private ChatInputManager() {
  }

  /**
   * Requests text input from a player via chat.
   * Closes any open GUI and prompts the player to type.
   */
  public static void requestInput(ServerPlayerEntity player, String prompt, Consumer<String> callback) {
    UIManager.closeUI(player);
    pendingInputs.put(player.getUuid(), new PendingInput(callback, System.currentTimeMillis()));

    LangConfig lang = ShopContext.get().getLang();
    PlayerUtils.sendMessage(player,
      lang.getEditorChatInputPrefix() + prompt,
      "", TypeMessage.CHAT);
    PlayerUtils.sendMessage(player,
      lang.getEditorChatInputPrompt(),
      "", TypeMessage.CHAT);
  }

  /**
   * Called when a player sends a chat message. Returns true if the message was consumed.
   */
  public static boolean handleChat(ServerPlayerEntity player, String message) {
    PendingInput pending = pendingInputs.remove(player.getUuid());
    if (pending == null) return false;

    if (System.currentTimeMillis() - pending.timestamp > 60_000) return false;

    if (message.equalsIgnoreCase("cancel")) {
      PlayerUtils.sendMessage(player, ShopContext.get().getLang().getEditorChatInputCancelled(), "", TypeMessage.CHAT);
      return true;
    }

    ShopContext.get().getAsyncContext().runAsync(() -> {
      try {
        pending.callback.accept(message.trim());
      } catch (Exception e) {
        PlayerUtils.sendMessage(player, ShopContext.get().getLang().getEditorChatInputError().replace("%error%", e.getMessage() != null ? e.getMessage() : ""), "", TypeMessage.CHAT);
      }
    });

    return true;
  }

  /**
   * Whether a player has a pending input request.
   */
  public static boolean hasPending(UUID uuid) {
    return pendingInputs.containsKey(uuid);
  }

  /**
   * Clears any pending input for a player.
   */
  public static void clear(UUID uuid) {
    pendingInputs.remove(uuid);
  }

  private record PendingInput(Consumer<String> callback, long timestamp) {
  }
}

