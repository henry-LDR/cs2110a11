package model;

import graph.MazeGraph.MazeVertex;
import java.awt.Color;

public class Inky extends Ghost {

    /**
     * Construct a Inky associated with the given `model` that is cyan with initialDelay 6000 ms.
     */
    public Inky(GameModel model) {
        super(model, Color.CYAN, 6000);
    }

    /**
     * Return the vertex that this ghost is targeting. In the CHASE state, it returns a target
     * vertex such that Pac-Mann's nearest vertex is the midpoint between Blinky's nearest vertex
     * and the target vertex. In the FLEE state, it returns the vertex with coordinates
     * (2, model.height()-3), ie. southwest corner.
     */
    @Override
    protected MazeVertex target() {
        if (state == GhostState.CHASE){
            MazeVertex pac = model.pacMann().location().nearestVertex();
            MazeVertex blinky = model.blinky().location().nearestVertex();
            int pacX = pac.loc().i();
            int pacY = pac.loc().j();
            int blinkyX = blinky.loc().i();
            int blinkyY = blinky.loc().j();

            int targetX = 2 * pacX - blinkyX;
            int targetY = 2 * pacY - blinkyY;

            return model.graph().closestTo(targetX, targetY);
        }
        else{ //FLEE
            return model.graph().closestTo(2, model.height() - 3);
        }


    }
}
