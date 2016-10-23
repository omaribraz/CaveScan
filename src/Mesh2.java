/**
 * Created by omar on 10/23/2016.
 */

import toxi.geom.IsectData3D;
import toxi.geom.Ray3D;
import toxi.geom.Triangle3D;
import toxi.geom.mesh.Face;
import toxi.geom.mesh.TriangleMesh;
import toxi.geom.mesh.WETriangleMesh;

import java.util.Iterator;

class Mesh2 extends TriangleMesh {

    private CaveScan p;

    Mesh2(CaveScan _p, String a) {
        super(a);
        p = _p;
    }

    public boolean intersectsRay(Ray3D var1) {
        Triangle3D var2 = this.intersector.getTriangle();
        Iterator var3 = this.faces.iterator();

        do {
            if(!var3.hasNext()) {
                return false;
            }

            Face var4 = (Face)var3.next();
            var2.set(var4.a, var4.b, var4.c);
        } while(!this.intersector.intersectsRay(var1));

        return true;
    }


    public int intersectsRayc(Ray3D var1) {
        Triangle3D var2 = this.intersector.getTriangle();
        Iterator var3 = this.faces.iterator();
        int count = 0;

        do {
            if (!var3.hasNext()) {
                return count = 0;
            }
            Face var4 = (Face) var3.next();
            var2.set(var4.a, var4.b, var4.c);
            count++;
        } while (!this.intersector.intersectsRay(var1));

        return count;
    }



}
