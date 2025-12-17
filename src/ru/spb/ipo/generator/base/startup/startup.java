package ru.spb.ipo.generator.base.startup;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import ru.spb.ipo.generator.base.startup.MenuFrame;

public class startup {
    private static ClassLoader loader;

    public static void main(String[] arrstring) throws Exception {
        loader = new URLClassLoader(new URL[]{new File("engine.jar").toURL()});
        new MenuFrame().setVisible(true);
    }

    public static ClassLoader getLoader() {
        return loader;
    }

    public static ClassLoader getEngineLoader() throws Exception {
        return new URLClassLoader(new URL[]{new File("engine.jar").toURL()});
        
    }
}