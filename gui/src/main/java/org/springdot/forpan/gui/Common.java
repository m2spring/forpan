package org.springdot.forpan.gui;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import java.util.regex.Pattern;

import static javafx.scene.input.KeyCombination.CONTROL_DOWN;

class Common{
    final static KeyCombination KEY_ESC = new KeyCodeCombination(KeyCode.ESCAPE);
    final static KeyCombination KEY_ENTER = new KeyCodeCombination(KeyCode.ENTER);
    final static KeyCombination KEY_DELETE = new KeyCodeCombination(KeyCode.DELETE);
    final static KeyCombination KEY_INSERT = new KeyCodeCombination(KeyCode.INSERT);
    final static KeyCombination KEY_CURSOR_DN = new KeyCodeCombination(KeyCode.DOWN);
    final static KeyCombination KEY_CONTROL_C = new KeyCodeCombination(KeyCode.C,CONTROL_DOWN);
    final static KeyCombination KEY_CONTROL_D = new KeyCodeCombination(KeyCode.D,CONTROL_DOWN);
    final static KeyCombination KEY_CONTROL_F = new KeyCodeCombination(KeyCode.F,CONTROL_DOWN);
    final static KeyCombination KEY_CONTROL_G = new KeyCodeCombination(KeyCode.G,CONTROL_DOWN);
    final static KeyCombination KEY_CONTROL_N = new KeyCodeCombination(KeyCode.N,CONTROL_DOWN);
    final static KeyCombination KEY_CONTROL_Q = new KeyCodeCombination(KeyCode.Q,CONTROL_DOWN);
    final static KeyCombination KEY_CONTROL_R = new KeyCodeCombination(KeyCode.R,CONTROL_DOWN);

    static void copyToClipboard(String content){
        var cbc = new ClipboardContent();
        cbc.putString(content);
        Clipboard.getSystemClipboard().setContent(cbc);
    }

    final static Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
}
