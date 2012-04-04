package de.jungblut.visualize;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleMatrix;
import de.jungblut.math.dense.DenseDoubleVector;
import de.jungblut.regression.PolynomialRegression;

public final class GnuPlot {

  public static String GNUPLOT_PATH = "gnuplot";
  public static String TMP_PATH = "/";

  public static void plot(DenseDoubleMatrix x, DoubleVector y,
      DoubleVector theta, int polyCount, DoubleVector mean, DoubleVector sigma) {
    /*
     * set xrange [" + (x.min(0) - 15) + ":" + (x.max(0) + 15) +
     * "] ; set yrange [" + (y.min() - 15) + ":" + (y.max() + 15) + "] ;
     */

    // calculate a few points
    DenseDoubleVector fromUpTo = DenseDoubleVector.fromUpTo(x.min(0) - 15,
        x.max(0) + 15, 0.05);

    DenseDoubleMatrix createPolynomials = PolynomialRegression
        .createPolynomials(new DenseDoubleMatrix(fromUpTo), polyCount);

    DenseDoubleMatrix xPolyNormalized = new DenseDoubleMatrix(
        DenseDoubleVector.ones(fromUpTo.getLength()),
        (DenseDoubleMatrix) createPolynomials.subtract(mean).divide(sigma));

    DoubleVector multiplyVector = xPolyNormalized.multiplyVector(theta);
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
        TMP_PATH + "gnuplot_function.in")))) {
      for (int i = 0; i < multiplyVector.getLength(); i++) {
        bw.write(fromUpTo.get(i) + " " + multiplyVector.get(i) + "\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    drawPoints(x, y, TMP_PATH + "gnuplot_function.in");

  }

  public static void drawPoints(DenseDoubleMatrix x, DoubleVector y,
      String functionFile) {
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
        TMP_PATH + "gnuplot.in")))) {
      for (int i = 0; i < y.getLength(); i++) {
        bw.write(x.get(i, 0) + " " + y.get(i) + "\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    // "plot "data" every 1000 using 1:2 with lines" for more data Page 30
    String exec = "set xzeroaxis; set yzeroaxis ; plot '" + TMP_PATH
        + "gnuplot.in' every 1000 using 1:2 with points";
    if (x.getRowCount() > 10000) {
      exec = "set xzeroaxis; set yzeroaxis ; plot '" + TMP_PATH
          + "gnuplot.in' every 1000 using 1:2 with points";
    } else {
      exec = "set xzeroaxis; set yzeroaxis ; plot '" + TMP_PATH
          + "gnuplot.in' with points";
    }
    if (functionFile != null) {
      exec += ",'" + functionFile + "' with lines;";
    }
    try {
      Files.write(FileSystems.getDefault().getPath(TMP_PATH + "exec.gp"),
          exec.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
          StandardOpenOption.TRUNCATE_EXISTING);
      Process exec2 = Runtime.getRuntime().exec(
          new String[] { GNUPLOT_PATH, "-p", TMP_PATH + "exec.gp" });
      Scanner scan = new Scanner(System.in);
      scan.nextLine();
      exec2.destroy();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void drawPointsPerIndex(DenseDoubleMatrix x, DoubleVector y,
      String functionFile, String featureOneTitle, String featureTwoTitle) {

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
        TMP_PATH + "gnuplot1.in")))) {
      for (int i = 0; i < y.getLength(); i++) {
        bw.write(i + " " + x.get(i, 0) + "\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
        TMP_PATH + "gnuplot2.in")))) {
      for (int i = 0; i < y.getLength(); i++) {
        bw.write(i + " " + y.get(i) + "\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    /*
     * For pictures: String exec =
     * "set terminal png size 1920,1080 ; set output \"/img/"
     * +id+".png\" ; set pointsize 2; set xzeroaxis; set yzeroaxis ; plot '" +
     * TMP_PATH + "gnuplot1.in' title \"" + featureOneTitle +
     * "\" with linespoints, '" + TMP_PATH + "gnuplot2.in' title \"" +
     * featureTwoTitle +
     * "\" with linespoints ; set terminal wxt size 1920,1080 ;";
     */
    String exec = "set pointsize 2; set xzeroaxis; set yzeroaxis ; plot '"
        + TMP_PATH + "gnuplot1.in' title \"" + featureOneTitle
        + "\" with linespoints, '" + TMP_PATH + "gnuplot2.in' title \""
        + featureTwoTitle + "\" with linespoints";
    if (functionFile != null) {
      exec += ",'" + functionFile + "' with lines;";
    }
    try {
      Files.write(FileSystems.getDefault().getPath(TMP_PATH + "exec.gp"),
          exec.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE,
          StandardOpenOption.TRUNCATE_EXISTING);
      Process exec2 = Runtime.getRuntime().exec(
          new String[] { GNUPLOT_PATH, "-p", TMP_PATH + "exec.gp" });
      Scanner scan = new Scanner(System.in);
      scan.nextLine();
      exec2.destroy();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String modelToGNUPlot(DoubleVector p) {
    String s = "";
    for (int i = 0; i < p.getLength(); i++) {
      if (i == 0) {
        s += "" + p.get(i);
      } else if (i == 1) {
        s = p.get(i) + "*x + " + s;
      } else {
        s = p.get(i) + "*x**" + i + "+" + s;
      }
    }
    return s;
  }
}