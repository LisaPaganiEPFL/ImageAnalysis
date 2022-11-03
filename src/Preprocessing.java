import ij.IJ;
import ij.ImagePlus;
import ij.plugin.GaussianBlur3D;
import ij.plugin.PlugIn;
import ij.process.Blitter;
import ij.process.ImageProcessor;

public class Preprocessing implements PlugIn{

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		
		ImagePlus in = IJ.getImage();
		
		ImagePlus hot = dog(in,5,3.41);
		
		hot.show();
		
	}
	
	public ImagePlus dog(ImagePlus in, double sigma, double step) {
		ImagePlus g1 = in.duplicate();
		ImagePlus g2 = in.duplicate();
		GaussianBlur3D.blur(g1, sigma, sigma, sigma/step);
		double newSigma = sigma * Math.sqrt(2);
		GaussianBlur3D.blur(g2, newSigma, newSigma, newSigma/step);
		ImageProcessor ip1 = g1.getProcessor();
		ImageProcessor ip2 = g2.getProcessor();
		ip1.copyBits(ip2, 0, 0, Blitter.SUBTRACT);
		g1.setTitle("DoG of " + in.getTitle());
		return g1;
	}

}
