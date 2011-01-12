package se.ritzau.gimli.android;

import se.ritzau.ui.ValueListener;
import se.ritzau.ui.VolumeControl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MarvinVolumeControl extends View {
    private VolumeControl control = new VolumeControl();
    
    private float dx(MotionEvent e) {
	return e.getX() - getWidth()/2;
    }

    private float dy(MotionEvent e) {
	return e.getY() - getHeight()/2;
    }

    private void init() {
	control.setRepaint(new Runnable() {
	    @Override public void run() { postInvalidate(); }
	});
    }
    
    public MarvinVolumeControl(Context context) {
	super(context);
	init();
    }

    public MarvinVolumeControl(Context context, AttributeSet attributes) {
	super(context, attributes);
	init();
    }
    
    public void setValueListener(ValueListener l) {
	control.setValueListener(l);
    }
    
    public boolean isControlledByUser() {
	return control.isControledByUser();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
	switch (e.getAction()) {
	case MotionEvent.ACTION_DOWN:
	    control.mousePressed(dx(e), dy(e));
	    break;
	case MotionEvent.ACTION_UP:
	    control.mouseReleased(dx(e), dy(e));
	    break;
	case MotionEvent.ACTION_MOVE:
	    control.mouseDragged(dx(e), dy(e));
	    break;
	}

	return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	int width, height;
	
	switch (MeasureSpec.getMode(widthMeasureSpec)) {
	case MeasureSpec.AT_MOST:
	    width = Math.min(200, MeasureSpec.getSize(widthMeasureSpec));
	    break;
	    
	case MeasureSpec.EXACTLY:
	    width = MeasureSpec.getSize(widthMeasureSpec);
	    break;
	    
	case MeasureSpec.UNSPECIFIED:
	    width = 200;
	    break;
	    
	default:
	    throw new IllegalArgumentException();
	}
	
	
	switch (MeasureSpec.getMode(heightMeasureSpec)) {
	case MeasureSpec.AT_MOST:
	    height = Math.min(Math.max(200, width), MeasureSpec.getSize(heightMeasureSpec));
	    break;
	    
	case MeasureSpec.EXACTLY:
	    height = MeasureSpec.getSize(heightMeasureSpec);
	    break;
	    
	case MeasureSpec.UNSPECIFIED:
	    height = Math.max(200, width);
	    break;
	    
	default:
	    throw new IllegalArgumentException();
	}

	setMeasuredDimension(width, height);
    }
    
    private Paint bgPaint = new Paint();
    private Paint dotPaint = new Paint();
    private Paint textPaint = new Paint();
    {
	bgPaint.setColor(Color.BLUE);
	dotPaint.setColor(Color.GREEN);
	textPaint.setColor(Color.WHITE);
	textPaint.setTextAlign(Align.CENTER);
	textPaint.setTextSize(60);
	textPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
	super.onDraw(canvas);

	final int MARG = 5;

	bgPaint.setColor(Color.BLUE);
	bgPaint.setStyle(Style.FILL);
	canvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/2-MARG, bgPaint);
	bgPaint.setColor(Color.WHITE);
	bgPaint.setStyle(Style.STROKE);
	canvas.drawCircle(getWidth()/2, getHeight()/2, getWidth()/2-MARG, bgPaint);

	final int R2 = 15;
	final int R = getWidth()/2 - MARG - R2 - 10;
	float rad = control.getAngle();

	int x = (int) (getWidth()/2 + R*Math.cos(rad));
	int y = (int) (getHeight()/2 + R*Math.sin(rad));

	canvas.drawCircle(x, y, R2, dotPaint);
	
	canvas.drawText(String.format("%.1f", control.getValue()), getWidth()/2, getHeight()/2-textPaint.ascent()/2, textPaint);
    }

    public void setVolume(float param) {
	control.setVolume(param);
    }
    
    public float getVolume() {
	return control.getValue();
    }
}

