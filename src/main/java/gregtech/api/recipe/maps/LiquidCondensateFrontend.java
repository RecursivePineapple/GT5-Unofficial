package gregtech.api.recipe.maps;

import static gregtech.api.util.GTUtility.formatNumbers;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.gtnewhorizons.modularui.api.math.Pos2d;

import gregtech.api.recipe.BasicUIPropertiesBuilder;
import gregtech.api.recipe.NEIRecipePropertiesBuilder;
import gregtech.api.recipe.RecipeMapFrontend;
import gregtech.api.util.MethodsReturnNonnullByDefault;
import gregtech.loaders.load.BECRecipeLoader;
import gregtech.nei.RecipeDisplayInfo;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LiquidCondensateFrontend extends RecipeMapFrontend {

    public LiquidCondensateFrontend(BasicUIPropertiesBuilder uiPropertiesBuilder,
        NEIRecipePropertiesBuilder neiPropertiesBuilder) {
        super(uiPropertiesBuilder, neiPropertiesBuilder);
    }

    @Override
    public List<Pos2d> getFluidInputPositions(int fluidInputCount) {
        return super.getItemInputPositions(fluidInputCount);
    }

    @Override
    public List<Pos2d> getFluidOutputPositions(int fluidOutputCount) {
        return super.getItemOutputPositions(fluidOutputCount);
    }

    @Override
    protected void drawDurationInfo(RecipeDisplayInfo recipeInfo) {
        
    }

    @Override
    protected void drawEnergyInfo(RecipeDisplayInfo recipeInfo) {
        recipeInfo.drawText(String.format("Quota Required: %s EU/L", formatNumbers(BECRecipeLoader.getRecipeCost(recipeInfo.recipe))));
    }
}
