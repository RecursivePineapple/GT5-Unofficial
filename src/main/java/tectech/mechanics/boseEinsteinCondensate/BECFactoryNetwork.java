package tectech.mechanics.boseEinsteinCondensate;

import gregtech.api.enums.Materials;
import gregtech.api.factory.standard.StandardFactoryNetwork;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

public class BECFactoryNetwork extends StandardFactoryNetwork<BECFactoryNetwork, BECFactoryElement, BECFactoryGrid> {
    
    public Object2LongOpenHashMap<Materials> getStoredCondensate() {
        Object2LongOpenHashMap<Materials> condensate = new Object2LongOpenHashMap<>();

        

        return condensate;
    }

}
