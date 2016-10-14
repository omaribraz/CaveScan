/**
 * Created by omar on 10/14/2016.
 */
import processing.core.PApplet;
import java.util.ArrayList;


public class Flock {
    private CaveScan p;
    ArrayList <Boid>boids;
    ArrayList<trail> trailPop;

    Flock(CaveScan _p){
        p = _p;
        boids = new ArrayList<Boid>();
        trailPop = new ArrayList<trail>();
    }
    void run() {
        for (Boid b : boids) {
            b.run();
        }
        for (int i = 0; i<trailPop.size(); i++) {
            trail t = (trail) trailPop.get(i);
            t.update();
        }
    }

    void addBoid( Boid b) {
        boids.add(b);
    }

    void removeBoid( Boid b) {
        boids.remove(b);
    }

    void addTrail( trail t) {
        trailPop.add(t);
    }

    void removeTrail( trail t) {
        trailPop.remove(t);
    }
}
