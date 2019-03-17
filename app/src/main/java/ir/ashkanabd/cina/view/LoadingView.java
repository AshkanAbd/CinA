package ir.ashkanabd.cina.view;

import android.content.Context;
import android.util.AttributeSet;
import com.tt.whorlviewlibrary.WhorlView;

/**
 * Created just for starting as view creates.
 * What super class doesn't support it
 */
public class LoadingView extends WhorlView {
    public LoadingView(Context context) {
        super(context);
        start();
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        start();
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        start();
    }
}
