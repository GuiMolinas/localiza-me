package com.queridinhos.tcc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.animation.ValueAnimator;
import java.util.List;

public class RouteView extends View {

    private Paint paint = new Paint();
    private List<PointF> routePoints;
    private float animatedValue = 0f;
    private ValueAnimator animator;
    private Matrix matrix = new Matrix();
    private Path routePath = new Path();

    public RouteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(15f); // Aumentei a espessura para melhor visualização
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
        // Adiciona um efeito de sombra para destacar a rota
        paint.setShadowLayer(10.0f, 0.0f, 0.0f, Color.argb(100, 0, 0, 0));
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
        invalidate();
    }

    /**
     * Define a rota a ser desenhada a partir de uma lista de pontos.
     * @param points A lista de PointF que compõe a rota.
     */
    public void setRoute(List<PointF> points) {
        this.routePoints = points;
        if (points != null && points.size() > 1) {
            buildPath();
            startAnimation();
        } else {
            clearRoute();
        }
    }

    public void clearRoute() {
        if (animator != null) {
            animator.cancel();
        }
        routePoints = null;
        routePath.reset();
        invalidate();
    }


    private void buildPath() {
        routePath.reset();
        if (routePoints == null || routePoints.size() < 2) {
            return;
        }
        PointF startPoint = routePoints.get(0);
        routePath.moveTo(startPoint.x, startPoint.y);
        for (int i = 1; i < routePoints.size(); i++) {
            PointF nextPoint = routePoints.get(i);
            routePath.lineTo(nextPoint.x, nextPoint.y);
        }
    }

    private void startAnimation() {
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(2000); // Duração da animação em milissegundos
        animator.addUpdateListener(animation -> {
            animatedValue = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (routePath.isEmpty()) {
            return;
        }

        canvas.save();
        canvas.concat(matrix);

        // Anima o desenho da rota
        Path partialPath = new Path();
        android.graphics.PathMeasure pathMeasure = new android.graphics.PathMeasure(routePath, false);
        pathMeasure.getSegment(0, pathMeasure.getLength() * animatedValue, partialPath, true);
        canvas.drawPath(partialPath, paint);

        // Desenha círculos no início e no fim da rota quando a animação termina
        if (animatedValue == 1f && routePoints != null && !routePoints.isEmpty()) {
            PointF startPoint = routePoints.get(0);
            PointF endPoint = routePoints.get(routePoints.size() - 1);

            Paint circlePaint = new Paint();
            circlePaint.setStyle(Paint.Style.FILL);

            // Círculo verde no início
            circlePaint.setColor(Color.GREEN);
            canvas.drawCircle(startPoint.x, startPoint.y, 20, circlePaint);

            // Círculo azul no fim
            circlePaint.setColor(Color.BLUE);
            canvas.drawCircle(endPoint.x, endPoint.y, 20, circlePaint);
        }

        canvas.restore();
    }
}