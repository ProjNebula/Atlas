package net.avicus.atlas.core.module.items;

import lombok.Getter;
import lombok.ToString;
import net.avicus.atlas.core.Atlas;
import net.avicus.atlas.core.item.ItemTag;
import net.avicus.atlas.core.match.Match;
import net.avicus.atlas.core.module.Module;
import net.avicus.atlas.core.module.checks.Check;
import net.avicus.atlas.core.util.Events;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ToString(exclude = "match")
public class ItemsModule implements Module {
  public static final String POWER_METADATA_TAG = "atlas.grenade-power";
  public static final String FIRE_METADATA_TAG = "atlas.grenade-fire";
  public static final String DESTROY_METADATA_TAG = "atlas.grenade-destroy";

  public static final ItemTag.Boolean GRENADE_TAG = new ItemTag.Boolean("atlas.grenade",
      true);
  public static final ItemTag.Double GRENADE_POWER_TAG = new ItemTag.Double("atlas.grenade-power",
      1.0);
  public static final ItemTag.Boolean GRENADE_FIRE_TAG = new ItemTag.Boolean("atlas.grenade-fire",
      false);
  public static final ItemTag.Boolean GRENADE_DESTROY_TAG = new ItemTag.Boolean("atlas.grenade-destroy",
      false);

  @Getter
  private final Match match;
  private final List<Listener> listeners;

  public ItemsModule(Match match, Optional<Check> removeDrops, Optional<Check> keepItems,
      Optional<Check> keepArmor, Optional<Check> repairTools) {
    this.match = match;

    this.listeners = new ArrayList<>();
    if (removeDrops.isPresent()) {
      this.listeners.add(new RemoveDropsListener(match, removeDrops.get()));
    }

    if (keepItems.isPresent() || keepArmor.isPresent()) {
      this.listeners.add(new KeepListener(match, keepItems, keepArmor));
    }

    if (repairTools.isPresent()) {
      this.listeners.add(new RepairToolsListener(match, repairTools.get()));
    }
  }

  @Override
  public void open() {
    Events.register(this.listeners);
  }

  @Override
  public void close() {
    Events.unregister(this.listeners);
  }

  public static void applyGrenadeFormat(ItemStack itemStack, double grenadePower,
                                        boolean grenadeFire, boolean grenadeDestroy) {
    GRENADE_TAG.set(itemStack, true);
    GRENADE_POWER_TAG.set(itemStack, grenadePower);
    GRENADE_FIRE_TAG.set(itemStack, grenadeFire);
    GRENADE_DESTROY_TAG.set(itemStack, grenadeDestroy);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onUseGrenade(ProjectileLaunchEvent event) {
    Projectile projectile = event.getEntity();

    if (projectile.getShooter() instanceof Player player) {
      ItemStack handItem = player.getInventory().getItemInHand();

      if (handItem != null && GRENADE_TAG.get(handItem)) {
        projectile.setMetadata(POWER_METADATA_TAG, new FixedMetadataValue(Atlas.get(), GRENADE_POWER_TAG.get(handItem)));
        projectile.setMetadata(DESTROY_METADATA_TAG, new FixedMetadataValue(Atlas.get(), GRENADE_DESTROY_TAG.get(handItem)));
        projectile.setMetadata(FIRE_METADATA_TAG, new FixedMetadataValue(Atlas.get(), GRENADE_FIRE_TAG.get(handItem)));
      }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onGrenadeHit(ProjectileHitEvent event) {
    Projectile projectile = event.getEntity();
    if (projectile.hasMetadata(POWER_METADATA_TAG)) {
      Location projectileHitLocation = projectile.getLocation();

      match.getWorld().createExplosion((Player) projectile.getShooter(),
          projectileHitLocation,
          projectile.getMetadata(POWER_METADATA_TAG).get(0).asFloat(),
          projectile.getMetadata(FIRE_METADATA_TAG).get(0).asBoolean(),
          projectile.getMetadata(DESTROY_METADATA_TAG).get(0).asBoolean());
    }
  }
}
