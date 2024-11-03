package gregtech.api.factory.test;

import static gregtech.api.enums.Dyes.MACHINE_METAL;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import gregtech.api.enums.Dyes;
import gregtech.api.enums.Textures.BlockIcons.CustomIcon;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.render.TextureFactory;
import it.unimi.dsi.fastutil.Pair;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import tectech.util.CommonValues;
import tectech.util.TTUtility;

public class TestFactoryHatch extends MTEHatch implements TestFactoryElement {

    public static final CustomIcon EM_D_ACTIVE = new CustomIcon("iconsets/OVERLAY_EM_D_ACTIVE");
    public static final CustomIcon EM_D_SIDES = new CustomIcon("iconsets/OVERLAY_EM_D_SIDES");
    public static final CustomIcon EM_D_CONN = new CustomIcon("iconsets/EM_DATA_CONN");

    protected TestFactoryHatch(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 0, aDescription, aTextures);
    }

    public TestFactoryHatch(int aID, String aName, String aNameRegional, int aTier) {
        super(
            aID,
            aName,
            aNameRegional,
            aTier,
            0,
            new String[] {
                CommonValues.TEC_MARK_EM,
                StatCollector.translateToLocal("gt.blockmachines.hatch.datain.desc.0"),
                StatCollector.translateToLocal("gt.blockmachines.hatch.datain.desc.1"),
                EnumChatFormatting.AQUA + StatCollector.translateToLocal("gt.blockmachines.hatch.datain.desc.2")
            });
        TTUtility.setTier(aTier, this);
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new TestFactoryHatch(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return new ITexture[] {
            aBaseTexture,
            TextureFactory.builder()
                .addIcon(EM_D_ACTIVE)
                .setRGBA(Dyes.getModulation(getBaseMetaTileEntity().getColorization(), MACHINE_METAL.getRGBA()))
                .build(),
            TextureFactory.of(EM_D_CONN)
        };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] {
            aBaseTexture,
            TextureFactory.builder()
                .addIcon(EM_D_SIDES)
                .setRGBA(Dyes.getModulation(getBaseMetaTileEntity().getColorization(), MACHINE_METAL.getRGBA()))
                .build(),
            TextureFactory.of(EM_D_CONN)
        };
    }

    @Override
    public boolean isFacingValid(ForgeDirection facing) {
        return true;
    }

    @Override
    public boolean isAccessAllowed(EntityPlayer aPlayer) {
        return true;
    }

    @Override
    public boolean isLiquidInput(ForgeDirection side) {
        return false;
    }

    @Override
    public boolean isFluidInputAllowed(FluidStack aFluid) {
        return false;
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return false;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return false;
    }

    @Override
    public boolean isValidSlot(int aIndex) {
        return false;
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
        currenttip.add("Network: " + accessor.getNBTData().getString("network"));
    }

    @Override
    public boolean isGivingInformation() {
        return true;
    }

    @Override
    public String[] getInfoData() {
        List<String> data = new ArrayList<>(Arrays.asList(super.getInfoData()));

        if (network == null) {
            data.add("No network");
        } else {
            for (TestFactoryHatch hatch : network.getComponents(TestFactoryHatch.class)) {
                IGregTechTileEntity base = hatch.getBaseMetaTileEntity();

                data.add(base.getXCoord() + ", " + base.getYCoord() + ", " + base.getZCoord() + ": " + hatch.toString());
            }
        }

        return data.toArray(new String[data.size()]);
    }

    @Override
    public List<Pair<Class<?>, Object>> getComponents() {
        return Collections.singletonList(Pair.of(TestFactoryHatch.class, this));
    }

    @Override
    public boolean canConnectOnSide(ForgeDirection side) {
        return side == getBaseMetaTileEntity().getFrontFacing();
    }

    @Override
    public void updateConnections() {
        
    }

    private WeakReference<TestFactoryElement> oldNeighbour;

    @Override
    public void getNeighbours(Collection<TestFactoryElement> neighbours) {
        TestFactoryElement old;
        if (oldNeighbour != null && (old = oldNeighbour.get()) != null) {
            old.updateConnections();
        }

        IGregTechTileEntity base = getBaseMetaTileEntity();

        if (base.getTileEntityAtSide(base.getFrontFacing()) instanceof IGregTechTileEntity igte && igte.getColorization() == base.getColorization() && igte.getMetaTileEntity() instanceof TestFactoryElement element) {
            neighbours.add(element);
            element.updateConnections();
            oldNeighbour = new WeakReference<>(element);
        } else {
            oldNeighbour = null;
        }
    }

    private TestFactoryNetwork network;

    @Override
    public TestFactoryNetwork getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(TestFactoryNetwork network) {
        this.network = network;
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);

        TestFactoryGrid.INSTANCE.addElement(this);
    }

    @Override
    public void onRemoval() {
        super.onRemoval();

        TestFactoryGrid.INSTANCE.removeElement(this);
    }

    @Override
    public void onFacingChange() {
        super.onFacingChange();

        TestFactoryGrid.INSTANCE.addElement(this);
    }

    @Override
    public void onColorChangeServer(byte aColor) {
        TestFactoryGrid.INSTANCE.addElement(this);
    }
}
