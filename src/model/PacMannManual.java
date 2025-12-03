package model;

import graph.MazeGraph;
import graph.MazeGraph.MazeEdge;
import graph.MazeGraph.MazeVertex;

public class PacMannManual extends PacMann{

    public PacMannManual(GameModel model){
        super(model);
    }

    /**
     * Returns the next edge that PacMann will traverse in the game graph. Will only be called
     * when PacMann is standing on a vertex, which must equal the returned edge's tail. The edge
     * is chosen based on the direction of the most recent player command.
     *
     * If there is an edge from PacMann's nearest vertex in the direction of the most recent player
     * command, return that edge.
     * If there is no such edge, check if there is an edge from the nearest vertex to the direction
     * that PacMann is currently traveling and return that edge if it exists.
     * If neither exists, then return null.
     */
    @Override
    public MazeEdge nextEdge() {
        //Can only be called when PacMann is standing on a vertex.
        assert location().atVertex();

        MazeGraph.Direction commandDirection = model.playerCommand();
        MazeVertex currentVertex = location().nearestVertex();
        MazeEdge retEdge;
        MazeEdge currentEdge = location().edge();

        if (commandDirection != null && currentVertex.edgeInDirection(commandDirection) != null){
            //If there is an edge in the command direction
            retEdge = currentVertex.edgeInDirection(commandDirection);
        }
        else if(currentEdge != null && currentVertex.edgeInDirection(currentEdge.direction()) != null){
            //If there is an edge in the current direction
            retEdge = currentVertex.edgeInDirection(location().edge().direction());
        }
        else{
            //Neither exists, return null
            return null;
        }

        assert retEdge.tail().equals(currentVertex);
        return retEdge;
    }
}
