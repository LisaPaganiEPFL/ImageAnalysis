import java.util.ArrayList;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.GaussianBlur3D;
import ij.plugin.PlugIn;


public class Experiments implements PlugIn{
	
	public static void main(String arg[]) {
		
		new ImageJ();

		
		// Non centred target
		
		/*GenerateVolume generator = new GenerateVolume();
		ImagePlus impVolume = generator.nonCentredSphere(101, 10);
		//ImagePlus impVolume = generator.sphere(101, 20);
		impVolume.show();
		VolumeA volume = new VolumeA(impVolume);
		PolarTransformerA transformer = new PolarTransformerA();
		ImagePlus polar = transformer.fastToPolar(volume,new Point3D(40,40,40), 1, true);
		polar.show();*/
		
		
		// Shell centred
		
		//GenerateVolume generator = new GenerateVolume();
		PolarTransformer trans = new PolarTransformer();
		ArrayList<double[]> norm = new ArrayList<double[]>();
		ArrayList<double[]> point = new ArrayList<double[]>();
		
		String currentDir = System.getProperty("user.dir");
		IJ.log("Current dir using System:" + currentDir);
		
		norm = trans.loadCSV("../imagej-space/norm.cvs",3);
		point = trans.loadCSV("../imagej-space/point.cvs",3);
		
		//ImagePlus impVolume = generator.sphere(101, 20);
		//ImagePlus impVolume = generator.smoothSphere(101, 30);
		//impVolume.show();
		//Volume volume = new Volume(impVolume);
		//PolarTransformer transformer = new PolarTransformer();
		//transformer.getNormal(volume, volume.getCenterMass(0, 0), 0, 0, 1, 0.8);
		//ImagePlus polar = transformer.fastToPolar(volume,new Point3D(40,40,40), 1, true);
		//polar.show();
		
		
	}

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		
		//new ImageJ();
		//GenerateVolume generator = new GenerateVolume();
		//ImagePlus fullCube = generator.fullCube(15);
		//fullCube.show();
		
		ImagePlus in = IJ.getImage();
		//GaussianBlur3D g = new GaussianBlur3D();
		//g.blur(in, 0, 0, 0);
		
		
		/*PolarTransformer transformer = new PolarTransformer();
		Volume volume = new Volume(in);
		ImagePlus polar = transformer.fastToPolar(volume, new Point3D(225,225,200), 2.33, true);
		//polar.getProcessor().resetMinAndMax();
		polar.show();*/
		
		
		// Brain surface detected 130 threshold
		
		PolarTransformer transformer = new PolarTransformer();
		
		
		transformer.arrListNorms = transformer.loadCSV("../imagej-space/normMedianGaussian.cvs",3);
		transformer.arrListPoints = transformer.loadCSV("../imagej-space/pointMedianGaussian.cvs",3);
		transformer.arrListAngles = transformer.loadCSV("../imagej-space/angleMedianGaussian.cvs",2);
		Volume volume1 = new Volume(in);
		ImagePlus color = transformer.getNomalSurfacesFromLoad(volume1, 0, 0, 3.41);
		color.show();
		
		//Volume volume = new Volume(in);
		//ImagePlus polar = transformer.getNomalSurfaces(volume, volume.getCenterMass(0, 0), 0, 0, 2.3, 6000);
		//ImagePlus polar = transformer.getNomalSurfaces(volume, volume.getCenterMass(0, 0), 0, 0, 1, 130);
		
		//transformer.sphere(volume1, volume1.getCenterMass(0, 0), 0, 0, 3.41);
		//IJ.log("center: " + volume.getCenterMass(0, 0).x + volume.getCenterMass(0, 0).y  + volume.getCenterMass(0, 0).z );
		//ImagePlus newPolar = transformer.getFollowNomalSurfacesMedian(volume, volume.getCenterMass(0, 0), 0, 0, 3.41, 200000);
		//ImagePlus polar = transformer.fastToPolar(volume1, new Point3D(225,225,200), 3.41, true);

		//polar.show();
		//ImagePlus newPolar1 = transformer.getNomalSurfacesFromInt(volume, volume.getCenterMass(0, 0), 0, 0, 1, 130);
			//ImagePlus polar = transformer.fastToPolar(volume, new Point3D(225,225,200), 1, true);
			//polar.getProcessor().resetMinAndMax();
		//polar.show();
		//newPolar.show();
		//newPolar1.show();
		
		/*PolarTransformer transformer = new PolarTransformer();
		//Volume volume = new Volume(in);
		ImagePlus median = transformer.median(in);
		median.show();*/
		//Volume med = new Volume(median);
		
		//transformer.getNomalSurfacesFromExt(med, volume.getCenterMass(0, 0), 0, 0, 2.5, 400).show();
		
		
	}


}
