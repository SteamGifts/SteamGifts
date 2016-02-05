package net.mabako.steamgifts.fragments.images;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.mabako.steamgifts.core.R;

import java.util.ArrayList;
import java.util.List;

public class ImagePagerFragment extends Fragment {
    private static final String ARG_IMAGES = "images";
    private List<String> images;

    public static ImagePagerFragment newInstance(List<String> images) {
        ImagePagerFragment fragment = new ImagePagerFragment();

        Bundle args = new Bundle();
        args.putStringArrayList(ARG_IMAGES, new ArrayList<>(images));
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        images = getArguments().getStringArrayList(ARG_IMAGES);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.image_pager_fragment, container, false);

        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        final ViewPager pager = (ViewPager) view.findViewById(R.id.viewPager);
        pager.setAdapter(new PagerAdapter(getFragmentManager()));

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
                    int currentPosition = pager.getCurrentItem();

                    ((TextView) view.findViewById(R.id.image_number)).setText(String.format("%d / %d", currentPosition + 1, images.size()));
                    ((TextView) view.findViewById(R.id.image_url)).setText(images.get(currentPosition));
                }
            }
        };
        pager.addOnPageChangeListener(listener);
        listener.onPageScrollStateChanged(ViewPager.SCROLL_STATE_IDLE);

        return view;
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.v(ImagePagerFragment.class.getSimpleName(), "image at position " + position + " is " + images.get(position));
            return ImageFragment.newInstance(images.get(position));
        }

        @Override
        public int getCount() {
            return images.size();
        }
    }
}
