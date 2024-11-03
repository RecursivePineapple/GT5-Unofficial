package gregtech.api.factory.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import gregtech.api.factory.BlockData;
import gregtech.api.factory.IFactoryNetwork;

public class TestFactoryNetwork implements IFactoryNetwork<TestFactoryNetwork, TestFactoryElement, TestFactoryGrid> {
    
    final BlockData<TestFactoryElement> elements = new BlockData<>();
    final HashMap<Class<?>, Collection<Object>> components = new HashMap<>();
    final HashMap<TestFactoryElement, List<TestFactoryElement>> neighbours = new HashMap<>();

    @Override
    public void addElement(TestFactoryElement element) {
        element.setNetwork(this);
        elements.set(element, element);
        
        for (var component : element.getComponents()) {
            addComponentImpl(component.left(), component.right());
        }

        updateNeighbours(element);
    }

    @Override
    public void removeElement(TestFactoryElement element) {
        element = elements.remove(element);

        if (element != null) {
            element.setNetwork(null);
    
            for (var component : element.getComponents()) {
                removeComponentImpl(component.left(), component.right());
            }

            for (TestFactoryElement neighbour : neighbours.remove(element)) {
                neighbour.updateConnections();
            }
        }
    }

    public void updateNeighbours(TestFactoryElement element) {
        List<TestFactoryElement> neighbours = new ArrayList<>();
        element.getNeighbours(neighbours);
        this.neighbours.put(element, neighbours);

        for (TestFactoryElement neighbour : neighbours) {
            neighbour.updateConnections();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<TestFactoryElement> getPreviousNeighbours(TestFactoryElement centre) {
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
    public Collection<TestFactoryElement> getElements() {
        return elements.values();
    }

    @Override
    public void subsume(TestFactoryGrid grid, TestFactoryNetwork other) {
        var iter = other.getElements().iterator();

        while (iter.hasNext()) {
            TestFactoryElement element = iter.next();

            grid.removeElement(element);

            grid.networks.set(element, this);
            addElement(element);

            iter.remove();
        }

        other.onNetworkRemoved();
    }
}
