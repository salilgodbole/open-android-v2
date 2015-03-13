package com.citruspay.sdkui;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.citrus.asynch.InitSDK;
import com.citrus.card.Card;
import com.citrus.interfaces.InitListener;
import com.citrus.mobile.Callback;
import com.citrus.mobile.Config;
import com.citrus.netbank.Bank;
import com.citrus.payment.Bill;
import com.citrus.payment.PG;
import com.citrus.payment.UserDetails;
import com.citrus.sdkui.CardOption;
import com.citrus.sdkui.CitrusCash;
import com.citrus.sdkui.NetbankingOption;
import com.citrus.sdkui.PaymentOption;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.citruspay.sdkui.CardPaymentFragment.OnCardPaymentListener;
import static com.citruspay.sdkui.PaymentProcessingFragment.OnTransactionCompleteListener;
import static com.citruspay.sdkui.PaymentStatusFragment.OnTransactionResponseListener;


public class MainActivity extends ActionBarActivity implements OnPaymentOptionSelectedListener, OnTransactionResponseListener, OnTransactionCompleteListener, OnCardPaymentListener, InitListener {

    private String mUserEmail = null;
    private String mUserMobile = null;
    private String mMerchantVanity = null;
    private String mMerchantName = null;
    private String mMerchantBillUrl = null;
    private double mTransactionAmount = 0.0;
    private ProgressDialog mProgressDialog = null;
    private FragmentManager mFragmentManager = null;
    private CitrusPaymentParams mPaymentParams = null;
    private String mColorPrimary = null;
    private String mColorPrimaryDark = null;
    private ActionBar mActionBar = null;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mActionBar = getSupportActionBar();
        mPaymentParams = getIntent().getParcelableExtra(Constants.INTENT_EXTRA_PAYMENT_PARAMS);

        // Get required details from intent.
        if (mPaymentParams != null) {
            mTransactionAmount = mPaymentParams.transactionAmount;

            CitrusUser user = mPaymentParams.user;
            if (user != null) {
                mUserEmail = user.getEmailId();
                mUserMobile = user.getMobileNo();
            }

            mMerchantVanity = mPaymentParams.vanity;
            mMerchantBillUrl = mPaymentParams.billUrl;
            mMerchantName = mPaymentParams.merchantName;

            mColorPrimary = mPaymentParams.colorPrimary;
            mColorPrimaryDark = mPaymentParams.colorPrimaryDark;

            // Set primary color
            if (mColorPrimary != null && mActionBar != null) {
                mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(mColorPrimary)));
            }

            // Set action bar color. Available only on android version Lollipop or higher.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mColorPrimaryDark != null) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor(mColorPrimaryDark));
            }

            if (mMerchantName != null) {
                setTitle(mMerchantName + "\t \t " + mTransactionAmount);
            }
        }

        // TODO Do not use static fields.
        Config.setVanity(mMerchantVanity);

        mProgressDialog = new ProgressDialog(this);
        mFragmentManager = getSupportFragmentManager();

        new InitSDK(this, this, mUserEmail, mUserMobile);

        showDialog("Initializing....", true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        dismissDialog();
        mProgressDialog = null;
        mFragmentManager = null;
        mUserEmail = null;
        mUserMobile = null;
        mColorPrimaryDark = null;
        mColorPrimary = null;
        mMerchantVanity = null;
        mMerchantBillUrl = null;
        mPaymentParams = null;
    }

    private void processResponse(String response, String error) {

        if (!TextUtils.isEmpty(response)) {
            try {

                JSONObject redirect = new JSONObject(response);
                Intent i = new Intent(MainActivity.this, CitrusPaymentActivity.class);

                if (!TextUtils.isEmpty(redirect.getString("redirectUrl"))) {
                    showPaymentFragment(redirect.getString("redirectUrl"));
                }

                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
        }
    }

    private List<PaymentOption> getCitrusWalletForUser() {
        List<PaymentOption> citrusWallet = Config.getCitrusWallet();


        return citrusWallet;
    }

    private JSONObject getCustomer() {

        JSONObject customer = null;

		/*
         * All the below mentioned parameters are mandatory - missing anyone of them may create errors Do not change the
		 * key in the json below - only change the values
		 */

        try {
            customer = new JSONObject();
            customer.put("firstName", "Tester");
            customer.put("lastName", "Citrus");
            customer.put("email", "tester@gmail.com");
            customer.put("mobileNo", "9170164284");
            customer.put("street1", "streetone");
            customer.put("street2", "streettwo");
            customer.put("city", "Mumbai");
            customer.put("state", "Maharashtra");
            customer.put("country", "India");
            customer.put("zip", "400052");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return customer;
    }

    private void showAddCardFragment() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(
                R.id.container, CardPaymentFragment.newInstance());
        ft.addToBackStack(null);
        ft.commit();
    }

    private void showNetbankingFragment() {
        // TODO: Show netbanking fragment. Need to display other banks
    }

    private void showPaymentFragment(String redirectUrl) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(
                R.id.container, PaymentProcessingFragment.newInstance(redirectUrl));
        ft.addToBackStack(null);
        ft.commit();
    }

    private void showPaymentStatusFragment(CitrusTransactionResponse transactionResponse, CitrusPaymentParams paymentParams) {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(
                R.id.container, PaymentStatusFragment.newInstance(transactionResponse, paymentParams));
        ft.addToBackStack(null);
        ft.commit();
    }


    private void showDialog(String message, boolean cancelable) {
        if (mProgressDialog != null) {
            mProgressDialog.setCancelable(cancelable);
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
        }
    }

    private void dismissDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    // Listeners

    @Override
    public void onOptionSelected(final PaymentOption paymentOption) {

        Log.d("Citrus", paymentOption.toString());

        if (paymentOption instanceof CardOption) {
            final CardOption cardOption = (CardOption) paymentOption;

            // If the Add Card button is clicked show the fragment to Add New Card.
            if (cardOption == CardOption.DEFAULT_CARD) {
                showAddCardFragment();

                return;
            }

            new GetBill(mMerchantBillUrl, mTransactionAmount, new Callback() {
                @Override
                public void onTaskexecuted(String billString, String error) {
                    if (TextUtils.isEmpty(error)) {
                        Bill bill = new Bill(billString);
                        Card card = null;

                        if (!TextUtils.isEmpty(cardOption.getToken())) {
                            // TODO Take the CVV instead of hardcoded value.
                            card = new Card(cardOption.getToken(), "123");
                        } else {
                            card = new Card(cardOption.getCardNumber(), cardOption.getCardExpiryMonth(), cardOption.getCardExpiryYear(), cardOption.getCardCVV(), cardOption.getCardHolderName(), cardOption.getCardType());
                        }

                        // TODO: Use customer data from User to fill the data in the getCustomer.
                        UserDetails userDetails = new UserDetails(getCustomer());

                        PG paymentGateway = new PG(card, bill, userDetails);

                        paymentGateway.charge(new Callback() {
                            @Override
                            public void onTaskexecuted(String success, String error) {
                                processResponse(success, error);
                            }
                        });
                    }
                }
            }).execute();
        } else if (paymentOption instanceof NetbankingOption) {

            final NetbankingOption netbankingOption = (NetbankingOption) paymentOption;

            if (netbankingOption == NetbankingOption.DEFAULT_BANK) {

                showNetbankingFragment();

                return;
            }

            new GetBill(mMerchantBillUrl, mTransactionAmount, new Callback() {
                @Override
                public void onTaskexecuted(String billString, String error) {
                    Bill bill = new Bill(billString);

                    Bank netbank = new Bank(netbankingOption.getBankCID());

                    // TODO Make token payment for bank

                    // TODO: Use customer data from User to fill the data in the getCustomer.
                    UserDetails userDetails = new UserDetails(getCustomer());

                    PG paymentgateway = new PG(netbank, bill, userDetails);

                    paymentgateway.charge(new Callback() {
                        @Override
                        public void onTaskexecuted(String success, String error) {
                            processResponse(success, error);
                        }
                    });
                }
            }).execute();
        } else if (paymentOption instanceof CitrusCash) {
            Toast.makeText(this, "Citrus Cash", Toast.LENGTH_SHORT).show();
        } else {
            mFragmentManager.beginTransaction()
                    .replace(R.id.container, CardPaymentFragment.newInstance())
                    .commit();
        }
    }


    @Override
    public void onSuccess(String response) {

        mPaymentParams.netbankingOptionList = Config.getBankList();
        mPaymentParams.userSavedOptionList = Config.getCitrusWallet();

        mFragmentManager.beginTransaction()
                .add(R.id.container, PaymentOptionsFragment.newInstance(mPaymentParams))
                .commit();

        dismissDialog();
    }

    @Override
    public void onBindFailed(String response) {
        Log.i("citrus", "onBindFailed");
    }

    @Override
    public void onWalletLoadFailed(String response) {
        Log.i("citrus", "onWalletLoadFailed");
    }

    @Override
    public void onNetBankingListFailed(VolleyError error) {
        Log.i("citrus", "onNetBankingListFailed");
    }

    @Override
    public void onError(Exception e) {
        Log.i("citrus", "onError");
    }

    @Override
    public void onTransactionComplete(CitrusTransactionResponse transactionResponse) {
        showPaymentStatusFragment(transactionResponse, mPaymentParams);
    }

    // TODO: Set the title of the activity depending upon the transcation status.

    @Override
    public void onRetryTransaction() {
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_out, android.R.anim.fade_in);
        ft.commit();
        mFragmentManager.popBackStack();
    }

    @Override
    public void onDismiss() {
        Toast.makeText(this, "Dismiss....", Toast.LENGTH_SHORT).show();

        // TODO: Set the result and return the transaction response.
        finish();
    }

    @Override
    public void onCardPaymentSelected(final CardOption cardOption) {
        mFragmentManager.popBackStack();

        new GetBill(mMerchantBillUrl, mTransactionAmount, new Callback() {
            @Override
            public void onTaskexecuted(String billString, String error) {
                Card card = null;
                Bill bill = null;
                if (TextUtils.isEmpty(error)) {
                    bill = new Bill(billString);
                    card = new Card(cardOption.getCardNumber(), cardOption.getCardExpiryMonth(), cardOption.getCardExpiryYear(), cardOption.getCardCVV(), cardOption.getCardHolderName(), cardOption.getCardType());
                }

                // TODO: Use customer data from User to fill the data in the getCustomer.
                UserDetails userDetails = new UserDetails(getCustomer());

                PG paymentGateway = new PG(card, bill, userDetails);

                paymentGateway.charge(new Callback() {
                    @Override
                    public void onTaskexecuted(String success, String error) {
                        processResponse(success, error);
                    }
                });
            }
        }).execute();

    }
}
