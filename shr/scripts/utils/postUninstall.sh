#!/bin/sh

rm -f /etc/init.d/bdshr
rm -f /etc/default/bdshr
rm -f /var/run/bdshr

#Remove bdshr from chkconfig
chkconfig --del bdshr