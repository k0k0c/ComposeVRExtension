#!/bin/sh
export Protocol_Dir=external/Protocol
export Java_Dir=src/main/java

for f in $Protocol_Dir/*.proto; do
	protoc "-I=$Protocol_Dir" "--java_out=$Java_Dir" "$f";	
done
