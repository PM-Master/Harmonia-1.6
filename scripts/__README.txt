This directory contains scripts used to alleviate some of the drudgery
involved in creating applications "the right way."  

1. generate_icon_info.rb

The first script that this directory will include is a script to
read from the images directory and generate from it the appropriate
icon references to be used in the application.

The script pulls information about the icons in their respective directories
and creates an "info" java class that can be accessed from any 
application using these icons.