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

if [ "$#" -ne 1 ]; then
	echo "Usage: dumpTestOutcomes.sh <test outcome file>"
	exit 1
fi

filename="$1"

# Make sure BUILDSERVER_ROOT is set
if [ -z "$BUILDSERVER_ROOT" ]; then
	echo "Environment variable BUILDSERVER_ROOT is not set"
	echo "Please set it to the directory where the BuildServer is checked out"
	exit 1
fi

classpath="$BUILDSERVER_ROOT/bin"
for j in modelClasses commons-httpclient commons-logging junit log4j dom4j; do
	classpath="$classpath:$BUILDSERVER_ROOT/lib/$j.jar"
done

java -classpath "$classpath" edu.umd.cs.buildServer.DumpTestOutcomes "$filename"
