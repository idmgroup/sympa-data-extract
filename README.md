sympa2googlegroups
==================

A pile of hackish code used to migrate lists and archives from Sympa to Google Groups for business. Used to migrate a few dozen ML from Sympa 5.3.4 to Groups in March 2014. Not designed to be generic in any way, should be enough to serve at starting point for a similar problem, i.e. contains what I would have liked to find before starting.

To use it, you shall first 

- have a working Maven environment
- have Firefox installed and in your PATH (or in the /Applications folder on OS X)
- have [Ditto GAM](|https://code.google.com/p/google-apps-manager/) v3.0.4 installed and in your PATH (at the time of this writing, 3.0.5 and 3.0.6 are out but groups settings command do not work)
- download sympa2mbox from http://6.ptmc.org/323/
- download mbox_send.py from https://gist.github.com/wojdyr/1176398

Description of the components
-------------

- sympa-data-extract: connects to the sympa web interface using admin credentials. Uses Selenium HQ to automate a Firefox browser, outputs:
  + a list.tsv file describing all lists hosted on the server (can be used in custom scripts later)
  + a lists folder with TSV files containing the subscribers foe each list (not even used in the final version in favor of the scripts)
  + an archives folder containing one archive zip file per list
  + a creation_scripts folder with list creation scripts (code shall be customized here to your needs)
  + an update_scripts folder with scripts to add subscribers to the lists
- sympa2mbox.sh
  + converts a folder full of sympa zip archives to a folder full of mbox files

Basic migration Steps
-------------

1. extract lists info using sympa-data-extract
1. convert archives to mbox files using sympa2mbox.sh
1. create all the lists using the scripts contained in the creation_scripts folder
1. import list archives using mbox_send.py
1. add list subscribers using the scripts contained in the update_scripts folder

License
-------------

![WTFPL](http://www.wtfpl.net/wp-content/uploads/2012/12/wtfpl-badge-2.png)

Code in this folder is licensed under the WTFPL
