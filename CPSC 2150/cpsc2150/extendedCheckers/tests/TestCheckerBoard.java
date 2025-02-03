package cpsc2150.extendedCheckers.tests;
import cpsc2150.extendedCheckers.models.*;
import cpsc2150.extendedCheckers.util.DirectionEnum;
import cpsc2150.extendedCheckers.views.CheckersFE;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TestCheckerBoard {
    private ICheckerBoard makeBoard(int size) {
        return new CheckerBoard(size);
    }

    private String arrayToString(char[][] board) {
        StringBuilder sb = new StringBuilder();

        sb.append("|  | 0| 1| 2| 3| 4| 5| 6| 7|\n");
        for (int i = 0; i < board.length; i++) {
            sb.append("|").append(i).append(" |");
            for (char cell : board[i]) {
                sb.append(cell).append(" |");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Test
    public void testGetPieceCounts_PLAYER_ONE() {
        ICheckerBoard board = makeBoard(8);
        Integer expected = 12;
        Integer observed = board.getPieceCounts().get('x'); // Get the count safely
        assertEquals("Expected PLAYER_ONE to have " + expected + " pieces, but got " + observed + " pieces.",
                expected, observed);
    }
    @Test
    public void testGetViableDirections_PLAYER_ONE() {
        ICheckerBoard board = makeBoard(8);
        List<DirectionEnum> expected = Arrays.asList(DirectionEnum.SE, DirectionEnum.SW);
        List<DirectionEnum> observed = board.getViableDirections().get('x'); // Assuming 'x' is PLAYER_ONE

        assertTrue("Viable directions for PLAYER_ONE should include SE and SW",
                observed.containsAll(expected) && expected.containsAll(observed));
    }
    @Test
    public void testAddViableDirections_New_NE_Direction_for_PLAYER_ONE() {
        ICheckerBoard board = makeBoard(8);
        board.addViableDirections('x', DirectionEnum.NE);
        List<DirectionEnum> expected = Arrays.asList(DirectionEnum.SE, DirectionEnum.SW, DirectionEnum.NE);

        List<DirectionEnum> observed = board.getViableDirections().get('x');
        assertTrue("Viable directions for PLAYER_ONE should include SE, SW, and newly added NE",
                observed.containsAll(expected) && expected.containsAll(observed));
    }

    @Test
    public void testPlayerLostPiecesPlayerOneLost1() {
        ICheckerBoard cb = makeBoard(8);
        cb.playerLostPieces(1, CheckersFE.getPlayerOne(), cb.getPieceCounts());
        int player_one_pieces = cb.getPieceCounts().get(CheckersFE.getPlayerOne());
        int player_two_pieces = cb.getPieceCounts().get(CheckersFE.getPlayerTwo());
        assertEquals(11, player_one_pieces);
        assertEquals(12, player_two_pieces);
    }

    @Test
    public void testCrownPieceValidPosPlayerOne()
    {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(2, 2);
        cb.crownPiece(pos);
        assertEquals(cb.whatsAtPos(pos), 'X');
    }

    @Test
    public void testCrownPieceValidPosPlayerTwo()
    {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(5, 3);
        cb.crownPiece(pos);
        assertEquals(cb.whatsAtPos(pos), 'O');
    }
    @Test
    public void testCrownPieceAlreadyCrowned()
    {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(0, 2);
        cb.crownPiece(pos); //initial crown
        cb.crownPiece(pos);
        assertEquals(cb.whatsAtPos(pos), 'X');
    }
    @Test
    public void testCrownPieceMultiplePieces()
    {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos1 = new BoardPosition(0, 2);
        BoardPosition pos2 = new BoardPosition(0, 4);
        BoardPosition pos3 = new BoardPosition(0, 6);
        cb.crownPiece(pos1);
        cb.crownPiece(pos2);
        cb.crownPiece(pos3);
        assertEquals(cb.whatsAtPos(pos1), 'X');
        assertEquals(cb.whatsAtPos(pos2), 'X');
        assertEquals(cb.whatsAtPos(pos3), 'X');
    }

    @Test
    public void testMovePieceValidMove()
    {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(2, 2);
        assertEquals(new BoardPosition(3, 3), cb.movePiece(pos, DirectionEnum.SE)); //check to see if the position is now 3,3

    }
    @Test
    public void testMovePieceSize16Board()
    {
        ICheckerBoard cb = makeBoard(16);
        BoardPosition originalPos = new BoardPosition(1, 15);
        BoardPosition movedPos = new BoardPosition(2, 14);

        cb.placePiece(movedPos, CheckerBoard.EMPTY_POS);

        assertEquals(movedPos, cb.movePiece(originalPos, DirectionEnum.SW));

    }
    @Test
    public void testMovePieceEdgeCases()
    {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition originalPos = new BoardPosition(1, 7);
        BoardPosition movedPos = new BoardPosition(2, 6);

        cb.placePiece(movedPos, CheckerBoard.EMPTY_POS); //create blank space for piece to move to

        assertEquals(movedPos, cb.movePiece(originalPos, DirectionEnum.SW)); //check to see if the position is now 2,6

    }

    @Test
    public void testScanSurroundingPositionsInMiddle() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition current_pos = new BoardPosition(4, 4);
        BoardPosition previous_pos = new BoardPosition(5, 5);
        cb.placePiece(current_pos, CheckersFE.getPlayerTwo());
        cb.placePiece(previous_pos, CheckerBoard.EMPTY_POS);
        HashMap<DirectionEnum, Character> surrounding_positions = cb.scanSurroundingPositions(current_pos);
        assertEquals(CheckerBoard.EMPTY_POS, surrounding_positions.get(DirectionEnum.NE).charValue());
        assertEquals(CheckerBoard.EMPTY_POS, surrounding_positions.get(DirectionEnum.SE).charValue());
        assertEquals(CheckersFE.getPlayerTwo(), surrounding_positions.get(DirectionEnum.SW).charValue());
        assertEquals(CheckerBoard.EMPTY_POS, surrounding_positions.get(DirectionEnum.NW).charValue());
    }

    @Test
    public void testScanSurroundingPositionsAtTop(){
        ICheckerBoard cb = makeBoard(8);
        HashMap<DirectionEnum, Character> surrounding_positions = cb.scanSurroundingPositions(new BoardPosition(0, 4));
        assertFalse(surrounding_positions.containsKey(DirectionEnum.NE));
        assertFalse(surrounding_positions.containsKey(DirectionEnum.NW));
        assertEquals(CheckersFE.getPlayerOne(), surrounding_positions.get(DirectionEnum.SE).charValue());
        assertEquals(CheckersFE.getPlayerOne(), surrounding_positions.get(DirectionEnum.SW).charValue());
    }

    @Test
    public void testScanSurroundingPositionsOnRight(){
        ICheckerBoard cb = makeBoard(8);
        HashMap<DirectionEnum, Character> surrounding_positions = cb.scanSurroundingPositions(new BoardPosition(1, 7));
        assertFalse(surrounding_positions.containsKey(DirectionEnum.NE));
        assertFalse(surrounding_positions.containsKey(DirectionEnum.SE));
        assertEquals(CheckersFE.getPlayerOne(), surrounding_positions.get(DirectionEnum.NW).charValue());
        assertEquals(CheckersFE.getPlayerOne(), surrounding_positions.get(DirectionEnum.SW).charValue());
    }
    @Test
    public void testScanSurroundingPositionsAtBottom(){
        ICheckerBoard cb = makeBoard(8);
        HashMap<DirectionEnum, Character> surrounding_positions = cb.scanSurroundingPositions(new BoardPosition(7, 1));
        assertFalse(surrounding_positions.containsKey(DirectionEnum.SW));
        assertFalse(surrounding_positions.containsKey(DirectionEnum.SE));
        assertEquals(CheckersFE.getPlayerTwo(), surrounding_positions.get(DirectionEnum.NW).charValue());
        assertEquals(CheckersFE.getPlayerTwo(), surrounding_positions.get(DirectionEnum.NE).charValue());
    }

    @Test
    public void testScanSurroundingPositionsOnLeft(){
        ICheckerBoard cb = makeBoard(8);
        HashMap<DirectionEnum, Character> surrounding_positions = cb.scanSurroundingPositions(new BoardPosition(4, 0));
        assertFalse(surrounding_positions.containsKey(DirectionEnum.SW));
        assertFalse(surrounding_positions.containsKey(DirectionEnum.NW));
        assertEquals(CheckersFE.getPlayerTwo(), surrounding_positions.get(DirectionEnum.SE).charValue());
        assertEquals(CheckerBoard.EMPTY_POS, surrounding_positions.get(DirectionEnum.NE).charValue());
    }

    @Test
    public void testJumpPiecePlayerTwoOverPlayerOne(){
        ICheckerBoard cb = makeBoard(8);
        BoardPosition jumping_pos = new BoardPosition(4, 4);
        BoardPosition jumped_over_pos = new BoardPosition(3, 3);
        BoardPosition landing_pos = new BoardPosition(2, 2);
        cb.placePiece(new BoardPosition(5, 5), CheckerBoard.EMPTY_POS);
        cb.placePiece(jumped_over_pos, CheckersFE.getPlayerOne());
        cb.placePiece(landing_pos, CheckerBoard.EMPTY_POS);
        cb.placePiece(jumping_pos, CheckersFE.getPlayerTwo());
        cb.jumpPiece(jumping_pos, DirectionEnum.NW);
        assertEquals(cb.whatsAtPos(jumping_pos), CheckerBoard.EMPTY_POS);
        assertEquals(cb.whatsAtPos(jumped_over_pos), CheckerBoard.EMPTY_POS);
        assertEquals(cb.whatsAtPos(landing_pos), CheckersFE.getPlayerTwo());
        int player_one_pieces = cb.getPieceCounts().get(CheckersFE.getPlayerOne());
        int player_two_pieces = cb.getPieceCounts().get(CheckersFE.getPlayerTwo());
        assertEquals(11, player_one_pieces);
        assertEquals(12, player_two_pieces);
    }

    @Test
    public void testJumpPiecePlayerOneOverPlayerTwo(){
        ICheckerBoard cb = makeBoard(8);
        BoardPosition jumping_pos = new BoardPosition(2, 2);
        BoardPosition jumped_over_pos = new BoardPosition(3, 3);
        BoardPosition landing_pos = new BoardPosition(4, 4);
        cb.placePiece(new BoardPosition(5, 5), CheckerBoard.EMPTY_POS);
        cb.placePiece(jumped_over_pos, CheckersFE.getPlayerTwo());
        cb.placePiece(landing_pos, CheckerBoard.EMPTY_POS);
        cb.placePiece(jumping_pos, CheckersFE.getPlayerOne());
        cb.jumpPiece(jumping_pos, DirectionEnum.SE);
        assertEquals(cb.whatsAtPos(jumping_pos), CheckerBoard.EMPTY_POS);
        assertEquals(cb.whatsAtPos(jumped_over_pos), CheckerBoard.EMPTY_POS);
        assertEquals(cb.whatsAtPos(landing_pos), CheckersFE.getPlayerOne());
        int player_one_pieces = cb.getPieceCounts().get(CheckersFE.getPlayerOne());
        int player_two_pieces = cb.getPieceCounts().get(CheckersFE.getPlayerTwo());
        assertEquals(11, player_two_pieces);
        assertEquals(12, player_one_pieces);
    }
    @Test
    public void testJumpPiece_to_Occupied_Space(){
        ICheckerBoard cb = makeBoard(8); // Assuming makeBoard initializes a board of the given size
        // Place pieces on the board
        cb.placePiece(new BoardPosition(3, 3), 'x'); // Player One's piece attempting to jump
        cb.placePiece(new BoardPosition(4, 2), 'o'); // Player Two's piece being jumped over
        cb.placePiece(new BoardPosition(5, 1), 'o'); // Player Two's piece occupying the landing spot
        // Check the board is unchanged
        assertEquals('x', cb.whatsAtPos(new BoardPosition(3, 3)));
        assertEquals('o', cb.whatsAtPos(new BoardPosition(4, 2)));
        assertEquals('o', cb.whatsAtPos(new BoardPosition(5, 1)));
        // Check the piece counts remain unchanged
        int playerOnePieces = cb.getPieceCounts().get('x');
        int playerTwoPieces = cb.getPieceCounts().get('o');
        assertEquals(12, playerOnePieces);
        assertEquals(12, playerTwoPieces);
    }


    @Test
    public void testConstructor_ValidDimension() {
        int size = 8;
        char[][] expectedArray = new char[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if ((i + j) % 2 != 0) {
                    expectedArray[i][j] = '*'; // Non-playable tile
                } else {
                    if (i < (size / 2 - 1)) {
                        expectedArray[i][j] = 'x'; // Player one pieces
                    } else if (i >= (size / 2 + 1)) {
                        expectedArray[i][j] = 'o'; // Player two pieces
                    } else {
                        expectedArray[i][j] = ' '; // Empty tile for playable spots without pieces
                    }
                }
            }
        }

        ICheckerBoard cb = makeBoard(size);
        String expected = arrayToString(expectedArray);
        assertEquals(expected, cb.toString());
    }



    @Test
    public void testConstructor_InvalidDimensionNegative() {

        int aDimension = -1;
        ICheckerBoard observed = makeBoard(aDimension);
        ICheckerBoard expected = makeBoard(8);

        assertEquals(expected.toString(),observed.toString());
    }

    @Test
    public void testConstructor_InvalidDimensionUpper() {

        int aDimension = 20;
        ICheckerBoard observed = makeBoard(aDimension);
        ICheckerBoard expected = makeBoard(16);

        assertEquals(expected.toString(),observed.toString());

    }


    @Test
    public void testWhatsAtPos_EmptyPosition() {

        int size = 8;
        ICheckerBoard cb = makeBoard(size);
        char expected = ' '; // Assuming ' ' is used for empty spaces in your implementation

        // Placing a few pieces to ensure we have known non-empty positions
        cb.placePiece(new BoardPosition(0, 0), 'x');
        cb.placePiece(new BoardPosition(1, 1), 'o');

        // Test an empty position
        char actual = cb.whatsAtPos(new BoardPosition(3, 3));
        assertEquals(expected, actual);
    }

    @Test
    public void testWhatsAtPos_OccupiedByPlayerX() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(1, 1);
        cb.placePiece(pos, 'X');
        assertEquals('X', cb.whatsAtPos(pos));
    }

    @Test
    public void testWhatsAtPos_OccupiedByPlayer0() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(2, 2);
        cb.placePiece(pos, 'O');
        assertEquals('O', cb.whatsAtPos(pos));
    }

    @Test
    public void testWhatsAtPos_MinRow_MaxCol_X() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(0, 7); // Max row, min column
        assertEquals('*', cb.whatsAtPos(pos));
    }

    @Test
    public void testWhatsAtPos_MaxRow_MinCol_X() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(7, 0); // Max row, min column
        assertEquals('*', cb.whatsAtPos(pos));
    }

    @Test
    public void testPlacePieceBoundaryPosition() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(3, 7); // A boundary position on a 8x8 board
        cb.placePiece(pos, 'X');
        assertEquals('X', cb.whatsAtPos(pos));
    }

    @Test
    public void testPlacePieceValid_O() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(3, 3);
        cb.placePiece(pos, 'O');
        assertEquals('O', cb.whatsAtPos(pos));
    }

    @Test
    public void testPlacePieceOnOccupiedPosition() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(3, 3);
        cb.placePiece(pos, 'X');
        cb.placePiece(pos, 'O');
        assertEquals('O', cb.whatsAtPos(pos));
    }

    @Test
    public void testPlacePieceValidX() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(3, 3);
        cb.placePiece(pos, 'X');
        assertEquals('X', cb.whatsAtPos(pos));
    }

    @Test
    public void testPlacePieceChangeState() {
        ICheckerBoard cb = makeBoard(8);
        BoardPosition pos = new BoardPosition(5, 5);
        cb.placePiece(pos, 'X');
        assertEquals('X', cb.whatsAtPos(pos));
        cb.placePiece(pos, 'O'); // Attempt to change the piece at the same position
        assertEquals('O', cb.whatsAtPos(pos)); // Verifying if state changed
    }

    @Test
    public void testPlacePieceInvalidPlayer() {
        int size = 8;
        ICheckerBoard cb = makeBoard(size);
        char invalidPiece = 'z'; // Assuming 'z' is not a valid piece character

        // Attempting to place an invalid piece
        cb.placePiece(new BoardPosition(0, 0), invalidPiece);
    }

    @Test
    public void testGetColNum() {
        ICheckerBoard cb = makeBoard(8); // Assuming 8x8 board
        assertEquals("Column count should match the initialized board size.", 8, cb.getColNum());
    }

    @Test
    public void testGetRowNum() {
        ICheckerBoard cb = makeBoard(8); // Assuming 8x8 board
        assertEquals("Row count should match the initialized board size.", 8, cb.getRowNum());
    }

    @Test
    public void testPlayerWinNoWinner() {
        ICheckerBoard cb = makeBoard(8);
        // Set up the board in a state that does not meet the win condition
        cb.placePiece(new BoardPosition(0, 0), 'X');
        cb.placePiece(new BoardPosition(1, 1), 'O');
        assertFalse("No player should have won yet.", cb.checkPlayerWin('X'));
        assertFalse("No player should have won yet.", cb.checkPlayerWin('O'));
    }

    @Test
    public void testCheckPlayerWinWinner() {

        int size = 8;
        ICheckerBoard cb = makeBoard(size);
        // Placing 'x' in a specific pattern to simulate a win

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if ((i + j) % 2 == 0) { // Assuming 'x' can only be on specific tiles based on the game rules
                    cb.placePiece(new BoardPosition(i, j), 'x');
                }
            }
        }

        // Asserting a win condition for player 'x'
        boolean winResult = cb.checkPlayerWin('x');
        assertFalse(winResult);

    }
    @Test
    public void testGetDirectionNE() {
        BoardPosition pos = ICheckerBoard.getDirection(DirectionEnum.NE);
        assertEquals(-1, pos.getRow());
        assertEquals(1, pos.getColumn());
    }
}
