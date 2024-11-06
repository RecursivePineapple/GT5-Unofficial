package tectech.thing.metaTileEntity.multi;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.lazy;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static gregtech.api.casing.Casings.AdvancedMolecularCasing;
import static gregtech.api.casing.Casings.ContainmentFieldGenerator;
import static gregtech.api.casing.Casings.MolecularCasing;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.ExoticEnergy;

import java.util.Arrays;
import java.util.Collection;
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
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.util.HatchElementBuilder;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.shutdown.ShutDownReason;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import tectech.mechanics.boseEinsteinCondensate.BECFactoryGrid;
import tectech.mechanics.boseEinsteinCondensate.BECInventory;
import tectech.mechanics.boseEinsteinCondensate.CondensateStack;
import tectech.thing.block.BlockQuantumGlass;
import tectech.thing.metaTileEntity.multi.base.MTEBECMultiblockBase;
import tectech.thing.metaTileEntity.multi.structures.BECGeneratorStructureDef;

public class MTEBECStorage extends MTEBECMultiblockBase<MTEBECStorage> implements BECInventory {
    
    private final Object2LongOpenHashMap<Materials> mStoredCondensate = new Object2LongOpenHashMap<>();

    public MTEBECStorage(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public MTEBECStorage(MTEBECStorage prototype) {
        super(prototype);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new MTEBECStorage(this);
    }

    @Override
    public String[][] getDefinition() {
        return BECGeneratorStructureDef.BEC_CONTAINMENT_FIELD;
    }

    @Override
    public IStructureDefinition<MTEBECStorage> compile(String[][] definition) {
        return StructureDefinition.<MTEBECStorage>builder()
            .addShape(STRUCTURE_PIECE_MAIN, definition)
            .addElement('A', MolecularCasing.asElement())
            .addElement('B', AdvancedMolecularCasing.asElement())
            .addElement('C', ContainmentFieldGenerator.asElement())
            .addElement('D', lazy(() -> ofBlock(BlockQuantumGlass.INSTANCE, 0)))
            .addElement('E', lazy(() -> 
                HatchElementBuilder.<MTEBECStorage>builder()
                    .anyOf(BECHatches.Hatch)
                    .casingIndex(MolecularCasing.getTextureId())
                    .dot(2)
                    .buildAndChain(MolecularCasing.asElement())
            ))
            .addElement('1', lazy(() -> 
                HatchElementBuilder.<MTEBECStorage>builder()
                    .anyOf(Energy, ExoticEnergy)
                    .casingIndex(MolecularCasing.getTextureId())
                    .dot(1)
                    .buildAndChain(MolecularCasing.asElement())
            ))
            .build();
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();

        // spotless:off
        tt.addMachineType("Bose-Einstein Condensate Storage")
            .addInfo("Stores fancy atoms")
            .toolTipFinisher(GTValues.AuthorPineapple);
        // spotless:on

        return tt;
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

        for (var e : mStoredCondensate.object2LongEntrySet()) {
            ret.append(EnumChatFormatting.AQUA)
                .append(e.getKey().mLocalizedName)
                .append(" Condensate")
                .append(EnumChatFormatting.WHITE)
                .append(" x ")
                .append(EnumChatFormatting.GOLD);
            numberFormat.format(e.getLongValue(), ret);
            ret.append(" L")
                .append(EnumChatFormatting.WHITE);
            ret.append('\n');
        }

        return ret.toString();
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);

        aNBT.setInteger("mCondCount", mStoredCondensate.size());

        int cursor = 0;

        for (var e : mStoredCondensate.object2LongEntrySet()) {
            aNBT.setString("mCondName." + cursor, e.getKey().mName);
            aNBT.setLong("mCondAmount." + cursor, e.getLongValue());
            cursor++;
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);

        mStoredCondensate.clear();

        int count = aNBT.getInteger("mCondCount");

        for(int i = 0; i < count; i++) {
            Materials material = Materials.get(aNBT.getString("mCondName." + i));
            long amount = aNBT.getLong("mCondAmount." + i);

            if (material != null && material != Materials._NULL && amount > 0) {
                mStoredCondensate.put(material, amount);
            }
        }
    }

    @Override
    protected @NotNull CheckRecipeResult checkProcessing_EM() {
        mMaxProgresstime = 20;
        mEfficiency = 10_000;

        useLongPower = true;
        lEUt = 0;

        if (network == null) {
            BECFactoryGrid.INSTANCE.addElement(this);
        }

        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

    @Override
    public void stopMachine(@Nonnull ShutDownReason reason) {
        super.stopMachine(reason);
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
        return mStoredCondensate.object2LongEntrySet()
            .stream()
            .map(e -> new CondensateStack(e.getKey(), e.getLongValue()))
            .collect(Collectors.toList());
    }

    @Override
    public void addCondensate(Collection<CondensateStack> stacks) {
        for (CondensateStack stack : stacks) {
            mStoredCondensate.merge(stack.material, stack.amount, Long::sum);
            stack.amount = 0;
        }
    }

    @Override
    public boolean removeCondensate(Collection<CondensateStack> stacks) {
        return false; // todo
    }
}
