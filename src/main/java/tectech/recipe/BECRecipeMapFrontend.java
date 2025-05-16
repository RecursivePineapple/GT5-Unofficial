package tectech.recipe;

import static com.gtnewhorizon.gtnhlib.util.AnimatedTooltipHandler.GRAY;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import gregtech.api.enums.NaniteTier;
import gregtech.api.items.ItemCondensate;
import gregtech.api.recipe.BasicUIPropertiesBuilder;
import gregtech.api.recipe.NEIRecipePropertiesBuilder;
import gregtech.api.recipe.maps.AssemblyLineFrontend;
import gregtech.api.util.GTBECRecipe;
import gregtech.api.util.GTDataUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.MethodsReturnNonnullByDefault;
import gregtech.nei.FluidDisplayFactory;
import gregtech.nei.FluidDisplayStackMode;
import gregtech.nei.GTNEIDefaultHandler.FixedPositionedStack;
import gregtech.nei.RecipeDisplayInfo;
import tectech.mechanics.boseEinsteinCondensate.CondensateStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BECRecipeMapFrontend extends AssemblyLineFrontend {

    public BECRecipeMapFrontend(BasicUIPropertiesBuilder uiPropertiesBuilder,
        NEIRecipePropertiesBuilder neiPropertiesBuilder) {
        super(uiPropertiesBuilder, neiPropertiesBuilder);
    }

    @Override
    protected List<String> handleNEIItemInputTooltip(List<String> currentTip, FixedPositionedStack pStack) {
        currentTip = super.handleNEIItemInputTooltip(currentTip, pStack);

        int slot = pStack.recipe.mInputs.indexOf(pStack);

        NaniteTier tier = GTDataUtils.getIndexSafe(((GTBECRecipe) pStack.recipe.mRecipe).mInputTiers, slot);

        if (tier != null) {
            currentTip.add(GRAY + GTUtility.translate("gt.tooltip.nanite-tier", tier.describe()));
        }

        return currentTip;
    }

    @Override
    protected void drawSpecialInfo(RecipeDisplayInfo recipeInfo) {
        super.drawSpecialInfo(recipeInfo);

        GTBECRecipe recipe = (GTBECRecipe) recipeInfo.recipe;

        if (recipe.mInputTiers != null && recipe.mInputTiers.length > 0) {
            NaniteTier maxTier = recipe.mInputTiers[0];

            for (NaniteTier tier : recipe.mInputTiers) {
                if (tier.tier > maxTier.tier) maxTier = tier;
            }

            recipeInfo.drawText(GTUtility.translate("gt.tooltip.required-nanite", maxTier.describe()));
        }
    }

    public static final FluidDisplayFactory CONDENSATE_FLUID_DISPLAY = new FluidDisplayFactory() {

        @Override
        public ItemStack getFluidDisplay(FluidStack fluid, FluidDisplayStackMode stackMode) {
            CondensateStack stack = CondensateStack.fromFluid(fluid);

            if (stack == null) return ItemCondensate.getForMaterial("error", fluid.amount);

            return ItemCondensate.getForMaterial(stack.getMaterialName(), fluid.amount);
        }

        @Override
        public @Nullable FluidStack getFluidFromStack(ItemStack stack) {
            return null;
        }
    };
}
