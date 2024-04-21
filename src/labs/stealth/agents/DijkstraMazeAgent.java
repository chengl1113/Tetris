package src.labs.stealth.agents;

// SYSTEM IMPORTS
import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Vertex;
import edu.bu.labs.stealth.graph.Path;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction; // Directions in Sepia

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue; // heap in java
import java.util.Set;

// JAVA PROJECT IMPORTS

public class DijkstraMazeAgent
        extends MazeAgent {

    public DijkstraMazeAgent(int playerNum) {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src,
            Vertex goal,
            StateView state) {

        // Map to keep track of distances for each Path
        Map<Path, Float> distances = new HashMap<>();

        // Set to track visited vertices
        Set<Vertex> visited = new HashSet<>();

        PriorityQueue<Path> priorityQueue = new PriorityQueue<>(
                (path1, path2) -> Float.compare(path1.getTrueCost(), path2.getTrueCost()));

        Path initPath = new Path(src);
        distances.put(initPath, 0f);

        // Add first path to priority queue
        priorityQueue.add(initPath);
        visited.add(src);

        while (priorityQueue.size() != 0) {
            Path path = priorityQueue.poll();
            Vertex v = path.getDestination();

            LinkedList<Vertex> adjList = getAdjacentVertices(v, state);
            if (adjList.contains(goal)) {
                break;
            }

            for (Vertex n : adjList) {

                float cost = getEdgeCost(v, n);
                Path newPath = new Path(n, cost, path);
                // vertex hasn't been found yet
                if (!visited.contains(n)) {
                    distances.put(newPath, newPath.getTrueCost());
                    priorityQueue.add(newPath);
                    visited.add(n);
                }
                // vertex has been found
                else {
                    // new path is better than existing path
                    if (newPath.getTrueCost() < distances.get(path)) {
                        distances.replace(path, newPath.getTrueCost());
                        priorityQueue.add(newPath);
                    }
                    // if not better then just ignore?
                }
            }
        }

        Path bestPath = null;
        float bestLength = Float.MAX_VALUE;
        for (Map.Entry<Path, Float> entry : distances.entrySet()) {
            if (entry.getValue() < bestLength
                    && getAdjacentVertices(entry.getKey().getDestination(), state).contains(goal)) {
                bestLength = entry.getValue();
                bestPath = entry.getKey();
            }
        }
        return bestPath;
    }

    private float getEdgeCost(Vertex src, Vertex dst) {
        Direction direction = getDirectionToMoveTo(src, dst);

        // East and West
        if (direction.compareTo(Direction.EAST) == 0 || direction.compareTo(Direction.WEST) == 0) {
            return 5f;
        }

        // South
        if (direction.compareTo(Direction.SOUTH) == 0) {
            return 1f;
        }

        // North
        if (direction.compareTo(Direction.NORTH) == 0) {
            return 10f;
        }

        // Northeast and Northwest
        if (direction.compareTo(Direction.NORTHEAST) == 0 || direction.compareTo(Direction.NORTHWEST) == 0) {
            return (float) Math.sqrt((Math.pow(10f, 2)) + Math.pow(5f, 2));
        }

        // Southeast and Southwest
        if (direction.compareTo(Direction.SOUTHEAST) == 0 || direction.compareTo(Direction.SOUTHWEST) == 0) {
            return (float) Math.sqrt((Math.pow(1f, 2)) + Math.pow(5f, 2));
        }

        return -1f;
    }

    private LinkedList<Vertex> getAdjacentVertices(Vertex v, StateView state) {
        LinkedList<Vertex> adjVertices = new LinkedList<>();
        int xCoord = v.getXCoordinate();
        int yCoord = v.getYCoordinate();

        Set<String> resources = new HashSet<>();
        for (ResourceView unit : state.getAllResourceNodes()) {
            resources.add(Integer.toString(unit.getXPosition()) + Integer.toString(unit.getYPosition()));
        }

        int xTemp = xCoord;
        int yTemp = yCoord;
        // check right of v
        xTemp = xCoord + 1;
        yTemp = yCoord;
        String coord = Integer.toString(xTemp) + Integer.toString(yTemp);
        // Coordinate is not a tree so add to adjvertices
        if (!resources.contains(coord) && state.inBounds(xTemp, yTemp)) {
            adjVertices.add(new Vertex(xTemp, yTemp));
        }

        // check bottom right of v
        xTemp = xCoord + 1;
        yTemp = yCoord + 1;
        coord = Integer.toString(xTemp) + Integer.toString(yTemp);
        // Coordinate is not a tree so add to adjvertices
        if (!resources.contains(coord) && state.inBounds(xTemp, yTemp)) {
            adjVertices.add(new Vertex(xTemp, yTemp));
        }

        // check bottom of v
        xTemp = xCoord;
        yTemp = yCoord + 1;
        coord = Integer.toString(xTemp) + Integer.toString(yTemp);
        // Coordinate is not a tree so add to adjvertices
        if (!resources.contains(coord) && state.inBounds(xTemp, yTemp)) {
            adjVertices.add(new Vertex(xTemp, yTemp));
        }

        // check bottom left of v
        xTemp = xCoord - 1;
        yTemp = yCoord + 1;
        coord = Integer.toString(xTemp) + Integer.toString(yTemp);
        // Coordinate is not a tree so add to adjvertices
        if (!resources.contains(coord) && state.inBounds(xTemp, yTemp)) {
            adjVertices.add(new Vertex(xTemp, yTemp));
        }

        // check left of v
        xTemp = xCoord - 1;
        yTemp = yCoord;
        coord = Integer.toString(xTemp) + Integer.toString(yTemp);
        // Coordinate is not a tree so add to adjvertices
        if (!resources.contains(coord) && state.inBounds(xTemp, yTemp)) {
            adjVertices.add(new Vertex(xTemp, yTemp));
        }

        // check top left of v
        xTemp = xCoord - 1;
        yTemp = yCoord - 1;
        coord = Integer.toString(xTemp) + Integer.toString(yTemp);
        // Coordinate is not a tree so add to adjvertices
        if (!resources.contains(coord) && state.inBounds(xTemp, yTemp)) {
            adjVertices.add(new Vertex(xTemp, yTemp));
        }

        // check top of v
        xTemp = xCoord;
        yTemp = yCoord - 1;
        coord = Integer.toString(xTemp) + Integer.toString(yTemp);
        // Coordinate is not a tree so add to adjvertices
        if (!resources.contains(coord) && state.inBounds(xTemp, yTemp)) {
            adjVertices.add(new Vertex(xTemp, yTemp));
        }

        // check top right of v
        xTemp = xCoord + 1;
        yTemp = yCoord - 1;
        coord = Integer.toString(xTemp) + Integer.toString(yTemp);
        // Coordinate is not a tree so add to adjvertices
        if (!resources.contains(coord) && state.inBounds(xTemp, yTemp)) {
            adjVertices.add(new Vertex(xTemp, yTemp));
        }

        // System.out.println(adjVertices.toString());
        return adjVertices;
    }

    @Override
    public boolean shouldReplacePlan(StateView state) {
        return false;
    }

}
