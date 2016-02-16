package net.mabako.steamgifts.fragments.images;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImagePagerFragment extends Fragment {
    private static final String TAG = ImagePagerFragment.class.getSimpleName();

    private static final String ARG_IMAGES = "images";
    private List<Image> images;

    public static ImagePagerFragment newInstance(List<Image> images) {
        ImagePagerFragment fragment = new ImagePagerFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_IMAGES, new ArrayList<>(images));
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        images = (List<Image>) getArguments().getSerializable(ARG_IMAGES);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.image_pager_fragment, container, false);

        fixStatusBarSpacing(view);

        final ViewPager pager = (ViewPager) view.findViewById(R.id.viewPager);
        pager.setAdapter(new PagerAdapter(getChildFragmentManager()));

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = pager.getCurrentItem();
                try {
                    Image image = images.get(currentPosition);

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, image.getUrl());
                    intent.putExtra(Intent.EXTRA_SUBJECT, image.getTitle());
                    intent.setType("text/plain");
                    startActivity(Intent.createChooser(intent, getString(R.string.share_image)));
                } catch (IndexOutOfBoundsException e) {
                    Log.e(TAG, "Unable to share image cause none exists (" + currentPosition + ")", e);
                }
            }
        });

        ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    // Fetch the current item.
                    int currentPosition = pager.getCurrentItem();
                    Image image = images.get(currentPosition);

                    // Show the current image, out of the number of images
                    String index_and_title = String.format(Locale.getDefault(), "%d / %d", currentPosition + 1, images.size());

                    // Append an optional title.
                    if (!TextUtils.isEmpty(image.getTitle()))
                        index_and_title = String.format("%s: %s", index_and_title, image.getTitle());

                    ((TextView) view.findViewById(R.id.image_number)).setText(index_and_title);
                    ((TextView) view.findViewById(R.id.image_url)).setText(image.getUrl());
                }
            }
        };
        pager.addOnPageChangeListener(listener);
        listener.onPageScrollStateChanged(ViewPager.SCROLL_STATE_IDLE);

        return view;
    }

    /**
     * In particular, some windows might have android:fitsSystemWindows="true" set.
     */
    private void fixStatusBarSpacing(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // Not applicable to API 15 or below, since there is no translucent status bar(?)

            DetailActivity activity = (DetailActivity) getActivity();
            if (activity.getLayoutId() == R.layout.activity_giveaway_detail) {
                view.setPadding(0, getResources().getDimensionPixelSize(R.dimen.status_bar_height), 0, 0);
            }
        }
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.v(TAG, "image at position " + position + " is " + images.get(position));
            return ImageFragment.newInstance(images.get(position).getUrl());
        }

        @Override
        public int getCount() {
            return images.size();
        }
    }
}
