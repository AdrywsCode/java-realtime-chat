package client.gui;

import client.ChatConnection;
import common.Protocol;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.DefaultListModel;

public class ChatWindow extends JFrame {
    private final JTextArea chatArea = new JTextArea();
    private final JTextField inputField = new JTextField();
    private final JButton sendButton = new JButton("Enviar");

    private final JTextField hostField = new JTextField("localhost");
    private final JTextField portField = new JTextField("5000");
    private final JTextField nickField = new JTextField();
    private final JButton connectButton = new JButton("Conectar");
    private final JLabel statusLabel = new JLabel("Desconectado");
    private final JComboBox<String> roomSelector = new JComboBox<>();
    private final JButton clearButton = new JButton("Limpar");
    private final DefaultListModel<String> userListModel = new DefaultListModel<>();
    private final JList<String> userList = new JList<>(userListModel);
    private final Map<String, DefaultListModel<String>> roomUsers = new HashMap<>();

    private final Map<String, StringBuilder> roomHistory = new HashMap<>();
    private String currentRoom = "lobby";
    private String serverRoom = "lobby";
    private Timer whoTimer;

    private ChatConnection connection;
    private boolean connected;

    public ChatWindow() {
        super("Chat");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(640, 420));
        setLayout(new BorderLayout(8, 8));

        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        JPanel fields = new JPanel(new BorderLayout(8, 8));
        JPanel leftFields = new JPanel(new BorderLayout(8, 8));
        JPanel rightFields = new JPanel(new BorderLayout(8, 8));

        leftFields.add(labeled("Host", hostField), BorderLayout.WEST);
        leftFields.add(labeled("Porta", portField), BorderLayout.CENTER);
        rightFields.add(labeled("Nick", nickField), BorderLayout.CENTER);
        rightFields.add(connectButton, BorderLayout.EAST);

        fields.add(leftFields, BorderLayout.WEST);
        fields.add(rightFields, BorderLayout.CENTER);
        top.add(fields, BorderLayout.CENTER);
        top.add(statusLabel, BorderLayout.EAST);

        JPanel roomPanel = new JPanel(new BorderLayout(8, 8));
        roomPanel.add(labeled("Sala", roomSelector), BorderLayout.CENTER);
        roomPanel.add(clearButton, BorderLayout.EAST);
        top.add(roomPanel, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(inputField, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.add(new JLabel("Usuarios"), BorderLayout.NORTH);
        right.add(new JScrollPane(userList), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        roomSelector.addItem(currentRoom);
        roomHistory.put(currentRoom, new StringBuilder());
        roomUsers.put(currentRoom, userListModel);
        setInputEnabled(false);

        connectButton.addActionListener(e -> connect());
        sendButton.addActionListener(e -> sendCurrentInput());
        inputField.addActionListener(e -> sendCurrentInput());
        roomSelector.addActionListener(e -> switchRoom((String) roomSelector.getSelectedItem()));
        clearButton.addActionListener(e -> clearCurrentRoom());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                disconnect();
            }
        });
    }

    private JPanel labeled(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.add(new JLabel(label), BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void setInputEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
        sendButton.setEnabled(enabled);
        roomSelector.setEnabled(enabled);
        clearButton.setEnabled(enabled);
    }

    private void connect() {
        if (connected) return;

        String host = hostField.getText().trim();
        String portText = portField.getText().trim();
        String nick = nickField.getText().trim();

        if (host.isEmpty() || portText.isEmpty() || nick.isEmpty()) {
            appendLine("Servidor: preencha host, porta e nick.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException ex) {
            appendLine("Servidor: porta invalida.");
            return;
        }

        try {
            connection = new ChatConnection();
            connection.connect(host, port, this::appendLine);
            connection.sendLine(Protocol.NICK + nick);
            connected = true;
            statusLabel.setText("Conectado");
            setInputEnabled(true);
            ensureRoom("lobby");
            ensureUserRoom("lobby");
            serverRoom = "lobby";
            switchRoom("lobby");
            startWhoTimer();
            inputField.requestFocusInWindow();
        } catch (IOException ex) {
            appendLine("Servidor: falha ao conectar.");
        }
    }

    private void disconnect() {
        if (!connected || connection == null) return;
        try {
            connection.close();
        } catch (IOException ignored) {
        } finally {
            connected = false;
            statusLabel.setText("Desconectado");
            setInputEnabled(false);
            stopWhoTimer();
        }
    }

    private void sendCurrentInput() {
        if (!connected || connection == null) return;
        String msg = inputField.getText().trim();
        if (msg.isEmpty()) return;

        if (msg.equalsIgnoreCase("/quit")) {
            connection.sendLine(Protocol.QUIT);
            disconnect();
            return;
        }

        if (msg.startsWith("/join ")) {
            String room = msg.substring(6).trim();
            connection.sendLine(Protocol.JOIN + room);
            if (!room.isEmpty()) {
                ensureRoom(room);
                switchRoom(room);
            }
        } else if (msg.equalsIgnoreCase("/who")) {
            connection.sendLine(Protocol.WHO);
        } else if (msg.startsWith("/pm ")) {
            String[] parts = msg.split(" ", 3);
            if (parts.length == 3) {
                connection.sendLine(Protocol.PM + parts[1] + ":" + parts[2]);
            } else {
                appendLine("Servidor: use /pm nick mensagem");
            }
        } else {
            connection.sendLine(Protocol.MSG + msg);
        }

        inputField.setText("");
        inputField.requestFocusInWindow();
    }

    private void appendLine(String line) {
        SwingUtilities.invokeLater(() -> {
            updateServerRoomFromLine(line);
            updateUserListFromLine(line);
            String room = extractRoom(line);
            addLineToRoom(room, line);
        });
    }

    private void ensureRoom(String room) {
        String normalized = normalizeRoom(room);
        if (!roomHistory.containsKey(normalized)) {
            roomHistory.put(normalized, new StringBuilder());
            roomSelector.addItem(normalized);
        }
    }

    private void ensureUserRoom(String room) {
        String normalized = normalizeRoom(room);
        if (!roomUsers.containsKey(normalized)) {
            roomUsers.put(normalized, new DefaultListModel<>());
        }
    }

    private void switchRoom(String room) {
        if (room == null) return;
        String normalized = normalizeRoom(room);
        currentRoom = normalized;
        StringBuilder sb = roomHistory.getOrDefault(normalized, new StringBuilder());
        chatArea.setText(sb.toString());
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
        DefaultListModel<String> model = roomUsers.getOrDefault(normalized, new DefaultListModel<>());
        roomUsers.putIfAbsent(normalized, model);
        userList.setModel(model);
    }

    private void addLineToRoom(String room, String line) {
        String normalized = normalizeRoom(room);
        StringBuilder sb = roomHistory.computeIfAbsent(normalized, key -> {
            roomSelector.addItem(key);
            return new StringBuilder();
        });
        sb.append(line).append(System.lineSeparator());
        if (normalized.equals(currentRoom)) {
            chatArea.append(line + System.lineSeparator());
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    private void clearCurrentRoom() {
        StringBuilder sb = roomHistory.get(currentRoom);
        if (sb != null) sb.setLength(0);
        chatArea.setText("");
    }

    private String extractRoom(String line) {
        if (line != null && line.startsWith("[")) {
            int idx = line.indexOf(']');
            if (idx > 1) return line.substring(1, idx);
        }
        return "geral";
    }

    private String normalizeRoom(String room) {
        if (room == null) return "geral";
        String trimmed = room.trim();
        return trimmed.isEmpty() ? "geral" : trimmed;
    }

    private void updateServerRoomFromLine(String line) {
        String prefix = "Servidor: agora voce esta na sala ";
        if (line != null && line.startsWith(prefix)) {
            String room = line.substring(prefix.length()).trim();
            if (!room.isEmpty()) {
                serverRoom = room;
                ensureRoom(room);
                ensureUserRoom(room);
                roomSelector.setSelectedItem(room);
            }
            return;
        }

        String okPrefix = "Servidor: OK! Sala atual = ";
        if (line != null && line.startsWith(okPrefix)) {
            String room = line.substring(okPrefix.length());
            int idx = room.indexOf('.');
            if (idx > 0) room = room.substring(0, idx);
            room = room.trim();
            if (!room.isEmpty()) {
                serverRoom = room;
                ensureRoom(room);
                ensureUserRoom(room);
            }
        }
    }

    private void updateUserListFromLine(String line) {
        String prefix = "Servidor: usuarios na sala '";
        if (line == null || !line.startsWith(prefix)) return;
        int endRoom = line.indexOf("': ");
        if (endRoom < 0) return;
        String room = line.substring(prefix.length(), endRoom);
        String listPart = line.substring(endRoom + 3).trim();

        ensureUserRoom(room);
        DefaultListModel<String> model = roomUsers.get(room);
        model.clear();
        if (!listPart.isEmpty()) {
            List<String> names = List.of(listPart.split(", "));
            for (String name : names) {
                if (!name.isBlank()) model.addElement(name.trim());
            }
        }
    }

    private void startWhoTimer() {
        if (whoTimer != null) {
            whoTimer.stop();
        }
        whoTimer = new Timer(5000, e -> {
            if (connected && connection != null) {
                connection.sendLine(Protocol.WHO);
            }
        });
        whoTimer.setInitialDelay(0);
        whoTimer.start();
    }

    private void stopWhoTimer() {
        if (whoTimer != null) {
            whoTimer.stop();
            whoTimer = null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatWindow window = new ChatWindow();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        });
    }
}
