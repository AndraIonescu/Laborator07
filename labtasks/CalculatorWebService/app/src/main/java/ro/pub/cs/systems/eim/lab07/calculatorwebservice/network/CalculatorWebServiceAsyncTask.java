package ro.pub.cs.systems.eim.lab07.calculatorwebservice.network;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import ro.pub.cs.systems.eim.lab07.calculatorwebservice.general.Constants;
import ro.pub.cs.systems.eim.lab07.calculatorwebservice.general.Utilities;

public class CalculatorWebServiceAsyncTask extends AsyncTask<String, Void, String> {

    private TextView resultTextView;

    public CalculatorWebServiceAsyncTask(TextView resultTextView) {
        this.resultTextView = resultTextView;
    }

    @Override
    protected String doInBackground(String... params) {
        String operator1 = params[0];
        String operator2 = params[1];
        String operation = params[2];
        int method = Integer.parseInt(params[3]);

        // TODO exercise 4
        // signal missing values through error messages
        if (operator1 == null || operator2 == null) {
            Log.e(Constants.TAG, "Missing operator(s)");
        }

        // create an instance of a HttpClient object

        try {
            HttpClient httpClient = new DefaultHttpClient();

            if (method == 0) {
                HttpGet httpGet = new HttpGet(Constants.GET_WEB_SERVICE_ADDRESS
                        + "?" + Constants.OPERATOR1_ATTRIBUTE + "=" + operator1
                        + "&" + Constants.OPERATOR2_ATTRIBUTE + "=" + operator2
                        + "&" + Constants.OPERATION_ATTRIBUTE + "=" + operation);
                HttpResponse httpGetResponse = httpClient.execute(httpGet);
                HttpEntity httpGetEntity = httpGetResponse.getEntity();
                if (httpGetEntity == null) {
                    // do something with the response
                    Log.i(Constants.TAG, "Null entity");
                }

                return Utilities.getReader(httpGetEntity).readLine();
            } else if (method == 1) {
                HttpPost httpPost = new HttpPost(Constants.POST_WEB_SERVICE_ADDRESS);
                List<NameValuePair> param = new ArrayList<>();
                param.add(new BasicNameValuePair(Constants.OPERATION_ATTRIBUTE, operation));
                param.add(new BasicNameValuePair(Constants.OPERATOR1_ATTRIBUTE, operator1));
                param.add(new BasicNameValuePair(Constants.OPERATOR2_ATTRIBUTE, operator2));
                try {
                    UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(param, HTTP.UTF_8);
                    httpPost.setEntity(urlEncodedFormEntity);
                } catch (UnsupportedEncodingException unsupportedEncodingException) {
                    Log.e(Constants.TAG, unsupportedEncodingException.getMessage());
                    if (Constants.DEBUG) {
                        unsupportedEncodingException.printStackTrace();
                    }
                }

                HttpResponse httpPostResponse = httpClient.execute(httpPost);
                HttpEntity httpPostEntity = httpPostResponse.getEntity();
                if (httpPostEntity == null) {
                    // do something with the response
                    Log.i(Constants.TAG, "Null entity");
                }

                return Utilities.getReader(httpPostEntity).readLine();
            }
        } catch (Exception exception) {
            Log.e(Constants.TAG, exception.getMessage());
            if (Constants.DEBUG) {
                exception.printStackTrace();
            }
        }
        // get method used for sending request from methodsSpinner

        // 1. GET
        // a) build the URL into a HttpGet object (append the operators / operations to the Internet address)
        // b) create an instance of a ResultHandler object
        // c) execute the request, thus generating the result

        // 2. POST
        // a) build the URL into a HttpPost object
        // b) create a list of NameValuePair objects containing the attributes and their values (operators, operation)
        // c) create an instance of a UrlEncodedFormEntity object using the list and UTF-8 encoding and attach it to the post request
        // d) create an instance of a ResultHandler object
        // e) execute the request, thus generating the result

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        // display the result in resultTextView
        Log.i(Constants.TAG, result);
        resultTextView.setText(result);
    }

}
