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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Utility class for SQL-specific helpers.
 * @author langmead
 */
public final class SqlUtilities {

    /** 
     * Set slot 'idx' in PreparedStatement 's' to be equal to the
     * integer wrapped by I, or to NULL if I is null.
     * 
     * @param s    statement to modify
     * @param idx  index in statement to set
     * @param I    integer to set it to; may be null
     * @throws SQLException
     */
    public static void setInteger(PreparedStatement s, int idx, Integer I)
    throws SQLException
    {
        if(I == null) {
            s.setNull(idx, Types.INTEGER);
        } else {
            s.setInt(idx, I);
        }
    }
    
    /** 
     * Get the Integer result in slot 'idx' of ResultSet 's', being
     * careful to return null when it's NULL.
     * 
     * @param s    statement to modify
     * @param idx  index in statement to set
     * @return     integer in slot idx of s
     * @throws SQLException
     */
    public static Integer getInteger(ResultSet s, int idx) throws SQLException {
        if(s.getString(idx) == null) {
            return null;
        } else {
            return s.getInt(idx);
        }
    }
}
