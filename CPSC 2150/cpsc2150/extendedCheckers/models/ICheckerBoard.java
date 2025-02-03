
package cpsc2150.extendedCheckers.models;

import cpsc2150.extendedCheckers.util.DirectionEnum;
import cpsc2150.extendedCheckers.views.CheckersFE;

import java.util.ArrayList;
import java.util.HashMap;

/** The ICheckerBoard interface represents the checkerboard and all of the operations that can be performed on a checkerboard
 * as well as checking its current state
 *
 * @defines game_board: with rows and columns that represents the game board and each element is space on the board
 * map_of_pieces: A mapping of the players and the pieces they have on the board
 * moveable_directions: A mapping of the players and the directions their pieces can move
 *
 * @constraints count_of_pieces <= [The initial amount of pieces the board should have]
 * game_board[0 <= row <= getRowNum()][0 <= column <= getColNum()]
 * moveable_directions = [SE & SW for playerOne] AND [NE & NW for playerTwo] AND [NE, NW, SE, SW for kinged pieces]
 *
 * @initilization_ensures The game_board is initialized with rows and columns and with the map_of_pieces
 * accurately reflecting the starting number of pieces for the players
 * The moveable_directions are set for both players
 */
public interface ICheckerBoard {

    public static final char EMPTY_POS = ' ';
    public static final char BLACK_TILE = '*';

    /** Accesses HashMap of moveable_directions
     *
     * @return hashmap of directions a piece can move in
     * @pre none
     * @post
     * moveable_directions = #moveable_directions AND game_board = #game_board AND map_of_pieces = #map_of_pieces
     * AND getViableDirections = moveable_directions
     */
    HashMap<Character, ArrayList<DirectionEnum>> getViableDirections();
    /** Accesses map_of_piecs
     *
     * @return hashmap of pieces each player has left on the game board
     * @pre none
     * @post
     * map_of_pieces = #map_of_pieces AND moveable_directions = #moveable_directions AND game_board = #game_board
     * AND getPieceCounts = [a HashMap containing the piece counts for each player]
     */
    HashMap<Character, Integer> getPieceCounts();
    /** gets number of rows on the game board
     *
     * @return number of rows on the game board
     * @pre none
     * @post
     * moveable_directions = #moveable_directions AND game_board = #game_board AND map_of_pieces = #map_of_pieces
     * AND getRowNum = rowNum
     */
     int getRowNum();

    /** gets number of columns on the game board
     *
     * @return number of columns on the game board
     * @pre none
     * @post moveable_directions = #moveable_directions AND game_board = #game_board AND map_of_pieces = #map_of_pieces
     * AND getColNum = colNum
     */
    int getColNum();

    /** places a player piece at a given position
     *
     * @param pos the location on the game board being changed
     * @param player value being placed on the board location
     * @pre
     * 0 <= pos.getRow() <= getRowNum() AND 0 <= pos.getColumn <= getColNum()
     * [player is a char that is currently being used by one of the players in the game]
     * @post
     * placePiece = [game_board unchanged except game_board[pos.getRow()][pos.getColumn()] = player]
     * AND game_board = [#game_board where a piece for the specified player has been placed at the specified position on the board]
     * AND moveable_directions = #moveable_directions AND map_of_pieces = #map_of_pieces
     */
    void placePiece(BoardPosition pos, char player);
    /** Accesses value at given position on game board
     *
     * @param pos the Boardposition position that is being accessed
     * @return character of piece at given BoardPosition
     * @pre 0 <= pos.getRow() <= rowNum AND 0 <= pos.getColumn() <= colNum
     * @post
     * game_board = #game_board AND moveable_directions = #vmoveable_directions AND map_of_pieces = #map_of_pieces
     * AND whatsAtPos = [value in given position]
     */
    char whatsAtPos(BoardPosition pos);

    /** Creates a BoardPosition to represent the values for a given direction
     *
     * @param dir directionEnum that represents the direction of the move
     * @return BoardPosition that represents the row and column offsets based on direction where the
     * positive axes will correlate to south for columns and east for rows
     * @pre dir is in DirectionEnum
     * @post moveable_directions = #moveable_directions AND game_board = #game_board AND map_of_pieces = #map_of_pieces
     * AND getDirection = [game board position representing the direction's values]
     */
    static BoardPosition getDirection(DirectionEnum dir) {
        if (dir == DirectionEnum.NE) {
            return new BoardPosition(-1, 1);
        } else if (dir == DirectionEnum.NW) {
            return new BoardPosition(-1, -1);
        } else if (dir == DirectionEnum.SE){
            return new BoardPosition(1, 1);
        } else {
            return new BoardPosition(1, -1);
        }
    }
    /** Converts piece at given position to a king
     *
     * @param posOfPlayer a Boardposition of where the player is on the board
     * @pre 0 <= posOfPlayer.getRow() <= rowNum AND 0 <= posOfPlayer.getColumn() <= colNum
     * AND whatsAtPos(posOfPlayer) = getPlayerOne() || getPlayerTwo()
     * @post game_board = [piece at given location on board becomes uppercase] AND
     * moveable_directions = #moveable_directions and map_of_pieces = #map_of_pieces
     */
    default void crownPiece(BoardPosition posOfPlayer) {
        //makes player piece upper case to signal that its a king piece
        placePiece(posOfPlayer, Character.toUpperCase(whatsAtPos(posOfPlayer)));
    }

    /** Checks if a given player has won the game
     *
     * @param player a Character player being checked for win
     * @return boolean if the player has won the game
     * @pre player = getPlayerOne() || getPlayerTwo()
     * @post
     * moveable_directions = #moveable_directions AND game_board = #game_board AND map_of_pieces = #map_of_pieces
     * AND checkPlayerWin = [true IFF opponents piece count = 0 OW checkPlayerWin = false]
     */
    default boolean checkPlayerWin(Character player){

        char other_player;
        if(player == CheckersFE.getPlayerOne()){
            other_player = CheckersFE.getPlayerTwo();
        } else {
            other_player = CheckersFE.getPlayerOne();
        }

        //returns whether or not there are any player pieces left to check for win condition for both players
        return getPieceCounts().get(other_player)  == 0;
    }

    /** Moves piece at given position in given direction and returns its new location
     *
     * @param startingPos Boardposition position of piece being moved
     * @param dir directionEnum direction to move piece
     * @return Boardposition position piece was moved to
     * @pre [startingPos is a valid position on the board]
     * AND [dir is a valid direction for the piece to move in]
     * AND relocation = [has EMPTY_POS before move]
     * AND relocation = [is inside the confines of the board]
     * @post whatsAtPos(startingPos) = EMPTY_POS and game_board = [piece being moved is now located two spot further in the given direction and old location now had EMPTY_POS]
     * movePiece = [new location of piece]
     * moveable_directions = #moveable_directions AND map_of_pieces = #map_of_pieces
     */
    default BoardPosition movePiece(BoardPosition startingPos, DirectionEnum dir){

        //Gets a new position based off of starting position and specified direction
        BoardPosition relocation = BoardPosition.add(startingPos, getDirection(dir));

        //Moves the piece by adding it to new position and removing from old position
        placePiece(relocation, whatsAtPos(startingPos));
        placePiece(startingPos, EMPTY_POS);

        //returns new position
        return relocation;
    }
    /** Adds the viable directions for players to be able to move their pieces on the board
     *
     * @param player character representing a player's piece
     * @param dir direction piece can move, represented by a DirectionEnum
     * @pre player = 'X' || player = 'O' || player = 'x' || player = 'o' AND dir != null
     * @post  game_board = #game_board AND map_of_pieces = #map_of_pieces
     * moveable_directions = [NE & NW IF getPlayerTwo(), SE & SW IF getPlayerOne(), AND NE & NW & SE & SW IF king piece]
     */
    default void addViableDirections(char player, DirectionEnum dir) {
        // Get the list of viable directions for player
        ArrayList<DirectionEnum> playerViableDirs = getViableDirections().get(player);
        if (playerViableDirs == null) {
            playerViableDirs = new ArrayList<>();
            getViableDirections().put(player, playerViableDirs);
        }
        getViableDirections().get(player).add(dir);
    }
    /** Performs a jump over an opponents piece in the game and returns the new position of the jumper
     *
     * @param startingPos position of piece about to perform jump
     * @param dir direction piece is jumping in
     * @return position of piece after jumping
     * @pre [startingPos is a valid position on the board]
     * AND [dir is a valid direction for the piece to move in]
     * @post
     * whatsAtPos(startingPos) = EMPTY_POS
     * AND map_of_pieces = [opponent's piece count -= 1]
     * AND game_board = [location of piece jumped is now EMPTY_POS and jumper's position has been updated]
     * AND jumpPiece = [jumper's location after jump]
     * AND moveable_directions = #moveable_directions
     */
    default BoardPosition jumpPiece(BoardPosition startingPos, DirectionEnum dir) {
        // Determine the player character of the jumped piece by checking what is at the jumped position
        char jumped_player = Character.toLowerCase(whatsAtPos(BoardPosition.add(startingPos, getDirection(dir))));
        //removes piece count from player whose piece was jumped over
        playerLostPieces(1, jumped_player, getPieceCounts());
        // Move the jumping piece to the new location, which is one move in the specified direction
        BoardPosition temp_location = movePiece(startingPos, dir);
        // Move the piece a second time in the same direction to complete the jump
        return movePiece(temp_location, dir);
    }
    /** Removes a numPieces amount of tokens, mapped to the parameter player, from the pieceCounts HashMap
     *
     * @param numPieces number of pieces being removed
     * @param player who's pieces are being removed
     * @param pieceCounts Hashmap of pieces belonging to each player
     * @pre 0 <= numPieces <= [initial number of player's pieces on the board] AND numPieces <= pieceCounts.get(player)
     * AND player = getPlayerOne() || getPlayerTwo() || [kinged player]
     * @post moveable_directions = #moveable_directions AND game_board = game_board
     * AND map_of_pieces -= numPieces
     */
    default void playerLostPieces(int numPieces, char player, HashMap<Character, Integer> pieceCounts) {

        //Checks for existence of player on pieceCounts map
        if (Character.isUpperCase(player)) {
            player = Character.toLowerCase(player);
        }

        //Retrieves the current count of pieces for player
        int currentCount = pieceCounts.get(player);
        //Calculates the updated count by removing the number of pieces lost
        int updatedCount = currentCount - numPieces;
        //updates the piece count for player
        pieceCounts.put(player, updatedCount);
    }
    /** Checks what values are adjacent to the given BoardPosition
     *
     * @param startingPos location to be looked next to
     * @return Hashmap of values in each direction adjacent to startingPos
     * @pre 0 <= startingPos.getRow() <= getRowNum() AND 0 <= startingPos.getColumn() <= getColNum()
     * @post
     * movable_directions = #movable_irections and game_board = #game_board and map_of_pieces = #map_of_pieces
     * scanSurroundingPositions = [HashMap with key:value {direction: value at location one spot from startingPos in this direction unless off the board} with an entry for each direction in DirectionEnum]
     */
    default HashMap<DirectionEnum, Character> scanSurroundingPositions(BoardPosition startingPos) {

        HashMap<DirectionEnum, Character> surroundingPositions = new HashMap<>();

        for (DirectionEnum dir : DirectionEnum.values()) {
            BoardPosition adjacent_position = BoardPosition.add(startingPos, getDirection(dir));
            // only adds direction to map when on board
            if (!(0 > adjacent_position.getRow() || adjacent_position.getRow() >= getRowNum() || 0 > adjacent_position.getColumn() || adjacent_position.getColumn() >= getColNum())) {
                surroundingPositions.put(dir, whatsAtPos(adjacent_position));
            }
        }
        return surroundingPositions;
    }
}
