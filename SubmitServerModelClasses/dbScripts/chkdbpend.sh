#!/bin/sh                                                                       

#  Marmoset: a student project snapshot, submission, testing and code review
#  system developed by the Univ. of Maryland, College Park
#
#  Developed as part of Jaime Spacco's Ph.D. thesis work, continuing effort led
#  by William Pugh. See http://marmoset.cs.umd.edu/
#
#  Copyright 2005 - 2011, Univ. of Maryland
#
#  Licensed under the Apache License, Version 2.0 (the "License"); you may not
#  use this file except in compliance with the License. You may obtain a copy of
#  the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#  License for the specific language governing permissions and limitations under
#  the License.

# Ask MySQL to tell us what tasks are new or pending.  If any of these are      
# the same as a task that was also new or pending the last time we asked,       
# output a message and return non-0.  Otherwise, return 0.  Useful for
# detecting nasty jobs when run, say, from a cron job with a period of 15
# or 20 minutes.            

subf=$HOME/.last.sub.chkdbpend
tsf=$HOME/.last.ts.chkdbpend
MY_CNF=$HOME/my.cnf

# List all as-yet-incomplete submissions
sql_sub_cmd=\
"select submission_pk, build_status, coursename, title\
    from submissions, projects, courses\
    where build_status != 'complete' and\
          submissions.project_pk = projects.project_pk and\
          projects.course_pk = courses.course_pk and\
          project_jarfile_pk != 0 and\
          build_status != 'broken';"

# List all new and pending test-setup submissions
sql_ts_cmd=\
"select test_setups.project_jarfile_pk, jarfile_status, coursename, projects.title\
    from submissions, test_setups, projects, courses \
    where \
    (test_setups.jarfile_status = 'new' OR test_setups.jarfile_status = 'pending') \
    AND test_setups.project_pk = projects.project_pk \
    AND submissions.project_pk = projects.project_pk \
    AND projects.course_pk = courses.course_pk \
    AND submissions.student_registration_pk = projects.canonical_student_registration_pk \
    ORDER BY submissions.submission_number DESC LOCK IN SHARE MODE;"

# Shuffle output files
for f in $subf $tsf ; do
    test -f "$f.old" && rm $f.old
    test -f "$f"     && mv $f $f.old
done

mysql --defaults-file=$MY_CNF -e "$sql_sub_cmd" > $subf.mysqlout 2>&1
mysql --defaults-file=$MY_CNF -e "$sql_ts_cmd"  > $tsf.mysqlout  2>&1

for f in $subf $tsf ; do
    egrep '[0-9]+' $f.mysqlout | sort > $f
done

if [ -f "$subf.old" ] ; then
    if comm -1 -2 $subf $subf.old | grep '.' ; then
        old=`stat -c %Y $subf.old`
        new=`stat -c %Y $subf`
        min=`expr \( 15 + $new - $old \) / 60`
        echo "Warning! Above submissions have been pending for $min minutes..."
        exit 1
    fi
fi

if [ -f "$tsf.old" ] ; then
    if comm -1 -2 $tsf $tsf.old | grep '.' ; then
        old=`stat -c %Y $tsf.old`
        new=`stat -c %Y $tsf`
        min=`expr \( 15 + $new - $old \) / 60`
        echo "Warning! Above test setups have been pending for $min minutes..."
        exit 1
    fi
fi
