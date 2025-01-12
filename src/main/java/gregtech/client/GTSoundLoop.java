package gregtech.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

@SideOnly(Side.CLIENT)
public class GTSoundLoop extends MovingSound {

    private static final float VOLUME_RAMP = 0.0625f;
    private final boolean whileActive;
    private final boolean whileInactive;
    private final int worldID, machineX, machineY, machineZ;
    
    private boolean fadeMe = false;
    private float targetVolume = 1;

    public GTSoundLoop(ResourceLocation p_i45104_1_, IGregTechTileEntity base, boolean stopWhenActive,
        boolean stopWhenInactive) {
        super(p_i45104_1_);
        this.whileActive = stopWhenActive;
        this.whileInactive = stopWhenInactive;
        xPosF = machineX = base.getXCoord();
        yPosF = machineY = base.getYCoord();
        zPosF = machineZ = base.getZCoord();
        worldID = base.getWorld().provider.dimensionId;
        repeat = true;
        volume = VOLUME_RAMP;
        if (base.getMetaTileEntity() instanceof ISoundLoopAware loopAware) {
            loopAware.modifySoundLoop(this);
        }
    }

    @Override
    public void update() {
        if (donePlaying) {
            return;
        }
        if (fadeMe) {
            volume -= VOLUME_RAMP * targetVolume;
            if (volume <= 0) {
                volume = 0;
                donePlaying = true;
            }
        } else if (volume < targetVolume) {
            volume += VOLUME_RAMP * targetVolume;
        }
        World world = Minecraft.getMinecraft().thePlayer.worldObj;
        donePlaying = world.provider.dimensionId != worldID
            || !world.checkChunksExist(machineX, machineY, machineZ, machineX, machineY, machineZ);
        if (donePlaying) return;
        TileEntity tile = world.getTileEntity(machineX, machineY, machineZ);
        if (tile instanceof IGregTechTileEntity igte) {
            fadeMe |= igte.isActive() ? whileActive : whileInactive;

            if (igte.getMetaTileEntity() instanceof ISoundLoopAware loopAware) {
                loopAware.onSoundLoopTicked(this);
            }
            return;
        }

        donePlaying = true;
    }

    public void setPosition(float x, float y, float z) {
        xPosF = x;
        yPosF = y;
        zPosF = z;
    }

    public boolean isFading() {
        return fadeMe;
    }

    public void setVolume(float volume) {
        if (isFading()) {
            targetVolume = volume;
        } else {
            this.volume = volume;
        }
    }

    public boolean isDonePlaying() {
        return donePlaying;
    }

    public void setDonePlaying(boolean isDone) {
        donePlaying = isDone;
    }
}
