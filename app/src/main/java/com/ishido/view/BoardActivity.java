/************************************************************
 * Name:  Sujil Maharjan                                    *
 * Project:  Project 1/Ishodo Game			               *
 * Class:  Artificial Intelligence/CMP 331                  *
 * Date:  2/5/2016			                               *
 ************************************************************/
package com.ishido.view;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.ishido.model.Board;
import com.ishido.model.Deck;
import com.ishido.model.FileAccess;
import com.ishido.model.Player;
import com.ishido.model.TableCoordinates;
import com.ishido.model.TileInfo;
import com.ishido.model.TileTree;

import org.problets.helloworld.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.Vector;

/**
 * This class holds the view of the Ishido game. It allows the user to place the tiles on the board, computes the score and tiles.
 */

public class BoardActivity extends Activity implements Cloneable {
    // Holds the constants of search types and color
    private final String MESSAGE = "score";
    private final int DEFAULT_COLOR = Color.parseColor("#FCEBB6");
    public static final int DFS = 0;
    public static final int BFS = 1;
    public static final int BEST_FS = 2;
    public static final int BRANCH_AND_BOUND = 3;

    // Initializes the board, deck, player objects
    private Board board = new Board();
    private Deck deck = new Deck();
    private Player player = new Player();

    // View of the Player
    private TextView playerView;

    // Consists the stock of tiles got from the file
    private LinkedList<Integer> stockQueue = new LinkedList<Integer>();

    // FileAccess and animation declarations
    private FileAccess fileAccess;
    private TextView currentBox = null;
    private Animation anim;

    // Declares the searchType
    private int searchType;

    // Holds the value of the working tile
    private TileInfo currentTile = new TileInfo();

    // Queue for the BFS
    Queue<TileTree> searchTree = new LinkedList<TileTree>();
    private boolean needNewTile = true;

    TileTree previousTileTree = new TileTree();

    // For DFS
    Vector<TileTree> visitedTiles = new Vector<TileTree>();
    Stack<TileTree> searchStack = new Stack<TileTree>();
    //private TileTree poppedTileTree = null;

    // For best first search
    Vector<TileTree> visitedBestFirst = new Vector<TileTree>();
    ArrayList<TileTree> sortedSearchArray = new ArrayList<TileTree>();

    // Primarily sets values for branch and bound search
    private int totalBranch = 0;
    Stack<TileTree> goalStack = new Stack<TileTree>();
    TileTree goalNode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        // Initializes the file access according to this context
        fileAccess = new FileAccess(getApplicationContext());

        // Reads the given file and stores the strings of board data, stock and score
        fileAccess.readData(R.raw.data);

        // Fills the board initially with the data retrieved from the file
        board.fillBoard(fileAccess.getBoardData(), deck);
        // Makes the table
        makeTable();

        // Updates the score in Player and also presents the score in the screen
        player.addScore(Integer.parseInt(fileAccess.getScore()));
        TextView score = (TextView) findViewById(R.id.playerScore);
        score.setText("" + player.getScore());

        // Sets the stcok primarily
        setTheStock(fileAccess.getStock());

        // Fill in the initial spinner for search options choice
        List<String> choices = new ArrayList<String>();
        choices.add("Depth First Search");
        choices.add("Breadth First Search");
        choices.add("Best Fit Search");
        choices.add("Branch and Bound Search");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, choices);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = (Spinner) findViewById(R.id.searchChoice);
        sItems.setAdapter(adapter);

        Button nextButton = (Button) findViewById(R.id.next);
        nextButton.setEnabled(false);

        // Helps blink the tile
        anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(50); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);

        // Assing the text view of player
        playerView = (TextView) findViewById(R.id.playerScore);

        // Displays the row numbers of the board from this table
        TableLayout table = (TableLayout) findViewById(R.id.rowIndexing);

        // Adds the grid in the android activity
        // It uses the TableLayout to create the overall table of the board
        for (int rowIndex = 0; rowIndex < 9; ++rowIndex) {
            TableRow row = new TableRow(this);

            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(4, 4, 4, 4);

            TextView rowView = new TextView(this);
            rowView.setText(rowIndex + "");
            rowView.setWidth(50);
            rowView.setHeight(35);
            rowView.setGravity(Gravity.CENTER);

            row.addView(rowView, params);
            table.addView(row);
        }

        // Displays the column numbers of the board
        table = (TableLayout) findViewById(R.id.columnIndexing);

        TableRow row = new TableRow(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(4, 4, 4, 4);

        for (int col = 0; col < 12; col++) {
            TextView colView = new TextView(this);
            colView.setText(col + 1 + "");
            colView.setWidth(50);
            colView.setHeight(35);
            colView.setGravity(Gravity.CENTER);
            row.addView(colView, params);
        }
        table.addView(row);


		/*
        // Makes a spinner for choosing the color
		List<String> colorChoices = new ArrayList<String>();
		colorChoices.add("Red");
		colorChoices.add("Blue");
		colorChoices.add("Green");
		colorChoices.add("Yellow");
		colorChoices.add("Gray");
		colorChoices.add("Cyan");

		ArrayAdapter<String> colorAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, colorChoices);

		colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner colorChooser = (Spinner) findViewById(R.id.colorChoices);
		colorChooser.setAdapter(colorAdapter);

		// Makes the spinner for choosing the symbols
		List<String> symbolChoices = new ArrayList<String>();
		symbolChoices.add("*");
		symbolChoices.add("&");
		symbolChoices.add("$");
		symbolChoices.add("#");
		symbolChoices.add("@");
		symbolChoices.add("%");

		ArrayAdapter<String> symbolAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, symbolChoices);

		symbolAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner symbolChooser = (Spinner) findViewById(R.id.symbolChoices);
		symbolChooser.setAdapter(symbolAdapter);

		*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_board, menu);
        return true;
    }

    /**
     * Sets the stock
     *
     * @param stock The string got from the file that defines the stock
     */
    public void setTheStock(String stock) {
        // Splits the string with spacebar and then adds it in the queue as integers
        String[] stockStr = stock.split(" ");

        while (!stockQueue.isEmpty()) {
            stockQueue.remove();
        }
        for (int index = 0; index < stockStr.length; index++) {
            // Adds the number into the queue
            stockQueue.add(Integer.parseInt(stockStr[index]));
        }
    }

    /**
     * Makes the table in the board
     */
    public void makeTable() {
        // Finds the table that we will be working with
        TableLayout table = (TableLayout) findViewById(R.id.givenGrid);

        // Adds the grid in the android activity
        // It uses the TableLayout to create the overall table of the board
        for (int rowIndex = 0; rowIndex < 8; ++rowIndex) {
            TableRow row = new TableRow(this);

            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(4, 4, 4, 4);
            for (int columnIndex = 0; columnIndex < 12; ++columnIndex) {
                // Specifies each cell of the table with TextView
                TextView columns = new TextView(this);
                columns.setWidth(50);
                columns.setHeight(35);
                columns.setTextSize(20);

                TileInfo boardTile = board.getTile(rowIndex, columnIndex);
                if (boardTile == null) {
                    columns.setText("");
                    columns.setBackgroundColor(DEFAULT_COLOR);
                } else {
                    columns.setText(boardTile.getSymbol());
                    columns.setBackgroundColor(boardTile.getColor());
                }
                columns.setGravity(Gravity.CENTER);

                // Sets the tag for each cell view so that we can retrieve the row and column number clicked
                TableCoordinates tableCoordinates = new TableCoordinates(rowIndex, columnIndex);
                columns.setTag(tableCoordinates);

                //columns.setOnClickListener(calculatePosition);
                row.addView(columns, params);
            }
            table.addView(row);
        }
    }

    /**
     * Handles the button press that determines what type of search the user wants to do
     *
     * @param view
     */
    public void searchHandle(View view) {
        // Finds out the selected type of search from the field
        Spinner spinner = (Spinner) findViewById(R.id.searchChoice);
        int selectedIndex = spinner.getSelectedItemPosition();

        // Check if the next button is already enabled. If it is, it means that the app did not open the first time.
        // So, clear all of the existing values as we are starting a new search
        Button nextButton = (Button) findViewById(R.id.next);
        if (nextButton.isEnabled()) {
            // Refreshes the different components of the app (board, tile, deck)
            refreshSearchTable();
            setTheStock(fileAccess.getStock());
        }

        // Clear everything for the first time use
        searchTree.clear();
        searchStack.clear();
        sortedSearchArray.clear();
        visitedBestFirst.clear();
        visitedTiles.clear();

        // Sets the search type as selected by the user
        searchType = selectedIndex;

        // Sets that we need a new tile (defines the beginning of certain kind of search)
        needNewTile = true;

        // Checks if the search type is branch and bound. If YES, then we need additional information from the user. So, don't set the next button as clickable yet.
        // If NO, set the next button as enabled.
        if (searchType == BRANCH_AND_BOUND) {
            // Make the input label visible
            LinearLayout branchLayout = (LinearLayout) findViewById(R.id.branchLayout);
            branchLayout.setVisibility(View.VISIBLE);

        } else {
            nextButton.setEnabled(true);
            nextButton.setClickable(true);
        }
    }

    /**
     * Sets the next button to enabled given all the criterias are fulfilled
     *
     * @param view It is the view of the app
     */
    public void enableNextForBNB(View view) {
        // Gets the view of the buttons and values of the branch that user provided
        Button nextButton = (Button) findViewById(R.id.next);
        EditText branchValue = (EditText) findViewById(R.id.branchInput);
        CheckBox searchWholeTree = (CheckBox) findViewById(R.id.wholeTreeSearch);

        // Checks if the user entered the branch value. If YES, then parse it into the integer and initialize stock accordingly.
        if (!branchValue.getText().toString().isEmpty()) {
            // Get the total branch entered by the user
            totalBranch = Integer.parseInt(branchValue.getText().toString());

            // Handle the stockQueue number according to the the given number
            LinkedList<Integer> tempStock = new LinkedList<Integer>();
            for (int index = 0; index < totalBranch; ++index) {
                tempStock.add(stockQueue.remove());
            }

            stockQueue = tempStock;
        } else if (searchWholeTree.isChecked()) {
            // At this point, the stock is kept as it is. Hence, it will search through the whole tree
            System.out.println("Checkbox marked");
        } else {
            // Do nothing. Just return
            Toast.makeText(getApplicationContext(), "Please select one of the choices", Toast.LENGTH_SHORT).show();
            return;
        }

        // If the above condition go through, then, hide the additional options
        LinearLayout branchLayout = (LinearLayout) findViewById(R.id.branchLayout);
        branchLayout.setVisibility(View.INVISIBLE);

        // Performs the branch and bound according to the given choices
        performBranchAndBound();

        // After the branch and bound is performed, then it enables the next button
        nextButton.setEnabled(true);
        nextButton.setClickable(true);
    }

    /**
     * It refreshes the search table. So, everytime we start a new search, the table, board, deck, score is cleared and restored
     */
    public void refreshSearchTable() {
        // Removes from the table view
        TableLayout table = (TableLayout) findViewById(R.id.givenGrid);
        table.removeAllViews();

        // Resets the board and deck
        board = new Board();
        deck = new Deck();

        // Initializes to the original set
        board.fillBoard(fileAccess.getBoardData(), deck);
        makeTable();
        player = new Player();
        player.addScore(Integer.parseInt(fileAccess.getScore()));
        TextView score = (TextView) findViewById(R.id.playerScore);
        score.setText("" + player.getScore());
    }

    /**
     * Initiates the game according to the search type selected on next button clicked
     *
     * @param view
     */
    public void initiateGame(View view) {
        if (searchType == BRANCH_AND_BOUND) {
            getNextBNBVal();
        } else if (stockQueue.isEmpty()) {
            Toast.makeText(getApplicationContext(), "The game is over!", Toast.LENGTH_SHORT).show();
            return;
        } else if (searchType == DFS) {
            performDFS();
        } else if (searchType == BFS) {
            performBFS();
        } else if (searchType == BEST_FS) {
            performBestFS();
        }


    }

    /**
     * Gets the next value from the goal queue we already generated for Branch and Bound
     */
    public void getNextBNBVal() {
        // Checks if the goalStack is empty. If YES, it means we are done with the branch and bound. If NO, then, pop the stack and display it to the user
        if (!goalStack.isEmpty()) {
            TileTree result = goalStack.pop();
            TableCoordinates coord = result.getCoordinates();
            TextView box = (TextView) findViewInTable(coord);
            box.setText(result.getTile().getSymbol());
            box.setBackgroundColor(result.getTile().getColor());


            box.startAnimation(anim);

            playerView = (TextView) findViewById(R.id.playerScore);
            playerView.setText(result.getTotalScore() + "");
        } else {
            Toast.makeText(getApplicationContext(), "GAME OVER!!!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Performs the branch and bound operation and populates the goalNode stack.
     * It performs the whole search at a time
     */
    public void performBranchAndBound() {
        // Initializes the final stage (leaf node stage) as false.
        boolean finalStage = false;

        // Checks if this is the first time the search has been initiated. If YES, get a new tile from the stock
        if (needNewTile) {
            int tileInteger = stockQueue.remove();
            currentTile = Board.calculateTile(tileInteger);

            // Initializes the goal node as null
            goalNode = new TileTree(null, null, 0, null);

            // Initializes the previous tile as null for the first run
            previousTileTree = null;
        }

        // Loop through the stock while it is the new tile or the queue for the tree is not empty
        while (!searchTree.isEmpty() || needNewTile == true) {
            // Check if this is a first run. If yes, just set the previous tile as NULL. Else, the current tile option would be previous tile.
            if (needNewTile == true) {
                previousTileTree = null;
            } else {
                // Remove a tile tree from the queue
                TileTree tempTree = searchTree.remove();

                // Checks if the new tile and previous tile we dealt were same. If YES, get a new tile to work with. Else, continue with the same tile
                if (previousTileTree == null || tempTree.getTile() != previousTileTree.getTile()) {
                    if (!stockQueue.isEmpty()) {
                        int tileInteger = stockQueue.remove();
                        currentTile = Board.calculateTile(tileInteger);

                        // Check if there is another tile in the stock. If not, specify that this newly generated tile is the final stage and all of the locations available from this are leaf nodes.
                        if (stockQueue.isEmpty()) {
                            finalStage = true;
                        }
                    }
                }

                // Sets the previous tile as the one popped from the queue
                previousTileTree = tempTree;

            }
            // Sets the new tile as false as it won't be first time anymore
            needNewTile = false;

            // Clear the board and the deck and populate again for previous tiles
            refreshSearchTable();
            TileTree preTile = previousTileTree;

            // Loops through the parent tiles of the generated tiletree and fills it in the board
            while (preTile != null) {
                board.fillTile(preTile.getCoordinates().getRow(), preTile.getCoordinates().getColumn(), preTile.getTile());
                deck.recordTile(preTile.getTile().getNumericColorVal(), preTile.getTile().getNumericSymbolVal());
                preTile = preTile.getMasterTileTree();
            }

            // Adds the available nodes to the list
            // Loops through all the board locations and assigns the available tile to TileTree with parent tile noted
            for (int rowIndex = 0; rowIndex < board.TOTAL_ROWS; rowIndex++) {
                for (int colIndex = 0; colIndex < board.TOTAL_COLUMNS; colIndex++) {
                    if (board.canFillTile(rowIndex, colIndex, currentTile)) {
                        int totalScore;
                        // Checks if previous tile tree is null. If not, then adds the total score for this tile tree from the one from previous tile
                        if (previousTileTree != null) {
                            totalScore = previousTileTree.getTotalScore() + board.calculateScore(rowIndex, colIndex, currentTile);
                        } else {
                            totalScore = player.getScore() + board.calculateScore(rowIndex, colIndex, currentTile);
                        }
                        TableCoordinates tileCoords = new TableCoordinates(rowIndex, colIndex);

                        // Checks if the tile we are working with is in final stage. If NO, then, just add it to the search tree queue
                        if (!finalStage) {
                            searchTree.add(new TileTree(currentTile, tileCoords, totalScore, previousTileTree));
                        } else {
                            // At this point, we are at the leaf node. So, check with the goal node. If the leaf node has higher score than the goal node, then it would now be the goal node
                            TileTree tempTree = new TileTree(currentTile, tileCoords, totalScore, previousTileTree);
                            // Here, we need to check the total score in this location with our goal node. If it is greater, declare this as the goal node
                            if (tempTree.getTotalScore() > goalNode.getTotalScore()) {
                                goalNode = tempTree;
                            }
                        }
                    }
                }
            }
        }

        // Check if the goal node is null. If NO, then, push it to the goalStack. From this we will get our main answer
        while (goalNode != null) {
            goalStack.push(goalNode);

            goalNode = goalNode.getMasterTileTree();
        }
    }

    /**
     * Performs the whole Depth First Search
     */
    public void performDFS() {
        // If the board is already done, the just return that the game is over
        if (board.isDone(deck)) {
            Toast.makeText(getApplicationContext(),"The game is over",Toast.LENGTH_SHORT).show();
            return;
        }

        // refresh the search table and the stock
        refreshSearchTable();
        int stockIndex = 0;

        // If this is the first time, then get the new tile
        if (needNewTile) {
            // First get the new tile from the stock
            int tileInteger = stockQueue.get(stockIndex);
            currentTile = Board.calculateTile(tileInteger);

            Toast.makeText(getApplicationContext(),"Setting up DFS...",Toast.LENGTH_SHORT).show();

            // Declare previous tile as null
            previousTileTree = null;
        }

        // If it is not the first time, then go to the stack and pop out the last element
        if (!needNewTile && !searchStack.isEmpty()) {
            // Initialize previousTileTree as the the one popped from the stack
            previousTileTree = searchStack.pop();

            // Loop through hierarchical parent of the previous tile and put it in the board
            TileTree temptree = previousTileTree;
            while(temptree != null) {
                TableCoordinates tempCoord = temptree.getCoordinates();

                // Increase the index of stock everytime
                stockIndex++;

                // Fill it in the board
                board.fillTile(tempCoord.getRow(), tempCoord.getColumn(), temptree.getTile());
                deck.recordTile(temptree.getTile().getNumericColorVal(), temptree.getTile().getNumericSymbolVal());

                TextView box = (TextView) findViewInTable(tempCoord);
                box.setText("" + temptree.getTile().getSymbol());
                box.setBackgroundColor(temptree.getTile().getColor());
                box.startAnimation(anim);

                temptree = temptree.getMasterTileTree();
            }

            playerView = (TextView) findViewById(R.id.playerScore);
            playerView.setText(previousTileTree.getTotalScore()+"");

            // Checks the stockIndex with the size of stock. If
            if (stockIndex<stockQueue.size()) {
                int tileInteger = stockQueue.get(stockIndex);
                currentTile = Board.calculateTile(tileInteger);
            }
            else {
                Toast.makeText(getApplicationContext(),"GAME OVER!",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else if (!needNewTile && searchStack.isEmpty()) {
            Toast.makeText(getApplicationContext(),"GAME OVER", Toast.LENGTH_SHORT).show();
        }


        // Perform location search
        for (int rowIndex=board.TOTAL_ROWS-1; rowIndex>=0; rowIndex--) {
            for (int colIndex=board.TOTAL_COLUMNS-1; colIndex >= 0; colIndex--) {
                if (board.canFillTile(rowIndex,colIndex,currentTile)) {
                    TableCoordinates tempCoord = new TableCoordinates(rowIndex,colIndex);
                    int totalScore=0;
                    // Checks if previous tile tree is null. If not, then adds the total score for this tile tree from the one from previous tile
                    if (previousTileTree != null) {
                        totalScore = previousTileTree.getTotalScore() + board.calculateScore(rowIndex, colIndex, currentTile);
                    } else {
                        totalScore = player.getScore() + board.calculateScore(rowIndex, colIndex, currentTile);
                    }
                    TileTree tempTree = new TileTree(currentTile, tempCoord, totalScore, previousTileTree );

                    // Check with visited arraylist
                    if (visitedTiles.contains(currentTile)) {
                        continue;
                    }

                    // If the tile is not in the visited tile, then save it to stack
                    searchStack.push(tempTree);
                }
            }
        }

        // Now make needNewTile as false as it won't be first run anymore
        needNewTile = false;


    }

    /**
     * Performs the best first search
     */
    public void performBestFS(){
        // If the board is already done, the just return that the game is over
        if (board.isDone(deck)) {
            Toast.makeText(getApplicationContext(),"The game is over",Toast.LENGTH_SHORT).show();
            return;
        }

        // refresh the search table and the stock
        refreshSearchTable();
        int stockIndex = 0;

        // If this is the first time, then get the new tile
        if (needNewTile) {
            // First get the new tile from the stock
            int tileInteger = stockQueue.get(stockIndex);
            currentTile = Board.calculateTile(tileInteger);

            Toast.makeText(getApplicationContext(),"Setting up best first search...",Toast.LENGTH_SHORT).show();

            // Declare previous tile as null
            previousTileTree = null;
        }

        // If it is not the first time, then go to the stack and pop out the last element
        if (!needNewTile && !sortedSearchArray.isEmpty()) {
            // Initialize previousTileTree as the the one popped from the stack
            previousTileTree = sortedSearchArray.remove(0);

            // Loop through hierarchical parent of the previous tile and put it in the board
            TileTree temptree = previousTileTree;
            while(temptree != null) {
                TableCoordinates tempCoord = temptree.getCoordinates();

                // Increase the index of stock everytime
                stockIndex++;

                // Fill it in the board
                board.fillTile(tempCoord.getRow(), tempCoord.getColumn(), temptree.getTile());
                deck.recordTile(temptree.getTile().getNumericColorVal(), temptree.getTile().getNumericSymbolVal());

                TextView box = (TextView) findViewInTable(tempCoord);
                box.setText("" + temptree.getTile().getSymbol());
                box.setBackgroundColor(temptree.getTile().getColor());

                if (stockIndex == 1) {
                    box.startAnimation(anim);
                }

                temptree = temptree.getMasterTileTree();
            }

            playerView = (TextView) findViewById(R.id.playerScore);
            playerView.setText(previousTileTree.getTotalScore()+"");

            // Checks the stockIndex with the size of stock. If
            if (stockIndex<stockQueue.size()) {
                int tileInteger = stockQueue.get(stockIndex);
                currentTile = Board.calculateTile(tileInteger);
            }
            else {
                Toast.makeText(getApplicationContext(),"GAME OVER!",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else if (!needNewTile && searchStack.isEmpty()) {
            Toast.makeText(getApplicationContext(),"GAME OVER", Toast.LENGTH_SHORT).show();
        }

        // Perform location search
        for (int rowIndex=0; rowIndex<board.TOTAL_ROWS; rowIndex++) {
            for (int colIndex=0; colIndex < board.TOTAL_COLUMNS; colIndex++) {
                if (board.canFillTile(rowIndex,colIndex,currentTile)) {
                    TableCoordinates tempCoord = new TableCoordinates(rowIndex,colIndex);
                    int totalScore=0;
                    // Checks if previous tile tree is null. If not, then adds the total score for this tile tree from the one from previous tile
                    if (previousTileTree != null) {
                        totalScore = previousTileTree.getTotalScore() + board.calculateScore(rowIndex, colIndex, currentTile);
                    } else {
                        totalScore = player.getScore() + board.calculateScore(rowIndex, colIndex, currentTile);
                    }
                    TileTree tempTree = new TileTree(currentTile, tempCoord, totalScore, previousTileTree );

                    // Check with visited arraylist
                    if (visitedBestFirst.contains(currentTile)) {
                        continue;
                    }

                    // If the tile is not in the visited tile, then save it to stack
                    sortedSearchArray.add(tempTree);
                }
            }
        }

        // Sort the given searchArray
        Collections.sort(sortedSearchArray, new Comparator<TileTree>() {
            @Override
            public int compare(TileTree lhs, TileTree rhs) {
                return rhs.getTotalScore()-lhs.getTotalScore();
            }
        });

        needNewTile = false;

    }

    /**
     * This will perform Breadth First Search
     */
    public void performBFS() {
        // If the board is already done, the just return that the game is over
        if (board.isDone(deck)) {
            Toast.makeText(getApplicationContext(),"The game is over",Toast.LENGTH_SHORT).show();
            return;
        }

        // refresh the search table and the stock
        refreshSearchTable();
        int stockIndex = 0;

        // If this is the first time, then get the new tile
        if (needNewTile) {
            // First get the new tile from the stock
            int tileInteger = stockQueue.get(stockIndex);
            currentTile = Board.calculateTile(tileInteger);

            Toast.makeText(getApplicationContext(),"Setting up best first search...",Toast.LENGTH_SHORT).show();

            // Declare previous tile as null
            previousTileTree = null;
        }

        // If it is not the first time, then go to the stack and pop out the last element
        if (!needNewTile && !searchTree.isEmpty()) {
            // Initialize previousTileTree as the the one popped from the stack
            previousTileTree = searchTree.remove();

            // Loop through hierarchical parent of the previous tile and put it in the board
            TileTree temptree = previousTileTree;
            while(temptree != null) {
                TableCoordinates tempCoord = temptree.getCoordinates();

                // Increase the index of stock everytime
                stockIndex++;

                // Fill it in the board
                board.fillTile(tempCoord.getRow(), tempCoord.getColumn(), temptree.getTile());
                deck.recordTile(temptree.getTile().getNumericColorVal(), temptree.getTile().getNumericSymbolVal());

                TextView box = (TextView) findViewInTable(tempCoord);
                box.setText("" + temptree.getTile().getSymbol());
                box.setBackgroundColor(temptree.getTile().getColor());

                //if (stockIndex == 1) {
                box.startAnimation(anim);
                //}

                temptree = temptree.getMasterTileTree();
            }

            playerView = (TextView) findViewById(R.id.playerScore);
            playerView.setText(previousTileTree.getTotalScore()+"");

            // Checks the stockIndex with the size of stock. If
            if (stockIndex<stockQueue.size()) {
                int tileInteger = stockQueue.get(stockIndex);
                currentTile = Board.calculateTile(tileInteger);
            }
            else {
                Toast.makeText(getApplicationContext(),"GAME OVER!",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else if (!needNewTile && searchStack.isEmpty()) {
            Toast.makeText(getApplicationContext(),"GAME OVER", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform location search
        for (int rowIndex=0; rowIndex<board.TOTAL_ROWS; rowIndex++) {
            for (int colIndex=0; colIndex < board.TOTAL_COLUMNS; colIndex++) {
                if (board.canFillTile(rowIndex,colIndex,currentTile)) {
                    TableCoordinates tempCoord = new TableCoordinates(rowIndex,colIndex);
                    int totalScore=0;

                    // Checks if previous tile tree is null. If not, then adds the total score for this tile tree from the one from previous tile
                    if (previousTileTree != null) {
                        totalScore = previousTileTree.getTotalScore() + board.calculateScore(rowIndex, colIndex, currentTile);
                    } else {
                        totalScore = player.getScore() + board.calculateScore(rowIndex, colIndex, currentTile);
                    }
                    TileTree tempTree = new TileTree(currentTile, tempCoord, totalScore, previousTileTree );

                    // If the tile is not in the visited tile, then save it to stack
                    searchTree.add(tempTree);
                }
            }
        }

        needNewTile = false;
    }


    /**
     * Finds the cell view in the TableLayout since it cannot be directly found
     *
     * @param inputCoordinates It is the given coordinates for the cell
     * @return
     */
    private TextView findViewInTable(TableCoordinates inputCoordinates) {
        TableLayout tableLayout = (TableLayout) findViewById(R.id.givenGrid);

        // Loops through each cell view and checks if it matches with the coordinates with the view tag
        for (int rows = 0; rows < tableLayout.getChildCount(); ++rows) {
            TableRow tableRow = (TableRow) tableLayout.getChildAt(rows);

            for (int col = 0; col < tableRow.getChildCount(); ++col) {
                TextView box = (TextView) tableRow.getChildAt(col);
                TableCoordinates boxCoordinates = (TableCoordinates) box.getTag();
                if (boxCoordinates.getRow() == inputCoordinates.getRow() && boxCoordinates.getColumn() == inputCoordinates.getColumn()) {
                    return box;
                }
            }
        }
        return null;

    }

    /**
     * Handles the onClickListener for the game board and performs the operation such as updating the table, player score, game deck
     */
	/*
	public View.OnClickListener calculatePosition = new View.OnClickListener() {
		public void onClick(View v) {
			// Retrieves the position (row/column numbers) for the clicked cell in the table
			TableCoordinates clickPosition = (TableCoordinates) v.getTag();

			// Checks if the tile is selected. If yes, it checks if the selected tile is available.
			if (!clickedTile.isTileEmpty()) {
				if (board.isTileAvailable(clickPosition.getRow(), clickPosition.getColumn())) {
					// Checks if the logic is true for the tile selected
					if (board.fillTile(clickPosition.getRow(), clickPosition.getColumn(), clickedTile) == true) {
						// Fill the box with symbol provided
						TextView box = (TextView) v;
						box.setText(clickedTile.getSymbol());
						box.setBackgroundColor(clickedTile.getColor());

						// Records the generated/selected tile in the deck so that there is more more than 2 repetitions of certain combination
						deck.recordTile(clickedTile.getNumericColorVal(), clickedTile.getNumericSymbolVal());

						// Generates and adds the score of the player
						player.addScore(board.calculateScore(clickPosition.getRow(), clickPosition.getColumn(), clickedTile));

						// Prints and updates the player score
						TextView playerScore = (TextView) findViewById(R.id.playerScore);
						playerScore.setText("" + player.getScore());

						// Refreshes the working tile
						clickedTile.makeTileEmpty();
						refreshChoices();

						// Checks if the game is over. If yes, the sends the user to GameOver activity.
						if (deck.isDone() || board.isDone(deck)) {
							Intent intent = new Intent(getApplicationContext(), GameOver.class);

							String totalScore = "Your total score is: " + player.getScore();
							intent.putExtra(MESSAGE,totalScore );
							startActivity(intent);
							finish();
						}


					} else {
						Toast.makeText(getApplicationContext(), "Illegal move", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(getApplicationContext(), "The position is already filled", Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(getApplicationContext(), "Please select the tile first", Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * Handles the selection of random/user choice spinner
	 *
	 * @param view It is the view of the item pressed
	 */
	/*
	public void calculateTile(View view) {
		// Gets the item selected from random/user choices
		Spinner spinner = (Spinner) findViewById(R.id.userChoices);
		String selectedValue = spinner.getSelectedItem().toString();

		// Finds the view for additional spinners provided so that we can change the visibility
		Spinner cChoices = (Spinner) findViewById(R.id.colorChoices);
		Spinner sChoices = (Spinner) findViewById(R.id.symbolChoices);
		Button generate = (Button) findViewById(R.id.generate);

		// Checks if random/user choice is selected. If random, it generates random tile from the deck
		if (selectedValue == "Random") {
			// Clear out the spinners for user's choice
			cChoices.setVisibility(View.INVISIBLE);
			sChoices.setVisibility(View.INVISIBLE);
			generate.setVisibility(View.INVISIBLE);

			// Generate the random tile
			TileInfo tile = new TileInfo();
			deck.generateTile(tile);
			clickedTile = tile;

			printResult();
		} else {
			refreshChoices();

			// Let the user pick the symbols and color
			cChoices.setVisibility(View.VISIBLE);
			sChoices.setVisibility(View.VISIBLE);
			generate.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Refreshes the TextView of the tile which shows the current tile that is selected to input in the game board
	 */
	/*
	public void refreshChoices() {
		TextView result = (TextView) findViewById(R.id.resultSymbol);
		result.setText("");
		result.setVisibility(View.INVISIBLE);
		clickedTile.makeTileEmpty();
	}

	/**
	 * Prints the tile that is randomly generated/selected by user
	 */
	/*
	public void printResult() {
		TextView result = (TextView) findViewById(R.id.resultSymbol);
		result.setBackgroundColor(clickedTile.getColor());
		result.setWidth(60);
		result.setHeight(60);
		result.setGravity(Gravity.CENTER);
		result.setText(clickedTile.getSymbol());
		result.setVisibility(View.VISIBLE);
	}

	/**
	 * It handles the tile selection by the user. It is the custom selection
	 *
	 * @param view
	 */
	/*
	public void recordUserInput(View view) {
		// Gets the values that user selected for color and symbol
		Spinner colorSpinner = (Spinner) findViewById(R.id.colorChoices);
		int selectedColor = colorSpinner.getSelectedItemPosition();

		Spinner symbolSpinner = (Spinner) findViewById(R.id.symbolChoices);
		int selectedSymbol = symbolSpinner.getSelectedItemPosition();

		// Checks if user selected tile is available in the deck or it already maxed out its capacity. If yes, it then selects the tile.
		if (deck.verifyTile(selectedColor, selectedSymbol)) {
			TileInfo tile = new TileInfo();
			tile.setSymbol(selectedSymbol);
			tile.setColor(selectedColor);
			clickedTile = tile;
			printResult();
		} else {
			Toast.makeText(getApplicationContext(), "The board maxed out the given combination", Toast.LENGTH_SHORT).show();
		}
	}
	*/
}
