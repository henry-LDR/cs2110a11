package model;

import graph.MazeGraph.MazeVertex;
import java.awt.Color;

public class Blinky extends Ghost {

    /**
     * Construct a Blinky associated with the given `model` that is red with initialDelay 2000 ms.
     */
    public Blinky(GameModel model, Color ghostColor, int initialDelay) {
        super(model, Color.RED, 2000);
    }

    /**
     * Return the vertex that Blinky is targeting. If it is in the CHASE state, it returns the vertex
     * that is PacMann's nearestVertex(). If it is in the FLEE state, it returns the vertex closest
     * to the coordinates (2,2).
     */
    @Override
    protected MazeVertex target() {
        if (state == GhostState.CHASE) {
            return model.pacMann().location().nearestVertex();
        }
        else{ //FLEE
            return model.graph().closestTo(2,2);
        }

    }
}
