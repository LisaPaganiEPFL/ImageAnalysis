import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;


public class Experiments implements PlugIn{
	
	public static void main(String arg[]) {
		
		new ImageJ();
		/*GenerateVolume generator = new GenerateVolume();
		ImagePlus volume = generator.zSphere(101, 30, 2);
		volume.show();
		PolarTransformer transformer = new PolarTransformer();
		ImagePlus polar = transformer.ztoPolar(volume, 2);
		polar.show();
		
		volume = generator.sphere(101,30);
		polar = transformer.toPolar(volume);
		polar.show();
		*/
		
		
		GenerateVolume generator = new GenerateVolume();
		ImagePlus volume = generator.fullCube(79);
		volume.show();
		PolarTransformer transformer = new PolarTransformer();
		ImagePlus polar = transformer.ztoPolar(volume,new Point3D(11,17,23), 1);
		polar.show();
		
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
		PolarTransformer transformer = new PolarTransformer();
		ImagePlus polar = transformer.ztoPolar(in,new Point3D(225,225,200), 1);
		//ImagePlus polar = transformer.toPolar(in);
		polar.show();
		
	}


}
