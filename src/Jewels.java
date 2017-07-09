package src;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.lang.Math;
/**
 * A Jewel Quest-like game: swap tiles to make rows of 3 or more of the same color, remove every tile to win.
 * @author Eric Rinehart
 */
public class Jewels extends JFrame implements ActionListener {
  /**
   * A tile class for the Jewels game.
   * @author Eric Rinehart
   */
  public class JewelButton extends JButton implements ActionListener {
    /** the color of the button */
    private Color color;
    
    /** whether the button should be removed by removeMarked() */
    private boolean needsRemoved;
    
    /** whether the button has been removed at some point */
    private boolean wasRemoved;
    
    /** the row the button is in */
    private int row;
    
    /** the column the button is in */
    private int column;
    
    /**
     * constructor: sets the color of the button and adds the actionListener
     * @param color the color of the button
     */
    public JewelButton(Color color, int row, int column) {
      this.color = color;
      this.row = row;
      this.column = column;
      addActionListener(this);
    }
    
    /**
     * get the value of needsRemoved
     * @return the value of needsRemoved
     */
    public boolean getNeedsRemoved() {
      return needsRemoved;
    }
    
    /**
     * set the value of needsRemoved
     * @param needsRemoved the value to set
     */
    public void setNeedsRemoved(boolean needsRemoved) {
      this.needsRemoved = needsRemoved;
      wasRemoved = true;
    }
    
    /**
     * get the value of wasRemoved
     * @return the value of wasRemoved
     */
    public boolean getWasRemoved() {
      return wasRemoved;
    }
    
    /**
     * set the value of wasRemoved
     * @param wasRemoved the value to set
     */
    public void setWasRemoved(boolean wasRemoved) {
      this.wasRemoved = wasRemoved;
    }

    /** gets the row the button is in */
    public int getRow() {
      return row;
    }
    
    /** gets the column the button is in */
    public int getColumn() {
      return column;
    }
    
    /**
     * gets the color of this button
     * @return the button's color
     */
    public Color getColor() {
      return color;
    }
    
    /**
     * set the color of this button
     * @param color the new color for this button
     */
    public void setColor(Color color) {
      this.color = color;
      repaint();
    }
    
    /**
     * Paints the tile. Outlines the tile if it is selected, gives it a yellow background if
     * it has been removed and then paints the jewel on the button.
     * @param g the Graphics object associated with this component
     */
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      int[] xCoords = {getWidth()/2, getWidth()-10, getWidth()/2, 10}; // the x-coordinates for the jewel
      int[] yCoords = {10, getHeight()/2, getHeight()-10, getHeight()/2}; // the y-coordinates for the jewel
      if (this == selected) {
        setBackground(Color.lightGray);
        g.setColor(wasRemoved ? Color.yellow : Color.white);
        g.fillRect(4, 4, getWidth()-8, getHeight()-8);
      }
      else {
        g.setColor(wasRemoved ? Color.yellow : Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
      }
      g.setColor(color);
      g.fillPolygon(xCoords, yCoords, 4);
      g.setColor(Color.black);
      g.drawPolygon(xCoords, yCoords, 4);
    }
    
    /**
     * Sets the tile actionPerformed is called on as selected if no tile has been selected.
     * Otherwise, if the tiles are adjacent and one of the tiles creates a row of 3 or more,
     * removes the rows and continues checking for more rows until none remain. Nothing happens
     * if the game has been won or if tiles are still being removed.
     * @param e the actionEvent passed by the OS when the action is performed
     */
    public void actionPerformed(ActionEvent e) {
      if (!modifyBoard.isRunning() && !checkWin()) {
        if (selected == null)
          selected = this;
        else if (isAdjacent(this, selected)) {
          swapColors(this, selected);
          markRow(rowStart(this));
          markCol(colStart(this));
          markRow(rowStart(selected));
          markCol(colStart(selected));
          if (this.getNeedsRemoved() || selected.getNeedsRemoved()) {
            numMoves++;
            removeMarked();
            modifyBoard.start();
          }
          else {
            swapColors(this, selected);
          }
          selected = null;
        }
        else {
          JewelButton temp = selected; // stores the selected button so it can be repainted
          selected = null;
          temp.repaint();
        }
      }
    }
  }
  
  /** DECLARATION OF INDEPENDENCE */
  public static final boolean INDEPENDENCE = true;
  
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
  
  /** the New Game button: resets the game */
  private JButton restart;
  
  /** the number of different jewels that the game uses */
  private int numJewels;
  
  /** number of rows on the game board */
  private int height;
  
  /** number of columns on the game board */
  private int width;
  
  /** the JewelButton that is currently selected */
  private JewelButton selected = null;
  
  /** the number of moves the player has made so far */
  private int numMoves = 0;
  
  /** the Timer that checks for tiles to remove and removes them every .5 seconds until
    * no more rows of the same color exist
    */
  private Timer modifyBoard;
  
  /** the ActionListener that is attached to the Timer that checks for tiles and removes them */
  private ActionListener taskPerformer;
  
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
    height = rows;
    width = columns;
    this.numJewels = numJewels;
    taskPerformer = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (checkRowsAndCols()) {
          removeMarked();
          repaintFrame();
        }
        else {
          modifyBoard.stop();
        }
        if (checkWin()) {
          JOptionPane.showMessageDialog(null, "You won in " + numMoves + " moves!");
          modifyBoard.stop();
        }
      }
    };
    modifyBoard = new Timer(500, taskPerformer);
    buttons = new JewelButton[height][width];
    initializeBoard();
    randomizeColors();
    setSize(width*50, (height+1)*50);
    setVisible(true);
  }
  
  /**
   * initializes each tile and adds them to the board
   */
  public void initializeBoard() {
    JPanel main = new JPanel(new GridLayout(height+1, 1)); // the main panel for the game board
    /* loop goal: creates a JPanel that represents a row of buttons, adds the buttons to it and adds the row to the main JPanel */
    for (int i = 0; i < height; i++) {
      JPanel row = new JPanel(new GridLayout(1, width)); // a panel for each row of the game board
      for (int j = 0; j < width; j++) {
        buttons[i][j] = new JewelButton(AVAILABLECOLORS[(int)(Math.random()*numJewels)], i, j);
        row.add(buttons[i][j]);
      }
      main.add(row);
    }
    restart = new JButton("New Game");
    restart.addActionListener(this);
    main.add(restart);
    getContentPane().add(main, "Center");
  }
  
  /**
   * sets each button to a random color and resets needsRemoved and wasRemoved.
   * checks that rows of 3 of the same color have not been created.
   */
  public void randomizeColors() {
    /* loop goal: sets the value of each button to a random color, checking that it doesn't create a row of three
     * buttons of the same color, and then resets the values of needsRemoved and wasRemoved.
     */
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        do {
          buttons[i][j].setColor(AVAILABLECOLORS[(int)(Math.random()*numJewels)]);
        } while (completesRowOrCol(buttons[i][j]));
        buttons[i][j].setNeedsRemoved(false);
        buttons[i][j].setWasRemoved(false);
      }
    }
  }
  
  /**
   * checks if a button creates a row of 3 of the same color
   * @param button the third button in a potential row or column of 3
   * @return true if the button completes a row or column of 3 tiles of the same color
   */
  public boolean completesRowOrCol(JewelButton button) {
    int row = button.getRow(); // button's row number
    int col = button.getColumn(); // button's column number
    Color leftColor = null, upColor = null;
    if (row > 1) {
      if (buttons[row-1][col].getColor() == buttons[row-2][col].getColor())
        leftColor = buttons[row-1][col].getColor();
    }
    if (col > 1) {
      if (buttons[row][col-1].getColor() == buttons[row][col-2].getColor())
        leftColor = buttons[row][col-1].getColor();
    }
    if (button.getColor() == leftColor || button.getColor() == upColor)
      return true;
    return false;
  }
  
  /**
   * actionPerformed is called when the New Game button is pressed and resets the board
   */
  public void actionPerformed(ActionEvent e) {
    randomizeColors();
    selected = null;
    numMoves = 0;
  }
  
  /**
   * determines if button1 is adjacent to button2
   * @param button1 the first button
   * @param button2 the second button
   * @return true if the buttons are adjacent
   */
  public boolean isAdjacent(JewelButton button1, JewelButton button2) {
    if (Math.abs(button1.getRow() - button2.getRow()) == 1 || Math.abs(button1.getColumn() - button2.getColumn()) == 1)
      return true;
    return false;
  }
  
  /**
   * swaps the colors of button1 and button2
   * @param button1 the first button
   * @param button2 the second button
   */
  public void swapColors(JewelButton button1, JewelButton button2) {
    Color temp = button1.getColor(); // temporarily stores the color of button1
    button1.setColor(button2.getColor());
    button2.setColor(temp);
  }
  
  /**
   * determines if button is in a row of 3 or more of the same color
   * @param button the button to check for a row of the same color
   * @return the start of the row, or null if there is no row
   */
  public JewelButton rowStart(JewelButton button) {
    int rowLength = 1; // the current length of the row of the same color
    int i;
    int row = button.getRow(); // stores the row number of button
    int col = button.getColumn(); // stores the column number of button
    /* loop goal: checks for buttons to the right of button with the same color as button and increments rowLength */
    for (i = col + 1; i < width && buttons[row][i].getColor() == button.getColor(); i++) {
      rowLength++;
    }
    /* loop goal: checks for buttons with the same color as button to the left of button and increments rowLength */
    for (i = col - 1; i >= 0 && buttons[row][i].getColor() == button.getColor(); i--) {
      rowLength++;
    }
    if (rowLength >= 3)
      return buttons[row][i+1];
    return null;
  }
  
  /**
   * determines if button is in a column of 3 or more of the same color
   * @param button the button to check for a column of the same color
   * @return the start of the column, or null if there is no column
   */
  public JewelButton colStart(JewelButton button) {
    int colLength = 1; // the current length of the column of the same color
    int i;
    int row = button.getRow(); // stores the row number of button
    int col = button.getColumn(); // stores the column number of button
    /* loop goal: checks for buttons below button with the same color and increments colLength */
    for (i = row + 1; i < width && buttons[i][col].getColor() == button.getColor(); i++) {
      colLength++;
    }
    /* loop goal: checks for buttons above button with the same color, increments colLength */
    for (i = row - 1; i >= 0 && buttons[i][col].getColor() == button.getColor(); i--) {
      colLength++;
    }
    if (colLength >= 3)
      return buttons[i+1][col];
    return null;
  }
  
  
  /**
   * marks a row of the same color for removal
   * @param start the start of the row of buttons
   */
  public void markRow(JewelButton start) {
    if (start != null) {
      int row = start.getRow(); // the row number of start
      int col = start.getColumn(); // the column number of start
      /* loop goal: marks a row of buttons with the same color for removal, starting with the button passed as a parameter */
      for (int i = col; i < width && buttons[row][i].getColor() == start.getColor(); i++) {
        buttons[row][i].setNeedsRemoved(true);
      }
    }
  }
  
  /**
   * marks a column of the same color for removal
   * @param start the start of the column of buttons
   */
  public void markCol(JewelButton start) {
    if (start != null) {
      int row = start.getRow(); // the row number of start
      int col = start.getColumn(); // the column number of start
      /* loop goal: marks a column of buttons with the same color for removal, starting with the button passed as a parameter */
      for (int i = row; i < height && buttons[i][col].getColor() == start.getColor(); i++) {
        buttons[i][col].setNeedsRemoved(true);
      }
    }
  }
  
  /**
   * removes every button that is marked for removal
   */
  public void removeMarked() {
    /* loop searches through the array of buttons and checks if a button needs to be removed */
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (buttons[i][j].getNeedsRemoved()) {
          /* loop goal: starts at the button to be removed and swaps every button's color with the button below it */
          for (int r = i; r > 0; r--) {
            swapColors(buttons[r][j], buttons[r-1][j]);
          }
          buttons[0][j].setColor(AVAILABLECOLORS[(int)(Math.random()*numJewels)]);
          buttons[i][j].setNeedsRemoved(false);
        }
      }
    }
  }
  
  /**
   * checks for any rows of the same color that have been created by removing marked buttons
   * @return true if new rows have been created
   */
  public boolean checkRowsAndCols() {
    boolean remove = false; // whether buttons need removed
    /* loop goal: checks if any new rows or columns of 3 of the same color have been created by removeMarked() */
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (!buttons[i][j].getNeedsRemoved()) {
          markRow(rowStart(buttons[i][j]));
          markCol(colStart(buttons[i][j]));
          if (buttons[i][j].getNeedsRemoved())
            remove = true;
        }
      }
    }
    return remove;
  }
  
  /**
   * checks if all of the buttons have been removed at some point
   * @return true if all buttons have been removed
   */
  public boolean checkWin() {
    /* loop goal: checks if the player has won by verifying that every button has been removed as some point */
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        if (!buttons[i][j].getWasRemoved())
          return false;
      }
    }
    return true;
  }
  
  /**
   * repaints the game board
   */
  public void repaintFrame() {
    this.repaint();
  }
  
  /**
   * checks if a valid height, width and number of jewels have been passed as arguments and creates a new jewels game.
   * calls the default constructor if 0-2 arguments have been passed
   */
  public static void main(String[] args) {
    try {
      if (args.length >= 3) {
        int height = Integer.parseInt(args[0]); // height of the game board
        int width = Integer.parseInt(args[1]); // width of the game board
        int numJewels = Integer.parseInt(args[2]); // number of jewel used in the game
        if (height < MINDIMENSION || height > MAXDIMENSION)
          JOptionPane.showMessageDialog(null, "The height must be between 5 and 16.");
        else if (width < MINDIMENSION || width > MAXDIMENSION)
          JOptionPane.showMessageDialog(null, "The width must be between 5 and 16.");
        else if (numJewels < MINJEWELS || numJewels > MAXJEWELS)
          JOptionPane.showMessageDialog(null, "The number of jewels must be between 2 and 8.");
        else
          new Jewels(height, width, numJewels);
      }
      else
        new Jewels();
    }
    catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(null, "Please only enter numbers for height, width and number of jewels.");
    }
  }
}