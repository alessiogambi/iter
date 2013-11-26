#!/bin/bash
#
#
# Author:	Alessio Gambi
#			alessio.gambi@usi.ch
#
# Note:		This software is released as it is, and the author is not responsible for
#			any damage or problem this may cause to its users
#
# Prerequisites: Maven, Java, Screen, Matlab (for the Plasticity search)
#
#
COMPONENT_NAME="iter"
COMPONENT_PATH="/opt/$COMPONENT_NAME/"
LOG_FOLDER="/var/log/$COMPONENT_NAME/"

write_log()
{
  if [ ! -e "$LOG_FILE" ] ; then setup_log ; fi
  echo "* $*" | tee -a "$LOG_FILE"
}

setup_log()
{
  if [ ! -e "$LOG_FOLDER" ]
  then
    mkdir "$LOG_FOLDER"
  fi
  LOG_FILE="$LOG_FOLDER/$COMPONENT_NAME.log"
  touch $LOG_FILE
}

clean()
{
	write_log "Clean $COMPONENT_NAME"
	rm -v "$LOG_FOLDER/$COMPONENT_NAME."*
	rm -v "$COMPONENT_PATH"start-in-screen.sh
}

buildStartupOption(){
	# Default values
	STARTUP_OPTIONS=""
	# JVM settings
	STARTUP_OPTIONS="$STARTUP_OPTIONS -Xms256m"
	STARTUP_OPTIONS="$STARTUP_OPTIONS -Xmx384m"
	# ITER Mandatory configurations	
	STARTUP_OPTIONS="$STARTUP_OPTIONS -Dlog4j.configuration=file://"$COMPONENT_PATH"conf/log4j.properties"
	STARTUP_OPTIONS="$STARTUP_OPTIONS -Dat.ac.tuwien.dsg.cloud.configuration="$COMPONENT_PATH"conf/cloud.properties"
}

start() {
	write_log "  Starting $COMPONENT_NAME"

	buildStartupOption

	local oldpath="$PWD"
	cd "$COMPONENT_PATH"

	export MAVEN_OPTS="$STARTUP_OPTIONS"

	MANIFEST_URL="http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-manifest.xml"
	JMX_URL="http://www.inf.usi.ch/phd/gambi/attachments/autocles/doodle-clients.jmx"

	mvn exec:java \
	-Dexec.args="-c ite -s ite -m $MANIFEST_URL -j $JMX_URL" 2>&1 | tee -a "$LOG_FOLDER/$COMPONENT_NAME.out"

	# FOR THE MEANING OF THE PARAMETERS/OPTIONS READ THE PROVIDED README.md FILE
	#
	# -c ite => NOTE THIS MUST BE 3 char long !
	# -s ite => NOTE THIS MUST BE 3 char long !
	#
	# -e plasticity (default, plasticity)
	# -b
	# --input-file bootstrap.xml
	# --output-file sawtooth.2013.11.05.xml
	# -l sawtooth-rand
	# -n 1
	# -N 1
	# -r 0
	cd "$oldpath"
}

stop()
{
	# TODO Use screen -S ites -X quit or something to kill the screen
	write_log "  Stopping $COMPONENT_NAME: NOT TESTED !"
	local oldpath="$PWD"
	cd "$COMPONENT_PATH"
		#screen -S $COMPONENT_NAME -X quit
		ps aux | grep iter | grep -v grep | awk '{print $2}' | sudo xargs kill -15 >> "$LOG_FOLDER/$COMPONENT_NAME".out 2>&1
	cd "$oldpath"
	write_log "Stopped"
}

#######################################################################
# Here the main begins
#######################################################################

case "$1" in
start)
  start
;;
stop)
  stop
;;
restart)
  stop
  start
;;
clean)
  clean
;;
*)
write_log "Wrong input $*"
write_log "Usage: $0 {start|stop|restart}"

RETVAL=1
;;
esac

exit $RETVAL
