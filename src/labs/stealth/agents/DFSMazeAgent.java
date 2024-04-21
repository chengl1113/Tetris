package src.labs.stealth.agents;

// SYSTEM IMPORTS
import edu.bu.labs.stealth.agents.MazeAgent;
import edu.bu.labs.stealth.graph.Vertex;
import edu.bu.labs.stealth.graph.Path;
import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
import edu.cwru.sepia.environment.model.state.State.StateView;

import java.util.LinkedList; // Used to get adjacent vertices
import java.util.HashSet; // will need for dfs
import java.util.Stack; // will need for dfs
import java.util.Set; // will need for dfs

// JAVA PROJECT IMPORTS

public class DFSMazeAgent
        extends MazeAgent {

    public DFSMazeAgent(int playerNum) {
        super(playerNum);
    }

    @Override
    public Path search(Vertex src,
            Vertex goal,
            StateView state) {

        // Adjacency List for neigher vertices
        LinkedList<Vertex> adj = new LinkedList<Vertex>();

        // Stack for DFS
        Stack<Path> stack = new Stack<Path>();

        // Set to track visited vertices
        Set<Vertex> visited = new HashSet<>();

        stack.add(new Path(src));

        while (stack.size() != 0) {
            Path path = stack.pop();
            Vertex v = path.getDestination();
            visited.add(v);
            // Get all adjacent vertices of v
            adj = getAdjacentVertices(v, state);
            // We next to TH
            if (adj.contains(goal)) {
                return path;
            }
            for (Vertex n : adj) {
                // If adjacent vertices not in visited
                if (!visited.contains(n)) {
                    // then add to visited and add to stack
                    visited.add(n);
                    stack.add(new Path(n, 1f, path));
                }
            }
        }
        return null;
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
