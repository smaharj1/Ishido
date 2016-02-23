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
    // Initializes the row and column for the class
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

    /**
     * Sets the row of the TableCoordinates
     * @param r It consists of the row number
     */
    public void setRow(int r) {
        row = r;
    }

    /**
     * Sets the column of the table coordinates
     * @param col It consists of the column number of the coordinates
     */
    public void setColumn(int col) {
        column = col;
    }


}
