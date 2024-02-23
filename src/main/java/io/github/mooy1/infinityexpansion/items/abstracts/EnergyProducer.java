package io.github.mooy1.infinityexpansion.items.abstracts;

import org.bukkit.block.Block;

public interface EnergyProducer {
    int getEnergyGenerated();
    default int getEnergyGenerated(Block block) {
        return getEnergyGenerated();
    }
}
