/**
 * Copyright (c) 2012, Marc Röttig, Stephan Aiche.
 *
 * This file is part of GenericKnimeNodes.
 * 
 * GenericKnimeNodes is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.genericworkflownodes.knime.nodegeneration.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class that provides convenience functions to handle zip files.
 * 
 * @author roettig, aiche
 */
public final class ZipUtils {

    private static final int BUFFER_SIZE = 2048;

    /**
     * Decompress the content of @p zipStream to the directory @p targetDir.
     * 
     * @param targetDir
     *            The directory where the zip should be extracted.
     * @param zipStream
     *            The zip file stream.
     * @throws UnZipFailureException
     *             If decompression fails.
     */
    public static void decompressTo(File targetDir, InputStream zipStream)
            throws UnZipFailureException {
        if (!targetDir.exists()) {
            boolean createDirs = targetDir.mkdirs();
            if (!createDirs) {
                throw new UnZipFailureException(
                        "Unable to create the target directory.");
            }
        }
        FileOutputStream fout = null;
        try {
            ZipInputStream zin = new ZipInputStream(zipStream);
            ZipEntry ze = null;

            byte[] buffer = new byte[BUFFER_SIZE];

            while ((ze = zin.getNextEntry()) != null) {
                File targetFile = new File(targetDir, ze.getName());

                if (ze.isDirectory()) {
                    targetFile.mkdirs();
                } else {
                    if (!targetFile.getParentFile().exists()) {
                        targetFile.getParentFile().mkdirs();
                    }

                    fout = new FileOutputStream(targetFile);

                    int size;
                    while ((size = zin.read(buffer, 0, BUFFER_SIZE)) != -1) {
                        fout.write(buffer, 0, size);
                    }

                    zin.closeEntry();
                    fout.flush();
                    fout.close();
                }
            }
            zin.close();
        } catch (Exception e) {
            // try to close the streams
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e1) {
                    throw new UnZipFailureException(
                            "Failed to close stream when handling first exception.",
                            e1);
                }
            }
            throw new UnZipFailureException(e.getMessage(), e);
        }
    }

    /**
     * Do not extract the stream, just count the number of zip entries.
     * 
     * @param zipStream
     *            The input stream containing the zip file.
     * @return The number of files/folders in the zip stream.
     * @throws UnZipFailureException
     *             If decompression fails.
     */
    public static int countEntries(InputStream zipStream)
            throws UnZipFailureException {
        int numEntries = 0;
        try {
            ZipInputStream zin = new ZipInputStream(zipStream);
            while ((zin.getNextEntry()) != null) {
                ++numEntries;
            }
            zin.close();
        } catch (Exception e) {
            throw new UnZipFailureException(e.getMessage(), e);
        }
        return numEntries;
    }

    /**
     * Private c'tor.
     */
    private ZipUtils() {
    }
}
