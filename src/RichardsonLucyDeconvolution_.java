import bva.util.ConvolutionFilter;
import bva.util.ImageJUtility;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.util.Random;

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
    double[][] inDataArrayDouble = ImageJUtility.convertToDoubleArr2D(inDataArrayInt, width, height);

    // user input
    GenericDialog gd = new GenericDialog("RLD Options");
    gd.addNumericField("Iter.:", 5, 0);
    gd.addNumericField("Lower Bound:", 0.995, 6);
    gd.addNumericField("Upper Bound:", 1.005, 6);
    gd.addCheckbox("Show iterations:", false);
    gd.addChoice("Init Img:", new String[]{"127-gray", "rand(100,180)"}, "127-gray");
    gd.showDialog();
    if (gd.wasCanceled()) {
      return;
    }
    int iter = (int) gd.getNextNumber();
    double lowerBound = gd.getNextNumber();
    double upperBound = gd.getNextNumber();
    boolean showIter = gd.getNextBoolean();
    int initImg = gd.getNextChoiceIndex();

    // create kernel and based on that observed image
    int r = 4;
    double[][] kernel = ConvolutionFilter.GetGaussMask(r, 0.5 * r);
    double[][] observedImg = ConvolutionFilter.ConvolveDoubleNorm(inDataArrayDouble, width, height, kernel, r);

    // create first guess
    // * image of random gray values [100 .. 180]
    // or image of avg gray values (127)
    double[][][] guessedImgs = new double[iter + 1][width][height];
    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        if (initImg == 0)
          guessedImgs[0][x][y] = 127;
        else
          guessedImgs[0][x][y] = new Random().nextInt(80) +100;

        // correct observed image to prevent later calculation breakage
        if (observedImg[x][y] == 0)
          observedImg[x][y] = 1;
      }
    }

    double[][][] intermediateImgs = new double[iter + 1][width][height];
    double[][][] coefficientImgs = new double[iter + 1][width][height];
    double avg = 0;
    for (int i = 0; i < iter; ++i) {
      // calculate intermediate image
      intermediateImgs[i] = ConvolutionFilter.ConvolveDoubleNorm(guessedImgs[i], width, height, kernel, r);

      // calculate correction coefficient image
      for (int w = 0; w < width; ++w) {
        for (int h = 0; h < height; ++h) {
          coefficientImgs[i][w][h] = observedImg[w][h] / intermediateImgs[i][w][h];
          avg += coefficientImgs[i][w][h];
        }
      }

      // brakes loop if average coefficient is close enough to 1
      avg /= (width * height);
      if (lowerBound < avg && avg < upperBound) break;

      // applying the convolved local correction coefficients to guessed image
      coefficientImgs[i] = ConvolutionFilter.ConvolveDoubleNorm(coefficientImgs[i], width, height, kernel, r);
      for (int x = 0; x < width; ++x) {
        for (int y = 0; y < height; ++y) {
          guessedImgs[i + 1][x][y] = guessedImgs[i][x][y] * coefficientImgs[i][x][y];

          if (guessedImgs[i + 1][x][y] > 255)
            guessedImgs[i + 1][x][y] = 255;
          else if (guessedImgs[i + 1][x][y] < 0)
            guessedImgs[i + 1][x][y] = 0;
        }
      }
      if (showIter)
        ImageJUtility.showNewImage(guessedImgs[i], width, height, "RDL [i=" + i + ", deviation=" + avg + "]");
    }
    ImageJUtility.showNewImage(guessedImgs[iter], width, height, "RDL [i=" + iter + ", deviation=" + avg + "]");
    ImageJUtility.showNewImage(observedImg, width, height, "RDL - Observed Image");
    ImageJUtility.showNewImageCheckerBoard(3, width, height, guessedImgs[iter], observedImg);
  } //run

  void showAbout() {
    IJ.showMessage("About Template_...",
        "this is a PluginFilter template\n");
  } //showAbout
} //class RichardsonLucyDeconvolution_

