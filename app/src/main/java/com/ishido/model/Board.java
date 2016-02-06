/************************************************************
 * Name:  Sujil Maharjan                                    *
 * Project:  Project 1/Ishodo Game			               *
 * Class:  Artificial Intelligence/CMP 331                  *
 * Date:  2/5/2016			                               *
 ************************************************************/

package com.ishido.model;

/**
 * Holds the game board and updates the board
 * Created by Tsujil on 1/26/2016.
 */
public class Board {
    // Total number of rows
    private final int TOTAL_ROWS = 8;

    // Total number of columns in the board
    private final int TOTAL_COLUMNS = 12;

    // Two dimensional array of tiles with their respective information
    private TileInfo board[][] = new TileInfo[TOTAL_ROWS][TOTAL_COLUMNS];

    /**
     * Initializes the board with null values in each cell
     */
    public Board() {
        for (int row = 0; row < TOTAL_ROWS; ++row) {
            for (int column = 0; column < TOTAL_COLUMNS; ++column) {
                board[row][column] = null;
            }
        }
    }

    /**
     * Checks if the tile is available in the board
     *
     * @param row    Row number in table
     * @param column Column number in table
     * @return Returns if the tile is available
     */
    public boolean isTileAvailable(int row, int column) {
        return board[row][column] == null;
    }

    /**
     * Fills the tile with TileInfo in the given cell by verifying if it can be placed.
     *
     * @param row    Row number in table
     * @param column Column number in table
     * @param tile   TileInfo provided
     * @return Returns if writing to the cell is successful or not
     */
    public boolean fillTile(int row, int column, TileInfo tile) {
        boolean isNull = true;

        // First verify if we can place the tile. If yes, then check with the tiles on the four sides if there is any rules restricting it from being placed.
        if (column > 0) {
            if (board[row][column - 1] != null) {
                isNull = false;
                if ((tile.getNumericColorVal() == board[row][column - 1].getNumericColorVal()) || (tile.getNumericSymbolVal() == board[row][column - 1].getNumericSymbolVal())) {
                    board[row][column] = tile;
                    //System.out.println("Color: " + board[row][column].getNumericColorVal() + " Symbol: " + board[row][column].getNumericSymbolVal());
                    return true;
                }
            }
        }
        if (column < 11) {
            if (board[row][column + 1] != null) {
                isNull = false;
                if ((tile.getNumericColorVal() == board[row][column + 1].getNumericColorVal()) || (tile.getNumericSymbolVal() == board[row][column + 1].getNumericSymbolVal())) {
                    board[row][column] = tile;
                    //System.out.println("Color: " + board[row][column].getNumericColorVal() + " Symbol: " + board[row][column].getNumericSymbolVal());
                    return true;
                }
            }
        }
        if (row > 0) {
            if (board[row - 1][column] != null) {
                isNull = false;
                if ((tile.getNumericColorVal() == board[row - 1][column].getNumericColorVal()) || (tile.getNumericSymbolVal() == board[row - 1][column].getNumericSymbolVal())) {
                    board[row][column] = tile;
                    return true;
                }
            }
        }
        if (row < 7) {
            if (board[row + 1][column] != null) {
                isNull = false;
                if ((tile.getNumericColorVal() == board[row + 1][column].getNumericColorVal()) || (tile.getNumericSymbolVal() == board[row + 1][column].getNumericSymbolVal())) {
                    board[row][column] = tile;

                    return true;
                }
            }
        }

        // If there is nothing on the sides, then place the tile
        if (isNull == true) {
            board[row][column] = tile;

            return true;
        }

        //System.out.println("end of conditions");
        return false;
    }

    /**
     * Calculates the score that the player gets in each entry
     *
     * @param row    Row number in the table
     * @param column Column number in the table
     * @param tile   TileInfo
     * @return Returns the score of the current tile placed
     */
    public int calculateScore(int row, int column, TileInfo tile) {
        int score = 0;

        // Checks all four sides of the tile for the similarities. If there are, then adds it to the score.
        if (column > 0) {
            if (board[row][column - 1] != null) {
                if ((tile.getNumericColorVal() == board[row][column - 1].getNumericColorVal()) || (tile.getNumericSymbolVal() == board[row][column - 1].getNumericSymbolVal())) {
                    score++;
                }
            }
        }
        if (column < 11) {
            if (board[row][column + 1] != null) {
                if ((tile.getNumericColorVal() == board[row][column + 1].getNumericColorVal()) || (tile.getNumericSymbolVal() == board[row][column + 1].getNumericSymbolVal())) {
                    score++;
                }
            }
        }
        if (row > 0) {
            if (board[row - 1][column] != null) {
                if ((tile.getNumericColorVal() == board[row - 1][column].getNumericColorVal()) || (tile.getNumericSymbolVal() == board[row - 1][column].getNumericSymbolVal())) {
                    score++;
                }
            }
        }
        if (row < 7) {
            if (board[row + 1][column] != null) {
                if ((tile.getNumericColorVal() == board[row + 1][column].getNumericColorVal()) || (tile.getNumericSymbolVal() == board[row + 1][column].getNumericSymbolVal())) {
                    score++;
                }
            }
        }

        return score;
    }

    /**
     * It checks if the board has any tiles left and if there are tiles left, it checks if the tiles are available to be placed.
     *
     * @param deck It consists of the deck that holds current tile combinations
     * @return Returns true if the board cannot place any tiles i.e. it represents the board is done with solutions
     */
    public boolean isDone(Deck deck) {
        // Go through each and every cells in the board and check if there are empty cells where tiles can be placed.
        for (int row = 0; row < TOTAL_ROWS; ++row) {
            for (int column = 0; column < TOTAL_COLUMNS; ++column) {
                // Each cell is represented by board[row][column]
                // Check if the current cell is empty. Only go forward if it is null
                if (board[row][column] == null) {
                    if (checkTileAvailability(row, column, deck)) {
                        return false;
                    }
                }
            }
        }


        return true;
    }

    /**
     * Checks if the tile that meets the color & symbol combination is available in the deck
     *
     * @param row    It represents the row number of the current empty cell
     * @param column It represents the column number of the current empty cell
     * @param deck   It represents the Deck class that contains the current combinations of colors & symbols
     * @return Returns true if there are tiles available to be placed in that particular cell.
     */
    private boolean checkTileAvailability(int row, int column, Deck deck) {
        boolean sideEmpty = true;
        boolean isColorAvailable = false;
        boolean isSymbolAvailable = false;

        // Checks if the color/symbol with different symbol/color is available in the deck. If yes, returns that there are tiles which can be placed in the selected cell.
        if (column > 0) {
            if (board[row][column - 1] != null) {
                sideEmpty = false;
                isColorAvailable = checkColorAvailability(board[row][column - 1].getNumericColorVal(), deck);
                isSymbolAvailable = checkSymbolAvailability(board[row][column - 1].getNumericSymbolVal(), deck);
                if (isColorAvailable || isSymbolAvailable) {
                    return true;
                }
            }
        }

        if (column < 11) {
            if (board[row][column + 1] != null) {
                sideEmpty = false;
                isColorAvailable = checkColorAvailability(board[row][column + 1].getNumericColorVal(), deck);
                isSymbolAvailable = checkSymbolAvailability(board[row][column + 1].getNumericSymbolVal(), deck);
                if (isColorAvailable || isSymbolAvailable) {
                    return true;
                }
            }
        }

        if (row > 0) {
            if (board[row - 1][column] != null) {
                sideEmpty = false;
                isColorAvailable = checkColorAvailability(board[row - 1][column].getNumericColorVal(), deck);
                isSymbolAvailable = checkSymbolAvailability(board[row - 1][column].getNumericSymbolVal(), deck);
                if (isColorAvailable || isSymbolAvailable) {
                    return true;
                }
            }
        }

        if (row < 7) {
            if (board[row + 1][column] != null) {
                sideEmpty = false;
                isColorAvailable = checkColorAvailability(board[row + 1][column].getNumericColorVal(), deck);
                isSymbolAvailable = checkSymbolAvailability(board[row + 1][column].getNumericSymbolVal(), deck);
                if (isColorAvailable || isSymbolAvailable) {
                    return true;
                }
            }
        }

        if (sideEmpty == true) {
            return true;
        }
        return false;
    }

    /**
     * It checks if the color with any symbol is available in the deck
     *
     * @param color It represents the color needed
     * @param deck  It represents the current deck to search into
     * @return Returns true if there is color available (doesn't matter which symbol)
     */
    private boolean checkColorAvailability(int color, Deck deck) {
        // Checks the deck for a certain color with different symbol and their availability
        for (int symbol = 0; symbol < deck.getTotalRowColumn(); ++symbol) {
            if (deck.verifyTile(color, symbol)) {
                return true;
            }
        }
        return false;
    }

    /**
     * It checks if the symbol with any color is available in the deck
     *
     * @param symbol It represents the symbol needed
     * @param deck   It represents the current deck to search into
     * @return Returns true if there is symbol available (doesn't matter which color)
     */
    private boolean checkSymbolAvailability(int symbol, Deck deck) {
        // Checks the deck for a certain symbol with different colors and their availability
        for (int color = 0; color < deck.getTotalRowColumn(); ++color) {
            if (deck.verifyTile(color, symbol)) {
                return true;
            }
        }
        return false;
    }
}
