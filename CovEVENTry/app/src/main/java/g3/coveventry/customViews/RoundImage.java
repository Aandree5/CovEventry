package g3.coveventry.customViews;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import g3.coveventry.R;

public class RoundImage extends CardView {
    ImageView imgView;

    public RoundImage(@NonNull Context context) {
        super(context);
        createImageView();
    }

    public RoundImage(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        createImageView();
    }

    public RoundImage(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        createImageView();
    }


    /**
     * Creates a new view to be used to show the images
     */
    private void createImageView(){
        imgView = new ImageView(getContext());
        imgView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        resetImage();

        addView(imgView);
    }


    /**
     * Set the image on the image view inside this view
     * @param image Image to be set on the image view
     */
    public void setImageBitmap(Bitmap image){
        imgView.setImageBitmap(image);
    }


    /**
     * Reset image to the user silhouette
     */
    public void resetImage()
    {
        imgView.setImageResource(R.drawable.ic_user_placeholder);
    }
}
