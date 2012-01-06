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

package edu.umd.cs.submitServer;



import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.umd.cs.marmoset.modelClasses.Project;
import edu.umd.cs.marmoset.modelClasses.StudentRegistration;
import edu.umd.cs.marmoset.modelClasses.Submission;
import edu.umd.cs.marmoset.utilities.DatabaseUtilities;
import edu.umd.cs.submitServer.policy.ReleaseTestAwareSubmissionPolicy;

/**
 * ReleaseTestAwareBestSubmissionPolicy
 * @author jspacco
 */
public class ReleaseTestAwareBestSubmissionPolicyTest
{

    /**
     * @param args
     */
    public static void main(String[] args)
    throws SQLException
    {
        System.out.println("Hello world");
        
        ReleaseTestAwareSubmissionPolicy policy=new ReleaseTestAwareSubmissionPolicy();
        Connection conn=null;
        try {
            conn=DatabaseUtilities.getConnection();
            
            int projectPK = 21;
            Project project=Project.lookupByProjectPK(projectPK,conn);
            System.out.println("project = " +project);
            
            Map<Integer,Submission> ontimeMap=policy.lookupChosenOntimeSubmissionMap(project,conn);
            Map<Integer,Submission> lateMap=policy.lookupChosenLateSubmissionMap(project,conn);
            
            Set<StudentRegistration> set=new TreeSet<StudentRegistration>();
            set.addAll(StudentRegistration.lookupAllWithAtLeastOneSubmissionByProjectPK(projectPK,conn));
            Map<Integer,Submission> best=policy.getChosenSubmissionMap(set,ontimeMap,lateMap);
            
            for (Submission submission: best.values()) {
                if (submission.getStudentRegistrationPK() == 113 ||
                    submission.getAdjustedScore() < 0)
                    System.out.println(submission.getStudentRegistrationPK() +" => "+
                        submission.getAdjustedScore()
                        + ", submissionPK = " +submission.getSubmissionPK());
            }
            
        } finally {
            DatabaseUtilities.releaseConnection(conn);
        }

    }

}
