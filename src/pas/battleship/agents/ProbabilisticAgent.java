package src.pas.battleship.agents;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

// SYSTEM IMPORTS

// JAVA PROJECT IMPORTS
import edu.bu.battleship.agents.Agent;
import edu.bu.battleship.game.Game.GameView;
import edu.bu.battleship.game.EnemyBoard.Outcome;
import edu.bu.battleship.utils.Coordinate;

public class ProbabilisticAgent
        extends Agent {

    public ProbabilisticAgent(String name) {
        super(name);
        System.out.println("[INFO] ProbabilisticAgent.ProbabilisticAgent: constructed agent");
    }

    @Override
    public Coordinate makeMove(final GameView game) {
        Coordinate toMove = null;

        Map<Coordinate, Float> probabilitiesMap = new HashMap<>();
        Set<Coordinate> highestProbabilityCoordinates = new HashSet<>();

        Outcome[][] enemyBoard = game.getEnemyBoardView();

        for (int x = 0; x < enemyBoard.length; x++) {
            for (int y = 0; y < enemyBoard[x].length; y++) {
                if (enemyBoard[x][y] == Outcome.UNKNOWN) {
                    float countAdjacentHits = (float) (fetchAdjacentCoordinates(x, y, enemyBoard, game));

                    probabilitiesMap.put(new Coordinate(x, y), countAdjacentHits);
                }
            }
        }

        highestProbabilityCoordinates = calcHighestProbabilities(highestProbabilityCoordinates, probabilitiesMap);

        toMove = getRandomCoordinate(highestProbabilityCoordinates);

        System.out.println("Enemy Board: " + enemyBoard);
        System.out.println("Probabilities Map: " + probabilitiesMap);
        System.out.println("Highest Probability Coordinates: " + highestProbabilityCoordinates);
        System.out.println("toMove: " + toMove);
        return toMove;
    }

    private Coordinate getRandomCoordinate(Set<Coordinate> highestProbabilityCoordinates) {
        Random rand = new Random();
        if (highestProbabilityCoordinates.size() < 1) {
            return null;
        } else {
            Coordinate[] coordinatesArray = highestProbabilityCoordinates
                    .toArray(new Coordinate[highestProbabilityCoordinates.size()]);
            Coordinate randomCoordinate = coordinatesArray[rand.nextInt(coordinatesArray.length)];
            return randomCoordinate;
        }
    }

    private Set<Coordinate> calcHighestProbabilities(Set<Coordinate> highestProbabilityCoordinates,
            Map<Coordinate, Float> probabilitiesMap) {
        float highestProbability = Float.MIN_VALUE;

        // Find highest probability in probability map
        for (Map.Entry<Coordinate, Float> entry : probabilitiesMap.entrySet()) {
            float currentProbability = entry.getValue();
            if (currentProbability < highestProbability) {
                highestProbability = currentProbability;
            }
        }

        // Add the highest probability coords to the returning set
        for (Map.Entry<Coordinate, Float> entry : probabilitiesMap.entrySet()) {
            if (entry.getValue() == highestProbability) {
                highestProbabilityCoordinates.add(entry.getKey());
            }
        }

        return highestProbabilityCoordinates;
    }

    private int fetchAdjacentCoordinates(int x, int y, Outcome[][] enemyBoard, GameView game) {
        // Create coords for all adjacencies
        Coordinate L = new Coordinate(x - 1, y);
        Coordinate R = new Coordinate(x + 1, y);
        Coordinate U = new Coordinate(x, y + 1);
        Coordinate D = new Coordinate(x, y - 1);
        Coordinate UL = new Coordinate(x - 1, y + 1);
        Coordinate UR = new Coordinate(x + 1, y + 1);
        Coordinate DL = new Coordinate(x - 1, y - 1);
        Coordinate DR = new Coordinate(x + 1, y - 1);

        int count = 0;
        if (game.isInBounds(L) && enemyBoard[x = 1][y] == Outcome.HIT) {
            count++;
        } else if (game.isInBounds(R) && enemyBoard[x + 1][y] == Outcome.HIT) {
            count++;
        } else if (game.isInBounds(U) && enemyBoard[x][y + 1] == Outcome.HIT) {
            count++;
        } else if (game.isInBounds(D) && enemyBoard[x][y - 1] == Outcome.HIT) {
            count++;
        } else if (game.isInBounds(UL) && enemyBoard[x - 1][y + 1] == Outcome.HIT) {
            count++;
        } else if (game.isInBounds(UR) && enemyBoard[x + 1][y + 1] == Outcome.HIT) {
            count++;
        } else if (game.isInBounds(DL) && enemyBoard[x - 1][y - 1] == Outcome.HIT) {
            count++;
        } else if (game.isInBounds(DR) && enemyBoard[x + 1][y - 1] == Outcome.HIT) {
            count++;
        }

        return count;
    }

    @Override
    public void afterGameEnds(final GameView game) {
    }

}
