package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class Main extends Application {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
    static String imgSavePath = "F:/";
    
    static Controller ctrl = new Controller();
    
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // ctrl.setHook();
        
        
        Image image = new Image("file:///F:/01.png");
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        
        Runtime run = Runtime.getRuntime();
        String path = new String("\"C:\\Program Files (x86)\\Tencent\\QQ\\Bin\\qq.exe\"");
        
        Process process = run.exec("cmd.exe /k " + path);
        
        checkProcessStart();
        
        //Thread.sleep(500);
        
        double[] posArr = ctrl.getQQPos();
        System.out.println("LINE53 RETURN X: " + posArr[0] + " Y: " + posArr[1]);
        Point point = new Point(posArr[0], posArr[1]);
        
        try {
            Robot robot = new Robot();
            
            //String saveName = cheers(robot);
            //Point point = loadPic(saveName);
            
            //System.out.println("LINE53 robotPosx: " + point.x + " robotPosY: " + point.y);
            
            doRobot(robot, point);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        
        
        BufferedReader br = new BufferedReader(new InputStreamReader(process
                .getInputStream()));
        String msg = null;
        while ((msg = br.readLine()) != null) {
            System.out.println("LINE66 完成");
        }
        
        
        /*
        StackPane root = new StackPane();
        root.getChildren().add(imageView);
        Scene scene = new Scene(root, 600, 600);
        primaryStage.setTitle("Image Read Test");
        primaryStage.setScene(scene);
        primaryStage.show();
       */
    }
    
    
    public static void main(String[] args) {
        ctrl.sayHello();
        launch(args);
    }
    
    //检测进程启动
    private static void checkProcessStart() {
        Boolean startFlag = false;
        
        while (startFlag != true) {
            String line;
            String pidInfo = "";
            Process processQQ = null;
            try {
                processQQ = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader input = new BufferedReader(new InputStreamReader(processQQ.getInputStream()));
            try {
                while ((line = input.readLine()) != null) {
                    pidInfo += line;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            if (pidInfo.contains("QQApp.exe")) {
                ctrl.hideQQ();
                System.out.println("LINE114 你打开了QQ");
                startFlag = true;
                return;
            } else {
                System.out.println("LINE118 你没有打开QQ");
            }
        }
    }
    
    //抓取全屏
    private static String cheers(Robot robot) {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        BufferedImage bufferedImage = robot.createScreenCapture(new Rectangle(d.width, d.height));
        
        
        System.out.println("LINE92: " + "Width: " + d.width + " Height: " + d.height);
        String saveName = imgSavePath + System.currentTimeMillis() + ".png";
        try {
            ImageIO.write(bufferedImage, "png", new File(saveName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return saveName;
    }
    
    //处理图片
    private static Point loadPic(String saveName) {
        Mat mat = Imgcodecs.imread(saveName);
        //String outStr = "F:/08.png";
        
        //去色
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        
        
        //带参数阈值化 后两个重点 图3-6
        //Imgproc.adaptiveThreshold(mat, mat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY , 5, 15);
        
        
        //阈值化 第三个参数决定阈值,图7
        Imgproc.threshold(mat, mat, 254, 255, Imgproc.THRESH_BINARY);
        
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        //腐蚀操作
        Imgproc.erode(mat, mat, element);
        //膨胀操作
        Imgproc.dilate(mat, mat, element);
        
        //边缘检测
        Imgproc.Canny(mat, mat, 60, 180, 3, true);
        
        Imgcodecs.imwrite(saveName + "3.png", mat);
        
        //轮廓向量
        List<MatOfPoint> list = new ArrayList<MatOfPoint>();
        List<MatOfPoint> outList = new ArrayList<MatOfPoint>();
        
        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        
        Mat counter = new Mat();
        
        //查找所有轮廓
        Imgproc.findContours(mat, list, counter, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        
        Point midPoint = new Point();
        
        //每个轮廓矩形逼近 outlist
        for (int i = 0; i < list.size(); i++) {
            Imgproc.approxPolyDP(new MatOfPoint2f(list.get(i).toArray()), matOfPoint2f, 4, true);
            outList.add(new MatOfPoint(matOfPoint2f.toArray()));
        }
        
        List<Point> pointList = new ArrayList<Point>();
        //遍历每个轮廓,如果大小符合就计算中点
        for (int i = 0; i < list.size(); i++) {
            Point[] pointArr = list.get(i).toArray();
            
            Point minPoint = new Point(10000, 10000);
            Point maxPoint = new Point(0, 0);
            for (int j = 0; j < pointArr.length; j++) {
                
                if (pointArr[j].x > maxPoint.x) {
                    maxPoint.x = pointArr[j].x;
                }
                if (pointArr[j].y > maxPoint.y) {
                    maxPoint.y = pointArr[j].y;
                }
                
                if (pointArr[j].x < minPoint.x) {
                    minPoint.x = pointArr[j].x;
                }
                if (pointArr[j].y < minPoint.y) {
                    minPoint.y = pointArr[j].y;
                }
                
            }//获取左上点min和右下点max
            if (maxPoint.y - minPoint.y >= 20 && maxPoint.y - minPoint.y <= 80 && maxPoint.x - minPoint.x >= 150 && maxPoint.x - minPoint.x <= 250) {
                
                midPoint.x = (minPoint.x + maxPoint.x) / 2;
                midPoint.y = (minPoint.y + maxPoint.y) / 2;
                pointList.add(midPoint);
                
                
                System.out.println("LINE181: " + "midX: " + midPoint.x + " midY: " + midPoint.y);
            }
        }
        
        if (pointList.size() == 1) {
            pointList.get(0).y -= 8;//如果只有一个大框就往上挪点
            
        } else if (pointList.size() == 2) {
            if (pointList.get(0).y > pointList.get(1).y) {
                pointList.get(0).y = pointList.get(1).y;
            }
        }//list0 即是上面的框的中点
        
        
        if (pointList.size() > 0) {
            return pointList.get(0);
        }
        return null;
        //画轮廓 图8
        //Imgproc.drawContours(mat, outList, -1, new Scalar(128, 255, 255), 1, Imgproc.LINE_AA, counter, 88, new Point(0, 0));
        
        //Imgcodecs.imwrite(outStr, mat);
        
    }
    
    //按键精灵
    private static void doRobot(Robot robot, Point point) {
        
        robot.setAutoDelay(1);
        
       // robot.mouseMove((int) point.x, (int) point.y);
       // robot.mousePress(InputEvent.BUTTON1_MASK);
       // robot.mouseRelease(InputEvent.BUTTON1_MASK);
        
        /*
        
        robot.keyPress(KeyEvent.VK_NUMPAD1);
        robot.keyPress(KeyEvent.VK_NUMPAD2);geggg
        robot.keyPress(KeyEvent.VK_NUMPAD6);
        robot.keyPress(KeyEvent.VK_NUMPAD5);
        robot.keyPress(KeyEvent.VK_NUMPAD8);
        robot.keyPress(KeyEvent.VK_NUMPAD9);
        robot.keyPress(KeyEvent.VK_BACK_SPACE);
        robot.keyPress(KeyEvent.VK_NUMPAD8);
        robot.keyPress(KeyEvent.VK_NUMPAD1);
        robot.keyPress(KeyEvent.VK_NUMPAD5);
        robot.keyPress(KeyEvent.VK_NUMPAD2);
        robot.keyPress(KeyEvent.VK_NUMPAD1);
        */
        
        //robot.keyPress(KeyEvent.VK_TAB);
        
        
        robot.keyPress(KeyEvent.VK_NUMPAD9);
        robot.keyPress(KeyEvent.VK_NUMPAD1);
        robot.keyPress(KeyEvent.VK_NUMPAD6);
        robot.keyPress(KeyEvent.VK_NUMPAD3);
        robot.keyPress(KeyEvent.VK_NUMPAD2);
        robot.keyPress(KeyEvent.VK_NUMPAD1);
        robot.keyPress(KeyEvent.VK_NUMPAD7);
        robot.keyPress(KeyEvent.VK_NUMPAD1);
        robot.keyPress(KeyEvent.VK_NUMPAD4);
        
        
        robot.keyPress(KeyEvent.VK_ENTER);
    
    
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ctrl.showQQ();
    }
    
    
}
