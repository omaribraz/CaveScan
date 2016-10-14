/**
 * Created by omar on 10/14/2016.
 */
import toxi.geom.PointOctree;
import toxi.geom.Vec3D;


class Octree extends PointOctree {

    private CaveScan p;


    Octree(CaveScan _p, Vec3D o, float d){
        super(o,d);
        p = _p;
    }
    void addBoid(Boid b) {
        addPoint(b);
    }


    void run() {
        updateTree();
        //drawNode(this);
    }

    private void updateTree() {
        empty();
        for (Boid b : p.flock.boids) {
            addBoid(b);
        }
    }

    void draw() {
        drawNode(this);
    }

    private void drawNode(PointOctree n) {
        if (n.getNumChildren() > 0) {
            p.noFill();
            p.stroke(255);
            p.pushMatrix();
            p.translate(n.x, n.y, n.z);
            p.box(n.getNodeSize());
            p.popMatrix();
            PointOctree[] childNodes=n.getChildren();
            for (int i = 0; i < 8; i++) {
                if (childNodes[i] != null) drawNode(childNodes[i]);
            }
        }
    }

}
