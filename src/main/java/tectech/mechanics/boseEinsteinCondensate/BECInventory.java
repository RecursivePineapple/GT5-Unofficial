package tectech.mechanics.boseEinsteinCondensate;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public interface BECInventory {

    @Nullable List<CondensateStack> getContents();

    void addCondensate(Collection<CondensateStack> stacks);

    boolean removeCondensate(Collection<CondensateStack> stacks);
}
