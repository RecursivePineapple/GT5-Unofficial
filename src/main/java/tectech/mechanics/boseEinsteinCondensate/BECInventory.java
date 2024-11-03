package tectech.mechanics.boseEinsteinCondensate;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import gregtech.api.objects.MaterialStack;

public interface BECInventory extends BECFactoryElement {

    @Nullable List<MaterialStack> getContents();

    void addCondensate(MaterialStack... stacks);

    void consumeCondensate(MaterialStack... stacks);
}
