#!/bin/sh

ln -s /opt/bdshr/bin/bdshr /etc/init.d/bdshr
ln -s /opt/bdshr/etc/bdshr /etc/default/bdshr
ln -s /opt/bdshr/var /var/run/bdshr

if [ ! -e /var/log/bdshr ]; then
    mkdir /var/log/bdshr
fi

# Add bdshr service to chkconfig
chkconfig --add bdshr