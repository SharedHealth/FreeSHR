#!/bin/sh
./gradlew clean -x test dist -Prelease=$GO_PIPELINE_COUNTER
