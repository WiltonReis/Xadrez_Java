package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.King;
import chess.pieces.Rook;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChessMatch {

    private int turn = 1;
    private Color currentPlayer = Color.WHITE;
    private Board board;
    private boolean check;
    private boolean checkMate;

    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();

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

    public boolean getCheck() {
        return check;
    }

    public boolean getCheckMate() {
        return checkMate;
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

        if (testCheck(currentPlayer)){
            undoMove(source, target, (ChessPiece)capturedPiece);
            throw new ChessException("You can't put yourself in check");
        }

        check = (testCheck(opponent(currentPlayer)));

        if(testCheckMate(opponent(currentPlayer))) checkMate = true;
        else changeTurn();

        return (ChessPiece)capturedPiece;
    }

    private Piece makeMove(Position source, Position target) {
        Piece piece = board.removePiece(source);
        Piece capturedPiece = board.removePiece(target);
        board.placePiece(piece, target);
        if (capturedPiece != null){
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }
        return capturedPiece;
    }

    public void undoMove(Position source, Position target, ChessPiece capturedPiece){
        Piece p = board.removePiece(target);
        board.placePiece(p, source);
        if (capturedPiece != null){
            board.placePiece(capturedPiece, target);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }
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
        piecesOnTheBoard.add(piece);
    }

    private Color opponent(Color color){
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private ChessPiece king(Color color){
        return (ChessPiece)piecesOnTheBoard.stream()
                .filter(x -> x instanceof King && ((ChessPiece)x).getColor() == color)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("There is no " + color + " king on the board"));
    }

    private boolean testCheck(Color color){
        Position kingPosition = king(color).getChessPosition().toPosition();
        return piecesOnTheBoard.stream()
                .filter(x -> ((ChessPiece)x).getColor() == opponent(color))
                .anyMatch(x -> x.possibleMove(kingPosition));
    }

    private boolean testCheckMate(Color color){
        if (!testCheck(color)) return false;

        List<Piece> list = piecesOnTheBoard.stream()
                .filter(x -> ((ChessPiece)x).getColor() == color)
                .collect(Collectors.toList());

        for (Piece p : list){
            boolean[][] mat = p.possibleMoves();
            for (int i = 0; i < board.getRows(); i++){
                for (int j = 0; j < board.getColumns(); j++){
                    if (mat[i][j]){
                        Position source = ((ChessPiece)p).getChessPosition().toPosition();
                        Position target = new Position(i, j);
                        Piece capturedPiece = makeMove(source, target);
                        boolean testCheck = testCheck(color);
                        undoMove(source, target, (ChessPiece)capturedPiece);
                        if (!testCheck) return false;
                    }
                }
            }
        }
        return true;
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
