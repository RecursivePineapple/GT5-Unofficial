package tectech.thing.metaTileEntity.multi.base;

import static gregtech.api.casing.Casings.MolecularCasing;

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
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import gregtech.api.enums.StructureError;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.render.TextureFactory;
import gregtech.api.structure.IStructureInstance;
import gregtech.api.structure.IStructureProvider;
import gregtech.api.structure.StructureWrapper;
import gregtech.api.structure.StructureWrapperInstanceInfo;
import gregtech.api.util.IGTHatchAdder;
import gtPlusPlus.xmod.gregtech.common.blocks.textures.TexturesGtBlock;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryElement;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryNetwork;
import tectech.thing.CustomItemList;
import tectech.thing.metaTileEntity.hatch.MTEHatchBEC;

public abstract class MTEBECMultiblockBase<TSelf extends MTEBECMultiblockBase<TSelf>> extends TTMultiblockBase
    implements ISurvivalConstructable, BECFactoryElement, IStructureProvider<TSelf> {

    protected static final String STRUCTURE_PIECE_MAIN = "main";

    protected final List<BECFactoryElement> mBECHatches = new ArrayList<>();

    protected BECFactoryNetwork network;

    protected final StructureWrapper<TSelf> structure;
    protected final StructureWrapperInstanceInfo<TSelf> structureInstanceInfo;

    public MTEBECMultiblockBase(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);

        structure = new StructureWrapper<>(this);
        structureInstanceInfo = null;

        structure.loadStructure();
    }

    protected MTEBECMultiblockBase(TSelf base) {
        super(base.mName);

        structure = base.structure;
        structureInstanceInfo = new StructureWrapperInstanceInfo<>(structure);
    }

    @Override
    public boolean shouldCheckMaintenance() {
        return false;
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
                textures.add(
                    TextureFactory.builder()
                        .addIcon(getActiveTextureGlow())
                        .extFacing()
                        .glow()
                        .build());
            } else {
                textures.add(
                    TextureFactory.builder()
                        .addIcon(getInactiveTexture())
                        .extFacing()
                        .build());
                textures.add(
                    TextureFactory.builder()
                        .addIcon(getInactiveTextureGlow())
                        .extFacing()
                        .glow()
                        .build());
            }
        }

        return textures.toArray(new ITexture[0]);
    }

    protected ITexture getCasingTexture() {
        return MolecularCasing.getCasingTexture();
    }

    protected IIconContainer getActiveTexture() {
        return TexturesGtBlock.oMCAAdvancedEBFActive;
    }

    protected IIconContainer getActiveTextureGlow() {
        return TexturesGtBlock.oMCAAdvancedEBFActiveGlow;
    }

    protected IIconContainer getInactiveTexture() {
        return TexturesGtBlock.oMCAAdvancedEBF;
    }

    protected IIconContainer getInactiveTextureGlow() {
        return TexturesGtBlock.oMCAAdvancedEBFGlow;
    }

    @Override
    public IStructureDefinition<? extends TTMultiblockBase> getStructure_EM() {
        return structure.structureDefinition;
    }

    @Override
    public IStructureInstance getStructureInstance() {
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
        structure.construct((TSelf) this, stackSize, hintsOnly);
    }

    @Override
    @SuppressWarnings("unchecked")
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        return structure.survivalConstruct((TSelf) this, stackSize, elementBudget, env);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean checkMachine_EM(IGregTechTileEntity iGregTechTileEntity, ItemStack itemStack) {
        return structure.checkStructure((TSelf) this);
    }

    private String errorMessage;

    @Override
    protected void validateStructure(Collection<StructureError> errors, NBTTagCompound context) {
        structureInstanceInfo.validate(errors, context);
    }

    @Override
    protected void localizeStructureErrors(Collection<StructureError> errors, NBTTagCompound context,
        List<String> lines) {
        structureInstanceInfo.localizeStructureErrors(errors, context, lines);
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

        if (igte == null || igte.isDead()) return FMLCommonHandler.instance()
            .getSide() == Side.SERVER;

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
                case Hatch -> CustomItemList.becConnectorHatch.getDisplayName();
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
