package gregtech.api.factory.test;

import gregtech.api.factory.IFactoryElement;
import net.minecraftforge.common.util.ForgeDirection;

public interface TestFactoryElement extends IFactoryElement<TestFactoryElement, TestFactoryNetwork, TestFactoryGrid> {
    
    public boolean canConnectOnSide(ForgeDirection side);
}
