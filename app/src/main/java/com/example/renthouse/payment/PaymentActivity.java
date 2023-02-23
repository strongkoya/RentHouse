package com.example.renthouse.payment;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.renthouse.BottomNavigationBar.BootomNavBarMain;
import com.example.renthouse.models.House;
import com.example.renthouse.models.Tenant;
import com.example.namespace.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.time.LocalDate;


public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {
    EditText tenant_name , tenant_phone,tenant_email,amount_paid,shifting_date , payment_date;

    Button pay , cancel;
    House house;
    Tenant tenant;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        tenant_name=findViewById(R.id.tenant_name);
        tenant_phone=findViewById(R.id.tenant_phone);
        tenant_email=findViewById(R.id.tenant_email);
        amount_paid=findViewById(R.id.amtPaid);
        shifting_date=findViewById(R.id.shiftingDate);
        payment_date=findViewById(R.id.paymentDate);
        payment_date.setEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            payment_date.setText(LocalDate.now().toString());
        }
        //Just for now otherwise , as it is in testing mode
        amount_paid.setText("500");
        amount_paid.setEnabled(false);
        Intent intent=getIntent();
        house= (House) intent.getSerializableExtra("house");
        Log.d("xxxxxpaymentactivity", "onCreate: of payment Activiyt "+house);
        pay=findViewById(R.id.acceptpayment);
        cancel=findViewById(R.id.cancelpayment);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(PaymentActivity.this,BootomNavBarMain.class));

            }
        });


        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Before Starting payment take all the required input and set the static data from backend only....


                startPayment();
                unregisterReceiver();
            }
        });

    }

    public void startPayment() {
          //for now ,some features has been hardcoded as api is in testmode ....
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_B8jwiZQeFZ9zZc");


        checkout.setImage(R.drawable.a);

        final Activity activity = PaymentActivity.this;



            try {
                if(validator(tenant_name.getText().toString(),tenant_phone.getText().toString(),tenant_email.getText().toString(),amount_paid.getText().toString(),shifting_date.getText().toString()))
                {
                    JSONObject options = new JSONObject();

                    options.put("name", tenant_name.getText().toString());
                    options.put("description", "Reference No. #123456");
                    options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
                    //options.put("order_id", "order_DBJOWzybf0sJbb");//
                    options.put("theme.color", "#3399cc");
                    options.put("currency", "INR");
                    options.put("amount", "50000");//Real_amt*100
                    options.put("prefill.email", tenant_email.getText().toString());
                    options.put("prefill.contact",tenant_phone.getText().toString());
                    JSONObject retryObj = new JSONObject();
                    retryObj.put("enabled", true);
                    retryObj.put("max_count", 4);
                    options.put("retry", retryObj);

                    checkout.open(activity, options);
                }


        } catch(Exception e) {
            Log.e("xxxxxRazorpay", "Error in starting Razorpay Checkout", e);
        }
    }

    private boolean validator(String name, String phone, String email, String amountPaid, String shiftingDate) {

        if(name.isEmpty()||phone.isEmpty()||email.isEmpty()||amountPaid.isEmpty()||shiftingDate.isEmpty())
        {
            Toast.makeText(this, "All feilds are mandatory", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void unregisterReceiver() {
    }

    @Override
    public void onPaymentSuccess(String s) {
       try {
           tenant=new Tenant();
           tenant.setName(tenant_name.getText().toString());
           tenant.setPhone(tenant_phone.getText().toString());
           tenant.setEmail(tenant_email.getText().toString());
           tenant.setShiftingDate(shifting_date.getText().toString());
           tenant.setPaymentDate(payment_date.getText().toString());
           tenant.setAmtPaid(amount_paid.getText().toString());

           tenant_name.setEnabled(false);
           tenant_email.setEnabled(false);
           tenant_phone.setEnabled(false);
           shifting_date.setEnabled(false);
           cancel.setVisibility(View.GONE);
           pay.setText("Paid");
           pay.setEnabled(false);
          Log.d("xxxxtenantdata", "onPaymentSuccess: tenant data "+tenant.toString());
            updateTenantDataInHouse(house,tenant); //for owner notification
             updateHouseDataInCustomer(house);   //for Tenant notification


           Toast.makeText(this, "On Payment Successful", Toast.LENGTH_LONG).show();
           //startActivity(new Intent(PaymentActivity.this, BootomNavBarMain.class));
       }catch (Exception e)
       {
           Log.d("xxxxexception", "onPaymentSuccess: exception in on payment success "+e.getLocalizedMessage());
       }


    }

    private void updateHouseDataInCustomer(House house) {
        String currentUserEmail= FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FirebaseFirestore firebaseFirestore= FirebaseFirestore.getInstance();
        DocumentReference documentReference=firebaseFirestore.collection("USERDATA").document(currentUserEmail);
        documentReference.update("house",house);
    }

    private void updateTenantDataInHouse(House house ,Tenant tenant) {

        //1. set data in tenant model
        //2.then set tenant in house model
        //3. then update house in firestore database.

        Log.d("xxxxHouseId", "updateTenantDataInHouse: houseid "+house.getDocId());
        FirebaseFirestore firebaseFirestore= FirebaseFirestore.getInstance();
        DocumentReference documentReference=firebaseFirestore.collection("HouseCollection").document(house.getDocId());
        documentReference.update("tenant",tenant);
        documentReference.update("availability",false);


    }

    @Override
    public void onPaymentError(int i, String s) {

    }
}