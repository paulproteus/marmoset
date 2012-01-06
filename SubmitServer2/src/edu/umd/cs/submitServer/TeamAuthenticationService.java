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

/**
 * Created on Dec 19, 2005
 *
 * @author jspacco
 */

package edu.umd.cs.submitServer;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Student;
import edu.umd.cs.submitServer.servlets.PerformLogin;

/**
 * TeamAuthenticationService: authenticate a team by authenticating its members
 * 
 * @author ayewah
 */
public class TeamAuthenticationService implements IAuthenticationService {

	@Override
	public void initialize(ServletContext context) {
		return;
	}

	@Override
	public Student authenticateLDAP(String campusUID, String uidPassword,
			Connection conn, boolean skipLDAP) throws SQLException,
			NamingException, ClientRequestException {
		return authenticateLDAP(campusUID, uidPassword, conn, skipLDAP,
				new GenericStudentPasswordAuthenticationService());
	}

	public Student authenticateLDAP(String campusUID, String uidPassword,
			Connection conn, boolean skipLDAP,
			IAuthenticationService authenticationService) throws SQLException,
			NamingException, ClientRequestException {
		Student student = Student.lookupByLoginName(campusUID, conn);
		if (student == null)
			throw new ClientRequestException("Cannot find user " + campusUID);
		if (skipLDAP)
			return student;
		if (!student.isTeamAccount())
			throw new ClientRequestException("Invalid authentication. "
					+ campusUID + " is not a team account.");

		// check password
		if (uidPassword == null || uidPassword.trim().equals(""))
			throw new BadPasswordException(HttpServletResponse.SC_UNAUTHORIZED,
					"Missing password for team " + campusUID);

		// Check team accounts
		if (student.getPassword() == null)
			throw new BadPasswordException(HttpServletResponse.SC_UNAUTHORIZED,
					"Invalid password in database for team " + campusUID);

		// get team members studentPKs and corresponding passwords
		String[] studentPKs = Student.parseTeamPassword(student.getPassword());
		String[] uidPasswords = uidPassword.trim().split("\\s+");

		if (studentPKs.length != uidPasswords.length)
			throw new BadPasswordException(HttpServletResponse.SC_UNAUTHORIZED,
					"Invalid Password for Team (incorrect number of passwords)"
							+ campusUID);

		// authenticate each student
		for (int i = 0; i < studentPKs.length; i++) {
			Student tempStudent = Student.lookupByStudentPK(
					Student.asPK(Integer.parseInt(studentPKs[i])), conn);
			if (tempStudent == null)
				throw new ClientRequestException(
						"Invalid Team (missing members): " + campusUID);
			PerformLogin.authenticateStudent(conn, tempStudent.getLoginName(),
					uidPasswords[i], skipLDAP, authenticationService);
		}
		// if no errors, then student is authenticated
		return student;
	}

}
