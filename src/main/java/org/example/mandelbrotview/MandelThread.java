package org.example.mandelbrotview;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import org.apache.commons.math3.complex.Complex;
import org.example.mandelbrotview.view.GuiFactory;
import org.example.mandelbrotview.view.MainWindow;
import org.example.mandelbrotview.view.StyleFactory;

import java.util.concurrent.CountDownLatch;

/**
 *
 * @author Maurice Amon
 */

public class MandelThread implements Runnable {

    private final Integer MIN_ITERATION = 0;

    private final Integer MAX_ITERATION;

    private GuiFactory guiFactory;

    private PixelRange widthRange;

    private PixelRange heightRange;

    private CountDownLatch countDownLatch;

    public MandelThread(PixelRange widthRange, PixelRange heightRange, CountDownLatch latch, Integer maxIterations) {
        this.widthRange = widthRange;
        this.heightRange = heightRange;
        guiFactory = new StyleFactory();
        this.countDownLatch = latch;
        this.MAX_ITERATION = maxIterations;
    }


    @Override
    public void run() {
        try {
            MandelbrotArray mandelbrotArray = MandelbrotArray.getInstance();
            for (int x = widthRange.getStartIndex(); x < widthRange.getEndIndex(); x++) {
                for (int y = heightRange.getStartIndex(); y < heightRange.getEndIndex(); y++) {
                    int width = x;
                    int height = y;
                    Double compX = -2.5 + ((x / 1199.0) * (1.0 - (-2.5)));
                    Double compY = 1.5 - ((y / 699.0) * (1.5 - (-1.5)));
                    Complex z = new Complex(compX, compY);
                    MandelbrotTuple tuple = isInMandelbrotSet(z);
                    mandelbrotArray.setPixelValue(width, height, tuple.getColor());
                }
                Thread.sleep(0);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            countDownLatch.countDown();
        }
    }

    private MandelbrotTuple isInMandelbrotSet(Complex z0) {
        Complex z = z0;
        for(int t = 0; t < MAX_ITERATION; t++) {

            if(z.abs() >= 2.0) {
                return new MandelbrotTuple(convertIterationToSmoothColor(z, t), false);
            }
            z = z.multiply(z).add(z0);
        }
        return new MandelbrotTuple(convertIterationToSmoothColor(z, MAX_ITERATION), true);
    }

    private Integer convertIterationToSmoothColor(Complex z, int iteration) {
        Double absoluteValue = Math.sqrt((z.getReal()*z.getReal()) + (z.getImaginary()*z.getImaginary()));
        Double value = (iteration + 1) - (Math.log(Math.log(absoluteValue)) / Math.log(2));
        // Normalization of the smooth value ..
        Integer t = (int)Math.floor(255*(Math.sqrt((value - MIN_ITERATION) / (MAX_ITERATION - MIN_ITERATION))));
        // for rather small max-iteration the value might be > 255, which is why we've to map it to 255.
        if(t > 255) {
            t = 255;
        }
        return t;
    }
}
