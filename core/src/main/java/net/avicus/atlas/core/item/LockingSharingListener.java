package net.avicus.atlas.core.item;

import javax.annotation.Nullable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * This prevents items from being moved/shared based on tags.
 */
public class LockingSharingListener implements Listener {

  private boolean isLocked(@Nullable ItemStack item) {
    return item != null && ItemUtils.LOCKED.get(item);
  }

  private boolean unShareable(@Nullable ItemStack item) {
    return item != null && (isLocked(item) || ItemUtils.UN_SHAREABLE.get(item));
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onInventoryClick(final InventoryClickEvent event) {
    if (isLocked(event.getCurrentItem())) {
      event.setCancelled(true);
      return;
    }

    // HOTBAR_SWAP/HOTBAR_MOVE_AND_READD can displace a locked hotbar item.
    // Use getWhoClicked().getInventory() — event.getInventory() is the top view
    // inventory (e.g. crafting grid when own inventory is open), not the hotbar.
    InventoryAction action = event.getAction();
    if (action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD) {
      if (isLocked(event.getWhoClicked().getInventory().getItem(event.getHotbarButton()))) {
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onCreativeInventoryAction(InventoryCreativeEvent event) {
    if (event.getWhoClicked().hasPermission("atlas.spectator.bypass-inventory-lock")) {
      return;
    }
    if (isLocked(event.getWhoClicked().getInventory().getItem(event.getSlot()))) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onDropItem(PlayerDropItemEvent event) {
    if (isLocked(event.getItemDrop().getItemStack())) {
      event.setCancelled(true);
    } else if (unShareable(event.getItemDrop().getItemStack())) {
      event.getItemDrop().remove();
    }
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onDeath(PlayerDeathEvent event) {
    event.getDrops().removeIf(this::unShareable);
  }
}
