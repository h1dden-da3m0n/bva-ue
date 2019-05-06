package bva.util;

public class ImageTransformationFilter {


  public static int[][] GetTransformedImage(int[][] inImg, int width, int height, int[] transferFunction) {
    int[][] returnImg = new int[width][height];

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        returnImg[x][y] = transferFunction[inImg[x][y]];
      }
    }

    return returnImg;
  }

  public static int[] GetTransformed1DImage(int[] inImg, int size, int[] transferFunction) {
    int[] returnImg = new int[size];
    for (int pos = 0; pos < size; pos++) {
      returnImg[pos]= transferFunction[inImg[pos]];
    }
    return returnImg;
  }

  public static int[] GetInversionTF(int maxVal) {
    int[] returnTF = new int[maxVal + 1];

    for (int i = 0; i <= maxVal; ++i)
      returnTF[i] = maxVal - i;

    return returnTF;
  }

  public static int[] GetHistogram(int maxVal, int[][] inImg, int width, int height) {
    int[] histogram = new int[maxVal + 1];

    for (int x = 0; x < width; ++x) {
      for (int y = 0; y < height; ++y) {
        histogram[inImg[x][y]]++;
      }
    }

    return histogram;
  }

  public static int[] GetGammaCorrTF(int maxVal, double gamma) {
    int[] returnTF = new int[maxVal + 1];

    for (int i = 0; i < maxVal; ++i)
      returnTF[i] = (int) (255 * Math.pow((i / 255.0), (1.0 / gamma)));

    return returnTF;
  }

  public static int[] GetBinaryThresholdTF(int maxVal, int thresholdVal, int FG_VAL, int BG_VAL) {
    int[] returnTF = new int[maxVal + 1];


    return returnTF;
  }

  public static int[] GetHistogramEqualizationTF(int maxVal, int[][] inImg, int width, int height) {
    int[] returnTF = new int[maxVal + 1];

    double[] prob = new double[maxVal + 1];
    int[] hist = GetHistogram(maxVal, inImg, width, height);
    for (int i = 0; i < maxVal; ++i) {
      prob[i] = ((double) hist[i]) / (width * height);
    }

    int aMin = 0;
    for (int i = 0; i <= maxVal; ++i) {
      if (hist[i] != 0) {
        aMin = i;
        break;
      }
    }

    int aMax = 0;
    for (int i = maxVal; i >= 0; --i) {
      if (hist[i] != 0) {
        aMax = i;
        break;
      }
    }

    int sum = 0;
    double pSum = 0;
    double[] cdf = new double[maxVal + 1];
    for (int i = 0; i <= maxVal; ++i) {
      sum += prob[i] * ((aMax - aMin) + 1);
      returnTF[i] = (int) Math.floor(sum) + aMin;

      pSum += prob[i];
      cdf[i] = pSum;
    }

    ImageJUtility.createAndShowPlot("Original - CDF", "Values", "Probability", cdf,
        ImageJUtility.PlotShapes.LINE);

    return returnTF;
  }

  public static int[] GetOptimalThresholdTF(int maxVal, int[] inImg, int size, int iterations, int FG_VAL, int BG_VAL) {
    int[] returnTF = new int[maxVal + 1];

    int lftSum = 0;
    int lftCnt = 0;
    int rgtSum = 0;
    int rgtCnt = 0;
    // threshold can be initialized with center of max value: 255/2 = 127.5
    int thold = 127;

    // try to improve threshold till optimum or max of iterations is reached
    for (int i = 0; i < iterations; ++i) {
      // calculate the left and right sites of values separated by the current threshold
      for (int pos = 0; pos < size; ++pos) {
        if (inImg[pos] < thold) {
          lftSum += inImg[pos];
          lftCnt++;
        } else {
          rgtSum += inImg[pos];
          rgtCnt++;
        }
      }

      // based on left & right side attempt to improve threshold
      int newThold = thold;
      if (lftCnt != 0 && rgtCnt != 0) {
        int leftMean = lftSum / lftCnt;
        int rightMean = rgtSum / rgtCnt;
        newThold = (leftMean + rightMean) / 2;
      }
      else if (lftCnt != 0)
        newThold = lftSum / lftCnt;
      else if (rgtCnt != 0)
        newThold = rgtSum / rgtCnt;

      // stop if current threshold is optimum threshold
      if (thold == newThold)
        break;

      thold = newThold;
    }

    for (int i = 0; i <= maxVal; ++i) {
      if (i <= thold)
        returnTF[i] = BG_VAL;
      else
        returnTF[i] = FG_VAL;
    }

    return returnTF;
  }
}
