/************************************************************
 * Name:  Sujil Maharjan                                    *
 * Project:  Project 1/Ishodo Game			               *
 * Class:  Artificial Intelligence/CMP 331                  *
 * Date:  2/5/2016			                               *
 ************************************************************/
package com.ishido.model;

/**
 * It holds the coordinates of the cell that the user pressed
 */
public class TableCoordinates {
    private int row;
    private int column;

    // It records the row and column number of the cell that the user pressed
    public TableCoordinates(int rowNumber, int columnNumber) {
        row = rowNumber;
        column = columnNumber;
    }

    /**
     * It returns the row number
     *
     * @return Returns the row number
     */
    public int getRow() {
        return row;
    }

    /**
     * It returns the column number
     *
     * @return Returns the column number
     */
    public int getColumn() {
        return column;
    }

    public void setRow(int r) {
        row = r;
    }

    public void setColumn(int col) {
        column = col;
    }


}
