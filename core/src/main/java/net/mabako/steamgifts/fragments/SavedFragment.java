package net.mabako.steamgifts.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;

public class SavedFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_saved, container, false);

        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null)
            toolbar.setTitle(R.string.navigation_saved_elements);

        ViewPager viewPager = (ViewPager) layout.findViewById(R.id.viewPager);
        TitledPagerAdapter viewPagerAdapter = new TitledPagerAdapter((AppCompatActivity) getActivity(), viewPager, new SavedGiveawaysFragment(), new SavedDiscussionsFragment());
        viewPager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = (TabLayout) layout.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        return layout;
    }

    private class TitledPagerAdapter extends FragmentAdapter {
        public TitledPagerAdapter(AppCompatActivity activity, ViewPager viewPager, Fragment... fragments) {
            super(getChildFragmentManager(), activity, viewPager, fragments);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Fragment fragment = getItem(position);
            if (fragment instanceof IActivityTitle)
                return getString(((IActivityTitle) fragment).getTitleResource());
            return null;
        }
    }
}
