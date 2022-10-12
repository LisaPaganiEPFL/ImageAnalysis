import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class GenerateVolume implements PlugIn {

	public static void main(String arg[]) {
		
		new ImageJ();
		GenerateVolume generator = new GenerateVolume();
		ImagePlus zSphere = generator.zSphere(101, 30, 2);
		zSphere.getProcessor().resetMinAndMax();
		zSphere.show();

	}

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
		
	
		GenerateVolume generator = new GenerateVolume();
		ImagePlus fullCube = generator.fullCube(101);
		ImagePlus cube = generator.cube(100, 30);
		ImagePlus smoothCube = generator.smoothCube(100, 30);
		ImagePlus sphere = generator.sphere(100, 30);
		ImagePlus smoothSphere = generator.smoothSphere(100,30);
		fullCube.show();
		cube.show();
		smoothCube.show();
		sphere.show();
		smoothSphere.show();
		
		PolarTransformer transformer = new PolarTransformer();
		ImagePlus polar = transformer.toPolar(fullCube);
		polar.show();
	}

	public ImagePlus fullCube(int size) {

		int nc = 1;
		int nt = 1;
		
		int center = size / 2;

		ImagePlus impCube = IJ.createHyperStack("synthetic full cube", size, size, nc, size, nt, 32);

		for (int c = 1; c <= nc; c++) {
			for (int z = 1; z <= size; z++) {
				for (int t = 1; t <= nt; t++) {

					impCube.setPositionWithoutUpdate(c, z, t);
					ImageProcessor ipCube = impCube.getProcessor();

					for (int x = 0; x < size; x++) {
						for (int y = 0; y < size; y++) {
							
							double r = getRadius(x,y,z,center);

							ipCube.putPixelValue(x, y, r);

						}
					}
				}
			}
		}

		return impCube;
	
	}
	
	public ImagePlus cube(int size, double dist) {

		int nc = 1;
		int nt = 1;

		int center = size / 2;

		ImagePlus impCube = IJ.createHyperStack("synthetic cube", size, size, nc, size, nt, 32);

		for (int c = 1; c <= nc; c++) {
			for (int z = 1; z <= size; z++) {
				for (int t = 1; t <= nt; t++) {

					impCube.setPositionWithoutUpdate(c, z, t);
					ImageProcessor ipCube = impCube.getProcessor();

					for (int x = 0; x < size; x++) {
						for (int y = 0; y < size; y++) {
							
							if(Math.abs(center-x)<dist && Math.abs(center-y)<dist && Math.abs(center-z)<dist) {
								ipCube.putPixelValue(x, y, 1);
							}

						}
					}
				}
			}
		}

		return impCube;
	}

	public ImagePlus smoothCube(int size, double dist) {

		int nc = 1;
		int nt = 1;

		int center = size / 2;

		ImagePlus impSmoothCube = IJ.createHyperStack("synthetic smooth cube", size, size, nc, size, nt, 32);

		for (int c = 1; c <= nc; c++) {
			for (int z = 1; z <= size; z++) {
				for (int t = 1; t <= nt; t++) {

					impSmoothCube.setPositionWithoutUpdate(c, z, t);
					ImageProcessor ipSmoothCube = impSmoothCube.getProcessor();

					for (int x = 0; x < size; x++) {
						for (int y = 0; y < size; y++) {

							ipSmoothCube.putPixelValue(x, y, sigmoid3D(x, y, z, center, dist));

						}
					}
				}
			}
		}

		return impSmoothCube;
	}
	
	public ImagePlus sphere(int size, double radius) {

		int nc = 1;
		int nt = 1;

		int center = size / 2;

		ImagePlus impSphere = IJ.createHyperStack("synthetic sphere", size, size, nc, size, nt, 32);

		for (int c = 1; c <= nc; c++) {
			for (int z = 1; z <= size; z++) {
				for (int t = 1; t <= nt; t++) {

					impSphere.setPositionWithoutUpdate(c, z, t);
					ImageProcessor ipSphere = impSphere.getProcessor();

					for (int x = 0; x < size; x++) {
						for (int y = 0; y < size; y++) {
							
							double r = getRadius(x,y,z,center);
							if(r<=radius) {
								ipSphere.putPixelValue(x, y, 1);
							}
							

						}
					}
				}
			}
		}

		return impSphere;
	}
	
	public ImagePlus zSphere(int size, double radius, double step) {

		int nc = 1;
		int nt = 1;

		int center = size / 2;
		
		int zDimension = (int) (size/step);
		
		if(step == 1) {
			zDimension = size;
		}

		ImagePlus impSphere = IJ.createHyperStack("synthetic sphere", size, size, nc, zDimension, nt, 32);

		for (int c = 1; c <= nc; c++) {
			for (int z = 1; z <= zDimension; z++) {
				for (int t = 1; t <= nt; t++) {

					impSphere.setPositionWithoutUpdate(c, z, t);
					ImageProcessor ipSphere = impSphere.getProcessor();

					for (int x = 0; x < size; x++) {
						for (int y = 0; y < size; y++) {
							
							double r = getRadius(x,y,(int)(z*step),center);
							if(r<=radius) {
								ipSphere.putPixelValue(x, y, 1);
							}
							

						}
					}
				}
			}
		}

		return impSphere;
	}

	public ImagePlus smoothSphere(int size, double radius) {

		int nc = 1;
		int nt = 1;

		int center = size / 2;

		ImagePlus impSmoothSphere = IJ.createHyperStack("synthetic smooth sphere", size, size, nc, size, nt, 32);

		for (int c = 1; c <= nc; c++) {
			for (int z = 1; z <= size; z++) {
				for (int t = 1; t <= nt; t++) {

					impSmoothSphere.setPositionWithoutUpdate(c, z, t);
					ImageProcessor ipSmoothSphere = impSmoothSphere.getProcessor();

					for (int x = 0; x < size; x++) {
						for (int y = 0; y < size; y++) {
							
							double r = getRadius(x,y,z,center);

							ipSmoothSphere.putPixelValue(x, y, sigmoid(r, radius));

						}
					}
				}
			}
		}

		return impSmoothSphere;
	}

	double sigmoid3D(int x, int y, int z, int center, double radius) {

		double sigX = sigmoidDouble(x, center, radius);
		double sigY = sigmoidDouble(y, center, radius);
		double sigZ = sigmoidDouble(z, center, radius);

		return sigX * sigY * sigZ;
	}

	double sigmoidDouble(int x, int center, double radius) {

		if (x == center) {
			return 1;
		} else {
			if (x < center) {
				return 1 / (1 + Math.exp(-(x - center + radius)));
			} else {
				return 1 / (1 + Math.exp(x - center - radius));
			}

		}

	}
	
	double sigmoid(double r, double radius) {
		
		return 1 / (1 + Math.exp(r - radius));
	}

	double getRadius(int x, int y, int d, int center) {
		
		int xx = x-center;
		int yy = y-center;
		int zz = d-center;
		
		return Math.sqrt(xx * xx + yy * yy + zz * zz);
	}

}
