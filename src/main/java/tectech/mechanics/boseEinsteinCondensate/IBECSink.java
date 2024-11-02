package tectech.mechanics.boseEinsteinCondensate;

import tectech.mechanics.pipe.IConnectsToBECPipe;

public interface IBECSink extends IConnectsToBECPipe, IBECInventory {
    void setSource(IBECInventory source);

    void onSourceInventoryChanged();
}
