package edu.kingsu.SoftwareEngineering.Chess.GUI;

import javax.swing.*;
import java.awt.*;

public class ApplicationFrame extends JFrame {

    private static final String WINDOW_TITLE = "Chess Game";
    private static final Dimension WINDOW_SIZE = new Dimension(1200, 900);

    private JPanel contentPanel;
    private CardLayout layout;
    private MainMenu mainMenu;
    private GameSetUp gameSetUp;

    public ApplicationFrame() {
        super(WINDOW_TITLE);
        contentPanel = new JPanel();
        add(contentPanel);
        layout = new CardLayout();
        mainMenu = new MainMenu(this);
        gameSetUp = new GameSetUp(this);

        contentPanel.setLayout(layout);

        //Add cards test
        contentPanel.add(mainMenu, "menu");
        contentPanel.add(gameSetUp, "gamesetup");        

        layout.show(contentPanel, "menu");

        setPreferredSize(WINDOW_SIZE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);


        pack();
        setVisible(true);
    }
    public void test(){
        // layout.show(contentPanel,"gamesetup");
        System.out.println("TEST");
    }
    public void show(String card){
        layout.show(contentPanel,card);
    }
}


