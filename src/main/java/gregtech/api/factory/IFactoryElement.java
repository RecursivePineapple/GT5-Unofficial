package gregtech.api.factory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.Pair;

public interface IFactoryElement<TSelf extends IFactoryElement<TSelf, TNetwork, TGrid>, TNetwork extends IFactoryNetwork<TNetwork, TSelf, TGrid>, TGrid extends IFactoryGrid<TGrid, TSelf, TNetwork>> {
    
    /**
     * Detects all adjacent elements, regardless of what network they're on.
     */
    void getNeighbours(Collection<TSelf> neighbours);

    TNetwork getNetwork();
    
    void setNetwork(TNetwork network);

    default void onNeighbourAdded(TSelf neighbour) {
        onNeighbourChanged(neighbour);
    }

    default void onNeighbourRemoved(TSelf neighbour) {
        onNeighbourChanged(neighbour);
    }

    default void onNeighbourChanged(TSelf neighbour) {

    }

    default List<Pair<Class<?>, Object>> getComponents() {
        return Collections.emptyList();
    }
}
