package gregtech.api.factory.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import gregtech.api.factory.BlockData;
import gregtech.api.factory.IFactoryGrid;

public class TestFactoryGrid implements IFactoryGrid<TestFactoryGrid, TestFactoryElement, TestFactoryNetwork> {
    
    public static final TestFactoryGrid INSTANCE = new TestFactoryGrid();

    final BlockData<TestFactoryNetwork> networks = new BlockData<>();

    @Override
    public void addElement(TestFactoryElement element) {
        removeElement(element);
        
        HashSet<TestFactoryElement> discovered = new HashSet<>();
        HashSet<TestFactoryNetwork> networks = new HashSet<>();

        walkAdjacency(element, discovered, networks, false);

        if (networks.size() == 0) {
            TestFactoryNetwork network = new TestFactoryNetwork();
            
            for (TestFactoryElement e : discovered) {
                network.addElement(e);
                this.networks.set(e, network);
            }
        } else if (networks.size() == 1) {
            TestFactoryNetwork network = networks.iterator().next();

            for (TestFactoryElement e : discovered) {
                network.addElement(e);
                this.networks.set(e, network);
            }
        } else {
            var iter = networks.iterator();

            TestFactoryNetwork biggestNetwork = iter.next();

            while (iter.hasNext()) {
                TestFactoryNetwork network = iter.next();

                if (network.getElements().size() > biggestNetwork.getElements().size()) biggestNetwork = network;
            }

            for (TestFactoryElement e : discovered) {
                biggestNetwork.addElement(e);
                this.networks.set(e, biggestNetwork);
            }

            for (TestFactoryNetwork network : networks) {
                if (network != biggestNetwork) biggestNetwork.subsume(this, network);
            }
        }
    }

    @Override
    public void removeElement(TestFactoryElement element) {
        TestFactoryNetwork network = networks.get(element);

        if (network == null) return;

        List<TestFactoryElement> neighbours = network.neighbours.get(element);

        network.removeElement(element);
        networks.remove(element);

        if (network.getElements().isEmpty()) {
            network.onNetworkRemoved();
            return;
        }

        if (neighbours == null || neighbours.size() <= 1) return;

        HashSet<HashSet<TestFactoryElement>> neighbouringClumps = new HashSet<>();

        HashSet<TestFactoryElement> allDiscovered = new HashSet<>();
        for (TestFactoryElement neighbour : neighbours) {
            if (allDiscovered.contains(neighbour)) continue;

            HashSet<TestFactoryElement> discovered = new HashSet<>();
            walkAdjacency(neighbour, discovered, null, true);
            neighbouringClumps.add(discovered);
            allDiscovered.addAll(discovered);
        }

        if (neighbouringClumps.size() <= 1) {
            return;
        }

        HashSet<TestFactoryElement> biggestClump = null;

        for (HashSet<TestFactoryElement> nn : neighbouringClumps) {
            if (biggestClump == null || nn.size() > biggestClump.size()) biggestClump = nn;
        }

        for (HashSet<TestFactoryElement> nn : neighbouringClumps) {
            if (nn != biggestClump) {
                for (TestFactoryElement e : nn) {
                    network.removeElement(e);
                }
    
                TestFactoryNetwork newNetwork = new TestFactoryNetwork();
                
                for (TestFactoryElement e : nn) {
                    newNetwork.addElement(e);
                    this.networks.set(e, newNetwork);
                }
            }
        }
    }

    private void walkAdjacency(TestFactoryElement start, HashSet<TestFactoryElement> discovered, HashSet<TestFactoryNetwork> networks, boolean includeNetworked) {
        LinkedList<TestFactoryElement> queue = new LinkedList<>();

        queue.add(start);

        List<TestFactoryElement> neighbours = new ArrayList<>();
        while (queue.size() > 0) {
            TestFactoryElement current = queue.removeFirst();
            discovered.add(current);
            if (networks != null) networks.add(current.getNetwork());

            // don't continue scanning elements that are already part of a network
            if (includeNetworked || current.getNetwork() == null) {
                neighbours.clear();
                current.getNeighbours(neighbours);
    
                for (TestFactoryElement neighbour : neighbours) {
                    if (!discovered.contains(neighbour)) {
                        queue.add(neighbour);
                    }
                }
            }
        }

        if (networks != null) networks.remove(null);
    }
}
