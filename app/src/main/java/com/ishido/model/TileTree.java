package com.ishido.model;

/**
 * This class holds the information of a single tile. It holds the information like what is the attributes of tile, the coordinates of tile
 * in the board, its hierarchical parent tile and the total score that would be if we place this tile on the board hierarchically.
 * Created by Tsujil on 2/15/2016.
 */
public class TileTree{
    // Declares the information of the tile
    private TileInfo tile;

    // Declares the table coordinates in which the tile belong to
    private TableCoordinates coordinates;

    // Declares the parent TileTree of this tile
    private TileTree previousElement;

    // Declares the total score until this tile
    private int totalScore;

    /**
     * Initializes the values of the tile, coordinates, parent tile, and the total score
     * @param tileInfo It consists of the tile information (TileInfo object)
     * @param tableCoordinates It consists of the TableCoordinates object
     * @param score It consists of the total score until this tile
     * @param previousTile It consists of the parent tile
     */
    public TileTree(TileInfo tileInfo, TableCoordinates tableCoordinates, int score, TileTree previousTile ) {
        tile = tileInfo;
        coordinates = tableCoordinates;
        previousElement = previousTile;
        totalScore = score;
    }

    /**
     * It initializes the new TileTree to consist of null values
     */
    public TileTree() {
        tile = null;
        coordinates = null;
        previousElement = null;
    }

    /**
     * Returns the TileInfo object (color/symbol) of the tile
     * @return Returns the tile
     */
    public TileInfo getTile() {
        return tile;
    }

    /**
     * Returns the coordinates of the tile
     * @return Returns the coordinates of the current tile
     */
    public TableCoordinates getCoordinates() {
        return coordinates;
    }

    /**
     * Returns the parent TileTree that resulted in this tile
     * @return Returns the parent TIleTree
     */
    public TileTree getMasterTileTree() {
        return previousElement;
    }

    /**
     * Checks if the TileTree is empty
     * @return Returns if the tiletree is empty or not
     */
    public boolean isEmpty() {
        return tile == null;
    }

    /**
     * Returns the total score until this tile
     * @return Returns the total score
     */
    public int getTotalScore() {
        return totalScore;
    }


}
