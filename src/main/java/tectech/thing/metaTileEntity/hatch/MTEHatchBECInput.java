package tectech.thing.metaTileEntity.hatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import gregtech.api.factory.test.TestFactoryHatch;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import it.unimi.dsi.fastutil.Pair;
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

public class MTEHatchBECInput extends MTEBaseFactoryHatch implements BECFactoryElement {

    private BECFactoryNetwork network;
    protected MTEHatchBECInput(MTEHatchBECInput prototype) {
        super(prototype);
    }

    public MTEHatchBECInput(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, null);
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity igte) {
        return new MTEHatchBECInput(this);
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
                IGregTechTileEntity base = inv.getBaseMetaTileEntity();

                data.add(base.getXCoord() + ", " + base.getYCoord() + ", " + base.getZCoord() + ": " + inv.getContents().toString());
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
    public void getNeighbours(Collection<BECFactoryElement> neighbours) {
        IGregTechTileEntity base = getBaseMetaTileEntity();

        if (base.getTileEntityAtSide(base.getFrontFacing()) instanceof IGregTechTileEntity igte) {
            if (igte.getColorization() == base.getColorization()) {
                if (igte.getMetaTileEntity() instanceof BECFactoryElement element) {
                    neighbours.add(element);
                }
            }
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
}
