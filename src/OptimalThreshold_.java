import bva.util.ImageJUtility;
import bva.util.ImageTransformationFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class OptimalThreshold_ implements PlugInFilter {

  public int setup(String arg, ImagePlus imp) {
    if (arg.equals("about")) {
      showAbout();
      return DONE;
    }
    return DOES_8G + DOES_STACKS + SUPPORTS_MASKING;
  } //setup

  public void run(ImageProcessor ip) {
    byte[] pixels = (byte[]) ip.getPixels();
    int width = ip.getWidth();
    int height = ip.getHeight();
    int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);

    GenericDialog gd = new GenericDialog("Optimal Threshold Settings");
    gd.addNumericField("Iterations:", 10, 0);
    gd.showDialog();
    if (gd.wasCanceled()) {
      return;
    } //if

    int iterations = (int) gd.getNextNumber();
    int FG_VAL = 255;
    int BG_VAL = 0;

    int imgSize = width * height;
    int[] inDaraArr1D = ImageJUtility.convertFrom2DTo1DIntArr(inDataArrInt, width, height, null);
    int[] tfArray = ImageTransformationFilter.GetOptimalThresholdTF(255, inDaraArr1D, imgSize, iterations, FG_VAL, BG_VAL);
    int[] tempResultArr = ImageTransformationFilter.GetTransformed1DImage(inDaraArr1D, imgSize, tfArray);
    int[][] resultImg = ImageJUtility.convertFrom1DTo2DIntArr(tempResultArr, width, height, null);

    ImageJUtility.showNewImage(resultImg, width, height,
        String.format("Optimal Threshold [%d Iterations]", iterations));
  } //run


  void showAbout() {
    IJ.showMessage("About OptimalThreshold_...", "This applies the optimal threshold\n");
  } //showAbout

} //class FilterTemplate_

