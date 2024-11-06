package gregtech.loaders.load;

import static gregtech.api.util.GTRecipeConstants.CONDENSATE_OUTPUT;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.enums.TierEU;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTRecipe;
import tectech.mechanics.boseEinsteinCondensate.CondensateStack;

public class BECRecipeLoader {
    
    private static final int EU_COST_PER_UNIT = (int) TierEU.RECIPE_UV;

    public static void run() {
        loadCreationRecipes();
    }

    private static void loadCreationRecipes() {
        for (GTRecipe recipe : RecipeMaps.fluidExtractionRecipes.getBackend().getAllRecipes()) {
            if (recipe.mFluidOutputs.length == 1 && recipe.mOutputs.length == 0) {
                Materials material = Materials.FLUID_MAP.get(recipe.mFluidOutputs[0].getFluid());
    
                if (material != null) {
                    GTValues.RA.stdBuilder()
                        .itemInputs(recipe.mInputs[0])
                        .metadata(CONDENSATE_OUTPUT, new CondensateStack(material, recipe.mFluidOutputs[0].amount))
                        .eut(EU_COST_PER_UNIT)
                        .duration(1)
                        .addTo(RecipeMaps.condensateCreationRecipes);
                }
            }
        }

        // for (GTRecipe recipe : RecipeMaps.arcFurnaceRecipes.getBackend().getAllRecipes()) {
        //     if (recipe.mInputs.length == 1 && recipe.mOutputs.length == 1 && recipe.mFluidOutputs.length == 0) {
        //         ItemData data = GTOreDictUnificator.getItemData(recipe.mOutputs[0]);
    
        //         if (data != null && data.mPrefix.mMaterialAmount > 0) {
        //             GTValues.RA.stdBuilder()
        //                 .itemInputs(recipe.mInputs[0])
        //                 .metadata(CONDENSATE_OUTPUT, new CondensateStack(data.mMaterial.mMaterial, data.mMaterial.mAmount * L / M))
        //                 .eut(EU_COST_PER_UNIT)
        //                 .duration(1)
        //                 .addTo(RecipeMaps.condensateCreationRecipes);
        //         }
        //     }
        // }

        // for (Entry<Fluid, Materials> e : Materials.FLUID_MAP.entrySet()) {
        //     GTValues.RA.stdBuilder()
        //         .fluidInputs(new FluidStack(e.getKey(), 1000))
        //         .metadata(CONDENSATE_OUTPUT, new CondensateStack(e.getValue(), 1000))
        //         .eut(EU_COST_PER_UNIT)
        //         .duration(1)
        //         .addTo(RecipeMaps.condensateCreationRecipes);
        // }
    }

}
