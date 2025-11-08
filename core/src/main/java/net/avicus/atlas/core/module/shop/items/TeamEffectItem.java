package net.avicus.atlas.core.module.shop.items;

import net.avicus.atlas.core.match.Match;
import net.avicus.atlas.core.module.checks.Check;
import net.avicus.atlas.core.module.groups.GroupsModule;
import net.avicus.atlas.core.module.locales.LocalizedXmlString;
import net.avicus.atlas.core.module.shop.ShopItem;
import net.avicus.atlas.core.util.ScopableItemStack;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public class TeamEffectItem extends ShopItem {
    private final PotionEffect effect;
    private final Match match;

    public TeamEffectItem(int price,
                         LocalizedXmlString name,
                         List<LocalizedXmlString> description,
                         Check purchaseCheck, ScopableItemStack stack, Match match, PotionEffect effect) {
        super(price, name, description, stack, purchaseCheck);
        this.effect = effect;
        this.match = match;
    }

    @Override
    public void give(Player player) {
        this.match.getRequiredModule(GroupsModule.class)
                .getCompetitorOf(player)
                .ifPresent(competitor -> competitor
                        .getPlayers()
                        .forEach(p -> p.addPotionEffect(this.effect)));
    }
}
