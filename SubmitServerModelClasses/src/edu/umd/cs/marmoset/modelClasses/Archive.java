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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

import edu.umd.cs.marmoset.utilities.Checksums;
import edu.umd.cs.marmoset.utilities.MarmosetPatterns;
import edu.umd.cs.marmoset.utilities.SqlUtilities;

/**
 * @author jspacco Contains a jar archive contained in a row of a database.
 */
public class Archive {


	/** List of columns in this table */
	final static String[] ATTRIBUTE_NAME_LIST = { "archive_pk", "archive",
			"checksum" };
	final String tableName;
	final int archivePK;
	byte[] archive;

	private Archive(String tableName, int archivePK, byte[] archive) {
		super();
		this.tableName = tableName;
		this.archivePK = archivePK;
		this.archive = archive;
	}

	public byte[] getArchive() {
		return archive;
	}

	public void setArchive(byte[] archive) {
		this.archive = archive;
	}

	public String getTableName() {
		return tableName;
	}

	public int getArchivePK() {
		return archivePK;
	}

	public static Archive getArchive(String tableName, int archivePK,
			Connection conn) throws SQLException {
		if (!MarmosetPatterns.isTableName(tableName)) {
			throw new IllegalArgumentException("tableName is malformed");
		}
		String attributes = Queries.getAttributeList(tableName,
				ATTRIBUTE_NAME_LIST);
		String query = " SELECT " + attributes + " FROM " + tableName
				+ " WHERE archive_pk = ? ";
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(query);
			SqlUtilities.setInteger(stmt, 1, archivePK);

			ResultSet rs = stmt.executeQuery();

			if (rs.next())
				return new Archive(tableName, archivePK, rs.getBytes(2));
		} finally {
			Queries.closeStatement(stmt);
		}
		return null;

	}

	public static Iterable<Archive> getAll(final String tableName,
			final Connection conn) {
		return new Iterable<Archive>() {

			@Override
			public Iterator<Archive> iterator() {
				if (!MarmosetPatterns.isTableName(tableName)) {
					throw new IllegalArgumentException("tableName is malformed");
				}

				String attributes = Queries.getAttributeList(tableName,
						ATTRIBUTE_NAME_LIST);
				String query = " SELECT " + attributes + " FROM " + tableName;
				PreparedStatement maybeStmt = null;
				try {
					maybeStmt = conn.prepareStatement(query);
					final PreparedStatement stmt = maybeStmt;
					final ResultSet rs = stmt.executeQuery();

					return new Iterator<Archive>() {

						boolean nextReady = false;
						boolean done = false;

						@Override
						public boolean hasNext() {
							try {
								if (done)
									return false;
								if (nextReady)
									return true;
								if (rs.next()) {
									nextReady = true;
									return true;
								} else {
									Queries.closeStatement(stmt);
									done = true;
									return false;
								}
							} catch (SQLException e) {
								Queries.closeStatement(stmt);
								throw new RuntimeException(e);
							}
						}

						@Override
						public Archive next() {
							try {

								if (!hasNext())
									throw new NoSuchElementException();
								nextReady=false;
								return new Archive(tableName, rs.getInt(1),
										rs.getBytes(2));
							} catch (SQLException e) {
								Queries.closeStatement(stmt);
								throw new RuntimeException(e);
							}
						}

						@Override
						public void remove() {
							throw new UnsupportedOperationException();

						}
					};
				} catch (SQLException e) {
					Queries.closeStatement(maybeStmt);
					throw new RuntimeException(e);
				}
			};

		};
	}

	public static byte[] downloadBytesFromArchive(String tableName,
			Integer archivePK, Connection conn) throws SQLException {
		if (conn == null)
			throw new NullPointerException("null connection");

		if (!MarmosetPatterns.isTableName(tableName)) {
			throw new IllegalArgumentException("tableName is malformed");
		}

		String attributes = Queries.getAttributeList(tableName,
				ATTRIBUTE_NAME_LIST);
		String query = " SELECT " + attributes + " FROM " + tableName
				+ " WHERE archive_pk = ? ";
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(query);
			SqlUtilities.setInteger(stmt, 1, archivePK);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getBytes(2);
			}
			throw new SQLException("cannot find archive in table " + tableName
					+ " with PK " + archivePK);
		} finally {
			Queries.closeStatement(stmt);
		}
	}

	/**
	 * Upload an archive to the database, returning the primary key for the
	 * created entry. If a matching archive already exists, return the primary
	 * key for that value, do not create a new one.
	 * 
	 * @param tableName
	 * @param bytes
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	static int uploadBytesToArchive(String tableName, byte[] bytes,
			Connection conn) throws SQLException {
		if (!MarmosetPatterns.isTableName(tableName)) {
			throw new IllegalArgumentException("tableName is malformed");
		}

		String digest = Checksums.getChecksum(bytes);
		String query = " SELECT archive_pk" + " FROM " + tableName
				+ " WHERE checksum = ? ";

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, digest);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getInt(1);
			}
		} finally {
			Queries.closeStatement(stmt);
		}

		String insert = " INSERT INTO " + tableName
				+ " (archive_pk, archive, checksum) " + " VALUES "
				+ " (DEFAULT, ?, ?) ";

		try {
			stmt = conn.prepareStatement(insert,
					Statement.RETURN_GENERATED_KEYS);
			stmt.setBytes(1, bytes);
			stmt.setString(2, digest);

			stmt.executeUpdate();

			return Queries.getGeneratedPrimaryKey(stmt);
		} finally {
			Queries.closeStatement(stmt);
		}
	}

	static void deleteArchiveEntry(String tableName, int pk, Connection conn)
			throws SQLException {
		if (!MarmosetPatterns.isTableName(tableName)) {
			throw new IllegalArgumentException("tableName is malformed");
		}

		String insert = " DELETE FROM " + tableName + " WHERE archive_pk = ? ";

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(insert);
			stmt.setInt(1, pk);

			stmt.executeUpdate();

		} finally {
			Queries.closeStatement(stmt);
		}
	}

	/**
	 * Update the bytes in the archive. This method is only necessary for
	 * example when students upload a zip archive that Java is unable to unzip
	 * (the zip standard, by the way, isn't really much of a standard and
	 * students find ways to upload zip archives that don't work correctly under
	 * Java but work with command-line zip utilities, or work with ZipFile but
	 * not ZipInputStream). In general, you should <b>not</b> be replacing
	 * archives, the whole point to the system is that we're <b>keeping</b>
	 * everything.
	 * 
	 * @param tableName
	 * @param archivePK
	 * @param cachedArchive
	 * @param conn
	 * @throws SQLException
	 */

	static void updateBytesInArchive(String tableName, Integer archivePK,
			byte[] cachedArchive, Connection conn) throws SQLException {
		if (!MarmosetPatterns.isTableName(tableName)) {
			throw new IllegalArgumentException("tableName is malformed");
		}
		String sql = " UPDATE " + tableName + " SET archive = ? "
				+ " WHERE archive_pk = ? ";
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(sql);
			stmt.setBytes(1, cachedArchive);
			SqlUtilities.setInteger(stmt, 2, archivePK);
			stmt.executeUpdate();
		} finally {
			Queries.closeStatement(stmt);
		}

	}
}
