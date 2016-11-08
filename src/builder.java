import processing.core.PApplet;
import toxi.geom.Vec3D;

import java.util.List;

import static processing.core.PApplet.degrees;

/**
 * Created by omar on 11/8/2016.
 */
public class builder extends Vec3D {

    private CaveScan p;
    private Vec3D vel;
    private float dia = 120;

    builder(CaveScan _p, Vec3D pos) {
        super(pos);
        p = _p;
        vel = new Vec3D(0, 0, 0);
    }

    public void run() {
        move();
        update();
        render();
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

                Vec3D a1 = var1.copy().subSelf(this);
                Vec3D a2 = p.Normal.get(var1);

                float ang = a2.angleBetween(a1, true);
                float ang2 = degrees(ang);
                if (ang2 > 90) {
                    a = a.copy().scaleSelf(-1);
                }
                    a = a.copy().scaleSelf(1 / rad);

                    if(a.magnitude()<3){
                        Vec3D b = new Vec3D(p.random(-p.TWO_PI, p.TWO_PI), p.random(-p.TWO_PI, p.TWO_PI), p.random(-p.TWO_PI, p.TWO_PI));
                        a.addSelf(b);
                    }
                    vel.addSelf(a);
                }
            }
        }



    private void update() {
        vel.limit(20.7f);
        this.addSelf(vel);
        vel.scaleSelf(0);
    }

    private void render() {
        float theta = vel.headingXY() + PApplet.radians(90);
        p.stroke(255);
        p.pushMatrix();
        p.translate(x, y, z);
        p.rotate(theta);
        p.obj.setFill(p.color(255, 255, 255));
        p.obj.setStroke(100);
        p.obj.scale(1);
        p.shape(p.obj);
        p.popMatrix();
    }



}
