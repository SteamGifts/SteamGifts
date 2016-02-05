package net.mabako.steamgifts.fragments.images;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.decoder.DecoderFactory;
import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;
import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttpDownloader;

import net.mabako.steamgifts.core.R;

import java.io.IOException;

import okio.Okio;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifIOException;
import pl.droidsonroids.gif.GifImageView;

/**
 * A single image on a single page.
 */
// TODO this probably needs some overhaul in terms of lifecycle management.
public class ImageFragment extends Fragment {
    private static final String TAG = ImageFragment.class.getSimpleName();
    private static final String ARG_URL = "image-url";

    private String url;
    private FetchImage fetchImage;

    private Bitmap bitmap;
    private GifDrawable gifDrawable;
    private SubsamplingScaleImageView imageView;

    public static ImageFragment newInstance(String imageUrl) {
        ImageFragment fragment = new ImageFragment();

        Bundle args = new Bundle();
        args.putString(ARG_URL, imageUrl);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getArguments().getString(ARG_URL);
        if (TextUtils.isEmpty(url))
            throw new IllegalStateException("No URL passed");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.image_item, container, false);

        // What if there's no saved instance state?
        if (savedInstanceState == null) {
            fetchImage = new FetchImage();
            fetchImage.execute();
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (fetchImage != null)
            fetchImage.cancel(true);

        if (imageView != null)
            imageView.recycle();

        if (bitmap != null)
            bitmap.recycle();

        if (gifDrawable != null)
            gifDrawable.recycle();

        super.onDestroy();
    }

    private void setImage(@NonNull final byte[] bytes) {
        View view = getView();
        if (view == null)
            return;

        view.findViewById(R.id.progressBar).setVisibility(View.GONE);

        try {
            setImage(new GifDrawable(bytes));
            return;
        } catch (GifIOException e) {
            // Not a gif.
            Log.v(TAG, "image is not a gif: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "IOException while parsing GIF " + url, e);
        }

        // Set a bitmap, whoo!

        imageView = (SubsamplingScaleImageView) view.findViewById(R.id.image);
        imageView.setBitmapDecoderFactory(new DecoderFactory<ImageDecoder>() {
            @Override
            public ImageDecoder make() throws IllegalAccessException, java.lang.InstantiationException {
                return new ImageDecoder() {
                    @Override
                    public Bitmap decode(Context context, Uri uri) throws Exception {
                        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    }
                };
            }
        });
        imageView.setRegionDecoderFactory(new DecoderFactory<ImageRegionDecoder>() {
            @Override
            /**
             * Since it is downright foolish to load the entire bitmap in memory (I've tried, to no avail), use this decoder to keep it to a minimum.
             */
            public ImageRegionDecoder make() throws IllegalAccessException, java.lang.InstantiationException {
                return new ImageRegionDecoder() {
                    private final Object decoderLock = new Object();
                    private BitmapRegionDecoder decoder;

                    @Override
                    public Point init(Context context, Uri uri) throws Exception {
                        decoder = BitmapRegionDecoder.newInstance(bytes, 0, bytes.length, true);
                        return new Point(decoder.getWidth(), decoder.getHeight());
                    }

                    @Override
                    public Bitmap decodeRegion(Rect rect, int sampleSize) {
                        synchronized (this.decoderLock) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = sampleSize;
                            options.inPreferredConfig = Bitmap.Config.RGB_565;
                            Bitmap bitmap = this.decoder.decodeRegion(rect, options);
                            if (bitmap == null) {
                                imageView.setVisibility(View.GONE);
                                showOpenInBrowserLink();
                                return null;
                            } else {
                                return bitmap;
                            }
                        }
                    }

                    @Override
                    public boolean isReady() {
                        return decoder != null && !decoder.isRecycled();
                    }

                    @Override
                    public void recycle() {
                        decoder.recycle();
                    }
                };
            }
        });
        imageView.setImage(ImageSource.uri(url));
        imageView.setVisibility(View.VISIBLE);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setImage(GifDrawable gifDrawable) {
        View view = getView();
        if (view == null) {
            gifDrawable.recycle();
            return;
        }
        this.gifDrawable = gifDrawable;

        GifImageView imageView = (GifImageView) view.findViewById(R.id.gif);
        imageView.setImageDrawable(gifDrawable);
        imageView.setVisibility(View.VISIBLE);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    private void showOpenInBrowserLink() {
        View view = getView();
        if (view == null)
            return;

        view.findViewById(R.id.progressBar).setVisibility(View.GONE);
        view.findViewById(R.id.image_not_loaded).setVisibility(View.VISIBLE);
        view.findViewById(R.id.open_browser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });

    }

    private class FetchImage extends AsyncTask<Void, Void, byte[]> {
        @Override
        protected byte[] doInBackground(Void... params) {
            try {
                OkHttpDownloader downloader = new OkHttpDownloader(getContext());
                Downloader.Response response = downloader.load(Uri.parse(url), 0);

                return Okio.buffer(Okio.source(response.getInputStream())).readByteArray();
            } catch (Exception e) {
                Log.d(ImageFragment.class.getSimpleName(), "Error fetching image", e);
                return null;
            }
        }

        @Override
        @SuppressWarnings("deprecation")
        protected void onPostExecute(byte[] response) {
            if (response != null) {
                setImage(response);
            } else {
                showOpenInBrowserLink();
            }
        }
    }
}
