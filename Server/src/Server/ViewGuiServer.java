/* представление - тут описывается сам графический интерфейс, а также обработка
событий по нажатию на кнопки или ввода текста в текстовое поле */

package Server;

import Server.Repository.JdbcDao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;


public class ViewGuiServer {
    private JFrame frame = new JFrame("Запуск сервера");
    private JTextArea dialogWindow = new JTextArea(10, 40);
    private JButton buttonStartServer = new JButton("Запустить сервер");
    private JButton buttonStopServer = new JButton("Остановить сервер");
    private JButton openMenu = new JButton("Открыть меню");
    private JPanel panelButtons = new JPanel();
    private final Server server;

    public ViewGuiServer(Server server) {
        this.server = server;
    }

    //метод инициализации графического интерфейса приложения сервера
    protected void initFrameServer() {
        dialogWindow.setEditable(false);
        dialogWindow.setLineWrap(true);  //автоматический перенос строки в JTextArea
        frame.add(new JScrollPane(dialogWindow), BorderLayout.CENTER);
        panelButtons.add(buttonStartServer);
        panelButtons.add(buttonStopServer);
        panelButtons.add(openMenu);
        frame.add(panelButtons, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null); // при запуске отображает окно по центру экрана
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //класс обработки события при закрытии окна приложения Сервера
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                server.stopServer();
                System.exit(0);
            }
        });
        frame.setVisible(true);

        buttonStartServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int port = getPortFromOptionPane();
                server.startServer(port);
            }
        });
        buttonStopServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.stopServer();
            }
        });

        openMenu.addActionListener(e -> sendOptionFrame());
    }

    protected void sendOptionFrame() {
        JButton createReport = new JButton("Выгрузить отчет");

        createReport.addActionListener(e -> {
            FileWriter myWriter = null;
            try {
                var path = new Date().getTime() + "_report.txt";
                myWriter = new FileWriter(path);
                myWriter.write(JdbcDao.Instance.fetchLogs());
                myWriter.close();
                JOptionPane.showMessageDialog(null, "Создан новый отчет: " + path, "Меню", JOptionPane.PLAIN_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        Object[] message = {
                createReport,
        };

        JOptionPane.showMessageDialog(null, message, "Меню", JOptionPane.PLAIN_MESSAGE);
    }

    //метод который добавляет в текстовое окно новое сообщение
    public void refreshDialogWindowServer(String serviceMessage) {
        dialogWindow.append(serviceMessage);
    }

    //метод вызывающий диалоговое окно для ввода порта сервера
    protected int getPortFromOptionPane() {
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
}