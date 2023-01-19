import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.RankFilters;
import ij.process.ImageProcessor;

// Open the image TelomereSequence.tif
//
// run("API 6 DialogAndImageProcessing", "gaussian=1.5 median=1.5");
//

public class Membrane_Development implements PlugIn {

	public void run(String arg) {
		ImagePlus imp = IJ.getImage().duplicate();

		GenericDialog dlg = new GenericDialog("Denoising");
		dlg.addNumericField("Gaussian", 1.5, 1);
		dlg.addNumericField("Median", 1.5, 1);
		dlg.showDialog();
		if (dlg.wasCanceled()) return;

		double sigma = dlg.getNextNumber();
		double radius = dlg.getNextNumber();

		String name = "Denoising-" + sigma + "-" + radius;
		GaussianBlur gf = new GaussianBlur();
		RankFilters rf = new RankFilters();
		int nt = imp.getNFrames();
		for (int t = 0; t < nt; t++) {
			imp.setPosition(1, 1, t + 1);
			ImageProcessor ip = imp.getProcessor();
			gf.blurGaussian(ip, sigma, sigma, 0.001);
			rf.rank(ip, radius, RankFilters.MEDIAN);
		}
		imp.setPosition(1, 1, 1);
		imp.show();
		imp.setTitle(name);
		IJ.save(imp, name + ".tif");
	}
}
