package com.ishido.model;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class deals with opening and parsing the files. It then parses into appropriate variables for ease.
 * Created by Tsujil on 2/12/2016.
 */
public class FileAccess {
    // Declares the constants for strings to consider while parsing the file
    private final String LAYOUT_START = "Layout";
    private final String STOCK_START = "Stock";
    private final String SCORE_START = "Score";

    // Declares the current context
    private Context currentContext;

    // Declares the string array of board that will hold the information on the board
    private String []boardData = new String[8];

    // Declares the stock and score
    private String stock;
    private String score;

    /**
     * Constructor of FileAccess class that will set the context of the fileAccess
     * @param context It consists of the context of the android app
     */
    public FileAccess(Context context) {
        currentContext = context;
    }

    /**
     * It reads everything from the file into a string
     * @param file It is the file that needs to be opened
     * @return Returns the string of the content of the file
     * @throws IOException
     */
    private String readFromFile( int file) throws IOException {
        // Read the string from the file into the input stream and builds it into the string
        InputStream inputStream = currentContext.getResources().openRawResource(file);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(inputStreamReader);

        // do reading, usually loop until end of file reading
        StringBuilder stringBuilder = new StringBuilder();
        String mLine = reader.readLine();
        while (mLine != null) {
            stringBuilder.append('\n');
            stringBuilder.append(mLine); // process line
            mLine = reader.readLine();
        }
        reader.close();

        // Convert it into the string and return
        return stringBuilder.toString();
    }

    /**
     * It reads the data and splits into the appropriate holdings (boardData, stock, score)
     * @param file It is the file that needs to be read
     */
    public void readData(int file) {
        String result = "";
        try {
            // Reads the file from the actual file and then converts it into the string
            result= readFromFile(file);

            // Splits the given string into an array corresponding to lines
            String [] lineSplit = result.split("\n");

            // Loops through each line and puts the data value accordingly
            for (int index=0; index < lineSplit.length; ++index) {
                // If the line contains "layout:", put the eight other lines into a loop and populate the Board string
                if (lineSplit[index].contains(LAYOUT_START)) {
                    index++;

                    // Loops through each row of the board
                    for (int boardIndex=0; boardIndex < 8 ; ++boardIndex) {
                        boardData[boardIndex] = lineSplit[index];
                        ++index;
                    }
                }
                else if (lineSplit[index].contains(STOCK_START)) {
                    index++;

                    stock = lineSplit[index];
                }
                else if (lineSplit[index].contains(SCORE_START)) {
                    index++;
                    score =lineSplit[index];
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Gets the string array of the board data
     * @return Returns the string of board data
     */
    public String[] getBoardData () {
        return boardData;
    }

    /**
     * Returns the stock
     * @return Returns the stock
     */
    public String getStock() {
        return stock;
    }

    /**
     * REturns the score
     * @return Returns the score
     */
    public String getScore() {
        return score;
    }
}
