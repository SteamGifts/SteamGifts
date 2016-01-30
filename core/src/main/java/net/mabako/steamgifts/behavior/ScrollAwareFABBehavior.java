package net.mabako.steamgifts.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.FragmentAdapter;

/**
 * Floating Action Button that is hiding if you're scrolling down a {@link RecyclerView}, and is
 * visible if you're beyond the first element and scroll up again.
 */
public class ScrollAwareFABBehavior extends FloatingActionButton.Behavior {
    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        if (child.getTag() == null) {
            return;
        }

        // try to get the current page, if there's any.
        View view = coordinatorLayout;
        ViewPager viewPager = (ViewPager) coordinatorLayout.findViewById(R.id.viewPager);
        if (viewPager != null && viewPager.getAdapter() instanceof FragmentAdapter) {
            int currentPage = viewPager.getCurrentItem();
            FragmentAdapter pagerAdapter = (FragmentAdapter) viewPager.getAdapter();

            Fragment currentItem = pagerAdapter.getItem(currentPage);

            view = currentItem.getView();
        }

        if (view == null) {
            // We do not have a view we can immediately find.
            child.hide();
            return;
        }

        // Hide if we're over the first item
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        if (recyclerView != null && recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager.findFirstVisibleItemPosition() == 0) {
                child.hide();
            } else if (dyConsumed > 1 && child.getVisibility() == View.VISIBLE)
                child.hide();
            else if (dyConsumed < 1 && child.getVisibility() != View.VISIBLE)
                child.show();
        } else {
            // no recyclerview to attach to?
            child.hide();
        }
    }
}
