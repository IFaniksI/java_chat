package Server;

public enum Action {
    NewMessage(1),
    NewConnection(2),
    NewUser(3),
    NewInformation(4);


    private final int value;
    Action(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
