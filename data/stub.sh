#!/bin/sh
#
# use this stub to create an executable from a single-jar file
#
# cat data/stub.sh build/ZurvingQuickEdit.jar > build/ZurvingQuickEdit.sh && chmod +x build/ZurvingQuickEdit.sh
#
# Reference: https://coderwall.com/p/ssuaxa
#
###################################################################################################################


MYSELF=`which "$0" 2>/dev/null`
[ $? -gt 0 -a -f "$0" ] && MYSELF="./$0"
java=java
if test -n "$JAVA_HOME"; then
    java="$JAVA_HOME/bin/java"
fi
exec "$java" $java_args -jar $MYSELF "$@"
exit 1 
