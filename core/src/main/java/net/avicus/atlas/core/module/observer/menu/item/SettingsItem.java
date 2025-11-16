package net.avicus.atlas.core.module.observer.menu.item;

import net.avicus.atlas.core.item.ItemStackBuilder;
import net.avicus.atlas.core.module.observer.menu.ObserverMenu;
import net.avicus.atlas.core.module.observer.menu.ObserverMenuItem;
import net.avicus.atlas.core.util.Translations;
import net.avicus.compendium.settings.menu.SettingsMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class SettingsItem extends ObserverMenuItem {

    public SettingsItem(final Player viewer, final ObserverMenu parent, final int index) {
        super(viewer, parent, index);
    }

    @Override
    public void onClick(ClickType type) {
        if (type.isLeftClick()) {
            SettingsMenu.create(this.viewer).open();
        }
    }

    @Override
    public ItemStack getItemStack() {
        return ItemStackBuilder.start()
                .material(Material.ANVIL)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .displayName(Translations.MODULE_OBSERVER_MENU_ITEM_SETTINGS_NAME.with(ChatColor.AQUA)
                        .render(this.viewer))
                .lore(Translations.MODULE_OBSERVER_MENU_ITEM_SETTINGS_DESCRIPTION.with(ChatColor.GRAY)
                        .render(this.viewer), MAX_LENGTH)
                .build();
    }
}
