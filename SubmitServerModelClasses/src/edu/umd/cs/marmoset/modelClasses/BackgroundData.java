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
 * @author jspacco
 */
package edu.umd.cs.marmoset.modelClasses;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.umd.cs.marmoset.utilities.SqlUtilities;

/**
 * @author jspacco
 *
 */
public class BackgroundData implements Serializable
{
    public static final String TABLE_NAME = "background_data";
	/**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;
    public static final String PREFER_NOT_TO_ANSWER = "na";
    public static final String NONE = "none";

    static final String[] ATTRIBUTE_NAME_LIST = {
			"student_pk", "gender", "ethnic_racial_association",
			"AmericanIndian", "Asian", "Black", "Caucasian", "LatinoLatina",
			"age", "high_school_country", "prior_programming_experience",
			"other_institution", "a_score", "ab_score",
			"umcp_placement_exam", "umcp_placement_exam_result","major"
    };

    static final String ATTRIBUTES =
		Queries.getAttributeList(TABLE_NAME, ATTRIBUTE_NAME_LIST);

    public static final String FEMALE = "female";
    public static final String MALE = "male";

    public static final String AMERICAN_INDIAN = "AmericanIndian";
    public static final String ASIAN = "Asian";
    public static final String BLACK = "Black";
    public static final String CAUCASIAN = "Caucasian";
    public static final String LATINO_LATINA = "LatinoLatina";

    public static final String COMMUNITY_COLLEGE = "Community College";
    public static final String OTHER_UM_INSTITUTION = "Other UM System Institution";
    public static final String OTHER_NON_UM_INSTITUTION = "Other non-UM System Institution";
    public static final String HIGH_SCHOOL_AP_COURSE = "High School AP Course";
    public static final String OTHER_HIGH_SCHOOL_COURSE = "Other High School Course";

    private @Student.PK int studentPK; // non-NULL
    private String gender;
    private String age;
    private String highSchoolCountry;
    private String placementExam;
    private String placementExamResult;
    private String major;

    private String priorProgrammingExperience;
    private String otherInstitution;
    private String aExamScore;
    private String abExamScore;

    private String EthnicRacialAssociation;
    private String AmericanIndian;
    private String Asian;
    private String Black;
    private String Caucasian;
    private String LatinoLatina;

    private boolean complete=false;

    public void fetchValues(ResultSet rs, int startingFrom)
    throws SQLException
    {
        setStudentPK(Student.asPK(rs.getInt(startingFrom+0)));
        setGender(rs.getString(startingFrom+1));
        setEthnicRacialAssociation(rs.getString(startingFrom+2));
        setAmericanIndian(rs.getString(startingFrom+3));
        setAsian(rs.getString(startingFrom+4));
        setBlack(rs.getString(startingFrom+5));
        setCaucasian(rs.getString(startingFrom+6));
        setLatinoLatina(rs.getString(startingFrom+7));
        setAge(rs.getString(startingFrom+8));
        setHighSchoolCountry(rs.getString(startingFrom+9));
        setPriorProgrammingExperience(rs.getString(startingFrom+10));
        setOtherInstitution(rs.getString(startingFrom+11));
        setAExamScore(rs.getString(startingFrom+12));
        setAbExamScore(rs.getString(startingFrom+13));
        setPlacementExam(rs.getString(startingFrom+14));
        setPlacementExamResult(rs.getString(startingFrom+15));
        setMajor(rs.getString(startingFrom+16));
    }

    /**
     * Inserts this BackgroundData object into the database as a new row
     * in the 'background_data' table.
     * <p>
     * We use a PreparedStatement so that
     *
     * @param conn the connection to the database
     * @throws SQLException
     */
    public void insertOrUpdate(Connection conn) throws SQLException
    {
        String update =
        	  Queries.makeInsertStatementUsingSetSyntax(ATTRIBUTE_NAME_LIST, TABLE_NAME, true)
        	  	+
        		" ON DUPLICATE KEY " +
        		" UPDATE ";
        for (int ii=0; ii < ATTRIBUTE_NAME_LIST.length-1; ii++)
        {
            update += ATTRIBUTE_NAME_LIST[ii] + " = ?, ";
        }
        update += ATTRIBUTE_NAME_LIST[ATTRIBUTE_NAME_LIST.length-1] + " = ? ";

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(update);

            stmt.setInt(1, studentPK);
            stmt.setString(2, gender);
            stmt.setString(3, EthnicRacialAssociation);
            stmt.setString(4, AmericanIndian);
            stmt.setString(5, Asian);
            stmt.setString(6, Black);
            stmt.setString(7, Caucasian);
            stmt.setString(8, LatinoLatina);
            stmt.setString(9, age);
            stmt.setString(10, highSchoolCountry);
            stmt.setString(11, priorProgrammingExperience);
            stmt.setString(12, otherInstitution);
            stmt.setString(13, aExamScore);
            stmt.setString(14, abExamScore);
            stmt.setString(15, placementExam);
            stmt.setString(16, placementExamResult);
            stmt.setString(17, major);


            stmt.setInt(18, studentPK);
            stmt.setString(19, gender);
            stmt.setString(20, EthnicRacialAssociation);
            stmt.setString(21, AmericanIndian);
            stmt.setString(22, Asian);
            stmt.setString(23, Black);
            stmt.setString(24, Caucasian);
            stmt.setString(25, LatinoLatina);
            stmt.setString(26, age);
            stmt.setString(27, highSchoolCountry);
            stmt.setString(28, priorProgrammingExperience);
            stmt.setString(29, otherInstitution);
            stmt.setString(30, aExamScore);
            stmt.setString(31, abExamScore);
            stmt.setString(32, placementExam);
            stmt.setString(33, placementExamResult);
            stmt.setString(34, major);

            stmt.executeUpdate();
        } finally {
            if (stmt != null) stmt.close();
        }
    }

    /**
     * Looks up the background data from the database for the student
     * with studentPK.
     *
     * Note that this could possibly leak database resources if creating
     * the PreparedStatement succeeds but any of the set...() methods fail.
     *
     * @param studentPK
     * @param conn
     * @return
     * @throws SQLException
     */
    public static BackgroundData lookupByStudentPK(@Student.PK Integer studentPK, Connection conn)
    throws SQLException
    {
        String query =
            " SELECT " +ATTRIBUTES+
            " FROM background_data " +
            " WHERE student_pk = ? ";

        PreparedStatement stmt = null;

		stmt = conn.prepareStatement(query);
        SqlUtilities.setInteger(stmt, 1, studentPK);

		return getFromPreparedStatement(stmt);
    }

    /**
     * Private helper method that executes a given PreparedStatement
     * and returns the resulting BackgroundData object.
     *
     * @param stmt the PreparedStatement to execute.
     * @return
     * @throws SQLException
     */
    private static BackgroundData getFromPreparedStatement(PreparedStatement stmt)
    throws SQLException
    {
        try {
	        ResultSet rs = stmt.executeQuery();

	        if (rs.first())
	        {
	            BackgroundData backgroundData = new BackgroundData();
	            backgroundData.fetchValues(rs, 1);
	            return backgroundData;
	        }
	        return null;
	    }
	    finally {
	        try {
	            if (stmt != null) stmt.close();
	        } catch (SQLException ignore) {
	            // ignore
	        }
	    }
    }

    private static final String NO_ANSWER = "'Prefer not to answer' if you do not wish to answer";

    private static boolean isEmptyOrNull(String string)
    {
        return string == null || string.equals("");
    }

    public void verifyFormat()
    throws IncorrectBackgroundDataException
    {
        if (isEmptyOrNull(gender))
        {
            throw new IncorrectBackgroundDataException(this, "Please select your gender, or " +NO_ANSWER);
        }

        // ethnicity must be either 'yes' or 'na'
        if (EthnicRacialAssociation != null &&
                !EthnicRacialAssociation.equals("na") &&
                !EthnicRacialAssociation.equals("yes"))
        {
            throw new IncorrectBackgroundDataException(this, "EthnicRacialAssociation must be either 'yes' or 'na'");
        }

        // if ethnicity is 'yes', then one or more categories must be selected
        if (EthnicRacialAssociation == null)
        {
            if (AmericanIndian == null &&
                    Asian == null &&
                    Black == null &&
                    Caucasian == null &&
                    LatinoLatina == null)
                throw new IncorrectBackgroundDataException(this, "Please select an Ethnic/Racial assocation, or " +NO_ANSWER);
        }

        // prompt for the student's age
        if (isEmptyOrNull(age))
        {
            throw new IncorrectBackgroundDataException(this, "Please select an age range, or " +NO_ANSWER);
        }

        // prompt for country where the student attended high school
        if (isEmptyOrNull(highSchoolCountry))
        {
            throw new IncorrectBackgroundDataException(this, "Please select the country where you attended high school or " +NO_ANSWER);
        }

        // prompt for the student's prior programming experience
        if (isEmptyOrNull(priorProgrammingExperience))
        {
            throw new IncorrectBackgroundDataException(this, "Please select your programming experience prior to UMCP or " +NO_ANSWER);
        }

        // prompt for the name of the community college if the student attended one
        if (priorProgrammingExperience.equals(COMMUNITY_COLLEGE) ||
                priorProgrammingExperience.equals(OTHER_UM_INSTITUTION) ||
                priorProgrammingExperience.equals(OTHER_NON_UM_INSTITUTION))
        {
            if (isEmptyOrNull(otherInstitution))
            {
                throw new IncorrectBackgroundDataException(this, "Please enter the name of the institution you attended prior to UMCP");
            }
        }

        // prompt for AP scores if the student took a high school AP course
        if (priorProgrammingExperience.equals(HIGH_SCHOOL_AP_COURSE))
        {
            if (aExamScore == null && abExamScore == null)
            {
                throw new IncorrectBackgroundDataException(this, "Please enter your scores for the AP exam(s) you took in high school");
            }
        }

        // prompt for the placement exam
        if (isEmptyOrNull(placementExam))
        {
            throw new IncorrectBackgroundDataException(this, "Please select whether you took a placement exam and the result, or " +NO_ANSWER);
        }
        // prompt for placement exam result if the student took a placement exam
        if (!placementExam.equals("none") &&
                !placementExam.equals(PREFER_NOT_TO_ANSWER))
        {
            if (isEmptyOrNull(placementExamResult))
            {
                throw new IncorrectBackgroundDataException(this, "Please enter the result of your placement exam");
            }
        }

        if (isEmptyOrNull(major))
        {
            throw new IncorrectBackgroundDataException(this, "Please select your major, or " +NO_ANSWER);
        }
        complete=true;
    }

    @Override
	public String toString()
    {
        StringBuffer buf = new StringBuffer();

        buf.append("studentPK: " +getStudentPK() + "<br>\n");
        buf.append("gender: " +getGender()+ "<br>\n");
        buf.append("ethnicRacialAssocitaion: " +getEthnicRacialAssociation()+"<br>\n");
        buf.append("AmericanIndian: " +getAmericanIndian() +"<br>\n");
        buf.append("Asian: " +getAsian() +"<br>\n");
        buf.append("Black: " +getBlack() +"<br>\n");
        buf.append("Caucasian: " +getCaucasian() +"<br>\n");
        buf.append("Latino/Latina: " +getLatinoLatina() +"<br>\n");
        buf.append("age: " +getAge() +"<br>\n");
        buf.append("high school country: " +getHighSchoolCountry() +"<br>\n");
        buf.append("prior programming experience: " +getPriorProgrammingExperience() +"<br>\n");
        buf.append("other institution: " +getOtherInstitution() +"<br>\n");
        buf.append("AP A Exam Score: " +getAExamScore() +"<br>\n");
        buf.append("AP AB Exam Score: " +getAbExamScore() +"<br>\n");
        buf.append("Placement exam: " +getPlacementExam() +"<br>\n");
        buf.append("Placement exam result: " +getPlacementExamResult() +"<br>\n");
        buf.append("Major: "+getMajor());

        return buf.toString();

    }

    /**
     * Empty constructor.
     *
     */
    public BackgroundData() {}

    /**
     * @return Returns the abExamScore.
     */
    public String getAbExamScore() {
        return abExamScore;
    }
    /**
     * @param abExamScore The abExamScore to set.
     */
    public void setAbExamScore(String abExamScore) {
        this.abExamScore = abExamScore;
    }
    /**
     * @return Returns the aExamScore.
     */
    public String getAExamScore() {
        return aExamScore;
    }
    /**
     * @param examScore The aExamScore to set.
     */
    public void setAExamScore(String examScore) {
        aExamScore = examScore;
    }
    /**
     * @return Returns the age.
     */
    public String getAge() {
        return age;
    }
    /**
     * @param age The age to set.
     */
    public void setAge(String age) {
        this.age = age;
    }
    /**
     * @return Returns the gender.
     */
    public String getGender() {
        return gender;
    }
    /**
     * @param gender The gender to set.
     */
    public void setGender(String gender) {
        this.gender = gender;
    }
    /**
     * @return Returns the highSchoolCountry.
     */
    public String getHighSchoolCountry() {
        return highSchoolCountry;
    }
    /**
     * @param highSchoolCountry The highSchoolCountry to set.
     */
    public void setHighSchoolCountry(String highSchoolCountry) {
        this.highSchoolCountry = highSchoolCountry;
    }
    /**
     * @return Returns the otherInstitution.
     */
    public String getOtherInstitution() {
        return otherInstitution;
    }
    /**
     * @param otherInstitution The otherInstitution to set.
     */
    public void setOtherInstitution(String otherInstitution) {
        this.otherInstitution = otherInstitution;
    }
    /**
     * @return Returns the placementExam.
     */
    public String getPlacementExam() {
        return placementExam;
    }
    /**
     * @param placementExam The placementExam to set.
     */
    public void setPlacementExam(String placementExam) {
        this.placementExam = placementExam;
    }
    /**
     * @return Returns the placementExamResult.
     */
    public String getPlacementExamResult() {
        return placementExamResult;
    }
    /**
     * @param placementExamResult The placementExamResult to set.
     */
    public void setPlacementExamResult(String placementExamResult) {
        this.placementExamResult = placementExamResult;
    }
    /**
     * @return Returns the major.
     */
    public String getMajor() {
        return major;
    }
    /**
     * @param major The major to set.
     */
    public void setMajor(String major) {
        this.major = major;
    }
    /**
     * @return Returns the priorProgrammingExperience.
     */
    public String getPriorProgrammingExperience() {
        return priorProgrammingExperience;
    }
    /**
     * @param priorProgrammingExperience The priorProgrammingExperience to set.
     */
    public void setPriorProgrammingExperience(String priorProgrammingExperience) {
        this.priorProgrammingExperience = priorProgrammingExperience;
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
    public void setStudentPK(@Student.PK int studentPK) {
        this.studentPK = studentPK;
    }
    /**
     * @return Returns the americanIndian.
     */
    public String getAmericanIndian() {
        return AmericanIndian;
    }
    /**
     * @param americanIndian The americanIndian to set.
     */
    public void setAmericanIndian(String americanIndian) {
        AmericanIndian = americanIndian;
    }
    /**
     * @return Returns the asian.
     */
    public String getAsian() {
        return Asian;
    }
    /**
     * @param asian The asian to set.
     */
    public void setAsian(String asian) {
        Asian = asian;
    }
    /**
     * @return Returns the black.
     */
    public String getBlack() {
        return Black;
    }
    /**
     * @param black The black to set.
     */
    public void setBlack(String black) {
        Black = black;
    }
    /**
     * @return Returns the caucasian.
     */
    public String getCaucasian() {
        return Caucasian;
    }
    /**
     * @param caucasian The caucasian to set.
     */
    public void setCaucasian(String caucasian) {
        Caucasian = caucasian;
    }
    /**
     * @return Returns the ethnicRacialAssociation.
     */
    public String getEthnicRacialAssociation() {
        return EthnicRacialAssociation;
    }
    /**
     * @param ethnicRacialAssociation The ethnicRacialAssociation to set.
     */
    public void setEthnicRacialAssociation(String ethnicRacialAssociation) {
        EthnicRacialAssociation = ethnicRacialAssociation;
    }
    /**
     * @return Returns the latinoLatina.
     */
    public String getLatinoLatina() {
        return LatinoLatina;
    }
    /**
     * @param latinoLatina The latinoLatina to set.
     */
    public void setLatinoLatina(String latinoLatina) {
        LatinoLatina = latinoLatina;
    }
    /**
     * @return Returns the valid.
     */
    public boolean isComplete()
    {
        // TODO this could be done better...  verifyFormat() throws an exception
        // if there are any problems, and resets complete.
        complete=false;
        try {
            verifyFormat();
            complete=true;
        } catch (IncorrectBackgroundDataException e) {

        }
        return complete;
    }
}
