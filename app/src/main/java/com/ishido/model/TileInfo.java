/************************************************************
 * Name:  Sujil Maharjan                                    *
 * Project:  Project 1/Ishodo Game			               *
 * Class:  Artificial Intelligence/CMP 331                  *
 * Date:  2/5/2016			                               *
 ************************************************************/
package com.ishido.model;

import android.graphics.Color;

/**
 * Holds the information of a tile
 */
public class TileInfo {
    // Numeric values of the colors
    private final int RED = 0;
    private final int BLUE = 1;
    private final int GREEN = 2;
    private final int YELLOW = 3;
    private final int GRAY = 4;
    private final int CYAN = 5;

    // Numeric values of the symbol
    private final int ASTERISK = 0;
    private final int AMPER = 1;
    private final int DOLLAR = 2;
    private final int HASH = 3;
    private final int RATE = 4;
    private final int PERCENT = 5;

    // Initialization of color, symbol
    private int color = -1;
    private String symbol = "";
    private boolean isEmpty;
    private int numericColorVal = -1;
    private int numericSymbolVal = -1;

    /**
     * Initializes the tile as empty
     */
    public TileInfo() {
        isEmpty = true;
    }

    /**
     * @return Returns the color of the tile
     */
    public int getColor() {
        return color;
    }

    /**
     * @return Returns the symbol of the tile
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @return Returns if the tile is empty
     */
    public boolean isTileEmpty() {
        return isEmpty;
    }

    /**
     * @return Returns the numeric equivalent of the color
     */
    public int getNumericColorVal() {
        return numericColorVal;
    }

    /**
     * @return Returns the numeric equivalent of the symbol
     */
    public int getNumericSymbolVal() {
        return numericSymbolVal;
    }

    /**
     * Sets the actual color from the given value
     *
     * @param col Numeric color value
     */
    public void setColor(int col) {
        numericColorVal = col;
        System.out.println("Set color to " + col);

        switch (col) {
            case RED:
                color = Color.RED;
                break;
            case BLUE:
                color = Color.BLUE;
                break;
            case GREEN:
                color = Color.GREEN;
                break;
            case YELLOW:
                color = Color.YELLOW;
                break;
            case GRAY:
                color = Color.GRAY;
                break;
            case CYAN:
                color = Color.CYAN;
                break;
        }

        isEmpty = false;
    }

    /**
     * Sets the symbol of the tile
     *
     * @param sym Numeric value of the symbol of the tile
     */
    public void setSymbol(int sym) {
        numericSymbolVal = sym;
        System.out.println("Set symbol to " + sym);
        switch (sym) {
            case ASTERISK:
                symbol = "*";
                break;
            case AMPER:
                symbol = "&";
                break;
            case DOLLAR:
                symbol = "$";
                break;
            case HASH:
                symbol = "#";
                break;
            case RATE:
                symbol = "@";
                break;
            case PERCENT:
                symbol = "%";
                break;
        }
    }

    /**
     * Indicates the tile as empty
     */
    public void makeTileEmpty() {
        isEmpty = true;
    }

}
