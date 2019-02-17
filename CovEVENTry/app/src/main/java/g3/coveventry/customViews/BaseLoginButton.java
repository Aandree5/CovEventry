package g3.coveventry.customViews;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;

import g3.coveventry.R;

/**
 * Class with defaults for login buttons
 */
public class BaseLoginButton extends AppCompatButton {
    public BaseLoginButton(Context context) {
        this(context, null);
    }

    public BaseLoginButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.buttonStyle);
    }

    public BaseLoginButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Resources res = getResources();

        // Set default text color
        setTextColor(Color.WHITE);

        // Temporary background color, so text is visible
        setBackgroundColor(Color.BLACK);

        // Set padding
        setCompoundDrawablePadding(res.getDimensionPixelSize(R.dimen.login_btn_drawable_padding));
        setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimensionPixelSize(R.dimen.login_btn_text_size));
        setTypeface(Typeface.DEFAULT_BOLD);
        setPadding(res.getDimensionPixelSize(R.dimen.login_btn_left_padding), 0, res.getDimensionPixelSize(R.dimen.login_btn_right_padding), 0);

        // Prevent text from defaulting to caps
        setAllCaps(false);
    }

    /**
     * Returns the activity, allowing the button to access it
     */
    protected Activity getActivity() {
        if (getContext() instanceof ContextThemeWrapper && ((ContextThemeWrapper) getContext()).getBaseContext() instanceof Activity) {
            return (Activity) ((ContextThemeWrapper) getContext()).getBaseContext();

        } else if (getContext() instanceof Activity) {
            return (Activity) getContext();

        } else if (isInEditMode()) {
            return null;

        } else {
            throw new IllegalStateException("Activity not found");
        }
    }
}
