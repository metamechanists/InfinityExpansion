package io.github.mooy1.infinityexpansion.items.quarries;

import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.RandomizedSet;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;

public record QuarryPool(Material commonDrop, RandomizedSet<Material> drops) {
    public static QuarryPool load(ConfigurationSection section) {
        if (section == null) {
            return new QuarryPool(Material.AIR, new RandomizedSet<>());
        }

        Material commonDrop = null;
        if (section.contains("common_drop")) {
            commonDrop = Material.getMaterial(section.getString("common_drop").toUpperCase(Locale.ROOT));
        }

        RandomizedSet<Material> drops = new RandomizedSet<>();
        ConfigurationSection dropsSection = section.getConfigurationSection("drops");
        if (dropsSection != null) {
            for (String dropType : dropsSection.getKeys(false)) {
                dropType = dropType.toUpperCase(Locale.ROOT);
                Material drop = Material.getMaterial(dropType);
                double weight = dropsSection.getDouble(dropType);
                if (drop != null && weight > 0) {
                    drops.add(drop, (float) weight);
                }
            }
        }

        if (commonDrop == null) {
            commonDrop = Material.COBBLESTONE;
        }
        return new QuarryPool(commonDrop, drops);
    }
}
