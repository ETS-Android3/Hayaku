package com.riel_dev.hayaku;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class SendTweetService extends Service {

    RemoteInput remoteInput;
    public static final String KEY_TWEET = "key_tweet";
    boolean isAlreadyLoggedInToTwitter;
    ConfigurationBuilder configurationBuilder;
    TwitterFactory twitterFactory;
    Status status;
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("onStartCommand", "실행됨");
        isAlreadyLoggedInToTwitter = CustomPreferenceManager.getBoolean(getApplicationContext(),"login");
        if(isAlreadyLoggedInToTwitter) {
            configurationBuilder = new ConfigurationBuilder();
            configurationBuilder.setOAuthConsumerKey(getString(R.string.consumer_key));
            configurationBuilder.setOAuthConsumerSecret(getString(R.string.consumer_key_secret));
            configurationBuilder.setOAuthAccessToken(CustomPreferenceManager.getString(getApplicationContext(), "access_token"));
            configurationBuilder.setOAuthAccessTokenSecret(CustomPreferenceManager.getString(getApplicationContext(), "access_secret"));
            twitterFactory = new TwitterFactory(configurationBuilder.build());
            Thread sendTwitter = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        twitter = twitterFactory.getInstance();
                        String tweetString = (String) getTweetText(intent);
                        status = twitter.updateStatus(tweetString);
                    } catch (TwitterException | NullPointerException exception) {
                        exception.printStackTrace();
                    }
                }
            });
            sendTwitter.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private CharSequence getTweetText(Intent intent){
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if(remoteInput != null){
            return remoteInput.getCharSequence(KEY_TWEET);
        }
        return null;
    }

}
