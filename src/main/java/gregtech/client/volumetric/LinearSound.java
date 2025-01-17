package gregtech.client.volumetric;

import net.minecraft.entity.player.EntityPlayer;

import org.joml.Vector3f;

import com.gtnewhorizon.structurelib.util.Vec3Impl;

import gregtech.api.enums.GTValues;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;
import gregtech.api.util.GTUtility;

public class LinearSound {

    private Vec3Impl start, end;
    private float maxRange = 64f * 64f, distance, weight1 = 1, weight2 = 0, centre = 0;

    public LinearSound setCoords(int x1, int y1, int z1, int x2, int y2, int z2) {
        start = new Vec3Impl(x1, -y1, z1);
        end = new Vec3Impl(x2, -y2, z2);
        distance = v(end).sub(v(start))
            .length();

        return this;
    }

    public LinearSound setRange(float range) {
        this.maxRange = range * range;

        return this;
    }

    public LinearSound setCentre(float weight1, float weight2, float centre) {
        this.weight1 = weight1;
        this.weight2 = weight2;
        this.centre = centre;

        return this;
    }

    public Vector3f getPosition(MTEEnhancedMultiBlockBase<?> multi) {
        EntityPlayer p = GTValues.GT.getThePlayer();

        IGregTechTileEntity igte = multi.getBaseMetaTileEntity();

        double dist2 = l2(p.posX - igte.getXCoord(), p.posY - igte.getYCoord(), p.posZ - igte.getZCoord());

        if (dist2 > maxRange) return null;

        Vector3f machine = new Vector3f(igte.getXCoord(), igte.getYCoord(), igte.getZCoord());

        Vector3f start = v(
            multi.getExtendedFacing()
                .getWorldOffset(this.start)).add(machine);
        Vector3f end = v(
            multi.getExtendedFacing()
                .getWorldOffset(this.end)).add(machine);

        Vector3f player = new Vector3f((float) p.posX, (float) p.posY, (float) p.posZ);

        Vector3f dir = new Vector3f(end).sub(start)
            .normalize();
        float k = new Vector3f(player).sub(start)
            .dot(dir);

        k = GTUtility.clamp(k, 0, distance);

        k = (k * weight1 + centre * weight2) / (weight1 + weight2);

        return new Vector3f(start.x + dir.x * k, start.y + dir.y * k, start.z + dir.z * k);
    }

    private static Vector3f v(Vec3Impl v) {
        return new Vector3f(v.get0(), v.get1(), v.get2());
    }

    private static double l2(double dx, double dy, double dz) {
        return dx * dx + dy * dy + dz * dz;
    }
}
