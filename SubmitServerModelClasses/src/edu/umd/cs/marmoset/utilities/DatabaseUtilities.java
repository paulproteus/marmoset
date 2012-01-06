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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utilities for loading the database driver, reading the database
 * properties file, and connecting to the submit-server database.
 *
 * @author jspacco
 * TODO Write a version of getConnection() that uses dbunit to read an xml file.
 */
public final class DatabaseUtilities
{
    private static final boolean DEBUG=Boolean.getBoolean("DEBUG");
    private static final String SERVER=System.getProperty("database");

    private static final String DRIVER = System.getProperty("database.driver", "org.gjt.mm.mysql.Driver");
    private static volatile Properties dbProps = null;

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_DATABASE = "submitserver";
    private static final String DEFAULT_CNF = System.getenv("HOME") +"/.my.cnf";

    private DatabaseUtilities() {}

    static {
        try {
            // Load the database driver
            Class.forName(DRIVER);
            if (DEBUG)
                System.out.println("Loaded db driver " +DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Can't find DB Driver: " +e.getMessage(),e);
        }
    }

    public static Properties getDbProps()
    throws SQLException
    {
        return getDbProps(System.getProperty("my.cnf.file", DEFAULT_CNF));
    }

    static String dbPropsSource;

    public static Properties getDbProps(String fname)
    throws SQLException
    {
        // Very hack-ish method
        // This will read all the properties out of the .my.cnf file, but doesn't do
        // very intelligently.  For example, it doesn't distinguish between the various
        // different categories like [mysql] vs. [mysqladmin] vs [client], and will
        // use the last user/password that show up in the file.
        // Thus the primary use of this method is to get the password out of the
        // .my.cnf file where it needs to be stored anyway.
        if (dbProps!=null)  return dbProps;

        synchronized(DatabaseUtilities.class) {
        	if (dbProps!=null)  return dbProps;
        	if (dbPropsSource != null && !dbPropsSource.equals(fname))
        		throw new IllegalArgumentException("Can't change dbProps source from " + dbPropsSource + " to " + fname);

            try {
                dbProps = new Properties();
                FileInputStream inStream = new FileInputStream(new File(fname));
				dbProps.load(inStream);
				inStream.close();
				dbPropsSource = fname;
            } catch (IOException e) {
                throw new SQLException(e.getMessage());
            }
            return dbProps;
        }

    }

    static class DBProps {
    	static final Properties dbProps = new Properties();
    	static {

    	}
    }
    /**
     * This is the preferred method of connecting to the database.
     * Will use the $HOME/.my.cnf file (if one is available) unless
     * over-ridden by setting -Dmy.cnf.file=&lt;filename&gt;
     * @param dbServer A string representing the protocol, database driver,
     *      URL, port and name of the database.
     * @return A connection to the database.
     * @throws SQLException
     */
    public static Connection getConnection(String dbServer)
    throws SQLException
    {
        return getConnection(dbServer, getDbProps());
    }

    private static Connection getConnection(String dbServer, Properties props)
    throws SQLException
    {
        if (DEBUG) {
            System.out.println("dbServer: " +dbServer);
            System.out.println("properties: " +props);
        }
        return DriverManager.getConnection(dbServer, props);
    }

    public static Connection getConnection()
    throws SQLException
    {
        if (SERVER!=null) {
            return getConnection(SERVER);
        }
        return getConnection(Integer.parseInt(getDbProps().getProperty("port","3306")));
    }

    /**
     * Connect to the database specified in the 'host' and 'database'
     * database-file properties, and the 'port' argument.  If the 'host'
     * system property is not specified, "localhost" is used.  The
     * 'database' system property must be specified.
     *
     * @param port the port to connect to on the host
     * @return a SQL connection to the host/port/database
     * @throws SQLException
     */
    private static Connection getConnection(int port)
    throws SQLException
    {
        String host = getDbProps().getProperty("host", DEFAULT_HOST);
        String database = getDbProps().getProperty("database", DEFAULT_DATABASE);
        String dbServer="jdbc:mysql://" + host+ ":" + port + "/" + database;
        return getConnection(dbServer);
    }

    public static void releaseConnection(Connection conn)
    {
        try {
            if (conn != null) conn.close();
        } catch (SQLException ignore) {
            // ignore
        }
    }
}
