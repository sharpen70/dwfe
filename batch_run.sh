#!/bin/bash

bench=./AGOSUV-bench/
output=./Result/

for case in $(ls ${bench})
do
  inputO=${bench}${case}/${case}.dlp
  inputQ=${bench}${case}/${case}_queries.dlp

  java -cp ./dwfe-0.0.1-SNAPSHOT.jar org.guiiis.dwfe.App ${inputO} \
    ${inputQ} #${output}${case}.re
done
