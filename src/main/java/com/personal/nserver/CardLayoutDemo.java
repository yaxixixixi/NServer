package com.personal.nserver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class CardLayoutDemo {

    final static String BUTTONPANEL = "Card with JButtons";
    final static String TEXTPANEL = "Card with JTextField";
    //Where instance variables are declared:
    JPanel cards;
    //Where the components controlled by the CardLayout are initialized:
//Create the "cards".
    JPanel card1 = new JPanel();
    JPanel card2 = new JPanel();

    private void createUi() {
        JFrame f = new JFrame();
        Container container = f.getContentPane();


        card1.setBackground(Color.red);
        card2.setBackground(Color.yellow);

//Create the panel that contains the "cards".
        cards = new JPanel(new CardLayout());

        cards.add(card1, BUTTONPANEL);
        cards.add(card2, TEXTPANEL);

        //Where the GUI is assembled:
//Put the JComboBox in a JPanel to get a nicer look.
        JPanel comboBoxPane = new JPanel(); //use FlowLayout
        String comboBoxItems[] = { BUTTONPANEL, TEXTPANEL };
        JComboBox cb = new JComboBox(comboBoxItems);
        cb.setEditable(false);
        cb.addItemListener(evt -> {
            CardLayout cl = (CardLayout)(cards.getLayout());
            cl.show(cards, (String)evt.getItem());
        });
        comboBoxPane.add(cb);
        container.add(comboBoxPane, BorderLayout.PAGE_START);
        container.add(cards, BorderLayout.CENTER);



        f.setSize(500, 500);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }



    public static void main(String[] a ){
        CardLayoutDemo demo = new CardLayoutDemo();
        demo.createUi();
    }
}
