package ogma.com.punjabirasoi.utility;

/**
 * Created by User on 21-02-2017.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * A RelativeLayout that will always be aspect width and height,
 * where the height is based off the width.
 */
public class AspectRelativeLayout extends RelativeLayout {

    public AspectRelativeLayout(Context context) {
        super(context);
    }

    public AspectRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AspectRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Set a square layout.
        super.onMeasure(widthMeasureSpec, (widthMeasureSpec * 9) / 16);
    }

}