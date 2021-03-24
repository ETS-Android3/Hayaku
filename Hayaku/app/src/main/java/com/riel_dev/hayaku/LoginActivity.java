package com.riel_dev.hayaku;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class LoginActivity extends AppCompatActivity {

    // Global Basic Type Objects
    boolean isAppropriatePinNumber;

    // Global View Type Objects
    WebView webView;
    EditText editText;
    Button button;

    // Global Twitter Type Objects
    AccessToken accessToken;
    Twitter twitter;

    /* Action Bar Menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }

    /* When Action Bar Menu Selected */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.otherBroswer_button){
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            String twitterAuthURL = bundle.getString("url");
            Intent openBroswerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(twitterAuthURL));
            startActivity(openBroswerIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Login");
        setContentView(R.layout.activity_login);

        /* Connect Views with findViewById */
        webView = findViewById(R.id.twitterLoginWebView);
        editText = findViewById(R.id.editTextNumberPassword2);
        button = findViewById(R.id.button);

        /* Get Intent From Main Activity */
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String twitterAuthURL = bundle.getString("url");
        RequestToken requestToken = (RequestToken)bundle.getSerializable("requestToken");
        Twitter twitter = (Twitter)bundle.getSerializable("twitter");

        /* Login with in app WebView Browser */
        webView.loadUrl(twitterAuthURL);
        webView.getSettings().setJavaScriptEnabled(true);

        /* When Next Button Clicked */
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread twitterLoginThread2 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String pinNumber = editText.getText().toString();
                        Log.d("Pin", pinNumber);
                        try {
                            isAppropriatePinNumber =true;
                            accessToken = twitter.getOAuthAccessToken(requestToken, pinNumber);
                        } catch (TwitterException e) {
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Unable to get the access token.\nPlease check your PIN number and try again.", Toast.LENGTH_LONG).show();
                                    isAppropriatePinNumber = false;
                                }},0);
                        }
                        if(isAppropriatePinNumber){
                            String access_token = accessToken.getToken();
                            String access_secret = accessToken.getTokenSecret();
                            CustomPreferenceManager.setString(getApplicationContext(), "access_token", access_token);
                            CustomPreferenceManager.setString(getApplicationContext(), "access_secret", access_secret);
                            Log.d("Access Token", access_token);
                            CustomPreferenceManager.setBoolean(getApplicationContext(), "firstLogin", true);
                            CustomPreferenceManager.setBoolean(getApplicationContext(), "login", true);
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            try {
                                User user = twitter.showUser(twitter.getId());
                                String twitterId = user.getScreenName();
                                Log.d("Twitter ID: ", twitterId);
                                CustomPreferenceManager.setString(getApplicationContext(), "twitterId", "\u0040" + twitterId);
                            } catch (TwitterException e) {
                                e.printStackTrace();
                            }
                            startActivity(intent);
                            finish();
                        }
                    }
                });
                twitterLoginThread2.start();
            }
        });
    }

}
