package net.mabako.steamgifts.fragments;

import android.support.v4.app.Fragment;

public class UserDetailFragment extends Fragment {
    public static final String ARG_USER = "user";

    private String userName;

    public static UserDetailFragment newInstance(String user) {
        UserDetailFragment fragment = new UserDetailFragment();
        fragment.userName = user;
        return fragment;
    }
}
