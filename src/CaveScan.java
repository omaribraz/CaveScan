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
import toxi.volume.*;
import toxi.math.waves.*;
import toxi.processing.ToxiclibsSupport;
import toxi.physics3d.*;
import toxi.physics3d.behaviors.*;

import wblut.processing.*;
import wblut.hemesh.*;
import wblut.geom.*;

import com.hamoid.*;

import pathfinder.*;

import controlP5.*;


public class CaveScan extends PApplet {


// Booleans

    boolean pathfind1 = false;
    boolean pathfind2 = false;

    boolean showscanmesh = false;

    boolean flockfly = false;
    boolean leavetrail = false;

    boolean readcirc = true;

    int slowFc = 0;

    PShape obj;
    PShape cone;


    public ToxiclibsSupport gfx;
    TriangleMesh cave;
    WETriangleMesh corridor;
    float DIM = 1500;
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
    public HashMap<Vec3D, Integer> ptscheck = new HashMap<>();
    public HashMap<Vec3D, Integer> ptsslope = new HashMap<>();
    public HashMap<Vec3D, Float> ptsvar = new HashMap<>();

    Octree meshoctree;
    Octree boidoctree;
    Octree ptsoctree;

    Flock flock;

    VerletPhysics3D physics;
    VideoExport videoExport;

    Graph gs = new Graph();
    GraphNode[] gNodes, rNodes;
    GraphEdge[] gEdges, exploredEdges;
    IGraphSearch pathFinder;
    GraphNode startNode, endNode;
    boolean[] showOption = new boolean[3];

    int start, end;
    float minValue;
    float maxValue;
    float slidermin;
    float slidermax;

    ArrayList<Vec3D> pts = new ArrayList<>();
    ArrayList<MeshLine> lines = new ArrayList<>();
    ArrayList<Vec3D> circpts = new ArrayList<>();

    public ArrayList<Float> variable = new ArrayList<>();
    public ArrayList<GraphEdge[]> pathtree = new ArrayList<>();
    public ArrayList<Integer> endpts = new ArrayList<>();
    public ArrayList<Pathagent> pathpts = new ArrayList<>();

    float ballvel = 0f;
    boolean ballmove = true;
    boolean buildmesh = false;
    boolean buildmesh1 = false;
    int RES = 64;
    Vec3D corridcntr = new Vec3D();
    float density=0.5f;

    PeasyCam cam;
    ControlP5 cp5;

    Vec3D SCALE=new Vec3D(1,1,1).scaleSelf(100);

    int DIMX=64;
    int DIMY=64;
    int DIMZ=64;

    VolumetricSpace volume;
    VolumetricBrush brush;
    IsoSurface surface;

    AbstractWave brushSize;



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

        volume=new VolumetricSpaceArray(SCALE.scaleSelf(20),DIMX,DIMY,DIMZ);
        brush=new RoundBrush(volume,SCALE.x/2);
        brushSize=new SineWave(0,0.1f,SCALE.x*0.07f,60f);
        surface=new ArrayIsoSurface(volume);
        corridor=new WETriangleMesh();

        if (pathfind1 || pathfind2) setpathfind();

        cam = new PeasyCam(this, meshcentre.x, meshcentre.y, 0, 2200);

        if (pathfind1) setgui();

        if (flockfly) {
            boidoctree = new Octree(this, new Vec3D(-1, -1, -1).scaleSelf(meshcentre), DIM * 2);

            for (int i = 0; i < 500; i++) {
                flock.addBoid(new Boid(this, new Vec3D(random(0, 1200), random(0, 1200), random(190, 350)), new Vec3D(random(-TWO_PI, TWO_PI), random(-TWO_PI, TWO_PI), random(-TWO_PI, TWO_PI))));
            }
        }

        if (readcirc) {
            readpath();
            corridor = new WETriangleMesh();
            for (Vec3D a : circpts) {
                Pathagent b = new Pathagent(this, a);
                pathpts.add(b);
            }
        }

    }

    public void draw() {
        background(0);

        ballvel = 0;

        if ((readcirc) && (ballmove)) {
            for (Pathagent a : pathpts) {
                a.run();
            }
            if (ballvel < 2) {
                ballmove = false;
            }
        }

        if (!ballmove) {
            if (!buildmesh) {
                brush.setSize(brushSize.update());
                for (Pathagent a : pathpts) {
                    brush.drawAtAbsolutePos(new Vec3D(a.x-meshcentre.x,a.y-meshcentre.y,a.z-meshcentre.z),density);
                }
                volume.closeSides();
                surface.reset();
                surface.computeSurfaceMesh(corridor,0.1f);
                for(int i=0; i<1; i++) {
                  new LaplacianSmooth().filter(corridor,1);
                }

                buildmesh = true;

            }

            if (!buildmesh1) {
//                corridcntr = corridor.computeCentroid();
//                MeshVoxelizer voxelizer = new MeshVoxelizer(RES);
//                voxelizer.setWallThickness(0);
//                VolumetricSpace vol = voxelizer.voxelizeMesh(corridor);
//                vol.closeSides();
//                IsoSurface surface = new HashIsoSurface(vol);
//                corridor = new WETriangleMesh();
//                surface.computeSurfaceMesh(corridor, 0.2f);
//                corridor.computeVertexNormals();
//
//                for(int i=0; i<4; i++) {
//                   new LaplacianSmooth().filter(corridor,1);
//                }
                buildmesh1 = true;
            }

            pushMatrix();
            stroke(255,0,0);
            translate(meshcentre.x,meshcentre.y,meshcentre.z);
            gfx.mesh(corridor);
            popMatrix();

        }

        if (pathfind1) {
            renderpath();
            for (MeshLine l : lines) {
                l.drawline();
            }
        }

        if (pathfind2) runpathfind();


        if ((flockfly) && (frameCount % 1 == 0)) {
            boidoctree.run();

            if (frameCount < 10) {
                for (int i = 0; i < flock.boids.size(); i++) {
                    Boid b = flock.boids.get(i);
                    b.checkMesh();
                }
            }

            flock.run();

//        boidoctree.draw();
        }

        if (frameCount % 1 == 0) {
            meshrun();
        }

//        videoExport.saveFrame();

        if (pathfind1) gui();

    }

    private void setgui() {
        cp5 = new ControlP5(this);
        cp5.addSlider("slidermin")
                .setPosition(100, 50)
                .setRange(0, 1)
                .setValue(0)
                .setNumberOfTickMarks(50);

        cp5.addSlider("slidermax")
                .setPosition(100, 100)
                .setRange(0, 2)
                .setValue(1)
                .setNumberOfTickMarks(50);

        cp5.setAutoDraw(false);
    }

    private void gui() {
        hint(DISABLE_DEPTH_TEST);
        cam.beginHUD();
        cp5.draw();
        cam.endHUD();
        if (cp5.isMouseOver() == true) {
            cam.setActive(false);
        } else {
            cam.setActive(true);
        }
        hint(ENABLE_DEPTH_TEST);
    }

    private void setupassets() {
        obj = loadShape("data/" + "drone.obj");
        obj.scale(3);

        cone = loadShape("data/" + "cone.obj");
        cone.scale(5);

        flock = new Flock(this);
    }

    private void readpath() {
        String linept[] = loadStrings("data/" + "path.txt");
        for (int i = 0; i < linept.length; i++) {
            String[] vec = split(linept[i], ",");
            Vec3D a = new Vec3D(Float.parseFloat(vec[0]), Float.parseFloat(vec[1]), Float.parseFloat(vec[2]));
            circpts.add(a);
        }
    }

    private void setpathfind() {

        readText();

        ptsoctree = new Octree(this, new Vec3D(-1, -1, -1).scaleSelf(meshcentre), DIM * 2);
        ptsoctree.addAll(pts);


        showOption[2] = true;
        gs = new Graph();

        Collections.reverse(pts);

        for (int i = 0; i < pts.size(); i++) {
            Vec3D f = pts.get(i);
            ptscheck.put(f, i);
            Vec3D ptmesh = cave.getClosestVertexToPoint(f);
            float meshrad = f.distanceTo(ptmesh);
            int slppt = ptsslope.get(ptmesh);
            float meshvariable = slppt / meshrad * meshrad;
            variable.add(meshvariable);
        }

        maxValue = Collections.max(variable);
        minValue = Collections.min(variable);

        System.out.println("Min = " + minValue);
        System.out.println("Max = " + maxValue);


        for (int i = 0; i < pts.size(); i++) {
            Vec3D f = pts.get(i);
            float var1 = variable.get(i);
            float var2 = map(var1, minValue, maxValue, 0.12f, 1.49f);
            ptsvar.put(f, var2);
        }

        for (int i = 0; i < pts.size(); i++) {
            Vec3D f = pts.get(i);
            gs.addNode(new GraphNode(i, f.x, f.y, f.z));
            for (int j = 0; j < pts.size(); j++) {
                Vec3D b = pts.get(j);
                if (b != f) {
                    if (b.distanceToSquared(f) < 80 * 80) {
                        float varline = ptsvar.get(f);
                        int c1 = color(255, 0, 0);
                        int c2 = color(0, 255, 0);
                        int c = lerpColor(c1, c2, varline);
                        gs.addEdge(i, j, varline);
                        MeshLine l1 = new MeshLine(this, f, b, c);
                        lines.add(l1);
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

    private void renderpath() {

        lines.clear();
        ptsvar.clear();

        for (int i = 0; i < pts.size(); i++) {
            Vec3D f = pts.get(i);
            float var1 = variable.get(i);
            float var2 = map(var1, minValue, maxValue, slidermin, slidermax);
            ptsvar.put(f, var2);
        }

        for (int i = 0; i < pts.size(); i++) {
            Vec3D f = pts.get(i);
            for (int j = 0; j < pts.size(); j++) {
                Vec3D b = pts.get(j);
                if (b != f) {
                    if (b.distanceToSquared(f) < 80 * 80) {
                        float varline = ptsvar.get(f);
                        int c1 = color(255, 0, 0);
                        int c2 = color(0, 255, 0);
                        int c = lerpColor(c1, c2, varline);
                        MeshLine l1 = new MeshLine(this, f, b, c);
                        lines.add(l1);
                    }
                }
            }
        }
    }

    private void runpathfind() {

        pathFinder = makePathFinder(3);
        usePathFinder(pathFinder);

//              drawEdges(exploredEdges, color(0, 0, 255), 1.8f);

//        if (exploredEdges != null) {
//            pushStyle();
//            noFill();
//            stroke(0,0,255);
//            strokeWeight(.2f);
//            for (GraphEdge ge : exploredEdges) {
//                    line(ge.from().xf(), ge.from().yf(), ge.from().zf(), ge.to().xf(), ge.to().yf(), ge.to().zf());
//                }
//            popStyle();
//        }

//              drawRoute(rNodes, color(200, 0, 0), 5.0f);

        String[] pathpts = new String[rNodes.length];

        if (frameCount == 1) {
            if (rNodes.length >= 2) {
                for (int i = 1; i < rNodes.length; i++) {
                    Vec3D pttrail = new Vec3D(rNodes[i].xf(), rNodes[i].yf(), rNodes[i].zf());
                    int ptid = ptscheck.get(pttrail);
                    endpts.add(ptid);
                }
                for (int i = 0; i < rNodes.length; i++) {
                    String a = (Float.toString(rNodes[i].xf()) + "," + Float.toString(rNodes[i].yf()) + "," + Float.toString(rNodes[i].zf()));
                    pathpts[i] = a;
                    System.out.println(a);
                }
                saveStrings("data/" + "path.txt", pathpts);
            }
        }
        for (int i = 0; i < endpts.size(); i++) {
            int pte = endpts.get(i);
            usePathFinderArray(pathFinder, 0, pte);
        }
        if (rNodes.length > slowFc) {
            if (rNodes.length >= 2) {
                pushStyle();
                stroke(255, 0, 0);
                strokeWeight(4);
                noFill();

                if (frameCount % 5 == 0) slowFc++;

                for (int i = 1; i < slowFc; i++) {
                    drawEdges(pathtree.get(i), color(0, 0, 255, 50), 1.8f);
                    line(rNodes[i - 1].xf(), rNodes[i - 1].yf(), rNodes[i - 1].zf(), rNodes[i].xf(), rNodes[i].yf(), rNodes[i].zf());
                }
                // Route start node
                strokeWeight(15.0f);
                stroke(0, 0, 160);
                fill(0, 0, 255);
                point(rNodes[0].xf(), rNodes[0].yf(), rNodes[0].zf());
                // Route end node
                stroke(160, 0, 0);
                fill(255, 0, 0);
                point(rNodes[rNodes.length - 1].xf(), rNodes[rNodes.length - 1].yf(), rNodes[rNodes.length - 1].zf());
                popStyle();
            }
        } else {
            drawRoute(rNodes, color(200, 0, 0), 5.0f);
        }
        if (showOption[0]) {
            drawNodes();
        }
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

    void usePathFinder(IGraphSearch pf) {
        pf.search(start, end, true);
        rNodes = pf.getRoute();
        exploredEdges = pf.getExaminedEdges();
    }

    void usePathFinderArray(IGraphSearch pf, int start1, int end1) {
        pf.search(start1, end1, true);
        GraphEdge[] e1 = pf.getExaminedEdges();
        pathtree.add(e1);
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

        cave = (TriangleMesh) new STLReader().loadBinary(sketchPath("data/" + "cave2.stl"), STLReader.TRIANGLEMESH);
        meshcentre = cave.computeCentroid();

//        int novert1 = cave.getNumVertices();


        cave.flipVertexOrder();

        cavepts = (new ArrayList<>(cave.getVertices()));

        mesh = new HEC_FromBinarySTLFile(sketchPath("data/" + "cave2.stl")).create();

        int novert = mesh.getNumberOfVertices();

        //        WB_KDTree vertexTree = mesh.getVertexTree();

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
            ptsslope.put(vertex, slopeint);
            CaveHe.put(vertex, vertex1);
            Normal.put(vertex, mnormv);
        }

        if (showscanmesh) {
            for (HE_Vertex a : mesh.getVerticesAsArray()) {
                a.setColor(color(40, 60));
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
        if ((flockfly) || (readcirc)) {
            meshoctree = new Octree(this, new Vec3D(-1, -1, -1).scaleSelf(meshcentre), DIM * 2);
            meshoctree.addAll(cavepts);
        }

        if (!showscanmesh) gfx = new ToxiclibsSupport(this);
        if (showscanmesh) render = new WB_Render(this);
    }

    private void meshrun() {

        if (showscanmesh) {
            if (flockfly) {
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
            }

            if (!flockfly) {
                for (HE_Vertex a : mesh.getVertices()) {
                    int b = CaveSl.get(a);
                    a.setColor(color(b, 60));
                }
            }

            noStroke();
            lights();
            render.drawFacesVC(mesh);
        }

        if (!showscanmesh) {
            pushMatrix();
            fill(40, 120);
            noStroke();
            lights();
            gfx.mesh(cave, false, 0);
            popMatrix();
        }

    }

    private void readText() {
        String[] attptList = loadStrings("data/" + "points.txt");
        for (int i = attptList.length - 1; i >= 0; i--) {
            String point[] = (split(attptList[i], ','));
            if (point.length == 3) {
                Vec3D Temp_PT = new Vec3D(Float.parseFloat(point[0]), Float.parseFloat(point[1]), Float.parseFloat(point[2]));
                pts.add(Temp_PT);
            }
        }
    }

}
