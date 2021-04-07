package com.riel_dev.hayaku;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import java.util.Random;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/*
TODO
1. 로그아웃하면 알림창 없애게 해야됨 -> 안그러면 로그아웃 하고도 알림창이 남음.
2. 로그인 할 때 PIN 번호 잘못 입력하면 튕기는데 이거 어떻게? (일단 고침) -> 그냥 PIN 안누르게 하는게 더 좋을텐데 (딥 링크)
3. 로그인 직후 glide에서 사진을 못 받아오는 원인 찾고 고치기 (일단 고침)
4. 그리고 로그인 화면에서 휴대폰 인증 하면 왜 크롬으로 나가지는거임?
5. 트위터 프록시처럼 버튼 누르면 트위터에 글 쓸 수 있게 해주는거
6. 타임라인 가져오는 기능 추가
7. 앱 알림 뱃지 1 뜨는거 없애기
8. 아이콘 해상도 낮아보이는거 고치기 -> 나인패치 적용하면 되려나
9. 트위터 데이터도 그냥 캐싱했다가 불러오는거로 바꿀까? 지금 데이터 로딩되는게 너무 느린거같아 (아니면 스플래쉬 이미지로?)
10. 앱 아이콘 각도 수정하기 (위로 올라가는게 더 좋대)
 */

public class SendTweetService extends Service {

    RemoteInput remoteInput;
    public static final String KEY_TWEET = "key_tweet";
    boolean isAlreadyLoggedInToTwitter;
    ConfigurationBuilder configurationBuilder;
    NotificationCompat.Builder builder;
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
            configurationBuilder.setDebugEnabled(true);
            configurationBuilder.setOAuthConsumerKey(getString(R.string.consumer_key));
            configurationBuilder.setOAuthConsumerSecret(getString(R.string.consumer_key_secret));
            configurationBuilder.setOAuthAccessToken(CustomPreferenceManager.getString(getApplicationContext(), "access_token"));
            configurationBuilder.setOAuthAccessTokenSecret(CustomPreferenceManager.getString(getApplicationContext(), "access_secret"));
            twitterFactory = new TwitterFactory(configurationBuilder.build());
            twitter = twitterFactory.getInstance();
            Thread sendTwitter = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String tweetString = (String) getTweetText(intent);
                        if((tweetString!= null) && (CustomPreferenceManager.getBoolean(getApplicationContext(), "footerSwitch"))){
                            tweetString += " ";
                            tweetString += (String)CustomPreferenceManager.getString(getApplicationContext(), "footerTextPreference");
                            Log.d("Footer", tweetString);
                        }
                        status = twitter.updateStatus(tweetString);
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Tweet sent!", Toast.LENGTH_LONG).show();
                            }
                        },0);
                    } catch (TwitterException exception) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Error: " + exception, Toast.LENGTH_LONG).show();
                            }
                        },0);
                    }  catch (NullPointerException exception){
                        exception.printStackTrace();
                    }
                    show();
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

    private void show() {
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder = new NotificationCompat.Builder(this, "twitterId")
                .setSmallIcon(R.drawable.ic_twitter)
                .setContentTitle("Hayaku is running")
                .setContentIntent(resultPendingIntent)
                .setShowWhen(false)
                .setOngoing(true)
                .setContentText("Logged into " + CustomPreferenceManager.getString(getApplicationContext(), "twitterId"));
        Log.d("알림 생성", "성공");
        remoteInput = new RemoteInput.Builder(KEY_TWEET)
                .setLabel("What's happening?")
                .build();
        int randomRequestCode = new Random().nextInt(54325);
        Intent resultIntent2 = new Intent(getApplicationContext(), SendTweetService.class);
        PendingIntent tweetPendingIntent =
                PendingIntent.getService(getApplicationContext(),randomRequestCode, resultIntent2, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action tweetAction = new NotificationCompat.Action.Builder(R.drawable.ic_edit, "Tweet", tweetPendingIntent)
                .addRemoteInput(remoteInput)
                .setAllowGeneratedReplies(true)
                .build();

        builder.addAction(tweetAction);
        Log.d("트윗 액션 부착: ", "성공");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("twitterId", KEY_TWEET, NotificationManager.IMPORTANCE_LOW));
        }
        notificationManager.notify(0, builder.build());

    }


}
