package thjread.organise;

import android.content.Context;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

//http://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures
public class OnSwipeTouchListener implements View.OnTouchListener {

    private GestureDetector gestureDetector;

    public OnSwipeTouchListener (Context ctx, View v, boolean longPress){
        gestureDetector = new GestureDetector(ctx, new GestureListener(v, longPress));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;
        private boolean startedTap = false;
        private View view;
        private boolean longPress;

        public GestureListener(View v, boolean longPress) {
            this.view = v;
            this.longPress = longPress;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            startedTap = true;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                    result = true;
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                } else {
                    onTap();
                }
                result = true;
                startedTap = false;

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (startedTap) {
                onTap();
                startedTap = false;
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (longPress) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            }
            onLongTap();
        }
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }

    public void onLongTap() {
    }

    public void onTap() {
    }
}
