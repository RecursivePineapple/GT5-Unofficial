package gregtech.api.enums;

import static gregtech.api.enums.GTValues.L;
import static gregtech.api.enums.GTValues.M;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import bartworks.system.material.WerkstoffLoader;
import gregtech.GTMod;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.util.GTRecipeBuilder;
import gregtech.api.util.GTUtility;
import gregtech.api.util.IntFraction;
import gregtech.api.util.Lazy;
import gregtech.api.util.GTRecipeConstants.FuelType;
import gregtech.common.items.ItemIntegratedCircuit;
import gregtech.common.render.items.GeneratedMaterialRenderer;
import gtPlusPlus.core.item.circuit.GTPPIntegratedCircuitItem;
import gtPlusPlus.xmod.gregtech.api.enums.GregtechItemList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class MaterialBuilder2 {
    
    public final int id;
    public final String name, defaultLocalName;

    public TextureSet textureSet = TextureSet.SET_DULL;
    public GeneratedMaterialRenderer renderer;
    
    public short[] colour = new short[] { 255, 255, 255, 255 };

    /** Used for lenses */
    public Dyes lensColour = Dyes._NULL;

    public OregenBuilder ore;
    public ToolBuilder tools;

    public FluidBuilder fluid, molten, gas, plasma;

    public BlockTexture blockTexture = BlockTexture.IRON_LIKE;
    public ResourceLocation customBlockTexture;

    public final ExtendedBitmask<Flags> flags = new ExtendedBitmask<>(Flags.class);

    /**
     * The first ID used for wire/cable MTEs. Wires will be +0 to +5, and cables will be +6 to +12.
     * Specifically, wires/cables are registered in this order (using ascending IDs): wire -> cable then 1x -> 16x.
     */
    public int wireStartID = 0;
    /** The amperage that a 1x cable can handle. */
    public int baseAmperage = 0;
    /** The voltage loss for a cable. */
    public int cableBaseLoss = 0;
    /** The voltage loss for a wire. */
    public int wireBaseLoss = 0;
    /** The max voltage that a wire or cable can handle. */
    public int maxVoltage = 0;
    /** Whether the bare wire shocks (should be false for superconductors). */
    public boolean wireShocks = true;

    /**
     * The first ID used for fluid pipe MTEs (+0 to +6).
     */
    public int fluidPipeStartID = 0;
    /** The amount of fluid that a normal pipe can hold (and transfer each second). */
    public int baseCapacity = 0;
    /** The max fluid temp that fluid pipes of this material can contain without catching on fire. */
    public int maxTemp = 0;
    /** Whether this material's fluid pipes can store gas or not. */
    public boolean gasProof = true;

    /**
     * The first ID used for item pipe MTEs (+0 to +4).
     */
    public int itemPipeStartID = 0;
    /** The number of stacks per second that normal item pipes of this material can transfer */
    public IntFraction pipeStacksPerSecond;

    /** The blast resistance blocks & machines of this material will have */
    public float blastResistance = 2;
    /** The hardness blocks & machines of this material will have */
    public int hardness = 1;
    /** The harvest level blocks & machines of this material will have */
    public int harvestLevel = 1;

    /** The type of generator this material can be put in */
    public @Nullable FuelType fuelType;
    /** The amount of EU generated per unit */
    public int fuelPower;

    public int processingMaterialTier = 0;

    public MaterialStack2[] contents;
    public String formula;

    public int mixerCircuit, mixerVoltage, mixerTime;
    public int centrifugeVoltage, centrifugeTime;
    public int electrolyzerVoltage, electrolyzerTime;
    public int alloySmelterVoltage, alloySmelterTime;
    public int absCircuit, absVoltage, absTime;
    public int ebfVoltage, ebfBaseTime, ebfHeat;
    public ArrayList<EBFGas> customEBFGasses;

    private ArrayList<IMaterialHandle> handles = new ArrayList<>(1);
    private Material2 material;

    private final Map<RecipeId, RecipeModification> recipeModifications = new HashMap<>();

    public static final ArrayList<MaterialBuilder2> BUILDERS = new ArrayList<>();

    public MaterialBuilder2(int id, String name, String defaultLocalName) {
        this(id, name, defaultLocalName, true);
    }

    public MaterialBuilder2(int id, String name, String defaultLocalName, boolean autoRegister) {
        this.id = id;
        this.name = name;
        this.defaultLocalName = defaultLocalName;
        
        if (autoRegister) {
            BUILDERS.add(this);
        }

        flags.addMany(Flags.STANDARD_RECIPES);
    }

    public MaterialBuilder2 setTextureSet(TextureSet textureSet) {
        this.textureSet = textureSet;
        return this;
    }

    public MaterialBuilder2 setColour(int r, int g, int b, int a) {
        setColour(new short[] { (short) r, (short) g, (short) b, (short) a });
        return this;
    }

    public MaterialBuilder2 setColour(short[] rgba) {
        colour = rgba;
        return this;
    }

    public MaterialBuilder2 setBlastResistance(float blastResistance) {
        this.blastResistance = blastResistance;
        return this;
    }

    public MaterialBuilder2 setHardness(int hardness) {
        this.hardness = hardness;
        return this;
    }

    public MaterialBuilder2 setHarvestLevel(int harvestLevel) {
        this.harvestLevel = harvestLevel;
        return this;
    }

    public MaterialBuilder2 addFlag(Flags first) {
        flags.add(first);
        return this;
    }

    public MaterialBuilder2 removeFlag(Flags first) {
        flags.remove(first);
        return this;
    }

    public boolean hasFlag(Flags first) {
        return flags.has(first);
    }

    public MaterialBuilder2 addIngot() {
        flags.add(Flags.GENERATE_INGOT);
        return this;
    }

    public MaterialBuilder2 addHotIngot() {
        flags.add(Flags.GENERATE_INGOT_HOT);
        return this;
    }

    public MaterialBuilder2 addBlock(BlockTexture texture) {
        flags.add(Flags.GENERATE_BLOCK);
        this.blockTexture = texture;
        return this;
    }

    public MaterialBuilder2 addBlock(ResourceLocation custom) {
        flags.add(Flags.GENERATE_BLOCK);
        this.customBlockTexture = custom;
        return this;
    }

    public static enum BlockTexture {
        DIAMOND_LIKE,
        REDSTONE_LIKE,
        EMERALD_LIKE,
        LAPIS_LIKE,
        IRON_LIKE;

        public ResourceLocation getBaseTexture(TextureSet set) {
            return set.getLocation(switch(this) {
                case DIAMOND_LIKE -> TextureSet.INDEX_block1;
                case REDSTONE_LIKE -> TextureSet.INDEX_block2;
                case EMERALD_LIKE -> TextureSet.INDEX_block3;
                case LAPIS_LIKE -> TextureSet.INDEX_block4;
                case IRON_LIKE -> TextureSet.INDEX_block5;
            });
        }
    }

    public MaterialBuilder2 addDusts() {
        flags.addMany(Flags.DUSTS);
        return this;
    }

    public MaterialBuilder2 addPlates() {
        flags.addMany(Flags.PLATES);
        return this;
    }

    public MaterialBuilder2 addMetalParts() {
        flags.addMany(Flags.METAL_PARTS);
        return this;
    }

    public MaterialBuilder2 addCells() {
        flags.addMany(Flags.CELLS);
        return this;
    }

    public MaterialBuilder2 addGems() {
        flags.addMany(Flags.GEMS);
        return this;
    }

    public FluidBuilder addFluid() {
        flags.add(Flags.GENERATE_FLUID);
        return fluid = new FluidBuilder(this)
            .setNames("fluid." + name.toLowerCase(), defaultLocalName)
            .setCellCapacity(1000)
            .setTextureName(this.textureSet.is_custom ? "fluid." + name.toLowerCase() : "autogenerated");
    }

    public FluidBuilder addMolten() {
        flags.add(Flags.GENERATE_MOLTEN);
        return molten = new FluidBuilder(this)
            .setNames("molten." + name.toLowerCase(), "Molten " + defaultLocalName)
            .setCellCapacity(144)
            .setTextureName(this.textureSet.is_custom ? "molten." + name.toLowerCase() : "molten.autogenerated");
    }

    public FluidBuilder addGas() {
        flags.add(Flags.GENERATE_GAS);
        return gas = new FluidBuilder(this)
            .setNames("gas." + name.toLowerCase(), defaultLocalName + " Gas")
            .setCellCapacity(1000)
            .setTextureName(this.textureSet.is_custom ? "gas." + name.toLowerCase() : "autogenerated");
    }

    public FluidBuilder addPlasma() {
        flags.add(Flags.GENERATE_PLASMA);
        return plasma = new FluidBuilder(this)
            .setNames("plasma." + name.toLowerCase(), defaultLocalName + " Plasma")
            .setCellCapacity(1000)
            .setTextureName(this.textureSet.is_custom ? "plasma." + name.toLowerCase() : "autogenerated")
            .setTemperature(10000);
    }

    public MaterialBuilder2 addLens(Dyes colour) {
        flags.add(Flags.GENERATE_LENS);
        this.lensColour = colour;
        return this;
    }

    public MaterialBuilder2 addWires(int startID, int voltage, int amperage, int wireLoss) {
        this.wireStartID = startID;
        this.maxVoltage = voltage;
        this.baseAmperage = amperage;
        this.wireBaseLoss = wireLoss;
        flags.addMany(Flags.WIRES);
        return this;
    }

    public MaterialBuilder2 addCables(int startID, int voltage, int amperage, int wireLoss, int cableLoss) {
        addWires(startID, voltage, amperage, wireLoss);
        this.cableBaseLoss = cableLoss;
        flags.addMany(Flags.CABLES);
        return this;
    }

    public MaterialBuilder2 addCables(int startID, int voltage, int amperage, int cableLoss) {
        addCables(startID, voltage, amperage, cableLoss * 2, cableLoss);
        return this;
    }

    public MaterialBuilder2 setWireShocks(boolean wireShocks) {
        this.wireShocks = wireShocks;
        return this;
    }

    public MaterialBuilder2 addFluidPipes(int startID, int baseCapacity, int maxTemp, boolean gasProof) {
        this.fluidPipeStartID = startID;
        this.baseCapacity = baseCapacity;
        this.maxTemp = maxTemp;
        this.gasProof = gasProof;
        flags.addMany(Flags.FLUID_PIPES);
        return this;
    }

    public MaterialBuilder2 addFluidPipes(int startID, int baseCapacity, int maxTemp) {
        addFluidPipes(startID, baseCapacity, maxTemp, true);
        return this;
    }

    public MaterialBuilder2 addItemPipes(int startID, IntFraction stacksPerSecond) {
        this.itemPipeStartID = startID;
        this.pipeStacksPerSecond = stacksPerSecond;
        flags.addMany(Flags.ITEM_PIPES);
        return this;
    }

    public MaterialBuilder2 addRestrictiveItemPipes() {
        flags.addMany(Flags.RESTRICTIVE_ITEM_PIPES);
        return this;
    }

    public MaterialBuilder2 addSimpleCableRecipes() {
        flags.add(Flags.GEN_RECIPE_SIMPLE_CABLES);
        return this;
    }

    public MaterialBuilder2 addComplexCableRecipes() {
        flags.add(Flags.GEN_RECIPE_COMPLEX_CABLES);
        return this;
    }

    public MaterialBuilder2 addBoltedCasings() {
        flags.add(Flags.GENERATE_BOLTED_CASING, Flags.GENERATE_REBOLTED_CASING);
        flags.add(Flags.GEN_RECIPE_BOLTED_CASING, Flags.GEN_RECIPE_REBOLTED_CASING);
        return this;
    }

    public MaterialBuilder2 setFuelValues(int fuelType, int fuelPower) {
        this.fuelType = FuelType.get(fuelType);
        this.fuelPower = fuelPower;

        return this;
    }

    public MaterialBuilder2 setFuelValues(FuelType fuelType, int fuelPower) {
        this.fuelType = fuelType;
        this.fuelPower = fuelPower;

        return this;
    }

    public MaterialBuilder2 setContents(MaterialStack2... contents) {
        this.contents = contents;
        return this;
    }

    public MaterialBuilder2 setFormula(String formula) {
        this.formula = formula;
        return this;
    }

    public MaterialBuilder2 addAlloySmelterRecipe(int voltage, int time) {
        flags.add(Flags.GEN_RECIPE_ALLOY_SMELTER);
        this.alloySmelterVoltage = voltage;
        this.alloySmelterTime = time;
        
        return this;
    }

    public MaterialBuilder2 addMixerRecipe(int circuit, int voltage, int time) {
        flags.add(Flags.GEN_RECIPE_MIXER);
        mixerCircuit = circuit;
        mixerVoltage = voltage;
        mixerTime = time;

        return this;
    }

    public MaterialBuilder2 addCentrifugeRecipe(int voltage, int time) {
        flags.add(Flags.GEN_RECIPE_CENTRIFUGE_RECYCLE);
        centrifugeVoltage = voltage;
        centrifugeTime = time;

        return this;
    }

    public MaterialBuilder2 addElectrolyzerRecipe(int circuit, int voltage, int time) {
        flags.add(Flags.GEN_RECIPE_ELECTROLYZER_RECYCLE);
        electrolyzerVoltage = voltage;
        electrolyzerTime = time;

        return this;
    }

    public MaterialBuilder2 addABSRecipe(int circuit, int voltage, int time, boolean fromContents) {
        flags.add(fromContents ? Flags.GEN_RECIPE_ABS_FROM_CONTENTS : Flags.GEN_RECIPE_ABS_FROM_DUST);
        absCircuit = circuit;
        absVoltage = voltage;
        absTime = time;

        return this;
    }

    public MaterialBuilder2 addEBFRecipe(int heat, int voltage, int baseTime, Flags gas) {
        flags.add(Flags.GEN_RECIPE_EBF, gas);
        ebfVoltage = voltage;
        ebfBaseTime = baseTime;
        ebfHeat = heat;

        return this;
    }

    public MaterialBuilder2 setEBFMakesHotIngots() {
        flags.add(Flags.EBF_MAKE_HOT_INGOTS);
        return this;
    }

    public MaterialBuilder2 addCustomEBFGas(IMaterial gas, int litres, IntFraction timeReduction) {
        customEBFGasses.add(new EBFGas(new MaterialStack2(gas, litres * M / L), timeReduction));

        return this;
    }

    public MaterialBuilder2 addCustomEBFGas(IMaterial gas, int litres, IntFraction timeReduction, int circuit) {
        customEBFGasses.add(new EBFGas(new MaterialStack2(gas, litres * M / L), timeReduction, circuit));

        return this;
    }

    public RecipeModification modifyRecipe(RecipeMap<?> recipeMap, Flags mainInput, Flags mainOutput) {
        RecipeId id = new RecipeId(recipeMap, mainInput, mainOutput);

        if (recipeModifications.containsKey(id)) {
            throw new IllegalStateException("Cannot modify the same recipe twice");
        }

        RecipeModification mod = new RecipeModification(this);
        recipeModifications.put(id, mod);

        return mod;
    }

    public void doRecipeModify(RecipeMap<?> recipeMap, Flags mainInput, Flags mainOutput, GTRecipeBuilder recipe) {
        RecipeModification mod = recipeModifications.get(new RecipeId(recipeMap, mainInput, mainOutput));

        if (mod != null) mod.modify(recipe);
    }

    public MaterialBuilder2 apply(Consumer<MaterialBuilder2> fn) {
        fn.accept(this);
        return this;
    }

    public MaterialBuilder2 thenAddTo(IMaterialHandle handle) {
        handles.add(handle);
        return this;
    }

    public static void onPreload() {
        GTMod.GT_FML_LOGGER.info("MaterialBuilder2 init");
        for (MaterialBuilder2 builder : BUILDERS) {
            builder.doPreload();
        }
    }

    public static void onLoad() {
        for (MaterialBuilder2 builder : BUILDERS) {
            builder.doLoad();
        }
    }

    public static void onPostLoad() {
        for (MaterialBuilder2 builder : BUILDERS) {
            builder.doPostLoad();
        }
    }

    /**
     * Sets up all material-related stuff.
     * Adds any material items or fluids.
     */
    private void doPreload() {
        GTMod.GT_FML_LOGGER.info("MaterialBuilder2 " + this.name);
        this.material = Material2.fromBuilder(this);

        for (IMaterialHandle handle : handles) {
            handle.setMaterial(material);
        }

        material.addStuff();
    }

    /**
     * Discover all items and fluids that we don't already know about.
     * Add machines.
     */
    public void doLoad() {
        material.discoverStuff();
    }

    /**
     * Generate recipes.
     */
    public void doPostLoad() {
        Material2RecipeGen.generate(this);
    }

    public Material2 getMaterial() {
        return material;
    }

    public boolean generatesPrefix(OrePrefixes prefix) {
        return switch (prefix) {
            case ingot -> flags.has(Flags.GENERATE_INGOT);
            case ingotHot -> flags.has(Flags.GENERATE_INGOT_HOT);
            case cell -> flags.has(Flags.GENERATE_FLUID_CELLS) || flags.has(Flags.GENERATE_GAS_CELLS);
            case cellMolten -> flags.has(Flags.GENERATE_MOLTEN_CELLS);
            case cellPlasma -> flags.has(Flags.GENERATE_PLASMA_CELLS);
            case dust -> flags.has(Flags.GENERATE_DUST);
            case dustSmall -> flags.has(Flags.GENERATE_DUST_SMALL);
            case dustTiny -> flags.has(Flags.GENERATE_DUST_TINY);
            case plate -> flags.has(Flags.GENERATE_PLATE);
            case plateDouble -> flags.has(Flags.GENERATE_PLATE2);
            case plateTriple -> flags.has(Flags.GENERATE_PLATE3);
            case plateQuadruple -> flags.has(Flags.GENERATE_PLATE4);
            case plateQuintuple -> flags.has(Flags.GENERATE_PLATE5);
            case plateDense -> flags.has(Flags.GENERATE_PLATE9);
            case foil -> flags.has(Flags.GENERATE_FOIL);
            case wireFine -> flags.has(Flags.GENERATE_FINE_WIRE);
            case stick -> flags.has(Flags.GENERATE_ROD);
            case stickLong -> flags.has(Flags.GENERATE_ROD_LARGE);
            case bolt -> flags.has(Flags.GENERATE_BOLT);
            case screw -> flags.has(Flags.GENERATE_SCREW);
            case ring -> flags.has(Flags.GENERATE_RING);
            case rotor -> flags.has(Flags.GENERATE_ROTOR);
            case nugget -> flags.has(Flags.GENERATE_NUGGET);
            case round -> flags.has(Flags.GENERATE_ROUND);
            case gearGtSmall -> flags.has(Flags.GENERATE_GEAR_SMALL);
            case gearGt -> flags.has(Flags.GENERATE_GEAR_NORMAL);
            case springSmall -> flags.has(Flags.GENERATE_SPRING_SMALL);
            case spring -> flags.has(Flags.GENERATE_SPRING_NORMAL);
            case itemCasing -> flags.has(Flags.GENERATE_CASING);
            case block -> flags.has(Flags.GENERATE_BLOCK);
            case frameGt -> flags.has(Flags.GENERATE_FRAME);
            case lens -> flags.has(Flags.GENERATE_LENS);
            case plateSuperdense -> flags.has(Flags.GENERATE_SUPERDENSE);
            case nanite -> flags.has(Flags.GENERATE_NANITES);
            case turbineBlade -> flags.has(Flags.GENERATE_TURBINE_BLADE);
            case gemChipped -> flags.has(Flags.GENERATE_GEM_CHIPPED);
            case gemFlawed -> flags.has(Flags.GENERATE_GEM_FLAWED);
            case gem -> flags.has(Flags.GENERATE_GEM_NORMAL);
            case gemFlawless -> flags.has(Flags.GENERATE_GEM_FLAWLESS);
            case gemExquisite -> flags.has(Flags.GENERATE_GEM_EXQUISITE);
            case wireGt01 -> flags.has(Flags.GENERATE_WIRE_1);
            case wireGt02 -> flags.has(Flags.GENERATE_WIRE_2);
            case wireGt04 -> flags.has(Flags.GENERATE_WIRE_4);
            case wireGt08 -> flags.has(Flags.GENERATE_WIRE_8);
            case wireGt12 -> flags.has(Flags.GENERATE_WIRE_12);
            case wireGt16 -> flags.has(Flags.GENERATE_WIRE_16);
            case cableGt01 -> flags.has(Flags.GENERATE_CABLE_1);
            case cableGt02 -> flags.has(Flags.GENERATE_CABLE_2);
            case cableGt04 -> flags.has(Flags.GENERATE_CABLE_4);
            case cableGt08 -> flags.has(Flags.GENERATE_CABLE_8);
            case cableGt12 -> flags.has(Flags.GENERATE_CABLE_12);
            case cableGt16 -> flags.has(Flags.GENERATE_CABLE_16);
            case pipeTiny -> flags.has(Flags.GENERATE_PIPE_TINY_FLUID) || flags.has(Flags.GENERATE_PIPE_TINY_ITEM);
            case pipeSmall -> flags.has(Flags.GENERATE_PIPE_SMALL_FLUID) || flags.has(Flags.GENERATE_PIPE_SMALL_ITEM);
            case pipeMedium -> flags.has(Flags.GENERATE_PIPE_NORMAL_FLUID) || flags.has(Flags.GENERATE_PIPE_NORMAL_ITEM);
            case pipeLarge -> flags.has(Flags.GENERATE_PIPE_LARGE_FLUID) || flags.has(Flags.GENERATE_PIPE_LARGE_ITEM);
            case pipeHuge -> flags.has(Flags.GENERATE_PIPE_HUGE_FLUID) || flags.has(Flags.GENERATE_PIPE_HUGE_ITEM);
            case pipeQuadruple -> flags.has(Flags.GENERATE_PIPE_QUADRUPLE_FLUID);
            case pipeNonuple -> flags.has(Flags.GENERATE_PIPE_NONUPLE_FLUID);
            case pipeRestrictiveTiny -> flags.has(Flags.GENERATE_PIPE_TINY_RESTRICTIVE_ITEM);
            case pipeRestrictiveSmall -> flags.has(Flags.GENERATE_PIPE_SMALL_RESTRICTIVE_ITEM);
            case pipeRestrictiveMedium -> flags.has(Flags.GENERATE_PIPE_NORMAL_RESTRICTIVE_ITEM);
            case pipeRestrictiveLarge -> flags.has(Flags.GENERATE_PIPE_LARGE_RESTRICTIVE_ITEM);
            case pipeRestrictiveHuge -> flags.has(Flags.GENERATE_PIPE_HUGE_RESTRICTIVE_ITEM);
            case blockCasing -> flags.has(Flags.GENERATE_BOLTED_CASING);
            case blockCasingAdvanced -> flags.has(Flags.GENERATE_REBOLTED_CASING);
            default -> false;
        };
    }

    public static class EBFGas {
        public MaterialStack2 gas;
        public IntFraction timeReduction;
        /** Set to 0 to not use circuit */
        public int circuit;

        public EBFGas() {
        }
        
        public EBFGas(MaterialStack2 gas, IntFraction timeReduction) {
            this.gas = gas;
            this.timeReduction = timeReduction;
            circuit = gas == null ? 1 : 11;
        }

        public EBFGas(MaterialStack2 gas, IntFraction timeReduction, int circuit) {
            this.gas = gas;
            this.timeReduction = timeReduction;
            this.circuit = circuit;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((gas == null) ? 0 : gas.hashCode());
            result = prime * result + ((timeReduction == null) ? 0 : timeReduction.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EBFGas other = (EBFGas) obj;
            if (gas == null) {
                if (other.gas != null)
                    return false;
            } else if (!gas.equals(other.gas))
                return false;
            if (timeReduction == null) {
                if (other.timeReduction != null)
                    return false;
            } else if (!timeReduction.equals(other.timeReduction))
                return false;
            return true;
        }

        public static final Lazy<EBFGas[]> NO_GASSES = new Lazy<>(() -> new EBFGas[] {
            new EBFGas(null, new IntFraction(1, 1)),
        });

        public static final Lazy<EBFGas[]> NITROGEN_SERIES = new Lazy<>(() -> new EBFGas[] {
            new EBFGas(MaterialStack2.litres(Materials.Nitrogen, 1000), new IntFraction(10, 10)),
            new EBFGas(MaterialStack2.litres(WerkstoffLoader.Xenon, 400), new IntFraction(4, 10)),
            new EBFGas(MaterialStack2.litres(WerkstoffLoader.Oganesson, 100), new IntFraction(3, 10)),
        });

        public static final Lazy<EBFGas[]> OXYGEN_SERIES = new Lazy<>(() -> new EBFGas[] {
            new EBFGas(null, new IntFraction(2, 1)),
            new EBFGas(MaterialStack2.litres(Materials.Oxygen, 1000), new IntFraction(1, 1)),
        });

        public static final Lazy<EBFGas[]> HYDROGEN_SERIES = new Lazy<>(() -> new EBFGas[] {
            new EBFGas(null, new IntFraction(2, 1)),
            new EBFGas(MaterialStack2.litres(Materials.Hydrogen, 1000), new IntFraction(1, 1)),
        });

        public static final Lazy<EBFGas[]> NOBLE_GAS_SERIES = new Lazy<>(() -> new EBFGas[] {
            new EBFGas(null, new IntFraction(4110, 4000)), // huh???
            new EBFGas(MaterialStack2.litres(Materials.Helium, 1000), new IntFraction(4000, 4000)),
            new EBFGas(MaterialStack2.litres(Materials.Argon, 850), new IntFraction(3855, 4000)),
            new EBFGas(MaterialStack2.litres(Materials.Radon, 700), new IntFraction(3111, 4000)),
            new EBFGas(MaterialStack2.litres(WerkstoffLoader.Neon, 550), new IntFraction(2666, 4000)),
            new EBFGas(MaterialStack2.litres(WerkstoffLoader.Krypton, 400), new IntFraction(2222, 4000)),
            new EBFGas(MaterialStack2.litres(WerkstoffLoader.Xenon, 250), new IntFraction(1777, 4000)),
            new EBFGas(MaterialStack2.litres(WerkstoffLoader.Oganesson, 100), new IntFraction(1333, 4000)),
        });
    }

    public class OregenBuilder {
        public final MaterialBuilder2 parent;

        public OregenBuilder(MaterialBuilder2 parent) {
            this.parent = parent;
            parent.ore = this;
        }

        public MaterialBuilder2 done() {
            return parent;
        }
    }

    public class ToolBuilder {
        public final MaterialBuilder2 parent;

        /** The durability of all tools (multiplied by 100) */
        public int durability;
        /** The base mining speed of tools  */
        public float toolSpeed;
        /** The mining level of tools */
        public byte toolQuality;

        public ToolBuilder(MaterialBuilder2 parent) {
            this.parent = parent;
            parent.tools = this;
        }

        public MaterialBuilder2 done() {
            return parent;
        }
    }

    public class FluidBuilder {
        public final MaterialBuilder2 parent;

        public String name = null, defaultLocalName = null, textureName = "autogenerated";
        public int temperature = 300, cellCapacity = 1000;
        public short[] colour = MaterialBuilder2.this.colour;

        public FluidBuilder(MaterialBuilder2 parent) {
            this.parent = parent;
        }

        public FluidBuilder setNames(String name, String defaultLocalName) {
            this.name = name;
            this.defaultLocalName = defaultLocalName;
            return this;
        }

        public FluidBuilder setTemperature(int temperature) {
            this.temperature = temperature;
            return this;
        }

        public FluidBuilder setCellCapacity(int capacity) {
            this.cellCapacity = capacity;
            return this;
        }

        public FluidBuilder setTextureName(String textureName) {
            this.textureName = textureName;
            return this;
        }

        public FluidBuilder setColour(int r, int g, int b, int a) {
            setColour(new short[] { (short) r, (short) g, (short) b, (short) a });
            return this;
        }
    
        public FluidBuilder setColour(short[] rgba) {
            colour = rgba;
            return this;
        }
    
        public MaterialBuilder2 done() {
            return parent;
        }
    }

    public static class RecipeId {
        public final RecipeMap<?> recipeMap;
        /**
         * An {@link OrePrefixes} or a {@link FluidType}.
         * Should be the 'main' input. Does not filter for auxiliary inputs.
         */
        public final Flags mainInput;
        /**
         * An {@link OrePrefixes} or a {@link FluidType}.
         * Should be the 'main' output. Does not filter for auxiliary outputs.
         */
        public final Flags mainOutput;

        public RecipeId(RecipeMap<?> recipeMap, Flags mainInput, Flags mainOutput) {
            this.recipeMap = recipeMap;
            this.mainInput = mainInput;
            this.mainOutput = mainOutput;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((recipeMap == null) ? 0 : recipeMap.hashCode());
            result = prime * result + ((mainInput == null) ? 0 : mainInput.hashCode());
            result = prime * result + ((mainOutput == null) ? 0 : mainOutput.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            RecipeId other = (RecipeId) obj;
            if (recipeMap == null) {
                if (other.recipeMap != null) return false;
            } else if (!recipeMap.equals(other.recipeMap)) return false;
            if (mainInput != other.mainInput) return false;
            if (mainOutput != other.mainOutput) return false;
            return true;
        }
    }

    public static class RecipeModification {
        private final MaterialBuilder2 parent;

        public Consumer<GTRecipeBuilder> customModifier;
        public Integer newVoltage, newTime, newCircuit;
        public boolean hasCircuit = false;

        public RecipeModification(MaterialBuilder2 parent) {
            this.parent = parent;
        }

        public RecipeModification custom(Consumer<GTRecipeBuilder> custom) {
            this.customModifier = custom;
            return this;
        }

        public RecipeModification setVoltage(int voltage) {
            newVoltage = voltage;
            return this;
        }

        public RecipeModification setTime(int time) {
            newTime = time;
            return this;
        }

        public RecipeModification removeCircuit() {
            newCircuit = null;
            hasCircuit = true;
            return this;
        }

        public RecipeModification setBasicCircuit(int circuit) {
            newCircuit = circuit;
            hasCircuit = true;
            return this;
        }

        public RecipeModification setBioCircuit(int circuit) {
            newCircuit = circuit + 24;
            hasCircuit = true;
            return this;
        }

        public RecipeModification setBreakthroughCircuit(int circuit) {
            newCircuit = circuit + 48;
            hasCircuit = true;
            return this;
        }

        public MaterialBuilder2 done() {
            return parent;
        }

        public void modify(GTRecipeBuilder builder) {
            if (newVoltage != null) builder.eut(newVoltage);
            if (newTime != null) builder.duration(newTime);

            if (hasCircuit) {
                List<ItemStack> withoutCircuits = new ArrayList<>(Arrays.asList(builder.getItemInputsBasic()));

                withoutCircuits.removeIf(i -> i.getItem() instanceof ItemIntegratedCircuit);
                withoutCircuits.removeIf(i -> i.getItem() instanceof GTPPIntegratedCircuitItem);

                if (newCircuit != null) {
                    if (newCircuit >= 48) {
                        withoutCircuits.add(GregtechItemList.Circuit_T3RecipeSelector.getWithDamage(0, newCircuit - 48));
                    } else if (newCircuit >= 24) {
                        withoutCircuits.add(GregtechItemList.Circuit_BioRecipeSelector.getWithDamage(0, newCircuit - 24));
                    } else {
                        withoutCircuits.add(ItemList.Circuit_Integrated.getWithDamage(0, newCircuit));
                    }
                }

                builder.itemInputs(withoutCircuits.toArray(new ItemStack[withoutCircuits.size()]));
            }

            if (customModifier != null) customModifier.accept(builder);
        }
    }

    public static enum Flags {
        GENERATE_INGOT,
        GENERATE_INGOT_HOT,

        GENERATE_FLUID,
        GENERATE_MOLTEN,
        GENERATE_GAS,
        GENERATE_PLASMA,
        GENERATE_FLUID_CELLS,
        GENERATE_MOLTEN_CELLS,
        GENERATE_GAS_CELLS,
        GENERATE_PLASMA_CELLS,

        GENERATE_DUST,
        GENERATE_DUST_SMALL,
        GENERATE_DUST_TINY,

        GENERATE_PLATE,
        GENERATE_PLATE2,
        GENERATE_PLATE3,
        GENERATE_PLATE4,
        GENERATE_PLATE5,
        GENERATE_PLATE9,

        GENERATE_FOIL,
        GENERATE_FINE_WIRE,
        GENERATE_ROD,
        GENERATE_ROD_LARGE,
        GENERATE_BOLT,
        GENERATE_SCREW,
        GENERATE_RING,
        GENERATE_ROTOR,
        GENERATE_NUGGET,
        GENERATE_ROUND,
        GENERATE_GEAR_SMALL,
        GENERATE_GEAR_NORMAL,
        GENERATE_SPRING_SMALL,
        GENERATE_SPRING_NORMAL,
        GENERATE_CASING,
        GENERATE_BLOCK,
        GENERATE_FRAME,

        GENERATE_LENS,
        GENERATE_SUPERDENSE,
        GENERATE_NANITES,
        GENERATE_TURBINE_BLADE,

        GENERATE_GEM_CHIPPED,
        GENERATE_GEM_FLAWED,
        GENERATE_GEM_NORMAL,
        GENERATE_GEM_FLAWLESS,
        GENERATE_GEM_EXQUISITE,

        GENERATE_WIRE_1,
        GENERATE_WIRE_2,
        GENERATE_WIRE_4,
        GENERATE_WIRE_8,
        GENERATE_WIRE_12,
        GENERATE_WIRE_16,
        GENERATE_CABLE_1,
        GENERATE_CABLE_2,
        GENERATE_CABLE_4,
        GENERATE_CABLE_8,
        GENERATE_CABLE_12,
        GENERATE_CABLE_16,

        GENERATE_PIPE_TINY_FLUID,
        GENERATE_PIPE_SMALL_FLUID,
        GENERATE_PIPE_NORMAL_FLUID,
        GENERATE_PIPE_LARGE_FLUID,
        GENERATE_PIPE_HUGE_FLUID,
        GENERATE_PIPE_QUADRUPLE_FLUID,
        GENERATE_PIPE_NONUPLE_FLUID,

        GENERATE_PIPE_TINY_ITEM,
        GENERATE_PIPE_SMALL_ITEM,
        GENERATE_PIPE_NORMAL_ITEM,
        GENERATE_PIPE_LARGE_ITEM,
        GENERATE_PIPE_HUGE_ITEM,
        GENERATE_PIPE_TINY_RESTRICTIVE_ITEM,
        GENERATE_PIPE_SMALL_RESTRICTIVE_ITEM,
        GENERATE_PIPE_NORMAL_RESTRICTIVE_ITEM,
        GENERATE_PIPE_LARGE_RESTRICTIVE_ITEM,
        GENERATE_PIPE_HUGE_RESTRICTIVE_ITEM,

        GENERATE_BOLTED_CASING,
        GENERATE_REBOLTED_CASING,

        GEN_RECIPE_SMELT_INGOT,
        GEN_RECIPE_SMELT_GEM,
        GEN_RECIPE_EBF_INGOT,
        GEN_RECIPE_VACUUM_FREEZER_HOT_INGOT,
        GEN_RECIPE_MACERATOR_RECYCLE,
        GEN_RECIPE_ARC_FURNACE_RECYCLE,
        GEN_RECIPE_FLUID_EXTRACTOR_RECYCLE,
        GEN_RECIPE_CENTRIFUGE_RECYCLE,
        GEN_RECIPE_ELECTROLYZER_RECYCLE,
        GEN_RECIPE_CELL_CANNING,
        GEN_RECIPE_GEM_CONVERSIONS,
        GEN_RECIPE_LENS,
        GEN_RECIPE_CUT_BLOCK,
        GEN_RECIPE_WIRES,
        GEN_RECIPE_SIMPLE_CABLES,
        GEN_RECIPE_COMPLEX_CABLES,
        GEN_RECIPE_FLUID_PIPES,
        GEN_RECIPE_ITEM_PIPES,
        GEN_RECIPE_METAL_PARTS,
        GEN_RECIPE_METAL_PARTS_HAND,
        GEN_RECIPE_GEM_PARTS,
        GEN_RECIPE_FRAME,
        GEN_RECIPE_BOLTED_CASING,
        GEN_RECIPE_REBOLTED_CASING,
        GEN_RECIPE_MIXER,
        GEN_RECIPE_ALLOY_SMELTER,
        GEN_RECIPE_ABS_FROM_DUST,
        GEN_RECIPE_ABS_FROM_CONTENTS,
        GEN_RECIPE_EBF,

        EBF_MAKE_HOT_INGOTS,
        EBF_USE_NITROGEN_SERIES,
        EBF_USE_NOBLE_GAS_SERIES,
        EBF_USE_HYDROGEN,
        EBF_USE_OXYGEN,

        /** Not actually a flag, just a placeholder for ABS/mixer/content recycling recipe modification. */
        CONTENTS,

        ;

        public static final Flags[] DUSTS = new Flags[] {
            GENERATE_DUST,
            GENERATE_DUST_SMALL,
            GENERATE_DUST_TINY
        };

        public static final Flags[] PLATES = new Flags[] {
            GENERATE_PLATE,
            GENERATE_PLATE2,
            GENERATE_PLATE3,
            GENERATE_PLATE4,
            GENERATE_PLATE5,
            GENERATE_PLATE9
        };

        public static final Flags[] METAL_PARTS = new Flags[] {
            GENERATE_FOIL,
            GENERATE_FINE_WIRE,
            GENERATE_ROD,
            GENERATE_ROD_LARGE,
            GENERATE_BOLT,
            GENERATE_SCREW,
            GENERATE_RING,
            GENERATE_ROTOR,
            GENERATE_NUGGET,
            GENERATE_ROUND,
            GENERATE_GEAR_SMALL,
            GENERATE_GEAR_NORMAL,
            GENERATE_SPRING_SMALL,
            GENERATE_SPRING_NORMAL,
            GENERATE_CASING,
            GENERATE_FRAME
        };

        public static final Flags[] GEMS = new Flags[] {
            GENERATE_GEM_CHIPPED,
            GENERATE_GEM_FLAWED,
            GENERATE_GEM_NORMAL,
            GENERATE_GEM_FLAWLESS,
            GENERATE_GEM_EXQUISITE
        };

        public static final Flags[] WIRES = new Flags[] {
            GENERATE_WIRE_1,
            GENERATE_WIRE_2,
            GENERATE_WIRE_4,
            GENERATE_WIRE_8,
            GENERATE_WIRE_12,
            GENERATE_WIRE_16
        };

        public static final Flags[] CABLES = new Flags[] {
            GENERATE_CABLE_1,
            GENERATE_CABLE_2,
            GENERATE_CABLE_4,
            GENERATE_CABLE_8,
            GENERATE_CABLE_12,
            GENERATE_CABLE_16
        };

        public static final Flags[] FLUID_PIPES = new Flags[] {
            GENERATE_PIPE_TINY_FLUID,
            GENERATE_PIPE_SMALL_FLUID,
            GENERATE_PIPE_NORMAL_FLUID,
            GENERATE_PIPE_LARGE_FLUID,
            GENERATE_PIPE_HUGE_FLUID,
            GENERATE_PIPE_QUADRUPLE_FLUID,
            GENERATE_PIPE_NONUPLE_FLUID
        };

        public static final Flags[] ITEM_PIPES = new Flags[] {
            GENERATE_PIPE_TINY_ITEM,
            GENERATE_PIPE_SMALL_ITEM,
            GENERATE_PIPE_NORMAL_ITEM,
            GENERATE_PIPE_LARGE_ITEM,
            GENERATE_PIPE_HUGE_ITEM
        };

        public static final Flags[] RESTRICTIVE_ITEM_PIPES = new Flags[] {
            GENERATE_PIPE_TINY_RESTRICTIVE_ITEM,
            GENERATE_PIPE_SMALL_RESTRICTIVE_ITEM,
            GENERATE_PIPE_NORMAL_RESTRICTIVE_ITEM,
            GENERATE_PIPE_LARGE_RESTRICTIVE_ITEM,
            GENERATE_PIPE_HUGE_RESTRICTIVE_ITEM
        };

        public static final Flags[] MACHINE_CASINGS = new Flags[] {
            GENERATE_BOLTED_CASING,
            GENERATE_REBOLTED_CASING
        };

        public static final Flags[] RECYCLEABLE = GTUtility.concat(new Flags[] { GENERATE_INGOT, GENERATE_BLOCK }, PLATES, METAL_PARTS, WIRES, CABLES, FLUID_PIPES, ITEM_PIPES, RESTRICTIVE_ITEM_PIPES);

        public static final Flags[] STANDARD_RECIPES = new Flags[] {
            GEN_RECIPE_MACERATOR_RECYCLE,
            GEN_RECIPE_ARC_FURNACE_RECYCLE,
            GEN_RECIPE_FLUID_EXTRACTOR_RECYCLE,
            GEN_RECIPE_CENTRIFUGE_RECYCLE,
            GEN_RECIPE_ELECTROLYZER_RECYCLE,
            GEN_RECIPE_CELL_CANNING,
            GEN_RECIPE_GEM_CONVERSIONS,
            GEN_RECIPE_LENS,
            GEN_RECIPE_CUT_BLOCK,
            GEN_RECIPE_WIRES,
            GEN_RECIPE_FLUID_PIPES,
            GEN_RECIPE_ITEM_PIPES,
            GEN_RECIPE_METAL_PARTS,
            GEN_RECIPE_METAL_PARTS_HAND,
            GEN_RECIPE_GEM_PARTS,
            GEN_RECIPE_FRAME,
            GEN_RECIPE_BOLTED_CASING,
            GEN_RECIPE_REBOLTED_CASING,
        };

        public static final Flags[] CELLS = new Flags[] {
            GENERATE_FLUID_CELLS,
            GENERATE_MOLTEN_CELLS,
            GENERATE_GAS_CELLS,
            GENERATE_PLASMA_CELLS
        };
        
        public static final Flags[] ALL_FLAGS = values();

        public static final Flags[] ITEMS, FLUIDS;

        static {
            ArrayList<Flags> items = new ArrayList<>();
            ArrayList<Flags> fluids = new ArrayList<>();

            for (Flags flag : ALL_FLAGS) {
                Object prefix = flag.getPrefix();

                if (prefix instanceof OrePrefixes) {
                    items.add(flag);
                }

                if (prefix instanceof FluidType) {
                    fluids.add(flag);
                }
            }

            ITEMS = items.toArray(new Flags[0]);
            FLUIDS = fluids.toArray(new Flags[0]);
        }

        public Object getPrefix() {
            return switch (this) {
                case GENERATE_INGOT -> OrePrefixes.ingot;
                case GENERATE_INGOT_HOT -> OrePrefixes.ingotHot;
                case GENERATE_FLUID -> FluidType.Fluid;
                case GENERATE_MOLTEN -> FluidType.Molten;
                case GENERATE_GAS -> FluidType.Gas;
                case GENERATE_PLASMA -> FluidType.Plasma;
                case GENERATE_FLUID_CELLS -> OrePrefixes.cell;
                case GENERATE_MOLTEN_CELLS -> OrePrefixes.cellMolten;
                case GENERATE_GAS_CELLS -> OrePrefixes.cell;
                case GENERATE_PLASMA_CELLS -> OrePrefixes.cellPlasma;
                case GENERATE_DUST -> OrePrefixes.dust;
                case GENERATE_DUST_SMALL -> OrePrefixes.dustSmall;
                case GENERATE_DUST_TINY -> OrePrefixes.dustTiny;
                case GENERATE_PLATE -> OrePrefixes.plate;
                case GENERATE_PLATE2 -> OrePrefixes.plateDouble;
                case GENERATE_PLATE3 -> OrePrefixes.plateTriple;
                case GENERATE_PLATE4 -> OrePrefixes.plateQuadruple;
                case GENERATE_PLATE5 -> OrePrefixes.plateQuintuple;
                case GENERATE_PLATE9 -> OrePrefixes.plateDense;
                case GENERATE_FOIL -> OrePrefixes.foil;
                case GENERATE_FINE_WIRE -> OrePrefixes.wireFine;
                case GENERATE_ROD -> OrePrefixes.stick;
                case GENERATE_ROD_LARGE -> OrePrefixes.stickLong;
                case GENERATE_BOLT -> OrePrefixes.bolt;
                case GENERATE_SCREW -> OrePrefixes.screw;
                case GENERATE_RING -> OrePrefixes.ring;
                case GENERATE_ROTOR -> OrePrefixes.rotor;
                case GENERATE_NUGGET -> OrePrefixes.nugget;
                case GENERATE_ROUND -> OrePrefixes.round;
                case GENERATE_GEAR_SMALL -> OrePrefixes.gearGtSmall;
                case GENERATE_GEAR_NORMAL -> OrePrefixes.gearGt;
                case GENERATE_SPRING_SMALL -> OrePrefixes.springSmall;
                case GENERATE_SPRING_NORMAL -> OrePrefixes.spring;
                case GENERATE_CASING -> OrePrefixes.itemCasing;
                case GENERATE_BLOCK -> OrePrefixes.block;
                case GENERATE_FRAME -> OrePrefixes.frameGt;
                case GENERATE_LENS -> OrePrefixes.lens;
                case GENERATE_SUPERDENSE -> OrePrefixes.plateSuperdense;
                case GENERATE_NANITES -> OrePrefixes.nanite;
                case GENERATE_TURBINE_BLADE -> OrePrefixes.turbineBlade;
                case GENERATE_GEM_CHIPPED -> OrePrefixes.gemChipped;
                case GENERATE_GEM_FLAWED -> OrePrefixes.gemFlawed;
                case GENERATE_GEM_NORMAL -> OrePrefixes.gem;
                case GENERATE_GEM_FLAWLESS -> OrePrefixes.gemFlawless;
                case GENERATE_GEM_EXQUISITE -> OrePrefixes.gemExquisite;
                case GENERATE_WIRE_1 -> OrePrefixes.wireGt01;
                case GENERATE_WIRE_2 -> OrePrefixes.wireGt02;
                case GENERATE_WIRE_4 -> OrePrefixes.wireGt04;
                case GENERATE_WIRE_8 -> OrePrefixes.wireGt08;
                case GENERATE_WIRE_12 -> OrePrefixes.wireGt12;
                case GENERATE_WIRE_16 -> OrePrefixes.wireGt16;
                case GENERATE_CABLE_1 -> OrePrefixes.cableGt01;
                case GENERATE_CABLE_2 -> OrePrefixes.cableGt02;
                case GENERATE_CABLE_4 -> OrePrefixes.cableGt04;
                case GENERATE_CABLE_8 -> OrePrefixes.cableGt08;
                case GENERATE_CABLE_12 -> OrePrefixes.cableGt12;
                case GENERATE_CABLE_16 -> OrePrefixes.cableGt16;
                case GENERATE_PIPE_TINY_FLUID -> OrePrefixes.pipeTiny;
                case GENERATE_PIPE_SMALL_FLUID -> OrePrefixes.pipeSmall;
                case GENERATE_PIPE_NORMAL_FLUID -> OrePrefixes.pipeMedium;
                case GENERATE_PIPE_LARGE_FLUID -> OrePrefixes.pipeLarge;
                case GENERATE_PIPE_HUGE_FLUID -> OrePrefixes.pipeHuge;
                case GENERATE_PIPE_QUADRUPLE_FLUID -> OrePrefixes.pipeQuadruple;
                case GENERATE_PIPE_NONUPLE_FLUID -> OrePrefixes.pipeNonuple;
                case GENERATE_PIPE_TINY_ITEM -> OrePrefixes.pipeTiny;
                case GENERATE_PIPE_SMALL_ITEM -> OrePrefixes.pipeSmall;
                case GENERATE_PIPE_NORMAL_ITEM -> OrePrefixes.pipeMedium;
                case GENERATE_PIPE_LARGE_ITEM -> OrePrefixes.pipeLarge;
                case GENERATE_PIPE_HUGE_ITEM -> OrePrefixes.pipeHuge;
                case GENERATE_PIPE_TINY_RESTRICTIVE_ITEM -> OrePrefixes.pipeRestrictiveTiny;
                case GENERATE_PIPE_SMALL_RESTRICTIVE_ITEM -> OrePrefixes.pipeRestrictiveSmall;
                case GENERATE_PIPE_NORMAL_RESTRICTIVE_ITEM -> OrePrefixes.pipeRestrictiveMedium;
                case GENERATE_PIPE_LARGE_RESTRICTIVE_ITEM -> OrePrefixes.pipeRestrictiveLarge;
                case GENERATE_PIPE_HUGE_RESTRICTIVE_ITEM -> OrePrefixes.pipeRestrictiveHuge;
                case GENERATE_BOLTED_CASING -> OrePrefixes.blockCasing;
                case GENERATE_REBOLTED_CASING -> OrePrefixes.blockCasingAdvanced;
                default -> null;
            };
        }
    }
}
