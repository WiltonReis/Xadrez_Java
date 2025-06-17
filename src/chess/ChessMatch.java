package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.King;
import chess.pieces.Rook;

public class ChessMatch {

    private int turn = 1;
    private Color currentPlayer = Color.WHITE;
    private Board board;

    public ChessMatch() {
        board = new Board(8, 8);
        initialSetup();
    }

    public int getTurn() {
        return turn;
    }

    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    public ChessPiece[][] getPieces(){
        ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                mat[i][j] = (ChessPiece) board.piece(i, j);
            }
        }
        return mat;
    }

    public ChessPiece performChessMove(ChessPosition positionSource, ChessPosition positionTarget) {
        Position source = positionSource.toPosition();
        Position target = positionTarget.toPosition();
        validateSourcePosition(source);
        validadeTargetPosition(source, target);
        Piece capturedPiece = makeMove(source, target);
        changeTurn();
        return (ChessPiece)capturedPiece;
    }

    private Piece makeMove(Position source, Position target) {
        Piece piece = board.removePiece(source);
        Piece capturedPiece = board.removePiece(target);
        board.placePiece(piece, target);
        return capturedPiece;
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition) {
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);
        return board.piece(position).possibleMoves();
    }

    private void validateSourcePosition(Position source) {
        if (!board.thereIsAPiece(source)) throw new ChessException("There is no piece on source position");
        if (((ChessPiece)board.piece(source)).getColor() != currentPlayer) throw new ChessException("The chosen piece is not yours");
        if (!board.piece(source).isThereAnyPossibleMove()) throw new ChessException("There is no possible moves for the chosen piece");
    }

    public void validadeTargetPosition(Position source, Position target) {
        if (!board.piece(source).possibleMove(target)) throw new ChessException("The chosen piece can't move to target position");
    }

    public void changeTurn() {
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private void placePiece(char column, int row, ChessPiece piece){
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
    }

    private void initialSetup() {
        placePiece('a', 1, new Rook(board, Color.WHITE));
        placePiece('h', 1, new Rook(board, Color.WHITE));
        placePiece('d', 1, new King(board, Color.WHITE));
        placePiece('a', 8, new Rook(board, Color.BLACK));
        placePiece('h', 8, new Rook(board, Color.BLACK));
        placePiece('e', 8, new King(board, Color.BLACK));

    }
}
