/************************************************************
 * Name:  Sujil Maharjan                                    *
 * Project:  Project 1/Ishodo Game			               *
 * Class:  Artificial Intelligence/CMP 331                  *
 * Date:  2/5/2016			                               *
 ************************************************************/
package com.ishido.model;

import java.util.Random;

/**
 * Deck keeps track of the tiles that have already been dealth and deal the next tile in random model
 */
public class Deck implements Cloneable{
    // Declares the total number of colors and symbols
    private final int ROW_COLUMN_NUMBER = 6;

    // Holds the total number of tiles used
    private int totalTilesUsed;

    // Declares a two dimensional array for recording the number of times certain combination is dealt
    private int deckEntries[][] = new int[ROW_COLUMN_NUMBER][ROW_COLUMN_NUMBER];

    /**
     * Declares the whole board as 0
     */
    public Deck() {
        for (int row = 0; row < ROW_COLUMN_NUMBER; ++row) {
            for (int column = 0; column < ROW_COLUMN_NUMBER; ++column) {
                deckEntries[row][column] = 0;
            }
        }
        totalTilesUsed = 0;
    }

    public void removeFromDeck(int color, int symbol) {
        deckEntries[color][symbol]--;
        totalTilesUsed--;
    }
    /**
     * Generates the tile for the random model
     *
     * @param tile TileInfo object that will hold the randomly generated tile
     */
    public boolean generateTile(TileInfo tile) {
        Random random = new Random();
        int color = random.nextInt(6);
        int symbol = random.nextInt(6);

        // Checks if the deck has already maxed out its capacity of this combination. If no, then set the attributes to the tile
        if (deckEntries[color][symbol] < 2) {
            // Put the tile and give the answer
            tile.setColor(color);
            tile.setSymbol(symbol);
        } else {
            // Generates a recursive function if already maxed tile is generated
            if (!isDone()) {
                generateTile(tile);
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifies if the tile is already maxed out. It is more independent function
     *
     * @param color  Color of the provided tile
     * @param symbol Symbol of the provided tile
     * @return Returns if the tile has maxed out its combination
     */
    public boolean verifyTile(int color, int symbol) {
        if (deckEntries[color][symbol] < 2) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Records the tile in the deck
     *
     * @param color  Color of the provided tile
     * @param symbol Symbol of the provided tile
     */
    public void recordTile(int color, int symbol) {
        if (verifyTile(color,symbol)) {
            deckEntries[color][symbol]++;
            totalTilesUsed++;
        }
    }

    /**
     * Checks if all of the tiles have been used
     *
     * @return Returns true if all of the tiles are used.
     */
    public boolean isDone() {
        if (totalTilesUsed >= 72) {
            return true;
        }
        return false;
    }

    /**
     * Gets the total number of rows and columns that the two dimensional array has
     *
     * @return Returns the row and column number
     */
    public int getTotalRowColumn() {
        return ROW_COLUMN_NUMBER;
    }

    public int getNumericTileVal(TileInfo tile ) {
        int total = 0;

        total += (tile.getNumericColorVal()+1) *10;
        total += (tile.getNumericSymbolVal()+1);

        return total;
    }
}
