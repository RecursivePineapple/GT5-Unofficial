package tectech.recipe;

import java.util.List;

import javax.annotation.Nonnull;


import gregtech.api.enums.NaniteTier;
import gregtech.api.recipe.BasicUIPropertiesBuilder;
import gregtech.api.recipe.NEIRecipePropertiesBuilder;
import gregtech.api.recipe.maps.AssemblyLineFrontend;
import gregtech.api.util.GTBECRecipe;
import gregtech.api.util.GTUtility;
import gregtech.nei.GTNEIDefaultHandler.FixedPositionedStack;
import org.jetbrains.annotations.NotNull;

public class BECRecipeMapFrontend extends AssemblyLineFrontend {

    public BECRecipeMapFrontend(BasicUIPropertiesBuilder uiPropertiesBuilder, NEIRecipePropertiesBuilder neiPropertiesBuilder) {
        super(uiPropertiesBuilder, neiPropertiesBuilder);
    }

    @Override
    protected @NotNull List<String> handleNEIItemInputTooltip(@Nonnull List<String> currentTip, @Nonnull FixedPositionedStack pStack) {
        currentTip = super.handleNEIItemInputTooltip(currentTip, pStack);

        int slot = pStack.recipe.mInputs.indexOf(pStack);

        if (slot > -1) {
            NaniteTier tier = ((GTBECRecipe)pStack.recipe.mRecipe).mInputTiers[slot];
            String name = tier.getMaterial().getLocalizedNameForItem("%material");

            currentTip.add(GTUtility.GRAY + GTUtility.ITALIC + GTUtility.translate("gt.tooltip.nanite-tier", name, tier.ordinal()));
        } else {
            currentTip.add(GTUtility.GRAY + GTUtility.ITALIC + GTUtility.translate("gt.tooltip.nanite-tier", "null"));
        }

        return currentTip;
    }
}
