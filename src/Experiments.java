import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;


public class Experiments implements PlugIn{
	
	public static void main(String arg[]) {
		
		new ImageJ();

		
		// Non centred target
		
		GenerateVolume generator = new GenerateVolume();
		ImagePlus impVolume = generator.nonCentredSphere(101, 10);
		//ImagePlus impVolume = generator.sphere(101, 20);
		impVolume.show();
		VolumeA volume = new VolumeA(impVolume);
		PolarTransformerA transformer = new PolarTransformerA();
		ImagePlus polar = transformer.fastToPolar(volume,new Point3D(40,40,40), 1, true);
		polar.show();
		
		
		// Shell centred
		
		/*GenerateVolume generator = new GenerateVolume();
		ImagePlus impVolume = generator.sphere(101, 20);
		//ImagePlus impVolume = generator.sphere(101, 20);
		impVolume.show();
		VolumeA volume = new VolumeA(impVolume);
		PolarTransformerA transformer = new PolarTransformerA();
		ImagePlus polar = transformer.fastToPolar(volume,new Point3D(40,40,40), 1, true);
		polar.show();*/
		
		
	}

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		
		//new ImageJ();
		//GenerateVolume generator = new GenerateVolume();
		//ImagePlus fullCube = generator.fullCube(15);
		//fullCube.show();
		
		ImagePlus in = IJ.getImage();
		
		// Brain test
		//PolarTransformer transformer = new PolarTransformer();
		//ImagePlus polar = transformer.ztoPolar(in,new Point3D(225,225,200), 1);
		//ImagePlus polar = transformer.toPolar(in);
		//polar.show();
		
		/*PolarTransformer transformer = new PolarTransformer();
		Volume volume = new Volume(in);
		ImagePlus polar = transformer.fastToPolar(volume, new Point3D(225,225,200), 1, true);
		//polar.getProcessor().resetMinAndMax();
		polar.show();*/
		
		PolarTransformerA transformer = new PolarTransformerA();
		VolumeA volume = new VolumeA(in);
		ImagePlus polar = transformer.fastToPolar(volume, new Point3D(225,225,200), 1, true);
		polar.getProcessor().resetMinAndMax();
		polar.show();
		
	}


}
