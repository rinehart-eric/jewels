package src;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.lang.Math;
import java.util.Optional;
/**
 * A Jewel Quest-like game: swap tiles to make rows of 3 or more of the same color, remove every tile to win.
 */
public class Jewels extends JFrame {
	private static final long serialVersionUID = 1L;

	/** the minimum allowable number of rows or columns for the game */
	private static final int MINDIMENSION = 5;

	/** the maximum allowable number of rows or columns for the game */
	private static final int MAXDIMENSION = 16;

	/** the minimum number of jewel colors */
	private static final int MINJEWELS = 2;

	/** the maximum number of jewel colors */
	private static final int MAXJEWELS = 8;

	/** the colors that the game can use for jewels */
	private static final Color AVAILABLECOLORS[] = {Color.red, Color.green, Color.blue, Color.gray, Color.magenta, Color.cyan, Color.darkGray, Color.pink};

	/** the array of tiles for the game board */
	private JewelButton[][] buttons;

	/** the number of different jewels that the game uses */
	private int numJewels;

	/** number of rows on the game board */
	private int height;

	/** number of columns on the game board */
	private int width;

	/** the JewelButton that is currently selected */
	private Optional<JewelButton> selected;

	/** the number of moves the player has made so far */
	private int numMoves = 0;

	/** the Timer that checks for tiles to remove and removes them every .5 seconds until
	 * no more rows of the same color exist
	 */
	private Timer modifyBoard;

	/**
	 * default constructor: 10 x 10 grid with 4 colors of jewels
	 */
	public Jewels() {
		this(10, 10, 4);
	}

	/**
	 * constructor: creates a board of size rows x columns with the specified number of jewels.
	 * also creates the Timer that creates a delay when removing rows
	 * @param rows the number of rows in the game board
	 * @param columns the number of columns in the game board
	 * @param numJewels the number of colors of jewels to use
	 */
	public Jewels(int rows, int columns, int numJewels) {
		this.height = rows;
		this.width = columns;
		this.numJewels = numJewels;
		selected = Optional.empty();
		modifyBoard = new Timer(500, e -> {
			if (checkRowsAndCols()) {
				removeMarked();
				repaint();
			} else {
				modifyBoard.stop();
			}
			if (checkWin()) {
				modifyBoard.stop();
				JOptionPane.showMessageDialog(null, "You won in " + numMoves + " moves!");
			}
		});
		initializeBoard();
		// initialize the colors
		resetBoard();
		setSize(width * 50, (height + 1) * 50);
		setVisible(true);
	}

	/**
	 * initializes each tile and adds them to the board
	 */
	private void initializeBoard() {
		buttons = new JewelButton[height][width];
		JPanel buttonPanel = new JPanel(new GridLayout(height, width));
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				buttons[i][j] = new JewelButton(i, j);
				buttons[i][j].addActionListener(this::buttonClickUpdate);
				buttonPanel.add(buttons[i][j]);
			}
		}
		
		JButton restart = new JButton("New Game");
		restart.addActionListener(e -> resetGame());
		
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(buttonPanel, BorderLayout.CENTER);
		content.add(restart, BorderLayout.SOUTH);
	}
	
	private void buttonClickUpdate(ActionEvent e) {
		if (modifyBoard.isRunning() || checkWin()) {
			return;
		}
		JewelButton source = (JewelButton) e.getSource();
		if (!selected.isPresent()) {
			selected = Optional.of(source);
			source.setSelected(true);
			return;
		}
		
		JewelButton selectedButton = selected.get();
		if (source.adjacentTo(selectedButton)) {
			source.swapColors(selectedButton);
			rowStart(source).ifPresent(this::markRow);
			colStart(source).ifPresent(this::markCol);
			rowStart(selectedButton).ifPresent(this::markRow);
			colStart(selectedButton).ifPresent(this::markCol);
			if (source.getNeedsRemoved() || selectedButton.getNeedsRemoved()) {
				numMoves++;
				removeMarked();
				modifyBoard.start();
			}
			else {
				source.swapColors(selectedButton);
			}
			selected = Optional.empty();
			selectedButton.setSelected(false);
		} else {
			selected = Optional.empty();
			selectedButton.setSelected(false);
			selectedButton.repaint();
		}
	}

	/**
	 * sets each button to a random color and resets needsRemoved and wasRemoved.
	 * checks that rows of 3 of the same color have not been created.
	 */
	private void resetBoard() {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				do {
					buttons[i][j].setColor(randomColor());
				} while (completesRowOrCol(buttons[i][j]));
				buttons[i][j].setNeedsRemoved(false);
				buttons[i][j].setWasRemoved(false);
				buttons[i][j].setSelected(false);
			}
		}
	}
	
	private Color randomColor() {
		return AVAILABLECOLORS[(int) (Math.random() * numJewels)];
	}

	/**
	 * checks if a button creates a row of 3 of the same color
	 * @param button the third button in a potential row or column of 3
	 * @return true if the button completes a row or column of 3 tiles of the same color
	 */
	private boolean completesRowOrCol(JewelButton button) {
		int row = button.getRow();
		int col = button.getColumn();
		if (row > 1 && button.isSameColor(buttons[row-1][col]) && button.isSameColor(buttons[row-2][col])) {
			return true;
		}
		if (col > 1 && button.isSameColor(buttons[row][col-1]) && button.isSameColor(buttons[row][col-2])) {
			return true;
		}
		return false;
	}

	/**
	 * Resets the game state.
	 */
	private void resetGame() {
		modifyBoard.stop();
		resetBoard();
		selected = Optional.empty();
		numMoves = 0;
	}

	/**
	 * determines if button is in a row of 3 or more of the same color
	 * @param button the button to check for a row of the same color
	 * @return the start of the row, if there is a row of 3 or more
	 */
	private Optional<JewelButton> rowStart(JewelButton button) {
		int rowLength = 1; // the current length of the row of the same color
		int i;
		int row = button.getRow(); // stores the row number of button
		int col = button.getColumn(); // stores the column number of button
		for (i = col + 1; i < width && buttons[row][i].isSameColor(button); i++) {
			rowLength++;
		}
		for (i = col - 1; i >= 0 && buttons[row][i].isSameColor(button); i--) {
			rowLength++;
		}
		return rowLength < 3 ? Optional.empty() : Optional.of(buttons[row][i + 1]);
	}

	/**
	 * determines if button is in a column of 3 or more of the same color
	 * @param button the button to check for a column of the same color
	 * @return the start of the column, if there is a column of 3 or more
	 */
	private Optional<JewelButton> colStart(JewelButton button) {
		int colLength = 1; // the current length of the column of the same color
		int i;
		int row = button.getRow(); // stores the row number of button
		int col = button.getColumn(); // stores the column number of button
		for (i = row + 1; i < width && buttons[i][col].isSameColor(button); i++) {
			colLength++;
		}
		for (i = row - 1; i >= 0 && buttons[i][col].isSameColor(button); i--) {
			colLength++;
		}
		return colLength < 3 ? Optional.empty() : Optional.of(buttons[i + 1][col]);
	}


	/**
	 * marks a row of the same color for removal
	 * @param start the start of the row of buttons
	 */
	private void markRow(JewelButton start) {
		int row = start.getRow();
		int col = start.getColumn();
		for (int i = col; i < width && buttons[row][i].isSameColor(start); i++) {
			buttons[row][i].setNeedsRemoved(true);
		}
	}

	/**
	 * marks a column of the same color for removal
	 * @param start the start of the column of buttons
	 */
	private void markCol(JewelButton start) {
		int row = start.getRow();
		int col = start.getColumn();
		for (int i = row; i < height && buttons[i][col].isSameColor(start); i++) {
			buttons[i][col].setNeedsRemoved(true);
		}
	}

	/**
	 * removes every button that is marked for removal
	 */
	private void removeMarked() {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (buttons[i][j].getNeedsRemoved()) {
					for (int r = i; r > 0; r--) {
						buttons[r][j].swapColors(buttons[r-1][j]);
					}
					buttons[0][j].setColor(randomColor());
					buttons[i][j].setNeedsRemoved(false);
				}
			}
		}
	}

	/**
	 * checks for any rows of the same color that have been created by removing marked buttons
	 * @return true if new rows have been created
	 */
	private boolean checkRowsAndCols() {
		boolean remove = false; // whether buttons need removed
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (!buttons[i][j].getNeedsRemoved()) {
					rowStart(buttons[i][j]).ifPresent(this::markRow);
					colStart(buttons[i][j]).ifPresent(this::markCol);
					if (buttons[i][j].getNeedsRemoved()) {
						remove = true;
					}
				}
			}
		}
		return remove;
	}

	/**
	 * checks if all of the buttons have been removed at some point
	 * @return true if all buttons have been removed
	 */
	private boolean checkWin() {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if (!buttons[i][j].getWasRemoved()) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * checks if a valid height, width and number of jewels have been passed as arguments and creates a new jewels game.
	 * calls the default constructor if 0-2 arguments have been passed
	 */
	public static void main(String[] args) {
		if (args.length >= 3) {
			try {
				int height = bound(Integer.parseInt(args[0]), MINDIMENSION, MAXDIMENSION); // height of the game board
				int width = bound(Integer.parseInt(args[1]), MINDIMENSION, MAXDIMENSION); // width of the game board
				int numJewels = bound(Integer.parseInt(args[2]), MINJEWELS, MAXJEWELS); // number of jewels used in the game
				
				new Jewels(height, width, numJewels);
			} catch (NumberFormatException e) {}
		}
		new Jewels();
	}
	
	private static int bound(int value, int min, int max) {
		return Math.max(Math.min(value, max), min);
	}
}