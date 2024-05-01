package io.github.mooy1.infinityexpansion.items.quarries;

import io.github.mooy1.infinityexpansion.InfinityExpansion;
import io.github.mooy1.infinityexpansion.items.abstracts.EnergyConsumer;
import io.github.mooy1.infinitylib.machines.AbstractMachineBlock;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import lombok.Getter;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mines stuff
 *
 * @author Mooy1
 */
@ParametersAreNonnullByDefault
public final class Quarry extends AbstractMachineBlock implements RecipeDisplayItem, EnergyConsumer {
    private static final List<Quarry> QUARRIES = new ArrayList<>();
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##%");
    private static final int INTERVAL = InfinityExpansion.config().getInt("quarry-options.ticks-per-output", 1, 100);
    private static final ItemStack MINING = new CustomItemStack(Material.LIME_STAINED_GLASS_PANE, "&aMining...");
    private static final ItemStack OSCILLATOR_INFO = new CustomItemStack(
            Material.CYAN_STAINED_GLASS_PANE,
            "&bOscillator Slot",
            "&7Place a quarry oscillator to",
            "&7boost certain material's rates!"
    );
    private static final int[] OUTPUT_SLOTS = {
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
    };
    private static final int OSCILLATOR_SLOT = 49;
    private static final int STATUS_SLOT = 4;

    @Getter
    private final int speed;
    @Getter
    private final int chance;
    private final Map<World.Environment, QuarryPool> pools;

    public Quarry(ItemGroup category, SlimefunItemStack item, RecipeType type, ItemStack[] recipe,
                  int speed, int chance, Map<World.Environment, QuarryPool> pools) {
        super(category, item, type, recipe);

        this.speed = speed;
        this.chance = chance;
        this.pools = pools;
        QUARRIES.add(this);
    }

    @Override
    protected void setup(@Nonnull BlockMenuPreset blockMenuPreset) {
        blockMenuPreset.drawBackground(new int[] {
                0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 51, 52, 53
        });
        blockMenuPreset.addItem(48, OSCILLATOR_INFO, ChestMenuUtils.getEmptyClickHandler());
        blockMenuPreset.addItem(50, OSCILLATOR_INFO, ChestMenuUtils.getEmptyClickHandler());
    }

    @Override
    public void onNewInstance(@Nonnull BlockMenu menu, @Nonnull Block b) {}

    @Override
    protected int getStatusSlot() {
        return STATUS_SLOT;
    }

    @Override
    protected int[] getInputSlots() {
        return new int[0];
    }

    @Override
    protected int[] getOutputSlots() {
        return OUTPUT_SLOTS;
    }

    @Override
    protected boolean process(Block b, BlockMenu inv) {
        if (inv.hasViewer()) {
            inv.replaceExistingItem(STATUS_SLOT, MINING);
        }

        if (InfinityExpansion.slimefunTickCount() % INTERVAL != 0) {
            return true;
        }

        QuarryPool pool = this.pools.get(b.getWorld().getEnvironment());
        ItemStack outputItem = new ItemStack(pool.commonDrop(), this.speed);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (ThreadLocalRandom.current().nextInt(this.chance) == 0) {
            final SlimefunItem sfItem = SlimefunItem.getByItem(inv.getItemInSlot(OSCILLATOR_SLOT));
            if (sfItem instanceof Oscillator oscillator && random.nextDouble() >= oscillator.chance) {
                outputItem = new ItemStack(oscillator.output(this, pool, random), this.speed);
            } else {
                outputItem = new ItemStack(pool.drops().getRandom(random), this.speed);
            }
        }

        inv.pushItem(outputItem, OUTPUT_SLOTS);
        return true;
    }

    @Override
    public int getEnergyConsumption() {
        return this.energyPerTick;
    }

    public Map<World.Environment, QuarryPool> getPools() {
        return Collections.unmodifiableMap(this.pools);
    }

    @Override
    public @Nonnull String getRecipeSectionLabel(@Nonnull Player ignored) {
        return "&7Mines:";
    }

    @Override
    public @Nonnull List<ItemStack> getDisplayRecipes() {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (World.Environment dimension : this.pools.keySet()) {
            QuarryPool pool = this.pools.get(dimension);
            int baseChance = pool.chanceOverride(this.chance);
            addWithChance(itemStacks, new ItemStack(pool.commonDrop(), this.speed), dimension, (baseChance - 1F) / baseChance);
            var entries = new ArrayList<>(pool.drops().toMap().entrySet());
            entries.sort(Comparator.comparingDouble(Map.Entry::getValue));
            Collections.reverse(entries);
            for (Map.Entry<Material, Float> drop : entries) {
                addWithChance(itemStacks, new ItemStack(drop.getKey(), this.speed), dimension, (1F / baseChance) * drop.getValue());
            }

            // Add divider between dimensions
            if (itemStacks.size() % 2 != 0) {
                itemStacks.add(null);
            }

            itemStacks.add(null);
            itemStacks.add(null);
        }

        return itemStacks;
    }

    @ParametersAreNonnullByDefault
    public void addWithChance(List<ItemStack> itemStacks, ItemStack itemStack, @Nullable World.Environment dimension, double chance) {
        itemStacks.add(new CustomItemStack(itemStack, meta -> {
            final List<String> lore = new ArrayList<>();

            if (dimension != null) {
                final String name = ChatUtils.humanize(dimension.name()).replace("Normal", "Overworld");
                lore.add(ChatColors.color("&7Mined In: &b" + name));
            }

            lore.add(ChatColors.color("&7Chance: &b" + FORMAT.format(chance)));
            meta.setLore(lore);
        }));
    }

    public static List<Quarry> getQuarries() {
        return Collections.unmodifiableList(QUARRIES);
    }
}
