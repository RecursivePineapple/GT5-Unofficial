package gregtech.api.factory.test;

import java.util.Collections;
import java.util.List;

import gregtech.api.factory.IFactoryElement;
import it.unimi.dsi.fastutil.Pair;
import net.minecraftforge.common.util.ForgeDirection;

public interface TestFactoryElement extends IFactoryElement<TestFactoryElement, TestFactoryNetwork, TestFactoryGrid> {
    
    public boolean canConnectOnSide(ForgeDirection side);

    public void updateConnections();
}
