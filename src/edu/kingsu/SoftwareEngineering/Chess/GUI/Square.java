package edu.kingsu.SoftwareEngineering.Chess.GUI;

import java.awt.Color;
import javax.swing.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.awt.Cursor;

public class Square extends JButton implements MouseListener {

    String location; // The location of this square within the 2D array (squareHolderArray[][]).
    boolean color; // White = true, Black = false

    /**
     * The constructor for Square class. Adds mouse listener to each square and
     * determines what color it is going to be based on its position on the board.
     * 
     * @param location The location of this square within the 2D array that holds
     *                 the board representation (squareHolderArray[][], found in the
     *                 ChessGameGUIView class).
     * @param color    The color of this square. true == White, false == Balck.
     */
    public Square(String location, boolean color) {
        addMouseListener(this);
        this.color = color;
        this.location = location;
        if (color == false) {
            this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(80, 80, 80)));
        } else {
            this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(232, 232, 232)));
        }

    }

    @Override
    public void paintComponent(Graphics g) {
        if (color == false) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, new Color(80, 80, 80), 0, h, new Color(128, 128, 128));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
            g2d.dispose();
        } else {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, new Color(232, 232, 232), 0, h, new Color(248, 248, 248));
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
            g2d.dispose();
        }
    }

    public void mouseClicked(MouseEvent e) {

    }

    /**
     * Changes the border color to blue, to highlight it when the mouse hovers over
     * it.
     */
    public void mouseEntered(MouseEvent e) {
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(49, 209, 245)));
    }

    /**
     * When the mouse leaves the square, changes it back to its original border
     * color/
     */
    public void mouseExited(MouseEvent e) {
        if (color == false) {
            this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(80, 80, 80)));
        } else {
            this.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(232, 232, 232)));
        }

    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {
        System.out.println("Location: " + location);

    }
}