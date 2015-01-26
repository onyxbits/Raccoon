Raccoon - Google Play Desktop client.
-------------------------------------

Allows you to download Android APK files to your desktop PC and archive them 
there. Reasons for using this app:

* You want to check out apps anonymously without Google knowing you installed 
  them (e.g. because your better half is not suppose to spot them in your 
  account or because you don't want app suggestions based on your test run).
* You want to install an app on a secondary device which does not have access
  to Google Play or is not officially supported by the app in question.
* You want to have the option of going back to a previous version in case an 
  update causes trouble for you.
* You don't have access to unlimited data plan and want to conserve bandwidth
  by keeping a local cache (from which to distribute the apps to all the 
  devices in your household).
* You don't have your device at hand, but want to check the permissions of
  an app from your desktop PC.


Usage
-----

When starting for the first time, Raccoon will ask you to create a new archive.
The archive is basically your download folder (several can be created if 
desired). Archives store downloaded apps and user credentials. You will need
a valid Google Play account and (optionally) an Android ID. The account can be 
your regular one, but doesn't have to be. If you use your regular account, you
should also use an Android ID from one of your devices. Not providing an ID will
result in Raccoon creating one and linking it up with a pseudo device.

Building
--------

Currently, this is only an eclipse project, so there is no ant file. You
have to import the project into your workspace and resolve the dependencies
listed in the libs/ dir.

Prebuild binaries
-----------------

Ready to use binaries are available from:

http://www.onyxbits.de/raccoon

SHA1 Checksums:

45e28f59a2a6d1fb17a100ee064bb035009331f5  raccoon-3.3.exe
648487120fafd5ebbf1838c48556c81375e74aab  raccoon-3.3.jar
3fe8548c944643500b8e5170d00e5c5ec70e266a  raccoon-3.2.exe
b172ab687a11c5a0f37a2baa0ebc1cfe47b8bccc  raccoon-3.2.jar
