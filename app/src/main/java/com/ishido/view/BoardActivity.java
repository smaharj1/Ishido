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
	private final String MESSAGE="score";
	private final int DEFAULT_COLOR = Color.parseColor("#FCEBB6");
	public static final int DFS= 0;
	public static final int BFS = 1;

	private Board board = new Board();
	private Deck deck = new Deck();
	//private TileInfo clickedTile = new TileInfo();
	private Player player = new Player();
	private LinkedList<Integer> stockQueue = new LinkedList<Integer>();
	// private SearchModel searchModel;
	private FileAccess fileAccess;
	private TextView currentBox = null;
	private Animation anim;
	private int searchType;

	private TileInfo currentTile = new TileInfo();

	// Queue for the BFS
	Queue<TileTree> searchTree = new LinkedList<TileTree>();
	private boolean needNewTile = true;
	TableCoordinates startingLocation = new TableCoordinates(0,0);
	TileTree previousInQueue = new TileTree();

	// For DFS
	Vector<TileTree> visitedTiles = new Vector<TileTree>();
	Stack<TileTree> dfsStack = new Stack<TileTree>();
	private TileTree poppedTileTree = null;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_board);

		fileAccess = new FileAccess(getApplicationContext());

		// Reads the given file and stores the strings of board data, stock and score
		fileAccess.readData(R.raw.data);

		// Fills the board initially with the data retrieved from the file
		board.fillBoard(fileAccess.getBoardData(), deck);
		makeTable();

		// Updates the score in Player and also presents the score in the screen
		player.addScore(Integer.parseInt(fileAccess.getScore()));
		TextView score = (TextView) findViewById(R.id.playerScore);
		score.setText("" + player.getScore());

		// Initiates the searchModel after populating the board and deck for the first time
		//searchModel = new SearchModel(board, deck);

		//searchModel.setStockQueue(fileAccess.getStock());
		setTheStock(fileAccess.getStock());

		// Fill in the initial spinner for search options choice
		List<String> choices = new ArrayList<String>();
		choices.add("Depth First Search");
		choices.add("Breadth First Search");
		choices.add("Best Fit Search");

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


	public void setTheStock(String stock) {
		String[] stockStr = stock.split(" ");

		while (!stockQueue.isEmpty()) {
			stockQueue.remove();
		}
		for (int index = 0; index < stockStr.length; index++) {
			// Adds the number into the queue
			stockQueue.add(Integer.parseInt(stockStr[index]));
		}
	}
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

		Button nextButton = (Button) findViewById(R.id.next);
		if (nextButton.isEnabled()) {
			refreshSearchTable();
			setTheStock(fileAccess.getStock());
		}

		searchType = selectedIndex;
		needNewTile = true;

		//searchModel.setSearchType(selectedIndex);
		nextButton.setEnabled(true);
		nextButton.setClickable(true);
	}

	public void refreshSearchTable() {
		TableLayout table = (TableLayout) findViewById(R.id.givenGrid);
		table.removeAllViews();
		board = new Board();
		deck = new Deck();
		//searchModel = new SearchModel(board,deck);
		//setTheStock(fileAccess.getStock());
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
		if (stockQueue.isEmpty()) {
			Toast.makeText(getApplicationContext(),"The game is over!" , Toast.LENGTH_SHORT).show();
			return;
		}
		if (searchType == DFS) {
			performDFS();
		}
		else if (searchType == BFS) {
			performBFS();
		}

	}

	public void performBFS() {
		// This is when we first take the tile from the queue
		if (needNewTile) {
			// First get the new tile from the stock
			//if (!stockQueue.isEmpty()) {
			int tileInteger = stockQueue.remove();
			currentTile = Board.calculateTile(tileInteger);

			needNewTile = false;
			//}
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
					//System.out.println("The total score player has right now is "+player.getScore());

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
			//System.out.println("The total score player has right now is " + player.getScore());

			// draw
			TextView box = findViewInTable(new TableCoordinates(elementCoord.getRow(),elementCoord.getColumn()));
			box.setText(elementInfo.getSymbol());
			box.setBackgroundColor(elementInfo.getColor());

			// Check if the current tile we traversed is same as the one removed from the queue. If yes, then generate new tile. Else, stick with currentTile
			if (!previousInQueue.isEmpty()) {
				if (previousInQueue.getTile() == elementInfo) {
					needNewTile = false;
				}
				else {
					// we need a new tile to play with
					needNewTile = true;
				}
			}
			else needNewTile = true;

			// Saves the newly removed tile from the queue for parenting hierarchy with new nodes
			previousInQueue = firstElement;

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

					TileTree tileTree = new TileTree(currentTile,previousInQueue, new TableCoordinates(row,col));

					// Push it into the queue
					searchTree.add(tileTree);

					// Generates and adds the score of the player
					player.addScore(board.calculateScore(row, col, currentTile));

					// Prints and updates the player score
					TextView playerScore = (TextView) findViewById(R.id.playerScore);
					playerScore.setText("" + player.getScore());

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

	private void drawTile(int row, int col, TileInfo tileInfo) {

		TextView box = findViewInTable(new TableCoordinates(row,col));
		if (box != null) {
			box.setText(tileInfo.getSymbol());
			box.setBackgroundColor(tileInfo.getColor());
			//System.out.println("ole inside yea");

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
		boolean firstTimer = false;
		if (needNewTile ) {
			// First get the new tile from the stock
			int tileInteger = stockQueue.remove();
			currentTile = Board.calculateTile(tileInteger);

			firstTimer = true;

			needNewTile = false;
		}

		if (!dfsStack.isEmpty() || firstTimer) {
			// If first timer, just push it to the stack and be done!

			TableCoordinates availableLocation = board.findNextAvailableLocation(0, 0, currentTile);
			// Since this is the first tile, it is never going to be NULL
			if (availableLocation != null) {
				TileTree newTileTree = new TileTree(currentTile, null, availableLocation);

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
						poppedTileTree = dfsStack.pop();
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
					newTileTree = new TileTree(currentTile,null,availableLocation);
				}

				// Push it to the stack of DFS
				dfsStack.push(newTileTree);
				visitedTiles.add(newTileTree);

				// Put it in the board
				board.fillTile(availableLocation.getRow(), availableLocation.getColumn(), currentTile);
				deck.recordTile(currentTile.getNumericColorVal(), currentTile.getNumericSymbolVal());

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
				poppedTileTree = dfsStack.pop();
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
			}


		}

	}


	/*
	private void performDFS() {
		int tileInteger = stockQueue.remove();

		TileInfo tile = Board.calculateTile(tileInteger);

		// Enters the tile in the board and deck according to search preferred and returns the table coordinates where the tile was placed
		// Returns null if the tile could not be placed anywhere

		for (int row=0; row < Board.TOTAL_ROWS; ++row ) {
			for (int col = 0; col < Board.TOTAL_COLUMNS; ++col) {
				if (board.canFillTile(row, col, tile)) {
					board.fillTile(row,col,tile);
					deck.recordTile(tile.getNumericColorVal(),tile.getNumericSymbolVal());

					// Generates and adds the score of the player
					player.addScore(board.calculateScore(row, col, tile));

					// Prints and updates the player score
					TextView playerScore = (TextView) findViewById(R.id.playerScore);
					playerScore.setText("" + player.getScore());


					// Draw the table accordingly
					TextView box = findViewInTable(new TableCoordinates(row,col));
					if (box != null) {
						box.setText(tile.getSymbol());
						box.setBackgroundColor(tile.getColor());

						if (currentBox != null) {
							currentBox.clearAnimation();
						}
						currentBox = box;
						currentBox.startAnimation(anim);
					}
					return;
				}
			}
		}
	}*/

	private TextView findViewInTable(TableCoordinates inputCoordinates) {
		TableLayout tableLayout = (TableLayout) findViewById(R.id.givenGrid);

		for (int rows=0; rows<tableLayout.getChildCount(); ++rows) {
			TableRow tableRow = (TableRow) tableLayout.getChildAt(rows);

			for (int col = 0; col < tableRow.getChildCount(); ++col) {
				TextView box = (TextView) tableRow.getChildAt(col);
				TableCoordinates boxCoordinates = (TableCoordinates) box.getTag();
				if (boxCoordinates.getRow() == inputCoordinates.getRow() && boxCoordinates.getColumn() == inputCoordinates.getColumn()) {
					return box;
				}
				//Toast.makeText(getApplicationContext(), "" + box, Toast.LENGTH_SHORT).show();
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
