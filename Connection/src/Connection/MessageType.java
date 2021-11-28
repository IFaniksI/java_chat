// в зависимости от типа сообщения реализуется поведение наших приложений
// например - когда сервер запрашивает имя пользователя он отправляет клиенту сообщение типа REQUEST_NAME_USER
// когда клиент пишет сообщение для других пользователей то у этого сообщения будет тип TEXT_MESSAGE, и т.д.
package Connection;

public enum MessageType {
    REQUEST_NAME_USER(1),
    TEXT_MESSAGE(2),
    NAME_ACCEPTED(3),
    USER_NAME(4),
    NAME_USED(5),
    USER_ADDED(6),
    DISABLE_USER(7),
    REMOVED_USER(8);

    private final int value;
    MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}