/**
 * @author Kanawanagasaki
 */

package me.juancarloscp52.entropy.events.db;

import me.juancarloscp52.entropy.Entropy;
import me.juancarloscp52.entropy.events.AbstractTimedEvent;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.ArrayList;

public class MidasTouchEvent extends AbstractTimedEvent {

    private static ArrayList<Item> _goldenItems = new ArrayList<Item>() {
        {
            add(Items.ENCHANTED_GOLDEN_APPLE);
            add(Items.GOLDEN_APPLE);
            add(Items.GOLDEN_AXE);
            add(Items.GOLDEN_BOOTS);
            add(Items.GOLDEN_CHESTPLATE);
            add(Items.GOLDEN_HELMET);
            add(Items.GOLDEN_HOE);
            add(Items.GOLDEN_HORSE_ARMOR);
            add(Items.GOLDEN_LEGGINGS);
            add(Items.GOLDEN_PICKAXE);
            add(Items.GOLDEN_SHOVEL);
            add(Items.GOLDEN_SWORD);
            add(Items.GOLD_BLOCK);
            add(Items.GOLD_INGOT);
            add(Items.GOLD_NUGGET);
            add(Items.GOLD_ORE);
            add(Items.NETHER_GOLD_ORE);
            add(Items.RAW_GOLD);
            add(Items.RAW_GOLD_BLOCK);
            add(Items.GOLDEN_CARROT);
        }
    };

    @Override
    public void tick() {
        for (var player : Entropy.getInstance().eventHandler.getActivePlayers()) {
            var minX = (int) (player.getX() - (player.getX() < 0 ? 1.5 : .5));
            var minY = (int) player.getY() - 1;
            var minZ = (int) (player.getZ() - (player.getZ() < 0 ? 1.5 : .5));
            var maxX = minX + 1;
            var maxY = minY + 3;
            var maxZ = minZ + 1;

            var world = player.getWorld();

            // Replace blocks around with golden blocks
            for (int ix = minX; ix <= maxX; ix++) {
                for (int iy = minY; iy <= maxY; iy++) {
                    for (int iz = minZ; iz <= maxZ; iz++) {

                        var blockPos = new BlockPos(ix, iy, iz);
                        var block = world.getBlockState(blockPos).getBlock();
                        if (block == Blocks.AIR ||
                                block == Blocks.GOLD_BLOCK ||
                                block == Blocks.GOLD_ORE ||
                                block == Blocks.RAW_GOLD_BLOCK ||
                                block == Blocks.BEDROCK ||
                                block == Blocks.END_PORTAL_FRAME ||
                                block == Blocks.END_PORTAL ||
                                block == Blocks.NETHER_GOLD_ORE)
                            continue;

                        var odds = player.getRandom().nextInt(100);

                        if (odds < 96)
                            world.setBlockState(blockPos,
                                    world.getRegistryKey() == World.NETHER
                                            ? Blocks.NETHER_GOLD_ORE.getDefaultState()
                                            : Blocks.GOLD_ORE.getDefaultState());
                        else if (odds < 98)
                            world.setBlockState(blockPos, Blocks.RAW_GOLD_BLOCK.getDefaultState());
                        else
                            world.setBlockState(blockPos, Blocks.GOLD_BLOCK.getDefaultState());
                    }
                }
            }

            // Kill mobs around and spawn golden items
            var box = new Box(minX, minY, minZ, maxX, maxY, maxZ);
            var mobs = world.getOtherEntities(player, box, x -> x instanceof LivingEntity && x.isAlive());
            for (var mob : mobs) {

                ItemStack itemStack;

                switch (player.getRandom().nextInt(16)) {
                    case 0:
                        itemStack = new ItemStack(Items.GOLD_INGOT);
                        break;
                    case 1:
                        itemStack = new ItemStack(Items.GOLD_BLOCK);
                        break;
                    case 2:
                        itemStack = new ItemStack(Items.RAW_GOLD_BLOCK);
                        break;
                    case 3:
                        itemStack = new ItemStack(Items.GOLD_ORE);
                        break;
                    case 4:
                        itemStack = new ItemStack(Items.NETHER_GOLD_ORE);
                        break;
                    default:
                        itemStack = new ItemStack(Items.GOLD_NUGGET, player.getRandom().nextInt(6) + 2);
                        break;
                }

                var entityItem = new ItemEntity(world, mob.getX(), mob.getY(), mob.getZ(), itemStack);
                world.spawnEntity(entityItem);
                mob.kill();
            }

            // Replace items that player holding in his hand and everything that player
            // wearing with golden variants
            var inventory = player.getInventory();
            var inventoryItems = new ArrayList<Pair<DefaultedList<ItemStack>, Integer>>() {
                {
                    add(new Pair<DefaultedList<ItemStack>, Integer>(inventory.offHand, 0));
                    add(new Pair<DefaultedList<ItemStack>, Integer>(inventory.armor, 0));
                    add(new Pair<DefaultedList<ItemStack>, Integer>(inventory.armor, 1));
                    add(new Pair<DefaultedList<ItemStack>, Integer>(inventory.armor, 2));
                    add(new Pair<DefaultedList<ItemStack>, Integer>(inventory.armor, 3));
                    add(new Pair<DefaultedList<ItemStack>, Integer>(inventory.main, inventory.selectedSlot));
                }
            };
            for (var pair : inventoryItems) {
                var itemStack = pair.getLeft().get(pair.getRight());
                var item = itemStack.getItem();
                if (item == Items.AIR)
                    continue;
                if (_goldenItems.contains(item))
                    continue;

                ItemStack newItemStack;
                if (item.isFood()) {
                    var odds = player.getRandom().nextInt(100);

                    if (odds < 75)
                        newItemStack = new ItemStack(Items.GOLDEN_CARROT, itemStack.getCount());
                    else if (odds < 95)
                        newItemStack = new ItemStack(Items.GOLDEN_APPLE, itemStack.getCount());
                    else
                        newItemStack = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, itemStack.getCount());
                } else if (item instanceof BlockItem) {
                    switch (player.getRandom().nextInt(6)) { // 50% for gold ore and 16.(6)% for something else
                        case 0:
                            newItemStack = new ItemStack(Items.GOLD_BLOCK, Math.min(itemStack.getCount(),3));
                            break;
                        case 1:
                            newItemStack = new ItemStack(Items.RAW_GOLD_BLOCK, Math.min(itemStack.getCount(),3));
                            break;
                        case 2:
                            newItemStack = new ItemStack(Items.NETHER_GOLD_ORE, Math.min(itemStack.getCount(),32));
                            break;
                        default:
                            newItemStack = new ItemStack(Items.GOLD_ORE, Math.min(itemStack.getCount(),10));
                            break;
                    }
                } else if (item instanceof ArmorItem) {
                    if (((ArmorItem) item).getSlotType() == EquipmentSlot.HEAD)
                        newItemStack = new ItemStack(Items.GOLDEN_HELMET, itemStack.getCount());
                    else if (((ArmorItem) item).getSlotType() == EquipmentSlot.CHEST)
                        newItemStack = new ItemStack(Items.GOLDEN_CHESTPLATE, itemStack.getCount());
                    else if (((ArmorItem) item).getSlotType() == EquipmentSlot.LEGS)
                        newItemStack = new ItemStack(Items.GOLDEN_LEGGINGS, itemStack.getCount());
                    else if (((ArmorItem) item).getSlotType() == EquipmentSlot.FEET)
                        newItemStack = new ItemStack(Items.GOLDEN_BOOTS, itemStack.getCount());
                    else
                        newItemStack = new ItemStack(Items.GOLD_INGOT);
                } else if (item instanceof PickaxeItem) {
                    newItemStack = new ItemStack(Items.GOLDEN_PICKAXE, itemStack.getCount());
                } else if (item instanceof AxeItem) {
                    newItemStack = new ItemStack(Items.GOLDEN_AXE, itemStack.getCount());
                } else if (item instanceof ShovelItem) {
                    newItemStack = new ItemStack(Items.GOLDEN_SHOVEL, itemStack.getCount());
                } else if (item instanceof HoeItem) {
                    newItemStack = new ItemStack(Items.GOLDEN_HOE, itemStack.getCount());
                } else if (item instanceof SwordItem) {
                    newItemStack = new ItemStack(Items.GOLDEN_SWORD, itemStack.getCount());
                } else if (item instanceof HorseArmorItem) {
                    newItemStack = new ItemStack(Items.GOLDEN_HORSE_ARMOR, itemStack.getCount());
                } else {
                    switch (player.getRandom().nextInt(3)) {
                        case 0:
                            newItemStack = new ItemStack(Items.GOLD_NUGGET, itemStack.getCount());
                            break;
                        case 1:
                            newItemStack = new ItemStack(Items.RAW_GOLD, itemStack.getCount());
                            break;
                        default:
                            newItemStack = new ItemStack(Items.GOLD_INGOT, itemStack.getCount());
                            break;
                    }
                }
                pair.getLeft().set(pair.getRight(), newItemStack);
            }
        }

        super.tick();

    }

    @Override
    public void render(MatrixStack matrixStack, float tickdelta) {
    }

    @Override
    public short getDuration() {
        return (short) (Entropy.getInstance().settings.baseEventDuration * .2);
    }

}
