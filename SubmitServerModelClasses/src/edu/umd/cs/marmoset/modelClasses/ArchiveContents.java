package edu.umd.cs.marmoset.modelClasses;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArchiveContents {

    public static final String TABLE_NAME = "archive_contents";

    static final String[] ATTRIBUTE_NAME_LIST = { "archive_pk", "file_pk" };
    public static final String ATTRIBUTES = Queries.getAttributeList(
            TABLE_NAME, ATTRIBUTE_NAME_LIST);

    public static void add(int archivePK, @FileContents.PK int filePK, Connection conn) throws SQLException {
        String query = Queries.makeInsertStatementUsingSetSyntax(
                ATTRIBUTE_NAME_LIST, TABLE_NAME, false);

        PreparedStatement stmt = conn.prepareStatement(query);

        stmt.setInt(1, archivePK);
        stmt.setInt(2, filePK);
        stmt.executeUpdate();
        Queries.closeStatement(stmt);
    }

}