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

package edu.umd.cs.submitServer.servlets;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.umd.cs.marmoset.modelClasses.Course;
import edu.umd.cs.marmoset.modelClasses.Project;

public class CourseCalendar extends SubmitServerServlet {


	private static final long serialVersionUID = 1L;

	// Format which includes the timezone at the end. This is the format
	// used by the tasklist's own written files for example.
	private static final String DATEFORMATZ = "yyyyMMdd'T'HHmmss'Z'"; // NOI18N
	// Format used when the timezone is specified separately, e.g. with TZ:PST
	private static final String DATEFORMAT = "yyyyMMdd'T'HHmmss"; // NOI18N
	private static final String DATEFORMATTZOFFSET = "Z"; // NOI18N


	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String uidBase = request.getServerName() +"/" +  request.getContextPath() + "/cCalendar";
		Course course = (Course) request.getAttribute(COURSE);
		List<Project> projects = (List<Project>) request.getAttribute(PROJECT_LIST);

		ServletOutputStream sout = response.getOutputStream();
		response.setContentType("text/calendar");

		SimpleDateFormat df = new SimpleDateFormat(DATEFORMATZ);
		SimpleDateFormat timezoneoffset = new SimpleDateFormat(DATEFORMATTZOFFSET);

		Writer out = new OutputStreamWriter(sout);

		writeEscaped(out,"BEGIN","VCALENDAR");
		writeEscaped(out,"VERSION","2.0");
		writeEscaped(out,"PRODID","Marmoset");
    
		writeEscaped(out,"X-WR-CALNAME",course.getCourseName());
		for(Project p : projects) {

			writeEscaped(out,"BEGIN","VEVENT");
			writeEscaped(out,"UID",uidBase + p.getProjectPK());
			writeEscaped(out,"SUMMARY",  course.getCourseName() + " project " + p.getProjectNumber()
						+ ": " + p.getTitle());
			Date dueDate = new Date(p.getOntime().getTime());
			writeEscaped(out,"DTSTART", df.format(dueDate));
			writeEscaped(out,"TZNAME",new SimpleDateFormat("z").format(dueDate));
			writeEscaped(out,"TZOFFSETTO",timezoneoffset.format(dueDate));

			String link = getContextLink(request) + getProjectLinkForStudent(p);
			writeEscaped(out,"URL;VALUE=URI", link);
			if (p.getDescription() != null)
				writeEscaped(out, "DESCRIPTION", p.getDescription());
			writeEscaped(out,"END","VEVENT");

		}
		writeEscaped(out,"END","VCALENDAR");


		out.close();

	}



	private void writeEscaped(Writer writer, String name, String value)
			throws IOException {
		writeEscaped(writer, name, null, value);
	}

	/*
	 * Write out a content line escaped according to the spec: break at 75
	 * chars, add escapes to certain characters, etc.
	 *
	 * @param writer the writer used to write the data
	 *
	 * @param name the name of the tag to write (without the ':')
	 *
	 * @param param the param of the field
	 *
	 * @param value the value to write
	 */
	private void writeEscaped(Writer writer, String name, String param,
			String value) throws IOException {
		int col = name.length();
		writer.write(name);

		if (param != null) {
			col += param.length() + 1; // NOI18N
			writer.write(" "); // NOI18N
			writer.write(param);
		}
		++col;
		writer.write(":");

		if (value != null)
			for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '\n':
				writer.write("\\n"); // NOI18N
				col++; // One extra char expansion
				break;
			case ';':
			case ',':
			case '\\':
				// Escape the character by preceding it by a "\"
				writer.write('\\');
				col++; // One extra char expansion
				// NOTE FALL THROUGH!
			default:
				writer.write(c);
				break;
			}

			col++;
			if (col >= 75) {
				col = 1; // for the space on the next line
				writer.write("\r\n "); // NOI18N note the space - important
			}
		}
		writer.write("\r\n");
	}
}
