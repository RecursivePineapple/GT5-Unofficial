package tectech.mechanics.boseEinsteinCondensate;

import static gregtech.api.enums.GTValues.L;
import static gregtech.api.enums.GTValues.M;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import bartworks.system.material.Werkstoff;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.items.ItemCondensate;
import gregtech.loaders.load.BECRecipeLoader;
import gregtech.loaders.load.BECRecipeLoader.MaterialInfo;
import gtPlusPlus.core.material.Material;
import gtPlusPlus.core.util.Utils;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;

public class CondensateStack {
    
    public Object material;
    /** Amount in litres. */
    public long amount;

    public CondensateStack() {

    }

    public CondensateStack(Object material, long amount) {
        if (!(material instanceof Materials) && !(material instanceof Material) && !(material instanceof Werkstoff)) {
            throw new IllegalArgumentException("material must be a GT, GT++, or Bartworks material: " + material);
        }

        this.material = material;
        this.amount = amount;
    }

    public CondensateStack copy() {
        return new CondensateStack(material, amount);
    }

    public static @Nullable CondensateStack fromFluid(FluidStack fluid) {
        if (fluid == null) return null;

        MaterialInfo material = BECRecipeLoader.FLUID_MATS.get(fluid.getFluid());

        if (material == null) return null;

        return new CondensateStack(material.getMaterial(), fluid.amount);
    }

    public static CondensateStack[] fromFluids(FluidStack... fluids) {
        return Arrays.stream(fluids).map(CondensateStack::fromFluid).filter(x -> x != null).toArray(CondensateStack[]::new);
    }

    public static @Nullable CondensateStack fromStack(ItemStack stack) {
        if (stack == null) return null;

        Pair<OrePrefixes, MaterialInfo> matInfo = BECRecipeLoader.findMaterialForStack(stack);

        if (matInfo == null) return null;

        return new CondensateStack(matInfo.right().getMaterial(), stack.stackSize * matInfo.left().mMaterialAmount * L / M);
    }

    public static CondensateStack[] fromStacks(ItemStack... fluids) {
        return Arrays.stream(fluids).map(CondensateStack::fromStack).filter(x -> x != null).toArray(CondensateStack[]::new);
    }

    public ItemStack getPreview() {
        return ItemCondensate.getForMaterial(getMaterialName(), amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount);
    }

    public String getMaterialName() {
        if (material instanceof Materials gtMat) {
            return gtMat.mName;
        }

        if (material instanceof Material gtppMat) {
            return Utils.sanitizeString(gtppMat.getUnlocalizedName());
        }

        if (material instanceof Werkstoff bartMat) {
            return bartMat.getVarName();
        }

        throw new IllegalStateException("material must be a GT, GT++, or Bartworks material");
    }

    public NBTTagCompound writeToTag(NBTTagCompound tag) {
        tag.setString("n", getMaterialName());
        tag.setLong("a", amount);
        return tag;
    }

    public static NBTTagList save(List<CondensateStack> stacks) {
        NBTTagList tags = new NBTTagList();

        for (CondensateStack stack : stacks) {
            tags.appendTag(stack.writeToTag(new NBTTagCompound()));
        }

        return tags;
    }

    @SuppressWarnings("unchecked")
    public static List<CondensateStack> load(NBTTagList stacks) {
        ArrayList<CondensateStack> out = new ArrayList<>();

        for (NBTTagCompound tag : ((List<NBTTagCompound>) stacks.tagList)) {
            CondensateStack stack = CondensateStack.readFromTag(tag);

            if (stack != null) out.add(stack);
        }

        return out;
    }

    public static @Nullable CondensateStack readFromTag(NBTTagCompound tag) {
        String matName = tag.getString("n");

        Object material = Materials.get(matName);

        if (material == Materials._NULL) material = Material.mMaterialCache.get(matName);

        if (material == null) return null;
        
        long amount = tag.getLong("a");

        return new CondensateStack(material, amount);
    }

    public static ItemStack[] getPreviews(CondensateStack[] condensate) {
        return Arrays.stream(condensate).map(CondensateStack::getPreview).toArray(ItemStack[]::new);
    }
}
