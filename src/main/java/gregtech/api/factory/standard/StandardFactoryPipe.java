package gregtech.api.factory.standard;

import static gregtech.api.enums.Dyes.MACHINE_METAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import gregtech.GTMod;
import gregtech.api.enums.Dyes;
import gregtech.api.enums.Textures.BlockIcons.CustomIcon;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaPipeEntity;
import gregtech.api.metatileentity.MetaPipeEntity;
import gregtech.api.render.TextureFactory;
import gregtech.common.GTClient;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import tectech.mechanics.pipe.IActivePipe;
import tectech.util.CommonValues;

public class StandardFactoryPipe extends MetaPipeEntity implements IActivePipe {
    
    private static final IIconContainer EM_PIPE = new CustomIcon("iconsets/EM_DATA");
    private static final IIconContainer EM_BAR = new CustomIcon("iconsets/EM_BAR");
    private static final IIconContainer EM_BAR_ACTIVE = new CustomIcon("iconsets/EM_BAR_ACTIVE");

    private boolean mIsActive;

    public StandardFactoryPipe(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 0);
    }

    public StandardFactoryPipe(String aName) {
        super(aName, 0);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new StandardFactoryPipe(mName);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, int aConnections,
        int colorIndex, boolean aConnected, boolean aRedstone) {
        return new ITexture[] {
            TextureFactory.of(EM_PIPE),
            TextureFactory.builder()
                .addIcon(getActive() ? EM_BAR_ACTIVE : EM_BAR)
                .setRGBA(Dyes.getModulation(colorIndex, MACHINE_METAL.getRGBA()))
                .build(),
        };
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity iGregTechTileEntity, int i, ForgeDirection side,
        ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity iGregTechTileEntity, int i, ForgeDirection side,
        ItemStack itemStack) {
        return false;
    }

    @Override
    public void loadNBTData(NBTTagCompound nbtTagCompound) {
        setActive(nbtTagCompound.getBoolean("eActive"));
        mConnections = nbtTagCompound.getByte("mConnections");
    }

    @Override
    public void saveNBTData(NBTTagCompound nbtTagCompound) {
        nbtTagCompound.setBoolean("eActive", getActive());
        nbtTagCompound.setByte("mConnections", mConnections);
    }

    @Override
    public boolean renderInside(ForgeDirection side) {
        return false;
    }

    @Override
    public byte getTileEntityBaseType() {
        return 4;
    }

    @Override
    public String[] getDescription() {
        return new String[] { CommonValues.TEC_MARK_EM, StatCollector.translateToLocal("gt.blockmachines.pipe.datastream.desc.0"), // Advanced
                                                                                                                     // data
                                                                                                                     // transmission
            EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD
                + StatCollector.translateToLocal("gt.blockmachines.pipe.datastream.desc.1"), // Don't stare at the beam!
            EnumChatFormatting.AQUA + StatCollector.translateToLocal("gt.blockmachines.pipe.datastream.desc.2"), // Must be
                                                                                                   // painted to
                                                                                                   // work
            EnumChatFormatting.AQUA + StatCollector.translateToLocal("gt.blockmachines.pipe.datastream.desc.3") // Do not cross or
                                                                                                  // split
        };
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World aWorld, int aX, int aY, int aZ) {
        float tSpace = (1f - getThickNess()) / 2;
        float tSide0 = tSpace;
        float tSide1 = 1f - tSpace;
        float tSide2 = tSpace;
        float tSide3 = 1f - tSpace;
        float tSide4 = tSpace;
        float tSide5 = 1f - tSpace;

        if (getBaseMetaTileEntity().getCoverIDAtSide(ForgeDirection.DOWN) != 0) {
            tSide0 = tSide2 = tSide4 = 0;
            tSide3 = tSide5 = 1;
        }
        if (getBaseMetaTileEntity().getCoverIDAtSide(ForgeDirection.UP) != 0) {
            tSide2 = tSide4 = 0;
            tSide1 = tSide3 = tSide5 = 1;
        }
        if (getBaseMetaTileEntity().getCoverIDAtSide(ForgeDirection.NORTH) != 0) {
            tSide0 = tSide2 = tSide4 = 0;
            tSide1 = tSide5 = 1;
        }
        if (getBaseMetaTileEntity().getCoverIDAtSide(ForgeDirection.SOUTH) != 0) {
            tSide0 = tSide4 = 0;
            tSide1 = tSide3 = tSide5 = 1;
        }
        if (getBaseMetaTileEntity().getCoverIDAtSide(ForgeDirection.WEST) != 0) {
            tSide0 = tSide2 = tSide4 = 0;
            tSide1 = tSide3 = 1;
        }
        if (getBaseMetaTileEntity().getCoverIDAtSide(ForgeDirection.EAST) != 0) {
            tSide0 = tSide2 = 0;
            tSide1 = tSide3 = tSide5 = 1;
        }

        byte tConn = ((BaseMetaPipeEntity) getBaseMetaTileEntity()).mConnections;
        if ((tConn & 1 << ForgeDirection.DOWN.ordinal()) != 0) {
            tSide0 = 0f;
        }
        if ((tConn & 1 << ForgeDirection.UP.ordinal()) != 0) {
            tSide1 = 1f;
        }
        if ((tConn & 1 << ForgeDirection.NORTH.ordinal()) != 0) {
            tSide2 = 0f;
        }
        if ((tConn & 1 << ForgeDirection.SOUTH.ordinal()) != 0) {
            tSide3 = 1f;
        }
        if ((tConn & 1 << ForgeDirection.WEST.ordinal()) != 0) {
            tSide4 = 0f;
        }
        if ((tConn & 1 << ForgeDirection.EAST.ordinal()) != 0) {
            tSide5 = 1f;
        }

        return AxisAlignedBB
            .getBoundingBox(aX + tSide4, aY + tSide0, aZ + tSide2, aX + tSide5, aY + tSide1, aZ + tSide3);
    }

    @Override
    public float getThickNess() {
        if (GTMod.instance.isClientSide() && GTClient.hideValue == 1) {
            return 0.0625F;
        }
        return 6.0f / 8.0f;
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
    public void markUsed() {
        setActive(true);
    }

    @Override
    public void setActive(boolean state) {
        if (state != mIsActive) {
            mIsActive = state;
            getBaseMetaTileEntity().issueTextureUpdate();
        }
    }

    @Override
    public boolean getActive() {
        return mIsActive;
    }

    public boolean canConnectOnSide(ForgeDirection side) {
        return true;
    }

    public void getNeighbours(Collection<TestFactoryElement> neighbours) {
        IGregTechTileEntity base = getBaseMetaTileEntity();

        if (base == null || base.isDead()) return;

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (base.getTileEntityAtSide(dir) instanceof IGregTechTileEntity igte) {
                if (igte.getColorization() == base.getColorization()) {
                    if (igte.getMetaTileEntity() instanceof TestFactoryElement element) {
                        if (element.canConnectOnSide(dir.getOpposite())) {
                            neighbours.add(element);
                        }
                    }
                }
            }
        }
    }

    public void onNeighbourUpdated() {
        mCheckConnections = true;
    }

    public void onNeighbourRemoved() {
        mCheckConnections = true;
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
        if (mCheckConnections && aBaseMetaTileEntity.isServerSide()) {
            mCheckConnections = false;
            checkConnections();
        }
    }

    @Override
    protected void checkConnections() {
        mConnections = 0;

        IGregTechTileEntity base = getBaseMetaTileEntity();

        if (base == null || base.isDead()) return;

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (base.getTileEntityAtSide(dir) instanceof IGregTechTileEntity igte) {
                if (igte.getColorization() == base.getColorization()) {
                    if (igte.getMetaTileEntity() instanceof TestFactoryElement element) {
                        if (element.canConnectOnSide(dir.getOpposite())) {
                            mConnections |= dir.flag;
                        }
                    }
                }
            }
        }
        
        if (network != null) network.updateNeighbours(this);
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
    public void onColorChangeServer(byte aColor) {
        TestFactoryGrid.INSTANCE.addElement(this);
    }
}
