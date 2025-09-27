package com.queridinhos.tcc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.animation.ValueAnimator;

public class RouteView extends View {

    private Paint paint = new Paint();
    private PointF startPoint;
    private PointF endPoint;
    private float animatedValue = 0f;
    private ValueAnimator animator;

    public RouteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setRoute(PointF startPoint, PointF endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        startAnimation();
    }

    private void startAnimation() {
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1500); // Duração da animação em milissegundos
        animator.addUpdateListener(animation -> {
            animatedValue = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (startPoint != null && endPoint != null) {
            // Desenha a linha da rota
            float currentX = startPoint.x + (endPoint.x - startPoint.x) * animatedValue;
            float currentY = startPoint.y + (endPoint.y - startPoint.y) * animatedValue;
            canvas.drawLine(startPoint.x, startPoint.y, currentX, currentY, paint);

            // Adiciona um círculo no ponto final quando a animação termina
            if (animatedValue == 1f) {
                Paint endCirclePaint = new Paint();
                endCirclePaint.setColor(Color.BLUE);
                endCirclePaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(endPoint.x, endPoint.y, 20, endCirclePaint);
            }
        }
    }
}