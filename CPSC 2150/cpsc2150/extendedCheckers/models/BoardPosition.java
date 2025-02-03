package cpsc2150.extendedCheckers.models;


/** The BoardPosition class represents a Board position on the checkerboard
 * It contains information about the board position's row and column as well as performing functions with those
 * values that can add two BoardPositions together, check if the current board position is within the bounds of
 * the checkerboard and check a BoardPosition object against another BoardPosition object
 *
 * @invariant 0 <= row <= rowBound AND 0 <= column <= columnBound
 */

public class BoardPosition
{

    /**
     * Row component of the BoardPosition
     */
    private int row;

    /**
     * Column component of the BoardPosition
     */
    private int column;

    /** Constructor for BoardPosition both the row and column instance variables to the default size
     *
     * @param aRow the row of the BoardPosition
     * @param aCol the column of the BoardPosition
     * @pre 0 <= aRow <= rowBound AND 0 <= aCol <= columnBound
     * @post row = aRow AND column = aCol
     *
     */
    public BoardPosition(int aRow, int aCol) {
        this.row = aRow;
        this.column = aCol;
    }
    /**
     Standard getter for row of checkerboard

     @return the number associated the row number
     @pre none
     @post getRow = row AND column = #column
     */
    public int getRow() {
        return this.row;
    }

    /**
     Standard getter for column of checkerboard

     @return the number associated the column number
     @pre none
     @post getColumn = column AND row = #row
     */
    public int getColumn() {
        return this.column;
    }

    /**
     *  This function adds two Board Positions rows together AND BoardPositions' columns together AND
     *  returns that value in the form of a new BoardPosition object
     *
     * @param posOne BoardPosition object
     * @param posTwo BoardPosition object
     * @return new BoardPosition that contains a
     * row equal to the sum of both parameters' rows and a column equal to the sum of both parameters' column
     * @pre none
     * @post newBoardPosition.row = posOne.row + posTwo.row AND
     * newBoardPosition.column = posOne.column + posTwo.column AND
     * posOne = #posOne AND posTwo = #posTwo
     */
    public static BoardPosition add(BoardPosition posOne, BoardPosition posTwo) {

        int row = posOne.getRow() + posTwo.getRow();
        int col = posOne.getColumn() + posTwo.getColumn();

        return new BoardPosition(row,col);
    }
    /**
     * This function creates a BoardPosition object that has a row and a column equal to double the row and double
     * the column of the parameter passed to the function
     *
     * @param pos a BoardPosition that is to be doubled
     * @return new BoardPosition object with a row and column that is doubled
     * @pre none
     * @post newBoardPosition.row = 2 * pos.row AND
     * newBoardPosition.column = 2 * pos.column AND
     * pos = #pos
     *
     */
    public static BoardPosition doubleBoardPosition(BoardPosition pos) {
        int row = 2 * pos.getRow();
        int col = 2 * pos.getColumn();
        return new BoardPosition(row,col);
    }

    /** Returns true or false depending on if the BoardPosition is within the bounds of
     * 0 and rowBound and columnBound parameter
     *
     * @param rowBound, the boundary int to check BoardPosition.row against
     * @param columnBound, the boundary int to check BoardPosition.column against
     * @return boolean, true if BoardPosition row and column >= 0 and <= their respective bounds
     * @pre none
     * @post isValid = [true IFF 0 <= BoardPosition.row <= rowBound AND
     * 0<= BoardPosition.column <= columnBound, False OW] AND
     * row = #row AND column = #column
     */
    public boolean isValid(int rowBound, int columnBound) {
        return this.row >= 0 && this.row < rowBound && this.column >= 0 && this.column < columnBound;
    }

    /** This function returns true if the BoardPosition is equal to the parameter object by checking if
     * their row and column values are the same
     *
     * @param obj, the object to check against another BoardPosition object
     * @return boolean, true if both BoardPosition and obj are equal, false if not
     * @pre none
     * @post equals = [true IFF this.row = obj.row AND
     * this.column = obj.column, False OW]
     * AND row = #row AND column = #column
     */
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(!(obj instanceof BoardPosition)) return false;
        BoardPosition diffPos = (BoardPosition) obj;
        return this.row == diffPos.getRow() && this.column == diffPos.getColumn();
    }

    /** Creates a String representation of the BoardPosition
     *
     * @return String representation of BoardPosition object
     * @pre none
     * @post toString = [a string representation of the row followed by a "," and then the column]
     * row = #row AND column = #column
     */
    public String toString() {
        return this.row + "," + this.column;
    }
}
