import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import nu.pattern.OpenCV;
import org.apache.commons.io.FileUtils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;

import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.getStructuringElement;


/**
 *
 * Resources:
 * - https://pastebin.com/H46wuXWn
 * - sudo apt install tesseract-ocr
 * - http://answers.opencv.org/question/69135/how-can-i-draw-rectangle-with-matofkeypoint-for-text-detection-java/
 * - http://installion.co.uk/ubuntu/xenial/universe/t/tesseract-ocr/uninstall/index.html
 * - https://lucacerone.net/2017/install-tesseract-3-0-5-in-ubuntu-16-04/
 *
 */
public class Program {

    private static final int
            CV_MOP_CLOSE = 3,
            CV_THRESH_OTSU = 8,
            CV_THRESH_BINARY = 0,
            CV_ADAPTIVE_THRESH_GAUSSIAN_C  = 1,
            CV_ADAPTIVE_THRESH_MEAN_C = 0,
            CV_THRESH_BINARY_INV  = 1;
    //static String path = "/home/uadmin/IdeaProjects/NumbersScanner02/src/main/program/";
    static String resultPath = "/home/uadmin/IdeaProjects/NumbersScanner02/src/main/result_data/";

    public static void main(String[] args) throws IOException {
        consoleTimestamp();

//        File folder = new File("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/input_images");
//      ffmpeg -i inputFile -vcodec copy -acodec copy -ss 00:09:23 -to 00:25:33 outputFile
        FFmpeg fFmpeg1 = new FFmpeg();
        FFmpegBuilder fFmpegBuilder1 =
                new FFmpegBuilder()
//                        .addInput("/home/uadmin/Downloads/720p.mp4")
                        .addInput("/home/uadmin/Downloads/20180618_180540.mp4")
                        .setStartOffset(60, TimeUnit.SECONDS)
                        .addOutput(
                                new FFmpegOutputBuilder()
                                        .setFilename("/home/uadmin/Downloads/720p_.mp4")
                                        .setDuration(20,TimeUnit.MINUTES)
                        );
        fFmpeg1.run(fFmpegBuilder1);


        FFmpeg fFmpeg = new FFmpeg();
        FFmpegBuilder fFmpegBuilder =
                new FFmpegBuilder()
                        .addInput("/home/uadmin/Downloads/720p_.mp4")
                        .addOutput(
                                new FFmpegOutputBuilder()
                                        .setFilename("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/reg_video/out_%4d.jpg")

                        );
        fFmpeg.run(fFmpegBuilder);

        //ffmpeg -i /home/uadmin/Downloads/720p.mp4 /home/uadmin/IdeaProjects/NumbersScanner02/src/main/reg_video/out%4d.jpg




        File folder = new File("/home/uadmin/IdeaProjects/NumbersScanner02/src/main/reg_video");
        FileUtils.cleanDirectory(new File(resultPath));
        File[] listOfFiles = folder.listFiles();
        for (File file:listOfFiles) {
            processImage(file, resultPath);
        }
        consoleTimestamp();
    }

    private static void consoleTimestamp(){
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        System.out.println("[" + strDate + "] ");
    }

    private static void processImage(File fileFullPath, String resultFolder) {
        OpenCV.loadLocally();
        try {

            Mat source = Imgcodecs.imread(fileFullPath.getAbsolutePath(), Imgcodecs.CV_LOAD_IMAGE_ANYCOLOR);
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
                        cropped.size().width>cropped.size().height*1.2&&
                        cropped.size().width*cropped.size().height>600
                )) {
                    String resultImage = resultFolder +
                            cropped.size().width+"x"+
                            cropped.size().height+"_"+
                            fileFullPath.getName().replaceFirst("[.][^.]+$", "")+"_"+
                            i + ".png";
                    Mat im_bw = new Mat();
//                    Imgproc.equalizeHist(cropped, im_bw);
//                    Imgproc.equalizeHist(im_bw, im_bw);
//                    Imgproc.threshold(im_bw,im_bw, 128, 255, CV_THRESH_OTSU+CV_THRESH_BINARY);
                    Imgcodecs.imwrite(resultImage, cropped);
                    File imageFile = new File(resultImage);
                    ITesseract instance = new Tesseract();
                    instance.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
                    instance.setLanguage("ukr");

                    try {
                        String result = instance.doOCR(imageFile);
                        String resultText = resultFolder +
                                fileFullPath.getName().replaceFirst("[.][^.]+$", "")+"_"+ i + ".txt";
//                        String patternString = "(.*)(\\w[2])(\\s)(\\w*)(\\s)(\\w[2])(.*)";
                        String patternString = ".*\\d{2}.*";
                        Pattern pattern = Pattern.compile(patternString);
                        Matcher matcher = pattern.matcher(result);
                        //System.out.println(matcher.matches());
                        if (matcher.find() && result.length()>=4 &&
                                result.chars().filter(ch->ch == ':').count()==0) {
                            try (PrintWriter out = new PrintWriter(resultText)) {
                                out.println(result);
                            }
                        } else {
                            imageFile.delete();
                        }
                        System.out.println(fileFullPath.getName()+" " +
                                imageFile.getName() + " " +
                                i +
                                ":(" + cropped.size().width+"x"+cropped.size().height + ") - >> " +
                                result);
                    } catch (TesseractException e) {
//                        System.err.println(e.getMessage());
                    }
                }
            }
//            Imgcodecs.imwrite(resultFolder +
//                    fileFullPath.getName().replaceFirst("[.][^.]+$", "")+"_"+ ".png", imgRegions);
        } catch (Exception e) {
//            System.out.println("error: " + e.getMessage());
        }
    }
}

