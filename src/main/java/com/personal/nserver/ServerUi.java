package com.personal.nserver;

import io.netty.channel.ChannelHandlerContext;

import javax.swing.*;
import java.awt.*;

public class ServerUi implements OnNewEventListener {


    public static final String SERVER = "server";
    private JTextField portField, timeoutField;
    private JButton connectBtn;
    private TextArea sendMsg;
    private JButton clearMsg;
    private JButton sendToClient;
    private TextArea msgInfo;
    private JButton clearInfo;
    private JPanel clientLayout;
    private JPanel container;
    private JPanel cardPanel;
    private JComboBox<Object> comboBox;
    private DefaultComboBoxModel<Object> comboBoxModel;
    private CardLayout cardLayout;

    public static void main(String[] a) {
        ServerUi serverUi = new ServerUi();
        serverUi.createAndShowUi();
    }


    private void createAndShowUi() {
        JFrame frame = new JFrame();

        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));

        JLabel portLabel = new JLabel("端口号:");
        portField = new JTextField("8080");
        portField.setMaximumSize(new Dimension(60, 20));
        portField.setPreferredSize(new Dimension(60, 20));
        portField.setMinimumSize(new Dimension(60, 20));
        connectBtn = new JButton("开启服务");
        JLabel timeoutLabel = new JLabel("读超时(s):");
        timeoutField = new JTextField("5");
        timeoutField.setMaximumSize(new Dimension(60, 20));
        timeoutField.setPreferredSize(new Dimension(60, 20));
        timeoutField.setMinimumSize(new Dimension(60, 20));

        p1.add(portLabel);
        p1.add(Box.createHorizontalStrut(10));
        p1.add(portField);
        p1.add(Box.createHorizontalStrut(10));
        p1.add(timeoutLabel);
        p1.add(Box.createHorizontalStrut(10));
        p1.add(timeoutField);
        p1.add(Box.createHorizontalStrut(10));
        p1.add(connectBtn);
        p1.add(Box.createHorizontalGlue());

        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.LEFT);
        clientLayout = new JPanel(layout);

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));

        sendMsg = new TextArea();
        clearMsg = new JButton("clearMsg");
        sendToClient = new JButton("send to client");
        sendToClient.setEnabled(false);
        sendMsg.setMaximumSize(new Dimension(700, 60));
        sendMsg.setPreferredSize(new Dimension(500, 60));
        sendMsg.setMinimumSize(new Dimension(200, 60));
//        sendMsg.setSelectionColor(Color.yellow);
//        sendMsg.setLineWrap(true);
//        sendMsg.set


        p2.add(sendMsg);
        p2.add(Box.createHorizontalGlue());
        p2.add(clearMsg);
        p2.add(Box.createHorizontalStrut(10));
        p2.add(sendToClient);

        JLabel p3 = new JLabel("----------------state info----------------");

        comboBox = new JComboBox<>();
        comboBoxModel = new DefaultComboBoxModel<>();
        comboBoxModel.addElement(SERVER);
        comboBox.setModel(comboBoxModel);

        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);
        cardPanel.setSize(new Dimension(800, 400));

        msgInfo = new TextArea();
        msgInfo.setEditable(false);

        comboBox.addItemListener(e -> {
            Object item = e.getItem();
            if (item instanceof ChannelInfo) {
                String clientInfo = ((ChannelInfo) item).getPhoneModel();
                cardLayout.show(cardPanel, clientInfo);
            } else if (item instanceof String) {
                cardLayout.show(cardPanel, ((String) item));
            }
            cardPanel.revalidate();
        });

        cardPanel.add(msgInfo, SERVER);

        clearInfo = new JButton("clearMsg sendMsg");


        container.add(p1);
        container.add(clientLayout);
        container.add(p2);
        container.add(p3);
        container.add(comboBox);
        container.add(cardPanel);
        container.add(clearInfo);
        container.add(Box.createVerticalGlue());

        frame.setContentPane(container);

        frame.setSize(800, 600);
        frame.setVisible(true);

        init();
    }

    private void init() {
        connectBtn.addActionListener(e -> new Thread(() -> {
            connectBtn.setEnabled(false);
            EchoServer echoServer = new EchoServer();
            echoServer.setCallback(ServerUi.this);
            echoServer.setReadTimeout(Long.parseLong(timeoutField.getText()));
            echoServer.startEchoServer(Integer.parseInt(portField.getText()));
        }).start());

        clearMsg.addActionListener(e -> sendMsg.setText(""));

        sendToClient.addActionListener(e -> {
            sendMsgToAllClient(true);
        });

        clearInfo.addActionListener(e -> msgInfo.setText(""));


    }


    private void sendMsgToAllClient(boolean selectedOnly) {
        for (Component component : clientLayout.getComponents()) {
            ChannelInfo info = ((MyBox) component).getInfo();
            if (selectedOnly) {
                if (((MyBox) component).isSelected())
                    info.getCtx().writeAndFlush(sendMsg.getText());
                continue;
            }
            info.getCtx().writeAndFlush(sendMsg.getText());
        }
    }

    @Override
    public void onMessage(ChannelHandlerContext ctx, String msg) {
        for (Component component : cardPanel.getComponents()) {
            if (component instanceof MyTextArea) {
                ChannelInfo info = ((MyTextArea) component).getInfo();
                if (info.getCtx() == ctx) {
                    String clientInfo = info.getPhoneModel();
                    String prefix = clientInfo.length() == 0 ? "server" : clientInfo;
                    appendInfo((MyTextArea) component, prefix + ":" + msg);
                    break;
                }
            }
        }
    }

    private void appendInfo(MyTextArea c, String msg) {
        appendInfo(msg);
        c.append(msg);
        c.append("\n");
    }

    private void appendInfo(String msg) {
        msgInfo.append(msg);
        msgInfo.append("\n");
    }

    @Override
    public void onServerFilure(Exception e, String errorMsg) {
//        connectBtn.setText("连接");
        appendInfo(errorMsg);
    }

    @Override
    public void onServerSuccess(String errorMsg) {
//        connectBtn.setText("断开");
        appendInfo(errorMsg);
    }

    @Override
    public void onChannelError(Exception e, String errorMsg) {
        appendInfo(errorMsg);
    }


    @Override
    public void onChannelsChanged(ChannelInfo info, Operate operate) {
        if (operate == Operate.ADD) {
            clientLayout.add(new MyBox(info, true));
        } else {
            for (Component component : clientLayout.getComponents()) {
                if (((MyBox) component).getInfo().getCtx() == info.getCtx()) {
                    if (operate == Operate.UPDATE) {
                        ((MyBox) component).setInfo(info);
                    } else {
                        clientLayout.remove(component);
                    }
                }
            }
            if (operate == Operate.UPDATE) {
                comboBoxModel.addElement(info);
                cardPanel.add(new MyTextArea(info), info.getPhoneModel());
            } else {
                comboBoxModel.removeElement(info);
                for (Component component : cardPanel.getComponents()) {
                    if (component instanceof MyTextArea && ((MyTextArea) component).getInfo().getCtx() == info.getCtx()) {
                        cardPanel.remove(component);
                        break;
                    }
                }
            }
        }
        sendToClient.setEnabled(clientLayout.getComponentCount() > 0);
        clientLayout.revalidate();
    }


    class MyBox extends JCheckBox {
        private ChannelInfo info;

        public MyBox(ChannelInfo info, boolean selected) {
            super(info.getPhoneModel(), selected);
            this.info = info;
        }

        public ChannelInfo getInfo() {
            return info;
        }

        public void setInfo(ChannelInfo info) {
            this.info = info;
            setText(info.getPhoneModel());
        }
    }


    class MyTextArea extends TextArea {
        private ChannelInfo info;

        public MyTextArea(ChannelInfo info) throws HeadlessException {
            this.info = info;
        }

        public ChannelInfo getInfo() {
            return info;
        }

        public void setInfo(ChannelInfo info) {
            this.info = info;
        }
    }
}
