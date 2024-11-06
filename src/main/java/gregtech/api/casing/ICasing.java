package gregtech.api.casing;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.lazy;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static gregtech.api.enums.Textures.BlockIcons.getCasingTextureForId;

import com.gtnewhorizon.structurelib.structure.IStructureElement;

import gregtech.api.interfaces.ITexture;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public interface ICasing {
    
    public Block getBlock();
    public int getMeta();
    public int getTextureId();

    public default ItemStack toStack(int amount) {
        return new ItemStack(getBlock(), amount, getMeta());
    }

    public default String getLocalizedName() {
        return new ItemStack(getBlock(), 1, getMeta()).getDisplayName();
    }

    public default <T> IStructureElement<T> asElement() {
        return lazy(() -> ofBlock(getBlock(), getMeta()));
    }

    public default ITexture getCasingTexture() {
        return getCasingTextureForId(getTextureId());
    }
}
