package cc.chenhaotian.helltaker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cc.chenhaotian.helltaker.floating.FloatingUtil;
import cc.chenhaotian.helltaker.floating.FloatingWindowService;
import cc.chenhaotian.helltaker.list.CardListAdapter;

public class MainActivity extends AppCompatActivity {
    private boolean isStart = false;
    private boolean isFixed = false;
    private String[] characters;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        if(FloatingUtil.checkPermission(this)){
            start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FloatingUtil.onActivityResult(this, requestCode, resultCode, data, new FloatingUtil.OnWindowPermissionListener() {
            @Override
            public void onSuccess() {
                start();
            }

            @Override
            public void onFailure() {

            }
        });
    }

    /**
     * 读取所有恶魔的名字
     */
    private void readAllCharactersName(){
        AssetManager am = getAssets();
        try {
            characters = am.list("Helltaker/");
            List<String> list = new ArrayList<>(Arrays.asList(characters));
            list.remove("Portal");
            characters = list.toArray(new String[0]);
            /*for(String c : characters){
                Log.i("FileName", c);
            }*/
        } catch (IOException e) {
            Toast.makeText(this, "读取文件时出错！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    /**
     * 对可爱的恶魔进行初始化
     */
    private void start(){
        if(!isStart){
            startService(new Intent(MainActivity.this, FloatingWindowService.class));
            readAllCharactersName();
            //网格布局，每三个换行一次
            GridLayoutManager gridLayoutManager
                    = new GridLayoutManager(this, 3);
            recyclerView.setLayoutManager(gridLayoutManager);
            CardListAdapter adapter = new CardListAdapter(characters);
            //设置恶魔列表的监听器
            adapter.setOnAddListener(new CardListAdapter.OnAddListener() {
                @Override
                public void OnAdd(String name) {
                    addNew(name);
                }
            });
            recyclerView.setAdapter(adapter);
            isStart = true;
        }
    }

    /**
     * 同步恶魔的步伐，此为点击事件的处理函数
     * @param view 按钮元素
     */
    public void synchronize(View view){
        Intent intent = new Intent();
        intent.putExtra("synchronize", true);
        intent.setAction(getPackageName());
        sendBroadcast(intent);
    }

    /**
     * 移除所有恶魔，此为点击事件的处理函数
     * @param view 按钮元素
     */
    public void removeAll(View view){
        Intent intent = new Intent();
        intent.putExtra("removeAll", true);
        intent.setAction(getPackageName());
        sendBroadcast(intent);
    }

    /**
     * 销毁所有的恶魔并退出，此为点击事件的处理函数
     * @param view 按钮元素
     */
    public void exit(View view){
        Intent intent = new Intent();
        intent.putExtra("stop", true);
        intent.setAction(getPackageName());
        sendBroadcast(intent);
        finish();
    }

    public void fixed(View view){
        isFixed = ! isFixed;
        Intent intent = new Intent();
        if(isFixed){
            intent.putExtra("fixed", true);
            ((Button)view).setText(R.string.unfixed);
        }else{
            intent.putExtra("unfixed", true);
            ((Button)view).setText(R.string.fixed);
        }
        intent.setAction(getPackageName());
        sendBroadcast(intent);
    }

    /**
     * 添加一个新恶魔
     * @param name 恶魔的名字
     */
    public void addNew(String name){
        Intent intent = new Intent();
        intent.putExtra("addNew", true);
        intent.putExtra("characterName", name);
        intent.setAction(getPackageName());
        sendBroadcast(intent);
    }
}
