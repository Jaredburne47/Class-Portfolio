package cpsc2150.extendedCheckers.models;

import cpsc2150.extendedCheckers.util.DirectionEnum;
import cpsc2150.extendedCheckers.views.CheckersFE;

import java.util.*;
/**
 * The CheckerBoardMem class stores information about the state of the game such as the current state of the board and the
 * number of pieces each player has remaining. The class contains functions to interact with the game (ex: moving a
 * players piece from one location to another) as well as to analyze the game state (ex: checking
 * if a player has won / checking the state of a specific spot on the board)
 * Is an implementation of ICheckerBoard interface
 *
 * @invariant board = [A map containing all BoardPositions where player's pieces are]
 *  0 <= i < rowNum and 0 <= j < rowNum.
 *
 * @invariant pieceCount.get(Character) = [a proper character] AND
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
public class CheckerBoardMem extends AbsCheckerBoard {
    /**
     * Map with a Character key and a List of values of BoardPositions. Represents the board by holding all locations of
     * each players pieces
     */
    private Map<Character, List<BoardPosition>> board;

    /**
     * Hashmap with Character key and Integer vals, used to map amount of pieces to each player
     */
    private HashMap<Character, Integer> pieceCount;

    /**
     * Hashmap, with a Character key and an Arraylist of DirectionEnums. Used to map the directions a piece is able to
     * move to its respective player
     */
    private HashMap<Character, ArrayList<DirectionEnum>> viableDirections;

    /**
     * Int variable representing number of rows on CheckerBoard
     */
    private int rowNum;

    /**
     * Int variable representing number of columns on CheckerBoard
     */
    private int colNum;

    /**
     * Int variable representing number of starting pieces each player has
     */
    private int startingCount;

    /** Constructs instance of CheckerBoardMem object initializing board, pieceCount, and viableDirections with
     * the proper values to represent the state of game before the first turn.
     *
     * @param aDimension the size of the CheckerBoardMem that determines number of starting pieces, and spaces on the board
     * @pre aDimension = [an even number between the min and max board sizes]
     * @post pieceCount.get(playerOne) = startingCount AND pieceCount.get(playerTwo) = startingCount
     * viableDirections.get(playerOne) = [DirectionEnum.SE, DirectionEnum.SW] AND viableDirections.get(playerTwo) = [DirectionEnum.NE, DirectionEnum.NW]
     * board = [populated with startingCount number of pieces for each player beginning in the top left corner for player one
     * and bottom right corner for player two.Every space adjacent to a piece or empty space is a BLACK_TILE.
     * Spaces not containing playerOne, playerTwo or BLACK_TILE are EMPTY_POS]
     * CheckerBoard = [new object with variables following previous conventions]
     */
    public CheckerBoardMem(int aDimension) {

        // Update the dimensions of the board
        rowNum = aDimension;
        colNum = aDimension;
        // Adjust the starting count based on the new board size
        startingCount = ((aDimension / 2) - 1) * (aDimension / 2);

        char playerOne = CheckersFE.getPlayerOne();
        char playerTwo = CheckersFE.getPlayerTwo();

        board = new HashMap<>();
        pieceCount = new HashMap<>();

        pieceCount.put(playerOne, startingCount);
        pieceCount.put(playerTwo, startingCount);

        viableDirections = new HashMap<>();

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


        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < colNum; j++) {
                if ((i + j) % 2 == 0) {
                    char player = EMPTY_POS;
                    if (i < (rowNum / 2 -1)) {
                        player = playerOne;
                    } else if (i >= (rowNum /2 +1)) {
                        player = playerTwo;
                    }
                    if (player != EMPTY_POS) {
                        board.putIfAbsent(player, new ArrayList<>());
                        board.get(player).add(new BoardPosition(i, j));
                    }
                }
            }
        }
        board.put(Character.toUpperCase(playerOne), new ArrayList<>());
        board.put(Character.toUpperCase(playerTwo), new ArrayList<>());
    }

    public HashMap<Character, ArrayList<DirectionEnum>> getViableDirections() {
        return viableDirections;
    }

    public HashMap<Character, Integer> getPieceCounts() {
        return pieceCount;
    }

    public void placePiece(BoardPosition pos, char player) {
        // removes any value previously in this position
        for (char key : board.keySet()){
            board.get(key).remove(pos);
        }
        // adds position to map if it is a game piece (not a EMPTY_POS or BLACK_TILE)
        if (board.containsKey(player)){
            board.get(player).add(pos);
        }
    }
    public char whatsAtPos(BoardPosition pos) {
        if ((pos.getRow() % 2 == 0 && pos.getColumn() % 2 == 1) || (pos.getRow() % 2 == 1 && pos.getColumn() % 2 == 0)){
            return BLACK_TILE;
        }
        for (Map.Entry<Character, List<BoardPosition>> entry : board.entrySet()) {
            if (entry.getValue().contains(pos)) {
                return entry.getKey();
            }
        }
        return EMPTY_POS;
    }
    public int getRowNum() {
        return rowNum;
    }

    public int getColNum() {
        return colNum;
    }
}
