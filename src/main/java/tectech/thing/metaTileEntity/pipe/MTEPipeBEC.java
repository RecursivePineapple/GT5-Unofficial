package tectech.thing.metaTileEntity.pipe;

import java.util.Collection;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryElement;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryGrid;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryNetwork;
import tectech.mechanics.boseEinsteinCondensate.BECInventory;

public class MTEPipeBEC extends MTEBaseFactoryPipe implements BECFactoryElement {

    private BECFactoryNetwork network;

    public MTEPipeBEC(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public MTEPipeBEC(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MTEPipeBEC(mName);
    }

    @Override
    protected void checkActive() {
        mIsActive = network.getComponents(BECInventory.class).size() > 0;
    }

    @Override
    public boolean canConnectOnSide(ForgeDirection side) {
        return true;
    }

    @Override
    public void getNeighbours(Collection<BECFactoryElement> neighbours) {
        IGregTechTileEntity base = getBaseMetaTileEntity();

        if (base == null || base.isDead()) return;

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (base.getTileEntityAtSide(dir) instanceof IGregTechTileEntity igte) {
                if (igte.getColorization() == base.getColorization()) {
                    if (igte.getMetaTileEntity() instanceof BECFactoryElement element) {
                        if (element.canConnectOnSide(dir.getOpposite())) {
                            neighbours.add(element);
                        }
                    }
                }
            }
        }
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

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (base.getTileEntityAtSide(dir) instanceof IGregTechTileEntity igte) {
                if (igte.getColorization() == base.getColorization()) {
                    if (igte.getMetaTileEntity() instanceof BECFactoryElement element) {
                        if (element.canConnectOnSide(dir.getOpposite())) {
                            mConnections |= dir.flag;
                        }
                    }
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
    public void onColorChangeServer(byte aColor) {
        BECFactoryGrid.INSTANCE.addElement(this);
    }
}
