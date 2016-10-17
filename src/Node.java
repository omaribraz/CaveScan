/**
 * Created by omar on 10/17/2016.
 */

import toxi.geom.Line3D;
import toxi.geom.Vec3D;

import java.util.ArrayList;

public class Node extends Vec3D {

    private CaveScan p;
    int age = 0;
    ArrayList<Line3D> lines;


    Node(CaveScan _p, Vec3D o) {
        super(o);
        p = _p;
        lines = new ArrayList<>();
    }

    void update() {

        if (p.random(100) < 1) {
           Vec3D dir = randomdir(new Vec3D(1, 0, 0), 50, p.PI/4);
 //           Vec3D dir = randomVector();
            dir.scaleSelf(50);
            Vec3D p2 = this.copy().addSelf(dir);
            Line3D l = new Line3D(this, p2);
            lines.add(l);

            Node b = new Node(p, p2);
            p.nodes.add(b);
        }
        render();
        age = age + 1;
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
        Vec3D var6 = var0.copy().getLimited(var1).getRotatedAroundAxis(var5,p.random(var3));
        return var6.getRotatedAroundAxis(var0.copy(),p.random(p.PI*2));
    }
}

