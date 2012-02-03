package edu.umd.cs.buildServer.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class ProcessTree {
    
    Multimap<Integer, Integer> children =  ArrayListMultimap.create();      
    
    public ProcessTree(Logger log) throws Exception {
        String user = System.getProperty("user.name");

        ProcessBuilder b = new ProcessBuilder(new String[] {"/bin/ps", "-u", user,
                        "-o", "pid,ppid"});
        Process p = b.start();
        
        p.getOutputStream().close();
        Scanner s = new Scanner(p.getInputStream());
        String header = s.nextLine();
        log.trace("ps header: " + header);
        while (s.hasNext()) {
            String txt = s.nextLine();
            log.debug(txt);
            try {
                String fields [] = txt.trim().split(" +");
                if (fields.length != 2)
                    throw new IllegalStateException("Got " + Arrays.toString(fields));
                int pid = Integer.parseInt(fields[0]);
                int  ppid = Integer.parseInt(fields[1]);
                children.put(ppid, pid);
            } catch (Exception e) {
                log.error("Error while building process treee, parsing " + txt, e);
            }
           
        }
        s.close();
         s = new Scanner(p.getErrorStream());
         while (s.hasNext()) {
             log.error(s.nextLine());
         }
         s.close();
        p.destroy();
    }
    public static void killProcess(int pid, int signal) throws IOException, InterruptedException {
        ProcessBuilder b = new ProcessBuilder(new String[] {"/bin/kill", "-"+signal, Integer.toString(pid)} );
        Process p = b.start();
        int exitCode = p.waitFor();
        if (exitCode != 0)
            throw new IOException("exit code " + exitCode);
    }
    private void findTree(Set<Integer> found, int pid) {
        if (!found.add(pid))
            return;
        for(int c : children.get(pid))
            findTree(found, c);
    }
    public Set<Integer> findTree(int rootPid) {
        HashSet<Integer> result = new HashSet<Integer>();
        findTree(result, rootPid);
        return result;
    }
    public  void killProcessTree(int rootPid, int signal) throws IOException, InterruptedException {
        Set<Integer> result = findTree(rootPid);
        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("/bin/kill");
        cmd.add("-"+signal);
        for(Integer i : result)
            cmd.add(i.toString());
        ProcessBuilder b = new ProcessBuilder(cmd );
        Process p = b.start();
        int exitCode = p.waitFor();
        if (exitCode != 0)
            throw new IOException("exit code " + exitCode);
      
         
    }

}
