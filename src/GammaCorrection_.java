import bva.util.ImageJUtility;
import bva.util.ImageTransformationFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class GammaCorrection_ implements PlugInFilter {

  private ImagePlus imagePlus = null;

  public int setup(String arg, ImagePlus imp) {
    if (arg.equals("about")) {
      showAbout();
      return DONE;
    }

    imagePlus = imp;
    return DOES_8G + DOES_STACKS + SUPPORTS_MASKING;
  } //setup


  public void run(ImageProcessor ip) {
    byte[] pixels = (byte[]) ip.getPixels();
    int width = ip.getWidth();
    int height = ip.getHeight();
    int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);

    double gammaVal = 1;
    double gammaMinVal = 0.2;
    double gammaMaxVal = 8.0;

    GenericDialog gd = new GenericDialog("Gamma Correction Input");
    gd.addSlider("gamma value", gammaMinVal, gammaMaxVal, gammaVal, 0.1);
    gd.addDialogListener((dialog, event) -> {
      int[] tfArray = ImageTransformationFilter.GetGammaCorrTF(255, dialog.getNextNumber());
      int[][] resultImg = ImageTransformationFilter.GetTransformedImage(inDataArrInt, width, height, tfArray);
      byte[] correctedPixels = ImageJUtility.convertFrom2DIntArr(resultImg, width, height);

      ip.reset();
      ip.setPixels(correctedPixels);
      imagePlus.setProcessor(ip);
      dialog.repaint();

      return true;
    });
    gd.showDialog();
    if (gd.wasCanceled()) {
      return;
    } //if

    gammaVal = gd.getNextNumber();
    int[] tfArray = ImageTransformationFilter.GetGammaCorrTF(255, gammaVal);
    int[][] resultImg = ImageTransformationFilter.GetTransformedImage(inDataArrInt, width, height, tfArray);

    ImageJUtility.showNewImage(resultImg, width, height, "Gamma Corrected Image (gamma=" + gammaVal + ")");
    ImageJUtility.createAndShowPlot("ReturnTF", "", "", tfArray);
  } //run


  void showAbout() {
    IJ.showMessage("About Gamma Correction...",
        "Allows live edit of image gamma values\n");
  } //showAbout

} //class GammaCorrection_