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
public class ImageFragment extends Fragment {
    private static final String TAG = ImageFragment.class.getSimpleName();
    private static final String ARG_URL = "image-url";

    private static final String SAVED_STATE = "image-state";
    private static final String SAVED_IMAGE_BYTES = "image-bytes";

    /**
     * URL of the image to display.
     */
    private String url;

    /**
     * Task to fetch the current image.
     */
    private FetchImageTask fetchImageTask;

    /**
     * Drawable used for the GIF file.
     */
    private GifDrawable gifDrawable;

    /**
     * Image View used for PNG/JPG files.
     */
    private SubsamplingScaleImageView imageView;

    /**
     * Image, in bytes.
     */
    private byte[] imageBytes;

    /**
     * Current fragment state.
     */
    private State state = State.NONE;

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

        if (savedInstanceState != null) {
            state = (State) savedInstanceState.getSerializable(SAVED_STATE);
            imageBytes = savedInstanceState.getByteArray(SAVED_IMAGE_BYTES);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_STATE, state);
        outState.putByteArray(SAVED_IMAGE_BYTES, imageBytes);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.image_page_item, container, false);

        switch (state) {
            case NONE:
                fetchImageTask = new FetchImageTask();
                fetchImageTask.execute();
                break;

            case UNABLE_TO_LOAD:
                showOpenInBrowserLink(view);
                break;

            case GIF:
                try {
                    createGif(view);
                } catch (Exception e) {
                    Log.e(TAG, "Unable to restore bytes");
                    showOpenInBrowserLink(view);
                }
                break;

            case BITMAP:
                createBitmap(view);
                break;
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (fetchImageTask != null)
            fetchImageTask.cancel(true);

        if (imageView != null)
            imageView.recycle();

        if (gifDrawable != null)
            gifDrawable.recycle();

        super.onDestroy();
    }

    /**
     * Create an image from a byte array.
     *
     * @param imageBytes the image's byte array
     */
    private void createImage(byte[] imageBytes) {
        View view = getView();
        if (view == null) {
            Log.v(TAG, "createImage: no view");
            return;
        }

        this.imageBytes = imageBytes;
        if (imageBytes != null) {
            try {
                // Can we parse this as GIF?
                createGif(view);
                state = State.GIF;
                return;
            } catch (GifIOException e) {
                // Not a gif, probably a normal image
            } catch (IOException e) {
                Log.w(TAG, "IOException while parsing GIF " + url, e);
            }

            // Normal image.
            createBitmap(view);
            state = State.BITMAP;
        } else {
            showOpenInBrowserLink(view);
            state = State.UNABLE_TO_LOAD;
        }
    }


    /**
     * Create a bitmap from a PNG/JPG.
     *
     * @param fragmentRootView the root view of the current fragment
     * @see #createImage(byte[])
     * @see #onCreateView(LayoutInflater, ViewGroup, Bundle)
     */
    private void createBitmap(@NonNull final View fragmentRootView) {
        Log.v(TAG, "Creating bitmap for " + url);

        // Hide the progress bar
        fragmentRootView.findViewById(R.id.progressBar).setVisibility(View.GONE);

        imageView = (SubsamplingScaleImageView) fragmentRootView.findViewById(R.id.image);
        imageView.setBitmapDecoderFactory(new DecoderFactory<ImageDecoder>() {
            @Override
            public ImageDecoder make() throws IllegalAccessException, java.lang.InstantiationException {
                return new ImageDecoder() {
                    @Override
                    public Bitmap decode(Context context, Uri uri) throws Exception {
                        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
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
                        decoder = BitmapRegionDecoder.newInstance(imageBytes, 0, imageBytes.length, true);
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
                                showOpenInBrowserLink(fragmentRootView);
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

    /**
     * Setup the container for a GIF file.
     *
     * @param fragmentRootView the root view of the current fragment
     * @see #createImage(byte[])
     * @see #onCreateView(LayoutInflater, ViewGroup, Bundle)
     */
    private void createGif(@NonNull View fragmentRootView) throws IOException {
        // Create the drawable, this will throw an exception if it's not a valid gif.
        gifDrawable = new GifDrawable(imageBytes);
        Log.v(TAG, "Creating gif for " + url);

        // Hide the progress bar
        fragmentRootView.findViewById(R.id.progressBar).setVisibility(View.GONE);

        GifImageView imageView = (GifImageView) fragmentRootView.findViewById(R.id.gif);
        imageView.setImageDrawable(gifDrawable);
        imageView.setVisibility(View.VISIBLE);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    /**
     * Has no image, allow open in browser instead.
     *
     * @param fragmentRootView the root view of the current fragment
     * @see #createImage(byte[])
     * @see #onCreateView(LayoutInflater, ViewGroup, Bundle)
     */
    private void showOpenInBrowserLink(@NonNull View fragmentRootView) {
        Log.v(TAG, "unable to load image " + url);
        fragmentRootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
        fragmentRootView.findViewById(R.id.image_not_loaded).setVisibility(View.VISIBLE);
        fragmentRootView.findViewById(R.id.open_browser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });

    }

    /**
     * Task to load the image asynchronously.
     */
    private class FetchImageTask extends AsyncTask<Void, Void, byte[]> {
        @Override
        protected byte[] doInBackground(Void... params) {
            try {
                // Grab an input stream to the image
                OkHttpDownloader downloader = new OkHttpDownloader(getContext());
                Downloader.Response response = downloader.load(Uri.parse(url), 0);

                // Read the image into a byte array
                return Okio.buffer(Okio.source(response.getInputStream())).readByteArray();
            } catch (Exception e) {
                Log.d(ImageFragment.class.getSimpleName(), "Error fetching image", e);
                return null;
            }
        }

        @Override
        @SuppressWarnings("deprecation")
        protected void onPostExecute(byte[] response) {
            createImage(response);
        }
    }

    /**
     * Current state of the fragment.
     */
    private enum State {
        /**
         * No image loaded yet.
         */
        NONE,

        /**
         * An error while loading image.
         */
        UNABLE_TO_LOAD,

        /**
         * Non-animated image such as a PNG or JPG file.
         */
        BITMAP,

        /**
         * GIF file
         */
        GIF;
    }
}
