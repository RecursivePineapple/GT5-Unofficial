package tectech.thing.metaTileEntity.hatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
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

public class MTEHatchBEC extends MTEBaseFactoryHatch implements BECFactoryElement {

    private BECFactoryNetwork network;

    private BECFactoryElement controller;

    protected MTEHatchBEC(MTEHatchBEC prototype) {
        super(prototype);
    }

    public MTEHatchBEC(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, null);
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity igte) {
        return new MTEHatchBEC(this);
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
    public BECFactoryElement.ConnectionType getConnectionOnSide(ForgeDirection side) {
        return side == getBaseMetaTileEntity().getFrontFacing()
            ? BECFactoryElement.ConnectionType.CONNECTED
            : BECFactoryElement.ConnectionType.NONE;
    }

    @Override
    public void getNeighbours(Collection<BECFactoryElement> neighbours) {
        IGregTechTileEntity base = getBaseMetaTileEntity();

        if (base == null || base.isDead()) return;

        if (base.getTileEntityAtSide(base.getFrontFacing()) instanceof IGregTechTileEntity igte) {
            if (igte.getColorization() == base.getColorization()) {
                if (igte.getMetaTileEntity() instanceof BECFactoryElement element) {
                    neighbours.add(element);
                }
            }
        }

        if (controller != null && controller.getNetwork() != null) {
            neighbours.add(controller);
        }
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
    public void onFacingChange() {
        super.onFacingChange();

        BECFactoryGrid.INSTANCE.addElement(this);
    }

    @Override
    public void onColorChangeServer(byte aColor) {
        BECFactoryGrid.INSTANCE.addElement(this);
    }

    public void setController(BECFactoryElement controller) {
        if (controller != this.controller) {
            this.controller = controller;
            BECFactoryGrid.INSTANCE.addElement(this);
        }
    }
}
