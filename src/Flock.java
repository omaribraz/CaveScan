/**
 * Created by omar on 10/14/2016.
 */
import processing.core.PApplet;

import java.util.ArrayList;


public class Flock {
    private CaveScan p;
    ArrayList <Boid>boids;

    Flock(CaveScan _p){
        p = _p;
        boids = new ArrayList<>();
    }

    void run() {
        for (Boid b : boids) {
            b.run();
          if(p.leavetrail)  b.trailupdate();
        }

    }

    void addBoid( Boid b) {
        boids.add(b);
    }

    void removeBoid( Boid b) {
        boids.remove(b);
    }

}
