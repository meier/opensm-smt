/************************************************************
 * Copyright (c) 2015, Lawrence Livermore National Security, LLC.
 * Produced at the Lawrence Livermore National Laboratory.
 * Written by Timothy Meier, meier3@llnl.gov, All rights reserved.
 * LLNL-CODE-673346
 *
 * This file is part of the OpenSM Monitoring Service (OMS) package.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (as published by
 * the Free Software Foundation) version 2.1 dated February 1999.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * OUR NOTICE AND TERMS AND CONDITIONS OF THE GNU GENERAL PUBLIC LICENSE
 *
 * Our Preamble Notice
 *
 * A. This notice is required to be provided under our contract with the U.S.
 * Department of Energy (DOE). This work was produced at the Lawrence Livermore
 * National Laboratory under Contract No.  DE-AC52-07NA27344 with the DOE.
 *
 * B. Neither the United States Government nor Lawrence Livermore National
 * Security, LLC nor any of their employees, makes any warranty, express or
 * implied, or assumes any liability or responsibility for the accuracy,
 * completeness, or usefulness of any information, apparatus, product, or
 * process disclosed, or represents that its use would not infringe privately-
 * owned rights.
 *
 * C. Also, reference herein to any specific commercial products, process, or
 * services by trade name, trademark, manufacturer or otherwise does not
 * necessarily constitute or imply its endorsement, recommendation, or favoring
 * by the United States Government or Lawrence Livermore National Security,
 * LLC. The views and opinions of authors expressed herein do not necessarily
 * state or reflect those of the United States Government or Lawrence Livermore
 * National Security, LLC, and shall not be used for advertising or product
 * endorsement purposes.
 *
 *        file: ZipUtil.java
 *
 *  Created on: Jan 9, 2014
 *      Author: meier3
 ********************************************************************/
package gov.llnl.lc.util;

import gov.llnl.lc.logging.CommonLogger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**********************************************************************
 * ZipUtil is a static utility class that can compress a list of files 
 * to the standard ZIP format, and can also extract files from a ZIP file.
 * <p>
 * 
 * @see ZipFile
 * 
 * @author meier3
 * 
 * @version Jan 9, 2014 10:12:38 AM
 **********************************************************************/
public class ZipUtil implements CommonLogger
{
  /**
   * A constant for the buffer size used to read/write data
   */
  private static final int BUFFER_SIZE = 4096;

  /************************************************************
   * Method Name:
   *  zipFiles
  **/
  /**
   * Compress a collection of files (or directories) to a destination
   * zip file.
   *
   * @param listFiles       A collection of files and directories
   * @param destZipFile     The path of the destination zip file
   * @throws FileNotFoundException
   * @throws IOException
   ***********************************************************/
  public static void zipFiles(List<File> listFiles, String destZipFile) throws FileNotFoundException, IOException
  {
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destZipFile));

    for (File file : listFiles)
    {
      if (file.isDirectory())
        addFolderToZip(file, file.getName(), zos);
      else
        addFileToZip(file, "", zos);
     }
    zos.flush();
    zos.close();
  }

  /************************************************************
   * Method Name:
   *  addFolderToZip
  **/
  /**
   * An internal method for adding a parent directory to the zip file.
   * This method is recursive, in that it will add not only the parent
   * folder and files in it, but also sub-directories and their files.
   *
   * @see     ZipOutputStream
   *
   * @param folder        the directory to add
   * @param parentFolder  the name of the directory to add
   * @param zos           the output stream to the zip file
   * @throws FileNotFoundException
   * @throws IOException
   ***********************************************************/
  private static void addFolderToZip(File folder, String parentFolder, ZipOutputStream zos) throws FileNotFoundException, IOException
  {
    logger.info("Adding Folder (" + parentFolder + ") to zip file");
    
    // make sure directories or folders end with a slash, so zip knows is a directory entry
    zos.putNextEntry(new ZipEntry(parentFolder + File.separator));
    
    for (File file : folder.listFiles())
    {
      // a folder can contain folders (subdirectories)...
      if (file.isDirectory())
      {
        // recursive - make sure it has the correct path for zip
        addFolderToZip(file, parentFolder + File.separator + file.getName(), zos);
        continue;
      }
      // but if its just a file, then just add it
      addFileToZip(file, parentFolder, zos);
      zos.closeEntry();
    }
  }

  /************************************************************
   * Method Name:
   *  addFileToZip
  **/
  /**
   * Adds, and writes, the specified file to the zip file.
   *
   * @see     ZipOutputStream
   *
   * @param file           the file to add
   * @param parentFolder   the name of the directory that contains this file
   * @param zos            the output stream for the zip file
   * @throws FileNotFoundException
   * @throws IOException
   ***********************************************************/
  private static void addFileToZip(File file, String parentFolder, ZipOutputStream zos) throws FileNotFoundException, IOException
  {
    String FileName = parentFolder + File.separator + file.getName();
    logger.info("Adding File (" + FileName + ") to zip file");
    zos.putNextEntry(new ZipEntry(FileName));

    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

    long bytesRead = 0;
    byte[] bytesIn = new byte[BUFFER_SIZE];
    int read = 0;

    while ((read = bis.read(bytesIn)) != -1)
    {
      zos.write(bytesIn, 0, read);
      bytesRead += read;
    }
    logger.info("  " + FileName + " bytes: " + bytesRead);

    bis.close();
    zos.closeEntry();
  }
  
  /************************************************************
   * Method Name:
   *  listFiles
  **/
  /**
   * Writes the contents of the zip file to stdout.
   *
   * @see     ZipUtil.getFileList()
   *
   * @param zipFilePath   the name of the zip file to list
   * @return              a list of Strings, representing the contents of the zip file
   * @throws IOException
   ***********************************************************/
  public static ArrayList<String> listFiles(String zipFilePath) throws IOException
  {
    java.util.ArrayList <String> sArray = getFileList(zipFilePath);  
    for(String filePath : sArray)
      System.out.println("Entry (" + filePath + ")");
    return sArray;
  }

  /************************************************************
   * Method Name:
   *  getFileList
  **/
  /**
   * Obtains an ArrayList of the contents of the zip file (files and directories).
   *
   * @see     ZipFile
   *
   * @param zipFilePath   the name of the zip file
   * @return              a list of Strings, representing the contents of the zip file
   * @throws IOException
   ***********************************************************/
  public static ArrayList<String> getFileList(String zipFilePath) throws IOException
  {
    java.util.ArrayList <String> sArray = new java.util.ArrayList<String>(); 
    ZipFile zFile = new ZipFile(zipFilePath);
    Enumeration<? extends ZipEntry> entries = zFile.entries();
    
    while (entries.hasMoreElements())
    {
      ZipEntry entry = entries.nextElement();
      String filePath = entry.getName();
      sArray.add(filePath);
    }
    return sArray;
  }

  /************************************************************
   * Method Name:
   *  unzipFile
  **/
  /**
   * Extract and uncompress the contents of a zip file into the
   * designated destination directory.
   *
   * @see     ZipInputStream
   *
   * @param zipFilePath     the path of the desired zip file
   * @param destDirectory   the path of the destination directory
   * @throws IOException
   ***********************************************************/
  public static void unzipFile(String zipFilePath, String destDirectory) throws IOException
  {
    File destDir = new File(destDirectory);
    if (!destDir.exists())
    {
      logger.info("Making the parent Zip Directory (" + destDirectory + ")");
      destDir.mkdirs();
    }

    ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
    ZipEntry entry = zipIn.getNextEntry();

    while (entry != null)
    {
      String filePath = destDirectory + File.separator + entry.getName();
      if (!entry.isDirectory())
        extractFile(zipIn, filePath);
      else
      {
        logger.info("Making Zip subdirectory (" + filePath + ")");
        File dir = new File(filePath);
        dir.mkdirs();
      }
      zipIn.closeEntry();
      entry = zipIn.getNextEntry();
    }
    zipIn.close();
  }

  /************************************************************
   * Method Name:
   *  extractFile
  **/
  /**
   * This private helper method does the actual reading of the zip file
   * and writing to the output file.
   *
   * @see     ZipInputStream
   *
   * @param zipIn         the input stream corresponding the the file to extract
   * @param filePath      the location for the extracted (destination) file
   * @throws IOException
   ***********************************************************/
  private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException
  {
    logger.info("Extracting zip file (" + filePath + ")");
    
    // make sure the path exists, all the way
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
    byte[] bytesIn = new byte[BUFFER_SIZE];
    int read = 0;
    while ((read = zipIn.read(bytesIn)) != -1)
    {
      bos.write(bytesIn, 0, read);
    }
    bos.close();
  }

  /************************************************************
   * Method Name: main
   **/
  /**
   * Test the three main public methods of this utility class
   * 
   * @see ZipUtil
   * 
   * @param args    ignored
   ***********************************************************/
  public static void main(String[] args) throws Exception
  {
    // test compress files
    File directoryToZip = new File("/home/meier3/scripts");
    String zipFilePath = "/home/meier3/myScripts.zip";
    List<File> listFiles = new ArrayList<File>();
    listFiles.add(directoryToZip);

    ZipUtil.zipFiles(listFiles, zipFilePath);

    ZipUtil.listFiles(zipFilePath);

    // test decompress a zip file
    String destFilePath = "/home/meier3/tmp/ZipTest";
    ZipUtil.unzipFile(zipFilePath, destFilePath);
    System.out.println("done");
  }
}
