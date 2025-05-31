package com.example._2048_expectimax;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;


import java.net.URL;
import java.util.*;

import static javafx.scene.paint.Color.WHITE;

public class AI_2048_Controller implements Initializable {

    @FXML
    private Label currentScore;
    @FXML
    private Label bestScore;
    @FXML
    private Label terminalLabel;
    @FXML
    private Button moveAI;
    @FXML
    private Button restart;
    @FXML
    private CheckBox movingAI;
    @FXML
    private GridPane gridPane;

    private AI_ScoreManager scoreManager;

    private final Random random = new Random();
    private Square[][] actualBoard;
    private List<Pair<Integer, Integer>> emptyPositions;
    private boolean moveOccurred;
    private boolean stop;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.scoreManager = new AI_ScoreManager(this, currentScore, bestScore);
        gridPane.requestFocus();
        this.actualBoard = new Square[4][4];
        this.emptyPositions = new ArrayList<>();
        this.moveOccurred = true;
        this.stop = false;

        createRandomSquare();
        createRandomSquare();
    }

    @FXML
    private void keyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        if(!stop) {
            if (code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                moveMergeSquares(actualBoard, code.toString(), true);
            }
        }
        if(code == KeyCode.R) {
            scoreManager.resetFile();
        }
    }

    @FXML
    private void buttonPressed(ActionEvent event) {
        Object node = event.getSource();
        if(!stop) {
            if (moveAI.equals(node)) {
                moveAI();

            } else if (movingAI.equals(node)) {
                Runnable backgroundTask = () -> {

                    stop = true;
                    while(movingAI.isSelected()) {
                        try {
                            moveAI();
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // Restore interrupted status
                            System.out.println("Background thread interrupted");
                        } catch (Exception e) {
                            System.out.println("Error: " + e);
                        }
                    }
                    stop = false;
                };

                Thread backgroundThread = new Thread(backgroundTask);
                backgroundThread.setDaemon(true);
                backgroundThread.start();
            }
        }

        if (restart.equals(node)) {
            reset();
        }
    }

    //Square -----------------------------------------------------------------------------------------------------
    class Square extends StackPane {

        private int value;
        private int column;
        private int row;

        private final Rectangle rectangle;
        private final Text text;
        private Square(int value, int column, int row) {
            this.value = value;
            this.column = column;
            this.row = row;

            this.rectangle = new Rectangle(98, 98, getColorFromValue());
            this.text = new Text(String.valueOf(value));
            text.setFont(new Font(40));
            this.getChildren().addAll(rectangle, text);
        }

        private void addToBoard(Square[][] board, boolean actual) {
            //Add to board Array
            board[column][row] = this;

            //Add to gridPane
            if(actual) {
                gridPane.getChildren().add(this);
                GridPane.setColumnIndex(this, column);
                GridPane.setRowIndex(this, row);
            }
        }

        private void changeSquarePosition(Square[][] board, int newCol, int newRow, boolean actual) {
            //Changing board position
            board[column][row] = null;

            this.column = newCol;
            this.row = newRow;
            board[column][row] = this;

            if(actual) {
                GridPane.setColumnIndex(this, column);
                GridPane.setRowIndex(this, row);
            }
        }
        private void destroySquare(Square[][] board, boolean actual) {
            if (actual) {
                gridPane.getChildren().remove(this);
            }
            board[column][row] = null;
        }

        private void updateValue() {
            this.value += value;
            this.text.setText(String.valueOf(value));
            this.rectangle.setFill(getColorFromValue());
        }

        private Paint getColorFromValue() {
            return switch (value) {
                case 2 -> Color.web("#FFCDD2");   // Light pink
                case 4 -> Color.web("#FFAB91");   // Light coral
                case 8 -> Color.web("#FF8A65");   // Light orange
                case 16 -> Color.web("#FF7043");  // Orange
                case 32 -> Color.web("#FF5722");  // Bright red-orange
                case 64 -> Color.web("#F57F17");  // Golden yellow
                case 128 -> Color.web("#F4B400"); // Yellow
                case 256 -> Color.web("#C6FF00"); // Lime green
                case 512 -> Color.web("#69F0AE"); // Turquoise
                case 1024 -> Color.web("#40C4FF");// Sky blue
                case 2048 -> Color.web("#7C4DFF");// Purple
                default -> WHITE;
            };
        }
        private int getValue() {
            return value;
        }
    }

    //Creating a Random Square -----------------------------------------------------------------
    private void createRandomSquare() {
        if (!checkFull(actualBoard) && moveOccurred) {
            int value = getRandomValue();
            int column ;
            int row;

            getEmptyPositions(actualBoard);

            Pair<Integer, Integer> pair = emptyPositions.get(random.nextInt(emptyPositions.size()));
            column = pair.getKey();
            row = pair.getValue();

            Square square = new Square(value, column, row);
            square.addToBoard(actualBoard, true);
        }
    }

    //Moving and Merging Squares ----------------------------------------------------------
    public void moveMergeSquares(Square[][] board, String direction, boolean actual) {
        long squareCount;
        long newSquareCount;
        moveOccurred = false;
        do {
            squareCount = Arrays.stream(board).flatMap(Arrays::stream).filter(Objects::nonNull).count();
            switch (direction) {
                //Moves and Merge the Squares UP
                case "UP" -> moveMergeUP(board, actual);
                //Moves and Merge the Squares DOWN
                case "DOWN" -> moveMergeDOWN(board, actual);
                //Moves and Merge the Squares Left
                case "LEFT" -> moveMergeLEFT(board, actual);
                //Moves and Merge the Squares Right
                case "RIGHT" -> moveMergeRIGHT(board, actual);
            }
            newSquareCount = Arrays.stream(board).flatMap(Arrays::stream).filter(Objects::nonNull).count();

        } while (newSquareCount != squareCount);

        if(actual) {
            String result = checkTerminal(actualBoard);
            if (result != null) {
                if(result.equals("W")) {
                    terminalLabel.setText("2048!");
                } else {
                    terminalLabel.setText("GAME\nOVER");
                }
                terminalLabel.setVisible(true);
                stop = true;
                movingAI.setSelected(false);
            } else {
                createRandomSquare();
            }
        }
    }
    private void moveMergeUP(Square[][] board, boolean actual) {
        //Move Squares UP
        for (int column = 0; column < 4; column++) {
            int freeRow = 0;
            for (int row = 0; row < 4; row++) {
                if (board[column][row] != null) {
                    if (freeRow != row) {
                        board[column][row].changeSquarePosition(board, column, freeRow, actual);
                        moveOccurred = true;
                    }
                    freeRow++;
                }
            }
        }
        //Merge Squares UP
        for (int column = 0; column < 4; column++) {
            for (int row = 1; row < 4; row++) {
                if (board[column][row] != null && board[column][row - 1] != null) {
                    if (board[column][row].getValue() == board[column][row - 1].getValue()) {
                        board[column][row - 1].updateValue();
                        board[column][row].destroySquare(board, actual);
                        scoreManager.addScore(board[column][row -1].getValue(), actual);
                        moveOccurred = true;
                    }
                }
            }
        }
    }
    private void moveMergeDOWN(Square[][] board, boolean actual) {
        //Move Squares DOWN
        for (int column = 0; column < 4; column++) {
            int freeRow = 3;
            for (int row = 3; row > -1; row--) {
                if (board[column][row] != null) {
                    if (freeRow != row) {
                        board[column][row].changeSquarePosition(board, column, freeRow, actual);
                        moveOccurred = true;                    }
                    freeRow--;
                }
            }
        }
        //Merge Squares DOWN
        for (int column = 0; column < 4; column++) {
            for (int row = 2; row > -1; row--) {
                if (board[column][row] != null && board[column][row + 1] != null) {
                    if (board[column][row].getValue() == board[column][row + 1].getValue()) {
                        board[column][row + 1].updateValue();
                        board[column][row].destroySquare(board, actual);
                        scoreManager.addScore(board[column][row +1].getValue(), actual);
                        moveOccurred = true;
                    }
                }
            }
        }
    }
    private void moveMergeLEFT(Square[][] board, boolean actual) {
        //Move Squares LEFT
        for (int row = 0; row < 4; row++) {
            int freeCol = 0;
            for (int column = 0; column < 4; column++) {
                if (board[column][row] != null) {
                    if (freeCol != column) {
                        board[column][row].changeSquarePosition(board, freeCol, row, actual);
                        moveOccurred = true;                    }
                    freeCol++;
                }
            }
        }
        //Merge Squares LEFT
        for (int row = 0; row < 4; row++) {
            for (int column = 1; column < 4; column++) {
                if (board[column][row] != null && board[column -1][row] != null) {
                    if (board[column][row].getValue() == board[column -1][row].getValue()) {
                        board[column -1][row].updateValue();
                        board[column][row].destroySquare(board, actual);
                        scoreManager.addScore(board[column -1][row].getValue(), actual);
                        moveOccurred = true;                    }
                }
            }
        }
    }
    private void moveMergeRIGHT(Square[][] board, boolean actual) {
        //Move Squares RIGHT
        for (int row = 0; row < 4; row++) {
            int freeCol = 3;
            for (int column = 3; column > -1; column--) {
                if (board[column][row] != null) {
                    if (freeCol != column) {
                        board[column][row].changeSquarePosition(board, freeCol, row, actual);
                        moveOccurred = true;                    }
                    freeCol--;
                }
            }
        }
        //Merge Squares RIGHT
        for (int row = 0; row < 4; row++) {
            for (int column = 2; column > -1; column--) {
                if (board[column][row] != null && board[column +1][row] != null) {
                    if (board[column][row].getValue() == board[column +1][row].getValue()) {
                        board[column +1][row].updateValue();
                        board[column][row].destroySquare(board, actual);
                        scoreManager.addScore(board[column +1][row].getValue(), actual);
                        moveOccurred = true;                    }
                }
            }
        }
    }

    //AI -------------------------------------------------------------------
    private void moveAI() {
        double maxUtility = Double.NEGATIVE_INFINITY;
        String bestMove = null;

        for(String move : new String[]{"UP", "DOWN", "LEFT", "RIGHT"}) {
            Square[][] boardCopy = getDeepCopyBoard(actualBoard);
            int oldScore = scoreManager.getCurrentScore();

            moveMergeSquares(boardCopy, move, false);
            if(!moveOccurred) {
                continue;
            }
            scoreManager.removeValueFromScore(oldScore);

            double utility = expectimax(boardCopy , 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
            if(utility > maxUtility) {
                maxUtility = utility;
                bestMove = move;
            }
        }
        String finalBestMove = bestMove;
        Platform.runLater(() -> {
            if(finalBestMove != null) moveMergeSquares(actualBoard, finalBestMove, true);

        });
    }

    private double expectimax(Square[][] board, int depth, double alpha, double beta, boolean isMaxing) {
        String result = checkTerminal(board);
        if(result != null) {
            if (result.equals("W")) return 1000 - depth;
            if (result.equals("L")) return depth - 1000;
        }

        if(depth >= 4 || checkFull(board)) {
            return evaluateBoardUtility(board);
        }

        if(isMaxing) {
            double maxUtility = Integer.MIN_VALUE;
            for(String move : new String[]{"UP", "DOWN", "LEFT", "RIGHT"}) {
                Square[][] boardCopy = getDeepCopyBoard(board);
                int oldScore = scoreManager.getCurrentScore();

                moveMergeSquares(boardCopy, move, false);
                if(!moveOccurred) {
                    continue;
                }
                scoreManager.removeValueFromScore(oldScore);

                double utility = expectimax(boardCopy, depth +1, alpha, beta, false);
                maxUtility = Math.max(maxUtility, utility);
                alpha = Math.max(alpha, maxUtility);
                if(beta <= alpha) {
                    break;
                }
            }
            return maxUtility;
        } else {
            double expectedUtility = 0.0;
            int numEmptyPositions = countEmptySquares(board);

            for (int column = 0; column < 4; column++) {
                for (int row = 0; row < 4; row++) {
                    if(board[column][row] == null) {
                        Square[][] boardCopy2 = getDeepCopyBoard(board);
                        Square square = new Square(2, column, row);
                        square.addToBoard(boardCopy2, false);

                        expectedUtility += 0.9 * expectimax(boardCopy2, depth +1, alpha, beta, true) / numEmptyPositions;

                        square = new Square(4, column, row);
                        square.addToBoard(boardCopy2, false);

                        expectedUtility += 0.1 * expectimax(boardCopy2, depth +1, alpha, beta, true) / numEmptyPositions;

                        beta = Math.min(expectedUtility, beta);
                        if(beta <= alpha) {
                            break;
                        }
                    }
                }
            }
            return expectedUtility;
        }
    }

    //Heuristics ----------------------------
    private int evaluateBoardUtility(Square[][] board) {
        double utility = 0;
        utility += countEmptySquares(board) * 10;
        utility += calculateMergingPotential(board) * 5;
        utility += calculateMonotonicity(board) * 4;
        return (int) utility;
    }
    private int countEmptySquares(Square[][] board) {
        getEmptyPositions(board);
        return emptyPositions.size();
    }
    private double calculateMergingPotential(Square[][] board) {
        double mergeCount = 0;
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                if(board[column][row] != null) {
                    if(row < 3 && board[column][row +1] != null && board[column][row].getValue() == board[column][row +1].getValue() ||
                            column < 3 && board[column +1][row] != null && board[column][row].getValue() == board[column +1][row].getValue()) {
                        mergeCount++;
                    } else if(row < 3 && column < 3 && board[column +1][row +1] != null && board[column][row].getValue() == board[column +1][row +1].getValue() || row > 0 && column > 0 && board[column -1][row -1] != null && board[column][row].getValue() == board[column -1][row -1].getValue()) {
                        mergeCount += 0.5;
                    }
                }
            }
        }
        return mergeCount;
    }
    private double calculateMonotonicity(Square[][] board) {

        double increaseMono = 0.0;
        double decreaseMono = 0.0;

        //Checking rows
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 3; column++) {
                if (board[column][row] != null && board[column + 1][row] != null) {
                    if (board[column][row].getValue() < board[column + 1][row].getValue()) {
                        increaseMono ++;
                    } else if (board[column][row].getValue() > board[column + 1][row].getValue()){
                        decreaseMono ++;
                    }
                }
            }
        }

        //Checking columns
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 3; row++) {
                if (board[column][row] != null && board[column][row + 1] != null) {
                    if (board[column][row].getValue() < board[column][row + 1].getValue()) {
                        increaseMono ++;
                    } else if (board[column][row].getValue() > board[column][row + 1].getValue()){
                        decreaseMono ++;
                    }
                }
            }
        }
        return -(increaseMono + decreaseMono); //Higher monotonicity (less negative) is better
    }

    //Other -----------------------------------------------------------------------------
    private String checkTerminal(Square[][] board) {
        if (checkWin(board)){
            return "W";
        } else if(checkFull(board) && !checkMergeable(board)) {
            return "L";
        }
        return null;
    }
    private boolean checkWin(Square[][] board) {
        return Arrays.stream(board).flatMap(Arrays::stream).filter(Objects::nonNull).anyMatch(square -> square.getValue() >= 2048);
    }
    private boolean checkFull(Square[][] board) {
        return Arrays.stream(board).flatMap(Arrays::stream).noneMatch(Objects::isNull);
    }
    private boolean checkMergeable(Square[][] board) {
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                if(row > 0 && board[column][row] != null && board[column][row].getValue() == board[column][row -1].getValue() ||
                        column > 0 && board[column][row] != null && board[column][row].getValue() == board[column -1][row].getValue()) {
                    return true;
                }
            }
        }
        return false;
    }
    private Square[][] getDeepCopyBoard(Square[][] board) {
        Square[][] newBoard = new Square[4][4];
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                if(board[column][row] != null) {
                    Square square = new Square(board[column][row].getValue(), column, row);
                    square.addToBoard(newBoard, false);
                }
            }
        }
        return newBoard;
    }
    private int getRandomValue() {
        double value = random.nextDouble();
        if (value <= 0.9) {
            return 2;
        }
        return 4;
    }
    private void getEmptyPositions(Square[][] board) {
        emptyPositions.clear();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if(board[i][j] == null) {
                    emptyPositions.add(new Pair<>(i, j));
                }
            }
        }
    }
    private void reset() {
        terminalLabel.setVisible(false);

        Arrays.stream(actualBoard).flatMap(Arrays::stream).filter(Objects::nonNull).forEach(square -> square.destroySquare(actualBoard, true));
        movingAI.setSelected(false);
        moveOccurred = true;
        createRandomSquare();
        createRandomSquare();
        scoreManager.reset();

        stop = false;
    }
}