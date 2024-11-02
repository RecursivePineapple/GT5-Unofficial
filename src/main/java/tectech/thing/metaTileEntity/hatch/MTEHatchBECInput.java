package tectech.thing.metaTileEntity.hatch;

import static gregtech.api.enums.Dyes.MACHINE_METAL;
import static gregtech.api.enums.GTValues.L;
import static gregtech.api.enums.GTValues.M;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.enums.Dyes;
import gregtech.api.enums.Textures.BlockIcons.CustomIcon;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.objects.MaterialStack;
import gregtech.api.render.TextureFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import tectech.mechanics.boseEinsteinCondensate.IBECInventory;
import tectech.mechanics.boseEinsteinCondensate.IBECSink;
import tectech.mechanics.pipe.IConnectsToBECPipe;
import tectech.util.CommonValues;
import tectech.util.TTUtility;

public class MTEHatchBECInput extends MTEHatch implements IBECSink {
    
    public static final CustomIcon EM_D_ACTIVE = new CustomIcon("iconsets/OVERLAY_EM_D_ACTIVE");
    public static final CustomIcon EM_D_SIDES = new CustomIcon("iconsets/OVERLAY_EM_D_SIDES");
    public static final CustomIcon EM_D_CONN = new CustomIcon("iconsets/EM_DATA_CONN");

    private IBECInventory mSource;

    protected MTEHatchBECInput(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 0, aDescription, aTextures);
    }

    public MTEHatchBECInput(int aID, String aName, String aNameRegional, int aTier) {
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
        return new MTEHatchBECInput(mName, mTier, mDescriptionArray, mTextures);
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
    public @Nullable IConnectsToBECPipe getNext(@NotNull IConnectsToBECPipe source) {
        return null;
    }

    @Override
    public int getConnectionOnSide(ForgeDirection side, byte colorization) {
        return colorization == getBaseMetaTileEntity().getColorization() && side == getBaseMetaTileEntity().getFrontFacing() ? CONNECTION_INPUT : CONNECTION_NONE;
    }

    @Override
    public void markUsed() {
        // do nothing
    }

    private boolean isConnected() {
        return mSource != null && mSource.isConnectedTo(this);
    }

    @Override
    public List<MaterialStack> getContents() {
        return isConnected() ? null : mSource.getContents();
    }

    @Override
    public boolean isEmpty() {
        return isConnected() ? false : mSource.isEmpty();
    }

    @Override
    public void consumeCondensate(MaterialStack... stacks) {
        if (isConnected()) {
            mSource.consumeCondensate(stacks);
        }
    }

    @Override
    public void addCondensate(MaterialStack... stacks) {
        if (isConnected()) {
            mSource.addCondensate(stacks);
        }
    }

    @Override
    public boolean isConnectedTo(IBECInventory other) {
        return other == mSource;
    }

    @Override
    public boolean isGivingInformation() {
        return true;
    }

    @Override
    public String[] getInfoData() {
        List<String> data = new ArrayList<>();

        if (isConnected()) {
            data.add("Connected.");

            List<MaterialStack> contents = mSource.getContents();

            if (!contents.isEmpty()) {
                data.add("Contained Bose-Einstein Condensate:");
            }

            for(MaterialStack stack : contents) {
                data.add(String.format("%s: %,d L", stack.mMaterial.mLocalizedName, stack.mAmount * L / M));
            }
        } else {
            data.add("Not connected.");
        }

        return data.toArray(new String[0]);
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);

        if (aBaseMetaTileEntity.isServerSide() && (aTick & 31) == 31) {
            getBaseMetaTileEntity().setActive(aBaseMetaTileEntity.isAllowedToWork() && mSource != null);
        }
    }

    @Override
    public void setSource(IBECInventory source) {
        mSource = source;
        getBaseMetaTileEntity().setActive(getBaseMetaTileEntity().isAllowedToWork() && mSource != null);
    }

    @Override
    public void onSourceInventoryChanged() {

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
}
