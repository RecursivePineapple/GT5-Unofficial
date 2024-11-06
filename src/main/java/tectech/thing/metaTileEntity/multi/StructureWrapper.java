package tectech.thing.metaTileEntity.multi;

import static gregtech.api.enums.Mods.NewHorizonsCoreMod;

import java.util.function.Consumer;

import org.joml.Vector3i;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;

import gregtech.GTMod;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;
import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import it.unimi.dsi.fastutil.ints.IntBinaryOperator;
import net.minecraft.item.ItemStack;

/**
 * A wrapper for structure checking.
 * This should only be stored in the prototype MTE, then shared among the instance MTEs.
 */
public class StructureWrapper<MTE extends MTEEnhancedMultiBlockBase<?>> {

    public static final String STRUCTURE_PIECE_MAIN = "main";

    public final IStructureProvider<MTE> provider;

    public String[][] definitionText;

    public IStructureDefinition<MTE> structureDefinition;

    public Vector3i offset, size;
    public Char2IntArrayMap definitionCasingCounts, actualCasingCounts;

    public StructureWrapper(IStructureProvider<MTE> provider) {
        this.provider = provider;

        loadStructure();
    }

    public void loadStructure() {
        structureDefinition = null;

        try {
            definitionText = provider.getDefinition();
            definitionCasingCounts = new Char2IntArrayMap();
    
            int width = 0;
            int height = 0;
            int length = definitionText.length;
    
            // find the controller offset and count the number of casings
            int z = 0;
            for (String[] a : definitionText) {
                int y = 0;
                height = Math.max(height, a.length);
                for (String b : a) {
                    width = Math.max(width, b.length());
                    for (int x = 0; x < b.length(); x++) {
                        char c = b.charAt(x);
                        if (c == ' ' || c == '-' || c == '+') continue;
    
                        definitionCasingCounts.put(c, definitionCasingCounts.getOrDefault(c, 0) + 1);
    
                        if (c == '~') {
                            offset = new Vector3i(x, y, z);
                        }
                    }
                    y++;
                }
                z++;
            }

            size = new Vector3i(width, height, length);
    
            structureDefinition = provider.compile(definitionText);
        } catch (Throwable t) {
            GTMod.GT_FML_LOGGER.error("Could not compile structure", t);
        }
    }

    public boolean checkStructure(MTE instance) {
        actualCasingCounts = new Char2IntArrayMap();

        if (NewHorizonsCoreMod.isModLoaded()) {
            return checkStructureImpl(instance);
        } else {
            try {
                return checkStructureImpl(instance);
            } catch (NoSuchMethodError ignored) {
                // probably got hotswapped
                GTMod.GT_FML_LOGGER.info("Caught an exception that was probably caused by a hotswap.", ignored);
    
                loadStructure();

                return checkStructureImpl(instance);
            }
        }
    }

    private boolean checkStructureImpl(MTE instance) {
        final IGregTechTileEntity tTile = instance.getBaseMetaTileEntity();
        return structureDefinition.check(
            instance,
            STRUCTURE_PIECE_MAIN,
            tTile.getWorld(),
            instance.getExtendedFacing(),
            tTile.getXCoord(),
            tTile.getYCoord(),
            tTile.getZCoord(),
            offset.x,
            offset.y,
            offset.z,
            !instance.mMachine);
    }

    public void construct(MTE instance, ItemStack trigger, boolean hintsOnly) {
        if (NewHorizonsCoreMod.isModLoaded()) {
            constructImpl(instance, trigger, hintsOnly);
        } else {
            try {
                constructImpl(instance, trigger, hintsOnly);
            } catch (NoSuchMethodError ignored) {
                // probably got hotswapped
                GTMod.GT_FML_LOGGER.info("Caught an exception that was probably caused by a hotswap.", ignored);
    
                loadStructure();

                constructImpl(instance, trigger, hintsOnly);
            }
        }
    }

    private void constructImpl(MTE instance, ItemStack trigger, boolean hintsOnly) {
        final IGregTechTileEntity tTile = instance.getBaseMetaTileEntity();
        structureDefinition.buildOrHints(
            instance,
            trigger,
            STRUCTURE_PIECE_MAIN,
            tTile.getWorld(),
            instance.getExtendedFacing(),
            tTile.getXCoord(),
            tTile.getYCoord(),
            tTile.getZCoord(),
            offset.x,
            offset.y,
            offset.z,
            hintsOnly);
    }

    public int survivalConstruct(MTE instance, ItemStack trigger, int elementBudget, ISurvivalBuildEnvironment env) {
        if (instance.mMachine) return -1;

        if (NewHorizonsCoreMod.isModLoaded()) {
            return survivalConstructImpl(instance, trigger, elementBudget, env);
        } else {
            try {
                return survivalConstructImpl(instance, trigger, elementBudget, env);
            } catch (NoSuchMethodError ignored) {
                // probably got hotswapped
                GTMod.GT_FML_LOGGER.info("Caught an exception that was probably caused by a hotswap.", ignored);
    
                loadStructure();

                return survivalConstructImpl(instance, trigger, elementBudget, env);
            }
        }
    }

    private int survivalConstructImpl(MTE instance, ItemStack trigger, int elementBudget, ISurvivalBuildEnvironment env) {
        final IGregTechTileEntity tTile = instance.getBaseMetaTileEntity();
        int built = structureDefinition.survivalBuild(
            instance,
            trigger,
            STRUCTURE_PIECE_MAIN,
            tTile.getWorld(),
            instance.getExtendedFacing(),
            tTile.getXCoord(),
            tTile.getYCoord(),
            tTile.getZCoord(),
            offset.x,
            offset.y,
            offset.z,
            elementBudget,
            env,
            false);

        if (built > 0) instance.checkStructure(true, tTile);

        return built;
    }

    public Consumer<MTE> getCasingAdder(char c) {
        final IntBinaryOperator sum = Integer::sum;

        return ignored -> {
            actualCasingCounts.mergeInt(c, 1, sum);
        };
    }

    public static interface IStructureProvider<MTE> {
        public String[][] getDefinition();

        public IStructureDefinition<MTE> compile(String[][] definition);
    }
}
