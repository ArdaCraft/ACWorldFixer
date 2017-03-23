package me.dags.blockr.world;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author dags <dags@dags.me>
 */
public class WorldDataResource extends WorldData {

    private final String path;

    public WorldDataResource(String path) {
        this.path = path;
    }

    public boolean validate() {
        return true;
    }

    @Override
    public String error() {
        return "Could not fine resource " + path;
    }

    InputStream getInputStream() throws IOException {
        return WorldDataResource.class.getResourceAsStream(path);
    }
}
