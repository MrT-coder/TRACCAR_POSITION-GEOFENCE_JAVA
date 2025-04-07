package com.traccar.PositionGeofence.database;

import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class MediaManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaManager.class);

    private final String path;

    public MediaManager(@Value("${media.path}") String path) {
        this.path = path;
    }

    private File createFile(String uniqueId, String name) throws IOException {
        Path filePath = Paths.get(path, uniqueId, name);
        Path directoryPath = filePath.getParent();
        if (directoryPath != null) {
            Files.createDirectories(directoryPath);
        }
        return filePath.toFile();
    }

    public OutputStream createFileStream(String uniqueId, String name, String extension) throws IOException {
        return new FileOutputStream(createFile(uniqueId, name + "." + extension));
    }

    public String writeFile(String uniqueId, ByteBuf buf, String extension) {
        if (path != null) {
            int size = buf.readableBytes();
            String name = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "." + extension;
            try (FileOutputStream output = new FileOutputStream(createFile(uniqueId, name));
                 FileChannel fileChannel = output.getChannel()) {
                ByteBuffer byteBuffer = buf.nioBuffer();
                int written = 0;
                while (written < size) {
                    written += fileChannel.write(byteBuffer);
                }
                fileChannel.force(false);
                return name;
            } catch (IOException e) {
                LOGGER.warn("Save media file error", e);
            }
        }
        return null;
    }
}