package cpsc2150.extendedCheckers.views;

import cpsc2150.extendedCheckers.models.BoardPosition;
import cpsc2150.extendedCheckers.models.CheckerBoard;
import cpsc2150.extendedCheckers.models.CheckerBoardMem;
import cpsc2150.extendedCheckers.models.ICheckerBoard;
import cpsc2150.extendedCheckers.util.DirectionEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * This class acts as the user interface for the game
 * It initializes the game and uses the CheckerBoard,BoardPosition, and DirectionEnum classes
 * to run,represent, and manage the game. It takes in user input to interact with the game logic
 * and display the current state of the game, including the board and player turns
 *
 * @invariant scanner != null AND scanner = [collects input from the user throughout the class]
 * checkerBoard != null AND checkerBoard = instanceof(ICheckerBoard)
 * direction_conversion_map  != null AND
 * direction_conversion_map = [contains mappings from string representations to DirectionEnum values.]
 */
public class CheckersFE {

    public static final int MIN_BOARD = 8;
    public static final int MAX_BOARD = 16;
    private static Scanner scanner = new Scanner(System.in);
    private static ICheckerBoard checkerBoard;

    private static HashMap<String, DirectionEnum> direction_conversion_map = new HashMap<String, DirectionEnum>();;

    private static char playerOne = 'x';

    private static char playerTwo = 'o';

    private static char choice = 'f';

    /** retrieves the piece that represents player one
     *
     * @return char of piece used by player one
     * @pre none
     * @post getPlayerOne = #playerOne
     */
    public static char getPlayerOne()
    {
        return playerOne;
    }

    /** retrieves the piece that represents player two
     *
     * @return char of piece used by player two
     * @pre none
     * @post getPlayerTwo = #playerTwo
     */
    public static char getPlayerTwo()
    {
        return playerTwo;
    }


    public static void main(String[] args) {

        System.out.println("Welcome to Checkers!");
        boolean playAgain;
        do {
            playAgain = false;

            // Game setup code
            System.out.println("Player 1, enter your piece:");
            char player1Piece = scanner.nextLine().charAt(0);
            while (player1Piece == ' ' || player1Piece == '*') {
                System.out.print("Invalid character. Choose a different character: ");
                player1Piece = scanner.nextLine().charAt(0);
            }
            playerOne = player1Piece;

            System.out.println("Player 2, enter your piece:");
            char player2Piece = scanner.nextLine().charAt(0);
            while (player2Piece == ' ' || player2Piece == '*' || player2Piece == player1Piece) {
                System.out.print("Invalid character. Choose a different character: ");
                player2Piece = scanner.nextLine().charAt(0);
            }
            playerTwo = player2Piece;

            System.out.println("Do you want a fast game (F/f) or a memory efficient game (M/m)?");
            choice = scanner.nextLine().charAt(0);
            while (choice != 'f' && choice != 'F' && choice != 'm' && choice != 'M') {
                System.out.println("Invalid choice. Enter 'F/f' for fast game or 'M/m' for memory efficient game:");
                choice = scanner.nextLine().charAt(0);
            }

            while (!(choice == 'f' || choice == 'F' || choice == 'm' || choice == 'M')) {
                System.out.println("Invalid Choice. Please pick again: ");
                choice = scanner.nextLine().charAt(0);
            }

            int boardSize = 0;
            do {
                System.out.println("How big should the board be? It can be 8x8, 10x10, 12x12, 14x14, or 16x16. Enter one number:");
                boardSize = scanner.nextInt();
                scanner.nextLine(); // Consume newline
            } while (!(boardSize % 2 == 0 && boardSize >= MIN_BOARD && boardSize <= MAX_BOARD));

            if (choice == 'f' || choice == 'F') {
                checkerBoard = new CheckerBoard(boardSize);
            } else if (choice == 'm' || choice == 'M') {
                checkerBoard = new CheckerBoardMem(boardSize);
            } else {
                System.out.println("Invalid choice. Using standard CheckerBoard.");
                checkerBoard = new CheckerBoard(boardSize);
            }

            populate_direction_map();

            // Game loop
            char player_turn = getPlayerOne();
            boolean gameInProgress = true;
            while (gameInProgress) {
                take_turn(player_turn);
                gameInProgress = !checkWin();
                if (gameInProgress) {
                    player_turn = (player_turn == getPlayerOne()) ? getPlayerTwo() : getPlayerOne();
                }
            }

            // Ask if the user wants to play again
            System.out.println("Would you like to play again? Enter 'Y' or 'N'");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("Y")) {
                playAgain = true;
            }

        } while (playAgain);

    }


    /** Prompts the player to enter the board position of the piece they wish to move and validates the input.
     *
     * @param player the character representing the current players piece
     * @return A valid BoardPosition where the player has a piece they can move
     * @pre checkerBoard != null AND player = getPlayerOne() || getPlayerTwo()
     * @post take_move_location = [a valid BoardPosition chosen by the user,repeates until valid input is chosen]
     */
    public static BoardPosition take_move_location_input(char player) {
        while (true) {
            System.out.printf("player %c, which piece do you wish to move? Enter the row followed by a space followed by the column.\n", player);
            try {
                String[] inputs = scanner.nextLine().split(" ");
                BoardPosition pos = new BoardPosition(Integer.parseInt(inputs[0]), Integer.parseInt(inputs[1]));
                if (pos.isValid(checkerBoard.getRowNum(), checkerBoard.getColNum()) && (checkerBoard.whatsAtPos(pos) == player) || checkerBoard.whatsAtPos(pos) == Character.toUpperCase(player)) {
                    return pos;
                }
                else if (checkerBoard.whatsAtPos(pos) == ICheckerBoard.BLACK_TILE)
                {
                    System.out.printf("Player %c, that isn't your piece. Pick one of your pieces.\n", player);
                }
                else
                {
                    System.out.println("Improper input format. It should be a row value\n" +
                            "followed by a space followed by a column value. E.x\n" +
                            ".: '4 5' ");
                }
            } catch (Exception e) {
                System.out.println("Improper input format. It should be a row value\n" +
                        "followed by a space followed by a column value. E.x\n" +
                        ".: '4 5' ");
            }
        }
    }

    /** Prompts the user to input a direction to move a piece on the checkerboard and validates the input against viable directions
     *
     * @param pos the board position of the current piece
     * @param current_piece the character representation of the current players piece
     * @return A DirectionEnum representing the valid direction chosen by the user from the available movement options.
     * @pre player = getPlayerOne() || getPlayerTwo() AND checkerBoard != null
     * @post take_movement_input = [takes in and validates a direction chosen by the user from the movement
     * options available to the current_piece at pos.]
     */
    public static DirectionEnum take_movement_input(BoardPosition pos, char current_piece) {
        // adds adjacent spaces from viableDirections that are not owned by the player to movement_options
        ArrayList<DirectionEnum> movement_options = new ArrayList<>();
        HashMap<DirectionEnum, Character> surroundings = checkerBoard.scanSurroundingPositions(pos);
        for (DirectionEnum direction : checkerBoard.getViableDirections().get(current_piece)) {
            if (surroundings.get(direction) != null && Character.toUpperCase(surroundings.get(direction)) != Character.toUpperCase(current_piece)) {
                //checks if jumping
                if (Character.toLowerCase(surroundings.get(direction)) == getOpponent(current_piece)){
                    // does not add direction to movement_options when exception is thrown
                    try{
                        BoardPosition landing_spot = BoardPosition.add(pos, BoardPosition.doubleBoardPosition(ICheckerBoard.getDirection(direction)));
                        // checks landing spot after jumping does not have other pieces and is on the board
                        if((checkerBoard.whatsAtPos(landing_spot) == CheckerBoard.EMPTY_POS) && landing_spot.isValid(checkerBoard.getRowNum(), checkerBoard.getColNum())){
                            movement_options.add(direction);
                        }
                    } catch (Exception e){}
                } else{
                    movement_options.add(direction);
                }
            }
        }
        while (true) {
            try {
                System.out.println("In which direction do you wish to move the piece? Enter one of these options:");
                for (DirectionEnum direction : movement_options) {
                    System.out.println(direction);
                }
                String input = scanner.nextLine();
                DirectionEnum input_direction = direction_conversion_map.get(input.toUpperCase());
                if (movement_options.contains(input_direction)) {
                    return input_direction;
                } else {
                    System.out.println("invalid direction choose one from the list");
                }
            } catch (Exception e) {
                System.out.println("invalid direction try again");
            }
        }
    }

    /** Executes a turn for the provided player. The turn includes selecting a piece to move, determining the movement direction,
     * and performing the move or jump as allowed by the game rules. If a move results in a piece reaching the opposite end
     * of the board, it may be crowned as a king.
     *
     * @param player the character representing the current player
     * @pre playerTurn = player
     * @post checkerBoard = [updated with pieces new position]
     */
    public static void take_turn(char player) {

       System.out.println(checkerBoard);

        BoardPosition pos = take_move_location_input(player);
        char current_piece = checkerBoard.whatsAtPos(pos);
        DirectionEnum movement_direction = take_movement_input(pos, current_piece);
        BoardPosition position_after_turn;
        // if value in movement direction is empty the piece will move but if an opponents piece is there it will jump
        if (checkerBoard.whatsAtPos(BoardPosition.add(pos, ICheckerBoard.getDirection(movement_direction))) == CheckerBoard.EMPTY_POS) {
            position_after_turn = checkerBoard.movePiece(pos, movement_direction);
        } else {
            position_after_turn = checkerBoard.jumpPiece(pos, movement_direction);
        }
        checkKing(checkerBoard.whatsAtPos(position_after_turn), position_after_turn);

    //System.out.println(checkerBoard);
    }

    /** Checks if a piece should be crowned as a king based on its position and the player.
     * If the piece satisfies the conditions for becoming a king, it is crowned.
     *
     * @param player the player piece that is being checked
     * @param pos the current position of the player piece on the board
     * @pre player = [piece chosen by player one and piece chosen by player two]
     * AND checkerBoard != null
     * @post checkKing = [IF player == 'X' AND pos.getRow() == ICheckerBoard.getRowNum() - 1:
     * checkerBoard[pos] is crowned as king.
     * Else if player == 'O' and pos.getRow() == 0:
     * checkerBoard[pos] is crowned as king.]
     */
    private static void checkKing(char player, BoardPosition pos) {
            if ((player == getPlayerOne() && pos.getRow() == checkerBoard.getRowNum() - 1) || (player == getPlayerTwo() && pos.getRow() == 0)) {
                checkerBoard.crownPiece(pos);
            }
    }

    /** Populates the direction conversion map with mappings from string representations of directions
     * to their corresponding DirectionEnum values.
     *
     * @pre none
     * @post populate_direction_map =
     * [{"NE" = DirectionEnum.NE},
     * {"NW" = DirectionEnum.NW},
     * {"SE" = DirectionEnum.SE},
     * {"SW" = DirectionEnum.SW]
     */
    private static void populate_direction_map() {
        direction_conversion_map.put("NE", DirectionEnum.NE);
        direction_conversion_map.put("NW", DirectionEnum.NW);
        direction_conversion_map.put("SE", DirectionEnum.SE);
        direction_conversion_map.put("SW", DirectionEnum.SW);
    }

    /** Checks if either players have met the win condition for the game
     *
     * @return true if a player has won, false OW
     * @pre checkerBoard != null
     * AND checkerBoard = [In a valid game state]
     * @post checkWin = [True IFF PLAYER_ONE || PLAYER_TWO have met win conditions, False OW]
     */
    private static boolean checkWin(){
        char winner;
        if (checkerBoard.checkPlayerWin(getPlayerOne())){
            winner = getPlayerOne();
        } else if (checkerBoard.checkPlayerWin(getPlayerTwo())){
            winner = getPlayerTwo();
        } else {
            return false;
        }
        System.out.printf("Player %c has won!\n", winner);
        return true;
    }

    /** figures a players opponent
     *
     * @param player player whos opponent should be found
     * @return char representing opponent
     * @pre player = [piece representing a player]
     * @post  getOpponent = [char of opposing player]
     */
    private static char getOpponent(char player){
        if (Character.toLowerCase(player) == getPlayerOne()){
            return getPlayerTwo();
        } else {
            return getPlayerOne();
        }
    }
}
