# Ishido Game
## Activities
1.	StartPageActivity:
This activity is the start page of the application. It will display the user that this is the “Ishido Game” and let the player start the game.
![1](https://cloud.githubusercontent.com/assets/8810802/12800916/6caedf70-caa6-11e5-9e48-8d078ef349fc.png)
2.	BoardActivity:
This activity handles the main game. It displays the main board in which the user can play and reports lets the user choose the tiles and report the scores. This activity communicates with the logic of the game so that it can display accordingly. For example,
![2](https://cloud.githubusercontent.com/assets/8810802/12800915/6cae9696-caa6-11e5-84d6-931b4eebbc0b.png)
The image above displays the main board and the choices that the user have (Random/User’s choice). Then,
![3](https://cloud.githubusercontent.com/assets/8810802/12800914/6cad7040-caa6-11e5-84fb-407f0eea3def.png)
This image shows the tile the user selected. When the user places the tile on the board, it looks like:
![4](https://cloud.githubusercontent.com/assets/8810802/12800917/6caff40a-caa6-11e5-83e0-f40a9ff8ffb9.png)
3.	GameOver:
This activity shows the user that the game is over and displays the score of the user. It lets them restart the game or exit the application.
![5](https://cloud.githubusercontent.com/assets/8810802/12800918/6cb209fc-caa6-11e5-87db-fa45ab64b243.png)
## AI:
AI algorithm is used after every tile placed by user. It is used to determine if the user has tiles left to place, but the board does not allow the placements due to Ishido rules. Once the user strikes such situation, the game is directed towards GameOver activity.
The method that uses this algorithm is checkTileAvailability() which is under isDone() in Board class.

## BUG REPORT:
I couldn’t figure out the org.problets.helloworld.R problem. I tried changing the package name, but it then gave me errors in activity classes. So, I just kept the prior since it removed all the errors.

## FEATURES:
###	Implemented:
1.	Users get to view their tile before they place it on the board.
2.	Users can regenerate the tiles randomly as many times as they want.
3.	All of the errors that user do during the game will be notified through a short message at the bottom of the page.
4.	Displays the score after the game is over.
5.	Provides the option of restarting the game if the user wants to play again.
6.	Option to exit the game when it is over.

###	Not implemented:
1.	A go back button during the game. It only works with the default go-back button of android.
2.	The pages are not graphically astonishing.
