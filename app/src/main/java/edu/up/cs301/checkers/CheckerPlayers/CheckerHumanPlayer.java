package edu.up.cs301.checkers.CheckerPlayers;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;


import edu.up.cs301.game.GameFramework.utilities.MessageBox;
import edu.up.cs301.checkers.CheckerActionMessage.CheckerMoveAction;
import edu.up.cs301.checkers.CheckerActionMessage.CheckerPromotionAction;
import edu.up.cs301.checkers.CheckerActionMessage.CheckerSelectAction;
import edu.up.cs301.checkers.InfoMessage.CheckerState;
import edu.up.cs301.checkers.Views.CheckerBoardSurfaceView;
import edu.up.cs301.checkers.InfoMessage.Piece;
import edu.up.cs301.game.GameFramework.GameMainActivity;
import edu.up.cs301.game.GameFramework.infoMessage.GameInfo;
import edu.up.cs301.game.GameFramework.infoMessage.IllegalMoveInfo;
import edu.up.cs301.game.GameFramework.infoMessage.NotYourTurnInfo;
import edu.up.cs301.game.GameFramework.players.GameHumanPlayer;
import edu.up.cs301.game.R;

/**
 * @author Griselda
 * @author Katherine
 * @author Ruth
 * @author Nick
 * @author Ethan
 * @version 4.13.2023
 */

public class CheckerHumanPlayer extends GameHumanPlayer implements View.OnTouchListener{

    //Tag for logging
    private static final String TAG = "CheckerHumanPlayer";

    // the surface view
    private CheckerBoardSurfaceView surfaceView;
    //public TextView movesLog;
    private CheckerBoardSurfaceView surfaceViewCheckerBoard;
    private Button resignButton;
    private Button rulesPopUp;

    public boolean isPromotion;
    public Piece currPiece = new Piece(Piece.PieceType.KING, Piece.ColorType.RED, 0, 0);


    // the ID for the layout to use
    private int layoutId;

    private CheckerState state;

    private int x = 8;
    private int y = 8;

    /**
     * constructor for CheckerHumanPlayer
     *
     * @param name the name of the player
     * @param layoutId the ID for the layout to use
     * @param state the CheckerState
     */
    public CheckerHumanPlayer(String name, int layoutId, CheckerState state) {
        super(name);
        this.layoutId = layoutId;
        isPromotion = false;
        this.state = state;
    }

    /** setter for CheckerState
     *
     * @param state new CheckerState
     */
    public void setState(CheckerState state) {
        this.state = state;
    }

    /**
     * Called when the player receives a game-state (or other info) from the
     * game.
     *
     * @param info the message from the game
     */
    @Override
    public void receiveInfo(GameInfo info) {
        if (surfaceViewCheckerBoard == null) {
            return;
        }

        if (info instanceof IllegalMoveInfo || info instanceof NotYourTurnInfo) {
            // if the move was out of turn or otherwise illegal, flash the screen
            surfaceViewCheckerBoard.flash(Color.RED, 50);
        } else if (!(info instanceof CheckerState)) {
            // if we do not have a state, ignore
            return;
        } else {
            surfaceViewCheckerBoard.setState((CheckerState) info);
            surfaceViewCheckerBoard.invalidate();
        }

    }

    /**
     * sets the current player as the activity's GUI
     *
     * @param activity the GameMainActivity
     */
    @Override
    public void setAsGui(GameMainActivity activity) {
        // load the layout resource for the new configuration
        activity.setContentView(layoutId);

        // set the surfaceView instance variable
        surfaceView = (CheckerBoardSurfaceView) myActivity.findViewById(R.id.checkerBoard);
        surfaceView.setOnTouchListener(this);

        surfaceViewCheckerBoard = (CheckerBoardSurfaceView) myActivity.findViewById(R.id.checkerBoard);

        // resignation
        resignButton = myActivity.findViewById(R.id.homeButton);

        // rules button pop-up
        rulesPopUp = myActivity.findViewById(R.id.rulesButton);

        surfaceViewCheckerBoard.setOnTouchListener(this);
        resignButton.setOnTouchListener(this);
        rulesPopUp.setOnTouchListener(this);
    }


    /**
     * returns the GUI's top view
     *
     * @return the GUI's top view
     */
    @Override
    public View getTopView() {
        return myActivity.findViewById(R.id.top_gui_layout);
    }

    /**
     * callback method when the screen it touched. We're
     * looking for a screen touch (which we'll detect on
     * the "up" movement" onto a tic-tac-tie square
     *
     * @param motionEvent the motion event that was detected
     */
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //Rules Button Pop up
        if(view.getId() == rulesPopUp.getId()){
            MessageBox.popUpMessage("Rules of the Game:\n" +
                    "Checkers is played by two people. Each player will alternate turns.\n\n" +
                    "Moves: Each piece moves diagonally, one square forward. Non-capturing moves may move one square forward.\n\n" +
                    "Capture: Jump the opponent's piece adn land in open space diagonally from current position. The open space must be empty\n\n" +
                    "Kinging: To king a piece, you must move across the board to the farthest row. Once kinged you may move in any direction diagonally.\n\n" +
                    "End Game: You win when the opponent is unable to make further moves. All pieces must be captured or trapped. ", myActivity);
        }

        if (view.getId() == resignButton.getId()) {
            CountDownTimer cdt = new CountDownTimer(10, 10) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    myActivity.recreate();
                }
            };
            cdt.start();
        }

        // ignore if not an "down" event
        if (motionEvent.getAction() != MotionEvent.ACTION_DOWN) {
            return true;
        }

        // loop through all of the locations on the board and compare
        // the location pressed to the pixels on the screen to find
        // the exact location of the click according to the b oard
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (!isPromotion) {
                    if (motionEvent.getX() > 20 + (i * 120) && motionEvent.getX() < 175 + (i * 120)) {
                        if (motionEvent.getY() > 20 + (j * 120) && motionEvent.getY() < 175 + (j * 120)) {
                                // create the select action
                            if (state.getPiece(i, j).getPieceColor() == Piece.ColorType.RED && state.getWhoseMove() == 0) {
                                CheckerSelectAction select = new CheckerSelectAction(this, i, j);
                                currPiece = state.getPiece(i, j);
                                game.sendAction(select);
                            } else if (state.getPiece(i, j).getPieceColor() == Piece.ColorType.BLACK && state.getWhoseMove() == 1) {
                                CheckerSelectAction select = new CheckerSelectAction(this, i, j);
                                currPiece = state.getPiece(i, j);
                                game.sendAction(select);
                            } else if (state.getPiece(i, j).getPieceColor() != Piece.ColorType.RED && state.getWhoseMove() == 0) {
                                if (j == 0 && currPiece.getPieceType() == Piece.PieceType.PAWN && state.getWhoseMove() == this.playerNum) {
                                    if (!validPawnMove(i, j, currPiece)) {
                                        CheckerMoveAction move = new CheckerMoveAction(this, i, j);
                                        game.sendAction(move);
                                        break;
                                    }
                                    break;
                                }
                                CheckerMoveAction move = new CheckerMoveAction(this, i, j);
                                game.sendAction(move);
                            } else if (state.getPiece(i, j).getPieceColor() != Piece.ColorType.BLACK && state.getWhoseMove() == 1) {
                                if (j == 7 && currPiece.getPieceType() == Piece.PieceType.PAWN && state.getWhoseMove() == this.playerNum) {
                                    if (!validPawnMove(i, j, currPiece)) {
                                        CheckerMoveAction move = new CheckerMoveAction(this, i, j);
                                        game.sendAction(move);
                                        break;
                                    }
                                    break;
                                }
                                CheckerMoveAction move = new CheckerMoveAction(this, i, j);
                                game.sendAction(move);
                            }
                            surfaceViewCheckerBoard.invalidate();
                        }
                    }
                    if (isPromotion) {
                        sendPromotionAction(i, j, Piece.ColorType.RED);
                        surfaceViewCheckerBoard.invalidate();
                    }
                }
            }
        }
        // register that we have handled the event
        return true;
    }

    public void sendPromotionAction(int xVal, int yVal, Piece.ColorType type) {
        game.sendAction(new CheckerPromotionAction(this,
                new Piece(Piece.PieceType.KING, type, xVal, yVal), xVal, yVal));
    }
    public boolean validPawnMove(int row, int col, Piece currPiece) {
        if(currPiece.getY() > col + 1){
            return false;
        }
        if (currPiece.getX() != row && currPiece.getX() != row - 1 && currPiece.getX() != row + 1) {
            return false;
        }
        if (currPiece.getX() == row) {
            if (state.getPiece(row, col).getPieceType() != Piece.PieceType.EMPTY) {
                return false;
            }
        }

        if (currPiece.getX() == row + 1 || currPiece.getX() == row - 1) {
            if (state.getPiece(row, col).getPieceType() == Piece.PieceType.EMPTY) {
                return false;
            }
        }
        return true;
    }
}
