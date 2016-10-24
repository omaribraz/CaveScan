/**
 * Created by omar on 10/14/2016.
 */

import processing.core.PApplet;

import java.lang.reflect.Array;
import java.util.*;

import processing.core.PShape;

import java.util.ArrayList;

import peasy.*;
import toxi.geom.*;
import toxi.geom.mesh.*;
import toxi.processing.ToxiclibsSupport;
import toxi.physics3d.*;
import toxi.physics3d.behaviors.*;

import wblut.processing.*;
import wblut.hemesh.*;
import wblut.geom.*;

import com.hamoid.*;

import pathfinder.*;


public class CaveScan extends PApplet {

    PShape obj;
    PShape cone;


    private ToxiclibsSupport gfx;
    TriangleMesh cave;
    private ArrayList<Vec3D> cavepts;
    Vec3D meshcentre = new Vec3D();

    private WB_Render render;
    private HE_Mesh mesh;

    private ArrayList<HE_Vertex> scanPts;
    public ArrayList<Vec3D> scanPtsV = new ArrayList<>();


    private HashMap<WB_Coord, Integer> Slope = new HashMap<>();
    public HashMap<Vec3D, WB_Coord> CaveHe = new HashMap<>();
    public HashMap<WB_Coord, Integer> CaveSl = new HashMap<>();
    public HashMap<Vec3D, Vec3D> Normal = new HashMap<>();


    Octree meshoctree;
    Octree boidoctree;

    Flock flock;

    public boolean bounce = true;
    public boolean reverse = false;

    VerletPhysics3D physics;

    VideoExport videoExport;

    Graph gs = new Graph();

    GraphNode[] gNodes, rNodes;
    GraphEdge[] gEdges, exploredEdges;

    IGraphSearch pathFinder;

    GraphNode startNode, endNode;

    boolean[] showOption = new boolean[3];

    int start, end;


    ArrayList<Vec3D> pts = new ArrayList<>();
    ArrayList<Line3D> lines = new ArrayList<>();

    ArrayList<Vec3D> pointsList = new ArrayList<>();

    PeasyCam cam;


    public static void main(String[] args) {
        PApplet.main("CaveScan", args);
    }

    public void settings() {
        size(1400, 800, P3D);
        smooth();
    }

    public void setup() {

        setupassets();

        meshsetup();

        readText();


        cam = new PeasyCam(this, meshcentre.x, meshcentre.y, 0, 2200);


        float DIM = 1500;
        meshoctree = new Octree(this, new Vec3D(-1, -1, -1).scaleSelf(meshcentre), DIM * 2);
        meshoctree.addAll(cavepts);

        boidoctree = new Octree(this, new Vec3D(-1, -1, -1).scaleSelf(meshcentre), DIM * 2);

        for (int i = 0; i < 250; i++) {
            flock.addBoid(new Boid(this, new Vec3D(random(0, 1200), random(0, 1200), random(190, 350)), new Vec3D(random(-TWO_PI, TWO_PI), random(-TWO_PI, TWO_PI), random(-TWO_PI, TWO_PI))));
        }


 //      meshpoints();


        showOption[2] = true;
        gs = new Graph();

        Collections.reverse(pts);

        for (int i = 0; i < pts.size(); i++) {
            Vec3D f = pts.get(i);
            if(i<2){
                println(f);
            }
            gs.addNode(new GraphNode(i, f.x, f.y, f.z));
            for (int j = 0; j < pts.size(); j++) {
                Vec3D b = pts.get(j);
                if (b != f) {
                    if (b.distanceToSquared(f) < 75*75) {
                        gs.addEdge(i, j, 0);
//                        Line3D l1 = new Line3D(f, b);
//                        lines.add(l1);
                    }
                }
            }
        }


        gNodes = gs.getNodeArray();
        gEdges = gs.getAllEdgeArray();
        start = 0;
        end = 1;
        gs.compact();


    }


    public void draw() {
        background(0);


//        for (Vec3D a : pts) {
//            stroke(255);
//            strokeWeight(2);
//            point(a.x, a.y, a.z);
//    }

//        for (Line3D l : lines) {
//            strokeWeight(.1f);
//            stroke(220);
//            line(l.a.x, l.a.y, l.a.z, l.b.x, l.b.y, l.b.z);
//        }

        pathFinder = makePathFinder(3);
        usePathFinder(pathFinder);

        drawEdges(exploredEdges, color(0, 0, 255), 1.8f);

        drawRoute(rNodes, color(200, 0, 0), 5.0f);

        if (showOption[0]) {
            drawNodes();
        }




        for (Boid b : flock.boids) {
            boidoctree.addBoid(b);
        }

        boidoctree.run();

        if (frameCount < 10) {
            for (int i = 0; i < flock.boids.size(); i++) {
                Boid b = flock.boids.get(i);
                b.checkMesh();
            }
        }

 //       flock.run();




//        boidoctree.draw();


        meshrun();

//        pushMatrix();
//        fill(40, 120);
//        noStroke();
//        lights();
//        gfx.mesh(cave, false, 0);
//        popMatrix();

//        videoExport.saveFrame();
        //       }

    }


    private void setupassets() {
        obj = loadShape("data/" + "drone.obj");
        obj.scale(3);

        cone = loadShape("data/" + "cone.obj");
        cone.scale(5);

        flock = new Flock(this);
    }


    IGraphSearch makePathFinder(int pathFinder) {
        IGraphSearch pf = null;
        float f = 1.0f;
        switch (pathFinder) {
            case 0:
                pf = new GraphSearch_DFS(gs);
                break;
            case 1:
                pf = new GraphSearch_BFS(gs);
                break;
            case 2:
                pf = new GraphSearch_Dijkstra(gs);
                break;
            case 3:
                pf = new GraphSearch_Astar(gs, new AshCrowFlight(f));
                break;
            case 4:
                pf = new GraphSearch_Astar(gs, new AshManhattan(f));
                break;
        }
        return pf;
    }

    void usePathFinder(IGraphSearch pf){
        pf.search(start, end, true);
        rNodes = pf.getRoute();
        exploredEdges = pf.getExaminedEdges();
    }

    void drawRoute(GraphNode[] r, int lineCol, float sWeight) {
        if (r.length >= 2) {
            pushStyle();
            stroke(lineCol);
            strokeWeight(sWeight);
            noFill();
            for (int i = 1; i < r.length; i++)
                line(r[i - 1].xf(), r[i - 1].yf(), r[i - 1].zf(), r[i].xf(), r[i].yf(), r[i].zf());
            // Route start node
            strokeWeight(15.0f);
            stroke(0, 0, 160);
            fill(0, 0, 255);
            point(r[0].xf(), r[0].yf(), r[0].zf());
            // Route end node
            stroke(160, 0, 0);
            fill(255, 0, 0);
            point(r[r.length - 1].xf(), r[r.length - 1].yf(), r[r.length - 1].zf());
            popStyle();
        }
    }


    void drawNodes() {
        pushStyle();
        noStroke();
        fill(255, 0, 255, 72);
        for (GraphNode node : gNodes)
            point(node.xf(), node.yf(), node.zf());
        popStyle();
    }

    void drawEdges(GraphEdge[] edges, int lineCol, float sWeight) {
        if (edges != null) {
            pushStyle();
            noFill();
            stroke(lineCol);
            strokeWeight(sWeight);
            for (GraphEdge ge : edges) {
                line(ge.from().xf(), ge.from().yf(), ge.from().zf(), ge.to().xf(), ge.to().yf(), ge.to().zf());
            }
            popStyle();
        }
    }

    private void meshsetup() {
        mesh = new HEC_FromBinarySTLFile(sketchPath("data/" + "cave2.stl")).create();
        cave = (TriangleMesh) new STLReader().loadBinary(sketchPath("data/" + "cave2.stl"), STLReader.TRIANGLEMESH);

        meshcentre = cave.computeCentroid();


        gfx = new ToxiclibsSupport(this);
        render = new WB_Render(this);

//        WB_KDTree vertexTree = mesh.getVertexTree();
//        int novert1 = cave.getNumVertices();

        int novert = mesh.getNumberOfVertices();

        cave.flipVertexOrder();


        cavepts = (new ArrayList<>(cave.getVertices()));


        for (int i = 0; i < novert; i++) {

            WB_Coord mnorm = mesh.getVertexNormal(i);
            WB_Coord vertex1 = mesh.getVertex(i);
            Vec3D vertex = cave.getVertexForID(i);

            float xnPos = mnorm.xf();
            float ynPos = mnorm.yf();
            float znPos = mnorm.zf();

            Vec3D mnormv = new Vec3D(xnPos, ynPos, znPos);
            Vec3D mvert = new Vec3D(0, 0, 1);

            float slope = mnormv.angleBetween(mvert);

            slope = degrees(slope);

            int slopeint = (int) slope;
            Slope.put(vertex1, slopeint);
            CaveHe.put(vertex, vertex1);
            Normal.put(vertex, mnormv);
        }

        for (HE_Vertex a : mesh.getVerticesAsArray()) {
            a.setColor(color(40, 30));
        }
        for (HE_Vertex a : mesh.getVerticesAsArray()) {
            int slp = Slope.get(a);
            float slp2 = map(slp, 0, 150, 0, 1);
            int c1 = color(255, 0, 0);
            int c2 = color(0, 255, 0);
            int c = lerpColor(c1, c2, slp2);
            CaveSl.put(a, c);
        }
    }

    private void meshrun() {
        scanPts = new ArrayList<>();
        for (Vec3D b : scanPtsV) {
            HE_Vertex c = (HE_Vertex) CaveHe.get(b);
            scanPts.add(c);
        }
        if (scanPts.size() > 0) {
            for (HE_Vertex a : scanPts) {
                int b = CaveSl.get(a);
                a.setColor(color(b, 60));
            }
        }
        noStroke();
        lights();
        render.drawFacesVC(mesh);


//        pushMatrix();
//        fill(40, 120);
//        noStroke();
//        lights();
//        gfx.mesh(cave, false, 0);
//        popMatrix();

    }

    private void meshpoints() {

//        for (int i = 0; i < 20; i++) {
//            for (int j = 0; j < 20; j++) {
//                for (int k = 0; k < 20; k++) {
//                    pts.add(new Vec3D(i * 70, j * 70, k * 30));
//                }
//            }
//        }

//        for (int j = 0; j < 10; j++) {
//            for (int i = 0; i < pts.size(); i++) {
//                Vec3D v = pts.get(i);
//                Vec3D v1 = cave.getClosestVertexToPoint(v);
////                Vec3D v2 = v1.copy().subSelf(v.copy());
////                Vec3D v3 = Normal.get(v1);
////                float a1 = v3.angleBetween(v2, true);
////                float a2 = degrees(a1);
////                if (a2 > 90) {
////                    pts.remove(v);
////                }
//                float distpt = v1.distanceToSquared(v);
//
//                if (distpt < 55 * 55) {
//                    pts.remove(this);
//                }
//            }
//        }
    }


    private void readText() {
        String[] attptList = loadStrings("data/" + "points.txt");
        for (int i = attptList.length-1; i >= 0; i--) {
            String point[] = (split(attptList[i], ','));
            if (point.length==3) {
                Vec3D Temp_PT = new Vec3D(Float.parseFloat(point[0]), Float.parseFloat(point[1]), Float.parseFloat(point[2]));
                pts.add(Temp_PT);
            }
        }
    }

}
