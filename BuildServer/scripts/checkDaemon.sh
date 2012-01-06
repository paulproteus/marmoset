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

# checkDaemon.sh --- Restart the BuildServer if it is not running

# Requires:
#  - that the configuration properties file and the runBuildServer
#    script are in the current directory

force=no
if [ "$1" = "-force" ]; then
	force=yes
	shift
fi

# The existence of the "do_not_run" file means that we
# should not try to start the daemon.
if [ "$force" = "no" ] && [ -r "do_not_run" ]; then
    echo "do_not_run file exists"
	exit 0
fi

propsfile=config.properties
if [ ! -z "$1" ]; then
	propsfile="$1"
fi

osname=`uname -s`
if [ "$osname" != "Linux" ] && [ "$osname" != "SunOS" ] && [ "$osname" != "Darwin" ]; then
	echo "This script only runs on Linux, Solaris or Darwin"
	exit 1
fi

if [ -r "buildserver.pid" ]; then
	pid=`cat buildserver.pid`
	if [ -r "/proc/$pid" ]; then
		# Daemon is still running. Cool.
		exit 0
	fi
fi

# Try to respawn.
# But don't actually wait to find out whether it succeeded.
./runBuildServer $propsfile > /dev/null 2>&1 &
exit 0
