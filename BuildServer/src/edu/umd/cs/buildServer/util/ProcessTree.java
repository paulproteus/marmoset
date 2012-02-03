package edu.umd.cs.buildServer.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class ProcessTree {
    
    Multimap<Integer, Integer> children =  ArrayListMultimap.create();      
    
    public ProcessTree() throws Exception {
        String user = System.getProperty("user.name");

        ProcessBuilder b = new ProcessBuilder(new String[] {"/bin/ps", "-u", user,
                        "-o", "pid= ppid="});
        Process p = b.start();
        
        p.getOutputStream().close();
        Scanner s = new Scanner(p.getInputStream());
        while (s.hasNext()) {
            int pid = s.nextInt();
            int  ppid = s.nextInt();
            children.put(ppid, pid);
        }
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
