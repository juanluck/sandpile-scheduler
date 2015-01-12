
#!/bin/bash

maxexp=50
exp=1

fixedparam="tasksperbotmethod=fixed tasksperbot=50 methodruntime=homogeneous runtimeavg=1 methodcodesize=homogeneous codesizeavg=0 methodarrival=homogeneous arrivalavg=0 parsing=true"

proc32="fileprocessors=increasingQ32 filenetwork=homogeneousC32 rewiring=0.2 b=32 q=32"
proc64="fileprocessors=increasingQ64 filenetwork=homogeneousC64 rewiring=0.2 b=64 q=64"
proc128="fileprocessors=increasingQ128 filenetwork=homogeneousC128 rewiring=0.2 b=128 q=128"
proc256="fileprocessors=increasingQ256 filenetwork=homogeneousC256 rewiring=0.2 b=256 q=256"
proc512="fileprocessors=increasingQ512 filenetwork=homogeneousC512 rewiring=0.2 b=512 q=512"
proc1024="fileprocessors=increasingQ1024 filenetwork=homogeneousC1024 rewiring=0.2 b=1024 q=1024"
proc2048="fileprocessors=increasingQ2048 filenetwork=homogeneousC2048 rewiring=0.2 b=2048 q=2048"

randomsandpile="assignation=random"
randompolicy="assignation=random sandpile=false"
roundrobinpolicy="assignation=roundrobin sandpile=false"

while [ $exp -le $maxexp ]; do
echo $exp

java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc32 $randomsandpile >> makespanSP32
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc64 $randomsandpile >> makespanSP64 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc128 $randomsandpile >> makespanSP128 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc256 $randomsandpile >> makespanSP256 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc512 $randomsandpile >> makespanSP512 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc1024 $randomsandpile >> makespanSP1024
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc2048 $randomsandpile >> makespanSP2048   


java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc32 $randompolicy >> makespanR32
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc64 $randompolicy >> makespanR64 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc128 $randompolicy >> makespanR128 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc256 $randompolicy >> makespanR256 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc512 $randompolicy >> makespanR512 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc1024 $randompolicy >> makespanR1024
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc2048 $randompolicy >> makespanR2048   

java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc32 $roundrobinpolicy >> makespanRR32
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc64 $roundrobinpolicy >> makespanRR64 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc128 $roundrobinpolicy >> makespanRR128 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc256 $roundrobinpolicy >> makespanRR256 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc512 $roundrobinpolicy >> makespanRR512 
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc1024 $roundrobinpolicy >> makespanRR1024
java -cp "$CLASSPATH:../snt/lib/*" org.Experiment $fixedparam $proc2048 $roundrobinpolicy >> makespanRR2048   


	exp=$[ $exp + 1 ]
done
