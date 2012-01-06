package edu.umd.cs.marmoset.modelClasses;


import java.sql.PreparedStatement;
import java.util.Random;

import edu.umd.cs.marmoset.utilities.DatabaseUtilities;

public class TestTextSize {
    
    public static void main(String args[]) throws Exception  {
        java.sql.Connection conn = DatabaseUtilities.getConnection();
        
        Random r = new Random();
        byte [] raw = new byte[8];
        r.nextBytes(raw);
        String txt = new String(raw);
        System.out.println(txt.length());
        int max = 61999;
        
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO test (size,result) VALUES (?,?)");
        while (true)  {
            if (txt.length() > max)
                txt = txt.substring(0, max);
            System.out.println("Trying with with size " + txt.length() + " " + txt.getBytes().length);
            
            stmt.setInt(1, txt.length());
            stmt.setString(2, txt);
            stmt.executeUpdate();
            if (txt.length() >= max-1) 
                break;
            txt = txt + txt;
        }
        
    }

}
