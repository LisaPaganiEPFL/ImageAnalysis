import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.GaussianBlur3D;
import ij.plugin.PlugIn;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

public class PolarTransformer implements PlugIn {
	
	public ArrayList<double[]> arrListPoints = new ArrayList<double[]>();
	public ArrayList<double[]> arrListNorms = new ArrayList<double[]>();
	public ArrayList<double[]> arrListAngles = new ArrayList<double[]>();

	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub

		// ImagePlus impCart = IJ.getImage();
		// ImagePlus impPolar = toPolar(impCart);
		// impPolar.show();

	}

	public ImagePlus fastToPolar(Volume volume, Point3D targetCenter, double step, boolean detect) {

		// RankFilter
		if (detect == true) {

			targetCenter = volume.getCenterMass(0, 0);

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
		// double radiusMass = volume.getRadiusMass(targetCenter,0,0);
		int radiusInt = getRadius(volume, targetCenter, step);
		int zTransform = radiusInt + 5;
		// int zTransform = 30;

		// int shell = (int) (radiusMass-15);

		// -- Create the new image
		ImagePlus impTransform = IJ.createHyperStack("synthetic volume", xTransform, yTransform, volume.nc, zTransform,
				volume.nt, 32);

		// Fill the Polar Grid
		// IJ.showStatus("Calculating...");
		for (int c = 1; c <= volume.nc; c++) {
			// IJ.log("c=" + c);
			for (int t = 1; t <= volume.nt; t++) {
				// IJ.log("t=" + t);
				for (int r = 1; r <= zTransform; r++) {

					impTransform.setPositionWithoutUpdate(c, r + 1 /* +shell */, t);
					ImageProcessor ipTransform = impTransform.getProcessor();

					for (int betaInt = 0; betaInt < yTransform; betaInt++) {
						for (int alphaInt = 0; alphaInt < xTransform; alphaInt++) {

							// For each polar pixel, need to convert it to Cartesian coordinates
							double alpha = (alphaInt / 360.0) * Math.PI * 2.0;
							double beta = (betaInt / 360.0) * Math.PI * 2.0;

							/*
							 * if (detect == true) { targetCenter = volume.getCenterMass(t-1,c-1); }
							 */

							double newValue = getInterpolatedValue(volume, targetCenter, r/* +shell */, alpha, beta,
									step, t - 1, c - 1);

							ipTransform.putPixelValue(alphaInt, betaInt, newValue);

						}

					}
				}
			}
		}

		return impTransform;
	}

	/*public void getNormal(Volume volume, Point3D targetCenter, int t, int c, double step, double threshold) {

		for (int betaInt = 0; betaInt < 181; betaInt++) {
			for (int alphaInt = 0; alphaInt < 360; alphaInt++) {
				boolean stop = false;
				double alpha = (alphaInt / 360.0) * Math.PI * 2.0;
				double beta = (betaInt / 360.0) * Math.PI * 2.0;
				for (int r = 1; r <= 100; r++) {
					if (stop == false) {
						double newValue = getInterpolatedValue(volume, targetCenter, r, alpha, beta, step, t, c);
						if (newValue <= threshold) {
							stop = true;
							double[] d = volume.getDerivatives(targetCenter, r, alpha, beta, t, c, step);
						}
					}
				}

			}
		}

	}*/
	
	public void sphere(Volume volume, Point3D targetCenter, int t, int c, double step) {

		ImagePlus impTransform = IJ.createHyperStack("normal volume", 360, 181, 1, 1, 1, 32);
		impTransform.setPositionWithoutUpdate(1, 1, 1);
		ImageProcessor ipTransform = impTransform.getProcessor();	
		int r = 12;
		
		for (int betaInt = 0; betaInt < 181; betaInt++) {
			for (int alphaInt = 0; alphaInt < 360; alphaInt++) {
				double alpha = (alphaInt / 360.0) * Math.PI * 2.0;
				double beta = (betaInt / 360.0) * Math.PI * 2.0;
				
				double newValue = getInterpolatedValue(volume, targetCenter, r, alpha, beta, step, t, c);
				
				ipTransform.putPixelValue(alphaInt, betaInt, newValue);
	
			}
		}
		
		impTransform.show();

	}

	public ImagePlus median(ImagePlus in) {

		ImagePlus inCopy = new ImagePlus();
		inCopy = in.duplicate();
		RankFilters rf = new RankFilters();

		for (int t = 1; t <= inCopy.getNFrames(); t++) {

			for (int z = 1; z <= inCopy.getNSlices(); z++) {
				inCopy.setPositionWithoutUpdate(1, z, t);
				ImageProcessor ip = inCopy.getProcessor();
				rf.rank(ip, 3, RankFilters.MEDIAN);
			}
		}

		return inCopy;
	}

	public ImagePlus getNomalSurfaces(Volume volume, Point3D targetCenter, int t, int c, double step,
			double threshold) {

		ImagePlus impTransform = IJ.createHyperStack("normal volume", 360, 181, 1, 5, 1, 32);

		for (int betaInt = 0; betaInt < 181; betaInt++) {
			double beta = (betaInt / 360.0) * Math.PI * 2.0;
			for (int alphaInt = 0; alphaInt < 360; alphaInt++) {

				double alpha = (alphaInt / 360.0) * Math.PI * 2.0;
				for (int r = 1; r <= 400; r++) {

					double newValue = getInterpolatedValue(volume, targetCenter, r, alpha, beta, step, t, c);
					// IJ.log("pixel"+newValue);
					if (newValue > threshold) {
						// IJ.log("pixel"+newValue);

						for (int rPos = 0; rPos < 5; rPos++) {
							impTransform.setPositionWithoutUpdate(1, rPos + 1, 1);
							ImageProcessor ipTransform = impTransform.getProcessor();
							ipTransform.putPixelValue(alphaInt, betaInt, newValue);
							newValue = getInterpolatedValue(volume, targetCenter, r + rPos + 1, alpha, beta, step, t,
									c);
						}
						break;
						// double[] d = volume.getDerivatives(targetCenter, r, alpha, beta, t, c, step);
					}

				}

			}
		}

		return impTransform;

	}

	public ImagePlus getFollowNomalSurfaces(Volume volume, Point3D targetCenter, int t, int c, double step,
			double threshold) {

		ImagePlus impTransform = IJ.createHyperStack("follow normal volume", 360, 181, 1, 5, 1, 32);

		//ArrayList<double[]> arrListPoints = new ArrayList<double[]>();
		//ArrayList<double[]> arrListNorms = new ArrayList<double[]>();

		for (int betaInt = 0; betaInt < 181; betaInt++) {
			double beta = (betaInt / 180.0) * Math.PI;
			for (int alphaInt = 0; alphaInt < 360; alphaInt++) {

				double alpha = (alphaInt / 180.0) * Math.PI;
				for (int r = getRadius(volume, targetCenter, step) + 5; r >= 1; r--) {

					double newValue = getInterpolatedValue(volume, targetCenter, r, alpha, beta, step, t, c);
					// IJ.log("pixel"+newValue);
					if (newValue > threshold) {

						double x = targetCenter.x + r * Math.cos(alpha) * Math.sin(beta);
						double y = targetCenter.y + r * Math.sin(alpha) * Math.sin(beta);
						double z = targetCenter.z * step + r * Math.cos(beta);

						if (isInside(volume, x, y, z, step) == false) {
							break;
						}

						// IJ.log("pixel"+newValue);
						double[] d = volume.getDerivatives(targetCenter, r, alpha, beta, t, c, step);

						double sum = Math.sqrt(d[0] * d[0] + d[1] * d[1] + d[2] * d[2]);

						double[] direction = new double[3];// check that the sum is not zero!!!
						if (sum != 0) {
							direction[0] = d[0] / sum;
							direction[1] = d[1] / sum;
							direction[2] = d[2] / sum;
						} else {
							direction[0] = 0;
							direction[1] = 0;
							direction[2] = 0;
						}

						arrListNorms.add(direction);

						arrListPoints.add(new double[] { x, y, z });

						// if check space limitation

						for (int rPos = 0; rPos < 5; rPos++) { // rNormal
							 double nValue = getInterpolatedValue(volume, x, y, z, direction, rPos, step,
							 t, c);
							//double nValue = getInterpolatedValue(volume, targetCenter, r - rPos, alpha, beta, step, t,
									//c);
							impTransform.setPositionWithoutUpdate(1, rPos + 1, 1);
							ImageProcessor ipTransform = impTransform.getProcessor();
							ipTransform.putPixelValue(alphaInt, betaInt, nValue);

						}
						break;

					}

				}

			}
		}

		//logParaview(arrListPoints, arrListNorms);
		saveCSV("point.cvs", arrListPoints, 3);
		saveCSV("norm.cvs", arrListNorms, 3);
		

		// String currentDir = System.getProperty("user.dir");
		// IJ.log("Current dir using System:" + currentDir);
		return impTransform;

	}
	
	public ImagePlus getFollowNomalSurfaces1(Volume volume, Point3D targetCenter, int t, int c, double step,
			double threshold) {

		ImagePlus impTransform = IJ.createHyperStack("follow normal volume", 360, 181, 1, 5, 1, 32);
		ImagePlus impRadius = IJ.createHyperStack("follow normal volume", 360, 181, 1, 1, 1, 32);
		impRadius.setPositionWithoutUpdate(1, 1, 1);
		ImageProcessor ipRadius = impRadius.getProcessor();
		//ArrayList<double[]> arrListPoints = new ArrayList<double[]>();
		//ArrayList<double[]> arrListNorms = new ArrayList<double[]>();

		for (int betaInt = 0; betaInt < 181; betaInt++) {
			double beta = (betaInt / 180.0) * Math.PI;
			for (int alphaInt = 0; alphaInt < 360; alphaInt++) {

				double alpha = (alphaInt / 180.0) * Math.PI;
				for (int r = getRadius(volume, targetCenter, step) + 5; r >= 1; r--) {

					double newValue = getInterpolatedValue(volume, targetCenter, r, alpha, beta, step, t, c);
					// IJ.log("pixel"+newValue);
					if (newValue > threshold) {

						double x = targetCenter.x + r * Math.cos(alpha) * Math.sin(beta);
						double y = targetCenter.y + r * Math.sin(alpha) * Math.sin(beta);
						double z = targetCenter.z * step + r * Math.cos(beta);

						if (isInside(volume, x, y, z, step) == false) {
							break;
						}

						// IJ.log("pixel"+newValue);
						double[] d = volume.getDerivatives(targetCenter, r, alpha, beta, t, c, step);

						double sum = Math.sqrt(d[0] * d[0] + d[1] * d[1] + d[2] * d[2]);

						double[] direction = new double[3];// check that the sum is not zero!!!
						if (sum != 0) {
							direction[0] = d[0] / sum;
							direction[1] = d[1] / sum;
							direction[2] = d[2] / sum;
						} else {
							direction[0] = 0;
							direction[1] = 0;
							direction[2] = 0;
						}

						arrListNorms.add(direction);

						arrListPoints.add(new double[] { x, y, z });

						// if check space limitation
						
						ipRadius.putPixelValue(alphaInt, betaInt, r);
						
						for (int rPos = 0; rPos < 5; rPos++) { // rNormal
							 double nValue = getInterpolatedValue(volume, x, y, z, direction, rPos, step,
							 t, c);
							//double nValue = getInterpolatedValue(volume, targetCenter, r - rPos, alpha, beta, step, t,
									//c);
							impTransform.setPositionWithoutUpdate(1, rPos + 1, 1);
							ImageProcessor ipTransform = impTransform.getProcessor();
							ipTransform.putPixelValue(alphaInt, betaInt, nValue);

						}
						break;

					}

				}

			}
		}

		//logParaview(arrListPoints, arrListNorms);
		//saveCSV("point.cvs", arrListPoints);
		//saveCSV("norm.cvs", arrListNorms);

		// String currentDir = System.getProperty("user.dir");
		// IJ.log("Current dir using System:" + currentDir);
		impRadius.show();
		return impTransform;
		//return impRadius;

	}
	
	public ImagePlus getFollowNomalSurfacesMedian(Volume volume, Point3D targetCenter, int t, int c, double step,
			double threshold) {

		//ImagePlus impTransform = IJ.createHyperStack("follow normal volume", 360, 181, 1, 5, 1, 32);
		ImagePlus impRadius = IJ.createHyperStack("radius", 360, 181, 1, 1, 1, 32);
		ImagePlus impX = IJ.createHyperStack("radius", 360, 181, 1, 1, 1, 32);
		ImagePlus impY = IJ.createHyperStack("radius", 360, 181, 1, 1, 1, 32);
		ImagePlus impZ = IJ.createHyperStack("radius", 360, 181, 1, 1, 1, 32);
		impRadius.setPositionWithoutUpdate(1, 1, 1);
		ImageProcessor ipRadius = impRadius.getProcessor();
		impX.setPositionWithoutUpdate(1, 1, 1);
		ImageProcessor ipX = impX.getProcessor();
		impY.setPositionWithoutUpdate(1, 1, 1);
		ImageProcessor ipY = impY.getProcessor();
		impRadius.setPositionWithoutUpdate(1, 1, 1);
		ImageProcessor ipZ = impZ.getProcessor();


		for (int betaInt = 0; betaInt < 181; betaInt++) {
			double beta = (betaInt / 180.0) * Math.PI;
			for (int alphaInt = 0; alphaInt < 360; alphaInt++) {

				double alpha = (alphaInt / 180.0) * Math.PI;
				for (int r = getRadius(volume, targetCenter, step) + 5; r >= 2; r--) {

					double newValue = getInterpolatedValue(volume, targetCenter, r, alpha, beta, step, t, c);
					// IJ.log("pixel"+newValue);
					if (newValue > threshold) {

						//double x = targetCenter.x + r * Math.cos(alpha) * Math.sin(beta);
						//double y = targetCenter.y + r * Math.sin(alpha) * Math.sin(beta);
						//double z = targetCenter.z * step + r * Math.cos(beta);

						//if (isInside(volume, x, y, z, step) == false) {
							//break;
						//}

						// IJ.log("pixel"+newValue);
						/*double[] d = volume.getDerivatives(targetCenter, r, alpha, beta, t, c, step);

						double sum = Math.sqrt(d[0] * d[0] + d[1] * d[1] + d[2] * d[2]);

						double[] direction = new double[3];// check that the sum is not zero!!!
						if (sum != 0) {
							direction[0] = d[0] / sum;
							direction[1] = d[1] / sum;
							direction[2] = d[2] / sum;
						} else {
							direction[0] = 0;
							direction[1] = 0;
							direction[2] = 0;
						}*/

						//arrListNorms.add(direction);

						//arrListPoints.add(new double[] { x, y, z });
						
						arrListAngles.add(new double[] {betaInt, alphaInt});

						// if check space limitation
						ipRadius.putPixelValue(alphaInt, betaInt, r);
						
						/*for (int rPos = 0; rPos < 5; rPos++) { // rNormal
							 //double nValue = getInterpolatedValue(volume, x, y, z, direction, rPos, step,
							 //t, c);
							double nValue = getInterpolatedValue(volume, targetCenter, r - rPos, alpha, beta, step, t,
									c);
							impTransform.setPositionWithoutUpdate(1, rPos + 1, 1);
							ImageProcessor ipTransform = impTransform.getProcessor();
							ipTransform.putPixelValue(alphaInt, betaInt, nValue);

						}*/
						break;

					}

				}

			}
		}
		ImagePlus impMedianRadius = median(impRadius);
		//ImagePlus impMedianRadius = impRadius;
		GaussianBlur3D.blur(impMedianRadius, 3, 3, 0);
		impMedianRadius.setPositionWithoutUpdate(1, 1, 1);
		ImageProcessor ipMedianRadius = impMedianRadius.getProcessor();
		
		
		for (int betaInt = 0; betaInt < 181; betaInt++) {
			double beta = (betaInt / 180.0) * Math.PI;
			for (int alphaInt = 0; alphaInt < 360; alphaInt++) {

				double alpha = (alphaInt / 180.0) * Math.PI;
				double r = ipMedianRadius.getPixelValue(alphaInt, betaInt);
				//IJ.log("r: " + r);
				
				
						
				double x = targetCenter.x + r * Math.cos(alpha) * Math.sin(beta);
				double y = targetCenter.y + r * Math.sin(alpha) * Math.sin(beta);
				double z = targetCenter.z * step + r * Math.cos(beta);
				
				
				volume.setPixel((int) x, (int) y, (int) (z/step), t, c, 70000000);
				
				arrListPoints.add(new double[] { x, y, z });
				
				double[] d = volume.getDerivatives(targetCenter, r, alpha, beta, t, c, step);

				double sum = Math.sqrt(d[0] * d[0] + d[1] * d[1] + d[2] * d[2]);

				double[] direction = new double[3];// check that the sum is not zero!!!
				if (sum != 0) {
					direction[0] = d[0] / sum;
					direction[1] = d[1] / sum;
					direction[2] = d[2] / sum;
				} else {
					direction[0] = 0;
					direction[1] = 0;
					direction[2] = 0;
				}
				
				//arrListNorms.add(direction);
				ipX.putPixelValue(alphaInt, betaInt, direction[0]);
				ipY.putPixelValue(alphaInt, betaInt, direction[1]);
				ipZ.putPixelValue(alphaInt, betaInt, direction[2]);
				
					/*for (int rPos = 0; rPos < 5; rPos++) { // rNormal
					 //double nValue = getInterpolatedValue(volume, x, y, z, direction, rPos, step,
					 //t, c);
					double nValue = getInterpolatedValue(volume, targetCenter, r - rPos, alpha, beta, step, t, c);
					//IJ.log("value: " + nValue);
					impTransform.setPositionWithoutUpdate(1, rPos + 1, 1);
					ImageProcessor ipTransform = impTransform.getProcessor();
					ipTransform.putPixelValue(alphaInt, betaInt, nValue);
				}*/
				
			}
		}
		
		ImagePlus impMedianX = median(impX);
		//ImagePlus impMedianRadius = impRadius;
		GaussianBlur3D.blur(impMedianX, 3, 3, 0);
		impX.setPositionWithoutUpdate(1, 1, 1);
		ImageProcessor ipMedianX = impMedianX.getProcessor();
		//impMedianX.show();
		
		ImagePlus impMedianY = median(impY);
		//ImagePlus impMedianRadius = impRadius;
		GaussianBlur3D.blur(impMedianY, 3, 3, 0);
		impY.setPositionWithoutUpdate(1, 1, 1);
		ImageProcessor ipMedianY = impMedianY.getProcessor();
		//impMedianY.show();
		
		ImagePlus impMedianZ = median(impZ);
		//ImagePlus impMedianRadius = impRadius;
		GaussianBlur3D.blur(impMedianZ, 3, 3, 0);
		impX.setPositionWithoutUpdate(1, 1, 1);
		ImageProcessor ipMedianZ = impMedianZ.getProcessor();
		//impMedianZ.show();
		
		for (int betaInt = 0; betaInt < 181; betaInt++) {
			for (int alphaInt = 0; alphaInt < 360; alphaInt++) {
				
				arrListNorms.add(new double[] {ipX.getPixelValue(alphaInt, betaInt),ipY.getPixelValue(alphaInt, betaInt),ipZ.getPixelValue(alphaInt, betaInt) });
				
			}
		}
		

		//logParaview(arrListPoints, arrListNorms);
		saveCSV("pointMedianGaussian6.cvs", arrListPoints, 3);
		saveCSV("normMedianGaussianSmooth6.cvs", arrListNorms, 3);
		saveCSV("angleMedianGaussian6.cvs", arrListAngles, 2);

		// String currentDir = System.getProperty("user.dir");
		// IJ.log("Current dir using System:" + currentDir);
		impRadius.show();
		impMedianRadius.show();
		//return impTransform;
		return volume.getVolume();

	}
	
	public void logParaview(ArrayList<double[]> points, ArrayList<double[]> norms) {

		
		int nrows = points.size();
		int ncols = 3;
		String row = "";
		IJ.log("# vtk DataFile Version 2.0");
		IJ.log("Unstructured grid legacy vtk file with point scalar data");
		IJ.log("ASCII" + "\n");

		IJ.log("DATASET UNSTRUCTURED_GRID");
		IJ.log("POINTS " + nrows + " float");

		for (int r = 0; r < nrows; r++) {
			row = "    ";
			for (int c = 0; c < ncols; c++)
				row += points.get(r)[c] + " ";
				
			IJ.log(row);
		}
		IJ.log("");
		IJ.log("POINT_DATA " + nrows + "\n");
		IJ.log("VECTORS n float");

		for (int r = 0; r < nrows; r++) {
			row = "    ";
			for (int c = 0; c < ncols; c++)
				row += norms.get(r)[c] + " ";
				
			IJ.log(row);
			
		}

		return;
	}

	public void saveCSV(String filename, ArrayList<double[]> vector, int number) {

		if (filename == null) {
			return;
		}

		File file = new File(filename);
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file));
			int nrows = vector.size();
			int ncols = number;
			String row = "";

			for (int r = 0; r < nrows; r++) {
				row = "";
				for (int c = 0; c < ncols; c++)
					row += vector.get(r)[c] + ",";
				buffer.write(row + "\n");
			}

			buffer.flush();
			buffer.close();
		} catch (IOException ex) {
			IJ.log(ex.toString());
		}
		return;
	}

	public ArrayList<double[]> loadCSV(String filename, int number) {
		ArrayList<double[]> arrList = new ArrayList<double[]>(); 
		if (filename == null)
			return null;

		try {
			BufferedReader buffer = new BufferedReader(new FileReader(filename));
			//buffer.readLine();
			String line = buffer.readLine();
			//StringTokenizer tokens = new StringTokenizer(line, ",");
			//String headers[] = new String[tokens.countTokens()];
			//int count = 0;
			//while (tokens.hasMoreTokens()) {
				//headers[count++] = tokens.nextToken();
			//}
			while (line != null) {
				String tokens[] = line.split(",");
				
				if (tokens.length >= number) {
					double row[] = new double[number];
					//IJ.log("Token 0:" + tokens[0]);
					row[0] = Double.parseDouble(tokens[0]);
					row[1] = Double.parseDouble(tokens[1]);
					if(number == 3) {
						row[2] = Double.parseDouble(tokens[2]);
					}
					arrList.add(row);
					
				}
				
				line = buffer.readLine();
			}
			buffer.close();
			//readTable();
		} catch (Exception ex) {
			IJ.error("Unable to read the table from " + filename);
		}
		return arrList;
	}

	public ImagePlus getNomalSurfacesFromLoad(Volume volume, int t, int c, double step) {

		ImagePlus impTransform = IJ.createHyperStack("normal volume from load files", 360, 181, 1, 10, 1, 32);
		
		for (int pos = 0; pos < arrListPoints.size(); pos++) {
			double[] p = arrListPoints.get(pos);
			double[] n = arrListNorms.get(pos);
			
			//int alpha = pos % 360;
			//int beta = (int) Math.floor(pos/360);
			
			double[] angles = arrListAngles.get(pos);
			
			double x = p[0];
			double y = p[1];
			double z = p[2];
			int betaInt = (int) angles[0];
			int alphaInt = (int) angles[1];
			
			//Follow radius
			
			//n[0] = Math.cos(angles[1]) * Math.sin(angles[0]);
			//n[1] = Math.sin(angles[1]) * Math.sin(angles[0]);
			//n[2] = Math.cos(angles[0]);
			
			for (int i=-5; i < 5; i++) {
				double nValue = getInterpolatedValue(volume, x, y, z, n, i, step, t, c);
				impTransform.setPositionWithoutUpdate(1, i + 1 + 5, 1);
				ImageProcessor ipTransform = impTransform.getProcessor();
				ipTransform.putPixelValue(alphaInt, betaInt, nValue);
			}
		}

		return impTransform;

	}
	
	public ImagePlus getNomalSurfacesFromLoadMulti(Volume volume, double step) {

		ImagePlus impTransform = IJ.createHyperStack("normal volume from load files", 360, 181, volume.nc, 10, volume.nt, 32);
		ImagePlus impTransformMax = IJ.createHyperStack("normal volume from load files", 360, 181, volume.nc, 1, volume.nt, 32);
		
		for(int c = 1; c <= volume.nc; c++) {
			for(int t = 1; t <= volume.nt; t++) {
				impTransformMax.setPositionWithoutUpdate(c, 1, t);
				ImageProcessor ipTransformMax = impTransformMax.getProcessor();
				for (int pos = 0; pos < arrListPoints.size(); pos++) {
					double[] p = arrListPoints.get(pos);
					double[] n = arrListNorms.get(pos);
					
					double[] angles = arrListAngles.get(pos);
					
					double x = p[0];
					double y = p[1];
					double z = p[2];
					int betaInt = (int) angles[0];
					int alphaInt = (int) angles[1];
					
					double value = 0;
					
					for (int i=-5; i < 5; i++) {
						double nValue = getInterpolatedValue(volume, x, y, z, n, i, step, t-1, c-1);
						if(nValue > value) {
							value = nValue;
						}
						impTransform.setPositionWithoutUpdate(c, i + 1 + 5, t);
						ImageProcessor ipTransform = impTransform.getProcessor();
						ipTransform.putPixelValue(alphaInt, betaInt, nValue);
					}
					
					
					ipTransformMax.putPixelValue(alphaInt, betaInt, value);
					
					
				}
			}
		}
		
		impTransformMax.show();

		return impTransform;

	}

	public ImagePlus getNomalSurfacesFromInt(Volume volume, Point3D targetCenter, int t, int c, double step,
			double threshold) {

		ImagePlus impTransform = IJ.createHyperStack("normal volume from int", 360, 181, 1, 5, 1, 32);

		for (int betaInt = 0; betaInt < 181; betaInt++) {
			double beta = (betaInt / 360.0) * Math.PI * 2.0;

			for (int alphaInt = 0; alphaInt < 360; alphaInt++) {
				// boolean stop = false;
				double alpha = (alphaInt / 360.0) * Math.PI * 2.0;
				double sum = 0;
				for (int r = 1; r <= 300; r++) {

					double newValue = getInterpolatedValue(volume, targetCenter, r, alpha, beta, step, t, c);
					sum = sum + newValue;
					if (Math.abs(sum / r - newValue) > threshold) {

						for (int rPos = 0; rPos < 5; rPos++) {
							impTransform.setPositionWithoutUpdate(1, rPos + 1, 1);
							ImageProcessor ipTransform = impTransform.getProcessor();
							ipTransform.putPixelValue(alphaInt, betaInt, newValue);
							newValue = getInterpolatedValue(volume, targetCenter, r + rPos + 1, alpha, beta, step, t,
									c);
						}
						break;
					}

				}

			}
		}

		return impTransform;

	}

	double getInterpolatedValue(Volume volume, double xx, double yy, double zz, double[] d, int delta, double step,
			int t, int c) {

		double x = xx - delta * d[0];
		double y = yy - delta * d[1];
		double z = (zz - delta * d[2])/step; // divided by step??

		// Linear interpolation
		return volume.getInterpolatedPixel(x, y, z, t, c);
	}

	double getInterpolatedValue(Volume volume, Point3D targetCenter, double r, double alpha, double beta, double step,
			int t, int c) {

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
		// Return the radius in the real domain, not in the compress domain

		int nx = volume.nx / 2;
		int ny = volume.ny / 2;
		int nz = (int) (step * volume.nz / 2);
		double xDist = Math.abs(targetCenter.x - nx) + nx;
		double yDist = Math.abs(targetCenter.y - ny) + ny;
		double zDist = Math.abs(targetCenter.z * step - nz) + nz;
		double radius = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);

		return (int) radius;

	}

	boolean isInside(Volume volume, double x, double y, double z, double step) {

		int xx = (int) Math.floor(x);
		int yy = (int) Math.floor(y);
		int zz = (int) Math.floor(z / step);

		if (xx + 1 >= volume.nx || yy + 1 >= volume.ny || zz + 1 >= volume.nz || xx < 0 || yy < 0 || zz < 0) {

			return false;
		}

		return true;
	}

}
