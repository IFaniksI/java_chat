// контроллер - тут реализованы методы которые осуществляют общение между клиентом и сервером
// после того как сервер запущен в бесконечном цикле мы ждем подключение от какого либо клиента
// после того как это подключение происходит метод accept() возвращает сокет данного подключения
// и этот сокет передается в виде параметра конструктора соответствующего класса Thread (ServerThread)
// то есть для каждого клиента у нас запускается поток исполнения
package Server;

import Connection.*;
import Server.Repository.JdbcDao;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Server {
    private ServerSocket serverSocket;
    private static ViewGuiServer gui; //объект класса представления
    private static ModelGuiServer model; //объект класса модели
    private static volatile boolean isServerStart = false; //флаг отражающий состояние сервера запущен/остановлен

    //метод, запускающий сервер
    protected void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            isServerStart = true;
            gui.refreshDialogWindowServer("Сервер запущен.\n");
        } catch (Exception e) {
            gui.refreshDialogWindowServer("Не удалось запустить сервер.\n");
        }
    }

    //метод останавливающий сервер
    protected void stopServer() {
        try {
            //если серверныйСокет не имеет ссылки или не запущен
            if (serverSocket != null && !serverSocket.isClosed()) {
                for (Map.Entry<String, Connection> user : model.getAllUsersMultiChat().entrySet()) {
                    user.getValue().close();
                }
                serverSocket.close();
                model.getAllUsersMultiChat().clear();
                gui.refreshDialogWindowServer("Сервер остановлен.\n");
            } else gui.refreshDialogWindowServer("Сервер не запущен - останавливать нечего!\n");
        } catch (Exception e) {
            gui.refreshDialogWindowServer("Остановить сервер не удалось.\n");
        }
    }

    //метод, в котором в бесконечном цикле сервер принимает новое сокетное подключение от клиента
    protected void acceptServer() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();// когла подключение происходит метод accept() возвращает сокет данного подключения
                new ServerThread(socket).start();// сокет передается в виде параметра конструктора соответствующего класса Thread (ServerThread)
            } catch (Exception e) {
                gui.refreshDialogWindowServer("Связь с сервером потеряна.\n");
                break;
            }
        }
    }

    //метод, рассылающий заданное сообщение всем клиентам из мапы
    protected void sendMessageAllUsers(Message message) {
        for (Map.Entry<String, Connection> user : model.getAllUsersMultiChat().entrySet()) {
            try {
                user.getValue().send(message);
            } catch (Exception e) {
                gui.refreshDialogWindowServer("Ошибка отправки сообщения всем пользователям!\n");
            }
        }
    }

    //точка входа для приложения сервера
    public static void main(String[] args) {
        Server server = new Server();
        gui = new ViewGuiServer(server);
        model = new ModelGuiServer();
        gui.initFrameServer();
        //цикл снизу ждет true от флага isServerStart (при старте сервера в методе startServer устанавливается в true)
        //после чего запускается бесконечный цикл принятия подключения от клиента в  методе acceptServer
        //до тех пор пока сервер не остановится, либо не возникнет исключение
        while (true) {
            if (isServerStart) {
                server.acceptServer();
                isServerStart = false;
            }
        }
    }

    //класс-поток, который запускается при принятии сервером нового сокетного соединения с клиентом, в конструктор
    //передается объект класса Socket
    private class ServerThread extends Thread {
        private Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        //метод который реализует запрос сервера у клиента имени и добавлении имени в мапу
        private String requestAndAddingUser(Connection connection) {
            while (true) {
                try {
                    connection.send(new Message(MessageType.REQUEST_NAME_USER));
                    Message responseMessage = connection.receive();
                    var message = responseMessage.getTextMessage().split(";");
                    String userName = message[0];
                    String password = message[1];

                    if(responseMessage.getTypeMessage() == MessageType.VERIFY_USER) {
                        if(!JdbcDao.Instance.checkUserAuth(userName, password)){
                            connection.send(new Message(MessageType.USER_INVALID));
                        }
                        else {
                            model.addUser(userName, connection);
                            Set<String> listUsers = new HashSet<>();
                            for (Map.Entry<String, Connection> users : model.getAllUsersMultiChat().entrySet()) {
                                listUsers.add(users.getKey());
                            }
                            connection.send(new Message(MessageType.NAME_ACCEPTED, listUsers));
                            sendMessageAllUsers(new Message(MessageType.USER_ADDED, userName));
                            JdbcDao.Instance.insertNewLog(Action.NewConnection, "Подключился новый пользователь: " + userName + " с удаленным хостом " + connection.Port());
                            JdbcDao.Instance.updateUserData(new Timestamp(Calendar.getInstance().getTime().getTime()), connection.Port(), userName);
                            return userName;
                        }
                    }

                    if(responseMessage.getTypeMessage() == MessageType.REG_USER){
                        if(JdbcDao.Instance.isUserExists(userName)){
                            connection.send(new Message(MessageType.USER_ALREADY_EXISTS));
                        }
                        else {
                            JdbcDao.Instance.insertNewUser(userName, password);
                            JdbcDao.Instance.insertNewLog(Action.NewUser, "Зарегистрирован новый пользователь: " + userName);
                            connection.send(new Message(MessageType.REG_USER));
                        }
                    }

                } catch (Exception e) {
                    gui.refreshDialogWindowServer("Возникла ошибка при запросе и добавлении нового пользователя\n");
                }
            }
        }

        //метод, реализующий обмен сообщениями между пользователями
        private void messagingBetweenUsers(Connection connection, String userName) {
            while (true) {
                try {
                    Message message = connection.receive();// сервер принимает сообщение от какого либо клиента
                    //приняли сообщение от клиента, если тип сообщения TEXT_MESSAGE то пересылаем его всем пользователям
                    if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {// проверяет его тип
                        String textMessage = String.format("%s: %s\n", userName, message.getTextMessage());
                        JdbcDao.Instance.insertNewLog(Action.NewMessage, "Пользователь " + userName + " отправил сообщение: " + message.getTextMessage());
                        sendMessageAllUsers(new Message(MessageType.TEXT_MESSAGE, textMessage)); // рассылает все это сообщене
                    }
                    //если тип сообщения DISABLE_USER, то рассылаем всем пользователям, что данный пользователь покинул чат,
                    //удаляем его из мапы, закрываем его connection
                    if (message.getTypeMessage() == MessageType.DISABLE_USER) {// если пользователь хочет отключится от чата
                        sendMessageAllUsers(new Message(MessageType.REMOVED_USER, userName));// рассылается сообщение об этом
                        model.removeUser(userName);// пользователь удаляется
                        JdbcDao.Instance.updateUserDisconnection(new Timestamp(Calendar.getInstance().getTime().getTime()), userName);
                        JdbcDao.Instance.insertNewLog(Action.NewInformation, "Пользователь " + userName + " с удаленным хостом " + connection.Port() +  " отключился.");
                        connection.close();// закрываются все потоки записи и чтения, а так же сокетного соединения
                        gui.refreshDialogWindowServer(String.format("Пользователь с удаленным доступом %s отключился.\n", socket.getRemoteSocketAddress()));
                        break;
                    }
                } catch (Exception e) {
                    gui.refreshDialogWindowServer(String.format("Произошла ошибка при рассылке сообщения от пользователя %s, либо отключился!\n", userName));
                    break;
                }
            }
        }

        @Override
        public void run() {
            gui.refreshDialogWindowServer(String.format("Подключился новый пользователь с удаленным сокетом - %s.\n", socket.getRemoteSocketAddress()));
            try {
                //получаем connection при помощи принятого сокета от клиента и запрашиваем имя, регистрируем, запускаем
                //цикл обмена сообщениями между пользователями
                Connection connection = new Connection(socket);// создаем Connection и передаем в него сокет
                String nameUser = requestAndAddingUser(connection);// получаем имя нового клиента (пользователя)
                messagingBetweenUsers(connection, nameUser);
            } catch (Exception e) {
                gui.refreshDialogWindowServer(String.format("Произошла ошибка при рассылке сообщения от пользователя!\n"));
            }
        }
    }
}
