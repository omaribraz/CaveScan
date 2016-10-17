/**
 * Created by omar on 10/17/2016.
 */

import toxi.geom.Line3D;
import toxi.geom.Vec3D;

import java.util.ArrayList;
import java.util.List;

import toxi.physics3d.*;
import toxi.physics3d.behaviors.AttractionBehavior3D.*;

public class Node extends VerletParticle3D {

    private CaveScan p;
    int age = 0;
    ArrayList<Line3D> lines;

    double linkLength = 10;
    double linkThreshold = 5;


    Node(CaveScan _p, Vec3D o) {
        super(o);
        p = _p;
        lines = new ArrayList<>();
    }

    public void update() {

        if (p.random(100) < 1) {
            Vec3D dir = randomdir(new Vec3D(1, 0, 0), 50, p.PI / 4);
            //           Vec3D dir = randomVector();
            dir.scaleSelf(50);
            Vec3D p2 = this.copy().addSelf(dir);
            Line3D l = new Line3D(this, p2);
            lines.add(l);

            Node b = new Node(p, p2);
            p.physics.addParticle(b);
 //           VerletSpring3D l1 = new VerletSpring3D(b, this, 100, 0.01f);
 //           p.physics.addSpring(l1);
//            p.physics.addBehavior(new AttractionBehavior3D(p, 20, -1.2f, 0.01f));
        }
 //       connect(p.physics.particles);
        render();
        age = age + 1;
    }


    void connect(List nodep) {
        for (int i = 0; i < nodep.size(); i++) {
            for (VerletParticle3D n1 : p.physics.particles) {
                if (n1 != this) {
                    if (n1.distanceTo(this) < linkThreshold) {
                        if (p.random(1000) < 1) {
                            VerletSpring3D l = new VerletSpring3D(n1, this, 100, 0.01f);
  //                          p.physics.addSpring(l);
                            Line3D l3 = new Line3D(n1,this);
                            lines.add(l3);
                        }
                    }
                }
            }
        }
    }


    void render() {
        p.stroke(225);
        p.point(this.x, this.y, this.z);
        for (Line3D l : lines) {
            p.stroke(220);
            p.line(l.a.x, l.a.y, l.a.z, l.b.x, l.b.y, l.b.z);
        }
    }

    Vec3D randomdir(Vec3D var0, float var1, float var3) {
        Vec3D var5 = new Vec3D();
        Vec3D polar = var0.copy().toCartesian();
        if ((polar.x == 0) && (polar.y == 0)) {
            var5 = new Vec3D(1, 0, 0).crossSelf(var0);
        } else {
            var5 = new Vec3D(0, 0, 1).crossSelf(var0);
        }
        Vec3D var6 = var0.copy().getLimited(var1).getRotatedAroundAxis(var5, p.random(var3));
        return var6.getRotatedAroundAxis(var0.copy(), p.random(p.PI * 2));
    }
}

