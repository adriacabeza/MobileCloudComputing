#!/bin/sh

# FOR ADVANCED USERS - adb inside the VM *without* USB passthrough
#
# This script allows us to use adb commands inside the VM with
# devices outside of it. It requires ADB installed and working
# in the host PC. The vagrant vm must be up already.
# Remember to disable USB passthrough in the Vagrantfile if you
# plan to use this alternative.

# (Re)start ADB server on localhost:5037
adb kill-server >/dev/null 2>&1
adb start-server &

# Connect to Vagrant VM, and bind vm:5037 to host:5037
exec vagrant ssh -- -R 5037:localhost:5037
