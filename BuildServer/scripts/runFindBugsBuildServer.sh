#! /bin/sh

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

# Use the BuildServer to run FindBugs on a number of submissions.

#BUILDSERVER_ROOT=$HOME/workspace/BuildServer
echo "BUILDSERVER_ROOT is $BUILDSERVER_ROOT"

# Make sure BUILDSERVER_ROOT is set
if [ -z "$BUILDSERVER_ROOT" ]; then
	echo "Environment variable BUILDSERVER_ROOT is not set"
	echo "Please set it to the directory where the BuildServer is checked out"
	exit 1
fi

props=""

while [ "$#" -gt 0 ] && [ `expr "$1" : "-"` -gt 0 ]; do
	opt="$1"
	shift
	
	case $opt in
		-debugSecurity)
			props="-Ddebug.security=true $props"
			;;

		-D*)
			props="$opt $props"
			;;
		
		*)
			echo "Unknown option: $opt"
			exit 1
			;;
	esac
done

if [ "$#" -ne 2 ]; then
	echo "Usage: testSubmission.sh <config file> <submission list>"
	exit 1
fi
config_file=$1
submission_list=$2

# Construct BuildServer runtime classpath.
# This should be kept in sync with the runBuildServer2 script
# that runs the BuildServer in daemon mode.
classpath="$BUILDSERVER_ROOT/bin"
for j in modelClasses findbugs commons-httpclient commons-logging junit bcel log4j dom4j-full; do
	classpath="$classpath:$BUILDSERVER_ROOT/lib/$j.jar"
done

echo "classpath is $classpath"

java -classpath "$classpath" \
	-Xmx64m \
	$props \
	edu.umd.cs.buildServer.FindBugsBuildServer \
	"$config_file" \
	"$submission_list"
