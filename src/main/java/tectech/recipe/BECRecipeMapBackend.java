package tectech.recipe;

import javax.annotation.Nonnull;

import gregtech.api.enums.NaniteTier;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.recipe.RecipeMapBackendPropertiesBuilder;
import gregtech.api.util.GTBECRecipe;
import gregtech.api.util.GTDataUtils;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTRecipeConstants;
import tectech.mechanics.boseEinsteinCondensate.CondensateStack;

public class BECRecipeMapBackend extends RecipeMapBackend {

    public BECRecipeMapBackend(RecipeMapBackendPropertiesBuilder propertiesBuilder) {
        super(propertiesBuilder);
    }

    @Override
    public @Nonnull GTRecipe compileRecipe(@Nonnull GTRecipe recipe) {
        CondensateStack[] inputs = GTDataUtils
            .mapToArray(recipe.mFluidInputs, CondensateStack[]::new, CondensateStack::fromFluid);

        CondensateStack[] outputs = GTDataUtils
            .mapToArray(recipe.mFluidOutputs, CondensateStack[]::new, CondensateStack::fromFluid);

        NaniteTier[] tiers = recipe.getMetadata(GTRecipeConstants.NANITE_TIERS);

        if (tiers != null && tiers.length != recipe.mInputs.length) {
            throw new IllegalArgumentException("nanite tiers length must match item input length");
        }

        return super.compileRecipe(new GTBECRecipe(recipe, inputs, outputs, tiers));
    }
}
