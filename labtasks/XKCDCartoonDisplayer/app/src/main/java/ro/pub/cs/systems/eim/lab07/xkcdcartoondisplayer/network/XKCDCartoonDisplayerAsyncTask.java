package ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.R;
import ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.entities.XKCDCartoonInformation;
import ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.general.Constants;
import ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.general.Utilities;

public class XKCDCartoonDisplayerAsyncTask extends AsyncTask<String, Void, XKCDCartoonInformation> {

    private TextView xkcdCartoonTitleTextView;
    private ImageView xkcdCartoonImageView;
    private TextView xkcdCartoonUrlTextView;
    private Button previousButton, nextButton;
    private XKCDCartoonButtonClickListener clickListener;
    private XKCDCartoonButtonClickListener listener;

    private class XKCDCartoonButtonClickListener implements Button.OnClickListener {

        private String xkcdComicUrl;

        public XKCDCartoonButtonClickListener(String xkcdComicUrl) {
            this.xkcdComicUrl = xkcdComicUrl;
        }

        @Override
        public void onClick(View view) {
            new XKCDCartoonDisplayerAsyncTask(xkcdCartoonTitleTextView, xkcdCartoonImageView, xkcdCartoonUrlTextView, previousButton, nextButton).execute(xkcdComicUrl);
        }

    }

    public XKCDCartoonDisplayerAsyncTask(TextView xkcdCartoonTitleTextView, ImageView xkcdCartoonImageView, TextView xkcdCartoonUrlTextView, Button previousButton, Button nextButton) {
        this.xkcdCartoonTitleTextView = xkcdCartoonTitleTextView;
        this.xkcdCartoonImageView = xkcdCartoonImageView;
        this.xkcdCartoonUrlTextView = xkcdCartoonUrlTextView;
        this.previousButton = previousButton;
        this.nextButton = nextButton;
    }

    @Override
    public XKCDCartoonInformation doInBackground(String... urls) {
        XKCDCartoonInformation xkcdCartoonInformation = new XKCDCartoonInformation();

        // TODO exercise 5a)
        // 1. obtain the content of the web page (whose Internet address is stored in urls[0])
        // - create an instance of a HttpClient object
        // - create an instance of a HttpGet object
        // - create an instance of a ResponseHandler object
        // - execute the request, thus obtaining the web page source code

        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(urls[0]);
        String doc = "";
        String line;
        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();

            BufferedReader bufferedReader = Utilities.getReader(httpEntity);
            while((line = bufferedReader.readLine()) != null) {
                doc += line;
            }

            Document document = Jsoup.parse(doc);
            Log.i(Constants.TAG, document.text());
            Element htmlTag = document.child(0);
            Log.i(Constants.TAG, htmlTag.text());

            Element divTagIdCtitle = htmlTag.getElementsByAttributeValue(Constants.ID_ATTRIBUTE, Constants.CTITLE_VALUE).first();
            xkcdCartoonInformation.setCartoonTitle(divTagIdCtitle.ownText());

            Element divTagIdComic = htmlTag.getElementsByAttributeValue(Constants.ID_ATTRIBUTE, Constants.COMIC_VALUE).first();
            String cartoonInternetAddress = Constants.XKCD_INTERNET_ADDRESS + divTagIdComic.getElementsByTag(Constants.IMG_TAG).attr(Constants.SRC_ATTRIBUTE);
            xkcdCartoonInformation.setCartoonUrl(cartoonInternetAddress);

            HttpGet httpGet1 = new HttpGet(cartoonInternetAddress);
            Log.i(Constants.TAG, cartoonInternetAddress);
            HttpResponse httpResponse1 = httpClient.execute(httpGet1);
            HttpEntity httpEntity1 = httpResponse1.getEntity();
            Bitmap bitmap = BitmapFactory.decodeStream(httpEntity1.getContent());
            xkcdCartoonInformation.setCartoonBitmap(bitmap);

            Element aTagRelPrev = htmlTag.getElementsByAttributeValue(Constants.REL_ATTRIBUTE, Constants.PREVIOUS_VALUE).first();
            String previousCartoonInternetAddress = Constants.XKCD_INTERNET_ADDRESS + aTagRelPrev.attr(Constants.HREF_ATTRIBUTE);
            xkcdCartoonInformation.setPreviousCartoonUrl(previousCartoonInternetAddress);
            clickListener = new XKCDCartoonButtonClickListener(previousCartoonInternetAddress);

            Element aTagRelNext = htmlTag.getElementsByAttributeValue(Constants.REL_ATTRIBUTE, Constants.NEXT_VALUE).first();
            String nextCartoonInternetAddress = Constants.XKCD_INTERNET_ADDRESS + aTagRelNext.attr(Constants.HREF_ATTRIBUTE);
            xkcdCartoonInformation.setNextCartoonUrl(nextCartoonInternetAddress);
            listener = new XKCDCartoonButtonClickListener(nextCartoonInternetAddress);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // 2. parse the web page source code
        // - cartoon title: get the tag whose id equals "ctitle"
        // - cartoon url
        //   * get the first tag whose id equals "comic"
        //   * get the embedded <img> tag
        //   * get the value of the attribute "src"
        //   * prepend the protocol: "http:"
        // - cartoon bitmap (only if using Apache HTTP Components)
        //   * create the HttpGet object
        //   * execute the request and obtain the HttpResponse object
        //   * get the HttpEntity object from the response
        //   * get the bitmap from the HttpEntity stream (obtained by getContent()) using Bitmap.decodeStream() method
        // - previous cartoon address
        //   * get the first tag whole rel attribute equals "prev"
        //   * get the href attribute of the tag
        //   * prepend the value with the base url: http://www.xkcd.com
        //   * attach the previous button a click listener with the address attached
        // - next cartoon address
        //   * get the first tag whole rel attribute equals "next"
        //   * get the href attribute of the tag
        //   * prepend the value with the base url: http://www.xkcd.com
        //   * attach the next button a click listener with the address attached

        return  xkcdCartoonInformation;
    }

    @Override
    protected void onPostExecute(final XKCDCartoonInformation xkcdCartoonInformation) {

        // TODO exercise 5b)
        // map each member of xkcdCartoonInformation object to the corresponding widget
        // cartoonTitle -> xkcdCartoonTitleTextView
        // cartoonBitmap -> xkcdCartoonImageView (only if using Apache HTTP Components)
        // cartoonUrl -> xkcdCartoonUrlTextView
        // based on cartoonUrl fetch the bitmap using Volley (using an ImageRequest object added to the queue)
        // and put it into xkcdCartoonImageView
        // previousCartoonUrl, nextCartoonUrl -> set the XKCDCartoonUrlButtonClickListener for previousButton, nextButton
        xkcdCartoonTitleTextView.setText(xkcdCartoonInformation.getCartoonTitle());
        xkcdCartoonImageView.setImageBitmap(xkcdCartoonInformation.getCartoonBitmap());
        xkcdCartoonUrlTextView.setText(xkcdCartoonInformation.getCartoonUrl());

        ImageRequest cartoonRequest = new ImageRequest(
                xkcdCartoonInformation.getCartoonUrl(),
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        xkcdCartoonImageView.setImageBitmap(bitmap);
                    }
                },
                0,
                0,
                null,
                Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d(Constants.TAG, volleyError.toString());
                        if (Constants.DEBUG) {
                            Toast.makeText(xkcdCartoonTitleTextView.getContext(), xkcdCartoonTitleTextView.getResources().getString(R.string.an_error_has_occurred), Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                }
        );
//        VolleyController.getInstance(xkcdCartoonImageView.getContext()).addToRequestQueue(cartoonRequest);

        previousButton.setOnClickListener(clickListener);
        nextButton.setOnClickListener(listener);


    }

}
