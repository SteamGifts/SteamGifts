package net.mabako.steamgifts.intro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.mikepenz.iconics.context.IconicsContextWrapper;

import net.mabako.steamgifts.core.R;

public class IntroActivity extends AppIntro2 {
    public static final String INTRO_MAIN = "main";
    public static final int INTRO_MAIN_VERSION = 2;

    public static void showIntroIfNeccessary(Activity parentActivity, final String type, final int version) {
        SharedPreferences sp = parentActivity.getSharedPreferences("intro", MODE_PRIVATE);
        int lastSeenVersion = sp.getInt(type, 0);
        if (lastSeenVersion < version) {
            // Show the activity
            Intent intent = new Intent(parentActivity, IntroActivity.class);
            intent.putExtra("type", type);
            parentActivity.startActivity(intent);

            SharedPreferences.Editor spe = sp.edit();
            spe.putInt(type, version);
            spe.apply();
        }
    }

    public static void showIntro(Activity parentActivity, final String type) {
        Intent intent = new Intent(parentActivity, IntroActivity.class);
        intent.putExtra("type", type);
        parentActivity.startActivity(intent);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void init(Bundle savedInstanceState) {
        switch (getIntent().getStringExtra("type")) {
            case INTRO_MAIN:
                setIndicatorColor(getResources().getColor(R.color.colorAccent), getResources().getColor(android.R.color.darker_gray));

                addSlide(Slide.newInstance(SubView.MAIN_WELCOME));
                addSlide(Slide.newInstance(SubView.MAIN_GIVEAWAY_1));
                addSlide(Slide.newInstance(SubView.MAIN_GIVEAWAY_2));
                addSlide(Slide.newInstance(SubView.MAIN_GIVEAWAY_3));
                break;
        }
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
        finish();
    }

    @Override
    public void onSlideChanged() {

    }

    /**
     * Allow icons to be used in {@link android.widget.TextView}
     *
     * @param newBase
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }
}
