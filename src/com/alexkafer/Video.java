package com.alexkafer;

import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class Video {

	public static void main(String[] args) throws InterruptedException {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		MatWindow window = new MatWindow("Camera");
		MatWindow threshWindow = new MatWindow("Thresh");

		VideoCapture camera = new VideoCapture();
		
		while (!camera.open("10.25.26.23/mjpg/1/video.mjpg"))
			Thread.sleep(100);

		JFrame jFrame = new JFrame("Options");
		jFrame.setSize(200, 200);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setLayout(new FlowLayout());

		JPanel panel = new JPanel();

		JSlider hueSlider = new JSlider(0, 255, 23);
		panel.add(hueSlider);

		JSlider satSlider = new JSlider(0, 255, 126);
		panel.add(satSlider);

		JSlider valSlider = new JSlider(10, 255, 233);
		panel.add(valSlider);

		JSlider tolSlider = new JSlider(0, 255, 74);
		panel.add(tolSlider);

		jFrame.setContentPane(panel);
		jFrame.setVisible(true);

		while (true) {
			Mat original = new Mat();

			if (!camera.read(original)) {
				continue;
			}

			Mat image = new Mat();

			Imgproc.cvtColor(original, image, Imgproc.COLOR_RGB2HSV);

			int hue = hueSlider.getValue();
			int satu = satSlider.getValue();
			int valu = valSlider.getValue();
			int tol = tolSlider.getValue();

			Core.inRange(
					image,
					new Scalar(Math.max(hue - tol, 0), Math.max(satu - tol, 0),
							Math.max(valu - tol, 0)),
					new Scalar(Math.min(hue + tol, 179), Math.min(satu + tol,
							255), Math.min(valu + tol, 255)), image);

			threshWindow.setImage(image);

			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

			Imgproc.findContours(image, contours, new Mat(),
					Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

			MatOfPoint largest = null;

			for (MatOfPoint contour : contours) {
				if (largest == null) {
					if (contour.width() * contour.height() > 400)
						largest = contour;
				} else if (contour.width() * contour.height() > largest.width()
						* largest.height()) {
					largest = contour;
				}
			}

			if (largest != null) {

				Rect yellowTote = Imgproc.boundingRect(largest);
				Core.rectangle(original, yellowTote.tl(), yellowTote.br(),
						new Scalar(0, 255, 255));
				Point toteCenter = new Point(
						(yellowTote.tl().x + yellowTote.br().x) / 2,
						(yellowTote.tl().y + yellowTote.br().y) / 2);
				Core.circle(original, toteCenter, 2, new Scalar(0, 0, 255), 4);

				String string = "TargetFound at X:"
						+ (yellowTote.tl().x + yellowTote.br().x) / 2 + "Y:"
						+ (yellowTote.tl().y + yellowTote.br().y) / 2;
				Core.putText(original, string, new Point(200,
						original.size().height - 10), Core.FONT_HERSHEY_PLAIN,
						1, new Scalar(0, 0, 255));

				double angularDifference = ((toteCenter.x - original.width() / 2) / yellowTote.width) * 67.5d;

				String dist = "Angular Difference: " + angularDifference
						+ " degrees";
				Core.putText(original, dist, new Point(200,
						original.size().height - 30), Core.FONT_HERSHEY_PLAIN,
						1, new Scalar(0, 255, 0));

			}

			Core.line(original, new Point(original.width() / 2, 0), new Point(
					original.width() / 2, original.height()), new Scalar(255,
					0, 0));

			window.setImage(original);
			System.out.println("Numb: " + contours.size());
			System.out.println("Hue: " + hue + " Sat: " + satu + " Value "
					+ valu + " Tol: " + tol);

		}
	}
}