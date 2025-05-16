package tectech.mechanics.boseEinsteinCondensate;

import java.util.Collection;
import java.util.HashSet;

import gregtech.api.factory.FactoryPath;
import gregtech.api.factory.FactoryRoutes;
import gregtech.api.factory.GraphRouteTracker;
import gregtech.api.factory.standard.StandardFactoryNetwork;

public class BECFactoryNetwork extends StandardFactoryNetwork<BECFactoryNetwork, BECFactoryElement, BECFactoryGrid> {

    public final GraphRouteTracker<BECFactoryElement, BECFactoryNetwork, BECFactoryGrid, NotableBECFactoryElement, BECRouteInfo> routeTracker = new GraphRouteTracker<>(NotableBECFactoryElement.class, new BECRouteInfo(0));

    private boolean networkChanged = false;

    @Override
    public void addElement(BECFactoryElement element) {
        super.addElement(element);

        routeTracker.onElementAdded(element);
        networkChanged = true;
    }

    @Override
    public void removeElement(BECFactoryElement element) {
        super.removeElement(element);

        routeTracker.onElementRemoved(element);
        networkChanged = true;
    }

    public void invalidateRoutes() {
        networkChanged = true;
        routeTracker.invalidateRoutes();
    }

    void onPostTick() {
        if (networkChanged) {
            networkChanged = false;

            routeTracker.updateEdges();
        }
    }

    public void drainCondensate(NotableBECFactoryElement drainer, Collection<CondensateStack> requests) {
        FactoryRoutes<NotableBECFactoryElement, BECRouteInfo> routes = routeTracker.dijkstra(drainer);

        for (FactoryPath<NotableBECFactoryElement, BECRouteInfo> node : routes) {
            if (!(node.end instanceof BECInventory inv)) continue;

            if (inv.removeCondensate(requests)) break;
        }
    }

    public int getSlowdowns(NotableBECFactoryElement drainer, Collection<Object> validMaterials) {
        FactoryRoutes<NotableBECFactoryElement, BECRouteInfo> routes = routeTracker.dijkstra(drainer);

        HashSet<Object> storedMaterials = new HashSet<>();

        for (FactoryPath<NotableBECFactoryElement, BECRouteInfo> node : routes) {
            if (!(node.end instanceof BECInventory inv)) continue;

            storedMaterials.addAll(inv.getContents().keySet());
        }

        for (Object mat : validMaterials) {
            storedMaterials.remove(mat);
        }

        return storedMaterials.size();
    }
}
