package io.github.mooy1.infinityexpansion.items.quarries;

import io.github.mooy1.infinityexpansion.categories.Groups;
import io.github.mooy1.infinityexpansion.items.materials.Materials;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerHead;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerSkin;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class DimensionOscillator extends Oscillator {
    protected static final ItemStack ARROW = PlayerHead.getItemStack(PlayerSkin.fromHashCode("682ad1b9cb4dd21259c0d75aa315ff389c3cef752be3949338164bac84a96e"));
    protected final World.Environment dimension;

    public DimensionOscillator(World.Environment dimension, Material itemType, double chance) {
        super(Groups.MAIN_MATERIALS, create(dimension, itemType), RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                Materials.MACHINE_PLATE, Materials.VOID_INGOT, Materials.MACHINE_PLATE,
                Materials.VOID_INGOT, new ItemStack(itemType), Materials.VOID_INGOT,
                Materials.MACHINE_PLATE, Materials.VOID_INGOT, Materials.MACHINE_PLATE
        }, chance);
        this.dimension = dimension;
    }

    private static SlimefunItemStack create(World.Environment dimension, Material display) {
        final String name = ChatUtils.humanize(dimension.name()).replace("Normal", "Overworld");
        return new SlimefunItemStack(
                "QUARRY_OSCILLATOR_" + name.toUpperCase(Locale.ROOT).replace(' ', '_'),
                display,
                "&b[" + name + "] Oscillator",
                "&7Adds a chance of mining from the targeted dimension!"
        );
    }

    @Nonnull
    @Override
    public List<ItemStack> getDisplayRecipes() {
        final List<ItemStack> itemStacks = new ArrayList<>();
        for (Quarry quarry : Quarry.getQuarries()) {
            final double baseChance = ((1D / quarry.chance()) * this.chance);
            itemStacks.add(quarry.getItem());
            itemStacks.add(new CustomItemStack(ARROW, " "));

            QuarryPool pool = quarry.getPools().get(this.dimension);
            ArrayList<Map.Entry<Material, Float>> entries = new ArrayList<>(pool.drops().toMap().entrySet());
            entries.sort(Comparator.comparingDouble(Map.Entry::getValue));
            Collections.reverse(entries);

            quarry.addWithChance(itemStacks, new ItemStack(pool.commonDrop(), quarry.speed()),
                    null, baseChance * (1D/10D));
            for (Map.Entry<Material, Float> drop : entries) {
                quarry.addWithChance(itemStacks, new ItemStack(drop.getKey(), quarry.speed()),
                        null, baseChance * (9D/10D) * drop.getValue());
            }

            // Add divider between quarries
            if (itemStacks.size() % 2 != 0) {
                itemStacks.add(null);
            }

            itemStacks.add(null);
            itemStacks.add(null);
        }
        return itemStacks;
    }

    @Override
    public Material output(Quarry quarry, QuarryPool pool, ThreadLocalRandom random) {
        return random.nextInt(10) == 0
                ? quarry.getPools().get(this.dimension).commonDrop()
                : quarry.getPools().get(this.dimension).drops().getRandom(random);
    }
}
