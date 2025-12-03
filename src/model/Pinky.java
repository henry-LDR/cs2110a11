package model;

import graph.MazeGraph;
import graph.MazeGraph.MazeVertex;
import java.awt.Color;

public class Pinky extends Ghost{


    /**
     * Construct a Pinky associated with the given `model` that is pink with initialDelay 4000 ms.
     */
    public Pinky(GameModel model) {
        super(model, Color.PINK, 4000);
    }

    /**
     * Return the vertex that Pinky is targeting. If it is in the CHASE state, it returns the
     * vertex closestTo() the coordinate that is 3 units away from Pac-Mann in the current direction
     * that Pac-Mann is facing. If it is in the FLEE state, return the vertex closestTo() the
     * coordinates (model.width() -3, 2) (northeast corner).
     */

    @Override
    protected MazeVertex target() {
        if (state == GhostState.CHASE) {
            MazeGraph.IPair pacLoc = model.pacMann().nearestVertex().loc();
            MazeGraph.Direction pacDir = model.pacMann().location().edge().direction();
            int targetI = pacLoc.i();
            int targetJ = pacLoc.j();

            switch(pacDir){
                case LEFT -> targetI -= 3;
                case RIGHT -> targetI += 3;
                case UP -> targetJ -= 3;
                case DOWN -> targetJ += 3;
            }
            return model.graph().closestTo(targetI, targetJ);

        }
        else{ //FLEE
            return model.graph().closestTo(model.width() - 3, 2);
        }
    }
}
