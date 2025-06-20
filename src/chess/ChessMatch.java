package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.*;

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
        ChessPiece piece = (ChessPiece) board.removePiece(source);
        piece.increaseMoveCount();
        Piece capturedPiece = board.removePiece(target);
        board.placePiece(piece, target);
        if (capturedPiece != null){
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }
        return capturedPiece;
    }

    public void undoMove(Position source, Position target, ChessPiece capturedPiece){
        ChessPiece p = (ChessPiece) board.removePiece(target);
        p.decreaseMoveCount();
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
        placePiece('c', 1, new Bishop(board, Color.WHITE));
        placePiece('f', 1, new Bishop(board, Color.WHITE));
        placePiece('c', 8, new Bishop(board, Color.BLACK));
        placePiece('f', 8, new Bishop(board, Color.BLACK));
        placePiece('b', 1, new Knight(board, Color.WHITE));
        placePiece('g', 1, new Knight(board, Color.WHITE));
        placePiece('b', 8, new Knight(board, Color.BLACK));
        placePiece('g', 8, new Knight(board, Color.BLACK));
        placePiece('e', 1, new Queen(board, Color.WHITE));
        placePiece('d', 8, new Queen(board, Color.BLACK));
        placePiece('a', 2, new Pawn(board, Color.WHITE));
        placePiece('b', 2, new Pawn(board, Color.WHITE));
        placePiece('c', 2, new Pawn(board, Color.WHITE));
        placePiece('d', 2, new Pawn(board, Color.WHITE));
        placePiece('e', 2, new Pawn(board, Color.WHITE));
        placePiece('f', 2, new Pawn(board, Color.WHITE));
        placePiece('g', 2, new Pawn(board, Color.WHITE));
        placePiece('h', 2, new Pawn(board, Color.WHITE));
        placePiece('a', 7, new Pawn(board, Color.BLACK));
        placePiece('b', 7, new Pawn(board, Color.BLACK));
        placePiece('c', 7, new Pawn(board, Color.BLACK));
        placePiece('d', 7, new Pawn(board, Color.BLACK));
        placePiece('e', 7, new Pawn(board, Color.BLACK));
        placePiece('f', 7, new Pawn(board, Color.BLACK));
        placePiece('g', 7, new Pawn(board, Color.BLACK));
        placePiece('h', 7, new Pawn(board, Color.BLACK));

    }
}
