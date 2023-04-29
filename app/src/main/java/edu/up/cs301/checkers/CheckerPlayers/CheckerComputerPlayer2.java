package edu.up.cs301.checkers.CheckerPlayers;

import java.util.ArrayList;
import java.util.Collections;

import edu.up.cs301.checkers.CheckerActionMessage.CheckerMoveAction;
import edu.up.cs301.checkers.CheckerActionMessage.CheckerPromotionAction;
import edu.up.cs301.checkers.CheckerActionMessage.CheckerSelectAction;
import edu.up.cs301.checkers.InfoMessage.CheckerState;
import edu.up.cs301.checkers.InfoMessage.Piece;
import edu.up.cs301.game.GameFramework.infoMessage.IllegalMoveInfo;
import edu.up.cs301.game.GameFramework.infoMessage.NotYourTurnInfo;
import edu.up.cs301.game.GameFramework.players.GameComputerPlayer;
import edu.up.cs301.game.GameFramework.infoMessage.GameInfo;

public class CheckerComputerPlayer2 extends GameComputerPlayer {
    private Piece selection;
    private ArrayList<Piece> availablePieces;
    private ArrayList<Piece> capturePieces;
    private boolean isCapturePieces = false;
    private int xVal;
    private int yVal;

    /**
     * Constructor for the CheckerComputerPlayer2 class
     * @param name name of smart ai
     */
    public CheckerComputerPlayer2(String name) {
        // invoke superclass constructor
        super(name); // invoke superclass constructor
    }

    /**
     * Called when the player receives a game-state (or other info) from the
     * game.
     *
     * @param info the message from the game
     */
    @Override
    protected void receiveInfo(GameInfo info) {
        // if it was a "not your turn" message, just ignore it
        if (info instanceof NotYourTurnInfo) return;
        //Ignore illegal move info too
        if (info instanceof IllegalMoveInfo) return;
        CheckerState checkerState = new CheckerState((CheckerState) info);

        //check if turn
        if (checkerState.getWhoseMove() == 1 && playerNum == 0) {
            return;
        }
        if (checkerState.getWhoseMove() == 0 && playerNum == 1) {
            return;
        }

        isCapturePieces = false;

        //check all of the pieces that can move on the computers side
        availablePieces = new ArrayList<>();
        capturePieces = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (checkerState.getDrawing(i, j) == 1) {
                    return;
                }
                if (checkerState.getDrawing(i, j) == 3) {
                    sleep(1);
                }
                Piece p = checkerState.getPiece(i, j);
                if (playerNum == 0 && p.getPieceColor() == Piece.ColorType.RED) {
                    availablePieces.add(p);
                    if (canTake(checkerState, i, j)) {
                        capturePieces.add(p);
                        isCapturePieces = true;
                    }
                } else if (playerNum == 1 && p.getPieceColor() == Piece.ColorType.BLACK) {
                    availablePieces.add(p);
                    if (canTake(checkerState, i, j)) {
                        capturePieces.add(p);
                        isCapturePieces = true;
                    }
                }
            }
        }

        // Returns if there are no more pieces
        if (availablePieces.isEmpty()) return;

        // Shuffle which pieces to choose
        Collections.shuffle(availablePieces);
        Collections.shuffle(capturePieces);

        // Choosing between piece that can capture or not
        if (!capturePieces.isEmpty()) {
            selection = capturePieces.get(0);
        } else {
            selection = availablePieces.get(0);
        }

        // create variables to hold the x and y of the position selected
        xVal = selection.getX();
        yVal = selection.getY();

        //call the selection game action
        game.sendAction(new CheckerSelectAction(this, xVal, yVal));

        //check if the selected piece can move
        CheckerState checkerState2 = (CheckerState) game.getGameState();
        if (isCapturePieces) {
            for (int i = 1; i < capturePieces.size(); i++) {
                if (!checkerState2.getCanMove()) {
                    selection = capturePieces.get(i);
                    xVal = selection.getX();
                    yVal = selection.getY();
                    game.sendAction(new CheckerSelectAction(this, xVal, yVal));
                } else {
                    break;
                }
            }
        } else {
            for (int i = 1; i < availablePieces.size(); i++) {
                if (!checkerState2.getCanMove()) {
                    selection = availablePieces.get(i);
                    xVal = selection.getX();
                    yVal = selection.getY();
                    game.sendAction(new CheckerSelectAction(this, xVal, yVal));
                } else {
                    break;
                }
            }
        }
        sleep(1);

        //check if the game is over and return
        if(checkerState2.getGameOver()) {
            return;
        }
        // an arrayList that holds the index values of the two movement arraylists (x and y)
        ArrayList<Integer> index = new ArrayList<>();

        // add all of the indexes into the ints value
        for (int i = 0; i < checkerState2.getNewMovementsX().size(); i++) {
            index.add(i);
        }

        // set the x and y values to the new movements array at the index
        for(int i = 0; i < index.size(); i++) {
            xVal = checkerState2.getNewMovementsX().get(index.get(i));
            yVal = checkerState2.getNewMovementsY().get(index.get(i));
            if (checkerState2.getPiece(xVal, yVal).getPieceColor() != Piece.ColorType.EMPTY) {
                break;
            }
        }
        if (isCapturePieces) {
            for (Piece piece : capturePieces) {
                if (canTake(checkerState2,piece.getX(),piece.getY())) {
                    takePiece(checkerState2,xVal,yVal,index);
                    break;
                }
            }
        } else {
            for (Piece piece : availablePieces) {
                if (canTake(checkerState2, piece.getX(), piece.getY())) {
                    takePiece(checkerState2, xVal, yVal, index);
                    break;
                }
            }
        }

        // if the piece is a pawn look for promotion
        if (selection.getPieceType() == Piece.PieceType.PAWN) {
            if (selection.getPieceColor() == Piece.ColorType.BLACK) {
                if (yVal == 7) {
                    game.sendAction(new CheckerPromotionAction(this,
                            new Piece(Piece.PieceType.KING,
                                    Piece.ColorType.BLACK, xVal, yVal), xVal, yVal));

                }
            } else if (selection.getPieceColor() == Piece.ColorType.RED) {
                if (yVal == 0) {
                    game.sendAction(new CheckerPromotionAction(this,
                            new Piece(Piece.PieceType.KING,
                                    Piece.ColorType.RED, xVal, yVal), xVal, yVal));

                }
            }
        }
        // send the new move action
        game.sendAction(new CheckerMoveAction(this, xVal, yVal));

        // logic for if the AI wins
        // assume true
        boolean gameOver = true;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if (checkerState2.getPiece(i,j).getPieceColor() == Piece.ColorType.RED) {
                    // if any black pieces remain, the game is not over
                    gameOver = false;
                }
            }
        }

        if(gameOver) {
            //line used to debug and ensure proper functionality
            System.out.println("test");
        }
    }

    /**
     * Take a piece
     *
     * @param checkerState2 second CheckerState
     * @param x x coord
     * @param y y coord
     * @param index arraylist storing index of moves
     */
    public void takePiece(CheckerState checkerState2, int x, int y, ArrayList<Integer> index) {
        for (int i = 0; i < checkerState2.getNewMovementsX().size(); i++) {
            if ((x - checkerState2.getNewMovementsX().get(i) == 2 || x - checkerState2.getNewMovementsX().get(i) == -2) &&
                    (y - checkerState2.getNewMovementsY().get(i) == 2 || y - checkerState2.getNewMovementsY().get(i) == -2)) {
                xVal = checkerState2.getNewMovementsX().get(index.get(i));
                yVal = checkerState2.getNewMovementsY().get(index.get(i));
                break;
            }
        }
    }

    /**
     * Check if a piece can take another piece
     *
     * @param checkerState2 second CheckerState
     * @param x x coord
     * @param y y coord
     * @return false if cannot take, true if can take
     */
    public boolean canTake(CheckerState checkerState2, int x, int y) {
        boolean take = false;
        Piece p = checkerState2.getPiece(x,y);
        for (int i = 0; i < checkerState2.getNewMovementsX().size(); i++) {
            if (p.getPieceType() == Piece.PieceType.PAWN) {
                if (Math.abs(x - checkerState2.getNewMovementsX().get(i)) == -2 &&
                        (Math.abs(y - checkerState2.getNewMovementsY().get(i)) == -2 || Math.abs(y - checkerState2.getNewMovementsY().get(i)) == 2)) {
                    take = true;
                    break;
                }
            } else if (p.getPieceType() == Piece.PieceType.KING) {
                if ((Math.abs(x - checkerState2.getNewMovementsX().get(i)) == -2 || Math.abs(x - checkerState2.getNewMovementsX().get(i)) == 2) &&
                        (Math.abs(y - checkerState2.getNewMovementsY().get(i)) == -2 || Math.abs(y - checkerState2.getNewMovementsY().get(i)) == 2)) {
                    take = true;
                    break;
                }
            }
        }
        return take;
    }



}