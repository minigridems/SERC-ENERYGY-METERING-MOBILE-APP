package edu.strathmore.serc.sercopenenergymonitorv3;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;

import java.util.ArrayList;
import java.util.List;

public class HelpActivity extends AhoyOnboarderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AhoyOnboarderCard ahoyOnboarderCard1 = new AhoyOnboarderCard("Welcome",
                "This app is has been created to monitor energy use remotely using the EmonCMS platform",
                R.drawable.lightning_icon);
        AhoyOnboarderCard ahoyOnboarderCard2 = new AhoyOnboarderCard("Current Data",
                "To get the most current reading, swipe down to refresh the data",
                R.drawable.swipe_down_illust_2);
        AhoyOnboarderCard ahoyOnboarderCard3 = new AhoyOnboarderCard("Graph",
                "To access the graph, click on any location",
                R.drawable.easel_icon);
        AhoyOnboarderCard ahoyOnboarderCard4 = new AhoyOnboarderCard("Account",
                "In order to communicate with the platform, you will be requested to provide some information",
                R.drawable.color_profile_icon);
        AhoyOnboarderCard ahoyOnboarderCard5 = new AhoyOnboarderCard("API Key",
                "The first is the READ API key found in the EmonCms account page",
                R.drawable.key_icon);
        AhoyOnboarderCard ahoyOnboarderCard6 = new AhoyOnboarderCard("Root Link",
                "The second is the root HTTP link for where the platform is hosted",
                R.drawable.web_http_icon);
        AhoyOnboarderCard ahoyOnboarderCard7 = new AhoyOnboarderCard("More Info",
                "To access the settings, click on the 3 dot menu on the top right of the main screen",
                R.drawable.three_dot_blue);


        List<AhoyOnboarderCard> pages = new ArrayList<>();

        pages.add(ahoyOnboarderCard1);
        pages.add(ahoyOnboarderCard2);
        pages.add(ahoyOnboarderCard3);
        pages.add(ahoyOnboarderCard4);
        pages.add(ahoyOnboarderCard5);
        pages.add(ahoyOnboarderCard6);
        pages.add(ahoyOnboarderCard7);

        for (AhoyOnboarderCard page : pages) {
            page.setBackgroundColor(R.color.black_transparent);
            page.setTitleColor(R.color.white);
            page.setDescriptionColor(R.color.grey_200);
            page.setTitleTextSize(dpToPixels(10, this));
            page.setDescriptionTextSize(dpToPixels(6, this));
            //page.setIconLayoutParams(width, height, marginTop, marginLeft, marginRight, marginBottom);
        }

        setFinishButtonTitle("Finish");
        showNavigationControls(true);
        setGradientBackground();

        //set the button style you created
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setFinishButtonDrawableStyle(ContextCompat.getDrawable(this, R.drawable.rounded_button));
        }



        setOnboardPages(pages);
    }

    @Override
    public void onFinishButtonPressed() {
        Intent goToMain = new Intent(this, MainActivity.class);
        startActivity(goToMain);
    }
}
