package gregtech.api.factory;

public interface IFactoryGrid<TSelf extends IFactoryGrid<TSelf, TElement, TNetwork>, TElement extends IFactoryElement<TElement, TNetwork, TSelf>, TNetwork extends IFactoryNetwork<TNetwork, TElement, TSelf>> {
    
    /**
     * Adds an element and does potentially expensive network topology updates.
     */
    public void addElement(TElement element);

    /**
     * Adds an element but does not do any network topology updates.
     * Use with caution.
     */
    public void addElementQuietly(TNetwork network, TElement element);
    
    /**
     * Removes an element and does potentially expensive network topology updates.
     */
    public void removeElement(TElement element);

    /**
     * Removes an element but does not do any network topology updates.
     * Use with caution.
     */
    public void removeElementQuietly(TElement element);
}
