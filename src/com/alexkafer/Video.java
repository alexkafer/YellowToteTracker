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
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class Video {

	public static void main(String[] args) throws InterruptedException {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		MatWindow window = new MatWindow("Camera");
		MatWindow threshWindow = new MatWindow("Thresh");

		//VideoCapture camera = new VideoCapture(0);
		
		//while (!camera.open("10.25.26.23/mjpg/1/video.mjpg"))
			//Thread.sleep(100);

		JFrame jFrame = new JFrame("Options");
		jFrame.setSize(200, 200);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setLayout(new FlowLayout());

		JPanel panel = new JPanel();

		JSlider hueSlider = new JSlider(0, 255, 150);
		panel.add(hueSlider);

		JSlider satSlider = new JSlider(0, 255, 168);
		panel.add(satSlider);

		JSlider valSlider = new JSlider(10, 255, 172);
		panel.add(valSlider);

		JSlider tolSlider = new JSlider(0, 255, 79);
		panel.add(tolSlider);

		jFrame.setContentPane(panel);
		jFrame.setVisible(true);

		while (true) {
			//Mat original = new Mat();
			Mat original = Highgui.imread("image20.jpg");
			//if (!camera.read(original))
				//continue;
			
			

			Mat threshImage = new Mat();

			Imgproc.cvtColor(original, threshImage, Imgproc.COLOR_RGB2HSV);

			int hue = hueSlider.getValue();
			int satu = satSlider.getValue();
			int valu = valSlider.getValue();
			int tol = tolSlider.getValue();

			Core.inRange(
					threshImage,
					new Scalar(Math.max(hue - tol, 0), Math.max(satu - tol, 0),
							Math.max(valu - tol, 0)),
					new Scalar(Math.min(hue + tol, 179), Math.min(satu + tol,
							255), Math.min(valu + tol, 255)), threshImage);

			threshWindow.setImage(threshImage);

			List<MatOfPoint> yellowTotes = new ArrayList<MatOfPoint>();

			Imgproc.findContours(threshImage, yellowTotes, new Mat(),
					Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

			double nearestAngle = 0;
			boolean flag = false;
			
			for (int i = 0; i < yellowTotes.size(); i++) {
				MatOfPoint contour = yellowTotes.get(i);
				if (contour.width() * contour.height() > 200) {
					//yellowTotes.remove(i);
					Rect yellowToteRect = Imgproc.boundingRect(contour);
					
					Core.rectangle(original, yellowToteRect.tl(), yellowToteRect.br(),
							new Scalar(0, 255, 255));
					
					Point toteCenter = new Point(
							(yellowToteRect.tl().x + yellowToteRect.br().x) / 2,
							(yellowToteRect.tl().y + yellowToteRect.br().y) / 2);
					Core.circle(original, toteCenter, 2, new Scalar(0, 0, 255), 4);

					String string = "TargetFound at X:"
							+ (yellowToteRect.tl().x + yellowToteRect.br().x) / 2 + "Y:"
							+ (yellowToteRect.tl().y + yellowToteRect.br().y) / 2;
					Core.putText(original, string, new Point(200,
							original.size().height - 10), Core.FONT_HERSHEY_PLAIN,
							1, new Scalar(0, 0, 255));

					double angularDifference = ((toteCenter.x - original.width() / 2) / yellowToteRect.width) * 67.5d;

					if (!flag || nearestAngle > angularDifference) {
						nearestAngle = angularDifference;
						flag = true;
					}
					
					String dist = "Angular Difference: " + angularDifference
							+ " degrees";
					Core.putText(original, dist, toteCenter, Core.FONT_HERSHEY_PLAIN,
							1, new Scalar(0, 255, 0));
					
					String area = "Area: " + contour.width() * contour.height();
					Core.putText(original, area, shiftPoint(toteCenter, 0, -20), Core.FONT_HERSHEY_PLAIN,
							1, new Scalar(0, 255, 0));
				}
				
			}

			
			
			String dist = "Nearest Angle: " + nearestAngle
					+ " degrees";
			Core.putText(original, dist, new Point(300, 10), Core.FONT_HERSHEY_PLAIN,
					1, new Scalar(0, 255, 0));

			// Draw blue (255, 0, 0) line down the middle of the original image
			Core.line(original, new Point(original.width() / 2, 0), new Point(
					original.width() / 2, original.height()), new Scalar(255,
					0, 0));

			// Update the image on the window
			window.setImage(original);
			
			
			// Output values
			System.out.println("Numb of Totes: " + yellowTotes.size());
			System.out.println("Hue: " + hue + " Sat: " + satu + " Value "
					+ valu + " Tol: " + tol);

		}
	}
	
	private static Point shiftPoint(Point original, int xDistance, int yDistance) {
		return new Point(original.x+xDistance, original.y+yDistance);
	}
}