package Client;

import Connection.Message;
import Connection.MessageType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Set;

public class ViewGuiClient {
    private final Client client;
    private JFrame frame = new JFrame("Чат");
    private JTextArea messages = new JTextArea(30, 20);
    private JTextArea users = new JTextArea(30, 15);
    private JPanel panel = new JPanel();
    private JTextField textField = new JTextField(40);
    private JButton buttonDisable = new JButton("Отключиться");
    private JButton buttonConnect = new JButton("Подключиться");

    public ViewGuiClient(Client client) {
        this.client = client;
    }

    //метод, инициализирующий графический интерфейс клиентского приложения
    protected void initFrameClient() {
        messages.setEditable(false);
        users.setEditable(false);
        frame.add(new JScrollPane(messages), BorderLayout.CENTER);
        frame.add(new JScrollPane(users), BorderLayout.EAST);
        panel.add(textField);
        panel.add(buttonConnect);
        panel.add(buttonDisable);
        frame.add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null); // при запуске отображает окно по центру экрана
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //класс обработки события при закрытии окна приложения Сервера
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client.isConnect()) {
                    client.disableClient();
                }
                System.exit(0);
            }
        });
        frame.setVisible(true);
        buttonDisable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.disableClient();
            }
        });
        buttonConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.connectToServer();
            }
        });
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.sendMessageOnServer(textField.getText());
                textField.setText("");
            }
        });
    }

    protected void addMessage(String text) {
        messages.append(text);
    }

    //метод обновляющий списо имен подлючившихся пользователей
    protected void refreshListUsers(Set<String> listUsers) {
        users.setText("");
        if (client.isConnect()) {
            StringBuilder text = new StringBuilder("Список пользователей:\n");
            for (String user : listUsers) {
                text.append(user + "\n");
            }
            users.append(text.toString());
        }
    }

    //вызывает окно для ввода адреса сервера
    protected String getServerAddressFromOptionPane() {
        while (true) {
            String addressServer = JOptionPane.showInputDialog(
                    frame, "Введите адрес сервера:",
                    "Ввод адреса сервера",
                    JOptionPane.QUESTION_MESSAGE
            );
            return addressServer.trim();
        }
    }

    //вызывает окно для ввода порта сервера
    protected int getPortServerFromOptionPane() {
        while (true) {
            String port = JOptionPane.showInputDialog(
                    frame, "Введите порт сервера:",
                    "Ввод порта сервера",
                    JOptionPane.QUESTION_MESSAGE
            );
            try {
                return Integer.parseInt(port.trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        frame, "Введен неккоректный порт сервера. Попробуйте еще раз.",
                        "Ошибка ввода порта сервера", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    protected void reg() throws IOException, ClassNotFoundException {
        JTextField username = new JTextField();
        JTextField password = new JPasswordField();
        JTextField password1 = new JPasswordField();
        Object[] message = {
                "Имя пользователя:", username,
                "Пароль:", password,
                "Повторите пароль:", password1,
        };


        int option = JOptionPane.showConfirmDialog(null, message, "Регистрация", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {

            if(!password.getText().equals(password1.getText())){
                JOptionPane.showMessageDialog(
                        frame, "Пароли не совпадают!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            client.connection.send(new Message(MessageType.REG_USER, username.getText() + ";" + password.getText()));
            Message msg = client.connection.receive();

            if (msg.getTypeMessage() == MessageType.USER_ALREADY_EXISTS) {
                JOptionPane.showMessageDialog(
                        frame, "Такой пользователь уже существует!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            else {
                JOptionPane.showMessageDialog(
                        frame, "Пользователь зарегистрирован!", "Регистрация", JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    //вызывает окна для ввода имени пользователя
    protected boolean auth() throws IOException, ClassNotFoundException {
        JTextField username = new JTextField();
        JTextField password = new JPasswordField();
        JButton registration = new JButton("Регистрация");
        Object[] message = {
                "Имя пользователя:", username,
                "Пароль:", password,
                "Нет аккаунта?", registration
        };

        registration.addActionListener(e -> {
            try {
                reg();
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        });

        int option = JOptionPane.showConfirmDialog(null, message, "Вход в аккаунт", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            client.connection.send(new Message(MessageType.VERIFY_USER, username.getText() + ";" + password.getText()));
            Message msg = client.connection.receive();
            if (msg.getTypeMessage() == MessageType.USER_INVALID) {
                JOptionPane.showMessageDialog(
                        frame, "Неверный логин или пароль!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            else {
                Client.model.setUsers(msg.getListUsers());
                return true;
            }

        } else {
            JOptionPane.showMessageDialog(
                    frame, "Необходимо произвести авторизацию!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    //вызывает окно ошибки с заданным текстом
    protected void errorDialogWindow(String text) {
        JOptionPane.showMessageDialog(
                frame, text,
                "Ошибка", JOptionPane.ERROR_MESSAGE
        );
    }
}