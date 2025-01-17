package gregtech.client.volumetric;

import net.minecraft.entity.player.EntityPlayer;

import org.joml.Vector3f;

import com.gtnewhorizon.structurelib.util.Vec3Impl;

import gregtech.api.enums.GTValues;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;

public class CircularSound {

    private Vec3Impl centre, normal;
    private float maxRange = 64f * 64f, radius;

    public CircularSound setCoords(int cx, int cy, int cz, int nx, int ny, int nz, float radius) {
        this.centre = new Vec3Impl(cx, -cy, cz);
        this.normal = new Vec3Impl(nx, -ny, nz);
        this.radius = radius;

        return this;
    }

    public CircularSound setRange(float range) {
        this.maxRange = range * range;

        return this;
    }

    private static final Vector3f ZERO = new Vector3f();
    private static final Vector3f FORWARD = new Vector3f(1, 0, 0);
    private static final Vector3f UP = new Vector3f(0, 1, 0);

    public Vector3f getPosition(MTEEnhancedMultiBlockBase<?> multi, boolean far) {
        EntityPlayer player = GTValues.GT.getThePlayer();

        IGregTechTileEntity igte = multi.getBaseMetaTileEntity();

        double dist2 = l2(
            player.posX - igte.getXCoord(),
            player.posY - igte.getYCoord(),
            player.posZ - igte.getZCoord());

        if (dist2 > maxRange) return null;

        var machine = new Vector3f(igte.getXCoord(), igte.getYCoord(), igte.getZCoord());

        var c = v(
            multi.getExtendedFacing()
                .getWorldOffset(this.centre)).add(machine);
        var n = v(
            multi.getExtendedFacing()
                .getWorldOffset(this.normal));

        var p = new Vector3f((float) player.posX, (float) player.posY, (float) player.posZ);

        p.sub(c);

        var q = new Vector3f(p).sub(new Vector3f(n).mul(n.dot(p)));

        if (q.equals(ZERO, 0.001f)) {
            if (n.maxComponent() == 1) {
                q.set(n)
                    .cross(UP);
            } else {
                q.set(n)
                    .cross(FORWARD);
            }
        }

        var r = new Vector3f(q).mul(radius / q.length());

        if (far) r.mul(-1);

        return r.add(c);
    }

    private static Vector3f v(Vec3Impl v) {
        return new Vector3f(v.get0(), v.get1(), v.get2());
    }

    private static double l2(double dx, double dy, double dz) {
        return dx * dx + dy * dy + dz * dz;
    }
}
