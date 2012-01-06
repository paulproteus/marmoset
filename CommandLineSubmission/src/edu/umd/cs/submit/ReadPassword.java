/**
 * Marmoset: an automated snapshot, submission and testing system
 * Copyright (C) 2005, University of Maryland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Email: marmoset-discuss@cs.umd.edu
 * 
 * Snail mail:
 * The Marmoset Project
 * Attention: Dr. Bill Pugh
 * Dept. of Computer Science
 * Univ. of Maryland
 * College Park, MD 20742
 */

package edu.umd.cs.submit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PushbackInputStream;

/**
 * @author pugh
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ReadPassword {

    /**
     * Reads and returns some sensitive piece of information (e.g. a password)
     * from the console (i.e. System.in and System.out) in a secure manner.
     * <p>
     * For top security, all console input is masked out while the user types in
     * the password. Once the user presses enter, the password is read via a
     * call to {@link #readLineSecure readLineSecure(in)}, using a
     * PushbackInputStream that wraps System.in.
     * <p>
     * This method never returns null.
     * <p>
     * 
     * @throws IOException
     *             if an I/O problem occurs
     * @throws InterruptedException
     *             if the calling thread is interrupted while it is waiting at
     *             some point
     * @see <a
     *      href="http://java.sun.com/features/2002/09/pword_mask.html">Password
     *      masking in console </a>
     */
    public static final String readConsoleSecure(String prompt) throws IOException {
        // start a separate thread which will mask out all chars typed on
        // System.in by overwriting them using System.out:
        StreamMasker masker = new StreamMasker(System.out, prompt);
        Thread threadMasking = new Thread(masker);
        threadMasking.start();

        // Goal: block this current thread (allowing masker to mask all user
        // input)
        // while the user is in the middle of typing the password.
        // This may be achieved by trying to read just the first byte from
        // System.in,
        // since reading from System.in blocks until it detects that an enter
        // has been pressed.
        // Wrap System.in with a PushbackInputStream because this byte will be
        // unread below.
        PushbackInputStream pin = new PushbackInputStream(System.in);
        int b = pin.read();

        // When current thread gets here, the block on reading System.in is over
        // (e.g. the user pressed enter, or some error occurred?)

        // signal threadMasking to stop and wait till it is dead:
        masker.stop();
        try {
            threadMasking.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // check for stream errors:
        if (b == -1)
            throw new IOException(
                    "end-of-file was detected in System.in without any data being read");
        if (System.out.checkError())
            throw new IOException("an I/O problem was detected in System.out");

        // pushback the first byte and supply the now unaltered stream to
        // readLineSecure which will return the complete password:
        pin.unread(b);
        return new BufferedReader(new InputStreamReader(pin)).readLine();

    }

    public static void main(String args[]) throws Exception {
        String password = readConsoleSecure("Password: ");
        System.out.println("Password: " + password);
    }

    /**
     * Masks an InputStream by overwriting blank chars to the PrintStream
     * corresponding to its output. A typical application is for password input
     * masking.
     * <p>
     * 
     * @see <a
     *      href="http://forum.java.sun.com/thread.jsp?forum=9&thread=490728&tstart=0&trange=15">My
     *      forum posting on password entry </a>
     * @see <a
     *      href="http://java.sun.com/features/2002/09/pword_mask.html">Password
     *      masking in console </a>
     */
    public static class StreamMasker implements Runnable {

        private static final String TEN_BLANKS = "          ";

        private final PrintStream out;

        private final String promptOverwrite;

        private volatile boolean doMasking; // MUST be volatile to ensure update

        // by one thread is instantly
        // visible to other threads

        /**
         * Constructor.
         * <p>
         * 
         * @throws IllegalArgumentException
         *             if out == null; prompt == null; prompt contains the char
         *             '\r' or '\n'
         */
        public StreamMasker(PrintStream out, String prompt)
                throws IllegalArgumentException {
            if (out == null)
                throw new IllegalArgumentException("out == null");
            if (prompt == null)
                throw new IllegalArgumentException("prompt == null");
            if (prompt.indexOf('\r') != -1)
                throw new IllegalArgumentException("prompt contains the char '\\r'");
            if (prompt.indexOf('\n') != -1)
                throw new IllegalArgumentException("prompt contains the char '\\n'");

            this.out = out;

            this.promptOverwrite = "\r" + // sets cursor back to beginning of
                    // line:
                    prompt + // writes prompt (critical: this reduces visual
                    // flicker in the prompt text that otherwise occurs
                    // if simply write blanks here)
                    TEN_BLANKS + // writes 10 blanks beyond the prompt to
                                    // mask
                    // out any input; go 10, not 1, spaces beyond
                    // end of prompt to handle the (hopefully rare)
                    // case that input occurred at a rapid rate
                    "\r" + // sets cursor back to beginning of line:
                    prompt; // writes prompt again; the cursor will now be
            // positioned immediately after prompt (critical:
            // overwriting only works if all input text starts
            // here)
        }

        /**
         * Repeatedly overwrites the current line of out with prompt followed by
         * blanks. This effectively masks any chars coming on out, as long as
         * the masking occurs fast enough.
         * <p>
         * To help ensure that masking occurs when system is in heavy use, the
         * calling thread will have its priority boosted to the max for the
         * duration of the call (with its original priority restored upon
         * return). Interrupting the calling thread will eventually result in an
         * exit from this method, and the interrupted status of the calling
         * thread will be set to true.
         * <p>
         * 
         * @throws RuntimeException
         *             if an error in the masking process is detected
         */
        public void run() throws RuntimeException {

            doMasking = true; // do this assignment here and NOT at variable
            // declaration line to allow this instance to be
            // restarted if desired
            while (doMasking) {
                out.print(promptOverwrite);
                // call checkError, which first flushes out, and then lets us
                // confirm that everything was written correctly:
                if (out.checkError())
                    throw new RuntimeException("an I/O problem was detected in out"); // should
                                                                                        // be
                // an
                // IOException,
                // but that
                // would
                // break
                // method
                // contract

                // limit the masking rate to fairly share the cpu; interruption
                // breaks the loop
                try {
                    Thread.sleep(10); // have experimentally found that
                    // sometimes see chars for a brief bit
                    // unless set this to its min value
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // resets the
                    // interrupted status,
                    // which is typically
                    // lost when an
                    // InterruptedException
                    // is thrown, as per our
                    // method contract; see
                    // Lea, "Concurrent
                    // Programming in Java
                    // Second Edition", p.
                    // 171
                    return; // return, NOT break, since now want to skip the
                    // lines below where write bunch of blanks since
                    // typically the user will not have pressed enter
                    // yet
                }
            }
            // erase any prompt that may have been spuriously written on the
            // NEXT line after the user pressed enter (see reply 2 of
            // http://forum.java.sun.com/thread.jsp?forum=9&thread=490728&tstart=0&trange=15)
            out.print('\r');
            for (int i = 0; i < promptOverwrite.length(); i++)
                out.print(' ');
            out.print('\r');

        }

        /** Signals any thread executing run to stop masking and exit run. */
        public void stop() {
            doMasking = false;
        }

    }
}