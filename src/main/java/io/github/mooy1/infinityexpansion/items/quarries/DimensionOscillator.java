package io.github.mooy1.infinityexpansion.items.quarries;

import io.github.mooy1.infinityexpansion.categories.Groups;
import io.github.mooy1.infinityexpansion.items.materials.Materials;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class DimensionOscillator extends Oscillator {
    protected final QuarryPool pool;

    public DimensionOscillator(World.Environment dimension, QuarryPool pool, Material itemType, double chance) {
        super(Groups.MAIN_MATERIALS, create(dimension, itemType), RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                Materials.MACHINE_PLATE, SlimefunItems.BLISTERING_INGOT_3, Materials.MACHINE_PLATE,
                SlimefunItems.BLISTERING_INGOT_3, new ItemStack(itemType), SlimefunItems.BLISTERING_INGOT_3,
                Materials.MACHINE_PLATE, SlimefunItems.BLISTERING_INGOT_3, Materials.MACHINE_PLATE
        }, chance);
        this.pool = pool;
    }

    private static SlimefunItemStack create(World.Environment dimension, Material display) {
        final String name = ChatUtils.humanize(dimension.name()).replace("Normal", "Overworld");
        return new SlimefunItemStack(
                "QUARRY_OSCILLATOR_" + name.toUpperCase(Locale.ROOT),
                display,
                "&d[" + name + "] Dimension Oscillator",
                "&7Adds a chance of mining from the targeted dimension!"
        );
    }

    @Nonnull
    @Override
    public List<ItemStack> getDisplayRecipes() {
        final List<ItemStack> recipes = new ArrayList<>();
        for (Quarry quarry : Quarry.getQuarries()) {
            final double baseChance = ((1D / quarry.chance()) * this.chance);
            final int speed = quarry.speed();
            recipes.add(quarry.getItem());
            for (Map.Entry<Material, Float> drop : this.pool.drops().toMap().entrySet()) {
                if (recipes.size() > 1) {
                    recipes.add(new ItemStack(Material.AIR));
                }

                recipes.add(new CustomItemStack(new ItemStack(drop.getKey(), speed), meta -> {
                    final List<String> lore = new ArrayList<>();
                    lore.add(ChatColors.color("&7Chance: &b" + FORMAT.format(baseChance * drop.getValue() * 100)));
                    meta.setLore(lore);
                }));
            }
        }
        return recipes;
    }

    @Override
    public Material output(QuarryPool pool, ThreadLocalRandom random) {
        return this.pool.drops().getRandom(random);
    }
}
