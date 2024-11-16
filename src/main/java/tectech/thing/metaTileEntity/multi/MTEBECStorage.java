package tectech.thing.metaTileEntity.multi;

import static gregtech.api.casing.Casings.AdvancedMolecularCasing;
import static gregtech.api.casing.Casings.ContainmentFieldGenerator;
import static gregtech.api.casing.Casings.MolecularCasing;
import static gregtech.api.casing.Casings.QuantumGlass;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.ExoticEnergy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.TierEU;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.util.HatchElementBuilder;
import gregtech.api.util.IntFraction;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.shutdown.ShutDownReason;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryGrid;
import tectech.mechanics.boseEinsteinCondensate.BECInventory;
import tectech.mechanics.boseEinsteinCondensate.CondensateStack;
import tectech.thing.metaTileEntity.multi.base.LedStatus;
import tectech.thing.metaTileEntity.multi.base.MTEBECMultiblockBase;
import tectech.thing.metaTileEntity.multi.base.Parameters;
import tectech.thing.metaTileEntity.multi.structures.BECStructureDefinitions;

public class MTEBECStorage extends MTEBECMultiblockBase<MTEBECStorage> implements BECInventory {
    
    private final HashMap<Object, CondensateStack> mStoredCondensate = new HashMap<>();

    public MTEBECStorage(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    protected MTEBECStorage(MTEBECStorage prototype) {
        super(prototype);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEBECStorage(this);
    }

    @Override
    public String[][] getDefinition() {
        return BECStructureDefinitions.BEC_CONTAINMENT_FIELD;
    }

    @Override
    public IStructureDefinition<MTEBECStorage> compile(String[][] definition) {
        structure.addCasing('A', MolecularCasing);
        structure.addCasing('B', AdvancedMolecularCasing);
        structure.addCasing('C', ContainmentFieldGenerator);
        structure.addCasing('D', QuantumGlass);

        return StructureDefinition.<MTEBECStorage>builder()
            .addShape(STRUCTURE_PIECE_MAIN, definition)
            .addElement('A', MolecularCasing.asElement())
            .addElement('B', AdvancedMolecularCasing.asElement())
            .addElement('C', ContainmentFieldGenerator.asElement())
            .addElement('D', QuantumGlass.asElement())
            .addElement('E', HatchElementBuilder.<MTEBECStorage>builder()
                    .anyOf(BECHatches.Hatch)
                    .casingIndex(MolecularCasing.getTextureId())
                    .dot(2)
                    .buildAndChain(structure.getCasingAdder('E', MolecularCasing, 6)))
            .addElement('1', HatchElementBuilder.<MTEBECStorage>builder()
                    .anyOf(Energy, ExoticEnergy, HatchElement.InputData, HatchElement.Param)
                    .casingIndex(MolecularCasing.getTextureId())
                    .dot(1)
                    .buildAndChain(structure.getCasingAdder('1', MolecularCasing, 8)))
            .build();
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();

        // spotless:off
        tt.addMachineType("Bose-Einstein Condensate Storage")
            .addInfo("Stores fancy atoms")
            .beginStructureBlock(structure.size.x, structure.size.y, structure.size.z, false)
            .addController("Front Center (bottom layer)")
            .pipe(tt2 -> {
                structure.addCasingInfoRange(tt2, MolecularCasing);
                structure.addCasingInfoExact(tt2, AdvancedMolecularCasing);
                structure.addCasingInfoExact(tt2, ContainmentFieldGenerator);
                structure.addCasingInfoExact(tt2, QuantumGlass);
            })
            .addEnergyHatch("Any " + MolecularCasing.getLocalizedName() + " on the front face", 1)
            .addOtherStructurePart(HatchElement.InputData.getDisplayName(), "Any " + MolecularCasing.getLocalizedName() + " on the front face", 1)
            .addOtherStructurePart(HatchElement.Param.getDisplayName(), "Any " + MolecularCasing.getLocalizedName() + " on the front face", 1)
            .addOtherStructurePart(BECHatches.Hatch.getDisplayName(), "The marked locations (0 to 6)", 2)
            .toolTipFinisher(GTValues.AuthorPineapple);
        // spotless:on

        return tt;
    }

    protected Parameters.Group.ParameterIn fieldStrength;
    protected Parameters.Group.ParameterOut optimalCapacity, amountStored, requiredComputation;

    @Override
    protected void parametersInstantiation_EM() {
        Parameters.Group hatch0 = parametrization.getGroup(0);
        fieldStrength = hatch0.makeInParameter(
            0, TierEU.LV,
            (t, iParameter) -> "Field Strength (EU/t)",
            (t, iParameter) -> iParameter.get() < 0 ? LedStatus.STATUS_TOO_LOW : LedStatus.STATUS_OK);
        optimalCapacity = hatch0.makeOutParameter(
            0, 0,
            (t, iParameter) -> "Optimal Capacity (L)",
            (t, iParameter) -> LedStatus.STATUS_OK);
        amountStored = hatch0.makeOutParameter(
            1, 0,
            (t, iParameter) -> "Total Stored Condensate (L)",
            (t, iParameter) -> {
                double ratio = amountStored.get() / optimalCapacity.get();

                if (ratio < 0.5) return LedStatus.STATUS_OK;
                if (ratio < 1) return LedStatus.STATUS_HIGH;
                return LedStatus.STATUS_TOO_HIGH;
            });

        Parameters.Group hatch1 = parametrization.getGroup(1);
        requiredComputation = hatch1.makeOutParameter(
            0, 0,
            (t, iParameter) -> "Required Computation",
            (t, iParameter) -> LedStatus.STATUS_OK);
    }

    private long getOptimalCapacity(long fieldStrength) {
        return fieldStrength;
    }

    @Override
    protected void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {
        super.drawTexts(screenElements, inventorySlot);

        screenElements.widget(
            TextWidget.dynamicString(this::generateStoredCondensateText)
                .setTextAlignment(Alignment.CenterLeft));
    }

    protected String generateStoredCondensateText() {
        StringBuffer ret = new StringBuffer();

        numberFormat.setMinimumFractionDigits(0);
        numberFormat.setMaximumFractionDigits(2);

        for (var e : mStoredCondensate.values()) {
            ret.append(EnumChatFormatting.AQUA)
                .append(e.getPreview().getDisplayName())
                .append(EnumChatFormatting.WHITE)
                .append(" x ")
                .append(EnumChatFormatting.GOLD);
            numberFormat.format(e.amount, ret);
            ret.append(" L")
                .append(EnumChatFormatting.WHITE);
            ret.append('\n');
        }

        return ret.toString();
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);

        aNBT.setTag("condensate", CondensateStack.save(new ArrayList<>(mStoredCondensate.values())));
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);

        List<CondensateStack> loaded = CondensateStack.load((NBTTagList)aNBT.getTag("condensate"));

        mStoredCondensate.clear();
        for (CondensateStack stack : loaded) {
            mStoredCondensate.put(stack.material, stack);
        }
    }

    @Override
    public void onPreTick(IGregTechTileEntity base, long aTick) {
        super.onPreTick(base, aTick);

        if (!base.isAllowedToWork() && !base.isActive() && aTick % 20 == 0) {
            long fieldStrength = (long) this.fieldStrength.get();
            long optimalCapacity = getOptimalCapacity(fieldStrength);
            this.optimalCapacity.set(optimalCapacity);
            this.requiredComputation.set(fieldStrength / 1000);
        }
    }

    @Override
    protected @NotNull CheckRecipeResult checkProcessing_EM() {
        long fieldStrength = (long) this.fieldStrength.get();
        long optimalCapacity = getOptimalCapacity(fieldStrength);
        this.optimalCapacity.set(optimalCapacity);
        this.requiredComputation.set(eRequiredData = fieldStrength / 1000);

        mMaxProgresstime = 20;
        mEfficiency = 10_000;
        useLongPower = true;
        lEUt = -fieldStrength;

        if (network == null) {
            BECFactoryGrid.INSTANCE.addElement(this);
        }

        double stored = 0;

        for (CondensateStack stack : mStoredCondensate.values()) {
            stored += stack.amount;
        }

        if (stored > optimalCapacity) {
            IntFraction decay = new IntFraction(9, 10);
    
            mStoredCondensate.forEach((mat, stack) -> stack.amount = decay.apply(stack.amount));
        }

        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

    @Override
    public void stopMachine(@Nonnull ShutDownReason reason) {
        super.stopMachine(reason);
        mStoredCondensate.clear();
        BECFactoryGrid.INSTANCE.removeElement(this);
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        BECFactoryGrid.INSTANCE.removeElement(this);
    }

    @Override
    public List<Pair<Class<?>, Object>> getComponents() {
        return Arrays.asList(
            Pair.of(BECInventory.class, this)
        );
    }

    @Override
    public @Nullable List<CondensateStack> getContents() {
        return mStoredCondensate.values()
            .stream()
            .map(CondensateStack::copy)
            .collect(Collectors.toList());
    }

    @Override
    public void addCondensate(Collection<CondensateStack> stacks) {
        for (CondensateStack stack : stacks) {
            CondensateStack existing = mStoredCondensate.get(stack.material);

            if (existing == null) {
                existing = new CondensateStack(stack.material, 0);
                mStoredCondensate.put(stack.material, existing);
            }

            existing.amount += stack.amount;
            stack.amount = 0;
        }

        double stored = 0;

        for (CondensateStack stack : mStoredCondensate.values()) {
            stored += stack.amount;
        }

        amountStored.set(stored);
    }

    @Override
    public boolean removeCondensate(Collection<CondensateStack> stacks) {
        boolean consumedEverything = true;

        for (CondensateStack stack : stacks) {
            CondensateStack stored = mStoredCondensate.get(stack.material);
            
            if (stored == null) {
                consumedEverything = false;
                continue;
            }

            long toConsume = Math.min(stored.amount, stack.amount);

            stored.amount -= toConsume;
            stack.amount -= toConsume;

            if (stack.amount > 0) consumedEverything = false;
        }

        double stored = 0;

        for (CondensateStack stack : mStoredCondensate.values()) {
            stored += stack.amount;
        }

        amountStored.set(stored);

        return consumedEverything;
    }
}
