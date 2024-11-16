package gregtech.api.items;

import bartworks.system.material.Werkstoff;
import cpw.mods.fml.common.FMLCommonHandler;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.IGT_ItemWithMaterialRenderer;
import gregtech.api.util.GTLanguageManager;
import gregtech.common.render.items.GeneratedMaterialRenderer;
import gregtech.loaders.load.BECRecipeLoader;
import gregtech.loaders.load.BECRecipeLoader.MaterialInfo;
import gtPlusPlus.core.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.MinecraftForgeClient;

public class ItemCondensate extends GTGenericItem implements IGT_ItemWithMaterialRenderer {

    public static ItemCondensate INSTANCE;

    public ItemCondensate() {
        super("condensate-preview", "%s Condensate", "Must be transferred via a condensate network");
        setHasSubtypes(false);

        INSTANCE = this;

        if (FMLCommonHandler.instance().getSide().isClient()) {
            MinecraftForgeClient.registerItemRenderer(this, RENDERER);
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        String name = "Invalid";

        if (tag != null) {
            MaterialInfo matInfo = BECRecipeLoader.findMaterialByName(tag.getString("material"));
            Object mat = matInfo != null ? matInfo.getMaterial() : null;

            if (mat instanceof Materials gtMat) name = gtMat.mLocalizedName;
            if (mat instanceof Material gtppMat) name = gtppMat.getLocalizedName();
            if (mat instanceof Werkstoff bartMat) name = bartMat.getLocalizedName();
        }

        String base = GTLanguageManager.getTranslation(getUnlocalizedName());
        return String.format(base, name);
    }

    @Override
    public int getDamage(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag == null) return 0;

        MaterialInfo mat = BECRecipeLoader.findMaterialByName(tag.getString("material"));
        
        if (mat != null) {
            if (mat.gtMat != null) return mat.gtMat.mMetaItemSubID;
            if (mat.gtppMat != null) return BECRecipeLoader.GTPP_IDS.getInt(mat.gtppMat) + 1000;
            if (mat.bartMat != null) return mat.bartMat.getmID() + 1000 + BECRecipeLoader.GTPP_IDS.size();
        }

        return 0;
    }

    public static ItemStack getForMaterial(String material, int amount) {
        ItemStack stack = new ItemStack(INSTANCE, amount);

        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("material", material);
        stack.setTagCompound(tag);

        return stack;
    }

    @Override
    public boolean shouldUseCustomRenderer(int aMetaData) {
        return true;
    }

    private static final GeneratedMaterialRenderer RENDERER = new GeneratedMaterialRenderer();

    @Override
    public GeneratedMaterialRenderer getMaterialRenderer(int aMetaData) {
        return RENDERER;
    }

    @Override
    public boolean allowMaterialRenderer(int aMetaData) {
        return true;
    }

    @Override
    public IIcon getIcon(int aMetaData, int pass) {
        return mIcon;
    }

    @Override
    public IIcon getOverlayIcon(int aMetaData, int pass) {
        return null;
    }

    @Override
    public short[] getRGBa(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag != null) {
            MaterialInfo mat = BECRecipeLoader.findMaterialByName(tag.getString("material"));
            
            if (mat != null) {
                if (mat.gtMat != null) return mat.gtMat.getRGBA();
                if (mat.gtppMat != null) return mat.gtppMat.getRGBA();
                if (mat.bartMat != null) return mat.bartMat.getRGBA();
            }
        }

        if (System.currentTimeMillis() / 500 % 2 == 0) {
            return new short[] { 254, 0, 91, 255 };
        } else {
            return new short[] { 0, 0, 0, 255 };
        }
    }
}
