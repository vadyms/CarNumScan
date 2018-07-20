import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.getStructuringElement;


/**
 *
 * Resources:
 * - https://pastebin.com/H46wuXWn
 * - sudo apt install tesseract-ocr
 * http://answers.opencv.org/question/69135/how-can-i-draw-rectangle-with-matofkeypoint-for-text-detection-java/
 */
public class ProgramWorking {

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

//        http://installion.co.uk/ubuntu/xenial/universe/t/tesseract-ocr/uninstall/index.html
//        https://lucacerone.net/2017/install-tesseract-3-0-5-in-ubuntu-16-04/
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        OpenCV.loadLocally();
        try {
            String image = "mb.jpg";
//            String image = "vaz_1118.jpg";
//            String image = "vaz_2101.jpg";
//            String image = "mini_cooper.jpg";
//            String image = "chevrolet_aveo.jpg";
//            String image = "RegVideo01.png";
//            String image = "RegVideo02.png";
//            String image = "RegVideo03.png";
//            String image = "RegVideo04.png";
//            String image = "RegVideo05.png";

//            String image = "digits.png";
//            String image = "Screenshot_e.png";

            Mat source = Imgcodecs.imread(path + image, Imgcodecs.CV_LOAD_IMAGE_ANYCOLOR);
//            Imgcodecs.imwrite(path + "1_color.png", source);

            Mat imgGray = new Mat();
            Imgproc.cvtColor(source, imgGray, Imgproc.COLOR_RGB2GRAY);
//            Imgcodecs.imwrite(path + "2_imgGray.png", imgGray);

            Mat imgHighContrast = new Mat();
            Imgproc.equalizeHist(imgGray, imgHighContrast);
//            Imgcodecs.imwrite(path+"3_imgHighContrast.png", imgHighContrast);

            Mat imgGaussianBlur = new Mat();
//            Imgproc.GaussianBlur(imgGray, imgGaussianBlur, new Size(1, 1), 0);
            Imgproc.GaussianBlur(imgGray, imgGaussianBlur, new Size(3, 3), 0);
//            Imgproc.GaussianBlur(imgGray, imgGaussianBlur, new Size(5, 5), 0);
//            Imgcodecs.imwrite(path + "4_imgGaussianBlur.png", imgGaussianBlur);

            Mat imgSobel = new Mat();
            Imgproc.Sobel(imgGaussianBlur, imgSobel, -1, 1, 0);
//            Imgcodecs.imwrite(path + "5_imgSobel.png", imgSobel);

            Mat imgThreshold= new Mat();
            Imgproc.threshold(imgSobel,imgThreshold, 0, 255, CV_THRESH_OTSU+CV_THRESH_BINARY);
//            Imgcodecs.imwrite(path + "6_imgThreshold.png", imgThreshold);

            Mat imgThresholdMorph= new Mat();
            Mat element = getStructuringElement(MORPH_RECT, new Size(17, 3));
            Imgproc.morphologyEx(imgThreshold, imgThresholdMorph, CV_MOP_CLOSE, element);
//            Imgcodecs.imwrite(path + "7_imgThresholdMorph.png", imgThresholdMorph);

            //Find contours of possibles plates
            Mat hierarchy= new Mat();
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(imgThresholdMorph,contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0])
            {
                //Imgproc.drawContours(source, contours, idx, new Scalar(250, 250, 0));
//                Imgproc.drawContours(source, contours, idx, new Scalar(250, 250, 0),0);
            }
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            Mat imgRegions = source.clone();
            //For each contour found
            for (int i=0; i<contours.size(); i++)
            {
                //Convert contours(i) from MatOfPoint to MatOfPoint2f
                MatOfPoint2f contour2f = new MatOfPoint2f( contours.get(i).toArray() );
                //Processing on mMOP2f1 which is in type MatOfPoint2f
                double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
                Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

                //Convert back to MatOfPoint
                MatOfPoint points = new MatOfPoint( approxCurve.toArray() );

                // Get bounding rect of contour
                Rect rect = Imgproc.boundingRect(points);

                // draw enclosing rectangle (all same color, but you could use variable i to make them unique)
                //Imgproc.rectangle(source, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), (255, 0, 0, 255), 3);
                Imgproc.rectangle(imgRegions, rect.tl(), rect.br(), new Scalar(255, 255, 0),1, 8,0);

                // Adding Text
                Imgproc.putText(
                        imgRegions,                          // Matrix obj of the image
                        String.valueOf(i),          // Text to be added
                        new Point(rect.tl().x, rect.br().y),               // point
                        Core.FONT_HERSHEY_SIMPLEX ,      // front face
                        0.4,                               // front scale
                        new Scalar(255, 0, 0),             // Scalar object for color
                        1                                // Thickness
                );

//                Mat cropped = new Mat(source, rect);
                Mat cropped = new Mat(imgGray, rect);

//                threshold( cropped, cropped, 50,255,THRESH_BINARY );
                if (cropped.size().width>9&&(cropped.size().height>9)
                        &&cropped.size().width<200&&(cropped.size().height<80&&
                        cropped.size().width>cropped.size().height*1.2)) {
                    String resultPath = path + i +"Ha.png";
                    Mat im_bw = new Mat();
//                    Imgproc.equalizeHist(cropped, im_bw);
//                    Imgproc.equalizeHist(im_bw, im_bw);
//                    Imgproc.threshold(im_bw,im_bw, 128, 255, CV_THRESH_OTSU+CV_THRESH_BINARY);
                    Imgcodecs.imwrite(resultPath, cropped);

                    File imageFile = new File(resultPath);
                    ITesseract instance = new Tesseract();
                    instance.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
                    instance.setLanguage("ukr");

//                    instance.setTessVariable("tessedit_char_whitelist", "АБВГДЕЗИІКЛМНОПРСТУФХЦЧЯ1234567890");
//                    instance.setLanguage("eng");
//                    instance.setTessVariable("tessedit_char_whitelist", "ABCDEFJHIJKLMNOPQRSTUVWXYZ0123456789");
//                    instance.setTessVariable("tessedit_char_whitelist", "abcdefjhijklmnopqrstuvwxyzABCDEFJHIJKLMNOPQRSTUVWXYZ0123456789");
//                    instance.setTessVariable("tessedit_char_blacklist", "abcdefjhijklmnopqrstuvwxyzABCDEFJHIJKLMNOPQRSTUVWXYZ");
//                    instance.setTessVariable("tessedit_char_whitelist", "0123456789");
//                    instance.setLanguage("equ");

                    try {
                        String result = instance.doOCR(imageFile);
                        System.out.println(i +":("+ cropped.size().width+"x"+cropped.size().height + ") - >> " + result);
                    } catch (TesseractException e) {
//                        System.err.println(e.getMessage());
                    }
                }
            }


            Imgcodecs.imwrite(path + "_Ha.png", imgRegions);

//            File imageFile = new File(path + "1_cvtColor.png");
//            ITesseract instance = new Tesseract();
//            instance.setDatapath("/usr/share/tesseract-ocr/tessdata");
//            instance.setLanguage("ukr");
//            instance.setTessVariable("tessedit_char_whitelist", "АБВЕМНРСТ1234567890");
//            try {
//                String result = instance.doOCR(imageFile);
//                System.out.println(result);
//            } catch (TesseractException e) {
//                System.err.println(e.getMessage());
//            }

        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
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

