package com.example.myapp;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.Nullable;

public class Mview extends View {
     public  static final float MAX_RADIUS=80;//最大半径
    public static final  float MIN_RADIUS=10;//最小半径
    public static final float MAX_SPAC=500;//最大拖动距离
    public static int DRAG=100;//拖动时
    public static int DESTROY=20;//销毁时 //爆炸时

//    public static
    float x,y,ex,ey;//x,y是圆的起始坐标  ex ey是随手势拖动后的坐标
    float radius;//主圆心的半径
    Paint paint;//画笔对象
    Path path;//path
    int Status;//状态
//    float viewx,viewy;
    public Mview(Context context) {
        super(context);
        init();
    }


    public Mview(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public Mview(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        path=new Path();
        paint = new Paint();
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);//填充
        paint.setColor(Color.RED);
        paint.setTextSize(100);
        radius=MAX_RADIUS;//假设半径 为 最大半径80
        x=400;y=500;//假设 初始圆心在 400.500
        ex=x;ey=y;
        Status=DRAG;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        path.reset();
        //先从主圆的圆心点 到 次元圆心点画一条直线
//        path.moveTo(x,y);
//        path.lineTo(ex,ey);
        if (Status==DESTROY){
            //这里就不绘制效果了 直接用文字代替了。。。。。
            canvas.drawText("爆炸",ex,ey,paint);
            return;
        }
        //先求∠A的度数
        float a=Math.abs(ex-x);
        float b=Math.abs(ey-y);
        float rang= (float) (Math.atan(b/a)*180/Math.PI);//反正切函数
        //再求第一个点的坐标
        float rangB=90-rang;
        rangB*=Math.PI/180;//∠B的度数
        //坐标点
        float dy= (float) (Math.sin(rangB)*radius);//y坐标
        float dx= (float) Math.sqrt(radius*radius-dy*dy);//勾股定理
        if (ex<x) dx=-dx;
        if (ey<y) dy=-dy;
        //将该店作为初始点开始绘制
        path.moveTo(x+dx,y-dy);
        //连接主圆的 第一个点
        path.quadTo(ex-(ex-x)/2,ey-(ey-y)/2,x+dx+ex-x,y-dy+ey-y);//用贝塞尔曲线 向两个圆心直线中点偏移
        //再连接其他点
        path.lineTo(x-dx+ex-x,y+dy+ey-y);//
        path.quadTo(ex-(ex-x)/2,ey-(ey-y)/2,x-dx,y+dy);
        path.lineTo(x+dx,y-dy);//连接初始点
        canvas.drawPath(path,paint);
        //绘制主圆
        canvas.drawCircle(ex,ey,MAX_RADIUS,paint);
        //拖动时原点绘制另一个圆
        canvas.drawCircle(x,y,radius,paint);//此圆半径可变
    }

    //根据手势拖拽圆
    PointF pointF;//手指落下时的点坐标
    boolean isDrag=true;//是否可以拖拽
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (Status==DESTROY) return true;
        switch (event.getAction()){

            case MotionEvent.ACTION_DOWN://手指落下时

                //记录按下的点
                pointF=new PointF(event.getX(),event.getY());
                if(getSpacingPoint(pointF,new PointF(ex,ey))<=radius){//小于半径时
                    isDrag=true;
                }else {
                    isDrag=false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //移动时先计算是否在圆的内部
                if (isDrag){
                    ex=event.getX();
                    ey=event.getY();
                    float spac=getSpacingPoint(pointF,new PointF(ex,ey));//先获取距离
                    if (spac>MAX_SPAC){
                        //大于最大距离时
                        Status = DESTROY;
                    }
                    radius=MAX_RADIUS-spac/10;//距离越长 次圆越小
                    radius=radius>MAX_RADIUS?MAX_RADIUS:radius;
                    radius=radius<MIN_RADIUS?MIN_RADIUS:radius;
                    invalidate();
                    ViewGroup.LayoutParams layoutParams=getLayoutParams();
//                    layoutParams.width= (int) Math.abs(ex-x+radius+MAX_RADIUS)*2;
//                    layoutParams.height= (int) Math.abs(ey-y+radius+MAX_RADIUS)*2;
                 //   setX(viewx);
                 //   setY(viewy);
                    setLayoutParams(layoutParams);
                }

                break;
            case MotionEvent.ACTION_UP:
                if (Status==DRAG){//没有达到爆炸距离
                    startAnim();//播放回到原点的动画
                }else{
                    //爆炸
                }
                break;
        }

        return true;
    }

    private void startAnim() {
        ValueAnimator animator1=ValueAnimator.ofFloat(ex,x);//从ex 到 x
        animator1.setDuration(200);//20ms
        animator1.start();
        animator1.setInterpolator(new OvershootInterpolator());
        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ex= (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator animatorY=ValueAnimator.ofFloat(ey,y);//从ey -- y
        animatorY.setDuration(200);
        animatorY.setInterpolator(new OvershootInterpolator());
        animatorY.start();
        animatorY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ey= (float) animation.getAnimatedValue();
                invalidate();//重绘
            }
        });
    }

    //获取触碰点与初始圆心的距离
    public float getSpacingPoint(PointF p1,PointF p2){
        float dx=p2.x-p1.x;
        float dy=p2.y-p1.y;
        return (float) Math.sqrt(dx*dx+dy*dy);//勾股定理
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }
}
