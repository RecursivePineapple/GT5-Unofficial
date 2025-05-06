package tectech.thing.metaTileEntity.hatch;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import gregtech.api.enums.Textures;
import gregtech.api.enums.VoltageIndex;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.render.TextureFactory;
import gregtech.common.gui.modularui.widget.EnumCycleButtonWidget;

public class MTEHatchBECIONodeController extends MTEBaseFactoryHatch {

    private Mode mode = Mode.PAUSE_INSTANT;

    public enum Mode {

        PAUSE_INSTANT,
        PAUSE_STEP;

        @Override
        public String toString() {
            return switch (this) {
                case PAUSE_INSTANT -> "Pause Immediately";
                case PAUSE_STEP -> "Pause Next Step";
            };
        }

        public String getTooltip() {
            return switch (this) {
                case PAUSE_INSTANT -> "The I/O Node will stop processing when it receives a redstone signal";
                case PAUSE_STEP -> "The I/O Node will stop upon transitioning into a new input step when it receives a redstone signal";
            };
        }
    }

    public MTEHatchBECIONodeController(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, VoltageIndex.UEV, null);
    }

    protected MTEHatchBECIONodeController(MTEHatchBECIONodeController prototype) {
        super(prototype);
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity igte) {
        return new MTEHatchBECIONodeController(this);
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

    public Mode getMode() {
        return mode;
    }

    public boolean receivingSignal() {
        return getBaseMetaTileEntity().getRedstone();
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer, ForgeDirection side,
        float aX, float aY, float aZ) {
        openGui(aPlayer);
        return true;
    }

    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
        builder.widgets(
            new FakeSyncWidget.IntegerSyncer(() -> mode.ordinal(), index -> mode = Mode.values()[index]),

            new TextWidget("Mode:").setDefaultColor(COLOR_TEXT_GRAY.get())
                .setTextAlignment(Alignment.CenterLeft)
                .setPos(10, 14),
            new EnumCycleButtonWidget<>(Mode.class, () -> mode, value -> mode = value).setPos(40, 9)
                .setSize(100, 18));
    }
}
