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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

/**
 * CheckUptime
 * @author jspacco
 */
public class CheckUptime
{

    /**
     * @param args
     */
    public static void main(String[] args)
    throws IOException
    {
        InetAddress address = InetAddress.getByName("localhost");
        if (args.length > 0) {
            address = InetAddress.getByName(args[0]);
        }
        int port=9999;
        if (args.length > 1) {
            port=Integer.parseInt(args[1]);
        }
        
        Socket s=new Socket(address,port);
        BufferedReader reader=new BufferedReader(new InputStreamReader(s.getInputStream()));
        while(true) {
            String line=reader.readLine();
            if (line==null) break;
            System.out.println(line);
        }
        reader.close();
        s.close();
    }
}
