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

package edu.umd.cs.marmoset.utilities;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.CopyUtils;
import org.apache.commons.io.IOUtils;

import edu.umd.cs.marmoset.modelClasses.Submission;

public class DirectSubmissionUpload
{

    /**
     * @param args
     */
    public static void main(String[] args)
    throws IOException, SQLException
    {
        if (args.length < 5) {
            System.err.println("Usage: java DirectSubmissionUpload \n " +
                    "<participants file> \n" +
                    "<coureName> \n" +
                    "<semester> \n" +
                    "<directory w/ snapshots> \n" +
                    "<database url> \n" +
                    "<p1> [ <p2> ... <pN> ]");
            System.exit(1);
        }

        File file=new File(args[0]);
        String courseName=args[1];
        String semester=args[2];
        String prefix=args[3];
        String dbUrl=args[4];

        String[] projectArr = new String[args.length-5];
        for (int ii=5; ii < args.length; ii++) {
            projectArr[ii-5] = args[ii];
        }

        upload(file, projectArr, courseName, semester, prefix, dbUrl);
    }

    private static void upload(File inputFile,
        String[] projectArr,
        String courseName,
        String semester,
        String prefix,
        String dbUrl)
    throws SQLException, IOException
    {
        Connection conn=null;

        try {
            conn=DatabaseUtilities.getConnection(dbUrl);
            System.out.println("Got database connection from " +dbUrl);

            // read the file full of cvs accounts into a collection
            List<String> accountList = new LinkedList<String>();
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            while (true) {
                String classAccount = reader.readLine();
                if (classAccount == null) break;
                classAccount = classAccount.replaceAll("#.*", "");
                if ("".equals(classAccount))
                    continue;
                accountList.add(classAccount);
            }
            reader.close();

            // iterate through the list of projects
            for (String projectNumber : projectArr) {
                // iterate through each classAccount
                for (String classAccount : accountList) {
                    String location = prefix +"/"+ classAccount+ "/" +projectNumber;
                    File dir = new File(location);
                    if (!dir.isDirectory()) {
                        System.err.println(location + " is not a directory");
                        continue;
                    }

                    File[] fileArr = dir.listFiles();
                    System.out.println(classAccount + " made " +fileArr.length+ " submissions for " +projectNumber);
                    int inserted=0;
                    for (File submission : fileArr) {
                        //System.out.println(file);
                        try {
                            String result=submit(submission, courseName, semester, conn);
                            if (result!=null) {
                                System.out.print(".");
                                inserted++;
                            }
                        } catch (SQLException e) {
                            System.out.println("SQLException! " +e);
                            //e.printStackTrace();
                        } catch (IOException e) {
                            System.out.println("IOException! " +e);
                            //e.printStackTrace();
                        }
                    }
                    System.out.println("\nInserted " +inserted);
                }
            }
        } finally {
            DatabaseUtilities.releaseConnection(conn);
        }
    }

    private static String submit(File file, String courseName, String semester, Connection conn)
    throws IOException, SQLException
    {
        String filename = file.getName();
        String[] tokens = filename.split("\\.");
        String projectNumber = tokens[0];
        String classAccount = tokens[1];
        long commitCvstag = Long.parseLong(tokens[2]);
        // cvsTagTimestamp can be null but *NOT* empty in the DB!
        String cvsTagTimestamp = tokens[3];
        if ("".equals(cvsTagTimestamp))
            cvsTagTimestamp = null;
        String zipExtension = tokens[4];
        assert zipExtension.equals(".zip");

          // copy the fileItem into a byte array
        FileInputStream fis = null;
        ByteArrayOutputStream bytes =null;
        try {
            fis = new FileInputStream(file);
            bytes = new ByteArrayOutputStream();
            CopyUtils.copy(fis, bytes);

            Submission submission=Submission.submitOneSubmission(
                bytes.toByteArray(),
                cvsTagTimestamp,
                classAccount,
                projectNumber,
                courseName,
                semester,
                conn);

            String result = "semester: " +semester+"\n"+
            "courseName: " +courseName+"\n"+
            "cvsTagTimestamp: " +cvsTagTimestamp+"\n"+
            "projectNumber: " +projectNumber+"\n"+
            "classAccount: " +classAccount;

            if (submission == null) {
                return "A duplicate!\n" + result;
            }
            return "Successfully inserted\n" +result;
        } finally {
            IOUtils.closeQuietly(fis);
            IOUtils.closeQuietly(bytes);
        }
    }
}
