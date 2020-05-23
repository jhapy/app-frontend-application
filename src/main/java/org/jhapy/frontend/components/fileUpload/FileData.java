package org.jhapy.frontend.components.fileUpload;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Class containing file information for upload.
 */
public class FileData implements Serializable {

  private final String fileName, mimeType;
  private final ObjectOutputStream outputBuffer;

  /**
   * Create a FileData instance for a file.
   *
   * @param fileName the file name
   * @param mimeType the file MIME type
   * @param outputBuffer the output buffer where to write the file
   */
  public FileData(String fileName, String mimeType,
      ObjectOutputStream outputBuffer) {
    this.fileName = fileName;
    this.mimeType = mimeType;
    this.outputBuffer = outputBuffer;
  }

  /**
   * Return the mimeType of this file.
   *
   * @return mime types of the files
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Return the name of this file.
   *
   * @return file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Return the output buffer for this file data.
   *
   * @return output buffer
   */
  public OutputStream getOutputBuffer() {
    return new ByteArrayOutputStream();
  }
}
