package tectech.mechanics.boseEinsteinCondensate;

import net.minecraftforge.common.util.ForgeDirection;

import gregtech.api.factory.IFactoryElement;

public interface BECFactoryElement extends IFactoryElement<BECFactoryElement, BECFactoryNetwork, BECFactoryGrid> {

    public static enum ConnectionType {
        CONNECTED,
        VISUAL_ONLY,
        NONE,
    }

    public ConnectionType getConnectionOnSide(ForgeDirection side);
}
