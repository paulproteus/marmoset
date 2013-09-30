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
package edu.umd.cs.marmoset.utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

/**
 * TextFileReader: Simple class for reading lines out of a text file using an iterator.
 * 
 * XXX Should this extend java.util.Reader or not?
 * @author jspacco
 */
public class TextFileReader implements Iterable<String> 
{
    private BufferedReader reader;
    
    public void close() throws IOException
    {
        reader.close();
    }
    
    public TextFileReader(String filename) throws IOException
    {
        reader=new BufferedReader(new FileReader(filename));
    }
    
    public TextFileReader(InputStream is) throws IOException
    {
        reader=new BufferedReader(new InputStreamReader(is));
    }
    
    public TextFileReader(Reader reader) throws IOException
    {
        this.reader=new BufferedReader(reader);
    }
    
    @Override
	public Iterator<String> iterator()
    {
        return new Iterator<String>() {
            private String line=null;

            /* (non-Javadoc)
             * @see java.util.Iterator#hasNext()
             */
            @Override
			public boolean hasNext()
            {
                try {
                    line=reader.readLine();
                    
                    return line!=null;
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#next()
             */
            @Override
			public @Nonnull String next()
            {
                if (line != null)
                    return line;
                throw new NoSuchElementException();
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            @Override
			public void remove()
            {
                throw new IllegalStateException("Cannot remove elements from a TextFileReader through its iterator()");
            }
            
        };
    }
}
