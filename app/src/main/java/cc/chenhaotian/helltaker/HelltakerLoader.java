package cc.chenhaotian.helltaker;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class HelltakerLoader {
    private String characterName;
    private Context context;
    private Bitmap[] images;
    private static HashMap<String, Bitmap> imagesCache = new HashMap<>();
    private int index = 0;

    public HelltakerLoader(String characterName, Context context){
        this.characterName = characterName;
        this.context = context;
        images = new Bitmap[12];
    }

    /**
     * 从Assets中读取图片
     */
    private Bitmap getImageFromAssetsFile(String fileName)
    {
        Bitmap image = null;
        AssetManager am = context.getAssets();
        try
        {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return image;

    }

    /**
     * 加载所有图片
     */
    public void loadBitmap(){
        char[] chars = characterName.toCharArray();
        chars[0] = chars[0] <= 'Z' && chars[0] >= 'A' ? (char)(chars[0] + 32) : chars[0];
        String lowerName = new String(chars);
        //直接用Unity Studio扒拉下来的图
        String rootPath = "Helltaker/" + characterName + "/Texture2D/" + lowerName + "_finalModel00";
        for(int i = 1; i <= 12; i++){
            String path = rootPath + (i < 10 ? "0" + i : i) + ".png";
            //如果之前加载过就从缓存里面加载
            if(imagesCache.containsKey(path))
                images[i - 1] = imagesCache.get(path);
            else{
                images[i - 1] = getImageFromAssetsFile(path);
                imagesCache.put(path, images[i - 1]);
            }
        }
    }

    /**
     * 自动播放控制
     * @return 返回应该显示的那一帧
     */
    public Bitmap next(){
        if(index >= 12) index = 0;
        index++;
        return images[index - 1];
    }

    /**
     * 重置
     */
    public void reset(){
        index = 0;
    }
}
