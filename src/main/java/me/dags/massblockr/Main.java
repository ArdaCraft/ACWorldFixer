package me.dags.massblockr;

import me.dags.massblockr.client.gui.GuiApp;
import me.dags.massblockr.client.headless.HeadlessApp;

import java.awt.*;

/**
 * @author dags <dags@dags.me>
 */
public class Main {

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless() || args.length > 0) {
            new HeadlessApp().launch(args);
        } else {
            new GuiApp().launch(args);
        }
    }
}
