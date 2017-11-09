package sample;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main extends Application {
    static Controller ctrl = new Controller();
    
    @Override
    public void start(Stage primaryStage) {
        Task<Void> progressTask = new Task<Void>() {
            @Override
            protected void succeeded() {
                super.succeeded();
                updateMessage("Succeeded");
            }
            
            @Override
            protected Void call() throws Exception {
                Runtime run = Runtime.getRuntime();
                String path = new String("\"F:\\TestForm.exe\"");
                
                
                Process process = null;
                try {
                    process = run.exec("cmd.exe /k " + path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                checkProcessStart();
                ctrl.hideForm();
                
                
                ctrl.getPop();
                //Thread.sleep(500);
                ctrl.showForm();
                
                BufferedReader br = new BufferedReader(new InputStreamReader(process
                        .getInputStream()));
                String msg = null;
                try {
                    while ((msg = br.readLine()) != null) {
                        System.out.println("LINE66 完成");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        new Thread(progressTask).start();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    //检测进程启动
    private static void checkProcessStart() {
        Boolean startFlag = false;
        
        while (startFlag != true) {
            String line;
            String pidInfo = "";
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
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
            
            if (pidInfo.contains("TestForm.exe")) {
                System.out.println("LINE100 你打开了C#");
                return;
            } else {
                System.out.println("LINE104 还没打开C#");
            }
        }
    }
}
