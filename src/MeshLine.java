import toxi.geom.Line3D;
import toxi.geom.Vec3D;

/**
 * Created by omar on 10/27/2016.
 */

public class MeshLine {
    private CaveScan p;
    Vec3D start;
    Vec3D end;
    public int var;

    MeshLine(CaveScan _p, Vec3D s, Vec3D e, int v) {
        p = _p;
        start = s.copy();
        end = e.copy();
        var = v;
    }

    public void drawline() {
        p.strokeWeight(1f);
        p.stroke(var);
        Line3D l =new Line3D(start,end);
        p.gfx.line(l);
    }

}

