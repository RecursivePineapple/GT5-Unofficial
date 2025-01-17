package tectech.mechanics.boseEinsteinCondensate;

import tectech.mechanics.pipe.IConnectsToBECPipe;

public interface IBECSink extends IConnectsToBECPipe, BECInventory {

    void setSource(BECInventory source);

    void onSourceInventoryChanged();
}
