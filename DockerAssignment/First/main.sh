#!/bin/sh

# INFO:
# This shell command will print a hello message to the terminal inside a running container
# The message takes a value of an environment variable which is specified inside a running container
# For this assignment, the environment variable should contain "2019" value (omid the "" when defining the value of the environment variable in your Dockerfile)
echo "hello MCC $MCCCOURSE!!!"
