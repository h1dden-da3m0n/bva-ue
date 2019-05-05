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
    int widthOffset = 0;
    int heightOffset = 0;

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
      if (heightOffset != 0 && (heightOffset - pxOverlap) > 0) {
        heightOffset -= pxOverlap;
        sectorHeight += pxOverlap;
      }

      for (int col = 0; col < nrOfSectors; ++col) {
        // calculate proper width offset for current sector
        int sectorWidth = sectorWidths[col];
        if (widthOffset != 0 && (widthOffset - pxOverlap) > 0) {
          widthOffset -= pxOverlap;
          sectorWidth += pxOverlap;
        }

        int sectorPxSize = sectorWidth * sectorHeight;
        int[] sector = new int[sectorPxSize];

        // get actual image values for the current sector
        int idx = 0;
        for (int w = widthOffset; w < (widthOffset + sectorWidth); ++w) {
          for (int h = heightOffset; h < (heightOffset + sectorHeight); ++h) {
            sector[idx] = inDataArrInt[w][h];
            ++idx;
          }
        }

        // calculate the optimal threshold for the current sector
        int[] tfArray = ImageTransformationFilter.GetOptimalThresholdTF(
            255, sector, sectorPxSize, iterations, FG_VAL, BG_VAL);
        int[] resultImg = ImageTransformationFilter.GetTransformed1DImage(sector, sectorPxSize, tfArray);

        // propagate back the newly calculated image with the optimal threshold applied
        idx = 0;
        for (int w = widthOffset; w < (widthOffset + sectorWidth); ++w) {
          for (int h = heightOffset; h < (heightOffset + sectorHeight); ++h) {
            inDataArrInt[w][h] = resultImg[idx];
            ++idx;
          }
        }

        widthOffset += sectorWidth;
      }

      widthOffset = 0;
      heightOffset += sectorHeight;
    }

    ImageJUtility.showNewImage(inDataArrInt, width, height,
        String.format(
            "Optimal Threshold [%d Sectors, %dpx Overlap, %d Iterations]", nrOfSectors, pxOverlap, iterations));
  } //run

  void showAbout() {
    IJ.showMessage("About OptimalThreshold_...", "This applies the optimal threshold\n");
  } //showAbout

} //class FilterTemplate_

