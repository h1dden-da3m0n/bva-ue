import bva.util.ImageJUtility;
import bva.util.ImageTransformationFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class AdaptiveOptimalThreshold_ implements PlugInFilter {

  public int setup(String arg, ImagePlus imp) {
    if (arg.equals("about")) {
      showAbout();
      return DONE;
    }
    return DOES_8G + DOES_STACKS + SUPPORTS_MASKING;
  } //setup

  public void run(ImageProcessor ip) {
    GenericDialog gd = new GenericDialog("Optimal Threshold Settings");
    gd.addNumericField("# of Sectors:", 4, 0);
    gd.addNumericField("Sector Overlap (px):", 2, 0);
    gd.addNumericField("Iterations:", 10, 0);
    gd.showDialog();
    if (gd.wasCanceled()) {
      return;
    } //if

    int nrOfSectors = (int) gd.getNextNumber();
    int pxOverlap = (int) gd.getNextNumber();
    int iterations = (int) gd.getNextNumber();

    byte[] pixels = (byte[]) ip.getPixels();
    int width = ip.getWidth();
    int height = ip.getHeight();
    int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);

    int FG_VAL = 255;
    int BG_VAL = 0;

    int[][] resultImg = applyAdaptiveOptimalThold(inDataArrInt, width, height,
        nrOfSectors, pxOverlap, iterations, FG_VAL, BG_VAL);

    ImageJUtility.showNewImage(resultImg, width, height,
        String.format(
            "Optimal Threshold [%d Sectors, %dpx Overlap, %d Iterations]", nrOfSectors, pxOverlap, iterations));
  } //run

  private int[][] applyAdaptiveOptimalThold(int[][] inDataArr, int width, int height,
                                            int nrOfSectors, int pxOverlap, int iterations,
                                            int fg_val, int bg_val) {
    int xOffset = 0;
    int yOffset = 0;

    // get px per Sector
    int[] sectorWidths = new int[nrOfSectors];
    int[] sectorHeights = new int[nrOfSectors];
    for (int i = 0; i < nrOfSectors; ++i) {
      sectorWidths[i] = width / nrOfSectors;
      sectorHeights[i] = height / nrOfSectors;
    }
    // add rest of px if height or width isn't a multiple of nrOfSectors
    for (int i = width % nrOfSectors; i > 0; --i)
      sectorWidths[i - 1] += 1;
    for (int i = height % nrOfSectors; i > 0; --i)
      sectorHeights[i - 1] += 1;

    for (int row = 0; row < nrOfSectors; ++row) {
      // calculate proper height offset for current sector
      int sectorHeight = sectorHeights[row];
      if (yOffset != 0 && (yOffset - pxOverlap) > 0) {
        yOffset -= pxOverlap;
        sectorHeight += pxOverlap;
      }

      for (int col = 0; col < nrOfSectors; ++col) {
        // calculate proper width offset for current sector
        int sectorWidth = sectorWidths[col];
        if (xOffset != 0 && (xOffset - pxOverlap) > 0) {
          xOffset -= pxOverlap;
          sectorWidth += pxOverlap;
        }

        // get actual image values for the current sector with offset (if given)
        int sectorPxSize = sectorWidth * sectorHeight;
        int adjustedEndX = (xOffset + sectorWidth);
        int adjustedEndY = (yOffset + sectorHeight);
        int[] sector = new int[sectorPxSize];
        sector = ImageJUtility.convertFrom2DTo1DIntArr(
            inDataArr, adjustedEndX, adjustedEndY, xOffset, yOffset, sector);

        // calculate the optimal threshold for the current sector
        int[] tfArray = ImageTransformationFilter.GetOptimalThresholdTF(
            255, sector, sectorPxSize, iterations, fg_val, bg_val);
        int[] resultImg = ImageTransformationFilter.GetTransformed1DImage(sector, sectorPxSize, tfArray);

        // propagate back the newly calculated image with the optimal threshold applied
        inDataArr = ImageJUtility.convertFrom1DTo2DIntArr(
            resultImg, adjustedEndX, adjustedEndY, xOffset, yOffset, inDataArr);

        xOffset += sectorWidth;
      }

      xOffset = 0;
      yOffset += sectorHeight;
    }

    return inDataArr;
  }

  void showAbout() {
    IJ.showMessage("About OptimalThreshold_...", "This applies the optimal threshold\n");
  } //showAbout

} //class FilterTemplate_

