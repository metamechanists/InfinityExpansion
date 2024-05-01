package io.github.mooy1.infinityexpansion.items.quarries;

import io.github.mooy1.infinityexpansion.InfinityExpansion;
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.RandomizedSet;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

public record QuarryPool(Material commonDrop, Integer chanceOverride, RandomizedSet<Material> drops) {
    public static QuarryPool load(ConfigurationSection section) {
        if (section == null) {
            return new QuarryPool(Material.AIR, null, new RandomizedSet<>());
        }

        Material commonDrop = null;
        if (section.contains("common_drop")) {
            commonDrop = Material.getMaterial(section.getString("common_drop").toUpperCase(Locale.ROOT));
        }

        Integer chanceOverride = null;
        if (section.contains("common_chance_override")) {
            chanceOverride = (int) (1 / ((-1 * section.getDouble("common_chance_override")) + 1));
        }

        RandomizedSet<Material> drops = new RandomizedSet<>();
        ConfigurationSection dropsSection = section.getConfigurationSection("drops");
        if (dropsSection != null) {
            for (String dropType : dropsSection.getKeys(false)) {
                try {
                    Material drop = Material.valueOf(dropType.toUpperCase(Locale.ROOT));
                    double weight = dropsSection.getDouble(dropType);
                    if (weight > 0) {
                        drops.add(drop, (float) weight);
                    } else {
                        InfinityExpansion.getInstance().getLogger().info("Quarry Drop " + dropType + " has 0 chance, skipping");
                    }
                } catch (Exception e) {
                    InfinityExpansion.getInstance().getLogger().warning("Invalid Quarry Drop: " + dropType + ", skipping");
                    InfinityExpansion.getInstance().getLogger().warning(e::getLocalizedMessage);
                }
            }
        }

        if (commonDrop == null) {
            commonDrop = Material.COBBLESTONE;
        }
        return new QuarryPool(commonDrop, chanceOverride, drops);
    }

    public int chanceOverride(int defaultChance) {
        return this.chanceOverride != null ? this.chanceOverride : defaultChance;
    }
}
