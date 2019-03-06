package g3.coveventry.customviews;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;

import g3.coveventry.R;


public class CovImageView extends FrameLayout {
    CardView cardView;
    ImageView imgView;
    Drawable placeholder;

    public CovImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CovImageView, 0, 0);
        FrameLayout parent = this;

        try
        {
            // Get the defined round attribute
            if (a.getBoolean(R.styleable.CovImageView_round, false))
            {
                cardView = new CardView(getContext());
                cardView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                cardView.setRadius(getWidth() / 2);

                addView(cardView);
                parent = cardView;
            }

            // Get the defined placeholder attribute
            placeholder = a.getDrawable(R.styleable.CovImageView_placeholder);
        }
        finally
        {
            a.recycle();
        }


        imgView = new ImageView(getContext());
        imgView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        resetImage();

        parent.addView(imgView);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // On layout created set cardView corner radius
        if(cardView != null)
            cardView.setRadius(getWidth() / 2);
    }


    /**
     * Set the image on the image view inside this view
     * @param image Image to be set on the image view
     */
    public void setImageBitmap(Bitmap image){
        imgView.setImageBitmap(image);
    }


    /**
     * Download the image from the web to the image view
     *
     * @param url Url of the image to download
     */
    public void setImageBitmap(URL url){
        // Start background task to download image
        new DownloadImage(new WeakReference<>(this)).execute(url);
    }


    /**
     * Download the image from the web to the image view and save it into a file
     *
     * @param url Url of the image to download
     * @param filePath Path to the file where to save the image
     */
    public void setImageBitmap(URL url, String filePath){
        // Start background task to download image
        new DownloadImage(new WeakReference<>(this), filePath).execute(url);
    }


    /**
     * Set image to a resource id drawable, with a specific theme
     *
     * @param drawable ID for the reseource drawable to load into the view
     * @param theme    Theme for the drawable
     */
    public void setImageDrawable(@DrawableRes int drawable, @Nullable Resources.Theme theme) {
        imgView.setImageDrawable(getResources().getDrawable(drawable, theme));
    }


    /**
     * Show drawable on view
     *
     * @param drawable Drawable to show
     */
    public void setImageDrawable(Drawable drawable) {
        imgView.setImageDrawable(drawable);
    }


    /**
     * Reset image to the user silhouette
     */
    public void resetImage()
    {
        imgView.setImageDrawable(placeholder);
    }

    /**
     * Downloads an image from the given URL to the given RoundImage view
     */
    private static class DownloadImage extends AsyncTask<URL, Void, Bitmap> {
        WeakReference<CovImageView> covImgView;
        String filePath = null;

        /**
         * With a reference to a CovImageView where to put the downloaded image
         *
         * @param covImgView Weak reference to the CovImageView
         */
        DownloadImage(WeakReference<CovImageView> covImgView) {
            this.covImgView = covImgView;
        }

        /**
         * With a reference to a CovImageView where to put the downloaded image and a
         * path where to save the image
         *
         * @param covImgView Weak reference to the CovImageView
         * @param filePath Path to the file where to save the image
         */
        DownloadImage(WeakReference<CovImageView> covImgView, String filePath) {
            this.covImgView = covImgView;
            this.filePath = filePath;
        }

        @Override
        protected Bitmap doInBackground(URL... urls) {
            Bitmap image = null;

            try {
                // Download image from url
                image = BitmapFactory.decodeStream(urls[0].openConnection().getInputStream());

                if (filePath != null)
                {
                    // Save image to file
                    FileOutputStream fOutStream = null;
                    try {
                        fOutStream = covImgView.get().getContext().openFileOutput(filePath, Context.MODE_PRIVATE);

                        image.compress(Bitmap.CompressFormat.PNG, 100, fOutStream);

                    } catch (Exception e) {
                        e.printStackTrace();

                    } finally {
                        if (fOutStream != null) {
                            try {
                                fOutStream.close();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return image;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            CovImageView ciView = covImgView.get();
            if (bitmap != null && ciView != null) {
                // Set user photo
                ciView.setImageBitmap(bitmap);
            }
        }
    }
}
