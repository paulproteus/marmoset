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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A PreparedStatement and a ResultSet representing an query,
 * positioned at some row.  The reason this class exists is
 * to make it possible to return a ResultSet from a method,
 * and still ensure that both statement and result set are
 * closed when the query is done.
 * 
 * @author daveho
 */
public class StatementAndResultSet {
	private PreparedStatement statement;
	private ResultSet resultSet;
	
	/**
	 * Constructor.
	 * @param statement the PreparedStatement to be executed
	 */
	public StatementAndResultSet(PreparedStatement statement) {
		this.statement = statement;
	}
	
	/**
	 * Execute the statement.
	 * If successful, the ResultSet will be available, positioned
	 * just before the first row returned.
	 * 
	 * @throws SQLException
	 */
	public void execute() throws SQLException {
		resultSet = statement.executeQuery();
	}
	
	/**
	 * Close the statement.
	 * As a side-effect, this closes the ResultSet (if any).
	 */
	public void close() {
		try {
			statement.close();
		} catch (SQLException e) {
			// Ignored
		}
	}

	/**
	 * @return Returns the resultSet.
	 */
	public ResultSet getResultSet() {
		return resultSet;
	}
	
	/**
	 * @return Returns the statement.
	 */
	public PreparedStatement getStatement() {
		return statement;
	}
}
