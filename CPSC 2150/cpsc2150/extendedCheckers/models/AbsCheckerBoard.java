package cpsc2150.extendedCheckers.models;

/** The AbsCheckerBoard class is an abstract class that implements ICheckerBoard and serves as the holder
 * of the toString method allowing for a visual representation of the checkerboard
 *
 */
public abstract class AbsCheckerBoard implements ICheckerBoard {
    /** Creates string representation of a checkers board
     *
     * @return A String representing the current state of the board
     * @pre none
     * @post toString = [string representation of board]
     * AND viableDirections = #viableDirections AND
     * board = #board and pieceCount = #pieceCount
     */
    @Override
    public String toString() {
        StringBuilder boardString = new StringBuilder();

        //Appends column numbers to the first row
        boardString.append("|  |");
        for(int col = 0; col < getColNum(); col++){
            // adds extra space for one-digit numbers so board lines up
            if (col < 10) {
                boardString.append(String.format(" %d|", col));
            } else {
                boardString.append(String.format("%d|", col));
            }
        }

        boardString.append("\n");

        //Iterates through rows and columns to build the board string

        for(int row = 0; row < getRowNum(); row++){

            //Retrieves the piece at current position and appends it to board string
            // adds extra space for one-digit numbers so board lines up
            if (row < 10) {
                boardString.append(String.format("|%d |", row));
            } else {
                boardString.append(String.format("|%d|", row));
            }
            for(int col = 0; col < getColNum(); col++){
                BoardPosition pos = new BoardPosition(row, col);
                char piece = whatsAtPos(pos);
                boardString.append(String.format("%c |", piece));
            }
            boardString.append("\n");
        }
        return boardString.toString();
    }

}
