package src.pas.tetris.agents;

// SYSTEM IMPORTS
import java.util.Iterator;
import java.util.List;
import java.util.Random;

// JAVA PROJECT IMPORTS
import edu.bu.tetris.agents.QAgent;
import edu.bu.tetris.agents.TrainerAgent.GameCounter;
import edu.bu.tetris.game.Block;
import edu.bu.tetris.game.Board;
import edu.bu.tetris.game.Game.GameView;
import edu.bu.tetris.game.minos.Mino;
import edu.bu.tetris.linalg.Matrix;
import edu.bu.tetris.nn.Model;
import edu.bu.tetris.nn.LossFunction;
import edu.bu.tetris.nn.Optimizer;
import edu.bu.tetris.nn.models.Sequential;
import edu.bu.tetris.nn.layers.Dense; // fully connected layer
import edu.bu.tetris.nn.layers.ReLU; // some activations (below too)
import edu.bu.tetris.nn.layers.Tanh;
import edu.bu.tetris.nn.layers.Sigmoid;
import edu.bu.tetris.training.data.Dataset;
import edu.bu.tetris.utils.Pair;

public class TetrisQAgent
        extends QAgent {

    public static final double EXPLORATION_PROB = 0.05;

    private Random random;

    public TetrisQAgent(String name) {
        super(name);
        this.random = new Random(12345); // optional to have a seed
    }

    public Random getRandom() {
        return this.random;
    }

    @Override
    public Model initQFunction() {
        // build a single-hidden-layer feedforward network
        // this example will create a 3-layer neural network (1 hidden layer)
        // in this example, the input to the neural network is the
        // image of the board unrolled into a giant vector
        final int numPixelsInImage = Board.NUM_ROWS * Board.NUM_COLS;
        final int hiddenDim = 2 * numPixelsInImage;
        final int outDim = 1;

        Sequential qFunction = new Sequential();
        qFunction.add(new Dense(7, 7));
        qFunction.add(new ReLU());
        qFunction.add(new Dense(7, 7));
        qFunction.add(new ReLU());
        qFunction.add(new Dense(7, outDim));

        return qFunction;
    }

    // java -cp "./lib/*;." edu.bu.tetris.Main -q src.pas.tetris.agents.TetrisQAgent

    /**
     * This function is for you to figure out what your features
     * are. This should end up being a single row-vector, and the
     * dimensions should be what your qfunction is expecting.
     * One thing we can do is get the grayscale image
     * where squares in the image are 0.0 if unoccupied, 0.5 if
     * there is a "background" square (i.e. that square is occupied
     * but it is not the current piece being placed), and 1.0 for
     * any squares that the current piece is being considered for.
     * 
     * We can then flatten this image to get a row-vector, but we
     * can do more than this! Try to be creative: how can you measure the
     * "state" of the game without relying on the pixels? If you were given
     * a tetris game midway through play, what properties would you look for?
     */
    @Override
    public Matrix getQFunctionInput(final GameView game,
            final Mino potentialAction) {
        Matrix input = Matrix.zeros(1, 7);
        try {

            // row 0 is the top of the board
            Matrix image = game.getGrayscaleImage(potentialAction);
            // System.out.println(image);

            /* Height of stack */

            int height = -1;
            boolean found = false;
            for (int i = 0; i < Board.NUM_ROWS; i++) {
                for (int j = 0; j < Board.NUM_COLS; j++) {
                    // Go from top to bottom and find the highest i occurrence of 0.5
                    if (image.get(i, j) == 0.5 || image.get(i, j) == 1.0) {
                        height = Board.NUM_ROWS - i;
                        found = true;
                        break;
                    }
                }
                if (found)
                    break;
            }
            // System.out.println(image);
            // System.out.println("height: " + height);

            /* Number of holes */
            int numHoles = 0;
            int openHoleCount = 0;
            int blocksAboveHoles = 0;

            for (int i = 0; i < Board.NUM_COLS; i++) {
                boolean blockFound = false;
                int numberOfBlocksFound = 0;

                for (int j = 0; j < Board.NUM_ROWS; j++) {
                    if (image.get(i, j) != 0.0) {
                        blockFound = true;
                        numberOfBlocksFound++;
                    } else if (blockFound) {
                        blocksAboveHoles += numberOfBlocksFound;

                        if (i < Board.NUM_COLS - 2) {
                            if (image.get(i + 1, j) == 0 && image.get(i + 2, j) == 0) {
                                if (j == Board.NUM_ROWS - 1 || image.get(i + 1, j + 1) != 0) {
                                    openHoleCount++;
                                    continue;
                                }
                            }
                        }

                        if (i >= 2) {
                            if (image.get(i - 1, j) == 0 && image.get(i - 2, j) == 0) {
                                if (j == Board.NUM_ROWS - 1 || image.get(i - 1, j + 1) != 0) {
                                    openHoleCount++;
                                    continue;
                                }
                            }
                        }

                        numHoles++;
                    }
                }
            }

            // for (int i = 0; i < Board.NUM_ROWS; i++) {
            // for (int j = 0; j < Board.NUM_COLS; j++) {
            // if (i != 0 && image.get(i, j) == 0.0
            // && (image.get(i - 1, j) == 0.5 || image.get(i - 1, j) == 1.0)) {
            // numHoles++;
            // }
            // }
            // }

            /* Bumpiness of top row */

            // int[] topHeight = new int[Board.NUM_COLS - 1];
            // for (int col = 0; col < Board.NUM_COLS - 1; col++) {
            // for (int row = 0; row < Board.NUM_ROWS; row++) {
            // if (image.get(row, col) == 0.5 || image.get(row, col) == 1.0) {
            // topHeight[col] = Board.NUM_ROWS - row;
            // break;
            // }
            // }
            // }

            // for (int i = 0; i < topHeight.length - 1; i++) {
            // bumpiness += Math.abs(topHeight[i] - topHeight[i + 1]);
            // }

            int bumpiness = 0;
            int previousLineHeight = 0;

            for (int i = 0; i < Board.NUM_COLS - 1; i++) { // Note: don't care about final row
                for (int j = 0; j < Board.NUM_ROWS; j++) {
                    if (image.get(i, j) != 0) {
                        int currentLineHeight = Board.NUM_ROWS - j;
                        if (i != 0) {
                            bumpiness += Math.abs(previousLineHeight - currentLineHeight);
                        }
                        previousLineHeight = currentLineHeight;
                        break;
                    }
                }
            }

            /* Number of blocks above each hole */

            // int blocksAboveHoles = 0;
            // for (int i = Board.NUM_ROWS - 1; i >= 0; i--) {
            // for (int j = Board.NUM_COLS - 1; j >= 0; j--) {
            // // found an empty space
            // if (i != 0 && image.get(i, j) == 0.0) {
            // // while the space above this is occuppied
            // int x = i - 1;
            // while (image.get(x, j) == 0.5) {
            // blocksAboveHoles++;
            // x--;
            // }

            // }
            // }
            // }

            /* blocks in right most lane */
            int blocksInRightLane = 0;
            for (int i = 0; i < Board.NUM_ROWS; i++) {
                if (image.get(i, Board.NUM_COLS - 1) == 0.5 || image.get(i, Board.NUM_COLS - 1) == 1.0) {
                    blocksInRightLane++;
                }
            }

            /* number of lines placing the block clears */
            int linesCleared = 0;
            for (int i = 0; i < Board.NUM_ROWS; i++) {
                boolean rowClear = true;
                for (int j = 0; j < Board.NUM_COLS; j++) {
                    if (image.get(i, j) == 0.0) {
                        rowClear = false;
                    }
                }

                if (rowClear)
                    linesCleared++;
            }

            /* get mino type */
            Mino.MinoType type = potentialAction.getType();

            // set matrix values
            input.set(0, 0, height);
            input.set(0, 1, numHoles);
            input.set(0, 2, openHoleCount);
            input.set(0, 3, blocksAboveHoles);
            input.set(0, 4, bumpiness);
            input.set(0, 5, blocksInRightLane);
            input.set(0, 6, linesCleared);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return input;
    }

    /**
     * This method is used to decide if we should follow our current policy
     * (i.e. our q-function), or if we should ignore it and take a random action
     * (i.e. explore).
     *
     * Remember, as the q-function learns, it will start to predict the same "good"
     * actions
     * over and over again. This can prevent us from discovering new, potentially
     * even
     * better states, which we want to do! So, sometimes we should ignore our policy
     * and explore to gain novel experiences.
     *
     * The current implementation chooses to ignore the current policy around 5% of
     * the time.
     * While this strategy is easy to implement, it often doesn't perform well and
     * is
     * really sensitive to the EXPLORATION_PROB. I would recommend devising your own
     * strategy here.
     */
    @Override
    public boolean shouldExplore(final GameView game,
            final GameCounter gameCounter) {
        return this.getRandom().nextDouble() <= EXPLORATION_PROB;
    }

    /**
     * This method is a counterpart to the "shouldExplore" method. Whenever we
     * decide
     * that we should ignore our policy, we now have to actually choose an action.
     *
     * You should come up with a way of choosing an action so that the model gets
     * to experience something new. The current implemention just chooses a random
     * option, which in practice doesn't work as well as a more guided strategy.
     * I would recommend devising your own strategy here.
     */
    @Override
    public Mino getExplorationMove(final GameView game) {
        int randIdx = this.getRandom().nextInt(game.getFinalMinoPositions().size());
        return game.getFinalMinoPositions().get(randIdx);
    }

    /**
     * This method is called by the TrainerAgent after we have played enough
     * training games.
     * In between the training section and the evaluation section of a phase, we
     * need to use
     * the exprience we've collected (from the training games) to improve the
     * q-function.
     *
     * You don't really need to change this method unless you want to. All that
     * happens
     * is that we will use the experiences currently stored in the replay buffer to
     * update
     * our model. Updates (i.e. gradient descent updates) will be applied per
     * minibatch
     * (i.e. a subset of the entire dataset) rather than in a vanilla gradient
     * descent manner
     * (i.e. all at once)...this often works better and is an active area of
     * research.
     *
     * Each pass through the data is called an epoch, and we will perform
     * "numUpdates" amount
     * of epochs in between the training and eval sections of each phase.
     */
    @Override
    public void trainQFunction(Dataset dataset,
            LossFunction lossFunction,
            Optimizer optimizer,
            long numUpdates) {
        for (int epochIdx = 0; epochIdx < numUpdates; ++epochIdx) {
            dataset.shuffle();
            Iterator<Pair<Matrix, Matrix>> batchIterator = dataset.iterator();

            while (batchIterator.hasNext()) {
                Pair<Matrix, Matrix> batch = batchIterator.next();

                try {
                    Matrix YHat = this.getQFunction().forward(batch.getFirst());

                    optimizer.reset();
                    this.getQFunction().backwards(batch.getFirst(),
                            lossFunction.backwards(YHat, batch.getSecond()));
                    optimizer.step();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }
    }

    /**
     * This method is where you will devise your own reward signal. Remember, the
     * larger
     * the number, the more "pleasurable" it is to the model, and the smaller the
     * number,
     * the more "painful" to the model.
     *
     * This is where you get to tell the model how "good" or "bad" the game is.
     * Since you earn points in this game, the reward should probably be influenced
     * by the
     * points, however this is not all. In fact, just using the points earned this
     * turn
     * is a **terrible** reward function, because earning points is hard!!
     *
     * I would recommend you to consider other ways of measuring "good"ness and
     * "bad"ness
     * of the game. For instance, the higher the stack of minos gets....generally
     * the worse
     * (unless you have a long hole waiting for an I-block). When you design a
     * reward
     * signal that is less sparse, you should see your model optimize this reward
     * over time.
     */
    @Override
    public double getReward(final GameView game) {
        List<Mino> m = game.getFinalMinoPositions();
        Matrix vectorScores = getQFunctionInput(game, m.get(0));

        double height = vectorScores.get(0, 0) * -100;
        double numHoles = vectorScores.get(0, 1) * -150;
        double openHoleCount = vectorScores.get(0, 2) * -75;
        double blocksAboveHoles = vectorScores.get(0, 3) * -50;
        double bumpiness = vectorScores.get(0, 4) * -25;
        double blocksInRightLane = vectorScores.get(0, 5) * -10;

        double linesCleared = vectorScores.get(0, 6);
        if (linesCleared >= 2.0)
            linesCleared = vectorScores.get(0, 6) * 100;

        double reward = height + numHoles + openHoleCount + blocksAboveHoles + bumpiness + blocksInRightLane
                + linesCleared;

        return reward;
    }

}
