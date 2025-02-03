package cpsc2150.extendedCheckers.models;

import cpsc2150.extendedCheckers.util.DirectionEnum;
import cpsc2150.extendedCheckers.views.CheckersFE;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The CheckerBoard class stores information about the state of the game such as the current state of the board and the
 * number of pieces each player has remaining. The class contains functions to interact with the game (ex: moving a
 * players piece from one location to another) as well as to analyze the game state (ex: checking
 * if a player has won / checking the state of a specific spot on the board)
 * Is an implementation of ICheckerBoard interface
 *
 * @invariant board[i][j] = [{a valid player piece, ' ', '*'}]
 *  0 <= i < rowNum and 0 <= j < colNum.
 *
 * @invariant pieceCount.get(Character) = getPlayerOne() || getPlayerTwo() AND
 * 0 <= pieceCount.get(Integer) <= startingCount.
 *
 * @invariant viableDirections = {SE, SW} IF PLAYER_ONE;
 * {NE, NW} IF PLAYER_TWO;
 * {NE,NW,SE,SW} IF kinged
 *
 * @corresponds game_board = board
 * map_of_pieces = pieceCount
 * moveable_directions = viableDirections
 */
public class CheckerBoard extends AbsCheckerBoard {
    /**
     * A 2D array of characters used to represent our checkerboard.
     */
    private char[][] board;

    /**
     * A HashMap, with a Character key and an Integer value, that is used to map a player's char to the number of
     * tokens that player still has left on the board.
     */
    private HashMap<Character, Integer> pieceCount;

    /**
     * A HashMap, with a Character key and an ArrayList of DirectionEnums value, used to map a player (and its king
     * representation) to the directions that player can viably move in. A non-kinged (standard) piece can only move
     * in the diagonal directions away from its starting position. A kinged piece can move in the same directions the
     * standard piece can move in plus the opposite directions the standard piece can move in.
     */
    private HashMap<Character, ArrayList<DirectionEnum>> viableDirections;

    /**
     * Int variable representing number of rows in the CheckerBoard
     */
    private int rowNum;

    /**
     * Int variable representing number of columns in the CheckerBoard
     */
    private int colNum;

    /**
     * Int variable representing the number of pieces each player starts with
     */
    private int startingCount ;

    /**
     * Constructs instance of checkerboard object initializing board, pieceCount, and viableDirections with
     * the proper values to represent the state of game before the first turn.
     *
     * @param aDimension the size of the CheckerBoard that determines number of starting pieces, and spaces on the board
     * @pre aDimension = [an even number between the min and max board sizes]
     * @post pieceCount.get(playerOne) = startingCount AND pieceCount.get(playerTwo) = startingCount
     * viableDirections.get(playerOne) = [DirectionEnum.SE, DirectionEnum.SW] AND viableDirections.get(playerTwo) = [DirectionEnum.NE, DirectionEnum.NW]
     * board = [populated with startingCount number of pieces for each player beginning in the top left corner for player one
     * and bottom right corner for player two.Every space adjacent to a piece or empty space is a BLACK_TILE.
     * Spaces not containing playerOne, playerTwo or BLACK_TILE are EMPTY_POS]
     * CheckerBoard = [new object with variables following previous conventions]
     */
    public CheckerBoard(int aDimension) {

        rowNum = aDimension;
        colNum = aDimension;

        startingCount = (aDimension / 2 - 1) * (aDimension / 2);

        char playerOne = CheckersFE.getPlayerOne();
        char playerTwo = CheckersFE.getPlayerTwo();

        //Initializes the game board
        board = new char[rowNum][colNum];
        // Initialize the pieceCount HashMap
        pieceCount = new HashMap<>();
        pieceCount.put(playerOne, startingCount);
        pieceCount.put(playerTwo, startingCount);

        // Initialize the viableDirections HashMap
        viableDirections = new HashMap<>();


        // Add initial viable directions for both players
        addViableDirections(playerOne, DirectionEnum.SE);
        addViableDirections(playerOne, DirectionEnum.SW);
        addViableDirections(playerTwo, DirectionEnum.NE);
        addViableDirections(playerTwo, DirectionEnum.NW);
        addViableDirections(Character.toUpperCase(playerOne), DirectionEnum.NE);
        addViableDirections(Character.toUpperCase(playerOne), DirectionEnum.NW);
        addViableDirections(Character.toUpperCase(playerOne), DirectionEnum.SE);
        addViableDirections(Character.toUpperCase(playerOne), DirectionEnum.SW);
        addViableDirections(Character.toUpperCase(playerTwo), DirectionEnum.NE);
        addViableDirections(Character.toUpperCase(playerTwo), DirectionEnum.NW);
        addViableDirections(Character.toUpperCase(playerTwo), DirectionEnum.SE);
        addViableDirections(Character.toUpperCase(playerTwo), DirectionEnum.SW);


        //places the pieces on the board
        int middle = rowNum / 2;
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                if ((i + j) % 2 != 0) {
                    board[i][j] = BLACK_TILE;
                } else if (i < middle - 1) {
                    board[i][j] = playerOne;
                } else if (i > middle) {
                    board[i][j] = playerTwo;
                } else {
                    board[i][j] = EMPTY_POS;
                }
            }
        }
    }


    public HashMap<Character, ArrayList<DirectionEnum>> getViableDirections() {
        return this.viableDirections;
    }

    public HashMap<Character, Integer> getPieceCounts() {
        return this.pieceCount;
    }

    public void placePiece(BoardPosition pos, char player) {
        this.board[pos.getRow()][pos.getColumn()] = player;
    }

    public char whatsAtPos(BoardPosition pos) {
        return this.board[pos.getRow()][pos.getColumn()];
    }

    public int getRowNum() {
        return rowNum;
    }

    public int getColNum() {
        return colNum;
    }
}