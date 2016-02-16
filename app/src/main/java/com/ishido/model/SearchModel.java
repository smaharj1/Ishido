package com.ishido.model;

/**
 * Created by Tsujil on 2/14/2016.
 */
public class SearchModel implements Cloneable{


    private Board originalBoard;
    private Deck originalDeck;
    private Board board;
    private Deck deck;
    private int searchType;
    private boolean getNextTile;

    public SearchModel(Board brd, Deck dk) {
        board = brd;
        deck = dk;
        getNextTile = true;
//        try {
//            originalBoard = (Board) board.clone();
//            originalDeck = (Deck) deck.clone();
//        } catch (CloneNotSupportedException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Sets the type of the search the user wants to perform
     * @param type Represents the type of search
     */
    public void setSearchType(int type ) {
        searchType=type;

        // Clear everything to default
    }

    public int getSearchType() {
        return searchType;
    }

//    public TableCoordinates getAvailableCoordinate(TileInfo tile) {
//        if (searchType==DFS) {
//            getNextTile = true;
//            return calculateDFS(tile);
//        }
//        else if (searchType == BFS) {
//            return calculateBFS(tile);
//        }
//
//        return null;
//    }


    private TableCoordinates calculateBFS(TileInfo tile) {

        return null;
    }
    /**
     * Performs the depth first search for the given tile
     * @param tile
     */
    private TableCoordinates calculateDFS(TileInfo tile) {
        // Declares the tableCoordinates
        TableCoordinates tableCoordinates;

        // Perform availability search from the first tile in the board
        for (int row=0; row < Board.TOTAL_ROWS; ++row ) {
            for (int col = 0; col < Board.TOTAL_COLUMNS; ++col) {
                if (board.canFillTile(row, col, tile)) {
                    board.fillTile(row, col, tile);
                    deck.recordTile(tile.getNumericColorVal(),tile.getNumericSymbolVal());
                    return new TableCoordinates(row,col);
                }
            }
        }

        return null;
    }

    public boolean isGetNextTile() {
        return getNextTile;
    }
}
