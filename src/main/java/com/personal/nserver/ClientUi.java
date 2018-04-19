package com.personal.nserver;

import javax.swing.*;
import java.awt.*;

public class ClientUi{

    private JTextField field;
    private JLabel prompt;

    public static void main(String[] a) {
        ClientUi ui = new ClientUi();
        ui.createUi();
    }

    private void createUi() {
        JFrame f = new JFrame();
        Container container = f.getContentPane();
        container.setLayout(new BorderLayout());


        JPanel paramPanel = new JPanel();
        paramPanel.setLayout(new BoxLayout(paramPanel,BoxLayout.Y_AXIS));

        JLabel label1 = new JLabel("型号:phoneModel");
        JTextField field1 = new JTextField("model-number1");
        JLabel label2 = new JLabel("序列号:serialNumber");
        JTextField field2 = new JTextField("serial-number1");
        JLabel label3 = new JLabel("工号:jobNember");
        JTextField field3 = new JTextField("job-nember1");

        field1.setMaximumSize(new Dimension(300,30));
        field2.setMaximumSize(new Dimension(300,30));
        field3.setMaximumSize(new Dimension(300,30));

        JButton connect = new JButton("connect to server");

        paramPanel.add(label1);
        paramPanel.add(field1);
        paramPanel.add(label2);
        paramPanel.add(field2);
        paramPanel.add(label3);
        paramPanel.add(field3);
        paramPanel.add(Box.createVerticalStrut(50));
        paramPanel.add(connect);
        paramPanel.add(Box.createVerticalGlue());

        container.add(paramPanel,BorderLayout.WEST);

        connect.addActionListener(e -> {
            ChannelInfo channelInfo = new ChannelInfo();
            channelInfo.setPhoneModel(field1.getText());
            channelInfo.setSerialNumber(field2.getText());
            channelInfo.setJobNember(field3.getText());

            EchoClient client = new EchoClient();
            client.setClientInfo(channelInfo);
            new Thread(() -> {
                try {
                    client.connect("127.0.0.1", 8080);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }).start();
        });

        f.setSize(500, 500);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

}
