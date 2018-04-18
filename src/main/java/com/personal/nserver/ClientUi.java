package com.personal.nserver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientUi implements ActionListener {

    private JTextField field;
    private JLabel prompt;

    public static void main(String[] a) {
        ClientUi ui = new ClientUi();
        ui.createUi();
    }

    private void createUi() {
        JFrame f = new JFrame();
        Container container = f.getContentPane();
        container.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel label = new JLabel("标识型号");
        field = new JTextField();
        field.setPreferredSize(new Dimension(200, 20));
        JButton connect = new JButton("连接服务");


        prompt = new JLabel();


        container.add(label);
        container.add(field);
        container.add(connect);
        container.add(prompt);

        connect.addActionListener(this);

        f.setSize(500, 500);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EchoClient client = new EchoClient();
        String text = field.getText();
        if (text.length() == 0) {
            prompt.setText("请输入合法字符");
            return;
        } else {
            prompt.setText("");
        }
        client.setText(text);
        new Thread(() -> {
            try {
                client.connect("127.0.0.1", 8080);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }).start();
    }
}
