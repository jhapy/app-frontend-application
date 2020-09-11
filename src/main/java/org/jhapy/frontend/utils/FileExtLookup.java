package org.jhapy.frontend.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;

/**
 * @author Alexandre Clavaud.
 * @version 1.0
 * @since 11/09/2020
 */
public class FileExtLookup {
  private static FileExtLookup instance;

  private FileExtLookup() {
  }

  public static final FileExtLookup getInstance() {
    if ( instance == null )
      instance = new FileExtLookup();
    return instance;
  }

public  boolean doesExtExists( String ext ) {
  ClassLoader classLoader = ClassLoader.getSystemClassLoader();
  try {
    InputStream found = classLoader.getResourceAsStream("META-INF/resources/images/filesExt/"+ext);
    return found != null;
  } catch (Exception e) {
    return false;
  }
}
}
