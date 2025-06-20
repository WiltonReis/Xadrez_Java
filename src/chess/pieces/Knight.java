package chess.pieces;

import boardgame.Board;
import boardgame.Position;
import chess.ChessPiece;
import chess.Color;

public class Knight extends ChessPiece {

    public Knight(Board board, Color color) {
        super(board, color);
    }

    @Override
    public boolean[][] possibleMoves() {
        boolean[][] moves = new boolean[getBoard().getRows()][getBoard().getColumns()];

        Position p = new Position(0, 0);

        //nw
        p.setValues(position.getRow() - 2, position.getColumn() - 1);
        if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
            moves[p.getRow()][p.getColumn()] = true;
        }
        if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
            moves[p.getRow()][p.getColumn()] = true;
        }

        //ne
        p.setValues(position.getRow() - 2, position.getColumn() + 1);
        if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
            moves[p.getRow()][p.getColumn()] = true;
        }
        if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
            moves[p.getRow()][p.getColumn()] = true;
        }

        //se
        p.setValues(position.getRow() + 2, position.getColumn() + 1);
        if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
            moves[p.getRow()][p.getColumn()] = true;
        }
        if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
            moves[p.getRow()][p.getColumn()] = true;
        }

        //sw
        p.setValues(position.getRow() + 2, position.getColumn() - 1);
        if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
            moves[p.getRow()][p.getColumn()] = true;
        }
        if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
            moves[p.getRow()][p.getColumn()] = true;
        }

        //nw
        p.setValues(position.getRow() - 1, position.getColumn() - 2);
        if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
            moves[p.getRow()][p.getColumn()] = true;
        }
        if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
            moves[p.getRow()][p.getColumn()] = true;
        }

        //ne
        p.setValues(position.getRow() - 1, position.getColumn() + 2);
        if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
            moves[p.getRow()][p.getColumn()] = true;
        }
        if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
            moves[p.getRow()][p.getColumn()] = true;
        }

        //se
        p.setValues(position.getRow() + 1, position.getColumn() + 2);
        if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
            moves[p.getRow()][p.getColumn()] = true;
        }
        if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
            moves[p.getRow()][p.getColumn()] = true;
        }

        //sw
        p.setValues(position.getRow() + 1, position.getColumn() - 2);
        if (getBoard().positionExists(p) && !getBoard().thereIsAPiece(p)) {
            moves[p.getRow()][p.getColumn()] = true;
        }
        if (getBoard().positionExists(p) && isThereOpponentPiece(p)) {
            moves[p.getRow()][p.getColumn()] = true;
        }

        return moves;
    }

    @Override
    public String toString() {
        return "N";
    }
}
