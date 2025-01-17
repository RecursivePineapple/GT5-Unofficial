package gregtech.api.structure;

import static net.minecraft.util.EnumChatFormatting.DARK_RED;
import static net.minecraft.util.EnumChatFormatting.RESET;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;

import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizons.modularui.api.drawable.TextRenderer;

import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;

public class StructureWrapperInstanceInfo<MTE extends MTEEnhancedMultiBlockBase<?> & IStructureProvider<MTE>> {

    public final StructureWrapper<MTE> structure;

    public Char2IntArrayMap actualCasingCounts = new Char2IntArrayMap();

    private boolean valid = false;

    private String error;

    public StructureWrapperInstanceInfo(StructureWrapper<MTE> structure) {
        this.structure = structure;
    }

    private static final int ERROR_WRAP_WIDTH = 180;

    /**
     * Validates this structure. Currently only checks casing counts.
     * @return True when valid.
     */
    public boolean validate() {
        List<String> lines = new ArrayList<>();

        this.error = null;
        valid = true;

        for (var e : structure.casings.char2ObjectEntrySet()) {
            int presentCasings = actualCasingCounts.get(e.getCharKey());
            int minCasings = e.getValue().definitionCasingCount - e.getValue().maxHatches;

            if (presentCasings < minCasings) {
                valid = false;

                String error = String.format(
                    "%sNot enough %s: need %d, but have %d.%s",
                    DARK_RED,
                    e.getValue().casing.getLocalizedName(),
                    minCasings,
                    presentCasings,
                    RESET
                );

                lines.addAll(TextRenderer.getFontRenderer().listFormattedStringToWidth(error, ERROR_WRAP_WIDTH));
            }
        }

        error = String.join("\n", lines);

        return valid;
    }

    public boolean checkStructure(MTE instance) {
        actualCasingCounts.clear();

        if (!structure.checkStructure(instance)) {
            valid = false;
            return false;
        }

        for (var e : structure.casings.char2ObjectEntrySet()) {
            actualCasingCounts.putIfAbsent(e.getCharKey(), e.getValue().definitionCasingCount);
        }

        validate();

        return valid;
    }

    public String getErrorMessage() {
        return error;
    }

    public boolean isValid() {
        return valid;
    }

    public void construct(MTE instance, ItemStack trigger, boolean hintsOnly) {
        structure.construct(instance, trigger, hintsOnly);
    }

    public int survivalConstruct(MTE instance, ItemStack trigger, int elementBudget, ISurvivalBuildEnvironment env) {
        return structure.survivalConstruct(instance, trigger, elementBudget, env);
    }
}
