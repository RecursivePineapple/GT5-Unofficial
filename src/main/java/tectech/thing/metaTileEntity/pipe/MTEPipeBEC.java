package tectech.thing.metaTileEntity.pipe;

import static gregtech.api.enums.Dyes.MACHINE_METAL;
import static net.minecraft.util.StatCollector.translateToLocal;

import org.jetbrains.annotations.Nullable;

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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import tectech.TecTech;
import tectech.loader.NetworkDispatcher;
import tectech.mechanics.pipe.IActivePipe;
import tectech.mechanics.pipe.IConnectsToBECPipe;
import tectech.mechanics.pipe.PipeActivityMessage;
import tectech.util.CommonValues;

/**
 * Created by Tec on 26.02.2017.
 */
public class MTEPipeBEC extends MetaPipeEntity implements IConnectsToBECPipe, IActivePipe {

    private static final IIconContainer EM_PIPE = new CustomIcon("iconsets/EM_DATA");
    private static final IIconContainer EM_BAR = new CustomIcon("iconsets/EM_BAR");
    private static final IIconContainer EM_BAR_ACTIVE = new CustomIcon("iconsets/EM_BAR_ACTIVE");

    private boolean mIsActive;

    public MTEPipeBEC(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 0);
    }

    public MTEPipeBEC(String aName) {
        super(aName, 0);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MTEPipeBEC(mName);
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
        return new String[] { CommonValues.TEC_MARK_EM, translateToLocal("gt.blockmachines.pipe.datastream.desc.0"), // Advanced
                                                                                                                     // data
                                                                                                                     // transmission
            EnumChatFormatting.AQUA.toString() + EnumChatFormatting.BOLD
                + translateToLocal("gt.blockmachines.pipe.datastream.desc.1"), // Don't stare at the beam!
            EnumChatFormatting.AQUA + translateToLocal("gt.blockmachines.pipe.datastream.desc.2"), // Must be
                                                                                                   // painted to
                                                                                                   // work
            EnumChatFormatting.AQUA + translateToLocal("gt.blockmachines.pipe.datastream.desc.3") // Do not cross or
                                                                                                  // split
        };
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        onPostTick(aBaseMetaTileEntity, 31);
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (aBaseMetaTileEntity.isServerSide()) {
            if ((aTick & 31) == 15) {
                if (TecTech.RANDOM.nextInt(15) == 0) {
                    NetworkDispatcher.INSTANCE.sendToAllAround(
                        new PipeActivityMessage.PipeActivityData(this),
                        aBaseMetaTileEntity.getWorld().provider.dimensionId,
                        aBaseMetaTileEntity.getXCoord(),
                        aBaseMetaTileEntity.getYCoord(),
                        aBaseMetaTileEntity.getZCoord(),
                        256);
                }

                if (getActive()) {
                    setActive(false);
                }
                
                updateConnections(aBaseMetaTileEntity);
            }
        } else if (aBaseMetaTileEntity.isClientSide() && GTClient.changeDetected == 4) {
            aBaseMetaTileEntity.issueTextureUpdate();
        }
    }

    private void updateConnections(IGregTechTileEntity aBaseMetaTileEntity) {
        mConnections = 0;

        byte myColor = aBaseMetaTileEntity.getColorization();

        if (aBaseMetaTileEntity.getColorization() < 0) {
            return;
        }

        for (final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            IConnectsToBECPipe connection = getBECConnection(aBaseMetaTileEntity.getTileEntityAtSide(side));

            if (connection != null && connection.getConnectionOnSide(side.getOpposite(), myColor) != CONNECTION_NONE) {
                mConnections |= side.flag;
            }
        }
    }

    @Override
    public @Nullable IConnectsToBECPipe getNext(IConnectsToBECPipe source) {
        if (Integer.bitCount(mConnections) != 2) {
            return null;
        }

        IGregTechTileEntity base = getBaseMetaTileEntity();
        byte myColor = base.getColorization();

        for (final ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            if ((mConnections & side.flag) == 0) {
                continue;
            }

            IConnectsToBECPipe connection = getBECConnection(base.getTileEntityAtSide(side));

            if (connection != null && connection != source) {
                if ((connection.getConnectionOnSide(side.getOpposite(), myColor) & CONNECTION_INPUT) != 0) {
                    connection.markUsed();
                    return connection;
                }
            }
        }

        return null;
    }

    public static @Nullable IConnectsToBECPipe getBECConnection(TileEntity tile) {
        if (tile instanceof IConnectsToBECPipe becConnection) {
            return becConnection;
        } else if (tile instanceof IGregTechTileEntity igte && igte.getMetaTileEntity() instanceof IConnectsToBECPipe becConnection) {
            return becConnection;
        }
        
        return null;
    }

    @Override
    public int getConnectionOnSide(ForgeDirection side, byte colorization) {
        return colorization == getBaseMetaTileEntity().getColorization() ? CONNECTION_PIPE : CONNECTION_NONE;
    }

    @Override
    public void markUsed() {
        setActive(true);
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
    public boolean isGivingInformation() {
        return true;
    }

    @Override
    public String[] getInfoData() {
        return new String[] {
            getActive() ? "Active." : "Not active."
        };
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
}
