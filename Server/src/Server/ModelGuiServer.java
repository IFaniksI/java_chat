/* модель - для сервера в мапе ключом является имя пользователя,
а значение ключа объект класса Connection соответствующий данному поьзователю */
package Server;

import Connection.Connection;

import java.util.HashMap;
import java.util.Map;

public class ModelGuiServer {
    //модель хранит карту со всеми подключившимися клиентами ключ - имя клиента, значение - объект connecton
    private Map<String, Connection> allUsersMultiChat = new HashMap<>();


    protected Map<String, Connection> getAllUsersMultiChat() {
        return allUsersMultiChat;
    }

    protected void addUser(String nameUser, Connection connection) {
        allUsersMultiChat.put(nameUser, connection);
    }

    protected void removeUser(String nameUser) {
        allUsersMultiChat.remove(nameUser);
    }

}
