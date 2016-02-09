package net.mabako.steam.store.viewholder;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class StoreImageGetter implements Html.ImageGetter {
    final Resources resources;
    final Picasso picasso;
    final TextView textView;

    public StoreImageGetter(final TextView textView, final Resources resources, final Picasso picasso) {
        this.textView = textView;
        this.resources = resources;
        this.picasso = picasso;
    }

    @Override
    public Drawable getDrawable(final String source) {
        Uri uri = Uri.parse(source);
        if (!"cdn.akamai.steamstatic.com".equals(uri.getHost()))
            return null;

        final BitmapDrawablePlaceHolder result = new BitmapDrawablePlaceHolder(resources);

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(final Void... meh) {
                try {
                    return picasso.load(source).get();
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final Bitmap bitmap) {
                try {
                    final BitmapDrawable drawable = new BitmapDrawable(resources, bitmap);

                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                    result.setDrawable(drawable);
                    result.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                    textView.setText(textView.getText());
                } catch (Exception e) {
                }
            }

        }.execute((Void) null);

        return result;
    }

    static class BitmapDrawablePlaceHolder extends BitmapDrawable {
        protected Drawable drawable;

        @SuppressWarnings("deprecation")
        public BitmapDrawablePlaceHolder(Resources res) {
            super(res);
        }

        @Override
        public void draw(final Canvas canvas) {
            if (drawable != null) {
                drawable.draw(canvas);
            }
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

    }
}