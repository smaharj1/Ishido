package com.ishido.model;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Tsujil on 2/12/2016.
 */
public class FileAccess {
    private final String LAYOUT_START = "Layout";
    private final String STOCK_START = "Stock";
    private final String SCORE_START = "Score";
    private Context currentContext;
    private String []boardData = new String[8];
    private String stock;
    private String score;

    public FileAccess(Context context) {
        currentContext = context;
    }

    private String readFromFile( int file) throws IOException {
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
        return stringBuilder.toString();
    }

    public String readData(int file) {
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
        return result;
    }

    public String[] getBoardData () {
        return boardData;
    }

    public String getStock() {
        return stock;
    }

    public String getScore() {
        return score;
    }
}
