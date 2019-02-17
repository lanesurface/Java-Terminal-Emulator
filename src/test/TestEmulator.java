package test;

import java.awt.Color;

import jtxt.Term;
import jtxt.emulator.EmulatedTerminal;
import jtxt.emulator.tui.GridLayout;
import jtxt.emulator.tui.TextBox;

public class TestEmulator {
    public static void main(String[] args) {
        Term terminal = new EmulatedTerminal("Emulator",
                                             80,
                                             20,
                                             "Consolas",
                                             12,
                                             Color.BLACK,
                                             0.6f);
        GridLayout layout = GridLayout.initializeForDimensions(1, 1);
        terminal.createRootContainer(layout);
        
        TextBox text = new TextBox(layout.getParametersForCell(0, 0),
                                   "Hello, O beautiful emulator!",
                                   TextBox.Position.CENTER);
        terminal.add(text);
    }
}