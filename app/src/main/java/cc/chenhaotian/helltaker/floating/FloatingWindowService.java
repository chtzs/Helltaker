package cc.chenhaotian.helltaker.floating;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

import cc.chenhaotian.helltaker.HelltakerCharacter;

public class FloatingWindowService extends Service {
    private WindowManager windowManager;
    //人物列表
    private LinkedList<HelltakerCharacter> characters;
    //传送门
    private HelltakerCharacter portal;
    //当前是第几帧
    private int index = 0;
    //上一帧出现的时间
    private long lastTime;
    //可重入锁
    private ReentrantLock lock = new ReentrantLock();
    //是否停止摇摆
    private boolean isStop = false;
    //妹子是否固定不可动
    private boolean isFixed = false;

    //每一帧所需的时间，这个是我自己打节拍测量出来的，平均值是150个节拍/分钟
    private static final double DELAY_PER_FRAME = 800d / 12;// = 66.66666666
    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        //广播接收器
        MyReceiver receiver = new MyReceiver();
        //广播过滤器
        IntentFilter filter = new IntentFilter();
        filter.addAction(getPackageName());
        FloatingWindowService.this.registerReceiver(receiver, filter);

        characters = new LinkedList<>();
        changeImageHandler = new Handler(this.getMainLooper(), changeImageCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 制造一个适合大小的放置妹子的布局
     * @return 布局
     */
    public WindowManager.LayoutParams createLayoutParams(){
        WindowManager.LayoutParams layoutParams;
        layoutParams = FloatingUtil.getFloatLayoutParam(false, true, getPackageName());
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.width = 200;
        layoutParams.height = 200;
        return layoutParams;
    }

    /**
     * 添加一个新的恶魔妹子（或者是传送门）
     * @param name 角色名称
     * @return 角色
     */
    public HelltakerCharacter addNewCharacter(String name){
        WindowManager.LayoutParams layoutParams = createLayoutParams();
        HelltakerCharacter character = new HelltakerCharacter(name,
                getApplicationContext(), windowManager, layoutParams);
        //如果是固定状态，那么新添加的就没法移动
        if(isFixed)
            character.fixed();
        characters.add(character);
        character.getImageView().setOnTouchListener(new FloatingOnTouchListener(layoutParams));
        return character;
    }

    /**
     * 同步所有妹子的舞蹈~
     */
    public void synchronizeAllCharacters(){
        lock.lock();
        for(HelltakerCharacter character : characters){
            character.reset();
            character.next();
        }
        lastTime = System.currentTimeMillis();
        index = 0;
        lock.unlock();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private Handler changeImageHandler;
    private Handler.Callback changeImageCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0 && !isStop) {
                lock.lock();
                long time;
                //精确踩点
                if((time = System.currentTimeMillis()) - lastTime >= (index + 1) * DELAY_PER_FRAME){
                    index++;
                    nextFrame();
                    if(index == 12) {
                        index = 0;
                        lastTime = time;
                    }
                }
                lock.unlock();
                changeImageHandler.sendEmptyMessageDelayed(0, 1);


            }
            return false;
        }
    };

    /**
     * 逐帧动画，进入下一帧
     */
    public void nextFrame(){
        //锁住，防止资源争夺导致莫名其妙的问题
        lock.lock();
        for(HelltakerCharacter character : characters){
            character.next();
        }
        portal.next();
        lock.unlock();
    }

    /**
     * 清空妹子们
     */
    private void clear(){
        lock.lock();
        for(HelltakerCharacter character : characters){
            character.destroy();
        }
        characters.clear();
        System.gc();
        lock.unlock();
    }

    /**
     * 初始化
     */
    public void start(){
        //进入循环动画
        changeImageHandler.sendEmptyMessageDelayed(0, 1);
        lastTime = System.currentTimeMillis();

        Display defaultDisplay = windowManager.getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        int x = point.x;
        int y = point.y;

        //制造一个传送门，进入门的恶魔妹子们将会去向何处呢？
        portal = addNewCharacter("Portal");
        //坐标（0， 0）是屏幕中心，匪夷所思...
        portal.getLayoutParams().y = y / 2 -  portal.getLayoutParams().height / 2;
        //固定传送门
        portal.fixed();
        //只有需要的时候传送门才会打开
        portal.getImageView().setVisibility(View.GONE);
        characters.clear();
    }

    /**
     * 固定恶魔妹子们，防止误触~
     */
    private void fixed(){
        for(HelltakerCharacter character : characters){
            character.fixed();
        }
    }

    /**
     * 解锁妹子，这样就可以愉快的玩耍啦
     */
    private void unfixed(){
        for(HelltakerCharacter character : characters){
            character.unfixed();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        start();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 接受主窗体传过来的广播事件
     */
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            assert bundle != null;
            if(bundle.getBoolean("addNew", false)){
                String name = bundle.getString("characterName", "Lucy");
                addNewCharacter(name);
            }else if(bundle.getBoolean("synchronize", false)){
                synchronizeAllCharacters();
            }else if(bundle.getBoolean("removeAll", false)){
                clear();
            }else if(bundle.getBoolean("stop", false)){
                isStop = true;
                clear();
                stopSelf();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }else if(bundle.getBoolean("fixed", false)) {
                fixed();
                isFixed = true;
            }else if(bundle.getBoolean("unfixed", false)) {
                unfixed();
                isFixed = false;
            }
        }
    }

    /**
     * 妹子拖动事件
     */
    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;
        private WindowManager.LayoutParams layoutParams;

        private FloatingOnTouchListener(WindowManager.LayoutParams layoutParams){
            this.layoutParams = layoutParams;
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    //传送门出现啦
                    portal.getImageView().setVisibility(View.VISIBLE);
                    view.performClick();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                case MotionEvent.ACTION_UP:
                    //再见，传送门
                    portal.getImageView().setVisibility(View.GONE);
                    WindowManager.LayoutParams lp = portal.getLayoutParams();
                    Display defaultDisplay = windowManager.getDefaultDisplay();
                    Point point = new Point();
                    defaultDisplay.getSize(point);
                    //屏幕宽高
                    int dx = point.x;
                    int dy = point.y;
                    //小可爱的位置
                    int cx = layoutParams.x;
                    int cy = layoutParams.y;
                    //碰撞检测，判断妹子是否进入传送门
                    if(cx >= -lp.width / 2 && cx <= lp.width / 2
                            && cy >= dy / 2 - lp.height){
                        lock.lock();
                        Iterator<HelltakerCharacter> iterator = characters.iterator();
                        HelltakerCharacter rm;
                        while (iterator.hasNext()){
                            if((rm = iterator.next()).getImageView() == view){
                                //再见...
                                iterator.remove();
                                rm.getImageView().setVisibility(View.GONE);
                                rm.destroy();
                                break;
                            }
                        }
                        lock.unlock();
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
