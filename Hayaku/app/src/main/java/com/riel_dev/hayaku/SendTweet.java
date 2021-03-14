package com.riel_dev.hayaku;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import static com.riel_dev.hayaku.MainActivity.KEY_TWEET;
import static com.riel_dev.hayaku.MainActivity.NOTIFICATION_ID;

public class SendTweet extends AppCompatActivity {

    boolean isAlreadyLoggedInToTwitter;
    ConfigurationBuilder configurationBuilder;
    TwitterFactory twitterFactory;
    Twitter twitter;

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("onNewIntent 실행: ", "됨");
        super.onNewIntent(intent);
        processInlineReply(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_tweet);
        isAlreadyLoggedInToTwitter = CustomPreferenceManager.getBoolean(getApplicationContext(), "login");
        if (isAlreadyLoggedInToTwitter) {
            configurationBuilder = new ConfigurationBuilder();
            configurationBuilder.setOAuthConsumerKey(getString(R.string.consumer_key));
            configurationBuilder.setOAuthConsumerSecret(getString(R.string.consumer_key_secret));
            configurationBuilder.setOAuthAccessToken(CustomPreferenceManager.getString(getApplicationContext(), "access_token"));
            configurationBuilder.setOAuthAccessTokenSecret(CustomPreferenceManager.getString(getApplicationContext(), "access_secret"));
            twitterFactory = new TwitterFactory(configurationBuilder.build());
            twitter = twitterFactory.getInstance();
        }
    }
    public void processInlineReply (Intent intent){
        Bundle tweetBundle = RemoteInput.getResultsFromIntent(intent);
        Log.d("remoteInput", "들어옴");
        if (tweetBundle != null) {
            Log.d("remoteInput", "들어옴");
            CharSequence charSequence = tweetBundle.getCharSequence(KEY_TWEET);
            Toast.makeText(getApplicationContext(), charSequence, Toast.LENGTH_LONG).show();

            Thread sendTwitter = new Thread(new Runnable() {
                @Override
                public void run() {
                    twitter = TwitterFactory.getSingleton();
                    try {
                        Log.d("Status update", "true");
                        Status status = twitter.updateStatus((String) charSequence);
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                }
            });
            sendTwitter.start();

            //Update the notification to show that the reply was received.
            NotificationCompat.Builder repliedNotification =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(
                                    android.R.drawable.stat_notify_chat)
                            .setContentText("Tweet Sent!");

            NotificationManager notificationManager =
                    (NotificationManager)
                            getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID,
                    repliedNotification.build());

        }
    }
}