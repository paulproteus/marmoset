package edu.umd.cs.buildServer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class ProcessTree {
    
    final Multimap<Integer, Integer> children =  ArrayListMultimap.create();  
    final Logger log;
    final String user;
    
    public ProcessTree(Logger log)  {
        this.log = log;
        user = System.getProperty("user.name");
    }
    void computeChildren()  {
        try {
        ProcessBuilder b = new ProcessBuilder(new String[] {"/bin/ps", "x", 
                        "-o", "pid,ppid,user,state,pcpu,args"});
        Process p = b.start();
        
        p.getOutputStream().close();
        Scanner s = new Scanner(p.getInputStream());
        String header = s.nextLine();
        log.trace("ps header: " + header);
        while (s.hasNext()) {
            String txt = s.nextLine();
            if (!txt.contains(user)) continue;
            log.debug(txt);
            try {
                String fields [] = txt.trim().split(" +");
                if (fields.length < 2)
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public  void killProcess(int pid, int signal) throws IOException, InterruptedException {
        ProcessBuilder b = new ProcessBuilder(new String[] {"/bin/kill", "-"+signal, Integer.toString(pid)} );
        execute(b);

    }
    public  void killProcess(int pid) throws IOException, InterruptedException {
        ProcessBuilder b = new ProcessBuilder(new String[] {"/bin/kill",  Integer.toString(pid)} );
        execute(b);
    }
    private void findTree(Set<Integer> found, int pid) {
        if (!found.add(pid))
            return;
        for(int c : children.get(pid))
            findTree(found, c);
    }
    public Set<Integer> findTree(int rootPid) {
        Set<Integer> result = new LinkedHashSet<Integer>();
        findTree(result, rootPid);
        return result;
    }

    public void killProcessTree(int rootPid, int signal) throws IOException, InterruptedException {
        Set<Integer> result = findTree(rootPid);
        log.info("Halting process tree starting at " + rootPid + " which is " + result);
        while (true) {
            killProcesses("-STOP", result);
            Thread.sleep(100);
            computeChildren();
            Set<Integer> r = findTree(rootPid);
            if (r.equals(result))
                break;
            result = r;
            log.info("process tree starting at " + rootPid + " changed to " + result);
        }
        log.info("Killing process tree starting at " + rootPid + " which is " + result);
        killProcesses("-KILL", result);
        Thread.sleep(1000);
        log.debug("tree should now be dead");
        computeChildren();
        result.retainAll(children.keySet());
        if (!result.isEmpty())
            log.error("Undead processes: " + result);
    }
    /**
     * @param result
     * @throws IOException
     * @throws InterruptedException
     */
    public void killProcesses(String signal, Set<Integer> result) throws IOException, InterruptedException {
        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("/bin/kill");
        cmd.add(signal);
        for (Integer i : result)
            cmd.add(i.toString());
        ProcessBuilder b = new ProcessBuilder(cmd);
        int exitCode = execute(b);
        if (exitCode != 0)
            log.warn("exit code from kill" + exitCode);
    }
    
    void drainToLog(final InputStream in) {
        Thread t = new Thread( new Runnable() {
            public void run() {
                try {
                BufferedReader r = new BufferedReader(new InputStreamReader(in));

                while (true) {
                    String txt = r.readLine();
                    if (txt == null) return;
                    log.debug("process generated: " + txt);
                }
                } catch (IOException e) {
                    if (!e.getMessage().equals("Stream closed"))
                      log.warn("error while draining", e);
                
                } finally { 
                    try {
                    in.close();
                    } catch (IOException e) {
                        assert false;
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
       
        
    }
    private  int execute(ProcessBuilder b) throws IOException, InterruptedException {
        b.redirectErrorStream(true);
        Process p = b.start();
        p.getOutputStream().close();
        drainToLog(p.getInputStream());
        int exitCode = p.waitFor();
        p.getInputStream().close();
        p.destroy();
        return exitCode;
    }
        
   

}
