package tectech.thing.metaTileEntity.hatch;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.VanillaButtonWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.NumericWidget;

import gregtech.api.enums.NaniteTier;
import gregtech.api.enums.Textures;
import gregtech.api.enums.VoltageIndex;
import gregtech.api.gui.modularui.GTUIInfos;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTUtility;

public class MTEHatchNaniteDetector extends MTEBaseFactoryHatch {

    private int configuredTier;
    private @Nullable NaniteTier requiredTier;
    private Op op = Op.EQ;

    enum Op {

        EQ,
        LT,
        GT,
        LTEQ,
        GTEQ;

        public String getDisplayString() {
            return switch (this) {
                case EQ -> "=";
                case LT -> "<";
                case GT -> ">";
                case LTEQ -> "<=";
                case GTEQ -> ">=";
            };
        }
    }

    public MTEHatchNaniteDetector(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, VoltageIndex.UEV, null);
    }

    protected MTEHatchNaniteDetector(MTEHatchNaniteDetector prototype) {
        super(prototype);
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity igte) {
        return new MTEHatchNaniteDetector(this);
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture baseTexture) {
        return new ITexture[] { baseTexture, TextureFactory.of(Textures.BlockIcons.OVERLAY_HATCH_NANITE_DETECTOR) };
    }

    @Override
    public ITexture[] getTexturesActive(ITexture baseTexture) {
        return new ITexture[] { baseTexture, TextureFactory.builder()
            .addIcon(Textures.BlockIcons.OVERLAY_HATCH_NANITE_DETECTOR_GLOW)
            .glow()
            .build() };
    }

    public void setRequiredTier(@Nullable NaniteTier requiredTier) {
        this.requiredTier = requiredTier;
    }

    @Override
    public boolean allowGeneralRedstoneOutput() {
        return true;
    }

    @Override
    public void onPostTick(IGregTechTileEntity baseMetaTileEntity, long tick) {
        super.onPostTick(baseMetaTileEntity, tick);

        boolean signal = requiredTier != null && switch (op) {
            case EQ -> requiredTier.tier == configuredTier;
            case LT -> requiredTier.tier < configuredTier;
            case GT -> requiredTier.tier > configuredTier;
            case LTEQ -> requiredTier.tier <= configuredTier;
            case GTEQ -> requiredTier.tier >= configuredTier;
        };

        IGregTechTileEntity igte = getBaseMetaTileEntity();

        if (igte == null || igte.isDead()) return;

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            igte.setStrongOutputRedstoneSignal(dir, (byte) 0);
        }

        igte.setStrongOutputRedstoneSignal(baseMetaTileEntity.getFrontFacing(), (byte) (signal ? 15 : 0));
        igte.setActive(signal);
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer, ForgeDirection side,
        float aX, float aY, float aZ) {
        GTUIInfos.openGTTileEntityUI(aBaseMetaTileEntity, aPlayer);
        return true;
    }

    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        builder.widgets(
            new FakeSyncWidget.IntegerSyncer(() -> op.ordinal(), index -> op = Op.values()[index]),
            new VanillaButtonWidget().setDisplayString(op.getDisplayString())
                .setOnClick((clickData, widget) -> {
                    int offset = 0;

                    if (clickData.mouseButton == 0) {
                        offset = 1;
                    } else if (clickData.mouseButton == 1) {
                        offset = -1;
                    }

                    op = Op.values()[GTUtility.mod(op.ordinal() + offset, Op.values().length)];

                    ((VanillaButtonWidget) widget).setDisplayString(op.getDisplayString());
                    widget.notifyTooltipChange();
                })
                .dynamicTooltip(
                    () -> Arrays.asList(
                        String.format(
                            "Emit a redstone signal when the required nanite tier is %s than the threshold",
                            switch (op) {
                            case EQ -> "equal to";
                            case LT -> "less than";
                            case LTEQ -> "less than or equal to";
                            case GT -> "greater than";
                            case GTEQ -> "greater than or equal to";
                            })))
                .setPos(10, 8),
            new NaniteTierNumericWidget(),
            new TextWidget(StatCollector.translateToLocal("GT5U.gui.text.nanite_threshold"))
                .setDefaultColor(COLOR_TEXT_GRAY.get())
                .setTextAlignment(Alignment.CenterLeft)
                .setPos(90, 14),
            TextWidget.dynamicString(() -> "Current: " + requiredTier)
                .setDefaultColor(COLOR_TEXT_GRAY.get())
                .setTextAlignment(Alignment.CenterLeft)
                .setPos(20, 35));
    }

    private class NaniteTierNumericWidget extends NumericWidget {

        public NaniteTierNumericWidget() {
            setBounds(
                1,
                Stream.of(NaniteTier.values())
                    .mapToInt(t -> t.tier)
                    .max()
                    .getAsInt());
            setGetter(() -> (double) configuredTier);
            setSetter((value) -> {
                configuredTier = (int) value;
                this.notifyTooltipChange();
            });
            setScrollValues(1, 1, 1);
            setMaximumFractionDigits(0);
            setTextColor(Color.WHITE.dark(1));
            setTextAlignment(Alignment.CenterLeft);
            setFocusOnGuiOpen(true);
            setBackground(GTUITextures.BACKGROUND_TEXT_FIELD.withOffset(-1, -1, 2, 2));
            dynamicTooltip(
                () -> Arrays.asList(
                    String.format(
                        "Nanites: %s",
                        Stream.of(NaniteTier.values())
                            .filter(t -> t.tier == configuredTier)
                            .map(t -> t.getMaterial().mLocalizedName)
                            .sorted()
                            .collect(Collectors.joining(", ")))));
            setPos(34, 12);
            setSize(50, 12);
        }

        @Override
        public ClickResult onClick(int buttonId, boolean doubleClick) {
            NaniteTier inCursor = NaniteTier.fromStack(
                getContext().getCursor()
                    .getItemStack());

            if (buttonId == 0 && inCursor != null) {
                this.setValue(inCursor.tier);
                return ClickResult.ACCEPT;
            }

            return super.onClick(buttonId, doubleClick);
        }
    }
}
