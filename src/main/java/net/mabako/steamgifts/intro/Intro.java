package net.mabako.steamgifts.intro;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;

import net.mabako.steamgifts.R;

public class Intro extends AppIntro2 {
    public static final String INTRO_MAIN = "main";
    public static final int INTRO_MAIN_VERSION = 1;

    public static void showIntroIfNeccessary(Activity parentActivity, String type, int version) {
        SharedPreferences sp = parentActivity.getSharedPreferences("intro", MODE_PRIVATE);
        int lastSeenVersion = sp.getInt(type, 0);
        if (lastSeenVersion < version) {
            // Show the activity
            Intent intent = new Intent(parentActivity, Intro.class);
            intent.putExtra("type", type);
            parentActivity.startActivity(intent);
        }
    }

    @Override
    public void init(Bundle savedInstanceState) {
        switch (getIntent().getStringExtra("type")) {
            case INTRO_MAIN:
                addSlide(Slide.newInstance(SubView.MAIN_GIVEAWAY_1));
                addSlide(Slide.newInstance(SubView.MAIN_GIVEAWAY_2));
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
}
