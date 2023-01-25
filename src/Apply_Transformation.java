import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Apply_Transformation implements PlugIn{

	
	public void run(String arg) {
		
		ImagePlus imp = IJ.getImage().duplicate();

		GenericDialog dlg = new GenericDialog("Apply Transformation");
		dlg.addNumericField("Anisotropic factor", 3.41, 2);
		dlg.addNumericField("Internal thickness", 5, 0);
		dlg.addNumericField("External thickness", 5, 0);
		dlg.showDialog();
		if (dlg.wasCanceled()) return;

		double a = dlg.getNextNumber();
		int it = (int) dlg.getNextNumber();
		int et = (int) dlg.getNextNumber();
		
		PolarTransformer transformer = new PolarTransformer();
		
		Volume volume = new Volume(imp);
		
		transformer.arrListNorms = transformer.loadCSV("normMedianGaussianSmooth6.cvs",3);
		transformer.arrListPoints = transformer.loadCSV("pointMedianGaussian6.cvs",3);
		transformer.arrListAngles = transformer.loadCSV("angleMedianGaussian6.cvs",2);
		
		
		ImagePlus color = transformer.getNomalSurfacesFromLoadMulti(volume, a, it, et);
		color.show();
		
		IJ.showMessage("Transformaion completed");
		
	}

}
