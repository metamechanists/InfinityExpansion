package io.github.mooy1.infinityexpansion.items.quarries;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.github.mooy1.infinityexpansion.categories.Groups;
import io.github.mooy1.infinityexpansion.items.materials.Materials;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Oscillator extends SlimefunItem implements RecipeDisplayItem {
    private static final Set<Oscillator> OSCILLATORS = new HashSet<>();
    public final double chance;

    protected Oscillator(ItemGroup group, SlimefunItemStack itemStack, RecipeType recipeType, ItemStack[] recipe, double chance) {
        super(group, itemStack, recipeType, recipe);
        this.chance = chance;
        OSCILLATORS.add(this);
    }

    public Oscillator(Material resource, double chance) {
        this(Groups.MAIN_MATERIALS, create(resource), RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                Materials.MACHINE_PLATE, SlimefunItems.BLISTERING_INGOT_3, Materials.MACHINE_PLATE,
                SlimefunItems.BLISTERING_INGOT_3, new ItemStack(resource), SlimefunItems.BLISTERING_INGOT_3,
                Materials.MACHINE_PLATE, SlimefunItems.BLISTERING_INGOT_3, Materials.MACHINE_PLATE
        }, chance);
    }

    private static SlimefunItemStack create(Material resource) {
        return new SlimefunItemStack(
                "QUARRY_OSCILLATOR_" + resource.name(),
                resource,
                "&b" + ItemUtils.getItemName(new ItemStack(resource)) + " Oscillator",
                "&7Increases the odds of mining this resource!"
        );
    }

    @Override
    public @Nonnull List<ItemStack> getDisplayRecipes() {
        final List<ItemStack> itemStacks = new ArrayList<>();
        for (Quarry quarry : Quarry.getQuarries()) {
            itemStacks.add(quarry.getItem());
            quarry.addWithChance(itemStacks, new ItemStack(this.getItem().getType(), quarry.speed()),
                    null, (1D / quarry.chance()) * this.chance);
        }
        return itemStacks;
    }

    public Material output(Quarry quarry, QuarryPool pool, ThreadLocalRandom random) {
        final Material output = this.getItem().getType();
        return pool.commonDrop() != output && !pool.drops().contains(output)
                ? pool.drops().getRandom(random)
                : output;
    }

    public static Set<Oscillator> getOscillators() {
        return Collections.unmodifiableSet(OSCILLATORS);
    }
}
