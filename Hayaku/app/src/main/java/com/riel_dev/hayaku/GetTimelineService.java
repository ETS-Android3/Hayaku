package com.riel_dev.hayaku;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class GetTimelineService extends Service {

    boolean isAlreadyLoggedInToTwitter;
    ConfigurationBuilder configurationBuilder;
    TwitterFactory twitterFactory;
    Twitter twitter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isAlreadyLoggedInToTwitter = CustomPreferenceManager.getBoolean(getApplicationContext(),"login");
        if(isAlreadyLoggedInToTwitter) {
            configurationBuilder = new ConfigurationBuilder();
            configurationBuilder.setDebugEnabled(true);
            configurationBuilder.setOAuthConsumerKey(getString(R.string.consumer_key));
            configurationBuilder.setOAuthConsumerSecret(getString(R.string.consumer_key_secret));
            configurationBuilder.setOAuthAccessToken(CustomPreferenceManager.getString(getApplicationContext(), "access_token"));
            configurationBuilder.setOAuthAccessTokenSecret(CustomPreferenceManager.getString(getApplicationContext(), "access_secret"));
            twitterFactory = new TwitterFactory(configurationBuilder.build());
            twitter = twitterFactory.getInstance();
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
