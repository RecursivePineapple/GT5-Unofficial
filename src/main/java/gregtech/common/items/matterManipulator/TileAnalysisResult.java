package gregtech.common.items.matterManipulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.gson.JsonElement;
import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.IAlignmentProvider;
import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;

import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AEColor;
import appeng.helpers.ICustomNameObject;
import appeng.parts.automation.UpgradeInventory;
import appeng.parts.p2p.PartP2PTunnelNormal;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileCableBus;
import appeng.util.IWideReadableNumberConverter;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;
import appeng.util.SettingsFrom;
import gregtech.GTMod;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.VoidingMode;
import gregtech.api.interfaces.IConfigurationCircuitSupport;
import gregtech.api.interfaces.IDataCopyable;
import gregtech.api.interfaces.metatileentity.IConnectable;
import gregtech.api.interfaces.metatileentity.IFluidLockable;
import gregtech.api.interfaces.metatileentity.IItemLockable;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicMachine;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.metatileentity.implementations.MTEHatchOutput;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.util.GTUtility.ItemId;
import gregtech.common.covers.CoverInfo;
import gregtech.common.items.matterManipulator.BlockAnalyzer.IBlockAnalysisContext;
import gregtech.common.items.matterManipulator.BlockAnalyzer.IBlockApplyContext;
import gregtech.common.tileentities.machines.MTEHatchOutputBusME;
import gregtech.common.tileentities.machines.MTEHatchOutputME;
import gtPlusPlus.xmod.gregtech.api.enums.GregtechItemList;

/**
 * Stores all data needed to reconstruct a tile entity
 */
public class TileAnalysisResult {

    // hopefully these field are self explanitory

    public byte mConnections = 0;
    public byte mGTColour = -1;
    public ForgeDirection mGTFront = null, mGTMainFacing = null;
    public short mGTFlags = 0;
    public ExtendedFacing mGTFacing = null;
    public CoverData[] mCovers = null;
    public byte mStrongRedstone = 0;
    public String mGTCustomName = null;
    public byte mGTGhostCircuit = 0;
    public PortableItemStack mGTItemLock = null;
    public String mGTFluidLock = null;
    public int mGTMode = 0;
    public NBTTagCompound mGTData = null;
    public long mGTMEBusCapacity = 0;

    public AEColor mAEColour = null;
    public ForgeDirection mAEUp = null, mAEForward = null;
    public NBTTagCompound mAEConfig = null;
    public PortableItemStack[] mAEUpgrades = null;
    public String mAECustomName = null;
    public AEPartData[] mAEParts = null;
    public InventoryAnalysis mAECells = null;
    public InventoryAnalysis mAEPatterns = null;

    public InventoryAnalysis mInventory = null;

    private static int counter = 0;
    private static final short GT_BASIC_IO_PUSH_ITEMS = (short) (0b1 << counter++);
    private static final short GT_BASIC_IO_PUSH_FLUIDS = (short) (0b1 << counter++);
    private static final short GT_BASIC_IO_DISABLE_FILTER = (short) (0b1 << counter++);
    private static final short GT_BASIC_IO_DISABLE_MULTISTACK = (short) (0b1 << counter++);
    private static final short GT_BASIC_IO_INPUT_FROM_OUTPUT_SIDE = (short) (0b1 << counter++);
    private static final short GT_INPUT_BUS_NO_SORTING = (short) (0b1 << counter++);
    private static final short GT_INPUT_BUS_NO_LIMITING = (short) (0b1 << counter++);
    private static final short GT_INPUT_BUS_NO_FILTERING = (short) (0b1 << counter++);
    private static final short GT_MULTI_PROTECT_ITEMS = (short) (0b1 << counter++);
    private static final short GT_MULTI_PROTECT_FLUIDS = (short) (0b1 << counter++);
    private static final short GT_MULTI_BATCH_MODE = (short) (0b1 << counter++);
    private static final short GT_MULTI_INPUT_SEPARATION = (short) (0b1 << counter++);
    private static final short GT_MULTI_RECIPE_LOCK = (short) (0b1 << counter++);

    private static final ForgeDirection[] ALL_DIRECTIONS = ForgeDirection.values();

    public TileAnalysisResult() {

    }

    public TileAnalysisResult(IBlockAnalysisContext context, TileEntity te) {
        if (te instanceof IGregTechTileEntity gte) {
            IMetaTileEntity mte = gte.getMetaTileEntity();

            // save the colour
            if (gte.getColorization() != -1) mGTColour = gte.getColorization();

            // if the machine is a singleblock, store its data
            if (mte instanceof MTEBasicMachine basicMachine) {
                mGTMainFacing = basicMachine.mMainFacing;

                byte flags = 0;

                if (basicMachine.mItemTransfer) flags |= GT_BASIC_IO_PUSH_ITEMS;
                if (basicMachine.mFluidTransfer) flags |= GT_BASIC_IO_PUSH_FLUIDS;
                if (basicMachine.mDisableFilter) flags |= GT_BASIC_IO_DISABLE_FILTER;
                if (basicMachine.mDisableMultiStack) flags |= GT_BASIC_IO_DISABLE_MULTISTACK;
                if (basicMachine.mAllowInputFromOutputSide) flags |= GT_BASIC_IO_INPUT_FROM_OUTPUT_SIDE;

                if (flags != 0) mGTFlags = flags;
            }

            // if the machine is a pipe/cable/etc, store its connections
            if (mte instanceof IConnectable connectable) {
                byte con = 0;

                for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                    if (connectable.isConnectedAtSide(dir)) {
                        con |= dir.flag;
                    }
                }

                mConnections = con;
            }

            // if the machine is alignable (basically everything) store its facing directly or extended alignment
            if (mte instanceof IAlignmentProvider provider) {
                IAlignment alignment = provider.getAlignment();

                mGTFacing = alignment != null ? alignment.getExtendedFacing() : null;
            } else {
                mGTFront = nullIfUnknown(gte.getFrontFacing());
            }

            CoverData[] covers = new CoverData[6];
            boolean hasCover = false;
            byte strongRedstone = 0;

            // check each side for covers
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                if (gte.getCoverIDAtSide(dir) != 0) {
                    covers[dir.ordinal()] = CoverData.fromInfo(gte.getCoverInfoAtSide(dir));
                    hasCover = true;

                    if (gte.getRedstoneOutputStrength(dir)) {
                        strongRedstone |= dir.flag;
                    }
                }
            }

            if (hasCover) mCovers = covers;
            mStrongRedstone = strongRedstone;

            // check if the machine has a custom name
            if (mte instanceof ICustomNameObject customName && customName.hasCustomName()) {
                mGTCustomName = customName.getCustomName();
            }

            // check if the machine has a ghost circuit slot
            if (mte instanceof IConfigurationCircuitSupport ghostCircuit && ghostCircuit.allowSelectCircuit()) {
                ItemStack circuit = mte.getStackInSlot(ghostCircuit.getCircuitSlot());

                if (circuit == null || circuit.getItem() == null) {
                    mGTGhostCircuit = 0;
                } else if (circuit.getItem() == ItemList.Circuit_Integrated.getItem()) {
                    mGTGhostCircuit = (byte) Items.feather.getDamage(circuit);
                } else if (circuit.getItem() == GregtechItemList.Circuit_BioRecipeSelector.getItem()) {
                    mGTGhostCircuit = (byte) (Items.feather.getDamage(circuit) + 24);
                } else if (circuit.getItem() == GregtechItemList.Circuit_T3RecipeSelector.getItem()) {
                    mGTGhostCircuit = (byte) (Items.feather.getDamage(circuit) + 48);
                }
            }

            // check if the machine is an input bus
            if (mte instanceof MTEHatchInputBus inputBus) {
                if (inputBus.disableSort) mGTFlags |= GT_INPUT_BUS_NO_SORTING;
                if (inputBus.disableLimited) mGTFlags |= GT_INPUT_BUS_NO_LIMITING;
                if (inputBus.disableFilter) mGTFlags |= GT_INPUT_BUS_NO_FILTERING;
            }

            // check if the machine has a locked item
            if (mte instanceof IItemLockable lockable && lockable.acceptsItemLock()
                && lockable.getLockedItem() != null) {
                mGTItemLock = new PortableItemStack(lockable.getLockedItem());
            }

            // check if the machine is an output hatch
            if (mte instanceof MTEHatchOutput outputHatch) {
                mGTMode = outputHatch.getMode();
            }

            // check if the machine has a locked fluid
            if (mte instanceof IFluidLockable lockable && lockable.isFluidLocked()) {
                mGTFluidLock = lockable.getLockedFluidName();
            }

            // check if the machine is a multi and store its settings
            if (mte instanceof MTEMultiBlockBase multi) {
                mGTMode = multi.machineMode;

                if (multi.getVoidingMode().protectFluid) mGTFlags |= GT_MULTI_PROTECT_FLUIDS;
                if (multi.getVoidingMode().protectItem) mGTFlags |= GT_MULTI_PROTECT_ITEMS;

                if (multi.isBatchModeEnabled()) mGTFlags |= GT_MULTI_BATCH_MODE;
                if (multi.isInputSeparationEnabled()) mGTFlags |= GT_MULTI_INPUT_SEPARATION;
                if (multi.isRecipeLockingEnabled()) mGTFlags |= GT_MULTI_RECIPE_LOCK;
            }

            // check if the machine can be copied with a data stick
            if (mte instanceof IDataCopyable copyable) {
                try {
                    // There's no reason for this EntityPlayer parameter besides sending chat messages, so we just fail
                    // if it actually needs the player.
                    NBTTagCompound data = copyable.getCopiedData(null);

                    if (data != null && !data.hasNoTags()) {
                        mGTData = data;
                    }
                } catch (Throwable t) {
                    // Probably an NPE, but we're catching Throwable just to be safe
                    GTMod.GT_FML_LOGGER.error("Could not copy IDataCopyable's data", t);
                }
            }

            if (mte instanceof MTEHatchOutputME || mte instanceof MTEHatchOutputBusME) {
                NBTTagCompound tag = new NBTTagCompound();
                mte.setItemNBT(tag);
                mGTMEBusCapacity = tag.getLong("baseCapacity");
            }
        }

        // check if the tile is an ae tile and store its facing info + config
        if (te instanceof AEBaseTile ae) {
            mAEUp = nullIfUnknown(ae.getUp());
            mAEForward = nullIfUnknown(ae.getForward());
            mAEConfig = ae.downloadSettings(SettingsFrom.MEMORY_CARD);
        }

        // check if the tile has a custom name
        if (te instanceof ICustomNameObject customName && !(te instanceof TileCableBus)) {
            mAECustomName = customName.hasCustomName() ? customName.getCustomName() : null;
        }

        // check if the tile has AE inventories
        if (te instanceof ISegmentedInventory segmentedInventory) {
            if (segmentedInventory.getInventoryByName("upgrades") instanceof UpgradeInventory upgrades) {
                mAEUpgrades = MMUtils.fromInventory(upgrades);
            }

            IInventory cells = segmentedInventory.getInventoryByName("cells");
            if (cells != null) {
                mAECells = InventoryAnalysis.fromInventory(cells, false);
            }

            IInventory patterns = segmentedInventory.getInventoryByName("patterns");
            if (patterns != null) {
                mAEPatterns = InventoryAnalysis.fromInventory(patterns, false);
            }
        }

        // check all sides for parts (+UNKNOWN for cables)
        if (te instanceof IPartHost partHost) {
            mAEParts = new AEPartData[ALL_DIRECTIONS.length];

            for (ForgeDirection dir : ALL_DIRECTIONS) {
                IPart part = partHost.getPart(dir);

                if (part != null) mAEParts[dir.ordinal()] = new AEPartData(part);
            }
        }

        // check its inventory
        if (te instanceof IInventory inventory) {
            mInventory = InventoryAnalysis.fromInventory(inventory, false);
        }
    }

    private static final TileAnalysisResult NO_OP = new TileAnalysisResult();

    public boolean doesAnything() {
        return !this.equals(NO_OP);
    }

    @SuppressWarnings("unused")
    public boolean apply(IBlockApplyContext ctx) {
        TileEntity te = ctx.getTileEntity();

        if (te instanceof IGregTechTileEntity gte) {
            IMetaTileEntity mte = gte.getMetaTileEntity();

            gte.setColorization(mGTColour);

            if (mte instanceof MTEBasicMachine basicMachine) {
                if (mGTMainFacing != null) {
                    basicMachine.setMainFacing(mGTMainFacing);
                    // stop MTEBasicMachine.doDisplayThings from overwriting the setFrontFacing call when the block is
                    // newly placed
                    basicMachine.mHasBeenUpdated = true;
                }

                basicMachine.mItemTransfer = (mGTFlags & GT_BASIC_IO_PUSH_ITEMS) != 0;
                basicMachine.mFluidTransfer = (mGTFlags & GT_BASIC_IO_PUSH_FLUIDS) != 0;
                basicMachine.mDisableFilter = (mGTFlags & GT_BASIC_IO_DISABLE_FILTER) != 0;
                basicMachine.mDisableMultiStack = (mGTFlags & GT_BASIC_IO_DISABLE_MULTISTACK) != 0;
                basicMachine.mAllowInputFromOutputSide = (mGTFlags & GT_BASIC_IO_INPUT_FROM_OUTPUT_SIDE) != 0;
            }

            // only (dis)connect sides that need to be updated
            if (mte instanceof IConnectable connectable) {
                for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                    boolean shouldBeConnected = (mConnections & dir.flag) != 0;
                    if (connectable.isConnectedAtSide(dir) != shouldBeConnected) {
                        if (shouldBeConnected) {
                            connectable.connect(dir);
                        } else {
                            connectable.disconnect(dir);
                        }
                    }
                }
            }

            // set the machine's facing and alignment
            if (mte instanceof IAlignmentProvider provider) {
                IAlignment alignment = provider.getAlignment();

                if (mGTFacing != null && alignment != null) {
                    gte.setFrontFacing(mGTFacing.getDirection());
                    alignment.setExtendedFacing(mGTFacing);
                }
            } else {
                if (mGTFront != null) {
                    gte.setFrontFacing(mGTFront);
                }
            }

            // install/remove/update the covers
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                CoverData expected = mCovers == null ? null : mCovers[dir.ordinal()];
                CoverInfo actual = new CoverInfo(
                    dir,
                    gte.getCoverIDAtSide(dir),
                    gte,
                    gte.getComplexCoverDataAtSide(dir));

                if (actual == null && expected != null) {
                    installCover(ctx, gte, dir, expected);
                } else if (actual != null && expected == null) {
                    removeCover(ctx, gte, dir);
                } else if (actual != null && expected != null) {
                    if (actual.getCoverID() != expected.getCoverID()) {
                        removeCover(ctx, gte, dir);
                        installCover(ctx, gte, dir, expected);
                    } else if (!Objects.equals(actual.getCoverData(), expected.getCoverData())) {
                        updateCover(ctx, gte, dir, expected);
                    }
                }

                // set the redstone strength
                gte.setRedstoneOutputStrength(dir, (mStrongRedstone & dir.flag) != 0);
            }

            // set the custom name
            if (mte instanceof ICustomNameObject customName && mGTCustomName != null) {
                customName.setCustomName(mGTCustomName);
            }

            // set the ghost circuit
            if (mte instanceof IConfigurationCircuitSupport ghostCircuit && ghostCircuit.allowSelectCircuit()) {
                ItemStack circuit = null;

                if (mGTGhostCircuit > 48) {
                    circuit = GregtechItemList.Circuit_T3RecipeSelector.getWithDamage(0, mGTGhostCircuit - 48);
                } else if (mGTGhostCircuit > 24) {
                    circuit = GregtechItemList.Circuit_BioRecipeSelector.getWithDamage(0, mGTGhostCircuit - 24);
                } else if (mGTGhostCircuit > 0) {
                    circuit = ItemList.Circuit_Integrated.getWithDamage(0, mGTGhostCircuit);
                }

                mte.setInventorySlotContents(ghostCircuit.getCircuitSlot(), circuit);
                mte.markDirty();
            }

            // set the various input bus options
            if (mte instanceof MTEHatchInputBus inputBus) {
                inputBus.disableSort = (mGTFlags & GT_INPUT_BUS_NO_SORTING) != 0;
                inputBus.disableLimited = (mGTFlags & GT_INPUT_BUS_NO_LIMITING) != 0;
                inputBus.disableFilter = (mGTFlags & GT_INPUT_BUS_NO_FILTERING) != 0;
            }

            // set the locked item
            if (mte instanceof IItemLockable lockable && lockable.acceptsItemLock()) {
                ItemStack lockedItem = mGTItemLock == null ? null : mGTItemLock.toStack();

                lockable.setLockedItem(lockedItem);
            }

            // set the output hatch mode
            if (mte instanceof MTEHatchOutput outputHatch) {
                outputHatch.mMode = (byte) mGTMode;
            }

            // set the locked fluid
            if (mte instanceof IFluidLockable lockable && lockable.isFluidLocked()) {
                lockable.setLockedFluidName(mGTFluidLock);
            }

            // set the various multi options
            if (mte instanceof MTEMultiBlockBase multi) {
                multi.machineMode = mGTMode;

                if (multi.supportsVoidProtection()) {
                    boolean protectFluids = (mGTFlags & GT_MULTI_PROTECT_FLUIDS) != 0;
                    boolean protectItems = (mGTFlags & GT_MULTI_PROTECT_ITEMS) != 0;

                    VoidingMode voidingMode = null;

                    for (VoidingMode mode : VoidingMode.values()) {
                        if (mode.protectFluid == protectFluids && mode.protectItem == protectItems) {
                            voidingMode = mode;
                            break;
                        }
                    }

                    if (voidingMode != null) {
                        multi.setVoidingMode(voidingMode);
                    } else {
                        throw new RuntimeException(
                            "This should never happen. protectFluids=" + protectFluids
                                + ", protectItems="
                                + protectItems);
                    }
                }

                if (multi.supportsBatchMode()) multi.setBatchMode((mGTFlags & GT_MULTI_BATCH_MODE) != 0);
                if (multi.supportsInputSeparation())
                    multi.setInputSeparation((mGTFlags & GT_MULTI_INPUT_SEPARATION) != 0);
                if (multi.supportsSingleRecipeLocking()) multi.setRecipeLocking((mGTFlags & GT_MULTI_RECIPE_LOCK) != 0);
            }

            // paste the data
            if (mte instanceof IDataCopyable copyable) {
                NBTTagCompound data = mGTData == null ? new NBTTagCompound() : mGTData;

                try {
                    // There's no reason for this EntityPlayer parameter besides sending chat messages, so we just fail
                    // if it actually needs the player.
                    if (!copyable.pasteCopiedData(null, data)) {
                        return false;
                    }
                } catch (Throwable t) {
                    // Probably an NPE, but we're catching Throwable just to be safe
                    GTMod.GT_FML_LOGGER.error("Could not paste IDataCopyable's data", t);
                }
            }
        }

        // apply upgrades, cells, and patterns
        if (te instanceof ISegmentedInventory segmentedInventory) {
            if (segmentedInventory.getInventoryByName("upgrades") instanceof UpgradeInventory upgrades) {
                MMUtils.installUpgrades(ctx, upgrades, mAEUpgrades, true, false);
            }

            IInventory cells = segmentedInventory.getInventoryByName("cells");
            if (mAECells != null && cells != null) {
                mAECells.apply(ctx, cells, true, false);
            }

            IInventory patterns = segmentedInventory.getInventoryByName("patterns");
            if (mAEPatterns != null && patterns != null) {
                mAEPatterns.apply(ctx, patterns, true, false);
            }
        }

        // set ae tile orientation and config
        if (te instanceof AEBaseTile ae) {
            if (mAEUp != null && mAEForward != null) {
                ae.setOrientation(mAEForward, mAEUp);
            }

            if (mAEConfig != null) {
                ae.uploadSettings(SettingsFrom.MEMORY_CARD, mAEConfig);
            }
        }

        // set ae tile custom name
        if (mAECustomName != null && te instanceof ICustomNameObject customName && !(te instanceof TileCableBus)) {
            customName.setCustomName(mAECustomName);
        }

        // add/remove/update ae parts and cables
        if (te instanceof IPartHost partHost && mAEParts != null) {
            for (ForgeDirection dir : ALL_DIRECTIONS) {
                IPart part = partHost.getPart(dir);
                AEPartData expected = mAEParts[dir.ordinal()];

                ItemId actualItem = part == null ? null
                    : ItemId.createWithoutNBT(part.getItemStack(PartItemStack.Break));
                ItemId expectedItem = expected == null ? null
                    : ItemId.createWithoutNBT(expected.getEffectivePartStack());

                boolean isAttunable = part instanceof PartP2PTunnelNormal && expected != null && expected.isAttunable();

                // if the p2p is attunable (non-interface) then we don't need to remove it
                if (!isAttunable) {
                    // change the part into the proper version
                    if (actualItem != null && (expectedItem == null || !Objects.equals(actualItem, expectedItem))) {
                        removePart(ctx, partHost, dir, false);
                        actualItem = null;
                    }

                    if (actualItem == null && expectedItem != null) {
                        if (!installPart(ctx, partHost, dir, expected, false)) {
                            return false;
                        }
                    }
                }

                if (expected != null) {
                    if (!expected.updatePart(ctx, partHost, dir)) {
                        return false;
                    }
                }

                Platform.notifyBlocksOfNeighbors(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
            }
        }

        // update the inventory
        if (te instanceof IInventory inventory && mInventory != null) {
            mInventory.apply(ctx, inventory, true, false);
        }

        return true;
    }

    private void removeCover(IBlockApplyContext context, IGregTechTileEntity gte, ForgeDirection side) {
        if (gte.getCoverIDAtSide(side) != 0) {
            context.givePlayerItems(gte.removeCoverAtSide(side, true));
        }
    }

    private void installCover(IBlockApplyContext context, IGregTechTileEntity gte, ForgeDirection side,
        CoverData cover) {
        if (gte.canPlaceCoverItemAtSide(side, cover.getCover()) && context.tryConsumeItems(cover.getCover())) {
            gte.setCoverIdAndDataAtSide(
                side,
                cover.getCoverID(),
                cover.getCoverBehaviour()
                    .allowsCopyPasteTool() ? cover.getCoverData() : null);
        }
    }

    private void updateCover(IBlockApplyContext context, IGregTechTileEntity gte, ForgeDirection side,
        CoverData target) {
        if (gte.getCoverIDAtSide(side) == target.getCoverID() && gte.getCoverBehaviorAtSideNew(side)
            .allowsCopyPasteTool()) {
            gte.setCoverDataAtSide(side, target.getCoverData());
        }
    }

    private void removePart(IBlockApplyContext context, IPartHost partHost, ForgeDirection side, boolean simulate) {
        IPart part = partHost.getPart(side);

        if (part == null) return;

        List<ItemStack> drops = new ArrayList<>();

        part.getDrops(drops, false);

        context.givePlayerItems(
            drops.stream()
                .map(ItemStack::copy)
                .toArray(ItemStack[]::new));

        ItemStack partStack = part.getItemStack(PartItemStack.Break)
            .copy();

        NBTTagCompound tag = partStack.getTagCompound();

        // manually clear the name
        if (tag != null) {
            tag.removeTag("display");

            if (tag.hasNoTags()) {
                partStack.setTagCompound(null);
            }
        }

        context.givePlayerItems(partStack);

        if (!simulate) partHost.removePart(side, false);
    }

    private boolean installPart(IBlockApplyContext context, IPartHost partHost, ForgeDirection side,
        AEPartData partData, boolean simulate) {
        ItemStack partStack = partData.getEffectivePartStack();

        if (!partHost.canAddPart(partStack, side)) {
            return false;
        }

        context.tryConsumeItems(partStack);

        if (!simulate) {
            if (partHost.addPart(partStack, side, context.getRealPlayer()) == null) {
                context.givePlayerItems(partStack);
                return false;
            }
        }

        return true;
    }

    /**
     * Get the required items for a block that exists in the world
     * 
     * @return True when this result can be applied to the tile, false otherwise
     */
    public boolean getRequiredItemsForExistingBlock(IBlockApplyContext context) {
        TileEntity te = context.getTileEntity();

        if (te instanceof IGregTechTileEntity gte) {
            for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                CoverData target = mCovers == null ? null : mCovers[side.ordinal()];
                CoverInfo actual = new CoverInfo(
                    side,
                    gte.getCoverIDAtSide(side),
                    gte,
                    gte.getComplexCoverDataAtSide(side));

                if (actual != null && (target == null || actual.getCoverID() != target.getCoverID())) {
                    context.givePlayerItems(
                        gte.getCoverItemAtSide(side)
                            .copy());
                    actual = null;
                }

                if (actual == null && target != null) {
                    if (gte.canPlaceCoverItemAtSide(side, target.getCover())) {
                        context.tryConsumeItems(target.getCover());
                    }
                }
            }
        }

        if (te instanceof ISegmentedInventory segmentedInventory) {
            if (mAEUpgrades != null
                && segmentedInventory.getInventoryByName("upgrades") instanceof UpgradeInventory upgrades) {
                MMUtils.installUpgrades(context, upgrades, mAEUpgrades, true, true);
            }

            IInventory cells = segmentedInventory.getInventoryByName("cells");
            if (mAECells != null && cells != null) {
                mAECells.apply(context, cells, true, true);
            }
        }

        if (mAEParts != null && te instanceof IPartHost partHost) {
            for (ForgeDirection dir : ALL_DIRECTIONS) {
                IPart part = partHost.getPart(dir);
                AEPartData expected = mAEParts[dir.ordinal()];

                ItemId actualItem = part == null ? null
                    : ItemId.createWithoutNBT(part.getItemStack(PartItemStack.Break));
                ItemId expectedItem = expected == null ? null
                    : ItemId.createWithoutNBT(expected.getEffectivePartStack());

                boolean isAttunable = part instanceof PartP2PTunnelNormal && expected != null && expected.isAttunable();

                if (!isAttunable) {
                    if ((expectedItem == null || !Objects.equals(actualItem, expectedItem)) && actualItem != null) {
                        removePart(context, partHost, dir, true);
                        actualItem = null;
                    }

                    if (actualItem == null && expectedItem != null) {
                        if (!installPart(context, partHost, dir, expected, true)) {
                            return false;
                        }
                    }
                }

                if (expected != null) {
                    if (!expected.getRequiredItemsForExistingPart(context, partHost, dir)) {
                        return false;
                    }
                }

                Platform.notifyBlocksOfNeighbors(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
            }
        }

        if (mInventory != null && te instanceof IInventory inventory) {
            mInventory.apply(context, inventory, true, true);
        }

        return true;
    }

    /**
     * Get the required items for a block that doesn't exist
     * 
     * @return True if this tile result is valid, false otherwise
     */
    public boolean getRequiredItemsForNewBlock(IBlockApplyContext context) {
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            CoverData target = mCovers == null ? null : mCovers[side.ordinal()];

            if (target != null) {
                context.tryConsumeItems(target.getCover());
            }
        }

        if (mAEUpgrades != null) {
            for (PortableItemStack upgrade : mAEUpgrades) {
                context.tryConsumeItems(upgrade.toStack());
            }
        }

        if (mAECells != null) {
            for (IItemProvider cell : mAECells.mItems) {
                if (cell != null) {
                    cell.getStack(context, true);
                }
            }
        }

        if (mAEParts != null) {
            for (ForgeDirection dir : ALL_DIRECTIONS) {
                AEPartData expected = mAEParts[dir.ordinal()];

                if (expected == null) continue;

                context.tryConsumeItems(expected.getEffectivePartStack());

                if (!expected.getRequiredItemsForNewPart(context)) {
                    return false;
                }
            }
        }

        if (mInventory != null) {
            for (IItemProvider item : mInventory.mItems) {
                if (item != null) {
                    item.getStack(context, true);
                }
            }
        }

        return true;
    }

    public NBTTagCompound getItemTag() {
        if (mGTMEBusCapacity != 0) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setLong("baseCapacity", mGTMEBusCapacity);
            return tag;
        }

        return null;
    }

    public String getItemDetails() {
        if (mGTMEBusCapacity != 0) {
            IWideReadableNumberConverter nc = ReadableNumberConverter.INSTANCE;
            return String.format(" (cache capacity: %s)", nc.toWideReadableForm(mGTMEBusCapacity));
        }

        return "";
    }

    private static ForgeDirection nullIfUnknown(ForgeDirection dir) {
        return dir == ForgeDirection.UNKNOWN ? null : dir;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + mConnections;
        result = prime * result + mGTColour;
        result = prime * result + ((mGTFront == null) ? 0 : mGTFront.hashCode());
        result = prime * result + ((mGTMainFacing == null) ? 0 : mGTMainFacing.hashCode());
        result = prime * result + mGTFlags;
        result = prime * result + ((mGTFacing == null) ? 0 : mGTFacing.hashCode());
        result = prime * result + Arrays.hashCode(mCovers);
        result = prime * result + mStrongRedstone;
        result = prime * result + ((mGTCustomName == null) ? 0 : mGTCustomName.hashCode());
        result = prime * result + mGTGhostCircuit;
        result = prime * result + ((mGTItemLock == null) ? 0 : mGTItemLock.hashCode());
        result = prime * result + ((mGTFluidLock == null) ? 0 : mGTFluidLock.hashCode());
        result = prime * result + mGTMode;
        result = prime * result + Long.hashCode(mGTMEBusCapacity);
        result = prime * result + ((mGTData == null) ? 0 : mGTData.hashCode());
        result = prime * result + ((mAEColour == null) ? 0 : mAEColour.hashCode());
        result = prime * result + ((mAEUp == null) ? 0 : mAEUp.hashCode());
        result = prime * result + ((mAEForward == null) ? 0 : mAEForward.hashCode());
        result = prime * result + ((mAEConfig == null) ? 0 : mAEConfig.hashCode());
        result = prime * result + Arrays.hashCode(mAEUpgrades);
        result = prime * result + ((mAECustomName == null) ? 0 : mAECustomName.hashCode());
        result = prime * result + Arrays.hashCode(mAEParts);
        result = prime * result + ((mAECells == null) ? 0 : mAECells.hashCode());
        result = prime * result + ((mInventory == null) ? 0 : mInventory.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TileAnalysisResult other = (TileAnalysisResult) obj;
        if (mConnections != other.mConnections) return false;
        if (mGTColour != other.mGTColour) return false;
        if (mGTFront != other.mGTFront) return false;
        if (mGTMainFacing != other.mGTMainFacing) return false;
        if (mGTFlags != other.mGTFlags) return false;
        if (mGTFacing != other.mGTFacing) return false;
        if (!Arrays.equals(mCovers, other.mCovers)) return false;
        if (mStrongRedstone != other.mStrongRedstone) return false;
        if (mGTCustomName == null) {
            if (other.mGTCustomName != null) return false;
        } else if (!mGTCustomName.equals(other.mGTCustomName)) return false;
        if (mGTGhostCircuit != other.mGTGhostCircuit) return false;
        if (mGTItemLock == null) {
            if (other.mGTItemLock != null) return false;
        } else if (!mGTItemLock.equals(other.mGTItemLock)) return false;
        if (mGTFluidLock == null) {
            if (other.mGTFluidLock != null) return false;
        } else if (!mGTFluidLock.equals(other.mGTFluidLock)) return false;
        if (mGTMode != other.mGTMode) return false;
        if (mGTData == null) {
            if (other.mGTData != null) return false;
        } else if (!mGTData.equals(other.mGTData)) return false;
        if (mGTMEBusCapacity != other.mGTMEBusCapacity) return false;
        if (mAEColour != other.mAEColour) return false;
        if (mAEUp != other.mAEUp) return false;
        if (mAEForward != other.mAEForward) return false;
        if (mAEConfig == null) {
            if (other.mAEConfig != null) return false;
        } else if (!mAEConfig.equals(other.mAEConfig)) return false;
        if (!Arrays.equals(mAEUpgrades, other.mAEUpgrades)) return false;
        if (mAECustomName == null) {
            if (other.mAECustomName != null) return false;
        } else if (!mAECustomName.equals(other.mAECustomName)) return false;
        if (!Arrays.equals(mAEParts, other.mAEParts)) return false;
        if (mAECells == null) {
            if (other.mAECells != null) return false;
        } else if (!mAECells.equals(other.mAECells)) return false;
        if (mInventory == null) {
            if (other.mInventory != null) return false;
        } else if (!mInventory.equals(other.mInventory)) return false;
        return true;
    }
}
