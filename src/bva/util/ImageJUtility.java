package bva.util;

import ij.ImagePlus;
import ij.gui.Plot;
import ij.gui.PointRoi;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.awt.*;
import java.util.Vector;

public class ImageJUtility {

  public static int[][] convertFrom1DByteArr(byte[] pixels, int width, int height) {

    int[][] inArray2D = new int[width][height];

    int pixelIdx1D = 0;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        inArray2D[x][y] = (int) pixels[pixelIdx1D];
        if (inArray2D[x][y] < 0) {
          inArray2D[x][y] += 256;
        } // if
        pixelIdx1D++;
      }
    }

    return inArray2D;
  }


  public static double[][] convertToDoubleArr2D(int[][] inArr, int width, int height) {
    double[][] returnArr = new double[width][height];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        returnArr[x][y] = inArr[x][y];
      }
    }

    return returnArr;
  }

  public static int[][] convertToIntArr2D(double[][] inArr, int width, int height) {
    int[][] returnArr = new int[width][height];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        returnArr[x][y] = (int) (inArr[x][y] + 0.5);
      }
    }

    return returnArr;
  }


  public static byte[] convertFrom2DIntArr(int[][] inArr, int width, int height) {
    int pixelIdx1D = 0;
    byte[] outArray2D = new byte[width * height];

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int resultVal = inArr[x][y];
        if (resultVal > 127) {
          resultVal -= 256;
        }
        outArray2D[pixelIdx1D] = (byte) resultVal;
        pixelIdx1D++;
      }
    }

    return outArray2D;
  }

  public static void showNewImage(int[][] inArr, int width, int height, String title) {
    byte[] byteArr = ImageJUtility.convertFrom2DIntArr(inArr, width, height);
    ImageJUtility.showNewImage(byteArr, width, height, title);
  }

  public static void showNewImage(double[][] inArr, int width, int height, String title) {
    int[][] intArr = ImageJUtility.convertToIntArr2D(inArr, width, height);
    byte[] byteArr = ImageJUtility.convertFrom2DIntArr(intArr, width, height);
    ImageJUtility.showNewImage(byteArr, width, height, title);
  }

  public static void showNewImage(byte[] inByteArr, int width, int height, String title) {
    ImageProcessor outImgProc = new ByteProcessor(width, height);
    outImgProc.setPixels(inByteArr);

    ImagePlus ip = new ImagePlus(title, outImgProc);
    ip.show();
  }

  public static void showNewImageChequerboard(int nrOfSectors, int width, int height,
                                              double[][]compImg, double[][] orgImg) {
    double[][] resultImg = new double[width][height];
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

    boolean imageToggle = true; // true=compImg, false=orgImg used
    for (int row = 0; row < nrOfSectors; ++row) {
      int sectorHeight = sectorHeights[row];

      for (int col = 0; col < nrOfSectors; ++col) {
        int sectorWidth = sectorWidths[col];

        // get actual image values for the current sector with offset (if given)
        int adjustedEndX = (xOffset + sectorWidth);
        int adjustedEndY = (yOffset + sectorHeight);

        for (int w = xOffset; w < adjustedEndX; ++w) {
          for (int h = yOffset; h < adjustedEndY; ++h) {
             if (imageToggle)
               resultImg[w][h] = compImg[w][h];
             else
               resultImg[w][h] = orgImg[w][h];
          }
        }

        imageToggle = !imageToggle;
        xOffset += sectorWidth;
      }

      xOffset = 0;
      yOffset += sectorHeight;
    }

    ImageJUtility.showNewImage(resultImg, width, height, "Chequerboard Image");
  }

  public enum PlotShapes {
    BAR("bar"),
    DOT("dot"),
    EX("x"),
    LINE("line");

    private final String shape;

    PlotShapes(String shape) {
      this.shape = shape;
    }

    public String shape() {
      return this.shape;
    }
  }

  public static void createAndShowPlot(String title, String xLabel, String yLabel, double[] values, PlotShapes pShape) {
    Plot p = new Plot(title, xLabel, yLabel);
    p.add(pShape.shape(), values);
    p.show();
  }

  public static void createAndShowPlot(String title, String xLabel, String yLabel, double[] values) {
    createAndShowPlot(title, xLabel, yLabel, values, PlotShapes.BAR);
  }

  public static void createAndShowPlot(String title, String xLabel, String yLabel, int[] values, PlotShapes pShape) {
    double[] doubleValues = new double[values.length];
    for (int i = 0; i < values.length; ++i)
      doubleValues[i] = values[i];
    createAndShowPlot(title, xLabel, yLabel, doubleValues, pShape);
  }

  public static void createAndShowPlot(String title, String xLabel, String yLabel, int[] values) {
    createAndShowPlot(title, xLabel, yLabel, values, PlotShapes.BAR);
  }

  public static double[][] cropImage(double[][] inImg, int width, int height, Rectangle roi) {
    int roiWidth = roi.width;
    int roiHeight = roi.height;

    int roiXseed = roi.x;
    int roiYseed = roi.y;

    double[][] returnImg = new double[roiWidth][roiHeight];
    for (int xIdx = 0; xIdx < width; xIdx++) {
      for (int yIdx = 0; yIdx < height; yIdx++) {
        int origXIdx = xIdx + roiXseed;
        int origYIdx = yIdx + roiYseed;
        returnImg[xIdx][yIdx] = inImg[origXIdx][origYIdx];
      }
    }

    return returnImg;
  }

  public static int CONVERSION_MODE_RGB_GRAYSCALE_MEAN = 1;

  public static double[][] getGrayscaleImgFromRGB(ImageProcessor imgProc, int conversionMode) {
    int width = imgProc.getWidth();
    int height = imgProc.getHeight();
    int[] rgbArr = new int[3];
    double[][] returnImg = new double[imgProc.getWidth()][imgProc.getHeight()];
    for (int xIdx = 0; xIdx < width; xIdx++) {
      for (int yIdx = 0; yIdx < height; yIdx++) {
        rgbArr = imgProc.getPixel(xIdx, yIdx, rgbArr);
        if (conversionMode == CONVERSION_MODE_RGB_GRAYSCALE_MEAN) {
          double meanVal = rgbArr[0] + rgbArr[1] + rgbArr[2];
          meanVal = meanVal / 3.0;
          returnImg[xIdx][yIdx] = meanVal;
        }
      }
    }

    return returnImg;
  }

  public static int[][] calculateImgDifference(int[][] inImgA, int[][] inImgB, int width, int height) {
    int[][] returnImg = new int[width][height];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        returnImg[x][y] = Math.abs(inImgA[x][y] - inImgB[x][y]);
      }
    }

    return returnImg;
  }

  public static Vector<Point> getSeedPositions(ImagePlus imagePlus) {
    PointRoi pr = (PointRoi) imagePlus.getRoi();
    int[] xCoords = pr.getXCoordinates();
    int[] yCoords = pr.getYCoordinates();
    int numOfElements = pr.getNCoordinates();
    Rectangle boundingRoi = pr.getBounds();

    Vector<Point> seedPositions = new Vector<>();

    for (int i = 0; i < numOfElements; ++i)
      seedPositions.add(new Point(xCoords[i] + boundingRoi.x, yCoords[i] + boundingRoi.y));

    return seedPositions;
  }

  // converts a int array to a double array
  // if a double array is passes as iArr it gets used instead of creating a new one
  public static double[] convertIntToDoubleArr(int size, int[] intArr, double[] iArr) {
    if (iArr == null) iArr = new double[size];
    for (int i = 0; i < size; ++i)
      iArr[i] = intArr[i];
    return iArr;
  }

  // converts a double array to a int array (WARNING: floors double value)
  // if a int array is passes as iArr it gets used instead of creating a new one
  public static int[] convertDoubleToIntArr(int size, double[] doubleArr, int[] iArr) {
    if (iArr == null) iArr = new int[size];
    for (int i = 0; i < size; ++i)
      iArr[i] = (int) doubleArr[i];
    return iArr;
  }

  public static int[] convertFrom2DTo1DIntArr(int[][] in2DArr, int width, int height,
                                              int xStartIdx, int yStartIdx, int[] iArr) {
    if (iArr == null) iArr = new int[width * height];
    int idx = 0;
    for (int x = xStartIdx; x < width; ++x) {
      for (int y = yStartIdx; y < height; ++y) {
        iArr[idx] = in2DArr[x][y];
        ++idx;
      }
    }
    return iArr;
  }

  public static int[] convertFrom2DTo1DIntArr(int[][] in2DArr, int width, int height, int[] iArr) {
    return convertFrom2DTo1DIntArr(in2DArr, width, height, 0, 0, iArr);
  }

  public static int[][] convertFrom1DTo2DIntArr(int[] in1DArr, int width, int height,
                                                int xStartIdx, int yStartIdx, int[][] iArr) {
    if (iArr == null) iArr = new int[width][height];
    int idx = 0;
    for (int x = xStartIdx; x < width; ++x) {
      for (int y = yStartIdx; y < height; ++y) {
        iArr[x][y] = in1DArr[idx];
        ++idx;
      }
    }
    return iArr;
  }

  public static int[][] convertFrom1DTo2DIntArr(int[] in1DArr, int width, int height, int[][] iArr) {
    return convertFrom1DTo2DIntArr(in1DArr, width, height, 0, 0, iArr);
  }
}
