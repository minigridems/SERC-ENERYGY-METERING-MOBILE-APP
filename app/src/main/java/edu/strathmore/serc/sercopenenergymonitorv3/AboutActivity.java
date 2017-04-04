package edu.strathmore.serc.sercopenenergymonitorv3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);


        // about_page_textview has links specified by putting <a> tags in the string
        // resource.  By default these links will appear but not
        // respond to user input.  To make them active, you need to
        // call setMovementMethod() on the TextView object.
        TextView aboutPageText = (TextView) findViewById(R.id.about_page_textview);
        aboutPageText.setMovementMethod(LinkMovementMethod.getInstance());

        // This makes the image clickable
        ImageView img = (ImageView)findViewById(R.id.about_page_image);
        img.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("http://serc.strathmore.edu/"));
                startActivity(intent);
            }
        });
    }
}
