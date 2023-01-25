import java.io.File;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageStatistics;

public class Detect_Membrane implements PlugIn{
	
public void run(String arg) {
		
		ImagePlus imp = IJ.getImage().duplicate();
		
		Volume volume = new Volume(imp);
		Point3D center = volume.getCenterMass(0, 0);
		
		ImageStatistics stats = imp.getStatistics();
		
		double t = (stats.max - stats.min)/2.;
		
		
		GenericDialog dlg = new GenericDialog("Detect Membrane");
		//String[] CenterList = {"Center of mass", "Center of image", "User-defined center"};
		//dlg.addChoice("Choose center mode:", CenterList, CenterList[0]);
		dlg.addNumericField("x center", center.x, 0);
		dlg.addNumericField("y center", center.y, 0);
		dlg.addNumericField("z center", center.z, 0);
		dlg.addNumericField("threshold", t, 1);
		dlg.addNumericField("anisotropic factor z/xy", 3.41, 2);
		dlg.showDialog();
		if (dlg.wasCanceled()) return;

		//String centerMode = dlg.getNextChoice();
		int xc = (int) dlg.getNextNumber();
		int yc = (int) dlg.getNextNumber();
		int zc = (int) dlg.getNextNumber();
		t = dlg.getNextNumber();
		double a = dlg.getNextNumber();
		
		
		
		//String currentDir = System.getProperty("user.home");
		//String path = currentDir + File.separator + "Desktop" + File.separator + imp.getTitle();
		//new File(path).mkdir();
		
		PolarTransformer transformer = new PolarTransformer();
		ImagePlus newPolar = transformer.getFollowNomalSurfacesMedian(volume, new Point3D(xc,yc,zc), 0, 0, a, t);
		
		//IJ.show 
	}

}
