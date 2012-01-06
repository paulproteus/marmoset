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

# For several important Marmoset logfiles, take a snapshot of key events
# and compare with previous snapshot.  Print out all new key events.  Which
# files and events are considered are hard coded in the code below.

# Do catalina.out
name=catalina.out
dir=$HOME/tomcat/logs
cp ${dir}/${name} $HOME/.${name}.new
if [ -f $HOME/.${name} ] ; then
    if comm -1 -3 $HOME/.${name} $HOME/.${name}.new | grep . > /dev/null ; then
        echo ${name}:
        comm -1 -3 $HOME/.${name} $HOME/.${name}.new | grep .
    fi
fi
mv -f $HOME/.${name}.new $HOME/.${name}

# Do servletException.log
name=servletException.log
dir=$HOME/tomcat/logs/marmoset
cp ${dir}/${name} $HOME/.${name}.new
if [ -f $HOME/.${name} ] ; then
    if comm -1 -3 $HOME/.${name} $HOME/.${name}.new | grep . > /dev/null ; then
        echo ${dir}/${name}:
        comm -1 -3 $HOME/.${name} $HOME/.${name}.new | grep .
    fi
fi
mv -f $HOME/.${name}.new $HOME/.${name}

# Do submitServerFilter.log
name=submitServerFilter.log
dir=$HOME/tomcat/logs/marmoset
egrep '(WARN|ERROR|FATAL)' ${dir}/${name} > $HOME/.${name}.new
if [ -f $HOME/.${name} ] ; then
    if comm -1 -3 $HOME/.${name} $HOME/.${name}.new | grep . > /dev/null ; then
        echo ${dir}/${name}:
        comm -1 -3 $HOME/.${name} $HOME/.${name}.new | grep .
    fi
fi
mv -f $HOME/.${name}.new $HOME/.${name}

# Do submitServerServlet.log
name=submitServerServlet.log
dir=$HOME/tomcat/logs/marmoset
egrep '(WARN|ERROR|FATAL)' ${dir}/${name} > $HOME/.${name}.new
if [ -f $HOME/.${name} ] ; then
    if comm -1 -3 $HOME/.${name} $HOME/.${name}.new | grep . > /dev/null ; then
        echo ${dir}/${name}:
        comm -1 -3 $HOME/.${name} $HOME/.${name}.new | grep .
    fi
fi
mv -f $HOME/.${name}.new $HOME/.${name}
