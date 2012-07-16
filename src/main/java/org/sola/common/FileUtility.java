/**
 * ******************************************************************************************
 * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO). All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,this list of conditions
 * and the following disclaimer. 2. Redistributions in binary form must reproduce the above
 * copyright notice,this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution. 3. Neither the name of FAO nor the names of its
 * contributors may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.sola.common;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.ImageIcon;
import org.apache.sanselan.Sanselan;
import org.sola.common.messaging.ClientMessage;
import org.sola.common.messaging.ServiceMessage;

/**
 * Provides static methods to manage various aspects related to the files.
 *
 * The FileUtility also maintains a cache of documents and will automatically purge old files from
 * the cache if the cache exceeds its maximum size (default max size is 200Mb).
 */
public class FileUtility {

    /**
     * Keeps track of the size of the documents cache, so that the cache can be purged if it becomes
     * too big.
     */
    private static long cacheSize = -1;
    /**
     * Maximum size for the documents cache in bytes. Default is 200Mb.
     */
    private static final long MAX_CACHE_SIZE_BYTES = 200 * 1024 * 1024;
    /**
     * The maximum size of the cache in bytes after it has been purged to remove old documents.
     * Default is 120Mb.
     */
    private static final long RESIZED_CACHE_SIZE_BYTES = 120 * 1024 * 1024;

    /**
     * Creates the file out of the given byte array in the documents cache folder.
     *
     * @param fileBinary Byte array representing file content.
     * @param tmpFileName The name to use as a temporary file name.
     */
    private static void createFile(byte[] fileBinary, File file) {
        try {
            File cache = new File(getCachePath());
            if (!cache.exists()) {
                // Need to create the file cache directory. 
                cache.mkdirs();
            } else {
                // Check if the cache needs to have some documents purged
                maintainCache(cache, fileBinary.length);
            }

            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            file.setLastModified(DateUtility.now().getTime());
            FileOutputStream fs = new FileOutputStream(file);
            fs.write(fileBinary);
            fs.flush();
            fs.close();

        } catch (IOException iex) {
            Object[] lstParams = {iex.getLocalizedMessage()};
            throw new SOLAException(ClientMessage.ERR_FAILED_CREATE_NEW_FILE, lstParams);
        }
    }

    /**
     * Checks the cache to ensure it won't exceed the max size cache size. If the new document will
     * cause the cache to exceed the max size, the older documents in the cache are deleted until
     * the cache reaches the resize limit.
     *
     * @param cache The directory for the documents cache
     * @param newFileSize The size of the new file to open in bytes.
     */
    private static void maintainCache(File cache, long newFileSize) {
        if (cacheSize < 0 || (cacheSize + newFileSize) > MAX_CACHE_SIZE_BYTES) {
            // Determine the actual size of the cache directory. Ignore the size of subdirectories.
            cacheSize = getDirectorySize(cache, false);
        }
        cacheSize += newFileSize;
        if (cacheSize > MAX_CACHE_SIZE_BYTES) {

            // The cache has exceeded its max size. Delete the oldest files in the cache based
            // on thier last modified date. 
            List<File> files = Arrays.asList(cache.listFiles());
            Collections.sort(files, new Comparator<File>() {

                @Override
                public int compare(File f1, File f2) {
                    return (f1.lastModified() > f2.lastModified() ? 1
                            : (f1.lastModified() == f2.lastModified() ? 0 : -1));
                }
            });

            for (File f : files) {
                // Only delete files - ignore subdirectories. 
                if (f.isFile()) {
                    cacheSize = cacheSize - f.length();
                    f.delete();
                    if (cacheSize < RESIZED_CACHE_SIZE_BYTES) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Returns
     * <code>byte[]</code> array of the file.
     *
     * @param filePath The full path to the file.
     */
    public static byte[] getFileBinary(String filePath) {
        try {
            File file = new File(filePath);

            if (!file.exists()) {
                return null;
            }

            InputStream inStream = new FileInputStream(file);

            // Get the size of the file
            long length = file.length();

            if (length > Integer.MAX_VALUE) {
                throw new Exception("File too large");
            }

            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = inStream.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }

            // Close the input stream and return bytes
            inStream.close();
            return bytes;

        } catch (Exception e) {
            throw new SOLAException(ServiceMessage.GENERAL_UNEXPECTED_ERROR_DETAILS,
                    new String[]{e.getLocalizedMessage()});
        }
    }

    /**
     * Returns file's extention.
     *
     * @param fileName The name of the file.
     */
    public static String getFileExtension(String fileName) {
        String ext = null;
        if (fileName.lastIndexOf(".") > 0 && fileName.lastIndexOf(".") < fileName.length()) {
            ext = fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return ext;
    }

    /**
     * Returns the absolute file path for the documents cache directory.
     */
    public static String getCachePath() {
        return System.getProperty("user.home") + "/sola/cache/documents/";
    }

    /**
     * Returns true if the file to check is already in the documents cache. Note that the document
     * name should include the rowVersion number to ensure any documents that get updated also get
     * reloaded in the cache.
     *
     * @param tmpFileName The name of the file to check in the documents cache.
     */
    public static boolean isCached(String tmpFileName) {
        File file = new File(getCachePath() + File.separator + tmpFileName);
        return file.exists();
    }

    /**
     * Returns the size of the directory. This is done by summing the size of each file in the
     * directory. The sizes of all subdirectories can be optionally included.
     *
     * @param directory The directory to calculate the size for.
     */
    public static long getDirectorySize(File directory, boolean recursive) {
        long length = 0;
        if (!directory.isFile()) {
            for (File file : directory.listFiles()) {
                if (file.isFile()) {
                    length += file.length();
                } else {
                    if (recursive) {
                        length += getDirectorySize(file, recursive);
                    }
                }

            }
        }
        return length;
    }

    /**
     * Opens the specified file from the documents cache. If the file does not exist in the cache a
     * File Open exception is thrown.
     *
     * @param tmpFileName The name of the file to open from the documents cache.
     */
    public static void openFile(String tmpFileName) {
        openFile(new File(getCachePath() + File.separator + tmpFileName));
    }

    /**
     * Creates a new file in the documents cache using the fileBinary data then opens the file for
     * display.
     *
     * @param fileBinary The binary content of the file to open.
     * @param fileName The name to use for creating the file. This name must exclude any file path.
     */
    public static void openFile(byte[] fileBinary, String fileName) {
        File file = new File(getCachePath() + File.separator + fileName);
        createFile(fileBinary, file);
        openFile(file);
    }

    /**
     * Opens the file from the documents cache using the Java Desktop.
     *
     * @param file The file to open
     * @throws SOLAException Failed to open file
     */
    private static void openFile(File file) throws SOLAException {
        try {
            // Try to open
            Desktop dt = Desktop.getDesktop();
            dt.open(file);
        } catch (IOException iex) {
            Object[] lstParams = {iex.getLocalizedMessage()};
            throw new SOLAException(ClientMessage.ERR_FAILED_OPEN_FILE, lstParams);
        }
    }

    /**
     * Creates thumbnail image for the given file. Returns null if format is not supported.
     *
     * @param filePath The full path to the file.
     * @param width Thumbnail width.
     * @param height Thumbnail height.
     */
    public static BufferedImage createImageThumbnail(String filePath, int width, int height) {
        try {
            File file = new File(filePath);

            if (!file.exists()) {
                return null;
            }

            Image thumbnail = null;
            String fileExt = getFileExtension(filePath);

            if (fileExt.equalsIgnoreCase("jpg") || fileExt.equalsIgnoreCase("jpeg")) {

                ImageIcon tmp = new ImageIcon(filePath);
                if (tmp == null || tmp.getIconWidth() <= 0
                        || tmp.getIconHeight() <= 0) {
                    return null;
                }

                ImageIcon scaled = null;

                if ((tmp.getIconWidth() > width && width > 0)
                        || (tmp.getIconHeight() > height && height > 0)) {
                    scaled = new ImageIcon(tmp.getImage().getScaledInstance(
                            width, height, Image.SCALE_SMOOTH));
                } else {
                    scaled = tmp;
                }

                BufferedImage buffered = new BufferedImage(
                        scaled.getIconWidth(),
                        scaled.getIconHeight(),
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D g = buffered.createGraphics();
                g.drawImage(scaled.getImage(), 0, 0, null);
                g.dispose();

                return buffered;

            } else {

                if (fileExt.equalsIgnoreCase("pdf")) {

                    RandomAccessFile raf = new RandomAccessFile(file, "r");
                    FileChannel channel = raf.getChannel();
                    ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                    PDFFile pdffile = new PDFFile(buf);

                    // draw the first page to an image
                    PDFPage page = pdffile.getPage(0);

                    //get the width and height for the doc at the default zoom 
                    Rectangle rect = new Rectangle(0, 0,
                            (int) page.getBBox().getWidth(),
                            (int) page.getBBox().getHeight());

                    //generate the image
                    thumbnail = page.getImage(
                            rect.width, rect.height, //width & height
                            rect, // clip rect
                            null, // null for the ImageObserver
                            true, // fill background with white
                            true // block until drawing is done
                            );

                    buf.clear();
                    channel.close();
                    raf.close();

                } else {

                    BufferedImage img = Sanselan.getBufferedImage(file);
                    thumbnail = Toolkit.getDefaultToolkit().createImage(img.getSource());

                }

                if (thumbnail == null || thumbnail.getWidth(null) <= 0
                        || thumbnail.getHeight(null) <= 0) {
                    return null;
                }

                Image scaled = null;

                if ((thumbnail.getWidth(null) > width && width > 0)
                        || (thumbnail.getHeight(null) > height && height > 0)) {
                    scaled = thumbnail.getScaledInstance(
                            width, height, Image.SCALE_SMOOTH);
                } else {
                    scaled = thumbnail;
                }

                BufferedImage buffered = new BufferedImage(
                        scaled.getWidth(null),
                        scaled.getHeight(null),
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D g = buffered.createGraphics();
                g.drawImage(scaled, 0, 0, null);
                g.dispose();

                return buffered;
            }

        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            return null;
        }
    }
}
