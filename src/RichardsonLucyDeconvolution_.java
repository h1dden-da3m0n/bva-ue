import bva.util.ConvolutionFilter;
import bva.util.ImageJUtility;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class RichardsonLucyDeconvolution_ implements PlugInFilter {
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
    int[][] inDataArrayInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);
    double[][] inDataArrayDbl = ImageJUtility.convertToDoubleArr2D(inDataArrayInt, width, height);

    // user input
    GenericDialog gd = new GenericDialog("User Input");
    gd.addNumericField("Iterations", 5, 0);
    gd.showDialog();
    if (gd.wasCanceled()) {
      return;
    }
    int maxIterations = (int) gd.getNextNumber();

    // create kernel and observed image
    int radius = 10;
    double[][] kernel = ConvolutionFilter.GetGaussMask(radius, 0.5 * radius);
    double[][] observedImage = ConvolutionFilter.ConvolveDoubleNorm(inDataArrayDbl, width, height, kernel, radius);

    // create first guess (image of average gray-value 127)
    double[][][] guessedImages = new double[maxIterations + 1][width][height];
    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        guessedImages[0][x][y] = 127;
        if (observedImage[x][y] == 0) // TODO
          observedImage[x][y] = 0.001;
      }
    }

    double[][][] intermediateImages = new double[maxIterations + 1][width][height];
    double[][][] coefficientImages = new double[maxIterations + 1][width][height];
    for (int t = 0; t < maxIterations; ++t) {
      // calculate intermediate image
      intermediateImages[t] = ConvolutionFilter.ConvolveDoubleNorm(guessedImages[t], width, height, kernel, radius);

      // calculate correction coefficient image
      for (int x = 0; x < width; ++x) {
        for (int y = 0; y < height; ++y) {
          coefficientImages[t][x][y] = observedImage[x][y] / intermediateImages[t][x][y];
        }
      }
      // TODO add loop break criteria check

      coefficientImages[t] = ConvolutionFilter.ConvolveDoubleNorm(coefficientImages[t], width, height, kernel, radius);

      // applying the convolved local correction coefficients to guessed image
      for (int x = 0; x < width; ++x) {
        for (int y = 0; y < height; ++y) {
          guessedImages[t + 1][x][y] = guessedImages[t][x][y] * coefficientImages[t][x][y];

          if (guessedImages[t + 1][x][y] > 255) {
            guessedImages[t + 1][x][y] = 255; // TODO
          } else if (guessedImages[t + 1][x][y] < 0) {
            guessedImages[t + 1][x][y] = 0; // TODO
          }
        }
      }
      ImageJUtility.showNewImage(guessedImages[t], width, height, "Iteration: " + t);
    }
    ImageJUtility.showNewImage(guessedImages[maxIterations], width, height, "Iteration: " + maxIterations);
    ImageJUtility.showNewImage(observedImage, width, height, "observed image");
  } //run

  void showAbout() {
    IJ.showMessage("About Template_...",
        "this is a PluginFilter template\n");
  } //showAbout
} //class RichardsonLucyDeconvolution_

