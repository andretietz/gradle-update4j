package com.andretietz.updater;

import org.update4j.Configuration;

import java.io.FileReader;
import java.io.IOException;

public class Bootstrap {
    public static void main(String[] args) throws IOException {
        BullShit.INSTANCE.someExternalLib();
        Configuration updater = Configuration.read(new FileReader("update.xml"));
        updater.update();
        updater.launch();
    }
}
