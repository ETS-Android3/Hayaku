package com.riel_dev.hayaku;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends AppCompatActivity {

    // Global Basic Types
    Boolean isAlreadyLoggedInToTwitter;
    String profilePicUrl;

    // Global View Type Objects
    CardView accountCard;
    ImageView imageView;
    TextView textView;
    TextView textView2;

    // Global Twitter Type Objects
    RequestToken requestToken;
    ConfigurationBuilder configurationBuilder;
    TwitterFactory twitterFactory;
    Twitter twitter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SettingsFragment settingsFragment;
        settingsFragment = (SettingsFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isNotificationOn = sharedPreferences.getBoolean("notification", false);
        if(isNotificationOn){
            // createNotification();
        }else{
            // removeNotification();
        }

        /* Connect Views with findViewById */
        accountCard = findViewById(R.id.twitterAccountCardView);
        imageView = (ImageView)findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);

        /* Bring Twitter Login Data with PreferenceManager then show into TextViews */
        isAlreadyLoggedInToTwitter = CustomPreferenceManager.getBoolean(getApplicationContext(),"login");
        if(isAlreadyLoggedInToTwitter){
            configurationBuilder = new ConfigurationBuilder();
            configurationBuilder.setOAuthConsumerKey(getString(R.string.consumer_key));
            configurationBuilder.setOAuthConsumerSecret(getString(R.string.consumer_key_secret));
            configurationBuilder.setOAuthAccessToken(CustomPreferenceManager.getString(getApplicationContext(), "access_token"));
            configurationBuilder.setOAuthAccessTokenSecret(CustomPreferenceManager.getString(getApplicationContext(), "access_secret"));
            twitterFactory = new TwitterFactory(configurationBuilder.build());
            twitter = twitterFactory.getInstance();
            Thread twitterDataLoadThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        /* TODO - Twitter Nickname & Twitter ID should be in shared preference
                        *       - Then change by shared preference                                */
                        // 트위터 인장 가져오기
                        User user = twitter.showUser(twitter.getId());
                        profilePicUrl = user.getOriginalProfileImageURLHttps();
                        CustomPreferenceManager.setString(getApplicationContext(), "profilePicUrl", profilePicUrl);
                        Log.d("프사좀", profilePicUrl);
                        // 트위터 닉네임 가져오기
                        String nickname = user.getName();
                        textView = findViewById(R.id.textView);
                        Log.d("nickname", nickname);
                        textView.setText(nickname);
                        // 트위터 아이디 가져오기
                        String twitterId = user.getScreenName();
                        textView2 = findViewById(R.id.textView2);
                        Log.d("twitterID", twitterId);
                        // @ in Unicode is \u0040
                        textView2.setText("\u0040" + twitterId);
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                }
            });
            twitterDataLoadThread.start();
        }


        /* Load Twitter Profile Image into ImageView */
        if(CustomPreferenceManager.getString(getApplicationContext(), "profilePicUrl") != null) {
            Log.d("Profile Pic Url", CustomPreferenceManager.getString(getApplicationContext(), "profilePicUrl"));
            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(20));
            Glide.with(MainActivity.this)
                    .load(Uri.parse(CustomPreferenceManager.getString(getApplicationContext(), "profilePicUrl")))
                    .apply(requestOptions)
                    .placeholder(R.drawable.egg)
                    .error(R.drawable.egg)
                    .into(imageView);
        }else{
            Glide.with(this).load(R.drawable.egg).into(imageView);
        }

        /* Describe When User touches CardView (Twitter Account Information) */
        accountCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If Application is not logged in to Twitter, go to LoginActivity
                if(!isAlreadyLoggedInToTwitter){
                    Thread twitterLoginThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final String consumer_key = getString(R.string.consumer_key);
                                final String consumer_key_secret = getString(R.string.consumer_key_secret);
                                ConfigurationBuilder builder = new ConfigurationBuilder();
                                builder.setOAuthConsumerKey(consumer_key);
                                builder.setOAuthConsumerSecret(consumer_key_secret);
                                Configuration configuration = builder.build();
                                TwitterFactory factory = new TwitterFactory(configuration);
                                twitter = factory.getInstance();
                                requestToken = twitter.getOAuthRequestToken();

                                String twitterAuthURL = requestToken.getAuthorizationURL();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                intent.putExtra("url", twitterAuthURL);
                                intent.putExtra("twitter", twitter);
                                intent.putExtra("requestToken", requestToken);
                                startActivity(intent);
                                finish();
                            } catch (TwitterException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    twitterLoginThread.start();
                }else{
                    // If Application is already logged in, show logout dialog
                    showLogoutDialog();
                }
            }
        });
    }

    /* Dialog For Twitter Logout */
    public void showLogoutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout?").setMessage("Do you want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CustomPreferenceManager.clear(getApplicationContext());
                Toast.makeText(getApplicationContext(), "Logout Success", Toast.LENGTH_SHORT).show();
                reload();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /* ReLoad View for some reasons such as Twitter data load */
    public void reload(){
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

}