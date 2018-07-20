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

import static org.opencv.imgcodecs.Imgcodecs.*;
import static org.opencv.imgproc.Imgproc.*;


/**
 *
 * Resources:
 * - https://pastebin.com/H46wuXWn
 * - sudo apt install tesseract-ocr
 */
public class ProgramCopy {

    static int width;
    static int height;
    static double alpha = 1;
    static double beta = 50;

    private static final int
            CV_MOP_CLOSE = 3,
            CV_THRESH_OTSU = 8,
            CV_THRESH_BINARY = 0,
            CV_ADAPTIVE_THRESH_GAUSSIAN_C  = 1,
            CV_ADAPTIVE_THRESH_MEAN_C = 0,
            CV_THRESH_BINARY_INV  = 1;


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



    public static void main(String[] args) throws Exception {

        // https://www.tutorialspoint.com/opencv/opencv_drawing_rectangle.htm

        OpenCV.loadLocally();

//        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        String filePath = "/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/mb.jpg";

//        OpenCV.loadLibrary();
        Mat image = imread(filePath, CV_LOAD_IMAGE_GRAYSCALE);
//        Mat image = imread(filePath, Imgproc.COLOR_BGR2HSV);
//
        Imgproc.rectangle (
                image,                    //Matrix obj of the image
                new Point(130, 50),        //p1
                new Point(300, 280),       //p2
                new Scalar(0, 0, 255),     //Scalar object for color
                5                          //Thickness of the line
        );



        System.out.println(imwrite("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/res.png",image));
        // remove some noise
//        Imgproc.blur(image, blurredImage, new Size(7, 7));

// convert the frame to HSV
//        Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);
//        Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
//        Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));

//        Imgproc.erode(mask, morphOutput, erodeElement);
//        Imgproc.erode(mask, morphOutput, erodeElement);
//        Imgproc.erode(mask, morphOutput, erodeElement);

//        Imgproc.dilate(mask, morphOutput, dilateElement);
//        Imgproc.dilate(mask, morphOutput, dilateElement);

        // show the partial output


        System.out.println(imwrite("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/res.png",image));
//        cvErode(image,image);
//        cvDilate(image,image);
//        cvCanny(image,image,75.0,200.0);
//
//        OpenCV.loadLibrary();
//
        Mat img = new Mat();
        Mat imgGray = new Mat();
        Mat imgGaussianBlur = new Mat();
        Mat imgSobel = new Mat();
        Mat imgThreshold = new Mat();
        Mat imgAdaptiveThreshold = new Mat();
        Mat imgAdaptiveThreshold_forCrop = new Mat();
        Mat imgMoprhological = new Mat();
        Mat imgContours = new Mat();
        Mat imgMinAreaRect = new Mat();
        Mat imgDetectedPlateCandidate = new Mat();
        Mat imgDetectedPlateTrue = new Mat();

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        img = imread(filePath, COLOR_BGR2GRAY);
//        imwrite("changes/st/1_True_Image.png", img);

        Imgproc.cvtColor(img, imgGray, COLOR_BGR2GRAY);
//        imwrite("changes/st/2_imgGray.png", imgGray);

        Imgproc.GaussianBlur(imgGray,imgGaussianBlur, new Size(3, 3),0);
//        Imgcodecs.imwrite("changes/st/3_imgGaussianBlur.png", imgGray);

        Imgproc.Sobel(imgGaussianBlur, imgSobel, -1, 1, 0);
//        Imgcodecs.imwrite("changes/st/4_imgSobel.png", imgSobel);

        Imgproc.threshold(imgSobel, imgThreshold, 0, 255,  CV_THRESH_OTSU + CV_THRESH_BINARY);
        //Imgcodecs.imwrite("changes/st/5_imgThreshold.png", imgThreshold);

        Imgproc.adaptiveThreshold(imgSobel, imgAdaptiveThreshold, 255, CV_ADAPTIVE_THRESH_GAUSSIAN_C, CV_THRESH_BINARY_INV, 75, 35);
        //Imgcodecs.imwrite("changes/st/5_imgAdaptiveThreshold.png", imgAdaptiveThreshold);

        Imgproc.adaptiveThreshold(imgGaussianBlur, imgAdaptiveThreshold_forCrop, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, 99, 4);
        //Imgcodecs.imwrite("changes/st/5_imgAdaptiveThreshold_forCrop.png", imgAdaptiveThreshold_forCrop);

//        Mat element = getStructuringElement(MORPH_RECT, new Size(17, 3));
//        Imgproc.morphologyEx(imgAdaptiveThreshold, imgMoprhological, CV_MOP_CLOSE, element); //или imgThreshold
//        Imgcodecs.imwrite("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/6_imgMoprhologicald.png", imgMoprhological);

        imgContours = imgMoprhological.clone();
        Imgproc.findContours(imgContours,
                contours,
                new Mat(),
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(imgContours, contours, -1, new Scalar(255,0,0));
        //Imgcodecs.imwrite("changes/st/7_imgContours.png", imgContours);

        imgMinAreaRect = img.clone();
        if (contours.size() > 0) {
            for (MatOfPoint matOfPoint : contours) {
                MatOfPoint2f points = new MatOfPoint2f(matOfPoint.toArray());

                RotatedRect box = Imgproc.minAreaRect(points);
                if(checkRatio(box)){
                    Imgproc.rectangle(imgMinAreaRect, box.boundingRect().tl(), box.boundingRect().br(), new Scalar(0, 0, 255));
//                    Core.rectangle(imgMinAreaRect, box.boundingRect().tl(), box.boundingRect().br(), new Scalar(0, 0, 255));
                    imgDetectedPlateCandidate = new Mat(imgAdaptiveThreshold_forCrop, box.boundingRect());
                    if(checkDensity(imgDetectedPlateCandidate))
                        imgDetectedPlateTrue = imgDetectedPlateCandidate.clone();
                }
                else
                    Imgproc.rectangle(imgMinAreaRect, box.boundingRect().tl(), box.boundingRect().br(), new Scalar(0, 255, 0));
//                    Core.rectangle(imgMinAreaRect, box.boundingRect().tl(), box.boundingRect().br(), new Scalar(0, 255, 0));
            }
        }

        //Imgcodecs.imwrite("changes/st/8_imgMinAreaRect.png", imgMinAreaRect);
        //Imgcodecs.imwrite("changes/st/9_imgDetectedPlateCandidate.png", imgDetectedPlateCandidate);
        imwrite("changes/st/10_imgDetectedPlateTrue.png", imgDetectedPlateTrue);

//        File imageFile = new File("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/scrn.png");
//        File imageFile = new File("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/ColorText.png");
//        File imageFile = new File("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources</img.png");
//        File imageFile = new File("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/digits.png");
        File imageFile = new File("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/res.png");

        ITesseract instance = new Tesseract();
        instance.setDatapath("/usr/share/tesseract-ocr/tessdata");
        instance.setLanguage("eng");
        //instance.setTessVariable("tessedit_char_whitelist", "acekopxyABCEHKMOPTXY0123456789");
//        instance.setTessVariable("tessedit_char_whitelist", "ABCDEFJHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-| ,.");
        instance.setTessVariable("tessedit_char_whitelist", "AE4555");
        try {
            String result = instance.doOCR(imageFile);
            System.out.println(result);
        } catch (TesseractException e) {
            System.err.println(e.getMessage());
        }

        Mat imgNext = new Mat();
        imgNext = Imgcodecs.imread("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/res.png");
        Imgcodecs.imwrite("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/True_Image.png", imgNext);

        Mat imgGrayNext = new Mat();
        Imgproc.cvtColor(img, imgGrayNext, Imgproc.COLOR_BGR2GRAY);
        Imgcodecs.imwrite("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/Gray.png", imgGrayNext);

        Mat imgGaussianBlurNext = new Mat();
        Imgproc.GaussianBlur(imgGray,imgGaussianBlurNext,new Size(3, 3),0);
        Imgcodecs.imwrite("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/gaussian_blur.png", imgGaussianBlurNext);

        Mat imgAdaptiveThresholdNext = new Mat();
        Imgproc.adaptiveThreshold(imgGaussianBlurNext, imgAdaptiveThresholdNext, 255, CV_ADAPTIVE_THRESH_MEAN_C ,CV_THRESH_BINARY, 99, 4);
        Imgcodecs.imwrite("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/adaptive_threshold.png", imgAdaptiveThresholdNext);

        Imgproc.Sobel(imgGaussianBlurNext, imgSobel, -1, 1, 0);
        Imgcodecs.imwrite("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/4_imgSobel.png", imgSobel);

        Imgproc.threshold(imgSobel, imgThreshold, 0, 255,  CV_THRESH_OTSU + CV_THRESH_BINARY);
        Imgcodecs.imwrite("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/5_imgThreshold.png", imgThreshold);

        Imgproc.adaptiveThreshold(imgSobel, imgAdaptiveThreshold, 255, CV_ADAPTIVE_THRESH_GAUSSIAN_C, CV_THRESH_BINARY_INV, 75, 35);
        Imgcodecs.imwrite("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/5_imgAdaptiveThreshold.png", imgAdaptiveThreshold);

        Imgproc.adaptiveThreshold(imgGaussianBlur, imgAdaptiveThreshold_forCrop, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, 99, 4);
        Imgcodecs.imwrite("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/5_imgAdaptiveThreshold_forCrop.png", imgAdaptiveThreshold_forCrop);

        Mat element = getStructuringElement(MORPH_RECT, new Size(17, 3));
        Imgproc.morphologyEx(imgAdaptiveThreshold, imgMoprhological, CV_MOP_CLOSE, element); //или imgThreshold
        Imgcodecs.imwrite("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/6_imgMoprhologicald.png", imgMoprhological);

        imgContours = imgMoprhological.clone();
        Imgproc.findContours(imgContours,
                contours,
                new Mat(),
                Imgproc.RETR_LIST,
                Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(imgContours, contours, -1, new Scalar(255,0,0));
        Imgcodecs.imwrite("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/7_imgContours.png", imgContours);


        imgMinAreaRect = img.clone();
        if (contours.size() > 0) {
            for (MatOfPoint matOfPoint : contours) {
                MatOfPoint2f points = new MatOfPoint2f(matOfPoint.toArray());

                RotatedRect box = Imgproc.minAreaRect(points);
                if(checkRatio(box)){
                    Imgproc.rectangle(imgMinAreaRect, box.boundingRect().tl(), box.boundingRect().br(), new Scalar(0, 0, 255));
                    imgDetectedPlateCandidate = new Mat(imgAdaptiveThreshold_forCrop, box.boundingRect());
                    if(checkDensity(imgDetectedPlateCandidate))
                        imgDetectedPlateTrue = imgDetectedPlateCandidate.clone();
                }
                else
                    Imgproc.rectangle(imgMinAreaRect, box.boundingRect().tl(), box.boundingRect().br(), new Scalar(0, 255, 0));
            }
        }

        Imgcodecs.imwrite("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/8_imgMinAreaRect.png", imgMinAreaRect);
        Imgcodecs.imwrite("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/9_imgDetectedPlateCandidate.png", imgDetectedPlateCandidate);
        Imgcodecs.imwrite("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/10_imgDetectedPlateTrue.png", imgDetectedPlateTrue);

        File imageFileNext = new File("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/resources/10_imgDetectedPlateTrue.png");
        ITesseract instanceNext = new Tesseract();
        instanceNext.setDatapath("/usr/share/tesseract-ocr/tessdata");
        instanceNext.setLanguage("eng");
        instanceNext.setTessVariable("tessedit_char_whitelist", "acekopxyABCEHKMOPTXY0123456789");
        String result = instanceNext.doOCR(imageFileNext);
        System.out.println(">>"+result);


    }
}
