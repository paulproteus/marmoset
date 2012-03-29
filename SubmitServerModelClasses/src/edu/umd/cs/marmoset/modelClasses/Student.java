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

package edu.umd.cs.marmoset.modelClasses;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.meta.TypeQualifier;

import edu.umd.cs.marmoset.utilities.SqlUtilities;

/**
 * @author jspacco
 * TODO refactor getAllFromPreparedStatement() and getFromPreparedStatement() so that they
 * don't try to close the statement themselves but rather allow their callers to do so.  The
 * way the code is currently structured could leak statements if any of the setString() methods
 * fail (though I don't think matters in practice since the connections all get closed anyway).
 */
public class Student  implements Comparable<Student> {

  static final boolean FAKE_NAMES = false;

  static {
      if (FAKE_NAMES)
          System.out.println("Using fake names");
  }

  @Documented
  @TypeQualifier(applicableTo = Integer.class)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface PK {}

  public static @PK int asPK( int pk) {
    return pk;
  }
  public static @PK Integer asPK(  Integer pk) {
    return pk;
  }


  private @Student.PK Integer studentPK; // non-NULL autoincrement
  private String loginName;
  private String campusUID;
  private String firstname;
  private String lastname;
  private boolean superUser = false;
  private String givenConsent = PENDING;
  private String accountType=NORMAL_ACCOUNT;
  private String email;
  private boolean hasPicture;
  private boolean canImportCourses;


  public static final @Deprecated  String DEMO_ACCOUNT = "demo";
  public static final String NORMAL_ACCOUNT = "normal";
  public static final String PSEUDO_ACCOUNT = "pseudo";
  // [NAT]
  public static final @Deprecated String TEAM_ACCOUNT = "team";

  public static final String TABLE_NAME="students";



  /**
   *  List of all attributes of students table.
   */
    static final String[] ATTRIBUTE_NAME_LIST = {
      "student_pk","login_name","campus_uid",
      "firstname","lastname","superuser","given_consent",
      "account_type", "email", "has_picture",
      "can_import_courses"
   };

   public static final String ADMIN_SUFFIX = "-admin";
   public static final String STUDENT_SUFFIX = "-student";

  public static String stripSuffixForLdap(String loginName) {

    if (loginName.endsWith(ADMIN_SUFFIX))
      loginName = loginName.substring(0, loginName.length()
          - ADMIN_SUFFIX.length());
    if (loginName.endsWith(STUDENT_SUFFIX))
      loginName = loginName.substring(0, loginName.length()
          - STUDENT_SUFFIX.length());
    return loginName;
  }

  public boolean hasLoginSuffix() {

    if (loginName.endsWith(ADMIN_SUFFIX))
      return true;
    if (loginName.endsWith(STUDENT_SUFFIX))
      return true;
    return false;
  }
  /**
   * Fully-qualified attributes for students table.
   */
   public static final String ATTRIBUTES =
    Queries.getAttributeList("students", ATTRIBUTE_NAME_LIST);

   // constants related to consent forms
   public static final String CONSENTED = "yes";
   public static final String NOT_CONSENTED = "no";
   public static final String UNDER_18 = "under 18";
   public static final String PENDING = "pending";


  @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((studentPK == null) ? 0 : studentPK.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Student))
            return false;
        Student other = (Student) obj;
        if (studentPK == null) {
            if (other.studentPK != null)
                return false;
        } else if (!studentPK.equals(other.studentPK))
            return false;
        return true;
    }
    

    @Override
    public String toString() {
        return getFullname() + "; studentPK=" + studentPK;
    }

    public void setSuperUser(boolean superUser) {
        this.superUser = superUser;
    }
  /**
   * @return Returns the loginName.
   */
  public String getLoginName() {
    return loginName;
  }
  /**
   * @param campusUID The loginName to set.
   */
  public void setLoginName(String loginName) {
    this.loginName = loginName;
  }
  /**
   * @return Returns the uid.
   */
  public String getCampusUID() {
    return campusUID;
  }
  /**
   * @param uid The uid to set.
   */
  public void setCampusUID(String uid) {
    this.campusUID = uid;
  }
  /**
   * @return Returns the firstname.
   */
  public String getFirstname() {
    if (FAKE_NAMES)
      return FakeNames.getFirstname(studentPK);
    return firstname;
  }
  /**
   * @param firstname The firstname to set.
   */
  public void setFirstname(String firstname) {
    if (FAKE_NAMES)
      return;
    this.firstname = firstname;
  }
  /**
   * @return Returns the lastname.
   */
  public String getLastname() {
    if (FAKE_NAMES)
      return FakeNames.getLastname(studentPK);

    return lastname;
  }
  /**
   * @param lastname The lastname to set.
   */
  public void setLastname(String lastname) {
    if (FAKE_NAMES)
      return;
    this.lastname = lastname;
  }
  public String getFullname() {
    if (FAKE_NAMES)
      return FakeNames.getFullname(studentPK);
    return firstname + " " + lastname + " (" +loginName +")";
  }
  /**
   * @return Returns the studentPK.
   */
  public @Student.PK int getStudentPK() {
    return studentPK;
  }
  /**
   * @param studentPK The studentPK to set.
   */
  public void setStudentPK(@Student.PK Integer studentPK) {
    this.studentPK = studentPK;
  }
    /**
     * @return Returns the givenConsent.
     */
    public String getGivenConsent() {
        return givenConsent;
    }
    /**
     * @param givenConsent The givenConsent to set.
     */
    public void setGivenConsent(String givenConsent) {
        this.givenConsent = givenConsent;
    }

    /**
     * @return account type e.g. team, demo, normal
     */
    public String getAccountType() {
        return accountType;
    }

    /**
     * @param accountType - set account type to this value
     */
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    /**
     * @return true if this is a team account, false otherwise
     */
    public @Deprecated boolean isTeamAccount() {
      return TEAM_ACCOUNT.equals(getAccountType());
    }
    
    public boolean isPseudoAccount() {
        return PSEUDO_ACCOUNT.equals(getAccountType());
    }

     public String getEmail() {
        if (FAKE_NAMES) {
            return FakeNames.getAccount(studentPK) + "@terpmail.umd.edu";
        }
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }
  public boolean getHasPicture() {
    if (FAKE_NAMES)
      return false;
    return hasPicture;
  }
  public void setHasPicture(boolean hasPicture) {
    if (FAKE_NAMES)
      return;
    this.hasPicture = hasPicture;
  }
  public boolean getCanImportCourses() {
    return canImportCourses || isSuperUser();
  }
  public void setCanImportCourses(boolean canImportCourses) {
    this.canImportCourses = canImportCourses;
  }
  public int fetchValues(ResultSet resultSet, int startingFrom) throws SQLException {
    setStudentPK(asPK(resultSet.getInt(startingFrom++)));
    setLoginName(resultSet.getString(startingFrom++));
    setCampusUID(resultSet.getString(startingFrom++));
    setFirstname(resultSet.getString(startingFrom++));
    setLastname(resultSet.getString(startingFrom++));
    setSuperUser(resultSet.getBoolean(startingFrom++));
    setGivenConsent(resultSet.getString(startingFrom++));
    setAccountType(resultSet.getString(startingFrom++));
    setEmail(resultSet.getString(startingFrom++));
    setHasPicture(resultSet.getBoolean(startingFrom++));
    setCanImportCourses(resultSet.getBoolean(startingFrom++));

    return startingFrom;
  }

  /**
   * Inserts a student into the database (using conn) with a fresh
   * primary key.
   *
   * Checks for duplicates and on a duplicate, throws an exception
   *
   * @param conn the connection to the database
   * @throws SQLException if something goes wrong or a duplicate is found
   */
  public Student insert(Connection conn) throws SQLException {
    Student student = lookupByCampusUID(getCampusUID(), conn);

    if (student != null && student.getLoginName().equals(getLoginName())) {
      return student;
    }
    student = lookupByLoginName(getLoginName(), conn);
    if (student != null) {
      // record exists so cannot insert it
      throw new SQLException("A record with directory id "
          + getLoginName() + " already exists");
    }
    executeInsert(conn);
    return this;
  }

  public static Student insertOrUpdateByUID(String campusUID,
        String firstname,
        String lastname,
        String loginName,
        @Nullable String email,
        Connection conn) throws SQLException {
    if (FAKE_NAMES) {
      throw new IllegalStateException();
    }
		Student student = lookupByLoginNameAndCampusUID(loginName, campusUID, conn);
		if (student == null)
			student = lookupByCampusUID(campusUID, conn);
		if (student == null) {
			student = new Student();
			student.setLoginName(loginName);
			student.setCampusUID(campusUID);
			student.setFirstname(firstname);
			student.setLastname(lastname);
			if (email != null) {
				student.setEmail(email);
			}
			student.insert(conn);
		} else if (student.loginName.endsWith(ADMIN_SUFFIX)) {
			// do nothing
		} else if (student.loginName.endsWith(STUDENT_SUFFIX)) {
			// do nothing
		} else if (!student.lastname.equals(lastname) || !student.firstname.equals(firstname)
		    || !student.loginName.equals(loginName)) {
			student.lastname = lastname;
			student.firstname = firstname;
			student.loginName = loginName;
			student.update(conn);
		}
		return student;
	}

  public static Student insertOrUpdateByLoginNameAndCampusUID(String campusUID,
      String firstname,
      String lastname,
      String loginName,
      Connection conn) throws SQLException {
    if (FAKE_NAMES)
      throw new IllegalStateException();
    Student student = lookupByLoginNameAndCampusUID(loginName, campusUID, conn);
    if (student == null) {
      student = new Student();
          student.setLoginName(loginName);
          student.setCampusUID(campusUID);
          student.setFirstname(firstname);
          student.setLastname(lastname);
          student.insert(conn);
    } else if (student.loginName.endsWith(ADMIN_SUFFIX)) {
      // do nothing
    } else if (student.loginName.endsWith(STUDENT_SUFFIX)) {
      // do nothing
    } else if (!student.lastname.equals(lastname)
        || !student.firstname.equals(firstname)) {
      student.lastname = lastname;
      student.firstname = firstname;
      student.loginName = loginName;
      student.update(conn);
    }
    return student;
    }


  private void executeInsert(Connection conn)
  throws SQLException
  {
    if (FAKE_NAMES)
      throw new IllegalStateException();
      String query = Queries.makeInsertStatementUsingSetSyntax(ATTRIBUTE_NAME_LIST, TABLE_NAME, true);

    PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

    putValues(stmt, 1);
    stmt.executeUpdate();
    setStudentPK(asPK(Queries.getGeneratedPrimaryKey(stmt)));

    Queries.closeStatement(stmt);
  }

  /**
   * If the record doesn't exist, then we insert it.  Otherwise we update the mutable fields.
   *
   * TODO handle givenConsent and superUser fields.
   *
   * @param conn the connection to the database
   * @throws SQLException
   */
  public @CheckReturnValue Student insertOrUpdate(Connection conn)
  throws SQLException
  {
    if (FAKE_NAMES)
      throw new IllegalStateException();
      Student existingStudent = lookupByCampusUID(campusUID, conn);

    if (existingStudent == null) {
      executeInsert(conn);
      return this;
    } else {
      copyTo(conn, existingStudent);
      return existingStudent;

    }
  }
  public void copyTo(Connection conn, Student existingStudent)
      throws SQLException {
    existingStudent.setLoginName(loginName);
    existingStudent.setFirstname(firstname);
    existingStudent.setLastname(lastname);
    existingStudent.setAccountType(accountType);
    existingStudent.setGivenConsent(givenConsent);
    existingStudent.setEmail(email);
    existingStudent.setHasPicture(hasPicture);
    existingStudent.setCanImportCourses(canImportCourses);
    existingStudent.update(conn);
  }
  /**
   * If the record doesn't exist, then we insert it.  Otherwise we update the mutable fields.
   *
   * TODO handle givenConsent and superUser fields.
   *
   * @param conn the connection to the database
   * @throws SQLException
   */
  public @CheckReturnValue Student insertOrUpdateCheckingLoginNameAndCampusUID(Connection conn)
  throws SQLException
  {
    if (FAKE_NAMES)
      throw new IllegalStateException();
      Student existingStudent = lookupByLoginNameAndCampusUID(loginName, campusUID, conn);

    if (existingStudent == null) {
      executeInsert(conn);
      return this;
    } else {
      copyTo(conn, existingStudent);
      return existingStudent;
    }
  }


  public void update(Connection conn)
  throws SQLException
  {
    if (FAKE_NAMES)
      throw new IllegalStateException();
      String update = Queries.makeUpdateStatementWithWhereClause(
              ATTRIBUTE_NAME_LIST,
              "students",
              " WHERE student_pk = ? ");

      PreparedStatement stmt = null;
      try {
          stmt = conn.prepareStatement(update);

          int index = putValues(stmt, 1);
          SqlUtilities.setInteger(stmt, index, getStudentPK());

          stmt.executeUpdate();

      } finally {
          Queries.closeStatement(stmt);
      }
  }

  private int putValues(PreparedStatement stmt, int index)
  throws SQLException
  {
      stmt.setString(index++, getLoginName());
      stmt.setString(index++, getCampusUID());
      stmt.setString(index++, getFirstname());
      stmt.setString(index++, getLastname());
      stmt.setBoolean(index++, superUser);
      stmt.setString(index++, getGivenConsent());
      stmt.setString(index++, getAccountType());
      stmt.setString(index++, email);
      stmt.setBoolean(index++, getHasPicture());
      stmt.setBoolean(index++, getCanImportCourses());
      return index;
  }

  /**
   * Returns true if this student (person, really) is a superuser, false otherwise.
   *
   * Currently the only way to
   */
  public boolean isSuperUser()
  {
    return superUser;
  }

  /**
   * Gets a student based on the studentPK.  This method will never return null; if the
   * a student with the given studentPK cannot be found, an exception is thrown.
   *
   * @param studentPK the PK of the student
   * @param conn the connection to the database
   * @return the student record if it's found.  This method never returns null; we throw
   * an exception if no student exists with the given PK.
   * @throws SQLException
   */
  public static Student getByStudentPK(@Student.PK Integer studentPK, Connection conn)
  throws SQLException
  {
      Student student = lookupByStudentPK(studentPK, conn);
      if (student == null)
          throw new SQLException("Unable to find student with PK: " +studentPK);
      return student;
  }

  /**
   * Looks up a student record based on its PK.
   *
   * @param studentPK the PK of the student record
   * @param connection the connection to the database
   * @return the student record if it is found, null otherwise
   * @throws SQLException
   */
  public static Student lookupByStudentPK(@Student.PK Integer studentPK, Connection connection)
  throws SQLException
  {
      String query = " SELECT " +ATTRIBUTES+
      " FROM students " +
      " WHERE students.student_pk = ? ";

      PreparedStatement stmt = connection.prepareStatement(query);
      SqlUtilities.setInteger(stmt, 1, studentPK);

      return getFromPreparedStatement(stmt);
  }
    /**
     * Looks up all students registered for a course.
     *
     * @param coursePK the PK of the course to lookup
     * @param connection the connection to the database
     * @return the student records if it is found, null otherwise
     * @throws SQLException
     */
    public static Map<Integer, Student> lookupAllByCoursePK(Integer coursePK, Connection connection)
    throws SQLException
    {
        String query = " SELECT " +ATTRIBUTES+
        " FROM students,student_registration  " +
        " WHERE students.student_pk = student_registration.student_pk "   +
        " AND student_registration.course_pk = ?";

        PreparedStatement stmt = connection.prepareStatement(query);
        SqlUtilities.setInteger(stmt, 1, coursePK);

        return getAllFromPreparedStatement(stmt);
    }

    // XXX remove [NAT]
    /**
     * Get all teams registered for a course
     *
     * @param coursePK the PK of the course to lookup
     * @param connection the connection to the database
     * @return the team records if found, null otherwise
     * @throws SQLException
     */
    public static Map<Integer, Student> lookupTeamsByCoursePK(Integer coursePK, Connection connection)
    throws SQLException
    {
        String query = " SELECT " +ATTRIBUTES+
        " FROM students,student_registration  " +
        " WHERE students.student_pk = student_registration.student_pk "   +
        " AND students.account_type = '"  + TEAM_ACCOUNT + "' " +
        " AND student_registration.course_pk = ?";

        PreparedStatement stmt = connection.prepareStatement(query);
        SqlUtilities.setInteger(stmt, 1, coursePK);

        return getAllFromPreparedStatement(stmt);
    }

    /**
     * Looks up a student record based on its PK.
     *
     * @param studentPK the PK of the student record
     * @param connection the connection to the database
     * @return the student record if it is found, null otherwise
     * @throws SQLException
     */
    public static Map<Integer, Student> lookupAll(Connection connection)
    throws SQLException
    {
        String query = " SELECT " +ATTRIBUTES+
        " FROM students";

        PreparedStatement stmt = connection.prepareStatement(query);

        return getAllFromPreparedStatement(stmt);
    }


    /** Returns true if any students exist in the database referenced by {@code connection}.
     *
     * @param connection The database to check for registered students.
     * @return true if any students are found.
     * @throws SQLException
     */
    public static boolean existAny(Connection connection)
    throws SQLException
    {
        String query = " SELECT student_pk "
        + " FROM students"
        + " LIMIT 1";

        PreparedStatement stmt = connection.prepareStatement(query);

        try {
            ResultSet rs = stmt.executeQuery();

            boolean result =  rs.next();
            rs.close();
            return result;
    } finally {
        stmt.close();
    }
    }


    /**
     * Looks up a student record based on the student's campusUID (these are unique IDs
     * supplied by the university).  Will not find a superuser account (since, typically,
     * those are auxiliary accounts).
     * Returns null if no student record with that campusUID exists.
     *
     * @param campusUID the id
     * @return the student record if it exists, else null.
     */
    public static Student lookupByCampusUID(String campusUID, Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +ATTRIBUTES+
            " FROM " +TABLE_NAME+
            " WHERE campus_UID = ? " +
            " AND superuser = ? ";


        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, campusUID);
        stmt.setBoolean(2, false);
        return getFromPreparedStatement(stmt);
    }

    /**
     * Looks up a student record based on the student's campusUID (these are unique IDs
     * supplied by the university).  Returns null if no student record with that
     * campusUID exists.
     *
     * @param campusUID the id
     * @return the student record if it exists, else null.
     */
    public static Student lookupByLoginNameAndCampusUID(String loginName, String campusUID, Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +ATTRIBUTES+
            " FROM " +TABLE_NAME+
            " WHERE login_name = ? " +
            " AND campus_UID = ? ";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, loginName);
        stmt.setString(2, campusUID);
        return getFromPreparedStatement(stmt);
    }


  public static Student lookupByLoginName(String loginName, Connection connection)
  throws SQLException
  {
    String query = " SELECT " +ATTRIBUTES+
        " FROM " +TABLE_NAME+
    " WHERE login_name = ?";

    PreparedStatement stmt = connection.prepareStatement(query);
    stmt.setString(1, loginName);
    return getFromPreparedStatement(stmt);
  }

  private static Student getFromPreparedStatement(PreparedStatement stmt)
            throws SQLException {
        try {
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Student student = new Student();
                student.fetchValues(rs, 1);
                return student;
            }
            return null;
        } finally {
            stmt.close();

        }
    }
    private static Map<Integer, Student> getAllFromPreparedStatement(PreparedStatement stmt)
            throws SQLException {
        Map<Integer, Student> result = new HashMap<Integer, Student>();
        try {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Student student = new Student();
                student.fetchValues(rs, 1);
                result.put(student.getStudentPK(), student);
            }
            return result;
        } finally {
            stmt.close();
        }
    }


  public static Map<Integer, Student> lookupAllCommentAuthorsPK(
       @Submission.PK int submissionPK, Connection conn) throws SQLException {
    String query = "SELECT DISTINCT "
        + ATTRIBUTES
        + " FROM "
        + TABLE_NAME
        + ", "
        + CodeReviewThread.TABLE_NAME
        + ","
        + CodeReviewComment.TABLE_NAME
        + " WHERE code_review_thread.submission_pk = ? "
        + " AND code_review_thread.code_review_thread_pk = code_review_comment.code_review_thread_pk "
        + " AND students.student_pk = code_review_comment.student_pk ";
    PreparedStatement stmt = null;
    try {
      stmt = conn.prepareStatement(query);
      SqlUtilities.setInteger(stmt, 1, submissionPK);
      return getAllFromPreparedStatement(stmt);
    } finally {
      Queries.closeStatement(stmt);
    }

  }


       /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
  public int compareTo(Student that) {
        Student thatStudent = that;
        int result = this.lastname.compareTo(thatStudent.lastname);
        if (result != 0) return result;
        result = this.firstname.compareTo(thatStudent.firstname);
        if (result != 0) return result;
        return this.studentPK - that.studentPK;
    }

    /**
     * @param emailAddress
     * @return
     */
    public static Student lookupByEmailAddress(String emailAddress,
            Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +ATTRIBUTES+
            " FROM " +TABLE_NAME+
            " WHERE email = ? ";

        PreparedStatement stmt= conn.prepareStatement(query);
        stmt.setString(1, emailAddress);
        return getFromPreparedStatement(stmt);
    }

}
