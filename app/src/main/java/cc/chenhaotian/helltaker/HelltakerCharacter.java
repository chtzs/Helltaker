package cc.chenhaotian.helltaker;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class HelltakerCharacter {
    private ImageView imageView;
    private HelltakerLoader loader;
    private String name;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    public HelltakerCharacter(String name, Context context,
                              WindowManager windowManager, WindowManager.LayoutParams layoutParams){
        this.name = name;
        this.windowManager = windowManager;
        this.layoutParams = layoutParams;
        loader = new HelltakerLoader(name, context);
        loader.loadBitmap();
        imageView = new ImageView(context);
        windowManager.addView(imageView, layoutParams);
        next();
    }

    /**
     * 下一帧
     */
    public void next(){
        imageView.setImageBitmap(loader.next());
    }

    /**
     * 获取角色的名字
     * @return 名字
     */
    public String getName(){
        return name;
    }

    /**
     * 获取一个妹子容器（笑）
     * @return 装妹子的容器呀
     */
    public ImageView getImageView(){
        return imageView;
    }

    /**
     * 获取布局
     * @return 布局
     */
    public WindowManager.LayoutParams getLayoutParams(){
        return layoutParams;
    }

    /**
     * 重置到第一帧
     */
    public void reset(){
        loader.reset();
    }

    /**
     * 固定妹子
     */
    public void fixed(){
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        update();
    }

    /**
     * 解锁妹子
     */
    public void unfixed(){
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        layoutParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        update();
    }

    /**
     * 更新布局
     */
    public void update(){
        windowManager.updateViewLayout(imageView, layoutParams);
    }

    /**
     * 摧毁妹子
     */
    public void destroy(){
        imageView.setVisibility(View.GONE);
        windowManager.removeView(imageView);
    }
}
