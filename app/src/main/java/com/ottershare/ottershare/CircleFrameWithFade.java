package com.ottershare.ottershare;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.RelativeLayout;

import static android.R.attr.animation;


public class CircleFrameWithFade extends Fragment {

    RelativeLayout shutterLayoutGroup;

    private Animation fadeOut;
    private Animation fadeIn;

    public CircleFrameWithFade() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setfadeOutAnim();
        setfadeInAnim();


    }

    private void setfadeInAnim() {
        fadeIn = new AlphaAnimation(0,1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(500);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                shutterLayoutGroup.setAlpha(1);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void setfadeOutAnim() {
        fadeOut = new AlphaAnimation(1,0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(500);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                shutterLayoutGroup.setAlpha(1);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                shutterLayoutGroup.setAlpha(0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }


        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_circle_frame_with_fade, container, false);

        shutterLayoutGroup = (RelativeLayout) view.findViewById(R.id.shutter_layout_group);

        return view;
    }

    public void fadeOutAnimation(){shutterLayoutGroup.startAnimation(fadeOut);}

    public void fadeInAnimation(){shutterLayoutGroup.startAnimation(fadeIn);}

}
