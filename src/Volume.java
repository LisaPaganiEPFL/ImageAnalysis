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
		nt = imp.getNFrames();
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
							image[x + nx * (y + ny * ((z - 1) + nz * ((t - 1) + (c - 1) * nt)))] = v;
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

							ip.putPixelValue(x, y, image[x + nx * (y + ny * ((z - 1) + nz * ((t - 1) + (c - 1) * nt)))]);

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

					sum += image[x + nx * (y + ny * (z + nz * (t + c * nt)))];
					sumX += x * image[x + nx * (y + ny * (z + nz * (t + c * nt)))];
					sumY += y * image[x + nx * (y + ny * (z + nz * (t + c * nt)))];
					sumZ += z * image[x + nx * (y + ny * (z + nz * (t + c * nt)))];

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

					sum += image[x + nx * (y + ny * (z + nz * (t + c * nt)))];
					sumR += r * image[x + nx * (y + ny * (z + nz * (t + c * nt)))];

				}
			}

		}

		double value = sumR / sum;

		return value;

	}

	public double getPixel(int x, int y, int z, int t, int c) {

		int idx = x + y * nx + z * nx * ny + t * nx * ny * nz + c * nx * ny * nz * nt;

		//if (x >= nx || y >= ny || z >= nz || t >= nt || c >= nc || x < 0 || y < 0 || z < 0 || t < 0 || c < 0) {
			//return 0;
		//} else {
		return image[idx];
		//}
	}

	public double getInterpolatedPixel(double x, double y, double z, int t, int c) {

		int xFloor = (int) Math.floor(x);
		int yFloor = (int) Math.floor(y);
		int zFloor = (int) Math.floor(z);
		
		if ((xFloor+1) >= nx || (yFloor+1) >= ny || (zFloor+1) >= nz || t >= nt || c >= nc || xFloor < 0 || yFloor < 0 || zFloor < 0 || t < 0 || c < 0) {
			return 0;
		}

		double xD = x - xFloor;
		double yD = y - yFloor;
		double zD = z - zFloor;

		double c000 = getPixel(xFloor, yFloor, zFloor, t, c);
		double c100 = getPixel(xFloor + 1, yFloor, zFloor, t, c);
		double c010 = getPixel(xFloor, yFloor + 1, zFloor, t, c);
		double c110 = getPixel(xFloor + 1, yFloor + 1, zFloor, t, c);

		double c001 = getPixel(xFloor, yFloor, zFloor + 1, t, c);
		double c101 = getPixel(xFloor + 1, yFloor, zFloor + 1, t, c);
		double c011 = getPixel(xFloor, yFloor + 1, zFloor + 1, t, c);
		double c111 = getPixel(xFloor + 1, yFloor + 1, zFloor + 1, t, c);

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

	public double[] getDerivatives(Point3D centerMass, int r, double alpha, double beta, int t, int c, double step) {

		double xx = r * Math.cos(alpha) * Math.sin(beta);
		double yy = r * Math.sin(alpha) * Math.sin(beta);
		double zz = r * Math.cos(beta) / step;

		// Add target centre
		int x = (int) (xx + centerMass.x);
		int y = (int) (yy + centerMass.y);
		int z = (int) (zz + centerMass.z);

		double dx = getPixel(x + 1, y, z, t, c) - getPixel(x - 1, y, z, t, c);
		double dy = getPixel(x, y + 1, z, t, c) - getPixel(x, y - 1, z, t, c);
		double dz = getPixel(x, y, z + 1, t, c) - getPixel(x, y, z - 1, t, c);

		double[] derivative = new double[3];

		derivative[0] = dx;
		derivative[1] = dy;
		derivative[2] = dz;

		//if (alpha == 0 && beta == Math.PI) {
		//IJ.log("x" + x + "y" + y + "z" + z + "dx" + dx + "dy" + dy + "dz" + dz);
		//}

		return derivative;
	}

}
