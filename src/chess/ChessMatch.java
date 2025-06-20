package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChessMatch {

    private int turn = 1;
    private Color currentPlayer = Color.WHITE;
    private Board board;
    private boolean check;
    private boolean checkMate;
    private ChessPiece enPassantVulnerable;
    private ChessPiece promoted;

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

    public ChessPiece getEnPassantVulnerable() {
        return enPassantVulnerable;
    }

    public ChessPiece getPromoted() {
        return promoted;
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

        ChessPiece movedPiece = (ChessPiece)board.piece(target);

        if (testCheck(currentPlayer)){
            undoMove(source, target, (ChessPiece)capturedPiece);
            throw new ChessException("You can't put yourself in check");
        }

        // #specialmove promotion
        promoted = null;
        if (movedPiece instanceof Pawn){
            if (movedPiece.getColor() == Color.WHITE && target.getRow() == 0 || movedPiece.getColor() == Color.BLACK && target.getRow() == 7){
                promoted = (ChessPiece) board.piece(target);
                promoted = replacePromotedPiece("Q");
            }
        }

        check = (testCheck(opponent(currentPlayer)));

        if(testCheckMate(opponent(currentPlayer))) checkMate = true;
        else changeTurn();

        // #specialmove en passant
        if (movedPiece instanceof Pawn && target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2){
            enPassantVulnerable = movedPiece;
        } else enPassantVulnerable = null;

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

        // #specialmove castling kingside rook
        if (piece instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceRook = new Position(source.getRow(), source.getColumn() + 3);
            Position targetRook = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece) board.removePiece(sourceRook);
            board.placePiece(rook, targetRook);
            rook.increaseMoveCount();
        }

        // #specialmove castling queenside rook
        if (piece instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceRook = new Position(source.getRow(), source.getColumn() - 4);
            Position targetRook = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece) board.removePiece(sourceRook);
            board.placePiece(rook, targetRook);
            rook.increaseMoveCount();
        }

        // #specialmove en passant
        if (piece instanceof  Pawn ){
            if (source.getColumn() != target.getColumn() && capturedPiece == null){
                Position pawnPosition;
                if (piece.getColor() == Color.WHITE) pawnPosition = new Position(target.getRow() + 1, target.getColumn());
                else pawnPosition = new Position(target.getRow() - 1, target.getColumn());
                capturedPiece = board.removePiece(pawnPosition);
                capturedPieces.add(capturedPiece);
                piecesOnTheBoard.remove(capturedPiece);
            }
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

        // #specialmove castling kingside rook
        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceRook = new Position(source.getRow(), source.getColumn() + 3);
            Position targetRook = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece) board.removePiece(targetRook);
            board.placePiece(rook, sourceRook);
            rook.decreaseMoveCount();
        }

        // #specialmove castling queenside rook
        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceRook = new Position(source.getRow(), source.getColumn() - 4);
            Position targetRook = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece) board.removePiece(targetRook);
            board.placePiece(rook, sourceRook);
            rook.decreaseMoveCount();
        }

        // #specialmove en passant
        if (p instanceof  Pawn ){
            if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable){
                ChessPiece pawn = (ChessPiece)board.removePiece(target);
                Position pawnPosition;
                if (p.getColor() == Color.WHITE) pawnPosition = new Position(3, target.getColumn());
                else pawnPosition = new Position(4, target.getColumn());

                board.placePiece(pawn, pawnPosition);
            }
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

    public ChessPiece replacePromotedPiece(String type) {
        if (promoted == null) throw new IllegalStateException("There is no piece to be promoted");
        if (!type.equals("B") && !type.equals("R") && !type.equals("Q") && !type.equals("N")) return promoted;

        Position pos = promoted.getChessPosition().toPosition();
        board.removePiece(pos);
        piecesOnTheBoard.remove(promoted);

        ChessPiece p = newPiece(type, promoted.getColor());
        board.placePiece(p, pos);
        piecesOnTheBoard.add(p);
        return p;
    }

    private ChessPiece newPiece(String type, Color color) {
        if (type.equals("R")) return new Rook(board, color);
        if (type.equals("B")) return new Bishop(board, color);
        if (type.equals("N")) return new Knight(board, color);
        return new Queen(board, color);
    }


    private void initialSetup() {
        placePiece('a', 1, new Rook(board, Color.WHITE));
        placePiece('b', 1, new Knight(board, Color.WHITE));
        placePiece('c', 1, new Bishop(board, Color.WHITE));
        placePiece('d', 1, new Queen(board, Color.WHITE));
        placePiece('e', 1, new King(board, Color.WHITE, this));
        placePiece('f', 1, new Bishop(board, Color.WHITE));
        placePiece('g', 1, new Knight(board, Color.WHITE));
        placePiece('h', 1, new Rook(board, Color.WHITE));
        placePiece('a', 2, new Pawn(board, Color.WHITE, this));
        placePiece('b', 2, new Pawn(board, Color.WHITE, this ));
        placePiece('c', 2, new Pawn(board, Color.WHITE, this));
        placePiece('d', 2, new Pawn(board, Color.WHITE, this));
        placePiece('e', 2, new Pawn(board, Color.WHITE, this));
        placePiece('f', 2, new Pawn(board, Color.WHITE, this));
        placePiece('g', 2, new Pawn(board, Color.WHITE, this));
        placePiece('h', 2, new Pawn(board, Color.WHITE, this));

        placePiece('a', 8, new Rook(board, Color.BLACK));
        placePiece('b', 8, new Knight(board, Color.BLACK));
        placePiece('c', 8, new Bishop(board, Color.BLACK));
        placePiece('d', 8, new Queen(board, Color.BLACK));
        placePiece('e', 8, new King(board, Color.BLACK, this));
        placePiece('f', 8, new Bishop(board, Color.BLACK));
        placePiece('g', 8, new Knight(board, Color.BLACK));
        placePiece('h', 8, new Rook(board, Color.BLACK));
        placePiece('a', 7, new Pawn(board, Color.BLACK, this));
        placePiece('b', 7, new Pawn(board, Color.BLACK, this));
        placePiece('c', 7, new Pawn(board, Color.BLACK, this));
        placePiece('d', 7, new Pawn(board, Color.BLACK, this));
        placePiece('e', 7, new Pawn(board, Color.BLACK, this));
        placePiece('f', 7, new Pawn(board, Color.BLACK, this));
        placePiece('g', 7, new Pawn(board, Color.BLACK, this));
        placePiece('h', 7, new Pawn(board, Color.BLACK, this));
    }
}
