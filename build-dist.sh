#!/bin/sh
./gradlew clean test dist -Prelease=$GO_PIPELINE_COUNTER
