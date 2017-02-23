package com.tomer.alwayson.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.tomer.alwayson.BuildConfig;
import com.tomer.alwayson.ContextConstatns;
import com.tomer.alwayson.Globals;
import com.tomer.alwayson.R;
import com.tomer.alwayson.SecretConstants;
import com.tomer.alwayson.helpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DonateActivity extends AppCompatActivity implements ContextConstatns {
    private static ServiceConnection mServiceConn;
    private static IInAppBillingService mService;

    public static void quicklyPromptToSupport(final Activity context, final View rootView) {
        if (mService != null) {
            String googleIAPCode = SecretConstants.getPropertyValue(context, "googleIAPCode");
            String IAP = SecretConstants.getPropertyValue(context, "IAPID1");
            try {
                Bundle buyIntentBundle = mService.getBuyIntent(3, context.getPackageName(),
                        IAP, "inapp", googleIAPCode);
                PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                if (pendingIntent == null)
                    Snackbar.make(rootView, context.getString(R.string.error_IAP), Snackbar.LENGTH_LONG).show();
                else
                    context.startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);
            } catch (RemoteException | IntentSender.SendIntentException e) {
                Snackbar.make(rootView, context.getString(R.string.error_0_unknown_error) + e.getMessage(), Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    public static void resetPaymentService(Context context) {
        if (BuildConfig.DEBUG && !Utils.isGooglePlayInstalled(context) && Globals.ownedItems == null)
            Globals.ownedItems = new ArrayList<String>() {{
                add(SecretConstants.getPropertyValue(context, "NO_PLAY_STORE_IAP"));
            }};
        else {
            mServiceConn = new ServiceConnection() {
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mService = null;
                }

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mService = IInAppBillingService.Stub.asInterface(service);
                    try {
                        int response = mService.isBillingSupported(3, context.getPackageName(), "inapp");
                        if (response == RESULT_BILLING_UNAVAILABLE)
                            return;
                        Globals.ownedItems = mService.getPurchases(3, context.getPackageName(), "inapp", null).getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                        Utils.logDebug("BOUGHT_ITEMS", String.valueOf(Globals.ownedItems));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            };
            Intent billingServiceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
            billingServiceIntent.setPackage("com.android.vending");
            try {
                context.unbindService(mServiceConn);
            } catch (Exception ignored) {
            }
            context.bindService(billingServiceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        }
    }

    public static void onDestroy(Context context) {
        try {
            context.unbindService(mServiceConn);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donation_activity);
        ArrayList<Item> items = getItemsList(this);
        if (items == null || items.isEmpty()) {
            Snackbar.make(findViewById(R.id.donation_list), getString(R.string.error_IAP), Snackbar.LENGTH_LONG).show();
            finish();
        } else {
            ((ListViewCompat) findViewById(R.id.donation_list)).setAdapter(new DonationAdapter(this, items));
        }
    }

    public ArrayList<Item> getItemsList(Context context) {
        ArrayList<Item> items = new ArrayList<>();
        ArrayList<String> IAPs = new ArrayList<>();
        for (int i = 1; i < 7; i++) {
            items.add(null);
            IAPs.add(SecretConstants.getPropertyValue(context, "IAPID" + i));
        }
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST", IAPs);

        try {
            Bundle skuDetails = mService.getSkuDetails(3, context.getPackageName(), "inapp", querySkus);
            int response = skuDetails.getInt("RESPONSE_CODE");
            if (response == 0) {
                ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                assert responseList != null;
                for (String thisResponse : responseList) {
                    try {
                        JSONObject object = new JSONObject(thisResponse);
                        items.set(IAPs.indexOf(object.getString("productId")), new Item(object.getString("price"), object.getString("productId")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return items;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            Utils.logDebug("Purchase state", String.valueOf(resultCode));
            if (resultCode == RESULT_OK) {
                if (Globals.ownedItems != null)
                    Globals.ownedItems.add(SecretConstants.getPropertyValue(this, "TEMP_IAP"));
                else
                    Globals.ownedItems = new ArrayList<>();
                Toast.makeText(getApplicationContext(), R.string.thanks, Toast.LENGTH_LONG).show();
                Snackbar.make(findViewById(android.R.id.content), R.string.thanks, 10000).setAction(R.string.action_restart, view -> {
                    finish();
                    startActivity(new Intent(getApplicationContext(), PreferencesActivity.class));
                }).show();
                resetPaymentService(this);
                Utils.logDebug("User bought item", data.getStringExtra("INAPP_PURCHASE_DATA"));
            }
        }
    }

    class Item {
        private String price;
        private String id;

        Item(String price, String id) {
            this.price = price;
            this.id = id;
        }

        String getPrice() {
            return price;
        }

        public String getId() {
            return id;
        }
    }

    class DonationAdapter extends ArrayAdapter<Item> {

        private List<Item> items;

        DonationAdapter(Context context, List<Item> items) {
            super(context, 0);
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View v = convertView;
            if (v == null)
                v = LayoutInflater.from(getContext()).inflate(R.layout.donation_item, null);

            String title = null;
            String description = null;
            int image = 0;
            int item = position + 1;
            switch (item) {
                case 1:
                    title = getString(R.string.support_1);
                    description = getString(R.string.support_1_desc);
                    image = R.drawable.ic_donation_unlock;
                    break;
                case 2:
                    title = getString(R.string.support_2);
                    description = getString(R.string.support_2_desc);
                    image = R.drawable.ic_donation_large;
                    break;
                case 3:
                    title = getString(R.string.support_5);
                    description = getString(R.string.support_5_desc);
                    image = R.drawable.ic_donation_gift;
                    break;
                case 4:
                    title = getString(R.string.support_10);
                    description = getString(R.string.support_10_desc);
                    image = R.drawable.ic_donation_thumb_up;
                    break;
                case 5:
                    title = getString(R.string.support_20);
                    description = getString(R.string.support_20_desc);
                    image = R.drawable.ic_donation_heart;
                    break;
                case 6:
                    title = getString(R.string.support_25);
                    description = getString(R.string.support_25_desc);
                    image = R.drawable.ic_donation_hands_up;
                    break;
            }

            ((TextView) v.findViewById(R.id.title_tv)).setText(title + " - " + items.get(position).getPrice());
            ((TextView) v.findViewById(R.id.description_tv)).setText(description);
            ((ImageView) v.findViewById(R.id.icon_iv)).setImageResource(image);
            ((ImageView) v.findViewById(R.id.icon_iv)).setColorFilter(ContextCompat.getColor(DonateActivity.this, R.color.text_color));

            if (Globals.ownedItems.contains(items.get(position).getId())) {
                v.findViewById(R.id.donation_item_wrapper).setEnabled(false);
                v.findViewById(R.id.title_tv).setEnabled(false);
                v.findViewById(R.id.description_tv).setEnabled(false);
                ((ImageView) v.findViewById(R.id.icon_iv)).setColorFilter(ContextCompat.getColor(DonateActivity.this, R.color.disabled_text));
            }

            v.setOnClickListener(v1 -> {
                try {
                    String googleIAPCode = SecretConstants.getPropertyValue(DonateActivity.this, "googleIAPCode");
                    String IAP = items.get(position).getId();
                    Bundle buyIntentBundle = mService.getBuyIntent(3, DonateActivity.this.getPackageName(),
                            IAP, "inapp", googleIAPCode);
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                    if (pendingIntent == null)
                        Snackbar.make(findViewById(android.R.id.content), DonateActivity.this.getString(R.string.error_IAP), Snackbar.LENGTH_LONG).show();
                    else
                        DonateActivity.this.startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), 0, 0, 0);
                } catch (RemoteException | IntentSender.SendIntentException e) {
                    Snackbar.make(findViewById(android.R.id.content), DonateActivity.this.getString(R.string.error_0_unknown_error) + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            });
            return v;
        }

    }
}
