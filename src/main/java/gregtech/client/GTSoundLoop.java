package gregtech.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.joml.Vector3f;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

@SideOnly(Side.CLIENT)
public class GTSoundLoop extends MovingSound {

    public static final float VOLUME_RAMP = 0.0625f;
    private final boolean whileActive;
    private final boolean whileInactive;
    private final int worldID, machineX, machineY, machineZ;

    private boolean fadeMe = false;
    private float targetVolume = 1, volumeRamp;

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
        volumeRamp = VOLUME_RAMP;
        volume = VOLUME_RAMP;
        if (base.getMetaTileEntity() instanceof ISoundLoopAware loopAware) {
            loopAware.modifySoundLoop(this);
        }
    }

    public GTSoundLoop(ResourceLocation p_i45104_1_, IGregTechTileEntity base, boolean stopWhenActive,
        boolean stopWhenInactive, float volumeRamp) {
        super(p_i45104_1_);
        this.whileActive = stopWhenActive;
        this.whileInactive = stopWhenInactive;
        xPosF = machineX = base.getXCoord();
        yPosF = machineY = base.getYCoord();
        zPosF = machineZ = base.getZCoord();
        worldID = base.getWorld().provider.dimensionId;
        repeat = true;
        this.volumeRamp = volumeRamp;
        volume = volumeRamp;
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
            volume -= volumeRamp * targetVolume;
            if (volume <= 0) {
                volume = 0;
                donePlaying = true;
            }
        } else if (volume < targetVolume) {
            volume += volumeRamp * targetVolume;
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

    public void setPosition(Vector3f v) {
        xPosF = v.x;
        yPosF = v.y;
        zPosF = v.z;
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

    public void setDonePlaying(boolean isDone) {
        this.donePlaying = isDone;
    }
}
