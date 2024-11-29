#!/bin/sh

set -euxo pipefail

if [ -e /dev/mmcblk0p1 ]; then
    mkdir -p /tmp/mmcblk0p1
    mount /dev/mmcblk0p1 /tmp/mmcblk0p1
    config_zip=$(find /tmp/mmcblk0p1 -type f ! -name '.*' -regex ".*connectionKit.*\.zip" -print)
    if [ -n "$config_zip" ]; then
        echo "connectionKit zip found"

        if [ -e /usr/bin/ggconfigd ]; then
            echo "ggconfigd found - gg-lite"
            config_file="/etc/greengrass/config.yaml"
            unzip -jo $config_zip -d /etc/greengrass/
            sed -i -e s:{{config_dir}}:\/etc\/greengrass:g -e s:{{nucleus_component}}:aws.greengrass.Nucleus-Lite:g $config_file
        elif [ -d "/greengrass/v2/" ]; then
            echo "/greengrass/v2/ found - gg-classic"
            config_file="/greengrass/v2/config/config.yaml"
            unzip -jo $config_zip -d /greengrass/v2/
            mv /greengrass/v2/config.yaml /greengrass/v2/config.yaml.fragment
            sed -i -e s:{{config_dir}}:\/greengrass\/v2:g -e s:{{nucleus_component}}:aws.greengrass.Nucleus:g /greengrass/v2/config.yaml.fragment
            # if there is a effective config that will be used instead of the new one, so delete it first
            rm -rf /greengrass/v2/config/*
            yq eval-all 'select(fileIndex == 0) * select(fileIndex == 1)' /greengrass/v2/config.yaml.fragment  /greengrass/v2/greengrass-classic.yaml.fragment > $config_file            
        else
            echo "ggconfigd and no directory /greengrass/v2/ found - doing nothing"    
            exit -1        
        fi
        chmod 644 $config_file
        rm $config_zip
        THING_NAME=$(grep 'thingName:' $config_file | awk '{print $2}' | tr -d '"' | sed 's/_//g')
        sync
        
        # TODO not working on readonly
        #if [ ! -z "$THING_NAME" ]; then
        #    echo "setting hostname to: $THING_NAME"
        #    sysctl kernel.hostname=$THING_NAME
        #    echo $THING_NAME > /etc/hostname
        #    sed -i 's/127.0.1.1.*/127.0.1.1\t'"$THING_NAME"'/g' /data/etc/hosts 
        #    sync
        # TODO not working            
        #fi
    fi
    if [ -f /tmp/mmcblk0p1/wpa_supplicant.conf ]; then
        echo "wpa_supplicant.conf found"
        mv /tmp/mmcblk0p1/wpa_supplicant.conf /etc/wpa_supplicant/wpa_supplicant-wlan0.conf
        chmod 600 /etc/wpa_supplicant/wpa_supplicant-wlan0.conf
        sync
    fi
    if [ -f /tmp/mmcblk0p1/wlan.network ]; then
        echo "wlan.network found"
        mv /tmp/mmcblk0p1/wlan.network  /etc/systemd/network/
        chmod 644 /etc/systemd/network/wlan.network
        sync
    fi
    umount /tmp/mmcblk0p1
    sync
fi