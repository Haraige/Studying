package ua.org.code.util;

import ua.org.code.App;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public class LoadProperties {
    public static Properties load() {
        Properties props = new Properties();

        try(InputStream input = App.class.getResourceAsStream("/jdbc.properties")) {
            props.load(input);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return props;
    }
}
