package model;

import graph.MazeGraph.Direction;
import graph.MazeGraph.MazeEdge;
import graph.MazeGraph.MazeVertex;
import graph.Pathfinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.GameModel.Item;
import model.Ghost.GhostState;


public class PacMannAI extends PacMann{

    /**
     * The distance threshold under which Pac-Mann will go from collecting items to escaping ghosts.
     */
    private static final double ESCAPE_THRESHOLD = 3.0;

    public PacMannAI(GameModel model) {
        super(model);
    }

        /**
         * Returns the next edge that this actor will traverse in the game graph. Will only be called
         * when this actor is standing on a vertex, which must equal the returned edge's tail.
         *
         * If the Euclidean distance from Pac-Mann to the nearest ghost is less than ESCAPE_THRESHOLD,
         * returns the edge out of all outgoing edges from Pac-Mann's current vertex whose head vertex
         * is the maximum distance from the nearest Ghost.
         *
         * Otherwise, if there are fleeing ghosts, it returns the edge to take the path of shortest
         * distance to the closest fleeing ghost.
         *
         * If there are no fleeing ghosts, it returns the edge to take the path of shortest distance
         * to the closest pellet.
         *
         * Otherwise, it returns the edge to the nearest dot.
         */
        @Override
    public MazeEdge nextEdge() {
        MazeVertex start = model.pacMann().nearestVertex();
        MazeEdge prevEdge = model.pacMann().location().edge();
        List<MazeVertex> pellets = getVerticesWithItem(Item.PELLET);
        List<MazeVertex> dots = getVerticesWithItem(Item.DOT);

        //Escape chasing ghosts
        double shortestGhost = shortestGhostDistance(start);
        if(shortestGhost < ESCAPE_THRESHOLD){
            return maximizeShortestChasingGhostDistance();
        }

        //Chase nearest fleeing ghost
        if(closestFleeingGhost() != null){
            return chaseClosestGhostEdge();
        }

        //Go towards nearest pellet if there are pellets remaining and multiple ghosts are chasing
        if(!pellets.isEmpty() && countChasingGhosts() >= 2){
            return edgeToClosestVertex(pellets);
        }
        else{
            return edgeToClosestVertex(dots);
        }
    }

    /*
     *      ------------------------------------------------
     *                  METHODS FOR GETTING ITEMS
     *      ------------------------------------------------
     */

    /**
     * Returns the edge Pac-Mann should take to reach the closest vertex in `targets`.
     * Returns null if `targets` is empty.
     */
    private MazeEdge edgeToClosestVertex(List<MazeVertex> targets) {
        if (targets.isEmpty()) return null;
        MazeVertex pacVertex = model.pacMann().location().nearestVertex();
        MazeVertex nearest = closestVertex(pacVertex, targets);
        return bestFirstStep(pacVertex, nearest, model.pacMann().location().edge());

    }
    /*
     *      ------------------------------------------------
     *                  METHODS FOR GHOST INTERACTION
     *      ------------------------------------------------
     */

    /**
     * Returns the number of ghosts that are actively chasing Pac-Mann.
     */
    private int countChasingGhosts() {
        int count = 0;

        for (Actor a : model.actors()) {
            if (a instanceof Ghost g && g.state() == GhostState.CHASE) {
                count++;
            }
        }

        return count;
    }


    /**
     * Returns the edge Pac-Mann should take to approach the nearest fleeing ghost, minimizing distance to ghost.
     * Can only be called when closestFleeingGhost() != null.
     */
    private MazeEdge chaseClosestGhostEdge() {
        Ghost target = closestFleeingGhost();
        MazeVertex pac = model.pacMann().nearestVertex();
        MazeVertex ghost = target.nearestVertex();
        return bestFirstStep(pac, ghost, model.pacMann().location().edge()); }


    /**
     * Returns the fleeing ghost closest to Pac-Mann.
     * If no ghosts are in the FLEE state, returns null.
     */
    private Ghost closestFleeingGhost() {
        Ghost closestGhost = null;
        double closestDist = Double.POSITIVE_INFINITY;

        for (Actor a : model.actors()) {
            if (a instanceof Ghost g && g.state() == GhostState.FLEE) {
                double dist = distance(model.pacMann().nearestVertex(), g.nearestVertex());

                if (dist < closestDist) {
                    closestDist = dist;
                    closestGhost = g;
                }
            }
        }

        return closestGhost; // null if none were fleeing
    }

    /**
     * Returns the MazeEdge whose head node maximizes the distance from the nearest chasing ghost.
     * Can only be called if there is a chasing ghost.
     */
    private MazeEdge maximizeShortestChasingGhostDistance(){
        MazeVertex current = model.pacMann().location().nearestVertex();
        double bestDistance = -1;
        MazeEdge bestEdge = null;

        // Try each possible direction
        for (MazeEdge e : current.outgoingEdges()) {
            // Compute how safe this move is
            double dist = shortestGhostDistance(e.head());
            if (dist > bestDistance) { //do not have to worry about positive infinity, b/c at least one ghost is chasing
                bestDistance = dist;
                bestEdge = e;
            }
        }

        return bestEdge;
    }


    /**
     * Returns the shortest distance to a ghost that is in the chase state from a MazeVertex v.
     * Returns Double.POSITIVE_INFINITY if no ghosts are in the chase state.
     */
    private double shortestGhostDistance(MazeVertex v){
        //Check for how close nearest chasing ghosts are
        double closestGhost = Double.POSITIVE_INFINITY;

        for(Actor a : model.actors()){
            if(a instanceof Ghost g && g.state() == GhostState.CHASE){
                double dist = distance(v, g.nearestVertex());
                closestGhost = (Math.min(dist, closestGhost));
            }
        }

        return closestGhost;
    }

    /*
     *      ------------------------------------------------
     *                  OTHER METHODS
     *      ------------------------------------------------
     */

    /**
     * Returns the Euclidean distance from a MazeVertex to ghost 'g'.
     */
    private double distance(MazeVertex v, MazeVertex w){

        MazeEdge prevEdge = model.pacMann().location().edge();
        if(prevEdge == null && !v.outgoingEdges().iterator().hasNext()){
            prevEdge = v.outgoingEdges().iterator().next(); // arbitrary first edge
        }

        List<MazeEdge> path = Pathfinding.shortestNonBacktrackingPath(v, w, prevEdge);
        return path == null ? Double.POSITIVE_INFINITY : path.size();

    }
    private List<MazeVertex> getVerticesWithItem(Item item) {
        List<MazeVertex> result = new ArrayList<>();
        for (MazeVertex v : model.graph().vertices()) {
            if (model.itemAt(v) == item) {
                result.add(v);
            }
        }
        return result;
    }
    /**
     * Gets the nearest vertex from a list
     */
    private MazeVertex closestVertex(MazeVertex start, List<MazeVertex> candidates) {
        MazeVertex best = null;
        double bestDist = Double.POSITIVE_INFINITY;

        for (MazeVertex v : candidates) {
            double d = distance(start, v);
            if (d < bestDist) {
                bestDist = d;
                best = v;
            }
        }
        return best;
    }

    /**
     * Returns the best first step to a target vertex from a starting vertex given a prevEdge.
     *
     */
    private MazeEdge bestFirstStep(MazeVertex start, MazeVertex target, MazeEdge prevEdge) {
        List<MazeEdge> path = Pathfinding.shortestNonBacktrackingPath(start, target, prevEdge);

        // If path is valid, use it
        if (path != null && !path.isEmpty()) {
            return path.get(0);
        }

        // Fallback: choose greedy best outgoing edge
        MazeEdge best = null;
        double bestDist = Double.POSITIVE_INFINITY;

        for (MazeEdge e : start.outgoingEdges()) {
            double d = distance(e.head(), target);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }

        return best;
    }


}
