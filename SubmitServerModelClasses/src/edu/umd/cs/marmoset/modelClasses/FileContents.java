package edu.umd.cs.marmoset.modelClasses;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.meta.TypeQualifier;

import edu.umd.cs.marmoset.utilities.Charsets;
import edu.umd.cs.marmoset.utilities.Checksums;

public class FileContents implements Comparable<FileContents> {

    public static final String TABLE_NAME = "file_contents";

    static final String[] ATTRIBUTE_NAME_LIST = { "file_pk", "name", "text", 
      "size",
            "checksum", "contents" };
    public static final String ATTRIBUTES = Queries.getAttributeList(
            TABLE_NAME, ATTRIBUTE_NAME_LIST);

    @TypeQualifier(applicableTo = Integer.class)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PK {
    }

    public static @PK
    int asPK(int pk) {
        return pk;
    }

    public static @PK
    Integer asPK(Integer pk) {
        return pk;
    }

    private @PK
    int file_pk;
    public final String name;
    public final int size;
    public final boolean text;
    public final String checksum;
    public final byte[] bytes;

       public FileContents(@PK int file_pk, String name, boolean text,
            int size, String checksum, byte[] bytes) {
        this.file_pk = file_pk;
        this.name = name;
        this.text = text;
        this.size = size;
        this.checksum = checksum;
        this.bytes = bytes;
    }

    public FileContents(String name,boolean text,  int size, String checksum, byte[] bytes) {
        this(0, name, text, size, checksum, bytes);
    }

    public FileContents(String name, boolean text, byte[] bytes) {
        this(name, text, bytes.length, Checksums.getChecksum(
                name.getBytes(Charsets.UTF8), text, bytes), bytes);
    }

    public @PK int getFilePk() {
        return file_pk;
    }

    @Override
    public int hashCode() {
        return checksum.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FileContents))
            return false;
        FileContents that = (FileContents) o;
        return this.checksum.equals(that.checksum);
    }

    @Override
    public int compareTo(FileContents that) {
        int result = this.name.compareTo(that.name);
        if (result != 0)
            return result;
        result = this.size - that.size;
        if (result != 0)
            return result;
        result = this.checksum.compareTo(that.checksum);

        return result;

    }

    private static FileContents fetchValues(ResultSet resultSet,
            int startingFrom) throws SQLException {
        int file_pk = asPK(resultSet.getInt(startingFrom++));
        String name = resultSet.getString(startingFrom++);
        boolean text = resultSet.getBoolean(startingFrom++);
        int size = resultSet.getInt(startingFrom++);
        String checksum = resultSet.getString(startingFrom++);
        byte[] bytes = resultSet.getBytes(startingFrom++);
        return new FileContents(file_pk, name, text, size, checksum, bytes);
    }
    
    private int putValues(PreparedStatement stmt, int index)
        throws SQLException {
    stmt.setString(index++, name);
    stmt.setBoolean(index++, text);
    stmt.setInt(index++, size);
    stmt.setString(index++, checksum);
    stmt.setBytes(index++, bytes);
    return index;
}


    public static FileContents get(@PK int file_pk, Connection connection)
            throws SQLException {
        String query = " SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
                + " WHERE file_pk = ?";

        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setInt(1, file_pk);
        return getFromPreparedStatement(stmt);
    }

    private static FileContents getFromPreparedStatement(PreparedStatement stmt)
            throws SQLException {
        try {
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return fetchValues(rs, 1);
            }
            return null;
        } finally {
            stmt.close();

        }
    }

    public static FileContents lookupOrInsert(Connection conn, String name,
           boolean text, byte[] bytes) throws SQLException {
        FileContents result = new FileContents(name, text, bytes);

        String query = " SELECT " + ATTRIBUTES + " FROM " + TABLE_NAME
                + " WHERE checksum = ?";

        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, result.checksum);
        FileContents existing = getFromPreparedStatement(stmt);
        if (existing != null)
            return existing;

        try {
            // may throw exception due to duplicate key causes by simultaneous
            // insertion
            result.insert(conn);
            return result;
        } catch (SQLException e) {
            existing = getFromPreparedStatement(stmt);
            if (existing != null)
                return existing;
            throw e;

        }
    }

    private void insert(Connection conn) throws SQLException {
        String query = Queries.makeInsertStatementUsingSetSyntax(
                ATTRIBUTE_NAME_LIST, TABLE_NAME, true);

        PreparedStatement stmt = conn.prepareStatement(query,
                Statement.RETURN_GENERATED_KEYS);

        putValues(stmt, 1);
        stmt.executeUpdate();
        this.file_pk = asPK(Queries.getGeneratedPrimaryKey(stmt));
        Queries.closeStatement(stmt);
    }

    public static void main(String args[]) throws Exception {

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:8889/submitserver", "submit",
                "jEnN6Q7xJCe7sYe7");
        byte[] bytes = { 1, 2, 3 };
        FileContents fc1 = FileContents.lookupOrInsert(conn, "test", false, bytes);
        FileContents fc2 = FileContents.lookupOrInsert(conn, "test", false, bytes);
        System.out.println(fc1.file_pk);
        System.out.println(fc2.file_pk);
    }
}