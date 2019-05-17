import bva.util.ConvolutionFilter;
import bva.util.ImageJUtility;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class Gauss_ implements PlugInFilter {

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
    double[][] inDataArrDouble = ImageJUtility.convertToDoubleArr2D(inDataArrInt, width, height);

    GenericDialog gd = new GenericDialog("Gauss Mask Filter");
    gd.addNumericField("Mask Radius:", 4, 0);
    gd.showDialog();
    if (gd.wasCanceled()) {
      return;
    } //if

    int tgtRadius = (int) gd.getNextNumber();
    double sigma = tgtRadius * 0.5; // sigma computation ala Wolfram Alpha

    // prepare kernel
    double[][] gaussKernel = ConvolutionFilter.GetGaussMask(tgtRadius, sigma);
    // apply kernel for convolution
    double[][] resultImg = ConvolutionFilter.ConvolveDoubleNorm(inDataArrDouble, width, height, gaussKernel, tgtRadius);

    int gaussMaskSize = tgtRadius * 2 + 1;
    double minGauss = Double.MAX_VALUE;
    double maxGauss = Double.MIN_VALUE;
    for (int x = 0; x < gaussMaskSize; ++x) {
      for (int y = 0; y < gaussMaskSize; ++y) {
        if (gaussKernel[x][y] < minGauss) minGauss = gaussKernel[x][y];
        if (gaussKernel[x][y] > maxGauss) maxGauss = gaussKernel[x][y];
      }
    }
    for (int x = 0; x < gaussMaskSize; ++x) {
      for (int y = 0; y < gaussMaskSize; ++y) {
        gaussKernel[x][y] = ((gaussKernel[x][y] - minGauss) / (maxGauss - minGauss)) * 255;
      }
    }
    ImageJUtility.showNewImage(gaussKernel, gaussMaskSize, gaussMaskSize, "Gauss Kernel r=" + tgtRadius);

    ImageJUtility.showNewImage(resultImg, width, height, "Image with Gauss Kernel r=" + tgtRadius);
  } //run

  void showAbout() {
    IJ.showMessage("About Mean...",
        "applies a mean convolution filter\n");
  } //showAbout

} //class Mean_