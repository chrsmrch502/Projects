Welcome to LyfeLine!
This README is here to make it a bit easier to start things up.

First, after pulling the repository, you'll need to import the folder "Project" into your IDE of choice.
It is recommended that you import it as a "Maven Project", then allow the pom.xml to load plugins and dependencies.

After that, you'll need to point the program to a MySQL server.
This is easiest to do by editing the values at the top of the DBM, found in Project/src/main/java/database/DBM.java
The DB_URL may or may not need to be changed for you, but the USER and PASS almost certainly will. The rest will likely be fine as-is.

Now you should be able to run the Main!
It is found in Project/src/main/java/Main.java.

Small tip about controls: a number of things can be double clicked:
Timelines and events can be opened, images on the Timeline View can be enlarged,
editors can have their edit/view modes toggled, and the zoom label can be reset by double clicking.

Additionally, the main Timeline View can be scrolled horizontally with Shift + Mouse Wheel,
and can be zoomed with Control + Mouse Wheel.

That should be all! The rest can be largely figured out through use, or through our presentation video!

We hope you enjoy using LyfeLine!
