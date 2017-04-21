package com.tenpa_mf.stringex;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by 慶太 on 2017/04/20.
 */

public class StringView extends View {
    //コンストラクタ
    public StringView(Context context){
        super(context);
        setBackgroundColor(Color.WHITE);
    }

    @Override
    protected  void onDraw(Canvas canvas){
       Paint paint =new Paint();
        paint.setAntiAlias(true);

        //文字サイズと文字色の指定
        paint.setTextSize(48);
        paint.setColor(Color.rgb(0,0,0));

        //画面サイズの取得
        canvas.drawText("画面サイズ"+getWidth()+"×"+getHeight(),0,60,paint);
        //文字幅の取得
        canvas.drawText("文字幅"+paint.measureText("A"),0,120,paint);

        //68ドット文字列の表示
        paint.setTextSize(68);
        paint.setColor(Color.rgb(255,0,0));
        canvas.drawText("68dot",0,60*3,paint);
    }
}
