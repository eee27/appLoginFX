package sample;

public class Controller {
    
    static {
        System.loadLibrary("Project1");
    }
    
    
    // 声明本地方法
    public native void sayHello();
    public native double[] getQQPos();
    public native void hideQQ();
    public native void showQQ();
    public native void setHook();
    
    
    public static void main(String[] args) {
        // 加载动态链接库
        
        Controller nativeCode = new Controller();
        //nativeCode.sayHello();
    }
}
