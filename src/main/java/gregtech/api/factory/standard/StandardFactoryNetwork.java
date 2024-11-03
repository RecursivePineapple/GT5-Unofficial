package gregtech.api.factory.standard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import gregtech.api.factory.BlockData;
import gregtech.api.factory.IFactoryElement;
import gregtech.api.factory.IFactoryGrid;
import gregtech.api.factory.IFactoryNetwork;

public class StandardFactoryNetwork<TSelf extends IFactoryNetwork<TSelf, TElement, TGrid>, TElement extends IFactoryElement<TElement, TSelf, TGrid>, TGrid extends IFactoryGrid<TGrid, TElement, TSelf>> implements IFactoryNetwork<TSelf, TElement, TGrid> {
    
    final BlockData<TElement> elements = new BlockData<>();
    final HashMap<Class<?>, Collection<Object>> components = new HashMap<>();
    final HashMap<TElement, List<TElement>> neighbours = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public void addElement(TElement element) {
        element.setNetwork((TSelf) this);
        elements.set(element, element);
        
        for (var component : element.getComponents()) {
            addComponentImpl(component.left(), component.right());
        }

        updateNeighbours(element, true);
    }

    @Override
    public void removeElement(TElement element) {
        element = elements.remove(element);

        if (element != null) {
            element.setNetwork(null);
    
            for (var component : element.getComponents()) {
                removeComponentImpl(component.left(), component.right());
            }

            for (TElement neighbour : neighbours.remove(element)) {
                neighbour.onNeighbourRemoved(element);
            }
        }
    }

    public void updateElement(TElement element) {
        updateNeighbours(element, false);
    }

    public void updateNeighbours(TElement element, boolean isAdd) {
        List<TElement> neighbours = new ArrayList<>();
        element.getNeighbours(neighbours);
        this.neighbours.put(element, neighbours);

        for (TElement neighbour : neighbours) {
            if (isAdd) {
                neighbour.onNeighbourAdded(neighbour);
            } else {
                neighbour.onNeighbourUpdated(element);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<TElement> getPreviousNeighbours(TElement centre) {
        return neighbours.getOrDefault(centre, Collections.EMPTY_LIST);
    }

    private void addComponentImpl(Class<?> iface, Object impl) {
        components.computeIfAbsent(iface, x -> new HashSet<>()).add(impl);
    }

    public <TIface, TImpl extends TIface> void addComponent(Class<TIface> iface, TImpl impl) {
        addComponentImpl(iface, impl);
    }

    private void removeComponentImpl(Class<?> iface, Object impl) {
        Collection<Object> s = components.get(iface);

        if (s != null) {
            s.remove(impl);

            if (s.isEmpty()) {
                components.remove(iface);
            }
        }
    }

    public <TIface, TImpl extends TIface> void removeComponent(Class<TIface> iface, TImpl impl) {
        removeComponentImpl(iface, impl);
    }

    @SuppressWarnings("unchecked")
    public <TIface> Collection<TIface> getComponents(Class<TIface> iface) {
        return (Collection<TIface>) components.getOrDefault(iface, Collections.emptyList());
    }

    @Override
    public Collection<TElement> getElements() {
        return elements.values();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void subsume(TGrid grid, TSelf other) {
        var iter = other.getElements().iterator();

        while (iter.hasNext()) {
            TElement element = iter.next();

            grid.removeElement(element);

            grid.addElementQuietly((TSelf) this, element);
            addElement(element);

            iter.remove();
        }

        other.onNetworkRemoved();
    }
}
