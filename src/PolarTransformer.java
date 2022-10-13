import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class PolarTransformer implements PlugIn {

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub

		//ImagePlus impCart = IJ.getImage();
		//ImagePlus impPolar = toPolar(impCart);
		//impPolar.show();

	}
	
	public ImagePlus fastToPolar(Volume volume, Point3D targetCenter, double step) {

		// Set up the Polar Grid:
		// Use x values for alpha
		// Use y values for beta
		// Use z values for radius

		// Need 360 degrees [0 to 360[
		int xTransform = 360;

		// Need 181 degrees [0,180]
		int yTransform = 181;

		// Distance from centre to the farthest corner

		int radiusInt = getRadius(volume, targetCenter, step);
		int zTransform = radiusInt + 5;
		
		int nt = volume.nt;

		// -- Create the new image
		ImagePlus impTransform = IJ.createHyperStack("synthetic volume", xTransform, yTransform, 1, zTransform, nt, 32);

		// Fill the Polar Grid
		IJ.showStatus("Calculating...");
		for (int t = 1; t <= nt; t++) {
			for (int r = 1; r <= zTransform; r++) {
	
				impTransform.setPositionWithoutUpdate(1, r + 1, t);
				ImageProcessor ipTransform = impTransform.getProcessor();
	
				for (int betaInt = 0; betaInt < yTransform; betaInt++) {
					for (int alphaInt = 0; alphaInt < xTransform; alphaInt++) {
	
						// For each polar pixel, need to convert it to Cartesian coordinates
						double alpha = (alphaInt / 360.0) * Math.PI * 2.0;
						double beta = (betaInt / 360.0) * Math.PI * 2.0;
	
						double newValue = getInterpolatedValue(volume, targetCenter, r, alpha, beta, step);
	
						ipTransform.putPixelValue(alphaInt, betaInt, newValue);
	
					}
	
				}
			}
		}
		return impTransform;
	}

	
	double getInterpolatedValue(Volume volume, Point3D targetCenter, double r, double alpha, double beta, double step) {
		
		double x = r * Math.cos(alpha) * Math.sin(beta);
		double y = r * Math.sin(alpha) * Math.sin(beta);
		double z = r * Math.cos(beta) / step;
		
		// Add target centre
		x = x + targetCenter.x;
		y = y + targetCenter.y;
		z = z + targetCenter.z;
		
		// Linear interpolation
		return volume.getInterpolatedPixel(x,y,z);
	}
	
	int getRadius(Volume volume,Point3D targetCenter, double step) {
		
		int nx = volume.nx / 2;
		int ny = volume.ny / 2;
		int nz = (int) (step * volume.nz / 2);
		double xDist = Math.abs(targetCenter.x - nx) + nx;
		double yDist = Math.abs(targetCenter.y - ny) + ny;
		double zDist = Math.abs(targetCenter.z * step - nz) + nz;
		double radius = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
		
		return (int) radius;
		
	}


}
