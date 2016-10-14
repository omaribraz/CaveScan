/**
 * Created by omar on 10/14/2016.
 */

import processing.core.PApplet;

import java.util.*;

import processing.core.PShape;

import java.util.ArrayList;

import peasy.*;
import toxi.geom.*;
import toxi.geom.mesh.*;
import toxi.processing.ToxiclibsSupport;

import wblut.processing.*;
import wblut.hemesh.*;
import wblut.geom.*;

public class CaveScan extends PApplet {

    PShape obj;
    PShape cone;

    private ToxiclibsSupport gfx;
    WETriangleMesh cave;
    private ArrayList<Vec3D> cavepts;

    private WB_Render render;
    private HE_Mesh mesh;

    private List<HE_Vertex> scanPts = null;
    public ArrayList <Vec3D> scanPtsV = new ArrayList<Vec3D>();

    private HashMap<WB_Coord, Integer> Slope = new HashMap<WB_Coord, Integer>();
    public HashMap<Vec3D, WB_Coord> CaveHe = new HashMap<Vec3D, WB_Coord>();


    Octree meshoctree;
    Octree boidoctree;

    Flock flock;


    public static void main(String[] args) {
        PApplet.main("CaveScan", args);
    }

    public void settings() {
        size(1400, 800, P3D);
        smooth();
    }

    public void setup() {

        obj = loadShape("data/" + "drone.obj");
        obj.scale(3);

        cone = loadShape("data/" + "cone.obj");
        cone.scale(3);

        flock = new Flock(this);

        meshsetup();

        Vec3D a = cave.computeCentroid();
        PeasyCam cam = new PeasyCam(this, a.x, a.y, 0, 2200);

        float DIM = 1500;
        meshoctree = new Octree(this, new Vec3D(-1, -1, -1).scaleSelf(a), DIM * 2);
        meshoctree.addAll(cavepts);

        boidoctree = new Octree(this, new Vec3D(-1, -1, -1).scaleSelf(a), DIM * 2);

        for (int i = 0; i < 10; i++) {
            flock.addBoid(new Boid(this, new Vec3D(random(0, 1200), random(0, 1200), random(190, 350)), new Vec3D(random(-TWO_PI, TWO_PI), random(-TWO_PI, TWO_PI), random(-TWO_PI, TWO_PI))));
        }

        gfx = new ToxiclibsSupport(this);

    }

    public void draw() {
        background(0);

        for (Boid b : flock.boids) {
            boidoctree.addBoid(b);
        }

        boidoctree.run();

        flock.run();

        if (frameCount < 10) {
            for (int i = 0; i < flock.boids.size(); i++) {
                Boid b = flock.boids.get(i);
                b.checkMesh();
            }
        }


        stroke(255, 0, 0);
        noFill();
//        boidoctree.draw();


        meshrun();

    }

    private void meshsetup() {
        mesh = new HEC_FromOBJFile(sketchPath("data/" + "cave.obj")).create();
        cave = (WETriangleMesh) new STLReader().loadBinary(sketchPath("data/" + "cave.stl"), STLReader.WEMESH);

        gfx = new ToxiclibsSupport(this);
        render = new WB_Render(this);

//        WB_KDTree vertexTree = mesh.getVertexTree();
        int novert = mesh.getNumberOfVertices();

        cave.flipVertexOrder();
//        int novert1 = cave.getNumVertices();

        cavepts = (new ArrayList<Vec3D>(cave.getVertices()));


        for (int i = 0; i < novert; i++) {

            WB_Coord mnorm = mesh.getVertexNormal(i);
            WB_Coord vertex1 = mesh.getVertex(i);
            Vec3D vertex = cave.getVertexForID(i);

            float xnPos = (Float) mnorm.xf();
            float ynPos = (Float) mnorm.yf();
            float znPos = (Float) mnorm.zf();

            Vec3D mnormv = new Vec3D(xnPos, ynPos, znPos);
            Vec3D mvert = new Vec3D(0, 0, 1);

            float slope = mnormv.angleBetween(mvert);

            slope = degrees(slope);

            int slopeint = (int) slope;
            Slope.put(vertex1, slopeint);
            CaveHe.put(vertex, vertex1);
        }

        for (HE_Vertex a : mesh.getVerticesAsArray()) {
            int c1 = color(10, 10, 10);
            a.setColor(color(c1, 40));
        }

    }

    private void meshrun() {

//        for (Vec3D b : scanPtsV) {
//            HE_Vertex c = (HE_Vertex) CaveHe.get(b);
//            scanPts.add(c);
//        }

//        for (HE_Vertex a : scanPts) {
//            int slp = Slope.get(a);
//            float slp2 = map(slp, 0, 150, 0, 1);
//            int c1 = color(255, 0, 0);
//            int c2 = color(0, 255, 000);
//            int c = lerpColor(c1, c2, slp2);
//            a.setColor(color(c, 40));
//        }

        noStroke();
        fill(100);
        render.drawFacesVC(mesh);

//        pushMatrix();
//        fill(40, 120);
//        noStroke();
//        lights();
//        gfx.mesh(cave, false, 10);
//        popMatrix();

    }

}
