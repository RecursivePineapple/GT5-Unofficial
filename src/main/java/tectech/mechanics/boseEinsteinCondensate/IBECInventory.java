package tectech.mechanics.boseEinsteinCondensate;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import gregtech.api.objects.MaterialStack;

public interface IBECInventory {
    @Nullable List<MaterialStack> getContents();

    boolean isEmpty();

    boolean isConnectedTo(IBECInventory other);

    void addCondensate(MaterialStack... stacks);

    void consumeCondensate(MaterialStack... stacks);
}
