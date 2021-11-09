package edu.kingsu.SoftwareEngineering.Chess.GUI;

import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.*;
import javax.swing.JLayeredPane;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * MainLayer creates the JPanel that provides the color of the background for
 * gameplay mode and holds all of the gameplay components.
 * 
 * @author Chelsie Bajic
 */
public class MainLayer extends JPanel {

    /**
     * Overrides JPanel's paintComponent to allow for gradient paint.
     * 
     * @param g Graphics object to build gradient on.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, new Color(16, 46, 60), 0, h, new Color(37, 108, 141));

        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);

        g2d.dispose();
    }

}