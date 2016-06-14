package com.emc.caspian.ccs.common.utils;

import com.emc.caspian.fabric.util.Validate;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper class for dealing with file handling
 */
public final class FileHelper {
  /**
   * Helper method to read file as string
   *
   * @param filePath: relative file path from working directory (storage directory)
   *
   * @return
   */
  public static String readFileAsString(final String filePath) {
    Validate.isNotNullOrEmpty(filePath, "filePath");
    try {
      return Files.toString(new File(filePath), Charsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * read file as stream
   *
   * @param filePath
   *
   * @return
   */
  public static InputStream readFileAsStream(final String filePath) {
    Validate.isNotNullOrEmpty(filePath, "filePath");
    try {
      return new FileInputStream(filePath);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Helper method to save input stream as a file. This will create all the intermediate directories before saving
   * <p/>
   * the file.
   *
   * @param filePath
   * @param inputStream
   */
  public static void saveStreamToFile(final String filePath, final InputStream inputStream) {
    Validate.isNotNullOrEmpty(filePath, "filePath");
    Validate.isNotNull(inputStream, "inputStream");
    new File(filePath).getParentFile().mkdirs();
    final File targetFile = new File(filePath);
    try {
      final OutputStream outStream = new FileOutputStream(targetFile);
      ByteStreams.copy(inputStream, outStream);
      outStream.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Helper method to verify if the file exists locally
   *
   * @param filePath
   *
   * @return
   */
  public static boolean checkFileExists(final String filePath) {
    Validate.isNotNullOrEmpty(filePath, "filePath");
    // lookup to see if the file is present in the p2p peerImageUrl
    final File f = new File(filePath);
    if (f.exists() && !f.isDirectory()) {
      return true;
    }
    return false;
  }
}
