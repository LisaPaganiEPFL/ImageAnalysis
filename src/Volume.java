import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Volume implements PlugIn {

	public double[] image;
	public int nx, ny, nz, nt, nc;

	public static void main(String arg[]) {

		new ImageJ();

	}

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub

	}

	public Volume(ImagePlus imp) {

		nx = imp.getWidth();
		ny = imp.getHeight();
		nz = imp.getNSlices();
		nt = imp.getFrame();
		nc = imp.getNChannels();

		image = new double[nx * ny * nz * nt * nc];

		for (int c = 1; c <= nc; c++) {
			for (int t = 1; t <= nt; t++) {
				for (int z = 1; z <= nz; z++) {
					imp.setPositionWithoutUpdate(c, z, t);
					ImageProcessor ip = imp.getProcessor();
					for (int y = 0; y < ny; y++) {
						for (int x = 0; x < nx; x++) {
							double v = ip.getPixelValue(x, y);
							image[x + y * nx + (z - 1) * nx * ny + (t - 1) * nx * ny * nz
									+ (c - 1) * nx * ny * nz * nt] = v;
						}
					}
				}
			}
		}

	}

	public ImagePlus getVolume() {

		ImagePlus imp = IJ.createHyperStack("Converted volume", nx, ny, nc, nz, nt, 32);

		for (int c = 1; c <= nc; c++) {
			for (int t = 1; t <= nt; t++) {
				for (int z = 1; z <= nz; z++) {

					imp.setPositionWithoutUpdate(c, z, t);
					ImageProcessor ip = imp.getProcessor();

					for (int x = 0; x < nx; x++) {
						for (int y = 0; y < ny; y++) {

							ip.putPixelValue(x, y, image[x + y * nx + (z - 1) * nx * ny + (t - 1) * nx * ny * nz
									+ (c - 1) * nx * ny * nz * nt]);

						}
					}

				}
			}
		}

		return imp;
	}

	public Point3D getCenterMass(int t, int c) {

		double sum = 0;
		double sumX = 0;
		double sumY = 0;
		double sumZ = 0;

		for (int z = 0; z < nz; z++) {
			for (int y = 0; y < ny; y++) {
				for (int x = 0; x < nx; x++) {

					sum += image[x + y * nx + z * nx * ny + t * nx * ny * nz + c * nx * ny * nz * nt];
					sumX += x * image[x + y * nx + z * nx * ny + t * nx * ny * nz + c * nx * ny * nz * nt];
					sumY += y * image[x + y * nx + z * nx * ny + t * nx * ny * nz + c * nx * ny * nz * nt];
					sumZ += z * image[x + y * nx + z * nx * ny + t * nx * ny * nz + c * nx * ny * nz * nt];

				}
			}

		}

		int x = (int) (sumX / sum);
		int y = (int) (sumY / sum);
		int z = (int) (sumZ / sum);

		Point3D centerMass = new Point3D(x, y, z);

		return centerMass;
	}

	public double getRadiusMass(Point3D centerMass, int t, int c) {

		double sum = 0;
		double sumR = 0;

		for (int z = 0; z < nz; z++) {

			int zz = z - centerMass.z;

			for (int y = 0; y < ny; y++) {

				int yy = y - centerMass.y;

				for (int x = 0; x < nx; x++) {

					int xx = x - centerMass.x;

					double r = Math.sqrt(xx * xx + yy * yy + zz * zz);

					sum += image[x + y * nx + z * nx * ny + t * nx * ny * nz + c * nx * ny * nz * nt];
					sumR += r * image[x + y * nx + z * nx * ny + t * nx * ny * nz + c * nx * ny * nz * nt];

				}
			}

		}

		double value = sumR / sum;
		
		return value;

	}

	public double getPixel(int x, int y, int z, int t, int c) {

		int idx = x + y * nx + z * nx * ny + t * nx * ny * nz + c * nx * ny * nz * nt;

		if (x >= nx || y >= ny || z >= nz || t >= nt || c >= nc || x < 0 || y < 0 || z < 0 || t < 0 || c < 0) {
			return 0;
		} else {
			return image[idx];
		}
	}

	public double getInterpolatedPixel(double x, double y, double z, int t, int c) {

		int xFloor = (int) Math.floor(x);
		int yFloor = (int) Math.floor(y);
		int zFloor = (int) Math.floor(z);

		int xCeil = (int) Math.ceil(x);
		int yCeil = (int) Math.ceil(y);
		int zCeil = (int) Math.ceil(z);

		double xD = x - xFloor;
		double yD = y - yFloor;
		double zD = z - zFloor;

		double c000 = getPixel(xFloor, yFloor, zFloor,t,c);
		double c100 = getPixel(xCeil, yFloor, zFloor,t,c);
		double c010 = getPixel(xFloor, yCeil, zFloor,t,c);
		double c110 = getPixel(xCeil, yCeil, zFloor,t,c);

		double c001 = getPixel(xFloor, yFloor, zCeil,t,c);
		double c101 = getPixel(xCeil, yFloor, zCeil,t,c);
		double c011 = getPixel(xFloor, yCeil, zCeil,t,c);
		double c111 = getPixel(xCeil, yCeil, zCeil,t,c);

		// Interpolate along x axis
		double c00 = c000 * (1 - xD) + c100 * xD;
		double c01 = c001 * (1 - xD) + c101 * xD;
		double c10 = c010 * (1 - xD) + c110 * xD;
		double c11 = c011 * (1 - xD) + c111 * xD;

		// Interpolate along y axis
		double c0 = c00 * (1 - yD) + c10 * yD;
		double c1 = c01 * (1 - yD) + c11 * yD;

		// Interpolate along z axis
		double result = c0 * (1 - zD) + c1 * zD;

		return result;
	}

}
