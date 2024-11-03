package gregtech.api.factory;

import java.util.Collection;

public interface IFactoryNetwork<TSelf extends IFactoryNetwork<TSelf, TElement, TGrid>, TElement extends IFactoryElement<TElement, TSelf, TGrid>, TGrid extends IFactoryGrid<TGrid, TElement, TSelf>> {
    
    public void addElement(TElement element);
    
    public void removeElement(TElement element);

    public default void onNetworkRemoved() {

    }

    public Collection<TElement> getElements();

    public Collection<TElement> getPreviousNeighbours(TElement centre);

    public void subsume(TGrid grid, TSelf other);
}
