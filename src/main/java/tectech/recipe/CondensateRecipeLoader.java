package tectech.recipe;

import static gregtech.api.recipe.RecipeMaps.alloySmelterRecipes;
import static gregtech.api.recipe.RecipeMaps.arcFurnaceRecipes;
import static gregtech.api.recipe.RecipeMaps.fluidExtractionRecipes;
import static gregtech.api.recipe.RecipeMaps.fluidSolidifierRecipes;
import static tectech.recipe.TecTechRecipeMaps.condensateRecipes;

import java.util.HashMap;
import java.util.List;

import gregtech.api.enums.Materials;
import gregtech.api.objects.MaterialStack;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTUtility.ItemId;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class CondensateRecipeLoader {

    public static void run() {
        
        HashMap<ItemId, MaterialStack> materials = new HashMap<>();

        // fluidExtractionRecipes.getAllRecipes()

    }

    private static class Tuple<S, T> {
        public S first;
        public T second;
        
        public Tuple() {
        }

        public Tuple(S first, T second) {
            this.first = first;
            this.second = second;
        }
    }

}
