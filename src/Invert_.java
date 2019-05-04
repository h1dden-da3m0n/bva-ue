import bva.util.ImageJUtility;
import bva.util.ImageTransformationFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Invert_ implements PlugInFilter {

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

    // get transform array
    int[] tfArray = ImageTransformationFilter.GetInversionTF(255);
    int[][] resultImg = ImageTransformationFilter.GetTransformedImage(inDataArrInt, width, height, tfArray);

    ImageJUtility.showNewImage(resultImg, width, height, "inverted image");
    ImageJUtility.createAndShowPlot("ReturnTF", "", "", tfArray);
  } //run

  void showAbout() {
    IJ.showMessage("About Invert...",
        "inverts the scalar value\n");
  } //showAbout

} //class FilterTemplate_

