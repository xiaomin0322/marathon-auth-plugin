# marathon-version 1.3.6


# marathon-auth-plugin

This plugin allows to delegate Marathon authentication to an external
frontend proxy which sets two (user configurable) headers with the login
name of the user and the groups he / she belongs to. In order to avoid
spoofing you must make sure that only the frontend can reach Marathon.

# Package

To build the package run this command: maven clean pack This will compile and package all plugins. 

maven package


#Using a Plugin

Run maven clean pack in the repository's root directory.
Start Marathon with the following flags: -Dmarathon.auth.conf=/etc/marathon/marathon-auth.json  -Djava.ext.dirs=/etc/marathon/plugin


-Djava.ext.dirs path where path is the directory in which the plugin jars are stored.

-Dmarathon.auth.conf conf.json where conf.json is the full path of the plugin configuration file.

#marathon-auth.json

https://github.com/xiaomin0322/marathon-auth-plugin/blob/master/src/main/resources/marathon-auth.json


 [
          {
            "user": "dev1",
            "password": "dev1",
            "permissions": [
              { "allowed": "view", "on": "/dev1" }
            ]
          },
          {
            "user": "dev3",
            "password": "dev3",
            "permissions": [
              { "allowed": "create", "on": "/dev3/" },
              { "allowed": "update", "on": "/dev3/" },
              { "allowed": "delete", "on": "/dev3/" },
              { "allowed": "view", "on": "/" }
            ]
          },
          {
            "user": "admin",
            "password": "admin",
            "permissions": [
              { "allowed": "create", "on": "/" },
              { "allowed": "update", "on": "/" },
              { "allowed": "delete", "on": "/" },
              { "allowed": "view", "on": "/" }
            ]
          }
 ]

