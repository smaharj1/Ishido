package com.ishido.model;

/**
 * Created by Tsujil on 2/15/2016.
 */
public class TileTree {
    private TileInfo tile;
    private TableCoordinates coordinates;
    private TileTree previousElement;

    public TileTree(TileInfo tileInfo, TileTree previousTile, TableCoordinates tableCoordinates) {
        tile = tileInfo;
        coordinates = tableCoordinates;
        previousElement = previousTile;
    }

    public TileTree() {
        tile = null;
        coordinates = null;
        previousElement = null;
    }

    public TileInfo getTile() {
        return tile;
    }

    public TableCoordinates getCoordinates() {
        return coordinates;
    }

    public TileTree getMasterTileTree() {
        return previousElement;
    }

    public boolean isEmpty() {
        return tile == null;
    }

}
