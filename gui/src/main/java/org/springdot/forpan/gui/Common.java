package org.springdot.forpan.gui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import static javafx.scene.input.KeyCombination.CONTROL_DOWN;

class Common{
    final static KeyCombination KEY_ESC = new KeyCodeCombination(KeyCode.ESCAPE);
    final static KeyCombination KEY_ENTER = new KeyCodeCombination(KeyCode.ENTER);
    final static KeyCombination KEY_DELETE = new KeyCodeCombination(KeyCode.DELETE);
    final static KeyCombination KEY_INSERT = new KeyCodeCombination(KeyCode.INSERT);
    final static KeyCombination KEY_CONTROL_D = new KeyCodeCombination(KeyCode.D,CONTROL_DOWN);
    final static KeyCombination KEY_CONTROL_N = new KeyCodeCombination(KeyCode.N,CONTROL_DOWN);
    final static KeyCombination KEY_CONTROL_Q = new KeyCodeCombination(KeyCode.Q,CONTROL_DOWN);
    final static KeyCombination KEY_CONTROL_R = new KeyCodeCombination(KeyCode.R,CONTROL_DOWN);
}
