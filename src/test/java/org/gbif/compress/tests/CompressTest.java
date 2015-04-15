package org.gbif.compress.tests;

import org.gbif.hadoop.compress.d2.D2CombineInputStream;
import org.gbif.hadoop.compress.d2.zip.ModalZipOutputStream;
import org.gbif.hadoop.compress.d2.zip.ZipEntry;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import org.junit.Test;

public class CompressTest {


  @Test
  public void testCompress() throws  IOException {

    try (
      FileOutputStream fileOutputStream = new FileOutputStream("download.zip");
      ModalZipOutputStream zos = new ModalZipOutputStream(new BufferedOutputStream(fileOutputStream));
    ) {


      ZipEntry ze = new ZipEntry("data.txt");
      zos.putNextEntry(ze, ModalZipOutputStream.MODE.DEFAULT);
      //Get all the files inside the directory and creates a list of InputStreams.
      try {
        D2CombineInputStream
          //Lists all the files inside the directory 'test/downloadfile/' and add each file to the zip file
          in = new D2CombineInputStream(Lists.transform(Lists.newArrayList(new File(CompressTest.class.getClassLoader()
                                                                                                      .getResource(
                                                                                                        "downloadfile/")
                                                                                                      .getFile()).listFiles()),
                                                                        new Function<File, InputStream>() {
                                                                          @Nullable
                                                                          @Override
                                                                          public InputStream apply(
                                                                            @Nullable File input
                                                                          ) {
                                                                            try {
                                                                              System.out.println(input.getName());
                                                                              return new ByteArrayInputStream(Files.readAllBytes(
                                                                                Paths.get(input.toURI())));
                                                                            } catch (IOException ex) {
                                                                              Throwables.propagate(ex);
                                                                              return null;
                                                                            }
                                                                          }
                                                                        }));
        ByteStreams.copy(in, zos);
        in.close(); // required to get the sizes
        ze.setSize(in.getUncompressedLength()); // important to set the sizes and CRC
        ze.setCompressedSize(in.getCompressedLength());
        ze.setCrc(in.getCrc32());
      } finally {

      }

      zos.closeEntry();
      // add an entry to compress
      zos.close();
    }
  }




}
