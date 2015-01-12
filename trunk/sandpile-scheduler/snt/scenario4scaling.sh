
#!/bin/bash

maxexp=50
exp=1

fixedparam="assignation=random tasksperbotmethod=fixed tasksperbot=50 methodruntime=homogeneous runtimeavg=1 methodcodesize=homogeneous codesizeavg=0 methodarrival=homogeneous arrivalavg=0 parsing=true"

proc32="fileprocessors=homogeneousQ32 filenetwork=homogeneousC32 rewiring=0.2 b=32 q=32"
proc64="fileprocessors=homogeneousQ64 filenetwork=homogeneousC64 rewiring=0.2 b=64 q=64"
proc128="fileprocessors=homogeneousQ128 filenetwork=homogeneousC128 rewiring=0.2 b=128 q=128"
proc256="fileprocessors=homogeneousQ256 filenetwork=homogeneousC256 rewiring=0.2 b=256 q=256"
proc512="fileprocessors=homogeneousQ512 filenetwork=homogeneousC512 rewiring=0.2 b=512 q=512"
proc1024="fileprocessors=homogeneousQ1024 filenetwork=homogeneousC1024 rewiring=0.2 b=1024 q=1024"
proc2048="fileprocessors=homogeneousQ2048 filenetwork=homogeneousC2048 rewiring=0.2 b=2048 q=2048"

randompolicy="sandpile=false"

while [ $exp -le $maxexp ]; do
echo $exp

java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc32  >> makespanSP32
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc64  >> makespanSP64 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc128 >> makespanSP128 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc256 >> makespanSP256 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc512 >> makespanSP512 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc1024 >> makespanSP1024
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc2048 >> makespanSP2048   


java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc32 $randompolicy >> makespanR32
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc64 $randompolicy >> makespanR64 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc128 $randompolicy >> makespanR128 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc256 $randompolicy >> makespanR256 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc512 $randompolicy >> makespanR512 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc1024 $randompolicy >> makespanR1024
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc2048 $randompolicy >> makespanR2048   

	exp=$[ $exp + 1 ]
done
