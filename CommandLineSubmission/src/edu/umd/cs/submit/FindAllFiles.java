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

/*
 * Created on Jan 24, 2005
 */
package edu.umd.cs.submit;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class FindAllFiles {
    Set<File> s = new TreeSet<File>();

    File root;

    String rootPath;

    int rootPathLength;
    Pattern p;

    FindAllFiles(File root, Pattern filesToIgnore) throws IOException {
        p = filesToIgnore;
        root = root.getCanonicalFile().getParentFile();
        rootPath = root.getCanonicalPath();
       // System.out.println("Root path: " + rootPath);
        rootPathLength = rootPath.length();
        s.add(root);
        searchFrom(root);
    }

    private void searchFrom(File dir) throws IOException {
        File f[] = dir.listFiles();
        if (f != null)
        for (File nextFile : f) {
            String nextPath = nextFile.getCanonicalPath();

            if (nextPath.startsWith(rootPath) && !p.matcher(nextFile.getName()).matches() && s.add(nextFile)) {
                // System.out.println("Adding " + nextPath.substring(rootPathLength + 1));
                if (nextFile.isDirectory())
                    searchFrom(nextFile);
            }
        }
    }

    public Collection<File> getAllFiles() {
        return s;
    }

    public static void main(String[] args) throws IOException {
        Pattern p = new FilesToIgnore().getPattern();
        FindAllFiles allFiles = new FindAllFiles(new File("."), p);
        System.out.println("---");
        for(File f : allFiles.s)
            if (!f.isDirectory())
                System.out.println(f.getPath().substring(allFiles.rootPathLength+1));


    }
}
