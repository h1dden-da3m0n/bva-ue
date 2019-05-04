import bva.util.ImageJUtility;
import bva.util.ImageTransformationFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class HistogramEqualization_ implements PlugInFilter {

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
    int maxVal = 255;

    // get transform array
    int[] tfArray = ImageTransformationFilter.GetHistogramEqualizationTF(maxVal, inDataArrInt, width, height);
    int[][] resultImg = ImageTransformationFilter.GetTransformedImage(inDataArrInt, width, height, tfArray);

    ImageJUtility.showNewImage(resultImg, width, height, "HE Image");
    ImageJUtility.createAndShowPlot("ReturnTF", "", "", tfArray);

    int[] histogram = ImageTransformationFilter.GetHistogram(maxVal, resultImg, width, height);
    double pSum = 0;
    double[] cdf = new double[maxVal + 1];
    for (int i = 0; i < maxVal; ++i) {
      pSum += ((double) histogram[i]) / (width * height);
      cdf[i] = pSum;
    }
    ImageJUtility.createAndShowPlot("Histogram Equalization - CDF", "Values", "Probability", cdf,
        ImageJUtility.PlotShapes.LINE);
  } //run

  void showAbout() {
    IJ.showMessage("About Histogram Equalization...",
        "Applies the default Histogram Equalization\n");
  } //showAbout

} //class FilterTemplate_
