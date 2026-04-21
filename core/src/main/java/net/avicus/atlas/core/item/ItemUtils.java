package net.avicus.atlas.core.item;

import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtils {

  public static final ItemTag.Boolean LOCKED = new ItemTag.Boolean("locked", false);
  public static final ItemTag.Boolean UN_SHAREABLE = new ItemTag.Boolean("un-shareable", false);

  public static ItemStack lock(ItemStack stack) {
    LOCKED.set(stack, true);
    return stack;
  }

  public static Optional<ItemMeta> tryMeta(ItemStack item) {
    return item.hasItemMeta() ? Optional.of(item.getItemMeta())
        : Optional.empty();
  }

  public static void updateMeta(ItemStack item, Consumer<ItemMeta> mutator) {
    final ItemMeta meta = item.getItemMeta();
    mutator.accept(meta);
    item.setItemMeta(meta);
  }

  public static void updateMetaIfPresent(@Nullable ItemStack item, Consumer<ItemMeta> mutator) {
    if (item != null && item.hasItemMeta()) {
      updateMeta(item, mutator);
    }
  }

  public static ItemStack normalize(ItemStack item) {
    // Ignore non-data durability
    if (item.getType().getMaxDurability() != 0) {
      item.setDurability((short) 0);
    }

    UN_SHAREABLE.clear(item);
    LOCKED.clear(item);

    return item;
  }
}
