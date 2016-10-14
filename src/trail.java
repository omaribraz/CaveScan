/**
 * Created by omar on 10/14/2016.
 */
import toxi.geom.Vec3D;
import processing.core.PApplet;

public class trail extends Vec3D {
    private CaveScan p;
    Vec3D orientation;
    int strength = 1000;

    trail(CaveScan _p, Vec3D pos, Vec3D o) {
        super(pos);
        p = _p;
        orientation = o.copy();
        orientation = orientation.normalize();
    }

    void update() {
        strength = strength - 5;
        render();
        if (strength < 1) {
            p.flock.removeTrail(this);
        }
    }

    void render() {
        p.stroke(strength/5);
        p.strokeWeight(2);
        p.point(x, y, z);
    }
}
