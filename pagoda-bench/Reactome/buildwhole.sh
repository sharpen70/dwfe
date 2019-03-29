#!/bin/bash
data=Reactome020

for ttl in $(find ./$data -name *.ttl)
do
  cat $ttl >> $data.owl
done
