import bva.util.ImageJUtility;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.*;
import java.util.Vector;

public class KMeansClustering_ implements PlugInFilter {
  private ImagePlus imagePlus = null;

  private int[] pxPerSector = null;
  private int[][] bestClusterPerPxIdx = null;

  public int setup(String arg, ImagePlus imp) {
    if (arg.equals("about")) {
      showAbout();
      return DONE;
    }

    this.imagePlus = imp;

    return DOES_RGB + DOES_STACKS + SUPPORTS_MASKING  + ROI_REQUIRED;
  } //setup

  private Vector<double[]> getCentroidClustersForPoints(ImageProcessor ip, Vector<Point> points) {
    Vector<double[]> returnCentroids = new Vector<>();

    for(Point point : points) {
      int[] rgbArr = new int[3];
      double[] rgbDoubleArr = new double[3];
      rgbArr = ip.getPixel(point.x, point.y, rgbArr);
      rgbDoubleArr = ImageJUtility.convertIntToDoubleArr(3, rgbArr, rgbDoubleArr);
      returnCentroids.add(rgbDoubleArr);
    }

    return  returnCentroids;
  }

  public void run(ImageProcessor ip) {
//    double[] blackCluster = new double[]{0, 0, 0};
//    double[] whiteCluster = new double[]{255, 255, 255};
//    double[] redCluster = new double[]{255, 0, 0};
//    double[] blueCluster = new double[]{0, 0, 255};
//    double[] greenCluster = new double[]{0, 255, 0};

//    Vector<double[]> centroidClusters = new Vector<>();
//    centroidClusters.add(blackCluster);
//    centroidClusters.add(whiteCluster);
//    centroidClusters.add(redCluster);
//    centroidClusters.add(greenCluster);
//    centroidClusters.add(blueCluster);
    Vector<Point> clusterPoints = ImageJUtility.getSeedPositions(imagePlus);
    Vector<double[]> centroidClusters = getCentroidClustersForPoints(ip, clusterPoints);

    GenericDialog gd = new GenericDialog("KMeans Options");
    gd.addNumericField("Iterations:", 5, 0);
    gd.showDialog();
    if (gd.wasCanceled()) {
      return;
    } //if

    int iterations = (int) gd.getNextNumber();

    int width = ip.getWidth();
    int height = ip.getHeight();

    pxPerSector = new int[centroidClusters.size()];
    for (int i = 0; i < centroidClusters.size(); ++i)
      pxPerSector[i] = 1;

    bestClusterPerPxIdx = new int[width][height];
    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        bestClusterPerPxIdx[x][y] = -1;
      }
    }

    for (int i = 0; i < iterations; ++i)
      centroidClusters = UpdateClusters(ip, centroidClusters);

    Vector<int[]> intCentroidClusterColours = new Vector<>();
    for (double[] cluster : centroidClusters) {
      int[] rgbArr = new int[3];
      intCentroidClusterColours.add(ImageJUtility.convertDoubleToIntArr(3, cluster, rgbArr));
    }

    int[] rgbArr = new int[3];
    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        rgbArr = ip.getPixel(x, y, rgbArr);
        int idx = GetBestCluster(rgbArr, centroidClusters);
        ip.putPixel(x, y, intCentroidClusterColours.get(idx));
      }
    }
  } //run

  // adds a value to a cluster, formula ref: Slide set "Segmentation" Slide 40
  private double[] addToCluster(double[] cluster, int N, int[] rgb) {
    for (int i = 0; i < 3; ++i)
      cluster[i] = (cluster[i] * N + rgb[i]) / (N + 1);

    return cluster;
  }

  // removes a value from a cluster, formula ref: Slide set "Segmentation" Slide 40
  private double[] removeFromCluster(double[] cluster, int N, int[] rgb) {
    for (int i = 0; i < 3; ++i)
      cluster[i] = (cluster[i] * N - rgb[i]) / (N - 1);

    return cluster;
  }

  private Vector<double[]> UpdateClusters(ImageProcessor ip, Vector<double[]> inClusters) {
    int width = ip.getWidth();
    int height = ip.getHeight();
    int[] rgb = new int[3];

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        // get colour for current pixel and find best cluster
        rgb = ip.getPixel(x, y, rgb);
        int idx = GetBestCluster(rgb, inClusters);

        // check if pixel has changed clusters
        if (bestClusterPerPxIdx[x][y] != idx) {
          int oldBestClusterIdx = bestClusterPerPxIdx[x][y];
          bestClusterPerPxIdx[x][y] = idx;

          // add value to new cluster
          double[] cn = inClusters.get(idx);
          cn = addToCluster(cn, pxPerSector[idx], rgb);
          pxPerSector[idx] += 1;
          inClusters.set(idx, cn);

          // remove value from an old cluster, should that value already have a cluster assigned
          if (oldBestClusterIdx != -1) {
            double[] cm = inClusters.get(oldBestClusterIdx);
            cm = removeFromCluster(cm, pxPerSector[oldBestClusterIdx], rgb);
            pxPerSector[oldBestClusterIdx] -= 1;
            inClusters.set(oldBestClusterIdx, cm);
          }
        }
      }
    }

    return inClusters;
  }

  // Colour Distance calculation Source: https://stackoverflow.com/a/2103608
  // just skips Math.sqrt() as we don't care about the exact distance
  private double ColourDist(double[] refColour, int[] currColour) {
    double redMean = (refColour[0] + currColour[0]) / 2;

    double r = refColour[0] - currColour[0];
    double g = refColour[1] - currColour[1];
    double b = refColour[2] - currColour[2];

    double weightR = 2 + redMean / 256;
    double weightG = 4.0;
    double weightB = 2 + (255 - redMean) / 256;

    return weightR * r * r + weightG * g * g + weightB * b * b;
  }

  private int GetBestCluster(int[] rgbArr, Vector<double[]> clusters) {
    int minIdx = -1;
    double minDistance = Double.MAX_VALUE;

    for (int i = 0; i < clusters.size(); i++) {
      double distance = ColourDist(clusters.get(i), rgbArr);
      if (distance < minDistance) {
        minIdx = i;
        minDistance = distance;
      }
    }

    return minIdx;
  }

  void showAbout() {
    IJ.showMessage("About KMeansClustering_...",
        "Plugin to apply K-Means Clustering\n");
  } //showAbout

} //class KMeansClusteringTemplate_