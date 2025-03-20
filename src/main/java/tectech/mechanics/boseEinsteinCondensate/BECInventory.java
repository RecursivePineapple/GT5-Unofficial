package tectech.mechanics.boseEinsteinCondensate;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.Object2LongMap;

public interface BECInventory {

    @NotNull Object2LongMap<Object> getContents();

    void addCondensate(Collection<CondensateStack> stacks);

    boolean removeCondensate(Collection<CondensateStack> stacks);
}
