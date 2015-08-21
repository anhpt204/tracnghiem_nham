#!/bin/sh
#

mvn clean
mvn install -DskipTests=true
# clean it again, so it will be built in the following using the release profile, and not the default profile
# release profile is necessary for customized classpath in jars' Manifest (e.g. a classpath that includes the libs-dependencies)
mvn clean
mvn source:jar

for contr in freight matsim4urbansim cadytsIntegration grips networkEditor roadpricing gtfs2matsimtransitschedule otfvis evacuation locationchoice parking transEnergySim
do
	cd $contr
	
	mvn -Prelease -DskipTests=true
	cd ..
done
