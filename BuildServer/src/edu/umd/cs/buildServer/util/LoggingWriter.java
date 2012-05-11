package edu.umd.cs.buildServer.util;

import java.io.PrintWriter;

import org.apache.log4j.Logger;

import edu.umd.cs.diffText.StringsWriter;

public class LoggingWriter extends StringsWriter {

    
    Logger logger;
    
    PrintWriter writer;
    
    
    public LoggingWriter(PrintWriter writer, Logger logger) {
        super();
        this.writer = writer;
        this.logger = logger;
    }


    @Override
    protected void got(String s) {
        logger.info("Wrote " + s);
        writer.println(s);
        
    }
    
    @Override
    public void close() {
        
        super.close();
        logger.info("done writing ");
        writer.close();
    }
 
        

}
