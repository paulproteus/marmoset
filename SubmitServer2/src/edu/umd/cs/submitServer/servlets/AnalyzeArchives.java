/**
 * Marmoset: a student project snapshot, submission, testing and code review
 * system developed by the Univ. of Maryland, College Park
 * 
 * Developed as part of Jaime Spacco's Ph.D. thesis work, continuing effort led
 * by William Pugh. See http://marmoset.cs.umd.edu/
 * 
 * Copyright 2005 - 2011, Univ. of Maryland
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */

package edu.umd.cs.submitServer.servlets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.Checksum;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.umd.cs.marmoset.modelClasses.Archive;
import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.FileContents;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.utilities.Checksums;
import edu.umd.cs.marmoset.utilities.TextUtilities;

public class AnalyzeArchives extends SubmitServerServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    Connection conn = null;
    response.setContentType("text/plain");
    PrintWriter writer = response.getWriter();

    Project project = (Project) request.getAttribute("project");
    Course course = (Course) request.getAttribute("course");

    long totalArchiveSpace = 0;
    long totalDistinctArchiveSpace = 0;
    HashSet<Integer> seen = new HashSet<Integer>();
    
    HashMap<String, FileContents> archiveContents = new HashMap<String, FileContents>();
    Multiset<String> files = HashMultiset.create();
    Multiset<String> checksums = HashMultiset.create();
    try {
      conn = getConnection();
      List<Integer> archives = Submission.getAllArchivesForProject(project.getProjectPK(), conn);
      writer.printf("Analyzing %d submissions for %s project %s%n", archives.size(), course.getCourseName(),
          project.getProjectNumber());
      for (Integer archivePK : archives) {
        byte[] bytes = Archive.downloadBytesFromArchive((String) Submission.SUBMISSION_ARCHIVES, (Integer) archivePK, (Connection) conn);
        totalArchiveSpace += bytes.length;
        if (!seen.add(archivePK)) 
          continue;
        totalDistinctArchiveSpace += bytes.length;
        TreeMap<String, byte[]> contents = Archive.unzip(new ByteArrayInputStream(bytes));

        for (Map.Entry<String, byte[]> e : contents.entrySet()) {
          byte[] archiveBytes = e.getValue();
          String checksum = Checksums.getChecksum(archiveBytes);
          String name = e.getKey();
          files.add(name);
          checksums.add(checksum);
          FileContents info = archiveContents.get(checksum);
          if (info == null) {

            info = new FileContents(name, TextUtilities.isText(TextUtilities.simpleName(name)),
                archiveBytes.length, checksum, null);
            archiveContents.put(checksum, info);
          }

        }

      }

    } catch (SQLException e) {
      throw new ServletException(e);
    } finally {
      releaseConnection(conn);
    }
    long totalSize = 0;
    TreeSet<FileContents> ordered = new TreeSet<FileContents>(archiveContents.values());
    writer.printf("%5s %9s %s%n", "#", "size", "name");

    String prevName = null;
    for (FileContents info : ordered) {
      if (prevName == null || !prevName.equals(info.name)) {
        if (prevName != null)
          writer.println();
        writer.printf("%5d %9s %s%n", files.count(info.name), " ", info.name);
        prevName = info.name;
      }
      int count = checksums.count(info.checksum);
      writer.printf("%5d %9d %s%n", count, info.size, info.name);
      totalSize += info.size;
    }
    writer.printf("%n");
    writer.printf("%d distinct archives%n", seen.size());
    writer.printf("%d distinct files%n", files.elementSet().size());
    writer.printf("%d total files%n", files.size());

    writer.printf("%d bytes in distinct archives%n", totalDistinctArchiveSpace);
    writer.printf("%d bytes in repeated archives%n", totalArchiveSpace);
    writer.printf("%d bytes as files%n", totalSize);
  }

}
