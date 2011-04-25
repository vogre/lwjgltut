LWJGLTut
========

This is an attempt to port the arcsynthesis OpenGL tutorials found at 
[the gltut tutorial](http://arcsynthesis.org/gltut/ "Learning Modern 3D Graphics Programming Through OpenGL") from C++ to Scala (and LWJGL).

The source gltut is hosted by alfonse at [bitbucket](https://bitbucket.org/alfonse/gltut/wiki/Home).

The port is using sbt as the build tool, so you will need that to build. All the dependencies are managed by sbt and the sbt-lwjgl-plugin.

Starting with Tutorial 6, the project uses [simplex3d](http://code.google.com/p/simplex3d/) - for now you should manually place the jar files for math and data in the lib folder.
