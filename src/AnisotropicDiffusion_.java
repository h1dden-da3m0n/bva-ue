import bva.util.ConvolutionFilter;
import bva.util.ImageJUtility;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class AnisotropicDiffusion_ implements PlugInFilter {

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
    double[][] imageData = ImageJUtility.convertToDoubleArr2D(inDataArrInt, width, height);

    GenericDialog gd = new GenericDialog("Anisotropic Diffusion");
    gd.addSlider("K:", 0, 100, 20, 1);
    gd.addNumericField("Iter.:", 10, 0);
    gd.addCheckbox("Show all Iter:", false);
    gd.showDialog();
    if (gd.wasCanceled()) {
      return;
    } //if
    int k = (int) gd.getNextNumber();
    int iter = (int) gd.getNextNumber();
    boolean showIter = gd.getNextBoolean();

    for (int i = 0; i < iter; ++i) {
      // calc gradients
      double[][] gN = ConvolutionFilter.ConvolveDouble(
          imageData, width, height, new double[][]{{0, 0, 0}, {1, -1, 0}, {0, 0, 0}}, 1);
      double[][] gNE = ConvolutionFilter.ConvolveDouble(
          imageData, width, height, new double[][]{{0, 0, 0}, {0, -1, 0}, {1, 0, 0}}, 1);
      double[][] gE = ConvolutionFilter.ConvolveDouble(
          imageData, width, height, new double[][]{{0, 0, 0}, {0, -1, 0}, {0, 1, 0}}, 1);
      double[][] gSE = ConvolutionFilter.ConvolveDouble(
          imageData, width, height, new double[][]{{0, 0, 0}, {0, -1, 0}, {0, 0, 1}}, 1);
      double[][] gS = ConvolutionFilter.ConvolveDouble(
          imageData, width, height, new double[][]{{0, 0, 0}, {0, -1, 1}, {0, 0, 0}}, 1);
      double[][] gSW = ConvolutionFilter.ConvolveDouble(
          imageData, width, height, new double[][]{{0, 0, 1}, {0, -1, 0}, {0, 0, 0}}, 1);
      double[][] gW = ConvolutionFilter.ConvolveDouble(
          imageData, width, height, new double[][]{{0, 1, 0}, {0, -1, 0}, {0, 0, 0}}, 1);
      double[][] gNW = ConvolutionFilter.ConvolveDouble(
          imageData, width, height, new double[][]{{1, 0, 0}, {0, -1, 0}, {0, 0, 0}}, 1);

      // calc diffusion coefficient
      double[][] cN = calcDiffCoeff(gN, width, height, k);
      double[][] cNE = calcDiffCoeff(gNE, width, height, k);
      double[][] cE = calcDiffCoeff(gE, width, height, k);
      double[][] cSE = calcDiffCoeff(gSE, width, height, k);
      double[][] cS = calcDiffCoeff(gS, width, height, k);
      double[][] cSW = calcDiffCoeff(gSW, width, height, k);
      double[][] cW = calcDiffCoeff(gW, width, height, k);
      double[][] cNW = calcDiffCoeff(gNW, width, height, k);

      for (int x = 0; x < width; ++x) {
        for (int y = 0; y < height; ++y) {
          // calculate delta
          imageData[x][y] += (1.0 / 7.0) * (
              gN[x][y] * cN[x][y] +
                  gNE[x][y] * cNE[x][y] * 0.7 +
                  gE[x][y] * cE[x][y] +
                  gSE[x][y] * cSE[x][y] * 0.7 +
                  gS[x][y] * cS[x][y] +
                  gSW[x][y] * cSW[x][y] * 0.7 +
                  gW[x][y] * cW[x][y] +
                  gNW[x][y] * cNW[x][y] * 0.7
          );
        }
      }

      if (showIter)
        ImageJUtility.showNewImage(imageData, width, height,
            "Anisotropic Diffusion [k=" + k + ", i=" + (i + 1) + "]");
    }

    if (!showIter)
      ImageJUtility.showNewImage(imageData, width, height,
          "Anisotropic Diffusion [k=" + k + ", i=" + iter + "]");
    ImageJUtility.showNewImageCheckerBoard(3, width, height, imageData, inDataArrDouble);
  } //run

  void showAbout() {
    IJ.showMessage("About AnisotropicDiffusion_...",
        "this is a PluginFilter template\n");
  } //showAbout

  private double[][] calcDiffCoeff(double[][] nabla, int width, int height, int k) {
    double[][] diffCoeff = new double[width][height];

    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        diffCoeff[x][y] = Math.pow(Math.E, -(Math.pow(Math.abs(nabla[x][y]), 2) / (2 * (k * k))));
      }
    }

    return diffCoeff;
  }

} //class FilterTemplate_

