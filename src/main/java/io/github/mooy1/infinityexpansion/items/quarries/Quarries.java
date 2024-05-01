package io.github.mooy1.infinityexpansion.items.quarries;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import lombok.experimental.UtilityClass;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import io.github.mooy1.infinityexpansion.InfinityExpansion;
import io.github.mooy1.infinityexpansion.categories.Groups;
import io.github.mooy1.infinityexpansion.items.SlimefunExtension;
import io.github.mooy1.infinityexpansion.items.blocks.InfinityWorkbench;
import io.github.mooy1.infinityexpansion.items.gear.Gear;
import io.github.mooy1.infinityexpansion.items.materials.Materials;
import io.github.mooy1.infinitylib.machines.MachineLore;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;

@UtilityClass
public final class Quarries {

    public static final SlimefunItemStack BASIC_QUARRY = new SlimefunItemStack(
            "BASIC_QUARRY",
            Material.CHISELED_SANDSTONE,
            "&9Basic Quarry",
            "&7Automatically mines overworld ores",
            "",
            MachineLore.speed(1),
            MachineLore.energyPerSecond(300)
    );
    public static final SlimefunItemStack ADVANCED_QUARRY = new SlimefunItemStack(
            "ADVANCED_QUARRY",
            Material.CHISELED_RED_SANDSTONE,
            "&cAdvanced Quarry",
            "&7Automatically mines overworld and nether ores",
            "",
            MachineLore.speed(2),
            MachineLore.energyPerSecond(900)
    );
    public static final SlimefunItemStack VOID_QUARRY = new SlimefunItemStack(
            "VOID_QUARRY",
            Material.CHISELED_NETHER_BRICKS,
            "&8Void Quarry",
            "&7Automatically mines overworld and nether ores",
            "",
            MachineLore.speed(6),
            MachineLore.energyPerSecond(3600)
    );
    public static final SlimefunItemStack INFINITY_QUARRY = new SlimefunItemStack(
            "INFINITY_QUARRY",
            Material.CHISELED_POLISHED_BLACKSTONE,
            "&bInfinity Quarry",
            "&7Automatically mines overworld and nether ores",
            "",
            MachineLore.speed(64),
            MachineLore.energyPerSecond(36000)
    );

    public static void setup(InfinityExpansion addon) {
        ConfigurationSection config = addon.getConfig().getConfigurationSection("quarry-options");
        Objects.requireNonNull(config);

        Map<World.Environment, QuarryPool> pools = new LinkedHashMap<>();
        ConfigurationSection poolsConfig = config.getConfigurationSection("pools");
        if (poolsConfig != null) {
            for (String poolType : poolsConfig.getKeys(false)) {
                try {
                    ConfigurationSection pool = poolsConfig.getConfigurationSection(poolType);
                    World.Environment dimension = World.Environment.valueOf(poolType.toUpperCase(Locale.ROOT).replace("OVERWORLD", "NORMAL"));
                    if (pool != null) {
                        pools.put(dimension, QuarryPool.load(pool));
                    } else {
                        addon.getLogger().warning("Missing pool section for " + poolType);
                    }
                } catch (Exception e) {
                    addon.getLogger().warning("Invalid Quarry Pool: " + poolType + ", skipping");
                    addon.getLogger().warning(e::getLocalizedMessage);
                }
            }
        }

        ConfigurationSection oscillators = config.getConfigurationSection("oscillators");
        if (oscillators != null) {
            for (String oscillator : oscillators.getKeys(false)) {
                try {
                    Material resource = Material.valueOf(oscillator.toUpperCase(Locale.ROOT));
                    double chance = oscillators.getDouble(oscillator);
                    if (chance > 0) {
                        new Oscillator(resource, chance).register(addon);
                    } else {
                        addon.getLogger().info("Oscillator " + oscillator + "has 0 chance, skipping");
                    }
                } catch (Exception e) {
                    addon.getLogger().warning("Invalid Oscillator: " + oscillator + ", skipping");
                    addon.getLogger().warning(e::getLocalizedMessage);
                }
            }
        }

        ConfigurationSection dimensionOscillators = config.getConfigurationSection("dimension_oscillators");
        if (dimensionOscillators != null) {
            for (String oscillatorType : dimensionOscillators.getKeys(false)) {
                ConfigurationSection oscillator = dimensionOscillators.getConfigurationSection(oscillatorType);
                if (oscillator != null) {
                    try {
                        World.Environment dimension = World.Environment.valueOf(oscillatorType.toUpperCase(Locale.ROOT).replace("OVERWORLD", "NORMAL"));
                        Material itemType = Material.valueOf(oscillator.getString("item_type").toUpperCase(Locale.ROOT));
                        double chance = oscillator.getDouble("chance");
                        if (chance > 0) {
                            new DimensionOscillator(dimension, itemType, chance).register(addon);
                        } else {
                            addon.getLogger().info("Oscillator " + oscillatorType + "has 0 chance, skipping");
                        }
                    } catch (Exception ignored) {
                        addon.getLogger().warning("Invalid Dimensional Oscillator: " + oscillatorType + ", skipping");
                    }
                } else {
                    addon.getLogger().warning("Missing oscillator section for " + oscillatorType);
                }
            }
        }

        new Quarry(Groups.ADVANCED_MACHINES, BASIC_QUARRY, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                Materials.MAGSTEEL_PLATE, SlimefunItems.CARBONADO_EDGED_CAPACITOR, Materials.MAGSTEEL_PLATE,
                new ItemStack(Material.IRON_PICKAXE), SlimefunItems.GEO_MINER, new ItemStack(Material.IRON_PICKAXE),
                Materials.MACHINE_CIRCUIT, Materials.MACHINE_CORE, Materials.MACHINE_CIRCUIT
        }, 1, 6, pools).energyPerTick(300).register(addon);

        new Quarry(Groups.ADVANCED_MACHINES, ADVANCED_QUARRY, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                Materials.MACHINE_PLATE, SlimefunItems.ENERGIZED_CAPACITOR, Materials.MACHINE_PLATE,
                new ItemStack(Material.DIAMOND_PICKAXE), BASIC_QUARRY, new ItemStack(Material.DIAMOND_PICKAXE),
                Materials.MACHINE_CIRCUIT, Materials.MACHINE_CORE, Materials.MACHINE_CIRCUIT
        }, 2, 4, pools).energyPerTick(900).register(addon);

        new Quarry(Groups.ADVANCED_MACHINES, VOID_QUARRY, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                Materials.VOID_INGOT, SlimefunExtension.VOID_CAPACITOR, Materials.VOID_INGOT,
                new ItemStack(Material.NETHERITE_PICKAXE), ADVANCED_QUARRY, new ItemStack(Material.NETHERITE_PICKAXE),
                Materials.MACHINE_CIRCUIT, Materials.MACHINE_CORE, Materials.MACHINE_CIRCUIT
        }, 6, 2, pools).energyPerTick(3600).register(addon);

        new Quarry(Groups.INFINITY_CHEAT, INFINITY_QUARRY, InfinityWorkbench.TYPE, new ItemStack[] {
                null, Materials.MACHINE_PLATE, Materials.MACHINE_PLATE, Materials.MACHINE_PLATE, Materials.MACHINE_PLATE, null,
                Materials.MACHINE_PLATE, Gear.PICKAXE, Materials.INFINITE_CIRCUIT, Materials.INFINITE_CIRCUIT, Gear.PICKAXE, Materials.MACHINE_PLATE,
                Materials.MACHINE_PLATE, VOID_QUARRY, Materials.INFINITE_CORE, Materials.INFINITE_CORE, VOID_QUARRY, Materials.MACHINE_PLATE,
                Materials.VOID_INGOT, null, Materials.INFINITE_INGOT, Materials.INFINITE_INGOT, null, Materials.VOID_INGOT,
                Materials.VOID_INGOT, null, Materials.INFINITE_INGOT, Materials.INFINITE_INGOT, null, Materials.VOID_INGOT,
                Materials.VOID_INGOT, null, Materials.INFINITE_INGOT, Materials.INFINITE_INGOT, null, Materials.VOID_INGOT
        }, 64, 1, pools).energyPerTick(36000).register(addon);
    }

}
