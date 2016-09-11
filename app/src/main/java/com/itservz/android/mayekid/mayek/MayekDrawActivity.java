package com.itservz.android.mayekid.mayek;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

import com.facebook.FacebookSdk;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.appevents.AppEventsLogger;
import com.itservz.android.mayekid.BaseActivity;
import com.itservz.android.mayekid.R;
import com.itservz.android.mayekid.utils.BackgroundMusicFlag;
import com.itservz.android.mayekid.utils.MayekCard;
import com.itservz.android.mayekid.utils.MayekSoundPoolPlayer;
import com.itservz.android.mayekid.utils.Mayeks;
import com.itservz.android.mayekid.utils.SoundPoolPlayer;

import java.util.List;

public class MayekDrawActivity extends BaseActivity implements View.OnClickListener {

    private MayekDrawView currentDrawView;
    private ImageView currPaint, drawBtn, soundBtn, newBtn, opacityBtn, nextBtn, previousBtn;
    private int[] imageIds;
    private int imageId;
    private Animation animation;
    private View animatedView;
    private ViewFlipper viewFlipper;
    private List<MayekCard> mayeks;
    private SoundPoolPlayer soundPoolPlayer;
    private MayekSoundPoolPlayer mayekSoundPoolPlayer;
    private Animation slowAnimation;
    private AdView adViewFacebook;

    private void setFlipperImage(int res) {
        MayekDrawView image = new MayekDrawView(getApplicationContext());
        image.setBackgroundResource(res);
        image.setTag(res);
        viewFlipper.addView(image);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_mayek_draw);

        //ads start - facebook - Initialize the SDK before executing any other operations,
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        //AdSettings.addTestDevice("428c356f11e7ebfb8aba611cf2dabe19");
        adViewFacebook = new AdView(this, "1782121292033969_1785195955059836", AdSize.BANNER_HEIGHT_50);
        LinearLayout layout = (LinearLayout)findViewById(R.id.mayekAdView);
        layout.addView(adViewFacebook);
        adViewFacebook.loadAd();

        //ads end
        Intent intent = getIntent();
        imageId = intent.getIntExtra("imageId", 0);
        imageIds = intent.getIntArrayExtra("imageIds");

        viewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        for (int i = 0; i < imageIds.length; i++) {
            setFlipperImage(imageIds[i]);
        }
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        soundPoolPlayer = new SoundPoolPlayer(getApplicationContext());
        mayekSoundPoolPlayer = new MayekSoundPoolPlayer(getApplicationContext());
        if (!wentToAnotherActivity && BackgroundMusicFlag.getInstance().isSoundOnOff()) {
            startService(backgroundMusicService);
        }
        wentToAnotherActivity = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        soundPoolPlayer.release();
        mayekSoundPoolPlayer.release();
        if (!wentToAnotherActivity && BackgroundMusicFlag.getInstance().isSoundOnOff()) {
            stopService(backgroundMusicService);
        }
    }

    @Override
    public void onDestroy() {
        if (adViewFacebook != null) {
            adViewFacebook.destroy();
        }
        super.onDestroy();
    }

    private void init() {
        //draw button
        drawBtn = (ImageView) findViewById(R.id.draw_btn);
        drawBtn.setOnClickListener(this);

        //erase button
        soundBtn = (ImageView) findViewById(R.id.sound_btn);
        soundBtn.setOnClickListener(this);

        //new button
        newBtn = (ImageView) findViewById(R.id.new_btn);
        newBtn.setOnClickListener(this);

        //opacity
        opacityBtn = (ImageView) findViewById(R.id.opacity_btn);
        opacityBtn.setOnClickListener(this);

        //previous button
        previousBtn = (ImageView) findViewById(R.id.previous_btn);
        previousBtn.setOnClickListener(this);

        //next
        nextBtn = (ImageView) findViewById(R.id.next_btn);
        nextBtn.setOnClickListener(this);

        animation = AnimationUtils.loadAnimation(this, R.anim.paint_animation);
        slowAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_animation);
        initCurrentView();
    }

    private String getMayekName(int imageId) {
        mayeks = Mayeks.getInstance().getCards();
        for (MayekCard mayek : mayeks) {
            if (mayek.getRes() == imageId) {
                return mayek.getTitle();
            }
        }
        return "";
    }

    private void initCurrentView() {
        currentDrawView = (MayekDrawView) viewFlipper.findViewWithTag(imageId);
        currentDrawView.setMayekName(getMayekName(imageId));
        viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(currentDrawView));
        currentDrawView.startAnimation(slowAnimation);
        float alpha = 0.9f;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            currentDrawView.setAlpha(alpha);
        }
        //get the palette and first color button
        LinearLayout paintLayout = (LinearLayout) findViewById(R.id.paint_colors);
        currPaint = (ImageButton) paintLayout.getChildAt(0);
        currentDrawView.setColor(currPaint.getTag().toString());
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    public void paintClicked(View view) {
        currentDrawView.setErase(false);
        currentDrawView.setPaintAlpha(100);
        currentDrawView.setBrushSize(currentDrawView.getLastBrushSize());

        if (view != currPaint) {
            ImageButton imageButton = (ImageButton) view;
            String color = view.getTag().toString();
            currentDrawView.setColor(color);
            //update ui
            imageButton.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint = (ImageButton) view;
        }
    }

    @Override
    public void onClick(View view) {
        currentDrawView.clearAnimation();
        if (animatedView != null) {
            animatedView.clearAnimation();
        }
        if (view.getId() == R.id.draw_btn) {
            soundPoolPlayer.playShortResource(R.raw.click);
            animatedView = animate(view);
            currentDrawView.brushSizeAction(this);

        } else if (view.getId() == R.id.sound_btn) {
            animatedView = animate(view);
            mayekSoundPoolPlayer.playShortResource(imageId);

        } else if (view.getId() == R.id.new_btn) {
            soundPoolPlayer.playShortResource(R.raw.click);
            animatedView = animate(view);
            currentDrawView.startNew();

        } else if (view.getId() == R.id.opacity_btn) {
            soundPoolPlayer.playShortResource(R.raw.click);
            animatedView = animate(view);
            currentDrawView.changeOpacity(this);

        } else if (view.getId() == R.id.next_btn) {
            soundPoolPlayer.playShortResource(R.raw.click);
            animatedView = animate(view);
            for (int i = 0; i < imageIds.length; i++) {
                if (imageId == imageIds[i] && i < imageIds.length - 1) {
                    imageId = imageIds[i + 1];
                    viewFlipper.showNext();
                    currentDrawView = (MayekDrawView) viewFlipper.getCurrentView();
                    currentDrawView.startAnimation(slowAnimation);
                    currentDrawView.setMayekName(getMayekName(imageId));
                    break;
                }
            }
        } else if (view.getId() == R.id.previous_btn) {
            soundPoolPlayer.playShortResource(R.raw.click);
            for (int i = 0; i < imageIds.length; i++) {
                if (imageId == imageIds[i] && i > 0) {
                    imageId = imageIds[i - 1];
                    viewFlipper.showPrevious();
                    currentDrawView = (MayekDrawView) viewFlipper.getCurrentView();
                    currentDrawView.startAnimation(slowAnimation);
                    currentDrawView.setMayekName(getMayekName(imageId));
                    break;
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        wentToAnotherActivity = true;
    }

    private View animate(View imageView) {
        imageView.startAnimation(animation);
        return imageView;
    }
}
