package com.demo.retroboot;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {


    TextView resultText;

    Event event;
    public Event getEvent(){return event;}
    public void setEvent(Event event){this.event = event;}

    public void setApi(DemoServiceApi api) {
        this.api = api;
    }

    public DemoServiceApi getApi() {
        return api;
    }
    private DemoServiceApi api;
    private CompositeSubscription _subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        resultText = (TextView)findViewById(R.id.resulttext);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultText.setText("");
                Snackbar.make(view, "Making the call", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });

        setApi(_createEventServiceApi());
    }

    private DemoServiceApi _createEventServiceApi() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        String serviceHost = "http://10.0.2.2:8080/";//getResources().getString(R.string.service_host);

        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(
                serviceHost).addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(client);

        return builder.build().create(DemoServiceApi.class);

    }



    public void updateUiWithResult(){

        String title = getEvent().getTitle();
        resultText.setText(title);

    }

    public void getEcho(){
        _subscriptions.add(
                getApi().getEcho()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .unsubscribeOn(Schedulers.io())
                        .subscribe(new Subscriber<Event>() {
                            @Override
                            public void onCompleted() {
                                Timber.d("Call 1 completed");
                                updateUiWithResult();
                            }

                            @Override
                            public void onError(Throwable e) {

                                Timber.d("Call 1 completed in ERROR");
                                Timber.d("error", e);
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(Event event) {

                                Timber.d("login response received: ");

                                setEvent(event);
                            }
                        }));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
