package tectech.thing.metaTileEntity.pipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.common.covers.CoverInfo;
import gregtech.common.covers.CoverShutter;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryElement;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryGrid;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryNetwork;
import tectech.mechanics.boseEinsteinCondensate.BECInventory;

public class MTEPipeBEC extends MTEBaseFactoryPipe implements BECFactoryElement {

    private BECFactoryNetwork network;

    public MTEPipeBEC(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        mThickness = 3f / 4f;
    }

    public MTEPipeBEC(MTEPipeBEC prototype) {
        super(prototype);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MTEPipeBEC(this);
    }

    private boolean wasAllowedToWork = false;

    @Override
    public void onPostTick(IGregTechTileEntity base, long aTick) {
        super.onPostTick(base, aTick);

        if (base.isAllowedToWork() != wasAllowedToWork) {
            wasAllowedToWork = base.isAllowedToWork();

            boolean hasShutter = false;

            for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                if (base.getCoverBehaviorAtSideNew(side) instanceof CoverShutter) {
                    hasShutter = true;
                    break;
                }
            }

            if (hasShutter) {
                BECFactoryGrid.INSTANCE.addElement(this);
            }
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound nbtTagCompound) {
        super.loadNBTData(nbtTagCompound);
        wasAllowedToWork = nbtTagCompound.getBoolean("wasAllowedToWork");
    }

    @Override
    public void saveNBTData(NBTTagCompound nbtTagCompound) {
        super.saveNBTData(nbtTagCompound);
        nbtTagCompound.setBoolean("wasAllowedToWork", wasAllowedToWork);
    }

    @Override
    protected void checkActive() {
        mIsActive = network != null && network.getComponents(BECInventory.class).size() > 0;
    }

    @Override
    public ConnectionType getConnectionOnSide(ForgeDirection side) {
        CoverInfo cover = getBaseMetaTileEntity().getCoverInfoAtSide(side);

        if (cover != null && cover.getCoverBehavior() instanceof CoverShutter shutter) {
            if (shutter.letsEnergyIn(side, cover.getCoverID(), cover.getCoverData(), getBaseMetaTileEntity())) {
                return ConnectionType.CONNECTED;
            }

            if (shutter.alwaysLookConnected(side, cover.getCoverID(), cover.getCoverData(), getBaseMetaTileEntity())) {
                return ConnectionType.VISUAL_ONLY;
            }

            return ConnectionType.NONE;
        } else {
            return ConnectionType.CONNECTED;
        }
    }

    @Override
    public void getNeighbours(Collection<BECFactoryElement> neighbours) {
        IGregTechTileEntity base = getBaseMetaTileEntity();

        if (base == null || base.isDead()) return;

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (base.getTileEntityAtSide(dir) instanceof IGregTechTileEntity igte) {
                if (igte.getColorization() == base.getColorization()) {
                    if (igte.getMetaTileEntity() instanceof BECFactoryElement element) {
                        if (this.getConnectionOnSide(dir) == ConnectionType.CONNECTED) {
                            if (element.getConnectionOnSide(dir.getOpposite()) == ConnectionType.CONNECTED) {
                                neighbours.add(element);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onAdjacentBlockChange(int x, int y, int z) {
        mCheckConnections = true;
    }

    @Override
    public void onNeighbourChanged(BECFactoryElement neighbour) {
        mCheckConnections = true;
    }

    @Override
    protected void checkConnections() {
        mConnections = 0;

        IGregTechTileEntity base = getBaseMetaTileEntity();

        if (base == null || base.isDead()) return;

        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            if (base.getTileEntityAtSide(side) instanceof IGregTechTileEntity igte) {
                if (igte.getColorization() == base.getColorization()) {
                    if (igte.getMetaTileEntity() instanceof BECFactoryElement element) {
                        if (element.getConnectionOnSide(side.getOpposite()) != ConnectionType.NONE) {
                            mConnections |= side.flag;
                        }
                    }
                }
            }
        }
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
            for (BECInventory inv : network.getComponents(BECInventory.class)) {
                data.add(inv.getContents().toString());
            }
        }

        return data.toArray(new String[data.size()]);
    }

    @Override
    public BECFactoryNetwork getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(BECFactoryNetwork network) {
        this.network = network;
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);

        BECFactoryGrid.INSTANCE.addElement(this);
    }

    @Override
    public void onRemoval() {
        super.onRemoval();

        BECFactoryGrid.INSTANCE.removeElement(this);
    }

    @Override
    public void onColorChangeServer(byte aColor) {
        BECFactoryGrid.INSTANCE.addElement(this);
    }
}
