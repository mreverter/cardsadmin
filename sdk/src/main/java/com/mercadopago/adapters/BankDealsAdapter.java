package com.mercadopago.adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mercadopago.R;
import com.mercadopago.callbacks.OnSelectedCallback;
import com.mercadopago.customviews.MPTextView;
import com.mercadopago.model.BankDeal;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BankDealsAdapter extends RecyclerView.Adapter<BankDealsAdapter.ViewHolder> {

    private Activity mActivity;
    private List<BankDeal> mData;
    private OnSelectedCallback<View> mCallback;
    private View.OnClickListener mListener = null;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public MPTextView mBankDescView;
        public ImageView mBankImageView;
        public MPTextView mInstallmentsView;

        public ViewHolder(View v, View.OnClickListener listener) {

            super(v);
            mBankDescView = (MPTextView) v.findViewById(R.id.mpsdkBankDesc);
            mBankImageView = (ImageView) v.findViewById(R.id.mpsdkBankImg);
            mInstallmentsView = (MPTextView) v.findViewById(R.id.mpsdkInstallments);
            if (listener != null) {
                v.setOnClickListener(listener);
            }
            v.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event != null && event.getAction() == KeyEvent.ACTION_DOWN
                            && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
                        mCallback.onSelected(v);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    public BankDealsAdapter(Activity activity, List<BankDeal> data, OnSelectedCallback<View> callback, View.OnClickListener listener) {
        mActivity = activity;
        mData = data;
        mCallback = callback;
        mListener = listener;
    }

    @Override
    public BankDealsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mpsdk_row_bank_deals, parent, false);

        return new ViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        BankDeal bankDeal = mData.get(position);

        // Set bank description
        holder.mBankDescView.setText(getBankDesc(bankDeal));
        if (!holder.mBankDescView.getText().equals("")) {
            holder.mBankDescView.setVisibility(View.VISIBLE);
        } else {
            holder.mBankDescView.setVisibility(View.GONE);
        }

        // Set bank image
        Picasso.with(mActivity)
                .load(getPicture(bankDeal))
                .into(holder.mBankImageView);

        // Set installments
        holder.mInstallmentsView.setText(Html.fromHtml(getRecommendedMessage(bankDeal)));

        // Set view tag item
        holder.itemView.setTag(bankDeal);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public BankDeal getItem(int position) {
        return mData.get(position);
    }

    private String getBankDesc(BankDeal bankDeal) {

        if (bankDeal.getPaymentMethods() != null) {
            String desc = "";
            for (int i = 0; i < bankDeal.getPaymentMethods().size(); i++) {
                desc += bankDeal.getPaymentMethods().get(i).getName();
                desc += " ";
                if (bankDeal.getPaymentMethods().size() > i + 2) {
                    desc += mActivity.getString(R.string.mpsdk_comma_separator) + " ";
                } else if (bankDeal.getPaymentMethods().size() > i + 1) {
                    desc += mActivity.getString(R.string.mpsdk_and) + " ";
                }
            }
            return desc;
        }
        return "";
    }

    private String getPicture(BankDeal bankDeal) {

        return ((bankDeal != null) && (bankDeal.getPicture() != null)) ? bankDeal.getPicture().getUrl() : null;
    }

    private String getRecommendedMessage(BankDeal bankDeal) {

        return (bankDeal != null) ? (bankDeal.getRecommendedMessage() != null) ? bankDeal.getRecommendedMessage() : "" : "";
    }
}
