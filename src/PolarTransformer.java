import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class PolarTransformer implements PlugIn {

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub

		ImagePlus impCart = IJ.getImage();
		ImagePlus impPolar = toPolar(impCart);
		impPolar.show();

	}

	public ImagePlus ztoPolar(ImagePlus impCart, Point3D targetCenter, int step) {

		// Establish the default centre of Cartesian space
		// Point3D center = zgetPolarCenter(impCart, step);
		// Point3D falseCenter = getPolarCenter(impCart);

		// Set up the Polar Grid:
		// Use x values for alpha
		// Use y values for beta
		// Use z values for radius

		// Need 360 degrees [0 to 360[
		int xTransform = 360;

		// Need 181 degrees [0,180]
		int yTransform = 181;

		// Distance from centre to the farthest corner

		int nx = impCart.getWidth() / 2;
		int ny = impCart.getHeight() / 2;
		int nz = step * impCart.getNSlices() / 2;
		double xDist = Math.abs(targetCenter.x - nx) + nx;
		double yDist = Math.abs(targetCenter.y - ny) + ny;
		double zDist = Math.abs(targetCenter.z * step - nz) + nz;
		double radius = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
		int radiusInt = (int) radius;
		int zTransform = radiusInt + 5;
		
		int nt = impCart.getNFrames();

		// -- Create the new image
		ImagePlus impTransform = IJ.createHyperStack("synthetic volume", xTransform, yTransform, 1, zTransform, nt, 32);

		// Fill the Polar Grid
		IJ.showStatus("Calculating...");
		for (int t = 1; t <= nt; t++) {
			for (int r = 0; r < zTransform; r++) {
	
				impTransform.setPositionWithoutUpdate(1, r + 1, t);
				// impTransform.setPosition(1, r + 1, 1);
				ImageProcessor ipTransform = impTransform.getProcessor();
	
				for (int betaInt = 0; betaInt < yTransform; betaInt++) {
					for (int alphaInt = 0; alphaInt < xTransform; alphaInt++) {
	
						// For each polar pixel, need to convert it to Cartesian coordinates
						double alpha = (alphaInt / 360.0) * Math.PI * 2.0;
						double beta = (betaInt / 360.0) * Math.PI * 2.0;
	
						// Convert (r,alpha,beta) into Cartesian pixel coordinates
						// Not for tri-linear
						Point3D cart = zgetCartesian(r, alpha, beta, step);
	
						// Tri-linear
						//double x = r * Math.cos(alpha) * Math.sin(beta);
						//double y = r * Math.sin(alpha) * Math.sin(beta);
						//double z = r * Math.cos(beta) / step;
	
						// Sum the centre
						cart.add(targetCenter);
	
						// Tri-linear
						// x = x + targetCenter.x;
						// y = y + targetCenter.y;
						// z = z + targetCenter.z;
	
						impCart.setPositionWithoutUpdate(1, cart.z + 1, t);
						ImageProcessor ipInitial = impCart.getProcessor();
	
						double newValue = ipInitial.getPixelValue(cart.x, cart.y);
	
						// Tri-linear
						// double newValue = getTrilinearPixelValue(impCart,x, y,z);
	
						if (cart.z >= 0 && cart.z < impCart.getNSlices()) {
							ipTransform.putPixelValue(alphaInt, betaInt, newValue);
						}
	
					}
	
				}
			}
		}
		return impTransform;
	}

	public ImagePlus toPolar(ImagePlus impCart) {

		// Establish the default centre of Cartesian space
		Point3D center = getPolarCenter(impCart);

		// Set up the Polar Grid:
		// Use x values for alpha
		// Use y values for beta
		// Use z values for radius

		// Need 360 degrees [0 to 360[
		int xTransform = 360;

		// Need 181 degrees [0,180]
		int yTransform = 181;

		// Distance from centre to origin
		// double radius = Math.sqrt(center.x * center.x + center.y * center.y +
		// center.z * center.z);
		double radius = center.getDistance(new Point3D(0, 0, 0));
		int radiusInt = (int) radius;
		int zTransform = radiusInt + 5;

		// -- Create the new image
		ImagePlus impTransform = IJ.createHyperStack("synthetic volume", xTransform, yTransform, 1, zTransform, 1, 32);

		// Fill the Polar Grid
		IJ.showStatus("Calculating...");
		for (int r = 0; r < zTransform; r++) {

			impTransform.setPositionWithoutUpdate(1, r + 1, 1);
			// impTransform.setPosition(1, r + 1, 1);
			ImageProcessor ipTransform = impTransform.getProcessor();

			for (int betaInt = 0; betaInt < yTransform; betaInt++) {
				for (int alphaInt = 0; alphaInt < xTransform; alphaInt++) {

					// For each polar pixel, need to convert it to Cartesian coordinates
					double alpha = (alphaInt / 360.0) * Math.PI * 2.0;
					double beta = (betaInt / 360.0) * Math.PI * 2.0;

					// Convert (r,alpha,beta) into Cartesian pixel coordinates
					Point3D cart = getCartesian(r, alpha, beta);
					// Sum the centre
					cart.add(center);

					impCart.setPositionWithoutUpdate(1, cart.z + 1, 1);
					ImageProcessor ipInitial = impCart.getProcessor();

					double newValue = ipInitial.getPixelValue(cart.x, cart.y);

					if (cart.z >= 0 && cart.z < impCart.getNSlices()) {
						ipTransform.putPixelValue(alphaInt, betaInt, newValue);
					}

				}

			}
		}
		return impTransform;
	}

	double getTrilinearPixelValue(ImagePlus impCart, double x, double y, double z) {

		int xFloor = (int) Math.floor(x);
		int yFloor = (int) Math.floor(y);
		int zFloor = (int) Math.floor(z);

		int xCeil = (int) Math.ceil(x);
		int yCeil = (int) Math.ceil(y);
		int zCeil = (int) Math.ceil(z);

		double xD = x - xFloor;
		double yD = y - yFloor;
		double zD = z - zFloor;

		impCart.setPositionWithoutUpdate(1, zFloor + 1, 1);
		ImageProcessor ipInitial = impCart.getProcessor();

		double c000 = ipInitial.getPixelValue(xFloor, yFloor);
		double c100 = ipInitial.getPixelValue(xCeil, yFloor);
		double c010 = ipInitial.getPixelValue(xFloor, yCeil);
		double c110 = ipInitial.getPixelValue(xCeil, yCeil);

		impCart.setPositionWithoutUpdate(1, zCeil + 1, 1);
		ipInitial = impCart.getProcessor();

		double c001 = ipInitial.getPixelValue(xFloor, yFloor);
		double c101 = ipInitial.getPixelValue(xCeil, yFloor);
		double c011 = ipInitial.getPixelValue(xFloor, yCeil);
		double c111 = ipInitial.getPixelValue(xCeil, yCeil);

		// Interpolate along x axis
		double c00 = c000 * (1 - xD) + c100 * xD;
		double c01 = c001 * (1 - xD) + c101 * xD;
		double c10 = c010 * (1 - xD) + c110 * xD;
		double c11 = c011 * (1 - xD) + c111 * xD;

		// Interpolate along y axis
		double c0 = c00 * (1 - yD) + c10 * yD;
		double c1 = c01 * (1 - yD) + c11 * yD;

		// Interpolate along z axis

		double c = c0 * (1 - zD) + c1 * zD;

		return c;
	}

	Point3D zgetPolarCenter(ImagePlus impCart, int step) {
		// Get image dimensions
		int nx = impCart.getWidth();
		int ny = impCart.getHeight();
		int nz = impCart.getNSlices();

		// Determine the centre coordinates
		int centerX = nx / 2;
		int centerY = ny / 2;
		int centerZ = nz * step / 2;

		// Return the centre as Point3D
		return new Point3D(centerX, centerY, centerZ);
	}

	Point3D getPolarCenter(ImagePlus impCart) {
		// Get image dimensions
		int nx = impCart.getWidth();
		int ny = impCart.getHeight();
		int nz = impCart.getNSlices();

		// Determine the centre coordinates
		int centerX = nx / 2;
		int centerY = ny / 2;
		int centerZ = nz / 2;

		// Return the centre as Point3D
		return new Point3D(centerX, centerY, centerZ);
	}

	Point3D zgetCartesian(double r, double alpha, double beta, int step) {

		double x = r * Math.cos(alpha) * Math.sin(beta);
		double y = r * Math.sin(alpha) * Math.sin(beta);
		double z = r * Math.cos(beta) / step;

		// Nearest neighbour approximation
		int xInt = (int) Math.round(x);
		int yInt = (int) Math.round(y);
		int zInt = (int) Math.round(z);

		Point3D cartesian = new Point3D(xInt, yInt, zInt);

		return cartesian;

	}

	Point3D getCartesian(double r, double alpha, double beta) {

		double x = r * Math.cos(alpha) * Math.sin(beta);
		double y = r * Math.sin(alpha) * Math.sin(beta);
		double z = r * Math.cos(beta);

		// Nearest neighbour approximation
		int xInt = (int) Math.round(x);
		int yInt = (int) Math.round(y);
		int zInt = (int) Math.round(z);

		Point3D cartesian = new Point3D(xInt, yInt, zInt);

		return cartesian;

	}

}
