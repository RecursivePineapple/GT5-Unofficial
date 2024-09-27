package gregtech.api.enums;

import static gregtech.api.enums.GTValues.L;
import static gregtech.api.enums.GTValues.M;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import gregtech.api.enums.MaterialBuilder2.EBFGas;
import gregtech.api.enums.MaterialBuilder2.Flags;
import gregtech.api.recipe.RecipeCategories;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTRecipeBuilder;
import gregtech.api.util.GTRecipeConstants;
import gregtech.api.util.GTUtility;
import gtPlusPlus.api.recipe.GTPPRecipeMaps;
import gtPlusPlus.core.util.math.MathUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.fluids.FluidStack;

public enum Material2RecipeGen {
    SmeltIngot((builder, adder) -> {
        if (builder.flags.has(Flags.GENERATE_DUST, Flags.GENERATE_INGOT, Flags.GEN_RECIPE_SMELT_INGOT)) {
            FurnaceRecipes.smelting().func_151394_a(
                builder.getMaterial().getItem(OrePrefixes.dust, 1),
                builder.getMaterial().getItem(OrePrefixes.ingot, 1),
                0);
        }
    }),

    SmeltGem((builder, adder) -> {
        if (builder.flags.has(Flags.GENERATE_DUST, Flags.GENERATE_GEM_NORMAL, Flags.GEN_RECIPE_SMELT_GEM)) {
            FurnaceRecipes.smelting().func_151394_a(
                builder.getMaterial().getItem(OrePrefixes.dust, 1),
                builder.getMaterial().getItem(OrePrefixes.gem, 1),
                0);
        }
    }),

    SmeltToNuggets(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_NUGGET,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.alloySmelterRecipes,
        ItemList.Shape_Mold_Nugget),
    SmeltFromNuggets(
        Flags.GENERATE_NUGGET,
        Flags.GENERATE_INGOT,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.alloySmelterRecipes,
        ItemList.Shape_Mold_Ingot),
    
    ExtrudeSmallGear(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_GEAR_SMALL,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Small_Gear),
    MoldSmallGear(
        Flags.GENERATE_MOLTEN,
        Flags.GENERATE_GEAR_SMALL,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.fluidSolidifierRecipes,
        ItemList.Shape_Mold_Gear_Small),
    CraftSmallGear(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_GEAR_SMALL,
        new Object[] {
            " R ",
            "hPw",
            " R ",
            'R', Flags.GENERATE_ROD,
            'P', Flags.GENERATE_PLATE,
        }),
    
    ExtrudeRotor(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_ROTOR,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.extruderRecipes,
        material -> {
            return GTValues.RA.stdBuilder()
                .itemInputs(
                    material.getMaterial().getItem(OrePrefixes.ingot, 5),
                        ItemList.Shape_Extruder_Rotor.get(0))
                .itemOutputs(
                    material.getMaterial().getItem(OrePrefixes.rotor, 1))
                .eut(24)
                .duration(20);
        }),
    MoldRotor(
        Flags.GENERATE_MOLTEN,
        Flags.GENERATE_ROTOR,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.fluidSolidifierRecipes,
        ItemList.Shape_Mold_Rotor),
    CraftRotor(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_ROTOR,
        new Object[] {
            "PhP",
            "SRf",
            "PsP",
            'P', Flags.GENERATE_PLATE,
            'R', Flags.GENERATE_RING,
            'S', Flags.GENERATE_SCREW,
        }),

    ExtrudeRing(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_RING,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Ring),
    MoldRing(
        Flags.GENERATE_MOLTEN,
        Flags.GENERATE_RING,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.fluidSolidifierRecipes,
        ItemList.Shape_Mold_Ring),
    CraftRing(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_RING,
        new Object[] {
            "h  ",
            "fR ",
            "   ",
            'R', Flags.GENERATE_ROD,
        }),
        
    ExtrudeBolt(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_BOLT,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Bolt),
    MoldBolt(
        Flags.GENERATE_MOLTEN,
        Flags.GENERATE_BOLT,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.fluidSolidifierRecipes,
        ItemList.Shape_Mold_Bolt),
    CraftBolt(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_BOLT,
        2,
        new Object[] {
            "S  ",
            " R ",
            "   ",
            'R', Flags.GENERATE_ROD,
            'S', ToolDictNames.craftingToolSaw.name(),
        }),
        
    LatheScrew(
        Flags.GENERATE_BOLT,
        Flags.GENERATE_SCREW,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.latheRecipes,
        material -> {
            return GTValues.RA.stdBuilder()
                .itemInputs(
                    material.getMaterial().getItem(OrePrefixes.bolt, 1))
                .itemOutputs(
                    material.getMaterial().getItem(OrePrefixes.screw, 1))
                .eut(24)
                .duration(20);
        }),
    MoldScrew(
        Flags.GENERATE_MOLTEN,
        Flags.GENERATE_SCREW,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.fluidSolidifierRecipes,
        ItemList.Shape_Mold_Screw),
    CraftScrew(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_SCREW,
        new Object[] {
            "fB ",
            "B  ",
            "   ",
            'B', Flags.GENERATE_BOLT,
        }),
        
    LatheRound(
        Flags.GENERATE_NUGGET,
        Flags.GENERATE_ROUND,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.latheRecipes),
    MoldRound(
        Flags.GENERATE_MOLTEN,
        Flags.GENERATE_ROUND,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.fluidSolidifierRecipes,
        ItemList.Shape_Mold_Round),
    CraftRound1(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_BOLT,
        new Object[] {
            "fN ",
            "Nh ",
            "   ",
            'N', Flags.GENERATE_NUGGET,
        }),
    CraftRound2(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_BOLT,
        4,
        new Object[] {
            "fIh",
            "   ",
            "   ",
            'I', Flags.GENERATE_INGOT,
        }),
        
    LatheRodMetal(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_ROD,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.latheRecipes,
        material -> {
            return GTValues.RA.stdBuilder()
                .itemInputs(
                    material.getMaterial().getItem(OrePrefixes.ingot, 1))
                .itemOutputs(
                    material.getMaterial().getItem(OrePrefixes.stick, 1),
                    material.getMaterial().getItem(OrePrefixes.dustSmall, 2))
                .eut(24)
                .duration(20);
        }),
    LatheRodGem(
        Flags.GENERATE_GEM_NORMAL,
        Flags.GENERATE_ROD,
        Flags.GEN_RECIPE_GEM_PARTS,
        RecipeMaps.latheRecipes,
        material -> {
            return GTValues.RA.stdBuilder()
                .itemInputs(
                    material.getMaterial().getItem(OrePrefixes.gem, 1))
                .itemOutputs(
                    material.getMaterial().getItem(OrePrefixes.stick, 1),
                    material.getMaterial().getItem(OrePrefixes.dustSmall, 2))
                .eut(24)
                .duration(20);
        }),
    MoldRod(
        Flags.GENERATE_MOLTEN,
        Flags.GENERATE_ROD,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.fluidSolidifierRecipes,
        ItemList.Shape_Mold_Rod),
    ExtrudeRod(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_ROD,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Rod),
    CutRod(
        Flags.GENERATE_ROD_LARGE,
        Flags.GENERATE_ROD,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.cutterRecipes,
        material -> {
            return GTValues.RA.stdBuilder()
                .itemInputs(material.getMaterial().getItem(OrePrefixes.stickLong, 1))
                .itemOutputs(material.getMaterial().getItem(OrePrefixes.stick, 2))
                .eut(24)
                .duration(20);
        }),
    CraftRod1(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_ROD,
        new Object[] {
            "f  ",
            " I ",
            "   ",
            'I', Flags.GENERATE_INGOT,
        }),
    CraftRod2(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_ROD,
        2,
        new Object[] {
            "S  ",
            "L  ",
            "   ",
            'S', ToolDictNames.craftingToolSaw.name(),
            'L', Flags.GENERATE_ROD_LARGE,
        }),
        
    ExtrudeGear(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_GEAR_NORMAL,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Gear),
    MoldGear(
        Flags.GENERATE_MOLTEN,
        Flags.GENERATE_GEAR_NORMAL,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.fluidSolidifierRecipes,
        ItemList.Shape_Mold_Gear),
    AlloyGear(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_GEAR_NORMAL,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.alloySmelterRecipes,
        material -> {
            return GTValues.RA.stdBuilder()
                .itemInputs(
                    material.getMaterial().getItem(OrePrefixes.ingot, 8),
                    ItemList.Shape_Mold_Gear.get(0))
                .itemOutputs(material.getMaterial().getItem(OrePrefixes.gearGt, 1))
                .eut(24)
                .duration(20);
        }),
    CraftGear(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_GEAR_NORMAL,
        new Object[] {
            "RPR",
            "PwP",
            "RPR",
            'P', Flags.GENERATE_PLATE,
            'R', Flags.GENERATE_ROD,
        }),
    
    SmeltCasing(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_CASING,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.alloySmelterRecipes,
        material -> {
            return GTValues.RA.stdBuilder()
                .itemInputs(
                    material.getMaterial().getItem(OrePrefixes.ingot, 2),
                    ItemList.Shape_Mold_Casing.get(0))
                .itemOutputs(material.getMaterial().getItem(OrePrefixes.itemCasing, 3))
                .eut(24)
                .duration(20);
        }),
    MoldCasing(
        Flags.GENERATE_MOLTEN,
        Flags.GENERATE_CASING,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.fluidSolidifierRecipes,
        ItemList.Shape_Mold_Casing),
    ExtrudeCasing(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_CASING,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Casing),
    CutCasing(
        Flags.GENERATE_PLATE,
        Flags.GENERATE_CASING,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.cutterRecipes),
    CraftCasing(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_CASING,
        new Object[] {
            "h P",
            "   ",
            "   ",
            'P', Flags.GENERATE_PLATE,
        }),

    MillFineWireFromIngot(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_FINE_WIRE,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.wiremillRecipes,
        3),
    MillFineWireFromRod(
        Flags.GENERATE_ROD,
        Flags.GENERATE_FINE_WIRE,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.wiremillRecipes,
        3),
    CraftFineWire(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_FINE_WIRE,
        new Object[] {
            "Ft ",
            "   ",
            "   ",
            'F', Flags.GENERATE_FOIL,
        }),

    MillSmallSpring(
        Flags.GENERATE_ROD,
        Flags.GENERATE_SPRING_SMALL,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.wiremillRecipes,
        1),
    CraftSmallSpring(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_SPRING_SMALL,
        new Object[] {
            " S ",
            "fRt",
            "   ",
            'R', Flags.GENERATE_ROD,
            'S', ToolDictNames.craftingToolSaw.name(),
        }),
    
    MillSpring(
        Flags.GENERATE_ROD_LARGE,
        Flags.GENERATE_SPRING_NORMAL,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.wiremillRecipes,
        1),
    CraftSpring(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_SPRING_NORMAL,
        new Object[] {
            " S ",
            "fRt",
            " R ",
            'R', Flags.GENERATE_ROD_LARGE,
            'S', ToolDictNames.craftingToolSaw.name(),
        }),
        
    HammerLongRod(
        Flags.GENERATE_ROD,
        Flags.GENERATE_ROD_LARGE,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.hammerRecipes),
    MoldLongRod(
        Flags.GENERATE_MOLTEN,
        Flags.GENERATE_ROD_LARGE,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.fluidSolidifierRecipes,
        ItemList.Shape_Mold_Rod_Long),
    CraftLongRod(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_ROD_LARGE,
        new Object[] {
            "RhR",
            "   ",
            "   ",
            'R', Flags.GENERATE_ROD,
        }),
    LatheLongRod(
        Flags.GENERATE_GEM_FLAWLESS,
        Flags.GENERATE_ROD_LARGE,
        Flags.GEN_RECIPE_GEM_PARTS,
        RecipeMaps.latheRecipes,
        material -> {
            return GTValues.RA.stdBuilder()
                .itemInputs(material.getMaterial().getItem(OrePrefixes.gemFlawless, 1))
                .itemOutputs(
                    material.getMaterial().getItem(OrePrefixes.stickLong, 1),
                    material.getMaterial().getItem(OrePrefixes.dust, 1))
                .eut(24)
                .duration(20);
        }),
    
    BendFoilFromIngot(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_FOIL,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.benderRecipes,
        10),
    BendFoilFromPlate(
        Flags.GENERATE_PLATE,
        Flags.GENERATE_FOIL,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.benderRecipes,
        1),
    CraftFoil(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_FOIL,
        2,
        new Object[] {
            "hP ",
            "   ",
            "   ",
            'P', Flags.GENERATE_PLATE,
        }),

    ExtrudeBlock(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_BLOCK,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Block),
    MoldBlock(
        Flags.GENERATE_MOLTEN,
        Flags.GENERATE_BLOCK,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.fluidSolidifierRecipes,
        ItemList.Shape_Mold_Block),
    CompressBlock(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_BLOCK,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.compressorRecipes),
    CutBlock(
        Flags.GENERATE_BLOCK,
        Flags.GENERATE_PLATE,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.cutterRecipes),
    AlloyBlock(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_BLOCK,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.alloySmelterRecipes,
        material -> {
            return GTValues.RA.stdBuilder()
                .itemInputs(
                    material.getMaterial().getItem(OrePrefixes.ingot, 9),
                    ItemList.Shape_Mold_Block.get(0))
                .itemOutputs(material.getMaterial().getItem(OrePrefixes.block, 1))
                .eut(24)
                .duration(20);
        }),

    AssembleFrame(
        Flags.GENERATE_ROD,
        Flags.GENERATE_FRAME,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.assemblerRecipes,
        4),
    CraftFrame(
        Flags.GEN_RECIPE_METAL_PARTS_HAND,
        Flags.GENERATE_FRAME,
        2,
        new Object[] {
            "RRR",
            "RwR",
            "RRR",
            'R', Flags.GENERATE_ROD,
        }),

    BendPlate1FromIngot(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_PLATE,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.benderRecipes,
        1),
    BendPlate2FromIngot(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_PLATE2,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.benderRecipes,
        2),
    BendPlate3FromIngot(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_PLATE3,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.benderRecipes,
        3),
    BendPlate4FromIngot(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_PLATE4,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.benderRecipes,
        4),
    BendPlate5FromIngot(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_PLATE5,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.benderRecipes,
        5),
    BendPlate9FromIngot(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_PLATE9,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.benderRecipes,
        9),

    BendPlate2FromPlate(
        Flags.GENERATE_PLATE,
        Flags.GENERATE_PLATE2,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.benderRecipes,
        2),
    BendPlate3FromPlate(
        Flags.GENERATE_PLATE,
        Flags.GENERATE_PLATE3,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.benderRecipes,
        3),
    BendPlate4FromPlate(
        Flags.GENERATE_PLATE,
        Flags.GENERATE_PLATE4,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.benderRecipes,
        4),
    BendPlate5FromPlate(
        Flags.GENERATE_PLATE,
        Flags.GENERATE_PLATE5,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.benderRecipes,
        5),
    BendPlate9FromPlate(
        Flags.GENERATE_PLATE,
        Flags.GENERATE_PLATE9,
        Flags.GEN_RECIPE_METAL_PARTS,
        RecipeMaps.benderRecipes,
        9),

    ItemPipe1(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_PIPE_TINY_ITEM,
        Flags.GEN_RECIPE_ITEM_PIPES,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Pipe_Tiny),
    ItemPipe2(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_PIPE_SMALL_ITEM,
        Flags.GEN_RECIPE_ITEM_PIPES,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Pipe_Small),
    ItemPipe3(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_PIPE_NORMAL_ITEM,
        Flags.GEN_RECIPE_ITEM_PIPES,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Pipe_Medium),
    ItemPipe4(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_PIPE_LARGE_ITEM,
        Flags.GEN_RECIPE_ITEM_PIPES,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Pipe_Large),
    ItemPipe5(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_PIPE_HUGE_ITEM,
        Flags.GEN_RECIPE_ITEM_PIPES,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Pipe_Huge),
    
    FluidPipe1(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_PIPE_TINY_FLUID,
        Flags.GEN_RECIPE_ITEM_PIPES,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Pipe_Tiny),
    FluidPipe2(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_PIPE_SMALL_FLUID,
        Flags.GEN_RECIPE_ITEM_PIPES,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Pipe_Small),
    FluidPipe3(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_PIPE_NORMAL_FLUID,
        Flags.GEN_RECIPE_ITEM_PIPES,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Pipe_Medium),
    FluidPipe4(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_PIPE_LARGE_FLUID,
        Flags.GEN_RECIPE_ITEM_PIPES,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Pipe_Large),
    FluidPipe5(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_PIPE_HUGE_FLUID,
        Flags.GEN_RECIPE_ITEM_PIPES,
        RecipeMaps.extruderRecipes,
        ItemList.Shape_Extruder_Pipe_Huge),
    
    Wire1FromIngot(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_WIRE_1,
        Flags.GEN_RECIPE_WIRES,
        RecipeMaps.wiremillRecipes,
        1),
    Wire2FromIngot(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_WIRE_2,
        Flags.GEN_RECIPE_WIRES,
        RecipeMaps.wiremillRecipes,
        2),
    Wire4FromIngot(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_WIRE_4,
        Flags.GEN_RECIPE_WIRES,
        RecipeMaps.wiremillRecipes,
        4),
    Wire8FromIngot(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_WIRE_8,
        Flags.GEN_RECIPE_WIRES,
        RecipeMaps.wiremillRecipes,
        8),
    Wire12FromIngot(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_WIRE_12,
        Flags.GEN_RECIPE_WIRES,
        RecipeMaps.wiremillRecipes,
        12),
    Wire16FromIngot(
        Flags.GENERATE_INGOT,
        Flags.GENERATE_WIRE_16,
        Flags.GEN_RECIPE_WIRES,
        RecipeMaps.wiremillRecipes,
        16),
    
    Wire1FromRod(
        Flags.GENERATE_ROD,
        Flags.GENERATE_WIRE_1,
        Flags.GEN_RECIPE_WIRES,
        RecipeMaps.wiremillRecipes,
        1),
    Wire2FromRod(
        Flags.GENERATE_ROD,
        Flags.GENERATE_WIRE_2,
        Flags.GEN_RECIPE_WIRES,
        RecipeMaps.wiremillRecipes,
        2),
    Wire4FromRod(
        Flags.GENERATE_ROD,
        Flags.GENERATE_WIRE_4,
        Flags.GEN_RECIPE_WIRES,
        RecipeMaps.wiremillRecipes,
        4),
    Wire8FromRod(
        Flags.GENERATE_ROD,
        Flags.GENERATE_WIRE_8,
        Flags.GEN_RECIPE_WIRES,
        RecipeMaps.wiremillRecipes,
        8),
    Wire12FromRod(
        Flags.GENERATE_ROD,
        Flags.GENERATE_WIRE_12,
        Flags.GEN_RECIPE_WIRES,
        RecipeMaps.wiremillRecipes,
        12),
    Wire16FromRod(
        Flags.GENERATE_ROD,
        Flags.GENERATE_WIRE_16,
        Flags.GEN_RECIPE_WIRES,
        RecipeMaps.wiremillRecipes,
        16),

    Cable1Complex((builder, adder) -> {
        if (builder.flags.has(Flags.GENERATE_WIRE_1, Flags.GENERATE_CABLE_1, Flags.GEN_RECIPE_COMPLEX_CABLES)) {
            ItemStack phenylene = GTOreDictUnificator.get(OrePrefixes.foil, Materials.PolyphenyleneSulfide, 1);
            ItemStack foil = builder.getMaterial().getItem(OrePrefixes.foil, 1);
            ItemStack siliconeDust = GTOreDictUnificator.get(OrePrefixes.dustSmall, Materials.Polydimethylsiloxane, 1);
            ItemStack pvcDust = GTOreDictUnificator.get(OrePrefixes.dustSmall, Materials.PolyvinylChloride, 1);
            ItemStack wire = builder.getMaterial().getItem(OrePrefixes.wireGt01, 1);
            ItemStack cable = builder.getMaterial().getItem(OrePrefixes.cableGt01, 1);

            FluidStack silicone = Materials.Silicone.getMolten(72);
            FluidStack sb = Materials.StyreneButadieneRubber.getMolten(108);

            Optional<GTRecipe.GTRecipe_WithAlt> recipe = GTValues.RA.stdBuilder()
                .itemInputs(new Object[] {
                    new ItemStack[] {phenylene, foil},
                    new ItemStack[] {siliconeDust, pvcDust},
                    wire
                })
                .fluidInputs(silicone)
                .itemOutputs(cable)
                .eut(24)
                .duration(20)
                .fake()
                .forceOreDictInput()
                .apply(r -> {
                    builder.doRecipeModify(RecipeMaps.assemblerRecipes, Flags.GENERATE_WIRE_1, Flags.GENERATE_CABLE_1, r);
                })
                .buildWithAlt();

            RecipeMaps.assemblerRecipes.add(recipe.get());

            recipe = GTValues.RA.stdBuilder()
                .itemInputs(new Object[] {
                    new ItemStack[] {phenylene, foil},
                    new ItemStack[] {siliconeDust, pvcDust},
                    wire
                })
                .fluidInputs(sb)
                .itemOutputs(cable)
                .eut(24)
                .duration(20)
                .fake()
                .forceOreDictInput()
                .apply(r -> {
                    builder.doRecipeModify(RecipeMaps.assemblerRecipes, Flags.GENERATE_WIRE_1, Flags.GENERATE_CABLE_1, r);
                })
                .buildWithAlt();

            RecipeMaps.assemblerRecipes.add(recipe.get());

            for (int i = 0; i < 8; i++) {
                adder.accept(
                    RecipeMaps.assemblerRecipes,
                    Flags.GENERATE_WIRE_1,
                    Flags.GENERATE_CABLE_1,
                    GTValues.RA.stdBuilder()
                        .itemInputs(new Object[] {
                            (i & 0b1) != 0 ? phenylene : foil,
                            (i & 0b10) != 0 ? siliconeDust : pvcDust,
                            wire
                        })
                        .fluidInputs((i & 0b100) != 0 ? silicone : sb)
                        .itemOutputs(cable)
                        .eut(24)
                        .duration(20)
                        .hidden());
            }
        }
    }),

    MacerateRecycle((builder, adder) -> {
        if (builder.flags.has(Flags.GEN_RECIPE_MACERATOR_RECYCLE, Flags.GENERATE_DUST)) {
            for (Flags flag : Flags.RECYCLEABLE) {
                if (flag == Flags.GENERATE_INGOT) {
                    continue;
                }

                if (flag.getPrefix() instanceof OrePrefixes input) {

                    long amount = input.mMaterialAmount;

                    ItemStack output = null;
                    if (amount % M == 0 || amount >= M * 16) {
                        output = builder.getMaterial().getItem(OrePrefixes.dust, (int) (amount / M));
                    }
                    if (output == null && ((amount * 4) % M == 0 || amount >= M * 8)) {
                        output = builder.getMaterial().getItem(OrePrefixes.dustSmall, (int) ((amount * 4 / M)));
                    }
                    if (output == null && (amount * 9) >= M) {
                        output = builder.getMaterial().getItem(OrePrefixes.dustTiny, (int) ((amount * 9) / M));
                    }

                    if (output != null) {
                        adder.accept(
                            RecipeMaps.maceratorRecipes,
                            flag,
                            Flags.GENERATE_DUST,
                            GTValues.RA.stdBuilder()
                                .itemInputs(builder.getMaterial().getItem(input, 1))
                                .itemOutputs(output)
                                .eut(24)
                                .duration(20)
                                .recipeCategory(RecipeCategories.maceratorRecycling));
                    }
                }
            }
        }
    }),

    FluidExtractorRecycle((builder, adder) -> {
        if (builder.flags.has(Flags.GEN_RECIPE_FLUID_EXTRACTOR_RECYCLE)) {
            if (!builder.flags.hasAny(Flags.GENERATE_MOLTEN, Flags.GENERATE_FLUID)) {
                return;
            }

            for (Flags flag : Flags.RECYCLEABLE) {
                if (flag.getPrefix() instanceof OrePrefixes input) {

                    int amount = (int) (input.mMaterialAmount * 144 / M);

                    FluidStack output = builder.getMaterial().getFluid(FluidType.Molten, amount);

                    if (output == null) {
                        output = builder.getMaterial().getFluid(FluidType.Fluid, amount);
                    }

                    if (output != null) {
                        adder.accept(
                            RecipeMaps.fluidExtractionRecipes,
                            flag,
                            Flags.GENERATE_MOLTEN,
                            GTValues.RA.stdBuilder()
                                .itemInputs(builder.getMaterial().getItem(input, 1))
                                .fluidOutputs(output)
                                .eut(24)
                                .duration(20)
                                .recipeCategory(RecipeCategories.fluidExtractorRecycling));
                    }
                }
            }
        }
    }),

    EBF((builder, adder) -> {
        if (builder.hasFlag(Flags.GEN_RECIPE_EBF)) {
            List<EBFGas[]> gasses = new ArrayList<>();

            if (builder.hasFlag(Flags.EBF_USE_NITROGEN_SERIES)) {
                gasses.add(EBFGas.NITROGEN_SERIES.get());
            }
            if (builder.hasFlag(Flags.EBF_USE_NOBLE_GAS_SERIES)) {
                gasses.add(EBFGas.NOBLE_GAS_SERIES.get());
            }
            if (builder.hasFlag(Flags.EBF_USE_HYDROGEN)) {
                gasses.add(EBFGas.HYDROGEN_SERIES.get());
            }
            if (builder.hasFlag(Flags.EBF_USE_OXYGEN)) {
                gasses.add(EBFGas.OXYGEN_SERIES.get());
            }
            if (builder.customEBFGasses != null) {
                gasses.add(builder.customEBFGasses.toArray(new EBFGas[builder.customEBFGasses.size()]));
            }

            EBFGas[] allGasses = null;

            if (gasses.size() == 1) {
                allGasses = gasses.get(0);
            } else if (gasses.size() > 1) {
                allGasses = GTUtility.concat(gasses.toArray(new EBFGas[gasses.size()][]));
            }

            if (allGasses == null) {
                allGasses = EBFGas.NO_GASSES.get();
            }

            ItemStack dust = builder.getMaterial().getItem(OrePrefixes.dust, 1);

            for (EBFGas gas : allGasses) {
                adder.accept(
                    RecipeMaps.blastFurnaceRecipes,
                    Flags.GENERATE_DUST,
                    builder.hasFlag(Flags.EBF_MAKE_HOT_INGOTS) ? Flags.GENERATE_INGOT_HOT : Flags.GENERATE_INGOT,
                    GTValues.RA.stdBuilder()
                        .apply(recipe -> {
                            if (gas.circuit > 0) {
                                recipe.itemInputs(dust, ItemList.Circuit_Integrated.getWithDamage(0, gas.circuit));
                            } else {
                                recipe.itemInputs(dust);
                            }
                            
                            if (gas.gas != null) {
                                recipe.fluidInputs(gas.gas.toFluidStack(FluidType.Gas));
                            }

                            if (builder.hasFlag(Flags.EBF_MAKE_HOT_INGOTS)) {
                                recipe.itemOutputs(builder.getMaterial().getItem(OrePrefixes.ingotHot, 1));
                            } else {
                                recipe.itemOutputs(builder.getMaterial().getItem(OrePrefixes.ingot, 1));
                            }
                        })
                        .metadata(GTRecipeConstants.COIL_HEAT, builder.ebfHeat)
                        .eut(builder.ebfVoltage)
                        .duration(gas.timeReduction.apply(builder.ebfBaseTime)));
            }
        }
    }),

    ABSFromContents((builder, adder) -> {
        if (builder.flags.has(Flags.GEN_RECIPE_ABS_FROM_CONTENTS, Flags.GENERATE_MOLTEN)) {
            MaterialStack2[] contents = builder.contents;

            int molar = 0;
            ItemStack[] inputs = new ItemStack[contents.length + (builder.absCircuit > 0 ? 1 : 0)];

            for (int i = 0; i < contents.length; i++) {
                MaterialStack2 content = contents[i];
                inputs[i] = content.toItemStack(OrePrefixes.dust);
                molar += content.getItemCount(OrePrefixes.dust);
            }

            if (builder.absCircuit > 0) {
                inputs[inputs.length - 1] = ItemList.Circuit_Integrated.getWithDamage(0, builder.absCircuit);
            }

            adder.accept(
                GTPPRecipeMaps.alloyBlastSmelterRecipes,
                Flags.CONTENTS,
                Flags.GENERATE_MOLTEN,
                GTValues.RA.stdBuilder()
                    .itemInputs(inputs)
                    .fluidOutputs(builder.getMaterial().getFluid(FluidType.Molten, molar * L))
                    .eut(builder.absVoltage)
                    .duration(builder.absTime));
        }
    }),

    ABSFromDust((builder, adder) -> {
        if (builder.flags.has(Flags.GEN_RECIPE_ABS_FROM_DUST, Flags.GENERATE_DUST, Flags.GENERATE_MOLTEN)) {
            adder.accept(
                GTPPRecipeMaps.alloyBlastSmelterRecipes,
                Flags.GENERATE_DUST,
                Flags.GENERATE_MOLTEN,
                GTValues.RA.stdBuilder()
                    .itemInputs(builder.getMaterial().getItem(OrePrefixes.dust, 1))
                    .fluidOutputs(builder.getMaterial().getFluid(FluidType.Molten, L))
                    .eut(builder.absVoltage)
                    .duration(builder.absTime));
        }
    }),

    Mix((builder, adder) -> {
        if (builder.hasFlag(Flags.GEN_RECIPE_MIXER)) {
            MaterialStack2[] contents = builder.contents;

            int molar = 0;
            ItemStack[] inputs = new ItemStack[contents.length + (builder.mixerCircuit > 0 ? 1 : 0)];

            for (int i = 0; i < contents.length; i++) {
                MaterialStack2 content = contents[i];
                inputs[i] = content.toItemStack(OrePrefixes.dust);
                molar += content.getItemCount(OrePrefixes.dust);
            }

            if (builder.mixerCircuit > 0) {
                inputs[inputs.length - 1] = ItemList.Circuit_Integrated.getWithDamage(0, builder.mixerCircuit);
            }

            adder.accept(
                RecipeMaps.mixerRecipes,
                Flags.CONTENTS,
                Flags.GENERATE_DUST,
                GTValues.RA.stdBuilder()
                    .itemInputs(inputs)
                    .itemOutputs(builder.getMaterial().getItem(OrePrefixes.dust, molar))
                    .eut(builder.mixerVoltage)
                    .duration(builder.mixerTime));
        }
    }),

    CentrifugeRecycle((builder, adder) -> {
        if (builder.hasFlag(Flags.GEN_RECIPE_CENTRIFUGE_RECYCLE)) {
            MaterialStack2[] contents = builder.contents;

            int molar = 0;
            ItemStack[] outputs = new ItemStack[contents.length];

            for (int i = 0; i < contents.length; i++) {
                MaterialStack2 content = contents[i];
                outputs[i] = content.toItemStack(OrePrefixes.dust);
                molar += content.getItemCount(OrePrefixes.dust);
            }

            GTValues.RA.stdBuilder()
                .itemInputs(builder.getMaterial().getItem(OrePrefixes.dust, molar))
                .itemOutputs(outputs)
                .eut(builder.centrifugeVoltage)
                .duration(builder.centrifugeTime)
                .addTo(RecipeMaps.centrifugeRecipes);
        }
    }),

    ElectrolyzerRecycle((builder, adder) -> {
        if (builder.hasFlag(Flags.GEN_RECIPE_ELECTROLYZER_RECYCLE)) {
            MaterialStack2[] contents = builder.contents;

            int molar = 0;
            ItemStack[] outputs = new ItemStack[contents.length];

            for (int i = 0; i < contents.length; i++) {
                MaterialStack2 content = contents[i];
                outputs[i] = content.toItemStack(OrePrefixes.dust);
                molar += content.getItemCount(OrePrefixes.dust);
            }

            adder.accept(
                RecipeMaps.electrolyzerRecipes,
                Flags.GENERATE_DUST,
                Flags.CONTENTS,
                GTValues.RA.stdBuilder()
                    .itemInputs(builder.getMaterial().getItem(OrePrefixes.dust, molar))
                    .itemOutputs(outputs)
                    .eut(builder.electrolyzerVoltage)
                    .duration(builder.electrolyzerTime));
        }
    }),

    CoolIngot(
        Flags.GENERATE_INGOT_HOT,
        Flags.GENERATE_INGOT,
        Flags.GEN_RECIPE_VACUUM_FREEZER_HOT_INGOT,
        RecipeMaps.vacuumFreezerRecipes),

    ;

    private static interface IRecipeAdder {
        public void accept(RecipeMap<?> recipeMap, Flags mainInput, Flags mainOutput, GTRecipeBuilder recipe);
    }

    private final Consumer<MaterialBuilder2> builder;

    private static final Material2RecipeGen[] RECIPES = values();

    private Material2RecipeGen(BiConsumer<MaterialBuilder2, IRecipeAdder> complex) {
        this.builder = builder -> {
            complex.accept(builder, (recipeMap, input, output, recipe) -> {
                builder.doRecipeModify(recipeMap, input, output, recipe);
                recipe.addTo(recipeMap);
            });
        };
    }

    private Material2RecipeGen(Flags inputFlag, Flags outputFlag, Flags recipeFlag, RecipeMap<?> recipeMap, Function<MaterialBuilder2, GTRecipeBuilder> recipe) {
        this.builder = builder -> {
            if (builder.flags.has(inputFlag, outputFlag, recipeFlag)) {
                GTRecipeBuilder recipeBuilder = recipe.apply(builder);
                builder.doRecipeModify(recipeMap, inputFlag, outputFlag, recipeBuilder);
                recipeBuilder.addTo(recipeMap);
            }
        };
    }

    private Material2RecipeGen(Flags inputFlag, Flags outputFlag, Flags recipeFlag, RecipeMap<?> recipeMap, Supplier<ItemStack> catalystSupplier) {
        this.builder = builder -> {
            if (builder.flags.has(inputFlag, outputFlag, recipeFlag)) {
                ItemStack[] catalyst = catalystSupplier == null ? new ItemStack[0] : new ItemStack[] { catalystSupplier.get() };

                Object input = inputFlag.getPrefix();
                Object output = outputFlag.getPrefix();
                GTRecipeBuilder recipeBuilder;

                if (input instanceof OrePrefixes inputPrefix && output instanceof OrePrefixes outputPrefix) {
                    recipeBuilder = generateConversion(
                        builder,
                        inputPrefix,
                        outputPrefix,
                        catalyst);    
                } else if (input instanceof FluidType inputFluid && output instanceof OrePrefixes outputPrefix) {
                    recipeBuilder = generateConversion(
                        builder,
                        inputFluid,
                        outputPrefix,
                        catalyst);
                } else if (input instanceof OrePrefixes inputPrefix && output instanceof FluidType outputFluid) {
                    recipeBuilder = generateConversion(
                        builder,
                        inputPrefix,
                        outputFluid,
                        catalyst);
                } else {
                    throw new IllegalArgumentException();
                }

                builder.doRecipeModify(recipeMap, inputFlag, outputFlag, recipeBuilder);
                recipeBuilder.addTo(recipeMap);
            }
        };
    }

    private Material2RecipeGen(Flags recipeFlag, Flags outputFlag, Object[] inputs) {
        this(recipeFlag, outputFlag, 1, inputs);
    }

    private Material2RecipeGen(Flags recipeFlag, Flags outputFlag, int outputCount, Object[] inputs) {
        List<Flags> deps = new ArrayList<>();

        for (int i = 0; i < inputs.length; i++) {
            Object input = inputs[i];
            if (input instanceof Flags flag) {
                deps.add(flag);
            }
        }

        Flags[] depsArray = deps.toArray(new Flags[0]);

        // can't be a static field due to some weird initializer rule
        Object[] tools = new Object[] {
            'f', ToolDictNames.craftingToolFile.name(),
            'w', ToolDictNames.craftingToolWrench.name(),
            'c', ToolDictNames.craftingToolCrowbar.name(),
            't', ToolDictNames.craftingToolWireCutter.name(),
            'h', ToolDictNames.craftingToolHardHammer.name(),
            'm', ToolDictNames.craftingToolSoftHammer.name(),
            's', ToolDictNames.craftingToolScrewdriver.name(),
            'i', ToolDictNames.craftingToolSolderingIron.name(),
        };
    
        this.builder = builder -> {
            if (builder.flags.has(recipeFlag, outputFlag) && builder.flags.hasMany(depsArray)) {
                Object[] transformed = new Object[inputs.length];

                for (int i = 0; i < inputs.length; i++) {
                    Object input = inputs[i];
                    if (input instanceof Flags flag) {
                        transformed[i] = builder.getMaterial().getItem((OrePrefixes) flag.getPrefix(), 1);
                    } else {
                        transformed[i] = input;
                    }
                }
                
                GTModHandler.addCraftingRecipe(
                    builder.getMaterial().getItem((OrePrefixes) outputFlag.getPrefix(), outputCount),
                    0,
                    GTUtility.concat(transformed, tools));
            }
        };
    }

    private Material2RecipeGen(Flags inputFlag, Flags outputFlag, Flags recipeFlag, RecipeMap<?> recipeMap, ItemList catalyst) {
        this(inputFlag, outputFlag, recipeFlag, recipeMap, () -> catalyst.get(0));
    }

    private Material2RecipeGen(Flags inputFlag, Flags outputFlag, Flags recipeFlag, RecipeMap<?> recipeMap, int circuit) {
        this(inputFlag, outputFlag, recipeFlag, recipeMap, () -> ItemList.Circuit_Integrated.getWithDamage(0, circuit));
    }

    private Material2RecipeGen(Flags inputFlag, Flags outputFlag, Flags recipeFlag, RecipeMap<?> recipeMap) {
        this(inputFlag, outputFlag, recipeFlag, recipeMap, (Supplier<ItemStack>) null);
    }

    public static void generate(MaterialBuilder2 material) {
        for (Material2RecipeGen recipe : RECIPES) {
            recipe.builder.accept(material);
        }
    }

    private static GTRecipeBuilder generateConversion(MaterialBuilder2 builder, FluidType input, OrePrefixes output, ItemStack... catalysts) {
        int inputAmount = (int) (output.mMaterialAmount * L / M);
        int outputAmount = 1;

        return GTValues.RA.stdBuilder()
            .itemInputs(catalysts)
            .fluidInputs(builder.getMaterial().getFluid(input, inputAmount))
            .itemOutputs(builder.getMaterial().getItem(output, outputAmount))
            .eut(24)
            .duration(20);
    }

    private static GTRecipeBuilder generateConversion(MaterialBuilder2 builder, OrePrefixes input, FluidType output, ItemStack... catalysts) {
        int inputAmount = 1;
        int outputAmount = (int) (input.mMaterialAmount * L / M);

        ArrayList<ItemStack> inputs = new ArrayList<>(catalysts.length + 1);

        for (ItemStack catalyst : catalysts) inputs.add(catalyst);
        inputs.add(builder.getMaterial().getItem(input, inputAmount));

        return GTValues.RA.stdBuilder()
            .itemInputs(inputs.toArray(new ItemStack[inputs.size()]))
            .fluidOutputs(builder.getMaterial().getFluid(output, outputAmount))
            .eut(24)
            .duration(20);
    }

    private static GTRecipeBuilder generateConversion(MaterialBuilder2 builder, OrePrefixes input, OrePrefixes output, ItemStack... catalysts) {
        long gcd = MathUtils.gcd(input.mMaterialAmount, output.mMaterialAmount);
        int inputAmount = (int) (output.mMaterialAmount / gcd);
        int outputAmount = (int) (input.mMaterialAmount / gcd);

        ArrayList<ItemStack> inputs = new ArrayList<>(catalysts.length + 1);

        for (ItemStack catalyst : catalysts) inputs.add(catalyst);
        inputs.add(builder.getMaterial().getItem(input, inputAmount));

        return GTValues.RA.stdBuilder()
            .itemInputs(inputs.toArray(new ItemStack[inputs.size()]))
            .itemOutputs(builder.getMaterial().getItem(output, outputAmount))
            .eut(24)
            .duration(20);
    }
}
