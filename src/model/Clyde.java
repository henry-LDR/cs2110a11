package model;

import graph.MazeGraph.MazeVertex;
import java.awt.Color;
import java.util.Random;

public class Clyde extends Ghost{

    /**
     * Random number generator.
     */
    private final Random rng;

    /**
     * Construct a Clyde associated with the given `model` and 'randomness'
     * that is yellow with initialDelay 8000 ms.
     */
    public Clyde(GameModel model, Random random) {
        super(model, Color.YELLOW, 8000);
        rng = random;
    }

    /**
     * Return the vertex that this ghost is targeting. In the CHASE state, if the Euclidean
     * distance between Pac-Mann's nearest vertex and Clyde's nearest vertex is 10 or greater,
     * return the vertex that is Pac-Mann's nearest vertex. Otherwise,
     * return the vertex closest to randomly chosen coordinates. In the FLEE state, return the vertex
     * with coordinates (model.width() -3, model.height() -3).
     */

    @Override
    protected MazeVertex target() {
        if(state == GhostState.CHASE){
            MazeVertex pac = model.pacMann().location().nearestVertex();
            MazeVertex clyde = model.clyde().location().nearestVertex();
            int pacX = pac.loc().i();
            int pacY = pac.loc().j();
            int clydeX = clyde.loc().i();
            int clydeY = clyde.loc().j();
            int dx = clydeX - pacX;
            int dy = clydeY - pacY;

            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance >= 10){
                return pac;
            }
            else{
                int randomX = rng.nextInt(model.width());
                int randomY = rng.nextInt(model.height());
                return model.graph().closestTo(randomX, randomY);
            }

        }
        else{ //FLEE
            return model.graph().closestTo(model.width() - 3, model.height() - 3);
        }
    }
}
