package gregtech.api.recipe.maps;

import static gregtech.api.util.GTUtility.formatNumbers;

import javax.annotation.ParametersAreNonnullByDefault;

import gregtech.api.recipe.BasicUIPropertiesBuilder;
import gregtech.api.recipe.NEIRecipePropertiesBuilder;
import gregtech.api.recipe.RecipeMapFrontend;
import gregtech.api.util.MethodsReturnNonnullByDefault;
import gregtech.loaders.load.BECRecipeLoader;
import gregtech.nei.RecipeDisplayInfo;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CondensateFrontend extends RecipeMapFrontend {

    public CondensateFrontend(BasicUIPropertiesBuilder uiPropertiesBuilder,
        NEIRecipePropertiesBuilder neiPropertiesBuilder) {
        super(uiPropertiesBuilder, neiPropertiesBuilder);
    }

    @Override
    protected void drawDurationInfo(RecipeDisplayInfo recipeInfo) {

    }

    @Override
    protected void drawEnergyInfo(RecipeDisplayInfo recipeInfo) {
        recipeInfo.drawText(
            String.format("Quota Required: %s EU", formatNumbers(BECRecipeLoader.getRecipeCost(recipeInfo.recipe))));
    }
}
