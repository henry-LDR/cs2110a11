package model;

import graph.MazeGraph.MazeVertex;
import java.awt.Color;

public class Clyde extends Ghost{

    /**
     * Construct a Clyde associated with the given `model` that is yellow with initialDelay 8000 ms.
     */
    public Clyde(GameModel model) {
        super(model, Color.YELLOW, 8000);
    }

    /**
     * Return the vertex that this ghost is targeting
     */
    @Override
    protected MazeVertex target() {


    }
}
