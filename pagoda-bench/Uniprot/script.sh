#!/bin/sh

echo
echo AAAI 2019 HSRIQ to Datalog
echo Ontology: Uniprot
echo Reasoner: Datalog Rewriting
echo

threads=4
tbox=uniprot.rdfox

java -jar RDFOx.jar $tbox Uniprot005 $threads
java -jar RDFOx.jar $tbox Uniprot010 $threads
java -jar RDFOx.jar $tbox Uniprot015 $threads
java -jar RDFOx.jar $tbox Uniprot020 $threads