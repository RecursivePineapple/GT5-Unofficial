package gregtech.common.items.matterManipulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;

import org.joml.Vector3i;

import com.mojang.authlib.GameProfile;

import gregtech.api.util.GTUtility;
import gregtech.api.util.GTUtility.FluidId;
import gregtech.api.util.GTUtility.ItemId;
import gregtech.common.items.matterManipulator.NBTState.Location;
import gregtech.common.items.matterManipulator.NBTState.PendingBlock;

public class BlockAnalyzer {

    private BlockAnalyzer() {}

    public static @Nullable TileAnalysisResult analyze(IBlockAnalysisContext context) {
        TileEntity te = context.getTileEntity();

        if (te == null) {
            return null;
        }

        TileAnalysisResult result = new TileAnalysisResult(context, te);

        return result.doesAnything() ? result : null;
    }

    public static RegionAnalysis analyzeRegion(World world, Location a, Location b, boolean checkTiles) {
        if (a == null || b == null || world.provider.dimensionId != a.worldId || a.worldId != b.worldId) return null;

        long pre = System.nanoTime();

        RegionAnalysis analysis = new RegionAnalysis();

        Vector3i deltas = MMUtils.getRegionDeltas(a, b);
        analysis.deltas = deltas;

        analysis.blocks = new ArrayList<>();

        BlockAnalysisContext context = new BlockAnalysisContext(world);

        for (Vector3i voxel : MMUtils.getBlocksInBB(a, deltas)) {
            PendingBlock pending = PendingBlock.fromBlock(world, voxel.x, voxel.y, voxel.z);

            if (pending == null) {
                continue;
            }

            if (checkTiles) {
                context.voxel = voxel;
                TileAnalysisResult tile = analyze(context);

                if (tile != null && tile.doesAnything()) {
                    pending.tileData = tile;
                }
            }

            pending.x -= a.x;
            pending.y -= a.y;
            pending.z -= a.z;

            analysis.blocks.add(pending);
        }

        long post = System.nanoTime();

        System.out.println("Analysis took " + (post - pre) / 1e6 + " ms");

        return analysis;
    }

    public static class RegionAnalysis {

        public Vector3i deltas;
        public List<PendingBlock> blocks;
    }

    public static interface IBlockAnalysisContext {

        public EntityPlayer getFakePlayer();

        public TileEntity getTileEntity();
    }

    public static class BlockAnalysisContext implements IBlockAnalysisContext {

        public World world;
        public EntityPlayer fakePlayer;
        public Vector3i voxel;

        public BlockAnalysisContext(World world) {
            this.world = world;
        }

        @Override
        public EntityPlayer getFakePlayer() {
            if (fakePlayer == null) {
                fakePlayer = new FakePlayer(
                    (WorldServer) world,
                    new GameProfile(UUID.randomUUID(), "BlockAnalyzer Fake Player"));
            }

            return fakePlayer;
        }

        @Override
        public TileEntity getTileEntity() {
            return world.getTileEntity(voxel.x, voxel.y, voxel.z);
        }
    }

    public static interface IBlockApplyContext extends IBlockAnalysisContext, IPseudoInventory {

        public EntityPlayer getRealPlayer();

        public boolean tryApplyAction(double complexity);

        public void warn(String message);

        public void error(String message);
    }

    public static class BlockApplyContext implements IBlockApplyContext {

        public World world;
        public int x, y, z;
        public EntityPlayer player;
        public PendingBuild build;
        public ItemStack manipulator;
        public FakePlayer fakePlayer;

        public static final double EU_PER_ACTION = 8192;

        @Override
        public EntityPlayer getFakePlayer() {
            if (fakePlayer == null) {
                fakePlayer = new FakePlayer((WorldServer) player.getEntityWorld(), player.getGameProfile());
            }

            return fakePlayer;
        }

        @Override
        public TileEntity getTileEntity() {
            return world.getTileEntity(x, y, z);
        }

        @Override
        public EntityPlayer getRealPlayer() {
            return player;
        }

        @Override
        public boolean tryApplyAction(double complexity) {
            return build.tryConsumePower(manipulator, x, y, z, EU_PER_ACTION * complexity);
        }

        @Override
        public boolean tryConsumeItems(ItemStack... items) {
            if (build == null) {
                for (ItemStack item : items) System.out.println("consume: " + item);
                return true;
            } else {
                return build.tryConsumeItems(items);
            }
        }

        @Override
        public void givePlayerItems(ItemStack... items) {
            if (build == null) {
                for (ItemStack item : items) System.out.println("give: " + item);
            } else {
                build.givePlayerItems(items);
            }
        }

        @Override
        public void givePlayerFluids(FluidStack... fluids) {
            build.givePlayerFluids(fluids);
        }

        @Override
        public void warn(String message) {
            GTUtility.sendChatToPlayer(player, String.format("§cWarning at block %d, %d, %d: %s§r", x, y, z, message));
        }

        @Override
        public void error(String message) {
            GTUtility.sendChatToPlayer(player, String.format("§cError at block %d, %d, %d: %s§r", x, y, z, message));
        }
    }

    private static class BlockItemCheckContext implements IBlockApplyContext {

        public World world;
        public int x, y, z;
        public EntityPlayer player;
        public FakePlayer fakePlayer;

        public HashMap<ItemId, Long> requiredItems = new HashMap<>();

        public HashMap<ItemId, Long> storedItems = new HashMap<>();
        public HashMap<FluidId, Long> storedFluids = new HashMap<>();

        @Override
        public EntityPlayer getFakePlayer() {
            if (fakePlayer == null) {
                fakePlayer = new FakePlayer((WorldServer) player.getEntityWorld(), player.getGameProfile());
            }

            return fakePlayer;
        }

        @Override
        public TileEntity getTileEntity() {
            return world.getTileEntity(x, y, z);
        }

        @Override
        public EntityPlayer getRealPlayer() {
            return player;
        }

        @Override
        public boolean tryApplyAction(double complexity) {
            return true;
        }

        @Override
        public boolean tryConsumeItems(ItemStack... items) {
            for (ItemStack item : items) {
                ItemId id = ItemId.create(item);

                Long stored = storedItems.get(id);
                if (stored == null) stored = 0l;

                long toConsume = Math.min(stored, item.stackSize);

                if (toConsume < stored) {
                    storedItems.put(id, stored - toConsume);
                } else {
                    storedItems.remove(id);
                }

                requiredItems.merge(id, (long) (item.stackSize - toConsume), Long::sum);
            }
            return true;
        }

        @Override
        public void givePlayerItems(ItemStack... items) {
            for (ItemStack item : items) {
                storedItems.merge(ItemId.createWithStackSize(item), (long) -item.stackSize, Long::sum);
            }
        }

        @Override
        public void givePlayerFluids(FluidStack... fluids) {
            for (FluidStack fluid : fluids) {
                storedFluids.merge(FluidId.createWithAmount(fluid), (long) -fluid.amount, Long::sum);
            }
        }

        @Override
        public void warn(String message) {
            GTUtility.sendChatToPlayer(player, String.format("§cWarning at block %d, %d, %d: %s§r", x, y, z, message));
        }

        @Override
        public void error(String message) {
            GTUtility.sendChatToPlayer(player, String.format("§cError at block %d, %d, %d: %s§r", x, y, z, message));
        }
    }

    public static class RequiredItemAnalysis {

        public HashMap<ItemId, Long> requiredItems;
        public HashMap<ItemId, Long> storedItems;
        public HashMap<FluidId, Long> storedFluids;
    }

    public static RequiredItemAnalysis getRequiredItemsForBuild(EntityPlayer player, List<PendingBlock> blocks) {
        BlockItemCheckContext context = new BlockItemCheckContext();
        context.player = player;
        context.world = player.getEntityWorld();

        for (PendingBlock block : blocks) {
            if (block.isInWorld(context.world)) {
                boolean isNew = true;

                if (!context.world.isAirBlock(block.x, block.y, block.z)) {
                    PendingBlock existing = PendingBlock.fromBlock(context.world, block.x, block.y, block.z);

                    if (PendingBlock.isSameBlock(existing, block)) {
                        isNew = false;
                    } else {
                        if (!block.isFree()) {
                            context.givePlayerItems(existing.toStack());
                        }
                    }
                }

                if (!block.isFree()) {
                    context.tryConsumeItems(block.toStack());
                }

                context.x = block.x;
                context.y = block.y;
                context.z = block.z;

                if (block.tileData != null) {
                    if (isNew) {
                        block.tileData.getRequiredItemsForNewBlock(context);
                    } else {
                        block.tileData.getRequiredItemsForExistingBlock(context);
                    }
                }
            }
        }

        RequiredItemAnalysis analysis = new RequiredItemAnalysis();
        analysis.requiredItems = context.requiredItems;
        analysis.storedItems = context.storedItems;
        analysis.storedFluids = context.storedFluids;

        return analysis;
    }
}