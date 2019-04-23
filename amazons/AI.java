package amazons;


import static java.lang.Math.*;

import static amazons.Piece.*;
import java.util.Iterator;


/** A Player that automatically generates moves.
 *  @author Mohammed
 */
class AI extends Player {

    /** A position magnitude indicating a win (for white if positive, black
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        _controller.reportMove(move);
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = board();
        if (_myPiece == WHITE) {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        } else {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        if (depth == 0 || board.winner() != null) {
            return staticScore(board);
        }
        if (sense == 1) {
            int score = -INFTY;
            Iterator<Move> whiteMoves = board.legalMoves();
            while (whiteMoves.hasNext()) {
                if (saveMove) {
                    _lastFoundMove = whiteMoves.next();
                    board.makeMove(_lastFoundMove);
                    score = max(score,
                            findMove(board, depth - 1, false, -1, alpha, beta));
                    alpha = max(alpha, score);
                    board.undo();
                } else {
                    board.makeMove(whiteMoves.next());
                    score = max(score,
                            findMove(board, depth - 1, false, -1, alpha, beta));
                    alpha = max(alpha, beta);
                    board.undo();
                }
                if (alpha >= beta) {
                    break;
                }
            }
            return score;
        } else {
            int score = INFTY;
            Iterator<Move> blackMoves = board.legalMoves();
            while (blackMoves.hasNext()) {
                if (saveMove) {
                    _lastFoundMove = blackMoves.next();
                    board.makeMove(_lastFoundMove);
                    score = min(score,
                            findMove(board, depth - 1, false, 1, alpha, beta));
                    beta = max(beta, score);
                    board.undo();
                } else {
                    board.makeMove(blackMoves.next());
                    score = min(score,
                            findMove(board, depth - 1, false, 1, alpha, beta));
                    beta = min(score, beta);
                    board.undo();
                }
                if (alpha >= beta) {
                    break;
                }
            }
            return score;
        }
    }


    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private int maxDepth(Board board) {
        return 1;
    }


    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        Piece winner = board.winner();
        if (winner == BLACK) {
            return -WINNING_VALUE;
        } else if (winner == WHITE) {
            return WINNING_VALUE;
        }

        int wCount = 0;
        int bCount = 0;

        Iterator<Move> whiteMoves = board.legalMoves(WHITE);
        Iterator<Move> blackMoves = board.legalMoves(BLACK);

        while (whiteMoves.hasNext()) {
            whiteMoves.next();
            wCount++;
        }

        while (blackMoves.hasNext()) {
            blackMoves.next();
            bCount++;
        }

        return wCount - bCount;
    }
}
