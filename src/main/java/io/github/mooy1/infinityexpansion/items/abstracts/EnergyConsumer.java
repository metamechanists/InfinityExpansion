package io.github.mooy1.infinityexpansion.items.abstracts;

import org.bukkit.block.Block;

public interface EnergyConsumer {
    int getEnergyConsumption();
    default int getEnergyConsumption(Block block) {
        return getEnergyConsumption();
    }
}
