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
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import edu.umd.cs.marmoset.modelClasses.Archive;
import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.Submission;

public class AnalyzeArchives extends SubmitServerServlet {

  static class Info implements Comparable<Info> {
    final String name;
    final int size;
    final BigInteger hash;
    int count = 0;

    public Info(String name, int size, BigInteger hash) {
      this.name = name;
      this.size = size;
      this.hash = hash;
    }

    public void saw() {
      count++;
    }

    @Override
    public int compareTo(Info that) {
      int result = this.name.compareTo(that.name);
      if (result != 0)
        return result;
      result = this.size - that.size;
      if (result != 0)
        return result;
      result = this.hash.compareTo(that.hash);

      return result;

    }

  }

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
    
    MessageDigest digest = getSHA1();
    HashMap<BigInteger, Info> archiveContents = new HashMap<BigInteger, Info>();
    Multiset<String> files = HashMultiset.create();
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
          digest.reset();
          byte[] archiveBytes = e.getValue();
          String name = e.getKey();
          files.add(name);
          byte[] hash = digest.digest(archiveBytes);
          BigInteger biHash = new BigInteger(hash);
          Info info = archiveContents.get(biHash);
          if (info == null) {

            info = new Info(name, archiveBytes.length, biHash);
            archiveContents.put(biHash, info);
          }
          info.saw();

        }

      }

    } catch (SQLException e) {
      throw new ServletException(e);
    } finally {
      releaseConnection(conn);
    }
    long totalSize = 0;
    TreeSet<Info> ordered = new TreeSet<Info>(archiveContents.values());
    writer.printf("%5s %9s %s%n", "#", "size", "name");

    String prevName = null;
    for (Info info : ordered) {
      if (prevName == null || !prevName.equals(info.name)) {
        if (prevName != null)
          writer.println();
        writer.printf("%5d %9s %s%n", files.count(info.name), " ", info.name);
        prevName = info.name;
      }
      writer.printf("%5d %9d %s%n", info.count, info.size, info.name);
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
