package amazons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

import static amazons.Piece.*;
import static amazons.Move.mv;
import static amazons.Controller.*;


/** The state of an Amazons Game.
 *  @author Mohammed Abu-Sharkh
 */
class Board implements Cloneable {

    /** The number of squares on a side of the board. */
    static final int SIZE = 10;

    /** Board 1D. */
    private Piece[] _board;
    /** Arraylist of moves. */
    private ArrayList<Move> moveList = new ArrayList<>();
    /** The number of moves taken thus far. */
    private int _nMoves;


    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        this._board = model._board.clone();
        this._turn = model._turn;
        this._nMoves = model._nMoves;
        this.moveList = model.moveList;
    }

    /** Clears the board to the initial position. */
    void init() {
        _turn = WHITE;
        _winner = null;
        _board = new Piece[(int) Math.pow(SIZE, 2)];
        _nMoves = -1;
        for (int i = 0; i < ((int) Math.pow(SIZE, 2)); i++) {
            _board[i] = Piece.EMPTY;
        }
        put(WHITE, 3, 0);
        put(WHITE, 6, 0);
        put(BLACK, 3, 9);
        put(BLACK, 6, 9);
        put(WHITE, 0, 3);
        put(WHITE, 9, 3);
        put(BLACK, 0, 6);
        put(BLACK, 9, 6);

    }

    /** Return the Piece whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the number of moves (that have not been undone) for this
     *  board. */
    int numMoves() {
        return _nMoves;
    }

    /** Return the winner in the current position, or null if the game is
     *  not yet finished. */
    Piece winner() {
        Iterator<Move> currentPlayerMoves = legalMoves();
        if (!currentPlayerMoves.hasNext()) {
            _winner = _turn.opponent();
            return _winner;
        } else {
            return null;
        }
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return _board[s.index()];
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return get(Square.sq(col, row));
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(Square.sq(col - 'a', row - 1));
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        _board[s.index()] = p;
        _winner = winner();
    }

    /** Set square (COL, ROW) to P. */
    final void put(Piece p, int col, int row) {
        put(p, Square.sq(col, row));

    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, col - 'a', row - 1);
    }

    /** Return true iff FROM - TO is an unblocked queen move on the current
     *  board, ignoring the contents of ASEMPTY, if it is encountered.
     *  For this to be true, FROM-TO must be a queen move and the
     *  squares along it, other than FROM and ASEMPTY, must be
     *  empty. ASEMPTY may be null, in which case it has no effect. */
    boolean isUnblockedMove(Square from, Square to, Square asEmpty) {
        if (!from.isQueenMove(to)) {
            return false;
        } else {
            int rowD = Math.abs(from.row() - to.row());
            int colD = Math.abs(from.col() - to.col());
            for (int i = 1; i <= Math.max(colD, rowD); i++) {
                Square tempS = from.queenMove(from.direction(to), i);
                Piece temp = get(tempS);
                if (!temp.toName().equals("EMPTY") && tempS != asEmpty) {
                    return false;
                }
            }
            return true;
        }
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        if (Square.exists(from.col(), from.row())) {
            if (_board[from.index()] == _turn) {
                return true;
            }
        }
        return false;
    }

    /** Return true iff FROM-TO is a valid first part of move, ignoring
     *  spear throwing. */
    boolean isLegal(Square from, Square to) {
        return isUnblockedMove(from, to, null) && isLegal(from);
    }

    /** Return true iff FROM-TO(SPEAR) is a legal move in the current
     *  position. */
    boolean isLegal(Square from, Square to, Square spear) {
        return isLegal(from, to) && isLegal(to, spear) && get(from) == _turn;
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to(), move.spear());
    }


    /** Move FROM-TO(SPEAR), assuming this is a legal move. */
    void makeMove(Square from, Square to, Square spear) {
        put(get(from), to);
        put(EMPTY, from);
        put(SPEAR, spear);
        moveList.add(mv(from, to, spear));
        _turn = _turn.opponent();
        _nMoves += 1;
    }

    /** Track whether move been made. */
    private boolean moveMade = false;
    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        if (isLegal(move)) {
            moveMade = true;
            makeMove(move.from(), move.to(), move.spear());
        }
    }
    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (winner() != null) {
            _winner = null;
        }
        if (moveMade) {
            Move lastMove = moveList.get(moveList.size() - 1);
            moveList.remove(moveList.size() - 1);
            put(get(lastMove.to()), lastMove.from());
            put(EMPTY, lastMove.to());
            put(EMPTY, lastMove.spear());
            _turn = _turn.opponent();
        }
    }

    /** Return an Iterator over the Squares that are reachable by an
     *  unblocked queen move from FROM. Does not pay attention to what
     *  piece (if any) is on FROM, nor to whether the game is finished.
     *  Treats square ASEMPTY (if non-null) as if it were EMPTY.  (This
     *  feature is useful when looking for Moves, because after moving a
     *  piece, one wants to treat the Square it came from as empty for
     *  purposes of spear throwing.) */
    Iterator<Square> reachableFrom(Square from, Square asEmpty) {
        return new ReachableFromIterator(from, asEmpty);
    }

    /** Return an Iterator over all legal moves on the current board. */
    Iterator<Move> legalMoves() {
        return new LegalMoveIterator(_turn);
    }

    /** Return an Iterator over all legal moves on the current board for
     *  SIDE (regardless of whose turn it is). */
    Iterator<Move> legalMoves(Piece side) {
        return new LegalMoveIterator(side);
    }

    /** An iterator used by reachableFrom. */
    private class ReachableFromIterator implements Iterator<Square> {

        /** Iterator of all squares reachable by queen move from FROM,
         *  treating ASEMPTY as empty. */
        ReachableFromIterator(Square from, Square asEmpty) {
            _from = from;
            _dir = -1;
            _steps = 1;
            _asEmpty = asEmpty;
            toNext();
        }

        @Override
        public boolean hasNext() {
            return _dir < 8 && _from != null;
        }

        @Override
        public Square next() {
            Square nextM = _from.queenMove(_dir, _steps);
            toNext();
            return nextM;
        }
        /** Advance _dir and _steps, so that the next valid Square is
         *  _steps steps in direction _dir from _from. */
        private void toNext() {
            _steps += 1;
            while (!isUnblockedMove(_from,
                    _from.queenMove(_dir, _steps), _asEmpty)
                    && hasNext()) {
                _steps = 1;
                _dir += 1;
            }
        }



        /** Starting square. */
        private Square _from;
        /** Current direction. */
        private int _dir;
        /** Current distance. */
        private int _steps;
        /** Square treated as empty. */
        private Square _asEmpty;

    }

    /** An iterator used by legalMoves. */
    private class LegalMoveIterator implements Iterator<Move> {

        /** All legal moves for SIDE (WHITE or BLACK). */
        LegalMoveIterator(Piece side) {
            _startingSquares = Square.iterator();
            _spearThrows = NO_SQUARES;
            _pieceMoves = NO_SQUARES;
            _fromPiece = side;
            toNext();
        }

        @Override
        public boolean hasNext() {
            return _startingSquares.hasNext();
        }

        @Override
        public Move next() {
            Move res = Move.mv(_start, _nextSquare, spSquare);
            toNext();
            return res;
        }


        /** Advance so that the next valid Move is
         *  _start-_nextSquare(sp), where sp is the next value of
         *  _spearThrows. */
        private void toNext() {
            if (!_spearThrows.hasNext()) {
                if (!_pieceMoves.hasNext()) {
                    while (_startingSquares.hasNext()) {
                        Square a = _startingSquares.next();
                        if (get(a).equals(_fromPiece)) {
                            _start = a;
                            _pieceMoves = reachableFrom(_start, null);
                            _nextSquare = _pieceMoves.next();
                            if (_nextSquare == null) {
                                continue;
                            }
                            _spearThrows = reachableFrom(_nextSquare, _start);
                            spSquare = _spearThrows.next();
                            break;
                        }
                    }
                } else {
                    _nextSquare = _pieceMoves.next();
                    _spearThrows = reachableFrom(_nextSquare, _start);
                    spSquare = _spearThrows.next();
                }
            } else {
                spSquare = _spearThrows.next();
            }
        }

        /** Color of side whose moves we are iterating. */
        private Piece _fromPiece;
        /** Current starting square. */
        private Square _start;
        /** Remaining starting squares to consider. */
        private Iterator<Square> _startingSquares;
        /** Current piece's new position. */
        private Square _nextSquare;
        /** Remaining moves from _start to consider. */
        private Iterator<Square> _pieceMoves;
        /** Remaining spear throws from _piece to consider. */
        private Iterator<Square> _spearThrows;
        /** Temp square holding sp for iterator.*/
        private Square spSquare;

    }

    @Override
    public String toString() {
        String toReturn = "";
        for (int i = SIZE; i > 0; i--) {
            toReturn += ("  ");
            for (int j = 0; j < SIZE; j++) {
                if (get(j, i - 1).equals(WHITE)) {
                    toReturn += " W";
                } else if (get(j, i - 1).equals(BLACK)) {
                    toReturn += " B";
                } else if (get(j, i - 1).equals(EMPTY)) {
                    toReturn += " -";
                } else {
                    toReturn += " S";
                }
            }
            toReturn += ("\n");
        }
        return toReturn;
    }

    /** An empty iterator for initialization. */
    private static final Iterator<Square> NO_SQUARES =
        Collections.emptyIterator();

    /** Piece whose turn it is (BLACK or WHITE). */
    private Piece _turn;
    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;
}
