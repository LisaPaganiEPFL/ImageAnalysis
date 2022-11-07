import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

public class PolarTransformer implements PlugIn {

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub

		// ImagePlus impCart = IJ.getImage();
		// ImagePlus impPolar = toPolar(impCart);
		// impPolar.show();

	}

	public ImagePlus fastToPolar(Volume volume, Point3D targetCenter, double step, boolean detect) {

		//RankFilter
		if (detect == true) {

			targetCenter = volume.getCenterMass(0,0);
			
		}
		// Set up the Polar Grid:
		// Use x values for alpha
		// Use y values for beta
		// Use z values for radius

		// Need 360 degrees [0 to 360[
		int xTransform = 360;

		// Need 181 degrees [0,180]
		int yTransform = 181;

		// Distance from centre to the farthest corner
		//double radiusMass = volume.getRadiusMass(targetCenter,0,0);
		int radiusInt = getRadius(volume, targetCenter, step);
		int zTransform = radiusInt + 5;
		//int zTransform = 30;

		//int shell = (int) (radiusMass-15);


		// -- Create the new image
		ImagePlus impTransform = IJ.createHyperStack("synthetic volume", xTransform, yTransform, volume.nc, zTransform, volume.nt, 32);

		// Fill the Polar Grid
		IJ.showStatus("Calculating...");
		for (int c = 1; c <= volume.nc; c++) {
			IJ.log("c=" + c);
			for (int t = 1; t <= volume.nt; t++) {
				IJ.log("t=" + t);
				for (int r = 1; r <= zTransform; r++) {
	
					impTransform.setPositionWithoutUpdate(c, r + 1 /*+shell*/, t);
					ImageProcessor ipTransform = impTransform.getProcessor();
	
					for (int betaInt = 0; betaInt < yTransform; betaInt++) {
						for (int alphaInt = 0; alphaInt < xTransform; alphaInt++) {
	
							// For each polar pixel, need to convert it to Cartesian coordinates
							double alpha = (alphaInt / 360.0) * Math.PI * 2.0;
							double beta = (betaInt / 360.0) * Math.PI * 2.0;
							
							/*if (detect == true) {
								targetCenter = volume.getCenterMass(t-1,c-1);
							}*/
	
							double newValue = getInterpolatedValue(volume, targetCenter, r/*+shell*/, alpha, beta, step, t-1, c-1);
	
							ipTransform.putPixelValue(alphaInt, betaInt, newValue);
	
						}
	
					}
				}
			}
		}
		
		return impTransform;
	}
	
	public void getNormal(Volume volume,Point3D targetCenter,int t, int c, double step, double threshold) {

		for (int betaInt = 0; betaInt < 181; betaInt++) {
			for (int alphaInt = 0; alphaInt < 360; alphaInt++) {
				boolean stop = false;
				double alpha = (alphaInt / 360.0) * Math.PI * 2.0;
				double beta = (betaInt / 360.0) * Math.PI * 2.0;
				for (int r = 1; r <= 100; r++) {
					if(stop == false) {
						double newValue = getInterpolatedValue(volume, targetCenter,r, alpha, beta, step, t, c);
						if(newValue <= threshold){
							stop = true;
							double[] d = volume.getDerivatives(targetCenter, r, alpha, beta, t, c, step);
						}
					}
				}
				
			}
		}
		
	}
	
	public ImagePlus median(ImagePlus in) {	
		
		ImagePlus inCopy = new ImagePlus();
		inCopy = in.duplicate();
		RankFilters rf = new RankFilters();
		
		for(int t=1; t <= inCopy.getNFrames(); t++) {
			
			for(int z=1; z <= inCopy.getNSlices(); z++) {
				inCopy.setPositionWithoutUpdate(1, z, t);
				ImageProcessor ip = inCopy.getProcessor();
				rf.rank(ip, 2, RankFilters.MEDIAN);
			}
		}
		
		
		return inCopy;
	}
	
	public ImagePlus getNomalSurfaces(Volume volume,Point3D targetCenter,int t, int c, double step, double threshold) {

		ImagePlus impTransform = IJ.createHyperStack("normal volume", 360, 181, 1, 5, 1, 32);
		
		for (int betaInt = 0; betaInt < 181; betaInt++) {
			double beta = (betaInt / 360.0) * Math.PI * 2.0;
			for (int alphaInt = 0; alphaInt < 360; alphaInt++) {
			
				double alpha = (alphaInt / 360.0) * Math.PI * 2.0;
				for (int r = 1; r <= 400; r++) {
					
						double newValue = getInterpolatedValue(volume, targetCenter,r, alpha, beta, step, t, c);
						//IJ.log("pixel"+newValue);
						if(newValue > threshold){
							//IJ.log("pixel"+newValue);
							
							for(int rPos = 0; rPos < 5; rPos++) {
							impTransform.setPositionWithoutUpdate(1, rPos+1, 1);
							ImageProcessor ipTransform = impTransform.getProcessor();
							ipTransform.putPixelValue(alphaInt, betaInt, newValue);
							newValue = getInterpolatedValue(volume, targetCenter,r + rPos + 1, alpha, beta, step, t, c);
							}
							break;
							//double[] d = volume.getDerivatives(targetCenter, r, alpha, beta, t, c, step);
						}
					
				}
				
			}
		}
		
		return impTransform;
		
	}
	
	public ImagePlus getFollowNomalSurfaces(Volume volume,Point3D targetCenter,int t, int c, double step, double threshold) {

		ImagePlus impTransform = IJ.createHyperStack("follow normal volume", 360, 181, 1, 5, 1, 32);
		
		ArrayList<double[]> arrListPoints = new ArrayList<double[]>();
		ArrayList<double[]> arrListNorms = new ArrayList<double[]>();
		
		for (int betaInt = 0; betaInt < 181; betaInt++) {
			double beta = (betaInt / 360.0) * Math.PI * 2.0;
			for (int alphaInt = 0; alphaInt < 360; alphaInt++) {
			
				double alpha = (alphaInt / 360.0) * Math.PI * 2.0;
				for (int r = 1; r <= 400; r++) {
					
						double newValue = getInterpolatedValue(volume, targetCenter,r, alpha, beta, step, t, c);
						//IJ.log("pixel"+newValue);
						if(newValue > threshold){
							//IJ.log("pixel"+newValue);
							double[] d = volume.getDerivatives(targetCenter, r, alpha, beta, t, c, step);
							
							double sum = Math.sqrt(d[0]*d[0] + d[1]*d[1] + d[2]*d[2]);
							
							double[] direction = new double[3];
							direction[0] = d[0]/sum;
							direction[1] = d[1]/sum;
							direction[2] = d[2]/sum;
							
							arrListNorms.add(direction);
							
							
							double x = targetCenter.x + r * Math.cos(alpha) * Math.sin(beta);
							double y = targetCenter.y + r * Math.sin(alpha) * Math.sin(beta);
							double z = targetCenter.z + r * Math.cos(beta);
							
							
							arrListPoints.add(new double[] {x,y,z});
							
							//if check space limitation
							
							for(int rPos = 0; rPos < 5; rPos++) { // rNormal
							double nValue = getInterpolatedValue(volume,x,y,z,direction,rPos, step, t, c);
							impTransform.setPositionWithoutUpdate(1, rPos+1, 1);
							ImageProcessor ipTransform = impTransform.getProcessor();
							ipTransform.putPixelValue(alphaInt, betaInt, nValue);
							
							}
							break;
							
						}
					
				}
				
			}
		}
		
		return impTransform;
		
	}
	
	
	public void saveCSV(String filename, ArrayList<double[]> points, ArrayList<double[]> norms) {
		
		if (filename == null) {
			return;
		}
		File file = new File(filename);
		//try {
		//BufferedWriter buffer = new BufferedWriter(new FileWriter(file));
		//int nrows = points.size();
		int ncols = 3;
			//String row = "";
​
			/*for (int r = 0; r < nrows; r++) {
				row = "";
				for (int c = 0; c < ncols; c++)
					row += points.get(r)[c] + (c == ncols - 1 ? "" : ", ");
				buffer.write(row + "\n");
			}

			for (int r = 0; r < nrows; r++) {
				row = "";
				for (int c = 0; c < ncols; c++)
					row += norms.get(r)[c] + (c == ncols - 1 ? "" : ", ");
				buffer.write(row + "\n");
			}

			buffer.close();
		//}
		//catch (IOException ex) {
		//}*/
		
	}
	
	public ImagePlus getNomalSurfacesFromExt(Volume volume,Point3D targetCenter,int t, int c, double step, double threshold) {

		ImagePlus impTransform = IJ.createHyperStack("normal volume", 360, 181, 1, 5, 1, 32);
		
		for (int betaInt = 0; betaInt < 181; betaInt++) {
			double beta = (betaInt / 360.0) * Math.PI * 2.0;
			
			for (int alphaInt = 0; alphaInt < 360; alphaInt++) {
				//boolean stop = false;
				double alpha = (alphaInt / 360.0) * Math.PI * 2.0;
				for (int r = 1; r <= 300; r++) {
				
					double newValue = getInterpolatedValue(volume, targetCenter,r, alpha, beta, step, t, c);
					if(newValue > threshold){
						//IJ.log("pixel"+newValue);

						for(int rPos = 0; rPos < 5; rPos++) {
						impTransform.setPositionWithoutUpdate(1, rPos+1, 1);
						ImageProcessor ipTransform = impTransform.getProcessor();
						ipTransform.putPixelValue(alphaInt, betaInt, newValue);
						newValue = getInterpolatedValue(volume, targetCenter,r + rPos + 1, alpha, beta, step, t, c);
						}
						break;
						}
					
				}
				
			}
		}
		
		return impTransform;
		
	}
	
	public ImagePlus getNomalSurfacesFromInt(Volume volume,Point3D targetCenter,int t, int c, double step, double threshold) {

		ImagePlus impTransform = IJ.createHyperStack("normal volume from int", 360, 181, 1, 5, 1, 32);
		
		for (int betaInt = 0; betaInt < 181; betaInt++) {
			double beta = (betaInt / 360.0) * Math.PI * 2.0;
			
			for (int alphaInt = 0; alphaInt < 360; alphaInt++) {
				//boolean stop = false;
				double alpha = (alphaInt / 360.0) * Math.PI * 2.0;
				double sum = 0;
				for (int r = 1; r <= 300; r++) {
				
					double newValue = getInterpolatedValue(volume, targetCenter,r, alpha, beta, step, t, c);
					sum = sum + newValue;
					if(Math.abs(sum/r - newValue) > threshold){

						for(int rPos = 0; rPos < 5; rPos++) {
						impTransform.setPositionWithoutUpdate(1, rPos+1, 1);
						ImageProcessor ipTransform = impTransform.getProcessor();
						ipTransform.putPixelValue(alphaInt, betaInt, newValue);
						newValue = getInterpolatedValue(volume, targetCenter,r + rPos + 1, alpha, beta, step, t, c);
						}
						break;
						}
					
				}
				
			}
		}
		
		return impTransform;
		
	}
	
	double getInterpolatedValue(Volume volume, double xx, double yy, double zz, double [] d, int delta, double step, int t, int c) {

		
		double x = xx + delta*d[0];
		double y = yy + delta*d[1];
		double z = zz + delta*d[2] / step;


		// Linear interpolation
		return volume.getInterpolatedPixel(x, y, z, t, c);
	}

	double getInterpolatedValue(Volume volume, Point3D targetCenter, double r, double alpha, double beta, double step, int t, int c) {

		double x = r * Math.cos(alpha) * Math.sin(beta);
		double y = r * Math.sin(alpha) * Math.sin(beta);
		double z = r * Math.cos(beta) / step;

		// Add target centre
		x = x + targetCenter.x;
		y = y + targetCenter.y;
		z = z + targetCenter.z;

		// Linear interpolation
		return volume.getInterpolatedPixel(x, y, z, t, c);
	}

	int getRadius(Volume volume, Point3D targetCenter, double step) {

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
