package Connection;

import java.io.Serializable;
import java.util.Set;

public class Message implements Serializable {
    private MessageType typeMessage; //тип сообщения
    private String textMessage; //текст сообщения
    private Set<String> listUsers; //множество имен уже подлючившихся пользователей (список активных пользователей подключенных к серверу в данный момент)

    public Message(MessageType typeMessage, String textMessage) {
        this.textMessage = textMessage;
        this.typeMessage = typeMessage;
    }

    public Message(MessageType typeMessage, Set<String> listUsers) {
        this.typeMessage = typeMessage;
        this.textMessage = null;
        this.listUsers = listUsers;
    }

    public Message(MessageType typeMessage) {
        this.typeMessage = typeMessage;
        this.textMessage = null;
    }

    public MessageType getTypeMessage() {
        return typeMessage;
    }

    public Set<String> getListUsers() {
        return listUsers;
    }

    public String getTextMessage() {
        return textMessage;
    }

}
