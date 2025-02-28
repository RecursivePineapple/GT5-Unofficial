package tectech.mechanics.boseEinsteinCondensate;

import java.util.HashSet;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import gregtech.api.factory.standard.StandardFactoryNetwork;

public class BECFactoryNetwork extends StandardFactoryNetwork<BECFactoryNetwork, BECFactoryElement, BECFactoryGrid> {

    public final HashSet<BECFactoryElement> routingVertices = new HashSet<>();
    public final SetMultimap<BECFactoryElement, BECFactoryElement> routingEdges = MultimapBuilder.hashKeys()
        .hashSetValues()
        .build();

}
