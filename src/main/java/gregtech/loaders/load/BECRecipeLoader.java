package gregtech.loaders.load;

import static gregtech.api.enums.GTValues.L;
import static gregtech.api.util.GTRecipeConstants.CONDENSATE_EU_COST;
import static gregtech.api.util.GTRecipeConstants.CONDENSATE_INPUTS;
import static gregtech.api.util.GTRecipeConstants.CONDENSATE_OUTPUTS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import bartworks.system.material.Werkstoff;
import bartworks.system.material.WerkstoffLoader;
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.enums.TierEU;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility.ItemId;
import gtPlusPlus.core.material.Material;
import gtPlusPlus.core.util.Utils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import tectech.mechanics.boseEinsteinCondensate.CondensateStack;
import tectech.recipe.TecTechRecipeMaps;

public class BECRecipeLoader {

    private static final int EU_COST_PER_UNIT = (int) TierEU.RECIPE_UV, EU_COST_PER_LITRE = (int) TierEU.RECIPE_HV;

    public static class MaterialInfo {

        public final String name;
        public Materials gtMat;
        public Material gtppMat;
        public Werkstoff bartMat;

        public MaterialInfo(Materials gtMat) {
            this.name = gtMat.mName;
            this.gtMat = gtMat;
        }

        public MaterialInfo(Material gtppMat) {
            this.name = Utils.sanitizeString(gtppMat.getUnlocalizedName());
            this.gtppMat = gtppMat;
        }

        public MaterialInfo(Werkstoff bartMat) {
            this.name = bartMat.getVarName();
            this.bartMat = bartMat;
        }

        public List<Fluid> getInputFluids() {
            ArrayList<Fluid> fluids = new ArrayList<>();

            if (gtMat != null) {
                fluids.add(gtMat.mFluid);
                fluids.add(gtMat.mStandardMoltenFluid);
                fluids.add(gtMat.mGas);
                fluids.add(gtMat.mPlasma);
                fluids.add(gtMat.mSolid);
            }

            if (gtppMat != null) {
                fluids.add(gtppMat.getFluid());
                fluids.add(gtppMat.getPlasma());
            }

            if (bartMat != null) {
                fluids.add(WerkstoffLoader.fluids.get(bartMat));
                fluids.add(WerkstoffLoader.molten.get(bartMat));
            }

            fluids.removeIf(x -> x == null);

            return fluids;
        }

        public Fluid getOutputFluid() {
            if (gtMat != null) {
                if (gtMat.mStandardMoltenFluid != null) return gtMat.mStandardMoltenFluid;
                if (gtMat.mFluid != null) return gtMat.mFluid;
                if (gtMat.mGas != null) return gtMat.mGas;
                if (gtMat.mSolid != null) return gtMat.mSolid;
            }

            if (gtppMat != null) {
                if (gtppMat.getFluid() != null) return gtppMat.getFluid();
            }

            if (bartMat != null) {
                if (WerkstoffLoader.molten.get(bartMat) != null) return WerkstoffLoader.molten.get(bartMat);
                if (WerkstoffLoader.fluids.get(bartMat) != null) return WerkstoffLoader.fluids.get(bartMat);
            }

            return null;
        }

        public Object getMaterial() {
            if (gtMat != null && gtMat.mMetaItemSubID > 0) return gtMat;
            if (bartMat != null) return bartMat;
            if (gtppMat != null) return gtppMat;
            throw new IllegalStateException("One of gtMat, gtppMat, bartMat must be non-null: " + name);
        }

        @Override
        public String toString() {
            return "MaterialInfo [name=" + name
                + ", gtMat="
                + gtMat
                + ", gtppMat="
                + gtppMat
                + ", bartMat="
                + bartMat
                + ", getInputFluids()="
                + getInputFluids()
                + ", getOutputFluid()="
                + getOutputFluid()
                + "]";
        }
    }

    public static final Object2ObjectOpenHashMap<Fluid, MaterialInfo> FLUID_MATS = new Object2ObjectOpenHashMap<>();
    public static final Object2IntOpenHashMap<Material> GTPP_IDS = new Object2IntOpenHashMap<>();
    public static final Object2ObjectOpenHashMap<String, MaterialInfo> MATS_BY_NAME = new Object2ObjectOpenHashMap<>();

    public static void run() {
        loadMaterials();
        loadCreationRecipes();
    }

    private static MaterialInfo getMaterial(Materials gt) {
        return MATS_BY_NAME.computeIfAbsent(gt.mName, x -> new MaterialInfo(gt));
    }

    private static MaterialInfo getMaterial(Material gtpp) {
        return MATS_BY_NAME
            .computeIfAbsent(Utils.sanitizeString(gtpp.getUnlocalizedName()), x -> new MaterialInfo(gtpp));
    }

    private static MaterialInfo getMaterial(Werkstoff bart) {
        return MATS_BY_NAME.computeIfAbsent(bart.getVarName(), x -> new MaterialInfo(bart));
    }

    private static void loadMaterials() {
        FLUID_MATS.clear();

        for (Materials mat : GregTechAPI.sGeneratedMaterials) {
            if (mat != null && mat != Materials._NULL && mat.mMetaItemSubID > 0) {
                getMaterial(mat).gtMat = mat;
            }
        }

        for (Material mat : Material.mMaterialMap) {
            GTPP_IDS.put(mat, GTPP_IDS.size());

            getMaterial(mat).gtppMat = mat;
        }

        for (Werkstoff mat : Werkstoff.werkstoffNameHashMap.values()) {
            getMaterial(mat).bartMat = mat;
        }

        for (MaterialInfo mat : MATS_BY_NAME.values()) {
            for (Fluid fluid : mat.getInputFluids()) {
                MaterialInfo prev = FLUID_MATS.remove(fluid);
                if (prev != null && prev != mat) {
                    GTMod.GT_FML_LOGGER.warn(
                        "Re-detected fluid " + fluid
                            + " for material "
                            + mat
                            + " (was "
                            + prev
                            + ", which will be ignored)");
                }

                FLUID_MATS.put(fluid, mat);
            }
        }
    }

    private static void loadCreationRecipes() {
        HashSet<ItemId> added = new HashSet<>();

        for (GTRecipe recipe : RecipeMaps.fluidExtractionRecipes.getBackend()
            .getAllRecipes()) {
            if (recipe.mFluidOutputs.length == 1 && recipe.mOutputs.length == 0) continue;

            if (!added.add(ItemId.create(recipe.mInputs[0]))) continue;

            CondensateStack output = CondensateStack.fromFluid(recipe.mFluidOutputs[0]);

            if (output == null) continue;

            CondensateStack[] outputs = new CondensateStack[] { output };

            GTValues.RA.stdBuilder()
                .itemInputs(recipe.mInputs[0])
                .itemOutputs(CondensateStack.getPreviews(outputs))
                .metadata(CONDENSATE_OUTPUTS, outputs)
                .eut(EU_COST_PER_UNIT * output.amount / L)
                .duration(1)
                .addTo(TecTechRecipeMaps.condensateCreationFromItemRecipes);
        }

        for (GTRecipe recipe : RecipeMaps.arcFurnaceRecipes.getBackend()
            .getAllRecipes()) {
            if (recipe.mInputs.length != 1 && recipe.mOutputs.length != 1) continue;

            if (!added.add(ItemId.create(recipe.mInputs[0]))) continue;

            Pair<OrePrefixes, MaterialInfo> input = findMaterialForStack(recipe.mInputs[0]);
            Pair<OrePrefixes, MaterialInfo> output = findMaterialForStack(recipe.mOutputs[0]);

            if (input == null || output == null || input.right() != output.right()) continue;

            CondensateStack outputCondensate = CondensateStack.fromStack(recipe.mOutputs[0]);
            Objects.requireNonNull(
                outputCondensate,
                () -> "Expected " + recipe.mOutputs[0] + "; '" + output.right().name + "' to return valid condensate");
            CondensateStack[] outputs = { outputCondensate };

            GTValues.RA.stdBuilder()
                .itemInputs(recipe.mInputs[0])
                .itemOutputs(CondensateStack.getPreviews(outputs))
                .metadata(CONDENSATE_OUTPUTS, outputs)
                .eut(EU_COST_PER_UNIT * outputCondensate.amount / L)
                .duration(1)
                .addTo(TecTechRecipeMaps.condensateCreationFromItemRecipes);
        }

        for (MaterialInfo mat : MATS_BY_NAME.values()) {
            CondensateStack[] condensate = { new CondensateStack(mat.getMaterial(), 1) };

            Fluid output = mat.getOutputFluid();

            if (output != null) {
                GTValues.RA.stdBuilder()
                    .itemInputs(CondensateStack.getPreviews(condensate))
                    .fluidOutputs(new FluidStack(output, 1))
                    .metadata(CONDENSATE_INPUTS, condensate)
                    .eut(EU_COST_PER_LITRE)
                    .duration(1)
                    .addTo(TecTechRecipeMaps.condensateLiquificationRecipes);
            }

            for (Fluid input : mat.getInputFluids()) {
                GTValues.RA.stdBuilder()
                    .fluidInputs(new FluidStack(input, 1))
                    .itemOutputs(CondensateStack.getPreviews(condensate))
                    .metadata(CONDENSATE_OUTPUTS, condensate)
                    .eut(EU_COST_PER_LITRE)
                    .duration(1)
                    .addTo(TecTechRecipeMaps.condensateCreationFromFluidRecipes);
            }
        }
    }

    public static long getRecipeCost(GTRecipe recipe) {
        Long cost = recipe.getMetadata(CONDENSATE_EU_COST);

        return cost == null ? recipe.mEUt : cost.longValue();
    }

    public static @Nullable MaterialInfo findMaterialByName(String name) {
        return MATS_BY_NAME.get(name);
    }

    public static @Nullable Pair<OrePrefixes, MaterialInfo> findMaterialForStack(ItemStack stack) {
        Pair<OrePrefixes, MaterialInfo> mat = null;

        for (Pair<OrePrefixes, String> prefix : OrePrefixes.detectPrefix(stack)) {
            MaterialInfo prefixMat = MATS_BY_NAME.get(prefix.right());

            if (prefixMat == null) continue;

            // already have a material and we found another oredict name with a conflicting prefix material
            if (mat != null && prefixMat != mat.right()) return null;

            mat = Pair.of(prefix.left(), prefixMat);
        }

        return mat;
    }
}
