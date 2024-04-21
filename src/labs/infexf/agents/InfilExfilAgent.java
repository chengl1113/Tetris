package src.labs.infexf.agents;

import java.util.HashSet;
import java.util.Set;

// SYSTEM IMPORTS
import edu.bu.labs.infexf.agents.SpecOpsAgent;
import edu.bu.labs.infexf.distance.DistanceMetric;
import edu.bu.labs.infexf.graph.Vertex;
import edu.bu.labs.infexf.graph.Path;
import edu.cwru.sepia.environment.model.state.Unit;
import edu.cwru.sepia.environment.model.state.State.StateView;

// JAVA PROJECT IMPORTS

public class InfilExfilAgent
        extends SpecOpsAgent {

    public InfilExfilAgent(int playerNum) {
        super(playerNum);
    }

    // if you want to get attack-radius of an enemy, you can do so through the enemy
    // unit's UnitView
    // Every unit is constructed from an xml schema for that unit's type.
    // We can lookup the "range" of the unit using the following line of code
    // (assuming we know the id):
    // int attackRadius = state.getUnit(enemyUnitID).getTemplateView().getRange();
    @Override
    public float getEdgeWeight(Vertex src,
            Vertex dst,
            StateView state) {

        // Get enemy attack radius and enemy coords
        int attackRadius = -1;
        Set<Integer> enemyUnitIds = this.getOtherEnemyUnitIDs();
        Set<String> enemyUnitCoords = new HashSet<>();
        for (Integer id : enemyUnitIds) {
            if (id == null || state.getUnit(id) == null) {
                break;
            }
            String xCoord = Integer.toString(state.getUnit(id).getXPosition());
            String yCoord = Integer.toString(state.getUnit(id).getYPosition());
            enemyUnitCoords.add(xCoord + " " + yCoord);
            int curAttackRadius = state.getUnit(id).getTemplateView().getRange();
            if (curAttackRadius > attackRadius) {
                attackRadius = curAttackRadius;
            }
        }

        if (inRangeOfEnemy(enemyUnitCoords, dst, 3) && dstCloserToEnemy(enemyUnitCoords, src, dst)) {
            return 10000f;
        }

        if (inRangeOfEnemy(enemyUnitCoords, dst, 3)) {
            return 1000f;
        }

        if (dstCloserToEnemy(enemyUnitCoords, src, dst)) {
            return 500f;
        }

        return 1f;
    }

    private boolean dstCloserToEnemy(Set<String> enemyCoords, Vertex src, Vertex dst) {
        int xCoordDst = dst.getXCoordinate();
        int yCoordDst = dst.getYCoordinate();
        int xCoordSrc = src.getXCoordinate();
        int yCoordSrc = src.getYCoordinate();

        int distSrc = 1000000000;
        int distDst = 1000000000;

        for (String coord : enemyCoords) {

            String[] coords = coord.split(" ");
            int xCoordEnemy = Integer.parseInt(coords[0]);
            int yCoordEnemy = Integer.parseInt(coords[1]);
            // System.out.println(xCoordEnemy + " " + yCoordEnemy);

            int curDistSrc = Math.max(Math.abs(xCoordSrc - xCoordEnemy), Math.abs(yCoordSrc - yCoordEnemy));
            distSrc = Math.min(distSrc, curDistSrc);

            int curDistDst = Math.max(Math.abs(xCoordDst - xCoordEnemy), Math.abs(yCoordDst - yCoordEnemy));
            distDst = Math.min(distDst, curDistDst);

            if (curDistDst < curDistSrc) {
                return true;
            }
        }
        return false;
    }

    // Helper method to check if dst Vertex is within 2 blocks of an enemy
    private boolean inRangeOfEnemy(Set<String> enemyCoords, Vertex dst, int closeness) {
        int xCoordDst = dst.getXCoordinate();
        int yCoordDst = dst.getYCoordinate();

        boolean inRange = false;

        // For each enemy coord, check if dst coord is
        // in range of enemy attack radius
        for (String coord : enemyCoords) {

            String[] coords = coord.split(" ");
            int xCoordEnemy = Integer.parseInt(coords[0]);
            int yCoordEnemy = Integer.parseInt(coords[1]);
            // System.out.println(xCoordEnemy + " " + yCoordEnemy);

            boolean inRangeX = xCoordDst <= xCoordEnemy + closeness && xCoordEnemy - closeness <= xCoordDst;
            boolean inRangeY = yCoordDst <= yCoordEnemy + closeness && yCoordEnemy - closeness <= yCoordDst;

            boolean curEnemyInRange = inRangeX && inRangeY;
            inRange = inRange || curEnemyInRange;
        }
        // System.out.println("Enemy in range: " + inRange);

        return inRange;
    }

    @Override
    public boolean shouldReplacePlan(StateView state) {
        // Unit.UnitView player = state.getUnit(this.getMyUnitID());
        // int xCoordPlayer = player.getXPosition();
        // int yCoordPlayer = player.getYPosition();
        // Vertex playerVertex = new Vertex(xCoordPlayer, yCoordPlayer);

        // // Get enemy attack radius and enemy coords
        // int attackRadius = -1;
        // Set<Integer> enemyUnitIds = this.getOtherEnemyUnitIDs();
        // System.out.println(enemyUnitIds.toString());
        // Set<String> enemyUnitCoords = new HashSet<>();
        // for (Integer id : enemyUnitIds) {
        // if (id == null || state.getUnit(id) == null) {
        // break;
        // }
        // String xCoord = Integer.toString(state.getUnit(id).getXPosition());
        // String yCoord = Integer.toString(state.getUnit(id).getYPosition());
        // enemyUnitCoords.add(xCoord + " " + yCoord);
        // int curAttackRadius = state.getUnit(id).getTemplateView().getRange();
        // if (curAttackRadius > attackRadius) {
        // attackRadius = curAttackRadius;
        // }
        // }

        // if (inRangeOfEnemy(enemyUnitCoords, playerVertex, 5)) {
        // return true;
        // }

        // return false;
        return true;
    }

}
