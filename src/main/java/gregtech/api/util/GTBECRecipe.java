package gregtech.api.util;

import net.minecraftforge.fluids.FluidStack;

import gregtech.api.enums.NaniteTier;
import gregtech.api.recipe.metadata.EmptyRecipeMetadataStorage;
import tectech.mechanics.boseEinsteinCondensate.CondensateStack;

public class GTBECRecipe extends GTRecipe {

    public final CondensateStack[] mCInput;
    public final CondensateStack[] mCOutput;
    public final NaniteTier[] mInputTiers;

    public GTBECRecipe(GTRecipe base, CondensateStack[] cInput, CondensateStack[] cOutput, NaniteTier[] inputTiers) {
        super(
            base.mInputs,
            base.mOutputs,
            new FluidStack[0],
            new FluidStack[0],
            base.mChances,
            base.mSpecialItems,
            base.mDuration,
            base.mEUt,
            base.mSpecialValue,
            base.mEnabled,
            base.mHidden,
            base.mFakeRecipe,
            base.mCanBeBuffered,
            base.mNeedsEmptyOutput,
            base.isNBTSensitive,
            base.getNeiDesc(),
            base.getMetadataStorage() == EmptyRecipeMetadataStorage.INSTANCE ? null : base.getMetadataStorage(),
            base.getRecipeCategory());

        mCInput = cInput;
        mCOutput = cOutput;
        mInputTiers = inputTiers;
    }

    public static int compare(GTRecipe a, GTRecipe b) {
        if (!(a instanceof GTBECRecipe bA)) return 0;
        if (!(b instanceof GTBECRecipe bB)) return 0;

        if (bA.mInputTiers != null && bB.mInputTiers != null) {
            NaniteTier[] iA = bA.mInputTiers;
            NaniteTier[] iB = bB.mInputTiers;

            for (int i = 0; i < Math.max(iA.length, iB.length); i++) {
                if (i >= iA.length) {
                    return -1;
                }

                if (i >= iB.length) {
                    return 1;
                }

                int result = iA[i].compareTo(iB[i]);

                if (result != 0) return result;
            }
        }

        int result = Integer.compare(bA.mEUt, bB.mEUt);

        if (result != 0) return result;

        return Integer.compare(bA.mDuration, bB.mDuration);
    }
}
