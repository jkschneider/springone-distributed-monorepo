#!/bin/sh

# This is what needs to happen to set up /etc/init.d/atlas, somehow

sudo curl -Lo /etc/init.d/atlas https://gist.githubusercontent.com/jkschneider/3d8594e1dd8d78ae3e5cb63089cd72d1/raw/3cfbbaa5d1e15cdff1f5f55080318d8f8334e2b2/atlas
sudo chmod 755 /etc/init.d/atlas
sudo chown root:root /etc/init.d/atlas

sudo /etc/init.d/atlas start
