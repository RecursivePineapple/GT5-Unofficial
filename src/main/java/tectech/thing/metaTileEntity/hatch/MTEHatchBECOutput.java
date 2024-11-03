package tectech.thing.metaTileEntity.hatch;

import static gregtech.api.enums.Dyes.MACHINE_METAL;
import static gregtech.api.enums.GTValues.L;
import static gregtech.api.enums.GTValues.M;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.enums.Dyes;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Textures.BlockIcons.CustomIcon;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.objects.MaterialStack;
import gregtech.api.render.TextureFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import tectech.mechanics.boseEinsteinCondensate.BECInventory;
import tectech.mechanics.boseEinsteinCondensate.IBECSink;
import tectech.mechanics.pipe.IConnectsToBECPipe;
import tectech.thing.metaTileEntity.pipe.MTEPipeBEC;
import tectech.util.CommonValues;
import tectech.util.TTUtility;

public class MTEHatchBECOutput extends MTEHatch implements IConnectsToBECPipe, BECInventory {
    
    public static final CustomIcon EM_D_ACTIVE = new CustomIcon("iconsets/OVERLAY_EM_D_ACTIVE");
    public static final CustomIcon EM_D_SIDES = new CustomIcon("iconsets/OVERLAY_EM_D_SIDES");
    public static final CustomIcon EM_D_CONN = new CustomIcon("iconsets/EM_DATA_CONN");

    private IBECSink mSink;

    private final List<MaterialStack> contents = new ArrayList<>();

    protected MTEHatchBECOutput(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 0, aDescription, aTextures);
    }

    public MTEHatchBECOutput(int aID, String aName, String aNameRegional, int aTier) {
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
        return new MTEHatchBECOutput(mName, mTier, mDescriptionArray, mTextures);
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
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);

        if (aBaseMetaTileEntity.isServerSide()) {
            if ((aTick & 31) == 31) {
                IBECSink sink = findEnd();

                if (sink != mSink) {
                    if (mSink != null) {
                        mSink.setSource(null);
                    }

                    mSink = sink;

                    if (mSink != null) {
                        mSink.setSource(this);
                        mSink.onSourceInventoryChanged();
                    }

                    aBaseMetaTileEntity.setActive(mSink != null);
                }
            }
        }
    }

    private IBECSink findEnd() {
        IConnectsToBECPipe current = this, prev = null, next;
        int range = 0;

        while ((next = current.getNext(prev)) != null && range++ < 128) {
            if (next instanceof IBECSink sink) {
                return sink;
            }

            prev = current;
            current = next;
        }

        return null;
    }

    //#region IConnectsToBECPipe impl

    @Override
    public @Nullable IConnectsToBECPipe getNext(@NotNull IConnectsToBECPipe source) {
        IGregTechTileEntity base = getBaseMetaTileEntity();

        IConnectsToBECPipe next = MTEPipeBEC.getBECConnection(base.getTileEntityAtSide(base.getFrontFacing()));

        if (next != null && (next.getConnectionOnSide(base.getFrontFacing().getOpposite(), base.getColorization()) & CONNECTION_INPUT) != 0) {
            next.markUsed();
            return next;
        } else {
            return null;
        }
    }

    @Override
    public int getConnectionOnSide(ForgeDirection side, byte colorization) {
        return colorization == getBaseMetaTileEntity().getColorization() && side == getBaseMetaTileEntity().getFrontFacing() ? CONNECTION_OUTPUT : CONNECTION_NONE;
    }

    @Override
    public void markUsed() {
        // do nothing
    }

    @Override
    public boolean isConnectedTo(BECInventory other) {
        return other == mSink;
    }

    //#endregion

    //#region IBECInventory impl

    @Override
    public List<MaterialStack> getContents() {
        return Collections.unmodifiableList(contents);
    }

    @Override
    public void consumeCondensate(MaterialStack... stacks) {
        for (MaterialStack toConsume : stacks) {
            for (MaterialStack stored : contents) {
                if (stored.mMaterial == toConsume.mMaterial) {
                    long toRemove = Math.min(toConsume.mAmount, stored.mAmount);
                    stored.mAmount -= toRemove;
                }
            }

            contents.removeIf(s -> s.mAmount <= 0);
        }
    }

    @Override
    public void addCondensate(MaterialStack... stacks) {
        for (MaterialStack toInsert : stacks) {
            for (MaterialStack existing : contents) {
                if (toInsert.mMaterial == existing.mMaterial) {
                    existing.mAmount += toInsert.mAmount;
                    toInsert.mAmount = 0;
                }
            }

            if (toInsert.mAmount > 0) {
                contents.add(toInsert);
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return contents == null || contents.isEmpty();
    }

    //#endregion

    //#region Misc GT TE code

    @Override
    public boolean isGivingInformation() {
        return true;
    }

    @Override
    public String[] getInfoData() {
        List<String> data = new ArrayList<>();

        data.add(mSink != null ? "Connected." : "Not connected.");

        if (!contents.isEmpty()) {
            data.add("Contained Bose-Einstein Condensate:");
        }

        for(MaterialStack stack : contents) {
            data.add(String.format("%s: %,d L", stack.mMaterial.mLocalizedName, stack.mAmount * L / M));
        }

        return data.toArray(new String[0]);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);

        if (!contents.isEmpty()) {
            aNBT.setInteger("mContentCount", contents.size());

            for (int i = 0; i < contents.size(); i++) {
                aNBT.setString("mMatName." + i, contents.get(i).mMaterial.mName);
                aNBT.setLong("mMatAmount." + i, contents.get(i).mAmount);
            }
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);

        int count = aNBT.getInteger("mContentCount");

        contents.clear();

        if (count > 0) {
            for(int i = 0; i < count; i++) {
                Materials material = Materials.get(aNBT.getString("mMatName." + i));
                long amount = aNBT.getLong("mMatAmount." + i);

                if (material != null && material != Materials._NULL && amount > 0) {
                    contents.add(new MaterialStack(material, amount));
                }
            }
        }
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

    //#endregion
}
