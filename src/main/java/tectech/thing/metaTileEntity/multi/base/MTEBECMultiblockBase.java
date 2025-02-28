package tectech.thing.metaTileEntity.multi.base;

import static gregtech.api.casing.Casings.MolecularCasing;
import static gregtech.api.util.GTUtility.filterValidMTEs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.IRecipeInput;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.ITransaction;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.structure.IStructureProvider;
import gregtech.api.structure.StructureWrapper;
import gregtech.api.structure.StructureWrapperInstanceInfo;
import gregtech.api.util.GTUtility;
import gregtech.api.util.IGTHatchAdder;
import gregtech.common.tileentities.machines.IDualInputHatchAware;
import gregtech.common.tileentities.machines.IDualInputInventory;
import gregtech.common.tileentities.machines.MTEHatchCraftingInputME;
import gregtech.common.tileentities.machines.MTEHatchCraftingInputSlave;
import gregtech.common.tileentities.machines.MTEHatchInputBusME;
import gtPlusPlus.xmod.gregtech.common.blocks.textures.TexturesGtBlock;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryElement;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryNetwork;
import tectech.thing.CustomItemList;
import tectech.thing.metaTileEntity.hatch.MTEHatchBEC;

public abstract class MTEBECMultiblockBase<TSelf extends MTEBECMultiblockBase<TSelf>> extends TTMultiblockBase
    implements ISurvivalConstructable, BECFactoryElement, IStructureProvider<TSelf>, IDualInputHatchAware {

    protected static final String STRUCTURE_PIECE_MAIN = "main";

    protected final List<BECFactoryElement> mBECHatches = new ArrayList<>();

    protected BECFactoryNetwork network;

    protected final StructureWrapper<TSelf> structure;
    protected final StructureWrapperInstanceInfo<TSelf> structureInstanceInfo;

    public MTEBECMultiblockBase(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);

        structure = new StructureWrapper<TSelf>(this);
        structureInstanceInfo = null;

        structure.loadStructure();
    }

    protected MTEBECMultiblockBase(TSelf base) {
        super(base.mName);

        structure = base.structure;
        structureInstanceInfo = new StructureWrapperInstanceInfo<>(structure);
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack aStack) {
        return false;
    }

    @Override
    public boolean shouldCheckMaintenance() {
        return false;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 10_000;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean active, boolean redstoneLevel) {

        ArrayList<ITexture> textures = new ArrayList<>(2);

        textures.add(getCasingTexture());

        if (side == facing) {
            if (active) {
                textures.add(
                    TextureFactory.builder()
                        .addIcon(getActiveTexture())
                        .extFacing()
                        .build());
            } else {
                textures.add(
                    TextureFactory.builder()
                        .addIcon(getInactiveTexture())
                        .extFacing()
                        .build());
            }
        }

        return textures.toArray(new ITexture[textures.size()]);
    }

    protected ITexture getCasingTexture() {
        return MolecularCasing.getCasingTexture();
    }

    protected IIconContainer getActiveTexture() {
        return TexturesGtBlock.Overlay_Machine_Controller_Advanced_Active;
    }

    protected IIconContainer getInactiveTexture() {
        return TexturesGtBlock.Overlay_Machine_Controller_Advanced;
    }

    @Override
    public IStructureDefinition<? extends TTMultiblockBase> getStructure_EM() {
        return structure.structureDefinition;
    }

    @Override
    public StructureWrapperInstanceInfo<TSelf> getWrapperInstanceInfo() {
        return structureInstanceInfo;
    }

    @Override
    protected void clearHatches_EM() {
        super.clearHatches_EM();

        mBECHatches.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        structureInstanceInfo.construct((TSelf) this, stackSize, hintsOnly);
    }

    @Override
    @SuppressWarnings("unchecked")
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        return structureInstanceInfo.survivalConstruct((TSelf) this, stackSize, elementBudget, env);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        return structureInstanceInfo.checkStructure((TSelf) this);
    }

    private String errorMessage;

    @Override
    protected void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {
        super.drawTexts(screenElements, inventorySlot);

        screenElements.widgets(new FakeSyncWidget.StringSyncer(() -> {
            structureInstanceInfo.validate();
            return structureInstanceInfo.getErrorMessage();
        }, error -> errorMessage = error),
            TextWidget.dynamicString(() -> errorMessage == null ? "" : errorMessage)
                .setTextAlignment(Alignment.CenterLeft)
                .setEnabled(errorMessage != null && !errorMessage.isEmpty()));
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
        tag.setString("network", network == null ? "null" : network.toString());
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        currenttip.add(
            "Network: " + accessor.getNBTData()
                .getString("network"));
    }

    @Override
    public void getNeighbours(Collection<BECFactoryElement> neighbours) {
        IGregTechTileEntity base = getBaseMetaTileEntity();

        if (base == null || base.isDead()) return;

        neighbours.addAll(mBECHatches);
    }

    @Override
    public BECFactoryNetwork getNetwork() {
        return this.network;
    }

    @Override
    public void setNetwork(BECFactoryNetwork network) {
        this.network = network;
    }

    @Override
    public ConnectionType getConnectionOnSide(ForgeDirection side) {
        return ConnectionType.NONE;
    }

    protected boolean isServerSide() {
        IGregTechTileEntity igte = getBaseMetaTileEntity();

        if (igte == null || igte.isDead()) return FMLCommonHandler.instance().getSide() == Side.SERVER;

        return igte.isServerSide();
    }

    public enum BECHatches implements IHatchElement<MTEBECMultiblockBase<?>> {

        Hatch(MTEHatchBEC.class) {

            @Override
            public long count(MTEBECMultiblockBase<?> t) {
                return t.mBECHatches.size();
            }
        };

        private final List<? extends Class<? extends IMetaTileEntity>> mteClasses;

        @SafeVarargs
        BECHatches(Class<? extends IMetaTileEntity>... mteClasses) {
            this.mteClasses = Collections.unmodifiableList(Arrays.asList(mteClasses));
        }

        @Override
        public List<? extends Class<? extends IMetaTileEntity>> mteClasses() {
            return mteClasses;
        }

        @Override
        public String getDisplayName() {
            return switch (this) {
                case Hatch -> CustomItemList.becConnectorHatch.get(1)
                    .getDisplayName();
            };
        }

        @Override
        public IGTHatchAdder<? super MTEBECMultiblockBase<?>> adder() {
            return (self, igtme, id) -> {
                IMetaTileEntity imte = igtme.getMetaTileEntity();

                if (imte instanceof MTEHatchBEC hatch) {
                    hatch.updateTexture(id);
                    hatch.updateCraftingIcon(self.getMachineCraftingIcon());
                    self.mBECHatches.add(hatch);
                    hatch.setController(self);
                    return true;
                } else {
                    return false;
                }
            };
        }
    }
}
