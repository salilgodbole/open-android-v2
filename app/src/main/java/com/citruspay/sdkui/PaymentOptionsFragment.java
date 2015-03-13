package com.citruspay.sdkui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.citrus.sdkui.CitrusCash;
import com.citrus.sdkui.NetbankingOption;
import com.citrus.sdkui.PaymentOption;

import java.util.ArrayList;
import java.util.List;

import static com.citruspay.sdkui.PaymentOptionsCardView.PaymentOptionsType;


/**
 * This fragment will handle displaying of all the available payment options.
 * <p/>
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.citruspay.sdkui.OnPaymentOptionSelectedListener} interface
 * to handle interaction events.
 * Use the {@link PaymentOptionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PaymentOptionsFragment extends Fragment implements OnPaymentOptionSelectedListener {

    private PaymentOptionsCardView mCardViewCitrusCash = null;
    private PaymentOptionsCardView mCardViewSavedCards = null;
    private PaymentOptionsCardView mCardViewDebitCreditCards = null;
    private PaymentOptionsCardView mCardViewNetbanking = null;
    private CitrusPaymentParams mPaymentParams = null;
    private OnPaymentOptionSelectedListener mListener;

    public PaymentOptionsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PaymentOptionsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PaymentOptionsFragment newInstance(CitrusPaymentParams paymentParams) {
        PaymentOptionsFragment fragment = new PaymentOptionsFragment();
        Bundle args = new Bundle();
        args.putParcelable(Constants.INTENT_EXTRA_PAYMENT_PARAMS, paymentParams);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPaymentParams = getArguments().getParcelable(Constants.INTENT_EXTRA_PAYMENT_PARAMS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_payment_options, container, false);
        mCardViewCitrusCash = (PaymentOptionsCardView) view.findViewById(R.id.cardview_citrus_cash);
        mCardViewSavedCards = (PaymentOptionsCardView) view.findViewById(R.id.cardview_saved_cards);
        mCardViewDebitCreditCards = (PaymentOptionsCardView) view.findViewById(R.id.cardview_debit_credit_cards);
        mCardViewNetbanking = (PaymentOptionsCardView) view.findViewById(R.id.cardview_netbanking);

        // Citrus Cash CardView
        PaymentOptionsType citrusCashCardsType = PaymentOptionsType.CITRUS_CASH;
        List<CitrusCash> citrusCashList = new ArrayList<>();
        citrusCashList.add(new CitrusCash("1400.00"));
        citrusCashCardsType.setHeaderText("Citrus Cash");
        citrusCashCardsType.setFooterText("Pay Now");
        // TODO: Need to add button for add and pay now.
        citrusCashCardsType.setPaymentOptionList(citrusCashList);
        mCardViewCitrusCash.init(this, citrusCashCardsType, mPaymentParams);

        // Saved Cards
        List<PaymentOption> userSavedOptionList = mPaymentParams.userSavedOptionList;
        if (userSavedOptionList != null && !userSavedOptionList.isEmpty()) {
            PaymentOptionsType savedCardsType = PaymentOptionsType.SAVED_CARDS;
            savedCardsType.setHeaderText("Saved Cards");
            savedCardsType.setFooterText("Add card");
            savedCardsType.setPaymentOptionList(userSavedOptionList);
            mCardViewSavedCards.init(this, savedCardsType, mPaymentParams);
        } else {
            //Hide the saved cards, as user has not saved any cards.
            mCardViewSavedCards.setVisibility(View.GONE);
        }

        // Debit or Credit Card
        PaymentOptionsType debitCreditCardsType = PaymentOptionsType.DEBIT_CREDIT_CARDS;
        debitCreditCardsType.setHeaderText("Credit/Debit Cards");
        debitCreditCardsType.setFooterText("Pay with credit or debit card");
        debitCreditCardsType.setPaymentOptionList(null); //Debit credit card does not have list.
        mCardViewDebitCreditCards.init(this, debitCreditCardsType, mPaymentParams);

        // Netbanking
        List<NetbankingOption> netbankingOptionList = mPaymentParams.netbankingOptionList;
        if (netbankingOptionList != null && !netbankingOptionList.isEmpty()) {
            PaymentOptionsType netbankingCardType = PaymentOptionsType.NETBANKING;
            netbankingCardType.setHeaderText("Netbanking");
            netbankingCardType.setFooterText("Select Other Bank");
            netbankingCardType.setPaymentOptionList(netbankingOptionList);
            mCardViewNetbanking.init(this, netbankingCardType, mPaymentParams);
        } else {
            //Hide the netbanking view as netbankings are not enabled for the merchant.
            mCardViewNetbanking.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnPaymentOptionSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mCardViewCitrusCash = null;
        mCardViewSavedCards = null;
        mCardViewDebitCreditCards = null;
        mCardViewNetbanking = null;
        mListener = null;
        mPaymentParams = null;
    }

    @Override
    public void onOptionSelected(PaymentOption paymentOption) {
        mListener.onOptionSelected(paymentOption);
    }
}