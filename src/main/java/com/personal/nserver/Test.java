package com.personal.nserver;


import io.netty.handler.codec.string.StringDecoder;
import jdk.nashorn.internal.ir.IfNode;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class Test {


    public static void main(String[] args) {


        new Test().createAndShowUi();

//        Test test = new Test();
//        Observable observable = test.new Observable();
//
//
//        observable.update("hello wwy", System.out::print);
    }

    private void createAndShowUi() {

        JFrame frame = new JFrame("login");
//        frame.setBounds(0,0,355,265);
        frame.setBounds(0, 0, 1024, 800);
        Container contentPanel = frame.getContentPane();
        contentPanel.setLayout(null);
        JTree jTree = new JTree();
        jTree.setBounds(0, 0, 500, 500);
        jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        DefaultMutableTreeNode union1 = new DefaultMutableTreeNode(new Bean("union1", false));
        DefaultMutableTreeNode graph1 = new DefaultMutableTreeNode(new Bean("graph1", false));

        graph1.add(new DefaultMutableTreeNode(new Bean("chapter1", false)));
        graph1.add(new DefaultMutableTreeNode(new Bean("chapter2", false)));
        graph1.add(new DefaultMutableTreeNode(new Bean("chapter3", true)));
        graph1.add(new DefaultMutableTreeNode(new Bean("chapter4", true)));
        graph1.add(new DefaultMutableTreeNode(new Bean("chapter5", true)));
        graph1.add(new DefaultMutableTreeNode(new Bean("chapter6", true)));

        union1.add(graph1);
        DefaultTreeModel model = new DefaultTreeModel(union1);
        jTree.setModel(model);
        TRenderer renderer = new TRenderer();
        renderer.setClosedIcon(new ImageIcon("image/unfold.png"));
        renderer.setOpenIcon(new ImageIcon("image/pack_up.png"));
        jTree.setCellRenderer(renderer);

        jTree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                JTree source = (JTree) e.getSource();
                TreePath selectionPath = source.getSelectionPath();
//                TreeSelectionModel selectionModel = jTree.getSelectionModel();
//                boolean pathSelected = selectionModel.isPathSelected(selectionPath);
//                if (pathSelected)selectionModel.clearSelection();
//                else selectionModel.addSelectionPath(selectionPath);
//                jTree.setSelectionModel(selectionModel);
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
                Bean bean = (Bean) node.getUserObject();
                bean.setState(!bean.state);
                jTree.updateUI();

            }
        });

        contentPanel.add(jTree);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    class TRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (sel)setLeafIcon(new ImageIcon("image/cam.png"));else setLeafIcon(new ImageIcon("image/cam2.png"));
            Bean bean = (Bean) ((DefaultMutableTreeNode) value).getUserObject();
            boolean state = bean.state;
            if (!leaf)return this;
//            System.out.println(bean.toString() + state);
            if (state) {
                setIcon(new ImageIcon("image/cam.png"));
                setText("cam");
            } else {
                setIcon(new ImageIcon("image/cam2.png"));
                setText("cam2");
            }
            return this;
        }
    }


    class Observable {
        public void update(String event, ThingListener l) {
            l.onThing(event);
        }
    }

    @FunctionalInterface
    interface ThingListener {
        void onThing(String s);
    }

    class Bean {
        private String name;
        private boolean state;

        public Bean(String name, boolean state) {
            this.name = name;
            this.state = state;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isState() {
            return state;
        }

        public void setState(boolean state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return name;
        }
    }


}
