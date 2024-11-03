package tectech.thing.metaTileEntity.pipe;

import static gregtech.api.enums.Dyes.MACHINE_METAL;
import static gregtech.api.util.GTRecipeBuilder.SECONDS;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.GTMod;
import gregtech.api.enums.Dyes;
import gregtech.api.enums.Textures.BlockIcons.CustomIcon;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaPipeEntity;
import gregtech.api.metatileentity.MetaPipeEntity;
import gregtech.api.render.TextureFactory;
import gregtech.common.GTClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import tectech.mechanics.pipe.IActivePipe;
import tectech.mechanics.pipe.PipeActivity;

public abstract class MTEBaseFactoryPipe extends MetaPipeEntity implements IActivePipe {
    
    public static final IIconContainer EM_PIPE = new CustomIcon("iconsets/EM_DATA");
    public static final IIconContainer EM_BAR = new CustomIcon("iconsets/EM_BAR");
    public static final IIconContainer EM_BAR_ACTIVE = new CustomIcon("iconsets/EM_BAR_ACTIVE");

    protected boolean mIsActive;

    protected float mThickness = 0.5f;

    public MTEBaseFactoryPipe(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 0);
    }

    public MTEBaseFactoryPipe(String aName) {
        super(aName, 0);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity base, ForgeDirection side, int aConnections,
        int colorIndex, boolean aConnected, boolean aRedstone) {
        return new ITexture[] {
            TextureFactory.of(EM_PIPE),
            TextureFactory.builder()
                .addIcon((getActive() && ((BaseMetaPipeEntity)base).mTickTimer % 20 >= 10) ? EM_BAR_ACTIVE : EM_BAR)
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
        return new String[] { };
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

        if ((mConnections & 1 << ForgeDirection.DOWN.ordinal()) != 0) {
            tSide0 = 0f;
        }
        if ((mConnections & 1 << ForgeDirection.UP.ordinal()) != 0) {
            tSide1 = 1f;
        }
        if ((mConnections & 1 << ForgeDirection.NORTH.ordinal()) != 0) {
            tSide2 = 0f;
        }
        if ((mConnections & 1 << ForgeDirection.SOUTH.ordinal()) != 0) {
            tSide3 = 1f;
        }
        if ((mConnections & 1 << ForgeDirection.WEST.ordinal()) != 0) {
            tSide4 = 0f;
        }
        if ((mConnections & 1 << ForgeDirection.EAST.ordinal()) != 0) {
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
        return mThickness;
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

    private boolean prevActivity;

    @Override
    public void onFirstTick(IGregTechTileEntity base) {
        super.onFirstTick(base);
        onPostTick(base, 31);
    }

    @Override
    public void onPostTick(IGregTechTileEntity base, long aTick) {
        super.onPostTick(base, aTick);

        if (base.isServerSide()) {
            if (mCheckConnections && base.isServerSide()) {
                mCheckConnections = false;
                checkConnections();
            }

            if (aTick % SECONDS == 0) {
                checkActive();

                boolean isActive = getActive();

                if (isActive != prevActivity || aTick % (60 * SECONDS) == 0) {
                    prevActivity = isActive;

                    PipeActivity.enqueueUpdate(base.getWorld(), base.getXCoord(), base.getYCoord(), base.getZCoord(), isActive);
                }
            }
        } else {
            if (GTClient.changeDetected == 4) {
                base.issueTextureUpdate();
            }
        }
    }

    @SideOnly(Side.SERVER)
    protected void checkActive() {
        mIsActive = false;
    }

    @Override
    protected void checkConnections() {

    }

    @Override
    public boolean isGivingInformation() {
        return true;
    }

    @Override
    public String[] getInfoData() {
        return new String[] {
            getActive() ? EnumChatFormatting.GREEN + "Active." : EnumChatFormatting.RED + "Not active."
        };
    }
}
