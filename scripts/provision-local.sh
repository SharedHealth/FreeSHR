#!/bin/bash
yum install -y http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm || true
yum install -y https://github.com/SharedHealth/FreeSHR-SCM/raw/master/dist/shr_scm_utils-0.1-1.noarch.rpm || true
yum install -y https://github.com/SharedHealth/FreeSHR-SCM/raw/master/dist/shr_scm-0.1-1.noarch.rpm || true

#Provision using ansible
sudo ansible-deploy  -i /vagrant/hosts/local /playbooks/FreeSHR-Playbooks/freeshr/site.yml

#Configure IPTables
(iptables -L | grep "CassandraDB") || (sudo /sbin/iptables -I INPUT 1 -p tcp --dport 9042 -j ACCEPT -m comment --comment "CassandraDB" && sudo service iptables save)

#Change host
sed -i 's/listen_address: localhost/listen_address: 192.168.33.10/g' /etc/cassandra/conf/cassandra.yaml
sed -i 's/rpc_address: localhost/rpc_address: 192.168.33.10/g' /etc/cassandra/conf/cassandra.yaml
sed -i 's/seeds: "127.0.0.1"/seeds: "192.168.33.10"/g' /etc/cassandra/conf/cassandra.yaml

#Start cassandra
service cassandra start
