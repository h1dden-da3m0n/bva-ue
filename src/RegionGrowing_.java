import bva.util.ImageJUtility;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.*;
import java.util.Stack;
import java.util.Vector;

public class RegionGrowing_ implements PlugInFilter {

  private static int BG_VAL = 0;
  private static int FG_VAL = 255;
  private static int UNPROCESSED = -1;
  private static int NB_ARR_RADIUS = 1;

  private ImagePlus imagePlus = null;

  public int setup(String arg, ImagePlus imp) {
    if (arg.equals("about")) {
      showAbout();
      return DONE;
    }

    imagePlus = imp;
    return DOES_8G + DOES_STACKS + SUPPORTS_MASKING + ROI_REQUIRED;
  } //setup

  public void run(ImageProcessor ip) {
    byte[] pixels = (byte[]) ip.getPixels();
    int width = ip.getWidth();
    int height = ip.getHeight();
    int[][] inDataArrInt = ImageJUtility.convertFrom1DByteArr(pixels, width, height);

    Vector<Point> seedPositions = ImageJUtility.getSeedPositions(imagePlus);
    int minVal = 0;
    int maxVal = 255;
    GenericDialog gd = new GenericDialog("User Input");
    gd.addSlider("lower thresh", minVal, maxVal, minVal);
    gd.addSlider("upper thresh", minVal, maxVal, maxVal);
    gd.showDialog();
    if (gd.wasCanceled()) {
      return;
    } //if

    int lowerThresh = (int) gd.getNextNumber();
    int upperThresh = (int) gd.getNextNumber();

    // adjacency: use NB!

    //int[][] nbArr = RegionGrowing_.GetNeighborArrN8();
    //int[][] rgResultImg = PerformRegionGrowing(inDataArrInt, width, height, nbArr, lowerThresh, upperThresh, seedPositions);
    int[][] rgResultImg = PerformRegionGrowing(inDataArrInt, width, height, lowerThresh, upperThresh, seedPositions);


    ImageJUtility.showNewImage(rgResultImg, width, height, "RG result for [" + lowerThresh + ";" + upperThresh + "] and #seeds=" + seedPositions.size());

  } //run

  private static int[][] PerformRegionGrowing(int[][] inImg, int width, int height, int lowerThresh, int upperThresh, Vector<Point> seedPositions) {
    int[][] returnImg = new int[width][height];

    // first init all with UNPROCESSED
    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        returnImg[x][y] = UNPROCESSED;
      }
    }

    Stack<Point> processingStack = new Stack<>();
    for (Point p : seedPositions) {
      if (returnImg[p.x][p.y] == UNPROCESSED) {
        returnImg[p.x][p.y]++;
        processingStack.push(p);
      }
    }

    while (!processingStack.empty()) {
      Point currPoint = processingStack.pop();

      // check interval
      int actVal = inImg[currPoint.x][currPoint.y];
      if (actVal >= lowerThresh && actVal <= upperThresh) {
        returnImg[currPoint.x][currPoint.y] = FG_VAL;

        // check neighbours
        for (int ox = -1; ox <= 1; ++ox) {
          for (int oy = -1; oy <= 1; ++oy) {
            int nbX = currPoint.x + ox;
            int nbY = currPoint.y + oy;

            // check range
            if (nbX >= 0 && nbX < width && nbY >= 0 && nbY < height) {
              if (returnImg[nbX][nbY] == UNPROCESSED) {
                returnImg[nbX][nbY]++;
                processingStack.push(new Point(nbX, nbY));
              }
            }
          }
        }
      } else
        returnImg[currPoint.x][currPoint.y] = BG_VAL;
    }

    // finally clean uo -1 => BG_VAL
    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        if (returnImg[x][y] == UNPROCESSED)
          returnImg[x][y] = BG_VAL;
      }
    }

    return returnImg;
  }

  public static int[][] PerformRegionGrowing(int[][] inImg, int width, int height, int[][] nbArr, int lowerThresh, int upperThresh, Vector<Point> seedPositions) {
    int[][] returnImg = new int[width][height];

    //TODO: implementation required

    return returnImg;
  }

  public static int[][] GetNeighborArrN4() {
    //TODO: implementation required
    return null;
  }

  public static int[][] GetNeighborArrN8() {
    return null;
  }

  void showAbout() {
    IJ.showMessage("About Template_...",
        "this is a RegionGrowing_ template\n");
  } //showAbout

} //class RegionGrowing_