/*
 * Copyright (c) 2003-2011, Simon Brown
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 *   - Neither the name of Pebble nor the names of its contributors may
 *     be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.sourceforge.pebble.web.action;

import net.sourceforge.pebble.Constants;
import net.sourceforge.pebble.PebbleContext;
import net.sourceforge.pebble.domain.FileManager;
import net.sourceforge.pebble.domain.FileMetaData;
import net.sourceforge.pebble.domain.Blog;
import net.sourceforge.pebble.domain.BlogManager;
import net.sourceforge.pebble.web.view.RedirectView;
import net.sourceforge.pebble.web.view.View;
import net.sourceforge.pebble.web.view.impl.FileTooLargeView;
import net.sourceforge.pebble.web.view.impl.NotEnoughSpaceView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Superclass for actions that allow the user to upload a file.
 *
 * @author    Simon Brown
 */
public abstract class UploadFileAction extends AbstractFileAction {

  private static final Log log = LogFactory.getLog(UploadFileAction.class);

  /**
   * Peforms the processing associated with this action.
   * Phase 3B-R: Replaced commons-fileupload with Spring Native Multipart support
   *
   * @param request  the HttpServletRequest instance
   * @param response the HttpServletResponse instance
   * @return the name of the next view
   */
  public View process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    Blog blog = (Blog)getModel().get(Constants.BLOG_KEY);

    String type = getType();
    String path = "";
    String[] filenames = new String[10];

    FileManager fileManager = new FileManager(blog, type);

    try {
      // Spring Native Multipart: Check if request is multipart
      if (request instanceof MultipartHttpServletRequest) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        // Get path parameter from regular request parameters
        path = multipartRequest.getParameter("path");
        if (path == null) {
          path = "";
        }

        // Get custom filename parameters (filename0, filename1, etc.)
        for (int i = 0; i < 10; i++) {
          String filename = multipartRequest.getParameter("filename" + i);
          if (filename != null && !filename.isEmpty()) {
            filenames[i] = filename;
            log.debug("index is " + i + ", filename is " + filenames[i]);
          }
        }

        // Process uploaded files
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
          String fieldName = entry.getKey();
          MultipartFile multipartFile = entry.getValue();

          // Process files with field names starting with "file" (file0, file1, etc.)
          if (fieldName.startsWith("file") && !multipartFile.isEmpty()) {
            int index = Integer.parseInt(fieldName.substring(fieldName.length() - 1));

            // if the filename hasn't been specified, use that from the file being uploaded
            if (filenames[index] == null || filenames[index].length() == 0) {
              filenames[index] = multipartFile.getOriginalFilename();
            }

            File destinationDirectory = fileManager.getFile(path);
            File file = new File(destinationDirectory, filenames[index]);

            // Security: Verify file is underneath root directory (prevent path traversal)
            if (!fileManager.isUnderneathRootDirectory(file)) {
              response.setStatus(HttpServletResponse.SC_FORBIDDEN);
              return null;
            }

            long itemSize = multipartFile.getSize() / 1024;
            if (FileManager.hasEnoughSpace(blog, itemSize)) {
              log.debug("Writing file " + filenames[index] + ", size is " + multipartFile.getSize());
              writeFile(fileManager, path, filenames[index], multipartFile);

              // if it's a theme file, also create a copy in blog.dir/theme
              if (type.equals(FileMetaData.THEME_FILE)) {
                writeFile(new FileManager(blog, FileMetaData.BLOG_DATA), "/theme" + path, filenames[index], multipartFile);
              }
            } else {
              return new NotEnoughSpaceView();
            }
          }
        }
      }

      blog.info("Files uploaded.");
    } catch (MaxUploadSizeExceededException e) {
      // Spring throws this when file size exceeds configured maximum
      return new FileTooLargeView();
    } catch (Exception e) {
      throw new ServletException(e);
    }

    FileMetaData directory = fileManager.getFileMetaData(path);

    return new RedirectView(blog.getUrl() + directory.getUrl());
  }

  /**
   * Helper method to write a file using Spring MultipartFile.
   * Phase 3B-R: Updated to use Spring Native Multipart instead of commons-fileupload
   *
   * @param fileManager    a FileManager instance
   * @param path           the path where to save the file
   * @param filename       the filename
   * @param multipartFile  the uploaded Spring MultipartFile
   * @throws Exception     if something goes wrong writing the file
   */
  private void writeFile(FileManager fileManager, String path, String filename, MultipartFile multipartFile) throws Exception {
    File destinationDirectory = fileManager.getFile(path);
    destinationDirectory.mkdirs();

    File file = new File(destinationDirectory, filename);
    multipartFile.transferTo(file);
  }

  /**
   * Gets the type of this upload (blog image, blog file or theme file).
   *
   * @return    a String representing the type
   * @see       net.sourceforge.pebble.domain.FileMetaData
   */
  protected abstract String getType();

  /**
   * Gets a list of all roles that are allowed to access this action.
   *
   * @return  an array of Strings representing role names
   */
  public abstract String[] getRoles(HttpServletRequest request);

}