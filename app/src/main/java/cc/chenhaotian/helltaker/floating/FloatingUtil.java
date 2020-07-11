package cc.chenhaotian.helltaker.floating;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.WindowManager;
import android.widget.Toast;

import static android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
import static android.view.WindowManager.LayoutParams.TYPE_TOAST;

public class FloatingUtil {

    /**
     * 获得一个很不错的布局配置
     * @param fullScreen 全屏嘛？
     * @param touchAble 可触摸嘛？
     * @param packageName 包名
     * @return 布局文件
     */
    public static WindowManager.LayoutParams getFloatLayoutParam(boolean fullScreen, boolean touchAble, String packageName) {

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = TYPE_APPLICATION_OVERLAY;
            //刘海屏延伸到刘海里面
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            layoutParams.type = TYPE_TOAST;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        layoutParams.packageName = packageName;

        layoutParams.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;

        //Focus会占用屏幕焦点，导致游戏无声
        if (touchAble) {
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        } else {
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }

        if (fullScreen) {
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        } else {
            layoutParams.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }

        layoutParams.format = PixelFormat.TRANSPARENT;

        return layoutParams;
    }

    /**
     * 检查悬浮权限
     * @param activity 主窗体
     * @return 是否拥有权限
     */
    public static boolean checkPermission(Activity activity){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(activity)) {
            Toast.makeText(activity, "当前无权限，请授权", Toast.LENGTH_SHORT).show();
            activity.startActivityForResult(
                    new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + activity.getPackageName())), 0);
            return false;
        }
        return true;
    }

    /**
     * 一个自定义的回调函数
     * @param activity 主窗体
     * @param requestCode 啥玩意
     * @param resultCode 不知道
     * @param data 反正我用不到
     * @param onWindowPermissionListener 权限回调
     */
    public static void onActivityResult(Activity activity,
                                        int requestCode,
                                        int resultCode,
                                        Intent data,
                                        OnWindowPermissionListener onWindowPermissionListener) {
        if (requestCode == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && !Settings.canDrawOverlays(activity)) {
                Toast.makeText(activity.getApplicationContext(), "授权失败", Toast.LENGTH_SHORT).show();
                if(onWindowPermissionListener!=null)
                    onWindowPermissionListener.onFailure();
            }else {
                Toast.makeText(activity.getApplicationContext(), "授权成功", Toast.LENGTH_SHORT).show();
                if(onWindowPermissionListener!=null)
                    onWindowPermissionListener.onSuccess();
            }
        }
    }

    /**
     * 小接口
     */
    public interface OnWindowPermissionListener{
        void onSuccess();
        void onFailure();
    }
}
