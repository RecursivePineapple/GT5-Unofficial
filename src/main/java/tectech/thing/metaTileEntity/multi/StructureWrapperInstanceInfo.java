package tectech.thing.metaTileEntity.multi;

import static net.minecraft.util.EnumChatFormatting.DARK_RED;
import static net.minecraft.util.EnumChatFormatting.RESET;

import java.util.ArrayList;
import java.util.List;

import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizons.modularui.api.drawable.TextRenderer;

import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;
import it.unimi.dsi.fastutil.chars.Char2IntArrayMap;
import net.minecraft.item.ItemStack;

public class StructureWrapperInstanceInfo<MTE extends MTEEnhancedMultiBlockBase<?> & IStructureProvider<MTE>> {
    public final StructureWrapper<MTE> structure;

    public Char2IntArrayMap actualCasingCounts = new Char2IntArrayMap();

    public boolean hasErrors = false;

    public StructureWrapperInstanceInfo(StructureWrapper<MTE> structure) {
        this.structure = structure;
    }

    private static final int ERROR_WRAP_WIDTH = 180;

    public String getErrors() {
        List<String> lines = new ArrayList<>();

        for (var e : structure.maxHatches.char2IntEntrySet()) {
            int presentCasings = actualCasingCounts.get(e.getCharKey());
            int minCasings = structure.definitionCasingCounts.get(e.getCharKey()) - e.getIntValue();

            if (presentCasings < minCasings) {
                hasErrors = true;
                
                String error = String.format(
                    "%sNot enough %s: need %d, but have %d.%s",
                    DARK_RED,
                    structure.casings.get(e.getCharKey()).getLocalizedName(),
                    minCasings,
                    presentCasings,
                    RESET);

                lines.addAll(TextRenderer.getFontRenderer().listFormattedStringToWidth(error, ERROR_WRAP_WIDTH));
            }
        }

        return String.join("\n", lines);
    }

    public boolean checkStructure(MTE instance) {
        actualCasingCounts.clear();
        hasErrors = false;

        if (!structure.checkStructure(instance)) {
            return false;
        }

        for (var e : structure.definitionCasingCounts.char2IntEntrySet()) {
            actualCasingCounts.putIfAbsent(e.getCharKey(), e.getIntValue());
        }

        // this is fine :fire:
        getErrors();

        return !hasErrors;
    }

    public void construct(MTE instance, ItemStack trigger, boolean hintsOnly) {
        structure.construct(instance, trigger, hintsOnly);
    }

    public int survivalConstruct(MTE instance, ItemStack trigger, int elementBudget, ISurvivalBuildEnvironment env) {
        return structure.survivalConstruct(instance, trigger, elementBudget, env);
    }
}