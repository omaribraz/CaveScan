/**
 * Created by omar on 10/14/2016.
 */

import processing.core.PApplet;
import toxi.geom.Vec3D;

import java.util.List;
import java.util.ArrayList;

import toxi.geom.Ray3D;


import static processing.core.PApplet.degrees;

class Boid extends Vec3D {
    private CaveScan p;
    private Vec3D vel;
    private Vec3D acc;
    private float maxforce;
    private float maxspeed;


    Boid(CaveScan _p, Vec3D pos, Vec3D _vel) {
        super(pos);
        p = _p;
        vel = _vel;
        acc = new Vec3D(0, 0, 0);
        float r = 7.0f;
        maxspeed = 2;
        maxforce = 0.07f;
    }

    void run() {
        flock();
        if ((p.frameCount % 1 == 0) && (p.frameCount > 11)) {
            trail();
        }
        update();
        if (p.frameCount > 10) borders();
        if (p.frameCount > 10) render();
    }

    private void applyForce(Vec3D force) {
        acc.addSelf(force);
    }

    void checkMesh() {

        Ray3D r = new Ray3D(this, new Vec3D(0, 0, 1));

        if (!p.cave.intersectsRay(r)) {
            p.flock.removeBoid(this);
        }

        Vec3D cavept = p.cave.getClosestVertexToPoint(this);
        float distpt = cavept.distanceToSquared(this);

        if (distpt < 55 * 55) {
            p.flock.removeBoid(this);
        }
    }


    private void flock() {

        List boidpos = p.boidoctree.getPointsWithinSphere(this.copy(), 120);

        if (boidpos != null) {

            Vec3D sep = separate(boidpos);
            Vec3D ali = align(boidpos);
            Vec3D coh = cohesion(boidpos);
            //Vec3D stig = seektrail(flock.trailPop);

            sep.scaleSelf(4.0f);
            ali.scaleSelf(0.6f);
            coh.scaleSelf(0.1f);
            //stig.scaleSelf(0.5);

            applyForce(sep);
            applyForce(ali);
            applyForce(coh);
            //applyForce(stig);
        }
    }


    private void update() {

        vel.addSelf(acc);
        vel.limit(maxspeed);
        this.addSelf(vel);
        acc.scaleSelf(0);
    }

    private Vec3D seek(Vec3D target) {
        Vec3D desired = target.subSelf(this);
        desired.normalize();
        desired.scaleSelf(maxspeed);
        Vec3D steer = desired.subSelf(vel);
        steer.limit(maxforce);
        return steer;
    }

    void trail() {
        trail tr = new trail(p, this.copy(), vel.copy());
        p.flock.addTrail(tr);
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
        p.cone.setFill(p.color(255, 255, 0, 20));
        p.shape(p.cone);
        p.popMatrix();
    }

    // Separation
    private Vec3D separate(List<Boid> boids) {
        float desiredseparation = 45.0f * 45.0f;
        Vec3D steer = new Vec3D(0, 0, 0);
        int count = 0;
        for (Boid other : boids) {
            float d = this.distanceToSquared(other);
            if ((d > 0) && (d < desiredseparation)) {
                Vec3D diff = this.sub(other);
                diff.normalize();
                diff.scaleSelf(1 / d);
                steer.add(diff);
                count++;
            }
        }
        if (count > 0) {
            steer.scaleSelf(1 / (float) count);
        }
        if (steer.magnitude() > 0) {
            steer.normalize();
            steer.scaleSelf(maxspeed);
            steer.subSelf(vel);
            steer.limit(maxforce);
        }
        return steer;
    }

    // Alignment
    private Vec3D align(List<Boid> boids) {
        float neighbordist = 70.0f * 70.0f;
        Vec3D sum = new Vec3D(0, 0, 0);
        int count = 0;
        for (Boid other : boids) {
            float d = this.distanceToSquared(other);
            if ((d > 0) && (d < neighbordist)) {
                sum.addSelf(other.vel);
                count++;
            }
        }
        if (count > 0) {
            sum.scaleSelf(1 / (float) count);
            sum.normalize();
            sum.scaleSelf(maxspeed);
            Vec3D steer = sum.subSelf(vel);
            steer.limit(maxforce);
            return steer;
        } else {
            return new Vec3D(0, 0, 0);
        }
    }


    // Cohesion
    private Vec3D cohesion(List<Boid> boids) {
        float neighbordist = 80.0f * 80.0f;
        Vec3D sum = new Vec3D(0, 0, 0);
        int count = 0;
        for (Boid other : boids) {
            float d = this.distanceToSquared(other);
            if ((d > 0) && (d < neighbordist)) {
                sum.addSelf(other);
                count++;
            }
        }
        if (count > 0) {
            sum.scaleSelf(1 / (float) count);
            return seek(sum);
        } else {
            return new Vec3D(0, 0, 0);
        }
    }

//    Vec3D seektrail(ArrayList tPop) {
//        float neighbordist = 90;
//        Vec3D sum = new Vec3D(0, 0, 0);
//        int count = 0;
//
//        for (int i = 0; i < tPop.size(); i++) {
//            trail t = (trail) tPop.get(i);
//            float distance = this.distanceTo(t);
//            if ((distance < neighbordist)&&(inView(t, 60))) {
//                sum.addSelf(t);
//                count++;
//            }
//        }
//        if (count > 0) {
//            sum.scaleSelf(1/(float)count);
//            return seek(sum);
//        }
//        return sum;
//    }

    boolean inView(Vec3D target, float angle) {
        boolean resultBool;
        Vec3D vec = target.copy().subSelf(this.copy());
        float result = vel.copy().angleBetween(vec);
        result = degrees(result);
        resultBool = result < angle;
        return resultBool;
    }

    // Wraparound
    private void borders() {
        List<Vec3D> cavepoints = null;

        cavepoints = p.meshoctree.getPointsWithinSphere(this.copy(), 50);


        if (cavepoints != null) {
            if (cavepoints.size() > 0) {
                vel.scaleSelf(-3);
            }

            List<Vec3D> scned;
            scned = p.meshoctree.getPointsWithinSphere(this.copy(), 100);
            if (scned != null) {
                for (Vec3D a : scned) {
                    p.scanPtsV.add(a);
                }
            }

        }


    }
    
}
