package org.freeshr.application.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SchemaDefinitions {

    private String path;

    public SchemaDefinitions(String path) {
        this.path = path;
    }

    public Map<String, byte[]> value() throws Exception {
        return readDefinitions(path);
    }

    private HashMap<String, byte[]> readDefinitions(String path) throws Exception {
        HashMap<String, byte[]> definitions = new HashMap<String, byte[]>();
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(loadResourceArchive(path)));
        ZipEntry ze;
        while ((ze = zip.getNextEntry()) != null) {
            if (!ze.getName().endsWith(".zip") && !ze.getName().endsWith(".jar")) {
                definitions.put(ze.getName(), readDefinition(zip).toByteArray());
            }
            zip.closeEntry();
        }
        zip.close();
        return definitions;
    }

    private byte[] loadResourceArchive(String src) throws Exception {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(src);
        byte[] buffer = new byte[in.available()];
        try {
            in.read(buffer);
        } finally {
            in.close();
        }
        return buffer;
    }

    private ByteArrayOutputStream readDefinition(ZipInputStream zip) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        int n;
        byte[] buf = new byte[1024];
        while ((n = zip.read(buf, 0, 1024)) > -1) {
            b.write(buf, 0, n);
        }
        return b;
    }
}
