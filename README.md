# Unwarp 3D membrane
Application on the C.Elegans embryo in 3D + T in fluorescence microscopy

Read the details of the project in this [report](https://it.overleaf.com/read/hwmbhfzbmysp).

## Repository content

- [`Membrane_Development.jar`](https://github.com/LisaPaganiEPFL/ImageAnalysis/blob/main/Membrane_Development.jar) final result to be used in combination with ImageJ.
- [`src`](https://github.com/LisaPaganiEPFL/ImageAnalysis/tree/main/src) folder contains all the classes used for the project.

## Classes

There are a total of 5 classes in the src folder. Here, there is a short description.

- [`Point3D.java`](https://github.com/LisaPaganiEPFL/ImageAnalysis/blob/main/src/Point3D.java) defines a 3D object with integer values. In this project is used for the volume pixel coordinates.
- [`Volume.java`](https://github.com/LisaPaganiEPFL/ImageAnalysis/blob/main/src/Volume.java) converts a ImagePlus image into an array.
- [`GenerateVolume.java`](https://github.com/LisaPaganiEPFL/ImageAnalysis/blob/main/src/GenerateVolume.java) creates synthtic volumes such as spheres or cubes.
- [`PolarTransformer.java`](https://github.com/LisaPaganiEPFL/ImageAnalysis/blob/main/src/PolarTransformer.java) contains all the methods and transformations used in the project.
- [`Detect_Membrane.java`](https://github.com/LisaPaganiEPFL/ImageAnalysis/blob/main/src/Detect_Membrane.java) takes as input a volume and detect the membrane according to some user parameters.
- [`Apply_Transformation.java`](https://github.com/LisaPaganiEPFL/ImageAnalysis/blob/main/src/Apply_Transformation.java) to be used after the `Detect_Membrane.java` PlugIn, it provides the polar transformation of the input image based on the membrane detected before.


