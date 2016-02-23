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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.Vector;

/**
 * This class holds the view of the Ishido game. It allows the user to place the tiles on the board, computes the score and tiles.
 */

public class BoardActivity extends Activity implements Cloneable{
	// Holds the constants of search types and color
	private final String MESSAGE="score";
	private final int DEFAULT_COLOR = Color.parseColor("#FCEBB6");
	public static final int DFS= 0;
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
	TableCoordinates startingLocation = new TableCoordinates(0,0);
	TileTree previousTileTree = new TileTree();

	// For DFS
	Vector<TileTree> visitedTiles = new Vector<TileTree>();
	Stack<TileTree> searchStack = new Stack<TileTree>();
	private TileTree poppedTileTree = null;

	// For best first search
	ArrayList<Stack<TileTree>> scoreSortedArray = new ArrayList<Stack<TileTree>>();
	Stack<TileTree> path = new Stack<TileTree>();
	Stack<TileTree> openTileTrees = new Stack<TileTree>();
	private boolean goBacktrack = false;

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

			row.addView(rowView,params);
			table.addView(row);
		}

		// Displays the column numbers of the board
		table = (TableLayout) findViewById(R.id.columnIndexing);

		TableRow row = new TableRow(this);
		TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
		params.setMargins(4, 4, 4, 4);

		for (int col=0; col<12; col++) {
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
				}
				else {
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

		// Sets the search type as selected by the user
		searchType = selectedIndex;

		// Sets that we need a new tile (defines the beginning of certain kind of search)
		needNewTile = true;

		// Checks if the search type is branch and bound. If YES, then we need additional information from the user. So, don't set the next button as clickable yet.
		// If NO, set the next button as enabled.
		if(searchType == BRANCH_AND_BOUND) {
			// Make the input label visible
			LinearLayout branchLayout = (LinearLayout) findViewById(R.id.branchLayout);
			branchLayout.setVisibility(View.VISIBLE);

		}
		else {
			nextButton.setEnabled(true);
			nextButton.setClickable(true);
		}
	}

	/**
	 * Sets the next button to enabled given all the criterias are fulfilled
	 * @param view It is the view of the app
	 */
	public void enableNextForBNB(View view) {
		// Gets the view of the buttons and values of the branch that user provided
		Button nextButton = (Button) findViewById(R.id.next);
		EditText branchValue = (EditText) findViewById(R.id.branchInput);
		CheckBox searchWholeTree= (CheckBox) findViewById(R.id.wholeTreeSearch);

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
		}
		else if (searchWholeTree.isChecked()) {
			// At this point, the stock is kept as it is. Hence, it will search through the whole tree
			System.out.println("Checkbox marked");
		}
		else {
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
	 * @param view
	 */
	public void initiateGame(View view) {
		if (searchType == BRANCH_AND_BOUND) {
			getNextBNBVal();
		}
		else if (stockQueue.isEmpty()) {
			Toast.makeText(getApplicationContext(),"The game is over!" , Toast.LENGTH_SHORT).show();
			return;
		}
		else if (searchType == DFS) {
			performDFS();
		}
		else if (searchType == BFS) {
			performBFS();
		}
		else if (searchType == BEST_FS) {
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
			if (currentBox != null) {
				currentBox.clearAnimation();

			}
			currentBox = box;

			currentBox.startAnimation(anim);

			playerView = (TextView) findViewById(R.id.playerScore);
			playerView.setText(result.getTotalScore()+"");
		}
		else {
			Toast.makeText(getApplicationContext(),"GAME OVER!!!" , Toast.LENGTH_SHORT).show();
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
			goalNode = new TileTree(null,null,0,null);

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
				deck.recordTile(preTile.getTile().getNumericColorVal(),preTile.getTile().getNumericSymbolVal());
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
	 * Performs the best first search
	 */
	public void performBestFS() {
		// Handles the first tile
		if (needNewTile) {
			int tileInteger = stockQueue.remove();
			currentTile = Board.calculateTile(tileInteger);

			needNewTile = false;
		}

		// First remove from scoreSortedArray since we are going to deal with completely new tile
		scoreSortedArray.clear();

		// Initialize the scoreSortedArray and make it ready for new run
		for (int index=0; index <5;++index) {
			scoreSortedArray.add(new Stack<TileTree>());
		}

		// Sets the back tracking to false
		goBacktrack = false;

		// This is to go through all the available locations from the beginning
		// Only needed when we need to calculate locations for the new tile
		for (int rowIndex = 0; rowIndex<board.TOTAL_ROWS; rowIndex++) {
			for (int colIndex = 0; colIndex <board.TOTAL_COLUMNS; colIndex++) {
				if (board.canFillTile(rowIndex, colIndex, currentTile)) {
					// Gets the score and puts it in the arraylist according to its score
					int score = board.calculateScore(rowIndex,colIndex,currentTile);
					TableCoordinates currentCoordinates = new TableCoordinates(rowIndex,colIndex);
					scoreSortedArray.get(score).push(new TileTree(currentTile, currentCoordinates, 0, null));
				}
			}
		}

		// If the locations are available, then add then to open list. OR take another tile from the open list
		for (int tempIndex =0; tempIndex<5; ++tempIndex) {
			if (!scoreSortedArray.get(tempIndex).isEmpty()) {
				goBacktrack = false;
				break;
			}

			// At this point, we are sure that we could not find any new location for the new tile
			if (tempIndex == 4) {
				int tileInt = deck.getNumericTileVal(currentTile);
				stockQueue.addFirst(tileInt);

				// Pop out from the path since that path don't give us optimal end state
				TileTree tempTree = path.pop();
				TableCoordinates tempTableCoord = tempTree.getCoordinates();

				currentTile = tempTree.getTile();

				// Clear the values from display screen and board
				board.removeTile(tempTableCoord.getRow(), tempTableCoord.getColumn());
				deck.removeFromDeck(tempTree.getTile().getNumericColorVal(), tempTree.getTile().getNumericSymbolVal());

				TextView box = findViewInTable(tempTableCoord);
				box.setText("");
				box.setBackgroundColor(DEFAULT_COLOR);
				box.clearAnimation();

				player.removeScore(board.calculateScore(tempTableCoord.getRow(),tempTableCoord.getColumn(),tempTree.getTile()));
				playerView.setText(""+player.getScore());

				goBacktrack = true;
			}
		}

		// Now push the newly generated tile from scoreSorted Array to the main Stack (open list). This will be put in score-sorted format directly
		if (!goBacktrack) {
			for (int scoreIndex = 0; scoreIndex < 5; ++scoreIndex) {
				//put the tiles from stack until the stack at this index is empty
				while (!scoreSortedArray.get(scoreIndex).isEmpty()) {
					openTileTrees.push(scoreSortedArray.get(scoreIndex).pop());
				}
			}

		}

		// Push the best node from openTileTrees to the actual PATH and print it on the board
		TileTree tempTileTree = openTileTrees.pop();
		TableCoordinates tempCoordinates = tempTileTree.getCoordinates();

		// The tiles can be different at this time. If it is, the pop again from the path and store previous one
		if (goBacktrack && tempTileTree.getTile() != path.peek().getTile()) {
			// Pop from the path again
			TileTree poppedTileTree = path.pop();
			TableCoordinates tempTableCoord = poppedTileTree.getCoordinates();

			int tileInt = deck.getNumericTileVal(currentTile);
			stockQueue.addFirst(tileInt);

			// Clear the values from display screen and board
			board.removeTile(tempTableCoord.getRow(), tempTableCoord.getColumn());
			deck.removeFromDeck(poppedTileTree.getTile().getNumericColorVal(), poppedTileTree.getTile().getNumericSymbolVal());

			TextView box = findViewInTable(tempTableCoord);
			box.setText("");
			box.setBackgroundColor(DEFAULT_COLOR);
			box.clearAnimation();



		}
		path.push(tempTileTree);

		// Fill it into the board and deck of the model
		board.fillTile(tempCoordinates.getRow(),tempCoordinates.getColumn(),tempTileTree.getTile());
		deck.recordTile(tempTileTree.getTile().getNumericColorVal(), tempTileTree.getTile().getNumericSymbolVal());

		// Get the score
		player.addScore(board.calculateScore(tempCoordinates.getRow(), tempCoordinates.getColumn(), tempTileTree.getTile()));

		TextView box = (TextView) findViewInTable(tempTileTree.getCoordinates());
		box.setText(tempTileTree.getTile().getSymbol());
		box.setBackgroundColor(tempTileTree.getTile().getColor());

		if (currentBox != null) {
			currentBox.clearAnimation();

		}
		currentBox = box;

		currentBox.startAnimation(anim);

		// Prints and updates the player score
		playerView.setText("" + player.getScore());


		// Get the next tile from the stock and put it in the currentTile
		if (!stockQueue.isEmpty()) {
			int tileInteger = stockQueue.remove();
			currentTile = Board.calculateTile(tileInteger);
		}
	}

	/**
	 * This will perform Breadth First Search
	 */
	public void performBFS() {
		// This is when we first take the tile from the queue
		if (needNewTile) {
			// First get the new tile from the stock
			int tileInteger = stockQueue.remove();
			currentTile = Board.calculateTile(tileInteger);

			needNewTile = false;
		}

		// Since we need to set a new tile once previous tile is done, check it with startingCoordinates
		if (startingLocation.getRow() == -1 ){
			// At this point, we need to access the queue for first element (remove from queue)
			TileTree firstElement = searchTree.remove();
			// The removed tile should be fixed to the given position
			TableCoordinates elementCoord = firstElement.getCoordinates();
			TileInfo elementInfo = firstElement.getTile();

			refreshSearchTable();

			// Check if the existing number has masters. IF yes, then it means print the masters before printing this tile
			if (!firstElement.getMasterTileTree().isEmpty())  {
				// Print all of the masters. Then print the new one
				TileTree master = firstElement.getMasterTileTree();
				while (!master.isEmpty()) {
					// If master is not empty, print the master first and then go through printing the childs
					board.fillTile(master.getCoordinates().getRow(), master.getCoordinates().getColumn(), master.getTile());
					deck.recordTile(master.getTile().getNumericColorVal(), master.getTile().getNumericSymbolVal());

					// Compute the players score for the tiles that are going to be consistent through out the whole period
					player.addScore((board.calculateScore(master.getCoordinates().getRow(), master.getCoordinates().getColumn(), master.getTile())));

					// Prints and updates the player score
					TextView playerScore = (TextView) findViewById(R.id.playerScore);
					playerScore.setText("" + player.getScore());

					// Displays it in the board
					TextView tempView  = findViewInTable(new TableCoordinates(master.getCoordinates().getRow(),master.getCoordinates().getColumn()));
					tempView.setText(master.getTile().getSymbol());
					tempView.setBackgroundColor(master.getTile().getColor());

					master = master.getMasterTileTree();
				}
			}

			board.fillTile(elementCoord.getRow(), elementCoord.getColumn(), elementInfo);
			deck.recordTile(elementInfo.getNumericColorVal(), elementInfo.getNumericSymbolVal());

			// Compute the players score for the tiles that are going to be consistent through out the whole period
			player.addScore((board.calculateScore(elementCoord.getRow(), elementCoord.getColumn(), elementInfo)));

			// Prints and updates the player score
			TextView playerScore = (TextView) findViewById(R.id.playerScore);
			playerScore.setText("" + player.getScore());

			// draw it in the board
			TextView box = findViewInTable(new TableCoordinates(elementCoord.getRow(),elementCoord.getColumn()));
			box.setText(elementInfo.getSymbol());
			box.setBackgroundColor(elementInfo.getColor());

			// Check if the current tile we traversed is same as the one removed from the queue. If yes, then generate new tile. Else, stick with currentTile
			if (!previousTileTree.isEmpty()) {
				if (previousTileTree.getTile() == elementInfo) {
					needNewTile = false;
				}
				else {
					// we need a new tile to play with
					needNewTile = true;
				}
			}
			else needNewTile = true;

			// Saves the newly removed tile from the queue for parenting hierarchy with new nodes
			previousTileTree = firstElement;

			// Check if we need a new element is needed
			if (needNewTile) {
				if (!stockQueue.isEmpty()) {
					int tileInteger = stockQueue.remove();
					currentTile = Board.calculateTile(tileInteger);

					needNewTile = false;
				}

			}

			// Then startingLocations should be set from beginning for the new tile
			startingLocation.setColumn(0);
			startingLocation.setRow(0);
		}



		// Then find the available coordinates for each. Once found, put it in the queue as a TileTree object
		for (int row = startingLocation.getRow(); row < Board.TOTAL_ROWS; ++row) {
			for (int col = startingLocation.getColumn(); col <Board.TOTAL_COLUMNS; ++col) {
				if (board.canFillTile(row, col, currentTile)) {
					// Create a new TileTree with that tile and coordinates and point its previous tile to the previous tiletree

					TileTree tileTree = new TileTree(currentTile, new TableCoordinates(row,col),0, previousTileTree);

					// Push it into the queue
					searchTree.add(tileTree);

					// Generates and adds the score of the player
					player.addScore(board.calculateScore(row, col, currentTile));

					// Prints and updates the player score
					playerView.setText("" + player.getScore());

					// Remove the score again since we just need it for display
					player.removeScore(board.calculateScore(row, col, currentTile));

					// Make sure same cell in the table is not visited next
					if (col <11) {
						startingLocation.setColumn(col+1);
						startingLocation.setRow(row);
					}
					else if (row <7){
						startingLocation.setRow(row+1);
						startingLocation.setColumn(0);
					}
					else {
						startingLocation.setRow(-1);
						startingLocation.setColumn(-1);
					}

					// Now draw that tile on the table
					drawTile(row,col,currentTile);

					return;
				}

			}
			// The column needs to be resetted every single time we reach the end of the solution
			startingLocation.setColumn(0);
		}

		// At this point, it is certain that none of the tiles are available.
		// So, set startingLocation to -1
		startingLocation.setRow(-1);
		startingLocation.setColumn(-1);

	}

	/**
	 * Draws the given tile on the baord
	 * @param row It is the row number in the board
	 * @param col It is the column number in the board
	 * @param tileInfo It is the information of the tile
	 */
	private void drawTile(int row, int col, TileInfo tileInfo) {
		// Finds the view in the table of the cell and then changes its properties according to choice
		TextView box = findViewInTable(new TableCoordinates(row,col));
		if (box != null) {
			box.setText(tileInfo.getSymbol());
			box.setBackgroundColor(tileInfo.getColor());

			if (currentBox != null) {
				currentBox.clearAnimation();

				// If we are dealing with the same tile, then remove all the functions from the currentBox to default
				currentBox.setText("");
				currentBox.setBackgroundColor(DEFAULT_COLOR);
			}
			currentBox = box;

			currentBox.startAnimation(anim);

		}
	}
	/**
	 * Performs the whole Depth First Search
	 */

	public void performDFS() {
		// Sets if this is the first time as false
		boolean firstTimer = false;
		if (needNewTile ) {
			// First get the new tile from the stock
			int tileInteger = stockQueue.remove();
			currentTile = Board.calculateTile(tileInteger);

			// Initializes that this is the first run
			firstTimer = true;

			needNewTile = false;
		}

		// If the search stack is not empty, then performs the depth first search
		if (!searchStack.isEmpty() || firstTimer) {
			TableCoordinates availableLocation = board.findNextAvailableLocation(0, 0, currentTile);


			if (availableLocation != null) {
				TileTree newTileTree = new TileTree(currentTile, availableLocation,0,null);

				// Checks if the visited tile already contains the new tile tree. This is done for back tracking
				while (visitedTiles.contains(newTileTree)) {
					int row = newTileTree.getCoordinates().getRow();
					int col = newTileTree.getCoordinates().getColumn();
					if (col <11) {
						col++;
					}
					else if (row <7){
						row++;
					}
					else {
						poppedTileTree = searchStack.pop();
						TableCoordinates poppedCoordinates = poppedTileTree.getCoordinates();

						// Put back the current tile we have back to the stock queue (at the front)
						stockQueue.addFirst(deck.getNumericTileVal(currentTile));

						// Set the current tile to be the popped one since we have to work on that first
						currentTile = poppedTileTree.getTile();

						board.removeTile(poppedCoordinates.getRow(), poppedCoordinates.getColumn());
						deck.removeFromDeck(poppedTileTree.getTile().getNumericColorVal(), poppedTileTree.getTile().getNumericSymbolVal());

						TextView box = findViewInTable(availableLocation);
						if (box != null) {
							box.setText("");
							box.setBackgroundColor(DEFAULT_COLOR);
						}
						break;
					}
					availableLocation = board.findNextAvailableLocation(row, col, currentTile);
					newTileTree = new TileTree(currentTile,availableLocation,0,null);
				}

				// Push it to the stack of DFS
				searchStack.push(newTileTree);
				visitedTiles.add(newTileTree);

				// Put it in the board
				board.fillTile(availableLocation.getRow(), availableLocation.getColumn(), currentTile);
				deck.recordTile(currentTile.getNumericColorVal(), currentTile.getNumericSymbolVal());

				// Print the score
				player.addScore(board.calculateScore(availableLocation.getRow(), availableLocation.getColumn(), currentTile));
				// Prints and updates the player score
				TextView playerScore = (TextView) findViewById(R.id.playerScore);
				playerScore.setText("" + player.getScore());

				// print it in the board
				// Draw the table accordingly
				TextView box = findViewInTable(availableLocation);
				if (box != null) {
					box.setText(currentTile.getSymbol());
					box.setBackgroundColor(currentTile.getColor());

					if (currentBox != null) {
						currentBox.clearAnimation();
					}
					currentBox = box;
					currentBox.startAnimation(anim);
				}

				// Get a new tile after we are done placing this tile for next run
				if (!stockQueue.isEmpty()) {
					int tileInteger = stockQueue.remove();
					currentTile = Board.calculateTile(tileInteger);
				} else {
					System.out.println("Game is done since all the tiles are placed");
				}

				return;


			}
			else {
				// Here, we have a tile but no location to put it. So, backtrack.
				// Pop the last one from the stack and remove it from board and screen
				poppedTileTree = searchStack.pop();
				TableCoordinates poppedCoordinates = poppedTileTree.getCoordinates();

				// Put back the current tile we have back to the stock queue (at the front)
				stockQueue.addFirst(deck.getNumericTileVal(currentTile));

				// Set the current tile to be the popped one since we have to work on that first
				currentTile = poppedTileTree.getTile();

				board.removeTile(poppedCoordinates.getRow(), poppedCoordinates.getColumn());
				deck.removeFromDeck(poppedTileTree.getTile().getNumericColorVal(), poppedTileTree.getTile().getNumericSymbolVal());

				TextView box = findViewInTable(poppedCoordinates);
				if (box != null) {
					box.setText("");
					box.setBackgroundColor(DEFAULT_COLOR);
					box.clearAnimation();
				}
			}
		}
	}

	/**
	 * Finds the cell view in the TableLayout since it cannot be directly found
	 * @param inputCoordinates It is the given coordinates for the cell
	 * @return
	 */
	private TextView findViewInTable(TableCoordinates inputCoordinates) {
		TableLayout tableLayout = (TableLayout) findViewById(R.id.givenGrid);

		// Loops through each cell view and checks if it matches with the coordinates with the view tag
		for (int rows=0; rows<tableLayout.getChildCount(); ++rows) {
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
