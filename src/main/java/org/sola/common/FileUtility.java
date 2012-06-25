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
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import javax.swing.ImageIcon;
import org.apache.sanselan.Sanselan;
import org.sola.common.messaging.ClientMessage;
import org.sola.common.messaging.ServiceMessage;

/**
 * Provides static methods to manage various aspects related to the files.
 */
public class FileUtility {

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
    public static String getFileExtesion(String fileName) {
        String ext = null;
        if (fileName.lastIndexOf(".") > 0 && fileName.lastIndexOf(".") < fileName.length()) {
            ext = fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return ext;
    }

    /**
     * Creates the file out of the given byte array in the temporary folder and runs it.
     *
     * @param fileBinary Byte array representing file content.
     * @param tmpFileName The name to use as a temporary file name.
     */
    public static void runFile(byte[] fileBinary, String tmpFileName) {

        // Create file in temp folder
        if (tmpFileName == null || tmpFileName.equals("") || fileBinary == null) {
            return;
        }

        File file = new File(String.format("%s%ssola_server_file_%s", System.getProperty("java.io.tmpdir"),
                File.separator, tmpFileName));

        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream fs = new FileOutputStream(file);
            fs.write(fileBinary);
            fs.flush();
            fs.close();
        } catch (IOException iex) {
            Object[] lstParams = {iex.getLocalizedMessage()};
            throw new SOLAException(ClientMessage.ERR_FAILED_CREATE_NEW_FILE, lstParams);
        }

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
            String fileExt = getFileExtesion(filePath);

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
