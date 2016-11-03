/**
 * Created by omar on 10/31/2016.
 */

import toxi.geom.ReadonlyVec3D;
import toxi.geom.Shape3D;
import toxi.geom.Sphere;
import toxi.geom.Vec3D;
import toxi.geom.mesh.Mesh3D;

import java.util.List;

public class Pathagent extends Vec3D {
    private CaveScan p;
    private Vec3D vel;
    public Sphere a;
    private float dia = 60;

    Pathagent(CaveScan _p, Vec3D pos) {
        super(pos);
        p = _p;
        vel = new Vec3D(0, 0, 0);
        this.z += 10;
    }

    public void run() {
        move();
        update();
        render();
    }

    private void update() {
        vel.limit(20.7f);
        p.ballvel += vel.magnitude();
        this.addSelf(vel);
        vel.scaleSelf(0);
    }

    private void move() {
        List<Vec3D> cavepts2 = null;
        cavepts2 = p.meshoctree.getPointsWithinSphere(this.copy(), dia);
        if (cavepts2 != null) {
            if (cavepts2.size() > 0) {
                Vec3D var1 = new Vec3D();
                float var3 = 3.4028235E38F;
                for (int i = 0; i < cavepts2.size(); i++) {
                    Vec3D vara = cavepts2.get(i);
                    float dista = vara.distanceToSquared(this);
                    if (dista < var3) {
                        var1 = vara;
                        var3 = dista;
                    }
                }
                Vec3D a = this.copy().subSelf(var1);

                float rad = var1.distanceTo(this);
                if (rad < (dia + 5)) {
//                    a.normalize();
                    a = a.copy().scaleSelf(1 / rad);
                    vel.addSelf(a);
                } else {
                    vel.scaleSelf(0);
                }
            }
        }
    }

    private void render() {
//        p.stroke(255, 0, 0);
//        p.pushMatrix();
//        p.translate(x, y, z);
//        p.sphere(dia);
//        p.popMatrix();
//        Mesh3D a1 = a.toMesh(a,256);
        a = new Sphere(this, dia);
        p.noFill();
        p.stroke(255, 0, 0);
        p.gfx.sphere(a, 8);
    }


}
