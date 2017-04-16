package com.tenpa_mf.shootingapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by 慶太 on 2017/04/16.
 */

public class ShootingView extends SurfaceView
        implements SurfaceHolder.Callback, Runnable {
    //シーン定数
    private final static int
        S_TITLE = 0, //タイトル
        S_PLAY = 1, //プレイ
        S_GAMEOVAER = 2; //ゲームオーバー

    //画面サイズ定数
    private final static int
        W= 480,//画面幅
        H= 800;//画面高さ
    //システム面こまごま
    private SurfaceHolder holder;
    private Graphics g;
    private Thread thread;
    private Bitmap[] bmp = new Bitmap[10];
    private int init = S_TITLE;
    private int scene = S_TITLE;
    private int score;
    private  int tick;
    private int tick2;
    private long gameovertime;

    //自機について
    private int shipX;//自機のx座標
    private int shipY = 600;//自機のY座標
    private int shipToX = shipX; //自機の移動先のx座標
    private int shipToY = shipY; //自機の移動先のY座標

    //爆発用クラス
    private class Bom{
        int x;
        int y;
        int life;

        //newされた直後に実行される処理(コンストラクタ)
       private Bom(int x, int y){
            this.x = x;
            this.y = y;
            this.life = 3;
        }
    }

    //隕石・弾・爆発
    private List<Point> meteos = new ArrayList<Point>();//隕石
    private List<Point> supermeteo = new ArrayList<Point>();//スーパー隕石
    private List<Point> shots  = new ArrayList<Point>();//弾
    private List<Bom> boms     = new ArrayList<Bom>();//爆発

    //コンストラクタの記述
    public ShootingView(Activity activity){
        super(activity);
        //ビットマップの読み込み
        for(int i = 0; i<=9;i++){
            bmp[i] = readBitmap(activity,"sht"+i);
        }

        //サーフェイスフォルダーの生成
        //サーフェイスというのは、画面上にさまざまなグラフィックを描画し表示するためのもので、
        // SurfaceViewではコンポーネント上にサーフェイスでさまざまな描画を表示する.
        holder = getHolder();
        holder.addCallback(this);

        //画面サイズの指定
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point p = new Point();
        display.getSize(p);
        int dh = W*p.y/p.x;

        //グラフィックスの生成
        g=new Graphics(W,dh,holder);
        g.setOrigin(0,(dh-H)/2);

    }

    //サーフェイス生成時に呼ばれる。
    public void surfaceCreated(SurfaceHolder holder){
        thread =new Thread(this);
        thread.start();
    }

    //サーフェイス終了時に呼ばれる。
    public void surfaceDestroyed(SurfaceHolder holder){thread=null;}

    //サーフェイス変更時に呼ばれる。
    public void surfaceChanged(SurfaceHolder holder,int format,int w,int h){};

    //スレッドの処理
    public void run(){
        while(thread!=null){
            //初期化
            if(init>=0){
                scene = init;
                //タイトル
                if(scene==S_TITLE){
                    shipX =W/2;
                    shots.clear();
                    meteos.clear();
                    boms.clear();
                    tick = 0;
                }
                //ゲームオーバー
                else if(scene==S_GAMEOVAER){
                    gameovertime = System.currentTimeMillis();
                }
                init = -1;
            }
            //プレイ時の処理
            if(scene==S_PLAY){
                //隕石とスーパーメテオのためのカウント開始
                tick++;
                tick2++;
                //隕石の出現
                if(tick>10){
                    tick = 0;
                    meteos.add(new Point(rand(315)+86,-50));
                }

                //スーパーメテオの出現
                if(tick2/20==1){
                    tick2 = 0;
                    supermeteo.add(new Point(rand(315)+86,-50));
                }

                //隕石の移動
                for(int i=meteos.size()-1;i>=0;i--){
                    Point pos = meteos.get(i);
                    pos.y += 5;

                    //隕石のゲームオーバー判定
                    if(pos.y>H){
                        init = S_GAMEOVAER;
                    }
                }

                //スーパーメテオの移動
               for(int i = supermeteo.size()-1; i>=0; i--){
                    Point position = supermeteo.get(i);
                    position.y +=3;
                    /*position.x += (int)Math.cos(3.14)*5;
                    position.y += (int)Math.sin(3.14/2)*5;*/

                    //枠外に出たら消去
                    if(position.x<0||position.x>400){
                        supermeteo.remove(i);
                    }

                    //スーパーメテオのゲームオーバー判定
                    if(position.y>H){
                        init = S_GAMEOVAER;
                    }
                }

                //弾の移動
                for(int i= shots.size()-1;i>=0;i--){
                    Point pos0 = shots.get(i);
                    pos0.y-=20;

                    //枠外に出た弾の削除
                    if(pos0.y<-100){
                        shots.remove(i);
                    }
                    //衝突
                    else{
                        for(int j=meteos.size()-1;j>=0;j--){
                            Point pos1 = meteos.get(j);
                            if(Math.abs(pos0.x-pos1.x)<50&&Math.abs(pos0.y-pos1.y)<50){
                                //爆発追加
                                boms.add(new Bom(pos1.x,pos1.y));
                                shots.remove(i);
                                meteos.remove(j);
                                score += 10;
                                break;
                            }
                        }
                    }
                    //スーパーメテオについての衝突
                   for(int k=supermeteo.size()-1;k>=0;k--){
                        Point position1 = supermeteo.get(k);
                        if(Math.abs(pos0.x-position1.x)<50&&Math.abs(pos0.y-position1.y)<50){
                            //爆発追加
                            boms.add(new Bom(pos0.x,pos0.y));
                            shots.remove(i);
                            supermeteo.remove(k);
                            score+=30;
                            break;
                        }
                    }
                }
                //爆発の遷移
                for(int i=boms.size()-1;i>=0;i--){
                    Bom bom = boms.get(i);
                    bom.life--;
                    if(bom.life<0){
                        boms.remove(i);
                    }
                }
                //宇宙船の移動
                if(Math.abs(shipX-shipToX)<10){
                    shipX=shipToX;
                }else if(shipX<shipToX){
                    shipX += 20;
                }else if(shipX>shipToX){
                    shipX -= 20;
                }

                if(Math.abs(shipY-shipToY)<10){
                    shipY = shipToY;
                }else if(shipY<shipToY){
                    shipY += 20;
                }else if(shipY>shipToY){
                    shipY -= 20;
                }
            }

            //背景の描画
            g.lock();
            g.drawBitmap(bmp[0],0,0);
            if(scene == S_GAMEOVAER){
                g.drawBitmap(bmp[3],0,H-190);
            }else {
                g.drawBitmap(bmp[2],0,H-190);
            }
            //自機の描画
            g.drawBitmap(bmp[6],shipX-48,shipY-50);

            //隕石の描画
            for(int i=meteos.size()-1;i>=0;i--){
                Point pos = meteos.get(i);
                g.drawBitmap(bmp[5],pos.x-43,pos.y-45);
            }
            //スーパーメテオの描写
           for(int i=supermeteo.size()-1;i>=0;i--){
                Point position = supermeteo.get(i);
                g.drawBitmap(bmp[7],position.x-43,position.y-45);
            }
            //弾の描画
            for(int i= shots.size()-1;i>=0;i--){
                Point pos = shots.get(i);
                g.drawBitmap(bmp[8],pos.x-10,pos.y-10);
            }
            //爆発の描画
            for(int i =boms.size()-1;i>=0;i--){
                Bom bom = boms.get(i);
                g.drawBitmap(bmp[1],bom.x-57,bom.y-57);
            }
            //メッセージの描画
            if(scene==S_TITLE){
                g.drawBitmap(bmp[9],(W-400)/2,150);
            }else if(scene == S_GAMEOVAER){
                g.drawBitmap(bmp[4],(W-300)/2,150);
            }
            //スコアの描画
            g.setColor(Color.WHITE);
            g.setTextSize(30);
            g.drawText("score"+num2str(score,6),10,10+g.getOriginY()-(int)g.getFontMetrics().ascent);
            g.unlock();

            //スリープ
            try{
                Thread.sleep(30);
            }catch (Exception e){
            }
        }
    }
    //タッチ時に呼ばれる。
    @Override
    public boolean onTouchEvent(MotionEvent event){
        int touchX = (int)(event.getX()*W/getWidth());
        int touchY = (int)(event.getY()*H/getHeight());
        int touchAction = event.getAction();
        if(touchAction==MotionEvent.ACTION_DOWN){
            if(scene==S_TITLE){
                init = S_PLAY;
            }
            //プレイシーンでのタッチアクション
            else if(scene==S_PLAY){
                //タッチで弾の追加
                shots.add(new Point(shipX,shipY-50));

                //自機の移動先の取得
                shipToX = touchX;
                shipToY = touchY;
            }
            //ゲームオーバー
            else if(scene==S_GAMEOVAER){
                //ゲームオーバー後1秒以上
                if(gameovertime+1000<System.currentTimeMillis()){
                    init = S_TITLE;
                    score = 0;
                }
            }
        }else if(touchAction == MotionEvent.ACTION_MOVE){
            //プレイ中
            if(scene == S_PLAY){
                //自機の移動
                shipToY = touchY;
               /* shipToX = touchX;*/
            }
        }
        return true;
    }

    //乱数取得のための関数
    private static Random rand = new Random();
    private static int rand(int num){
        return (rand.nextInt()>>>1)%num;
    }

    //数値から文字列への変換
    private static String num2str(int num,int len){
        String str = ""+num;
        while(str.length()<len){
            str = "0"+str; //0を頭に付けている
        }
        return str;
    }

    //Bitmapの読み込み
    private static Bitmap readBitmap(Context context, String name){
        int resID = context.getResources().getIdentifier(name,"drawable",context.getPackageName());
        return BitmapFactory.decodeResource(context.getResources(),resID);
    }
}