package gregtech.api.enums;

import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import gregtech.api.enums.MaterialBuilder2.BlockTexture;
import gregtech.api.enums.MaterialBuilder2.Flags;
import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.IntFraction;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public enum MaterialList implements IMaterial, IMaterialHandle {
    Foo,
    Bar;

    public IMaterial material;

    final Supplier<String> missingMaterialError = () -> String.format("Material for MaterialList.%s was not set.", this.name());

    @Override
    public void setMaterial(IMaterial material) {
        this.material = material;
    }

    @Override
    public int getID() {
        return material.getID();
    }

    @Override
    public String getName() {
        return material.getName();
    }

    @Override
    public String getDefaultLocalName() {
        return material.getDefaultLocalName();
    }

    @Override
    public short[] getRGBA() {
        return material.getRGBA();
    }

    @Override
    public ItemStack getItem(OrePrefixes prefix, int amount) {
        Objects.requireNonNull(material, missingMaterialError);

        return material.getItem(prefix, amount);
    }

    @Override
    public FluidStack getFluid(FluidType fluidType, int amount) {
        Objects.requireNonNull(material, missingMaterialError);
        
        return material.getFluid(fluidType, amount);
    }

    @Override
    public @Nullable Materials getLegacyMaterial() {
        Objects.requireNonNull(material, missingMaterialError);
        
        return material.getLegacyMaterial();
    }

    public static void init() {
        new MaterialBuilder2(450, "Foo", "Foo")
            .setColour(150, 50, 25, 255)
            .addIngot()
            .addHotIngot()
            .addPlates()
            .addMetalParts()
            .addBlock(BlockTexture.IRON_LIKE)
            .addDusts()
            .addCells()
            .addGems()
            .addMolten().done()
            .addFluid().done()
            .addGas().done()
            .addPlasma().done()
            .addCables(23920, 32_768, 4, 2)
            .addComplexCableRecipes()
            .addFluidPipes(23910, 5000, 1000)
            .addItemPipes(23900, new IntFraction(1, 1))
            .setContents(
                MaterialStack2.items(Materials.Iron, 1),
                MaterialStack2.items(Materials.Redstone, 3))
            .addEBFRecipe(5000, 32, 100, Flags.EBF_USE_HYDROGEN)
            .addABSRecipe(5, 123, 567, true)
            .addMixerRecipe(2, 100, 131)
            .setEBFMakesHotIngots()
            .modifyRecipe(RecipeMaps.benderRecipes, Flags.GENERATE_INGOT, Flags.GENERATE_PLATE)
                .setVoltage(10000)
                .setBioCircuit(1)
            .done()
            .modifyRecipe(RecipeMaps.latheRecipes, Flags.GENERATE_GEM_EXQUISITE, Flags.GENERATE_LENS)
                .setVoltage(10000)
                .setTime(100)
            .done()
            .thenAddTo(Foo);
    }
}
