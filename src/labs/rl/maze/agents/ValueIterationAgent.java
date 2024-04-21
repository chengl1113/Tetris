package src.labs.rl.maze.agents;

// SYSTEM IMPORTS
import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.util.Direction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// import edu.bu.battleship.game.Game;
// JAVA PROJECT IMPORTS
import edu.bu.labs.rl.maze.agents.StochasticAgent;
import edu.bu.labs.rl.maze.agents.StochasticAgent.RewardFunction;
import edu.bu.labs.rl.maze.agents.StochasticAgent.TransitionModel;
import edu.bu.labs.rl.maze.utilities.Coordinate;
import edu.bu.labs.rl.maze.utilities.Pair;

public class ValueIterationAgent
        extends StochasticAgent {

    public static final double GAMMA = 1; // feel free to change this around!
    public static final double EPSILON = 1e-6; // don't change this though

    private Map<Coordinate, Double> utilities;

    public ValueIterationAgent(int playerNum) {
        super(playerNum);
        this.utilities = null;
    }

    public Map<Coordinate, Double> getUtilities() {
        return this.utilities;
    }

    private void setUtilities(Map<Coordinate, Double> u) {
        this.utilities = u;
    }

    public boolean isTerminalState(Coordinate c) {
        return c.equals(StochasticAgent.POSITIVE_TERMINAL_STATE)
                || c.equals(StochasticAgent.NEGATIVE_TERMINAL_STATE);
    }

    /**
     * A method to get an initial utility map where every coordinate is mapped to
     * the utility 0.0
     */
    public Map<Coordinate, Double> getZeroMap(StateView state) {
        Map<Coordinate, Double> m = new HashMap<Coordinate, Double>();
        for (int x = 0; x < state.getXExtent(); ++x) {
            for (int y = 0; y < state.getYExtent(); ++y) {
                if (!state.isResourceAt(x, y)) {
                    // we can go here
                    m.put(new Coordinate(x, y), 0.0);
                }
            }
        }
        return m;
    }

    public void valueIteration(StateView state) {
        // TODO: complete me!
        Map<Coordinate, Double> u = getZeroMap(state);
        Map<Coordinate, Double> uPrime = getZeroMap(state);
        uPrime.put(StochasticAgent.POSITIVE_TERMINAL_STATE, new Double(1));
        uPrime.put(StochasticAgent.NEGATIVE_TERMINAL_STATE, new Double(-1));
        double maxChange = 0;

        do {
            maxChange = 0;
            u = deepCopy(uPrime);
            for (Coordinate coord : u.keySet()) {
                if (isTerminalState(coord))
                    continue;

                // get action utility for each direction
                double north = getActionUtility(state,
                        TransitionModel.getTransitionProbs(state, coord, Direction.NORTH), u);
                double south = getActionUtility(state,
                        TransitionModel.getTransitionProbs(state, coord, Direction.SOUTH), u);
                double east = getActionUtility(state, TransitionModel.getTransitionProbs(state, coord, Direction.EAST),
                        u);
                double west = getActionUtility(state, TransitionModel.getTransitionProbs(state, coord, Direction.WEST),
                        u);

                Set<Double> utils = new HashSet<>();
                utils.add(north);
                utils.add(south);
                utils.add(east);
                utils.add(west);

                Double max = utils.stream().max(Comparator.naturalOrder()).orElse(null);
                Double bellmanEquation = RewardFunction.getReward(coord)
                        + GAMMA * max;
                uPrime.put(coord, bellmanEquation);

                // calculate error between steps:
                if (Math.abs(uPrime.get(coord) - u.get(coord)) > maxChange) {
                    maxChange = Math.abs(uPrime.get(coord) - u.get(coord));
                }
            }

        } while (maxChange > (EPSILON * (1 - GAMMA) / GAMMA));

        setUtilities(u);
    }

    private Map<Coordinate, Double> deepCopy(Map<Coordinate, Double> uPrime) {
        Map<Coordinate, Double> copy = new HashMap<>();
        for (Coordinate c : uPrime.keySet()) {
            copy.put(c, uPrime.get(c));
        }

        return copy;
    }

    private double getActionUtility(StateView state, Set<Pair<Coordinate, Double>> outcomes,
            Map<Coordinate, Double> u) {
        double result = 0.0;

        for (Pair<Coordinate, Double> p : outcomes) {
            Double utitlity = u.get(p.getFirst());
            Double prob = p.getSecond();
            result += utitlity * prob;
        }

        return result;
    }

    @Override
    public void computePolicy(StateView state,
            HistoryView history) {
        // compute the utilities
        this.valueIteration(state);

        // compute the policy from the utilities
        Map<Coordinate, Direction> policy = new HashMap<Coordinate, Direction>();

        for (Coordinate c : this.getUtilities().keySet()) {
            // figure out what to do when in this state
            double maxActionUtility = Double.NEGATIVE_INFINITY;
            Direction bestDirection = null;

            // go over every action
            for (Direction d : TransitionModel.CARDINAL_DIRECTIONS) {

                // measure how good this action is as a weighted combination of future state's
                // utilities
                double thisActionUtility = 0.0;
                for (Pair<Coordinate, Double> transition : TransitionModel.getTransitionProbs(state, c, d)) {
                    thisActionUtility += transition.getSecond() * this.getUtilities().get(transition.getFirst());
                }

                // keep the best one!
                if (thisActionUtility > maxActionUtility) {
                    maxActionUtility = thisActionUtility;
                    bestDirection = d;
                }
            }

            // policy recommends the best action for every state
            policy.put(c, bestDirection);
        }

        this.setPolicy(policy);
    }

}
