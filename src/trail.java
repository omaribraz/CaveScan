/**
 * Created by omar on 10/14/2016.
 */

import toxi.geom.Vec3D;

public class trail extends Vec3D {
    private CaveScan p;
    Vec3D orientation;
    public int  trailNo = 1000;
    public int strength = trailNo;
    Boid b;

    trail(CaveScan _p, Vec3D pos, Vec3D o, Boid _b) {
        super(pos);
        p = _p;
        b = _b;
        orientation = o.copy();
        orientation = orientation.normalize();
        b.trailPop.add(this);
    }

    void update() {
        strength = strength - 5;
        if (strength < 1) {
            b.trailPop.remove(this);
        }
    }
}
