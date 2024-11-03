package gregtech.api.factory.standard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import gregtech.api.factory.BlockData;
import gregtech.api.factory.IFactoryElement;
import gregtech.api.factory.IFactoryGrid;
import gregtech.api.factory.IFactoryNetwork;

public abstract class StandardFactoryGrid<TSelf extends StandardFactoryGrid<TSelf, TElement, TNetwork>, TElement extends IFactoryElement<TElement, TNetwork, TSelf>, TNetwork extends IFactoryNetwork<TNetwork, TElement, TSelf>> implements IFactoryGrid<TSelf, TElement, TNetwork> {
    
    public final BlockData<TNetwork> networks = new BlockData<>();

    protected StandardFactoryGrid() {
        
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addElement(TElement element) {
        removeElement(element);
        
        HashSet<TElement> discovered = new HashSet<>();
        HashSet<TNetwork> networks = new HashSet<>();

        walkAdjacency(element, discovered, networks, false);

        if (networks.size() == 0) {
            TNetwork network = createNetwork();
            
            for (TElement e : discovered) {
                network.addElement(e);
                this.networks.set(e, network);
            }
        } else if (networks.size() == 1) {
            TNetwork network = networks.iterator().next();

            for (TElement e : discovered) {
                network.addElement(e);
                this.networks.set(e, network);
            }
        } else {
            var iter = networks.iterator();

            TNetwork biggestNetwork = iter.next();

            while (iter.hasNext()) {
                TNetwork network = iter.next();

                if (network.getElements().size() > biggestNetwork.getElements().size()) biggestNetwork = network;
            }

            for (TElement e : discovered) {
                addElementQuietly(biggestNetwork, e);
            }

            for (TNetwork network : networks) {
                if (network != biggestNetwork) biggestNetwork.subsume((TSelf) this, network);
            }
        }
    }

    @Override
    public void addElementQuietly(TNetwork network, TElement element) {
        network.addElement(element);
        this.networks.set(element, network);
    }

    protected abstract TNetwork createNetwork();

    @Override
    public void removeElement(TElement element) {
        TNetwork network = networks.get(element);

        if (network == null) return;

        Collection<TElement> neighbours = network.getPreviousNeighbours(element);

        network.removeElement(element);
        networks.remove(element);

        if (network.getElements().isEmpty()) {
            network.onNetworkRemoved();
            return;
        }

        if (neighbours == null || neighbours.size() <= 1) return;

        HashSet<HashSet<TElement>> neighbouringClumps = new HashSet<>();

        HashSet<TElement> allDiscovered = new HashSet<>();
        for (TElement neighbour : neighbours) {
            if (allDiscovered.contains(neighbour)) continue;

            HashSet<TElement> discovered = new HashSet<>();
            walkAdjacency(neighbour, discovered, null, true);
            neighbouringClumps.add(discovered);
            allDiscovered.addAll(discovered);
        }

        if (neighbouringClumps.size() <= 1) {
            return;
        }

        HashSet<TElement> biggestClump = null;

        for (HashSet<TElement> nn : neighbouringClumps) {
            if (biggestClump == null || nn.size() > biggestClump.size()) biggestClump = nn;
        }

        for (HashSet<TElement> nn : neighbouringClumps) {
            if (nn != biggestClump) {
                for (TElement e : nn) {
                    network.removeElement(e);
                }
    
                TNetwork newNetwork = createNetwork();
                
                for (TElement e : nn) {
                    newNetwork.addElement(e);
                    this.networks.set(e, newNetwork);
                }
            }
        }
    }

    @Override
    public void removeElementQuietly(TElement element) {
        TNetwork network = networks.get(element);

        if (network == null) return;

        network.removeElement(element);
        networks.remove(element);
    }

    private void walkAdjacency(TElement start, HashSet<TElement> discovered, HashSet<TNetwork> networks, boolean includeNetworked) {
        LinkedList<TElement> queue = new LinkedList<>();

        queue.add(start);

        List<TElement> neighbours = new ArrayList<>();
        while (queue.size() > 0) {
            TElement current = queue.removeFirst();
            discovered.add(current);
            if (networks != null) networks.add(current.getNetwork());

            // don't continue scanning elements that are already part of a network
            if (includeNetworked || current.getNetwork() == null) {
                neighbours.clear();
                current.getNeighbours(neighbours);
    
                for (TElement neighbour : neighbours) {
                    if (!discovered.contains(neighbour)) {
                        queue.add(neighbour);
                    }
                }
            }
        }

        if (networks != null) networks.remove(null);
    }
}
