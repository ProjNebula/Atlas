package net.avicus.atlas.core.module.groups;

import net.avicus.atlas.core.match.Match;
import net.avicus.atlas.core.util.AtlasTask;
import net.avicus.atlas.core.util.Messages;
import net.avicus.compendium.TextStyle;
import net.avicus.compendium.settings.PlayerSettings;
import net.avicus.compendium.settings.Setting;
import net.avicus.compendium.settings.types.SettingTypes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ObserverTask extends AtlasTask {

  public static final Setting<Boolean> SPECTATOR_VIEW_SETTING = new Setting<>(
      "spectator-view",
      SettingTypes.BOOLEAN,
      true,
      Messages.SETTINGS_SPECTATOR_VIEW,
      Messages.SETTINGS_SPECTATOR_VIEW_SUMMARY
  );

  static {
    PlayerSettings.register(SPECTATOR_VIEW_SETTING);
  }

  private final Match match;
  private final GroupsModule module;

  public ObserverTask(Match match, GroupsModule module) {
    super();
    this.match = match;
    this.module = module;
  }

  @Override
  public void run() {
    execute();
  }

  public ObserverTask start() {
    this.repeat(0, 20);
    return this;
  }

  public void execute() {
    var messageToSend = this.match.getRequiredModule(GroupsModule.class)
            .getGroups()
            .stream()
            .filter(g -> !g.isSpectator())
            .allMatch(g -> g.isFull(false))
        ? Messages.UI_SPECTATOR_ACTION_BAR_MATCH_FULL.with(ChatColor.RED)
        : Messages.UI_SPECTATOR_ACTION_BAR_SPECTATING.with(ChatColor.AQUA,
            Messages.UI_SPECTATOR_ACTION_BAR_SLASH_JOIN.with(TextStyle.ofColor(ChatColor.GREEN).bold()));

    for (Player player : Bukkit.getOnlinePlayers()) {
      boolean observing = this.module.isObserving(player);

      if (observing) {
        player.setFireTicks(0);
        player.setRemainingAir(20);

        player.sendActionBar(messageToSend.render(player));
      }

      for (Player target : Bukkit.getOnlinePlayers()) {
        if (player.equals(target)) {
          continue;
        }

        if (shouldSee(player, target)) {
          player.showPlayer(target);
        } else {
          player.hidePlayer(target);
        }
      }
    }
  }

  private boolean shouldSee(Player player, Player target) {
    boolean playerObserver = this.module.isObserving(player);
    boolean targetObserver = this.module.isObserving(target);
    boolean targetDead = this.module.isObservingOrDead(target) && !targetObserver;

    // No one can see dead players
    if (targetDead) {
      return false;
    }

    // Allow player to see others if they are an observer
    if (playerObserver) {
      return !targetObserver || PlayerSettings.get(player, SPECTATOR_VIEW_SETTING);
    }

    // Otherwise the target must simply be participating in the match
    return !targetObserver;
  }
}
