import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * Resources:
 * - https://pastebin.com/H46wuXWn
 * - sudo apt install tesseract-ocr
 * http://answers.opencv.org/question/69135/how-can-i-draw-rectangle-with-matofkeypoint-for-text-detection-java/
 */
public class Program_ {

    private static final int
            CV_MOP_CLOSE = 3,
            CV_THRESH_OTSU = 8,
            CV_THRESH_BINARY = 0,
            CV_ADAPTIVE_THRESH_GAUSSIAN_C  = 1,
            CV_ADAPTIVE_THRESH_MEAN_C = 0,
            CV_THRESH_BINARY_INV  = 1;
    static String path = "/home/uadmin/IdeaProjects/NumbersScanner02/src/main/program/";

    public static boolean checkRatio(RotatedRect candidate) {
        double error = 0.3;
        //Spain car plate size: 52x11 aspect 4,7272
        //Russian 12x2 (52cm / 11.5)
        //double aspect = 52/11.5;
        double aspect = 6;
        int min = 15 * (int)aspect * 15;
        int max = 125 * (int)aspect * 125;
        //Get only patchs that match to a respect ratio.
        double rmin= aspect - aspect*error;
        double rmax= aspect + aspect*error;
        double area= candidate.size.height * candidate.size.width;
        float r= (float)candidate.size.width / (float)candidate.size.height;
        if(r<1)
            r= 1/r;
        if(( area < min || area > max ) || ( r < rmin || r > rmax )){
            return false;
        }else{
            return true;
        }
    }

    public static boolean checkDensity(Mat candidate) {
        float whitePx = 0;
        float allPx = 0;
        whitePx = Core.countNonZero(candidate);
        allPx = candidate.cols() * candidate.rows();
        //System.out.println(whitePx/allPx);
        if (0.62 <= whitePx/allPx)
            return true;
        else
            return false;
    }

    public static void main(String[] args) throws InterruptedException {



//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        OpenCV.loadLocally();
        try {
//            Mat source = Imgcodecs.imread(path+"mb.jpg", Imgcodecs.IMREAD_GRAYSCALE);
            Mat source = Imgcodecs.imread(path+"mb.jpg", Imgcodecs.CV_LOAD_IMAGE_ANYCOLOR);
//            Mat source = Imgcodecs.imread(path+"vaz_1118__213972441bx.jpg", Imgcodecs.IMREAD_GRAYSCALE);
//            Imgcodecs.imwrite(path + "1_cvtColor.png", source);
//            Mat destination = Imgcodecs.imread(path+"mb.jpg", COLOR_RGB2GRAY);
            Mat destination = new Mat(source.rows(), source.cols(), source.type());
            Mat blurredImage = new Mat();
            Mat gray = new Mat();
            Mat hsvImage = new Mat();
            Mat thresh = new Mat();
            Mat mask = new Mat();
            Mat morphOutput = new Mat();
            Mat hierarchy = new Mat();
            List<MatOfPoint> contours = new ArrayList<>();

// remove some noise
            Imgproc.blur(source, blurredImage, new Size(7, 7));
//            Imgcodecs.imwrite(path + "-1_blure.png", blurredImage);
// convert the frame to HSV
//            Imgproc.cvtColor(source, hsvImage, Imgproc.COLOR_BGR2HSV);
//            Imgcodecs.imwrite(path + "0_hsvImage.png", hsvImage);
//            Imgproc.cvtColor(source, gray, Imgproc.COLOR_RGB2GRAY);
//            Imgproc.threshold(gray,thresh, 127,255,1);
//            Imgproc.findContours(thresh,contours,hierarchy,Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
////            Imgproc.findContours(gray, contours, hierarchy, 5, Imgproc.RETR_LIST);
//            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
//            {
//                Imgproc.drawContours(source, contours, idx, new Scalar(250, 250, 0));
//            }
            mask = source;
            Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(48, 24));
            Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 12));

            Imgproc.erode(mask, morphOutput, erodeElement);
            Imgproc.erode(mask, morphOutput, erodeElement);

            Imgproc.dilate(mask, morphOutput, dilateElement);
            Imgproc.dilate(mask, morphOutput, dilateElement);
            Imgproc.cvtColor(morphOutput, gray, Imgproc.COLOR_RGB2GRAY);
            Imgcodecs.imwrite(path + "Ha_.png", gray);
            Imgproc.threshold(gray,thresh, 127,255,1);
            Imgcodecs.imwrite(path + "Ha__.png", thresh);
            Imgproc.findContours(morphOutput, contours, hierarchy, Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
            {
                Imgproc.drawContours(source, contours, idx, new Scalar(250, 250, 0));
            }
            Imgcodecs.imwrite(path + "Ha.png", source);





//            // find contours
//            Mat mask1 = new Mat( new Size( source.cols(), source.rows() ), CvType.CV_8UC1 );
//            //mask1.setTo( new Scalar( 0.0 ) );
//            Imgproc.findContours(mask1, contours, hierarchy, Imgproc.MORPH_RECT, Imgproc.CHAIN_APPROX_SIMPLE);
//            // if any contour exist...
//            if (hierarchy.size().height > 0 && hierarchy.size().width > 0)
//            {
//                // for each contour, display it in blue
//                for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
//                {
//                    Imgproc.drawContours(source, contours, idx, new Scalar(250, 250, 0));
//                }
//            }
//            Imgcodecs.imwrite(path + "-1_cvtColor.png", source);

//            Imgproc.threshold(destination, destination, 30, 255, Imgproc.THRESH_BINARY_INV);
//            Imgproc.threshold(destination, destination, 30, 255, Imgproc.THRESH_BINARY_INV);
//            Imgproc.blur(destination, destination, new Size(3, 3));
//            Imgproc.GaussianBlur(destination, destination, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT);
//            Imgproc.adaptiveThreshold(destination, destination, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 40);
//            Imgproc.threshold(destination, destination, 0, 255, Imgproc.THRESH_BINARY);
//            Imgproc.equalizeHist(destination, destination);
//            Imgcodecs.imwrite(path + "0_cvtColor.png", destination);
            Imgproc.blur(destination, destination, new Size(2, 2));
            Imgproc.adaptiveThreshold(destination, destination, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 40);
            Imgproc.threshold(destination, destination, 0, 255, Imgproc.THRESH_BINARY);
            FeatureDetector fd = FeatureDetector.create(FeatureDetector.MSER);
            MatOfKeyPoint mokp = new MatOfKeyPoint();
            Mat edges = new Mat();
            //for mask
            Imgproc.Canny(destination, edges, 400, 450);
            fd.detect(destination, mokp, edges);
            Imgcodecs.imwrite(path + "edges.png", edges);
            //for drawing keypoints
            Features2d.drawKeypoints(destination, mokp, destination);





            Imgcodecs.imwrite(path + "kp.png", destination);
//            Imgproc.GaussianBlur(destination, destination, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT);

            Imgcodecs.imwrite(path + "1_cvtColor.png", destination);
//            Imgproc.equalizeHist(destination, destination);
//            Imgcodecs.imwrite(path + "2_.png", destination);
//            Imgproc.GaussianBlur(destination, destination, new Size(5, 5), 0, 0, Core.BORDER_DEFAULT);
//            Imgproc.blur(destination, destination, new Size(2, 2));
//            Imgcodecs.imwrite(path + "3_.png", destination);
//
//            Imgproc.Canny(destination, destination, 50, 100);
//            Imgcodecs.imwrite(path + "4_.png", destination);
//            Imgproc.adaptiveThreshold(destination, destination, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 40);
//            Imgcodecs.imwrite(path + "5_.png", destination);
//            Imgproc.threshold(destination, destination, 0, 255, Imgproc.THRESH_BINARY);
//            Imgcodecs.imwrite(path + "6_.png", destination);

            if (destination != null) {
                Mat lines = new Mat();
                Imgproc.HoughLinesP(destination, lines, 1, Math.PI / 180, 50, 30, 10);
                Mat houghLines = new Mat();
                houghLines.create(destination.rows(), destination.cols(), CvType.CV_8UC1);

                //Drawing lines on the image
                for (int i = 0; i < lines.cols(); i++) {
                    double[] points = lines.get(0, i);
                    double x1, y1, x2, y2;
                    x1 = points[0];
                    y1 = points[1];
                    x2 = points[2];
                    y2 = points[3];

                    Point pt1 = new Point(x1, y1);
                    Point pt2 = new Point(x2, y2);

                    //Drawing lines on an image
                    Imgproc.line(source, pt1, pt2, new Scalar(0, 0, 255), 4);
                }
            }

            Imgcodecs.imwrite(path + "rectangle_houghtransform.jpg", source);

        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        }


        File imageFile = new File(path + "1_cvtColor.png");
        ITesseract instance = new Tesseract();
        instance.setDatapath("/usr/share/tesseract-ocr/tessdata");
        instance.setLanguage("ukr");
        instance.setTessVariable("tessedit_char_whitelist", "АБВЕМНРСТ1234567890");
        try {
            String result = instance.doOCR(imageFile);
            System.out.println(result);
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }
    }


    private static void findRectangle(Mat src) throws Exception {
        Mat blurred = src.clone();
        Imgproc.medianBlur(src, blurred, 9);

        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U), gray = new Mat();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        List<Mat> blurredChannel = new ArrayList<Mat>();
        blurredChannel.add(blurred);
        List<Mat> gray0Channel = new ArrayList<Mat>();
        gray0Channel.add(gray0);

        MatOfPoint2f approxCurve;

        double maxArea = 0;
        int maxId = -1;

        for (int c = 0; c < 3; c++) {
            int ch[] = { c, 0 };
            Core.mixChannels(blurredChannel, gray0Channel, new MatOfInt(ch));

            int thresholdLevel = 1;
            for (int t = 0; t < thresholdLevel; t++) {
                if (t == 0) {
                    Imgproc.Canny(gray0, gray, 10, 20, 3, true); // true ?
                    Imgproc.dilate(gray, gray, new Mat(), new Point(-1, -1), 1); // 1
                    // ?
                } else {
                    Imgproc.adaptiveThreshold(gray0, gray, thresholdLevel,
                            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                            Imgproc.THRESH_BINARY,
                            (src.width() + src.height()) / 200, t);
                }

                Imgproc.findContours(gray, contours, new Mat(),
                        Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

                for (MatOfPoint contour : contours) {
                    MatOfPoint2f temp = new MatOfPoint2f(contour.toArray());

                    double area = Imgproc.contourArea(contour);
                    approxCurve = new MatOfPoint2f();
                    Imgproc.approxPolyDP(temp, approxCurve,
                            Imgproc.arcLength(temp, true) * 0.02, true);

                    if (approxCurve.total() == 4 && area >= maxArea) {
                        double maxCosine = 0;

                        List<Point> curves = approxCurve.toList();
                        for (int j = 2; j < 5; j++) {

                            double cosine = Math.abs(angle(curves.get(j % 4),
                                    curves.get(j - 2), curves.get(j - 1)));
                            maxCosine = Math.max(maxCosine, cosine);
                        }

                        if (maxCosine < 0.3) {
                            maxArea = area;
                            maxId = contours.indexOf(contour);
                        }
                    }
                }
            }
        }

        if (maxId >= 0) {
            Imgproc.drawContours(src, contours, maxId, new Scalar(255, 0, 0,
                    .8), 8);

        }
        Imgcodecs.imwrite(path + "rectengle.jpg", src);
    }

    private static double angle(Point p1, Point p2, Point p0) {
        double dx1 = p1.x - p0.x;
        double dy1 = p1.y - p0.y;
        double dx2 = p2.x - p0.x;
        double dy2 = p2.y - p0.y;
        return (dx1 * dx2 + dy1 * dy2)
                / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2)
                + 1e-10);
    }
}

