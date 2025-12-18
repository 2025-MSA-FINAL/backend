package com.popspot.popupplatform.global.utils;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

public class HeicConverter {
    public static File convertHeicToJpg(File heicFile) {
        try {
            File jpgFile = Files.createTempFile(
                    "heic-converted-" + UUID.randomUUID(),
                    ".jpg"
            ).toFile();

            Process process = new ProcessBuilder(
                    "vips",
                    "copy",
                    heicFile.getAbsolutePath(),
                    jpgFile.getAbsolutePath()
            ).start();

            int exitCode = process.waitFor();
            if (exitCode != 0 || !jpgFile.exists()) {
                throw new RuntimeException("vips HEIC → JPG 변환 실패");
            }

            return jpgFile;
        } catch (Exception e) {
            throw new RuntimeException("HEIC 변환 중 오류", e);
        }
    }
}
