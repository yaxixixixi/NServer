package com.personal.nserver;

import javax.swing.*;
import java.awt.*;

public class DynamicAddComp {

    public static void main(String[] a){
        DynamicAddComp dynamicAddComp = new DynamicAddComp();
        dynamicAddComp.createUi();
    }



    private void createUi(){
        JFrame frame = new JFrame("Dynamic Add Component");
        JButton add = new JButton("add");
//        add.setSize(50,20);
        Container contentPane = frame.getContentPane();
        FlowLayout mgr = new FlowLayout();
        mgr.setAlignment(FlowLayout.LEFT);
        contentPane.setLayout(mgr);

        add.addActionListener(e ->{
            JCheckBox label = new JCheckBox("test box");
//            JLabel label = new JLabel("test label");
//            label.setSize(50,20);
            contentPane.add(label);
            contentPane.revalidate();
        });
        contentPane.add(add);
        frame.setSize(1000,500);
        frame.setVisible(true);
    }
}
