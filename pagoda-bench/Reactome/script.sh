#!/bin/sh

echo
echo AAAI 2019 HSRIQ to Datalog
echo Ontology: Reactome
echo Reasoner: Datalog Rewriting
echo

threads=4
tbox=reactome.rdfox

java -jar RDFOx.jar $tbox Reactome020 $threads
java -jar RDFOx.jar $tbox Reactome040 $threads
java -jar RDFOx.jar $tbox Reactome060 $threads
java -jar RDFOx.jar $tbox Reactome080 $threads