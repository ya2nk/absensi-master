package com.waroengweb.absensi.helpers;

import android.content.ContentResolver;

import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UriUtils {
    public static File getFileFromUri(ContentResolver contentResolver, Uri uri, File directory) throws IOException {
        File file = File.createTempFile("suffix_", ".pdf", directory);
        try (OutputStream outputStream = new FileOutputStream(file);
             InputStream inputStream = contentResolver.openInputStream(uri)) {
            if (inputStream != null) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
        return file;
    }

    public static byte[] fileToBytes(String path) {
        File file = new File(path);
        byte[] bytes = new byte[0];
        try(FileInputStream inputStream = new FileInputStream(file)) {
            bytes = new byte[inputStream.available()];
            //noinspection ResultOfMethodCallIgnored
            inputStream.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }
}
