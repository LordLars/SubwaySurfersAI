package Main;

import java.awt.event.KeyEvent;

public enum Action {

    None(0),
    Left (KeyEvent.VK_LEFT),
    Right(KeyEvent.VK_RIGHT),
    Jump (KeyEvent.VK_UP),
    Roll (KeyEvent.VK_DOWN);

    private final int keycode;

    Action(int keycode) {
        this.keycode = keycode;
    }

    public int getKeycode() {
        return keycode;
    }

}
