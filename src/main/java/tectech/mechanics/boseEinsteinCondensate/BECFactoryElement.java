package tectech.mechanics.boseEinsteinCondensate;

import gregtech.api.factory.IFactoryElement;
import net.minecraftforge.common.util.ForgeDirection;

public interface BECFactoryElement extends IFactoryElement<BECFactoryElement, BECFactoryNetwork, BECFactoryGrid> {

    public boolean canConnectOnSide(ForgeDirection side);
}
