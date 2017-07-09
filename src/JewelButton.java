package src;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JButton;

/**
 * A tile class for the Jewels game.
 */
public class JewelButton extends JButton {
	private static final long serialVersionUID = 1L;

	/** the color of the button */
	private Color color;

	/** whether the button should be removed by removeMarked() */
	private boolean needsRemoved;

	/** whether the button has been removed at some point */
	private boolean wasRemoved;
	
	/** whether the button is currently selected */
	private boolean selected;

	/** the row the button is in */
	private int row;

	/** the column the button is in */
	private int column;

	/**
	 * constructor: sets the color of the button and adds the actionListener
	 * @param color the color of the button
	 */
	public JewelButton(int row, int column) {
		this.row = row;
		this.column = column;
		boundSize(new Dimension(50, 50));
	}
	
	private void boundSize(Dimension dims) {
		setPreferredSize(dims);
		setMaximumSize(dims);
		setMinimumSize(dims);
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
	 * Set whether the tile should paint as selected.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * Paints the tile. Outlines the tile if it is selected, gives it a yellow background if
	 * it has been removed and then paints the jewel on the button.
	 * @param g the Graphics object associated with this component
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int width = getWidth();
		int height = getHeight();
		
		int[] xCoords = {width / 2, width - 10, width / 2, 10};
		int[] yCoords = {10, height / 2, height - 10, height / 2};
		
		g.setColor(wasRemoved ? Color.yellow : Color.white);
		if (selected) {
			setBackground(Color.gray);
			g.fillRect(4, 4, width - 8, height - 8);
		} else {
			g.fillRect(0, 0, width, height);
		}
		
		g.setColor(color);
		g.fillPolygon(xCoords, yCoords, 4);
		g.setColor(color.darker());
		g.drawPolygon(xCoords, yCoords, 4);
	}
	
	/**
	 * determines if this button is adjacent to another
	 * @param other the second button
	 * @return true if the buttons are adjacent
	 */
	public boolean adjacentTo(JewelButton other) {
		int dX = Math.abs(getColumn() - other.getColumn());
		int dY = Math.abs(getRow() - other.getRow());
		return (dX == 1 && dY == 0) || (dY == 1 && dX == 0);
	}

	/**
	 * swaps the color of this button with another
	 * @param other the second button
	 */
	public void swapColors(JewelButton other) {
		Color temp = getColor();
		setColor(other.getColor());
		other.setColor(temp);
	}
	
	/**
	 * Checks if this button has the same color as another.
	 */
	public boolean isSameColor(JewelButton other) {
		return getColor().equals(other.getColor());
	}
}