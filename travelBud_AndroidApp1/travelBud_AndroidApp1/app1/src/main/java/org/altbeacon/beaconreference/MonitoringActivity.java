package org.altbeacon.beaconreference;

import android.os.Bundle;
import android.os.RemoteException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import java.util.Locale;


public class MonitoringActivity extends Activity  {
	protected static final String TAG = "MonitoringActivity";
    private BeaconManager beaconManager;
    TextToSpeech t1;
    ImageButton button1;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        verifyBluetooth();
        button1 =(ImageButton)findViewById(R.id.Button01);


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //String toSpeak = button1.getText().toString();
                String toSpeak ="Start Route Search";
                speakWords(toSpeak);
                //t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                Intent myIntent = new Intent(MonitoringActivity.this,RangingActivity.class);
                startActivity(myIntent);
                // this.startActivity(myIntent);
            }
        });

       // logToDisplay("Application just launched" );
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
      //  t1.speak("travelBud opened", TextToSpeech.QUEUE_FLUSH, null);

}
    //speak the user text
    private void speakWords(String speech) {
        //speak straight away
        t1.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }


//	public void onRangingClicked(View view) {

	//}

    @Override
    public void onResume() {
        super.onResume();
        ((BeaconReferenceApplication) this.getApplicationContext()).setMonitoringActivity(this);
    }

    @Override
    public void onPause() {
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }

        super.onPause();
        ((BeaconReferenceApplication) this.getApplicationContext()).setMonitoringActivity(null);
    }

	private void verifyBluetooth() {

		try {
			if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Bluetooth not enabled");			
				builder.setMessage("Please enable bluetooth in settings and restart this application.");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						finish();
			            System.exit(0);					
					}					
				});
				builder.show();
			}			
		}
		catch (RuntimeException e) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Bluetooth LE not available");			
			builder.setMessage("Sorry, this device does not support Bluetooth LE.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
		            System.exit(0);					
				}
				
			});
			builder.show();
			
		}
		
	}	

    public void logToDisplay(final String line) {
    	runOnUiThread(new Runnable() {
    	    public void run() {
    	    	EditText editText = (EditText)MonitoringActivity.this
    					.findViewById(R.id.monitoringText);
       	    	editText.append(line+"\n");
    	    }
    	});
    }

}
