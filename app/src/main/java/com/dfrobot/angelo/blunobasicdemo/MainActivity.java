package com.dfrobot.angelo.blunobasicdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import static com.dfrobot.angelo.blunobasicdemo.R.raw.alarm_beeps;

public class MainActivity  extends BlunoLibrary {
	private Button buttonScan;
    private TextView btstatus;
	private TextView mtstatus;
	private TextView hrstatus;
	private TextView heartraten;
	private ImageView gambarwarna;
	public int motionstatus = 0;
	public int ppgstatus = 0;
	int drowsycount = 0;
    private String selection;
    private int modeselected;
    MediaPlayer beepbeep;
    public AlertDialog alert;
    public AlertDialog alertb;
    private ImageView sensorstatusv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        onCreateProcess();														//onCreate Process by BlunoLibrary


        serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200

        btstatus = (TextView) findViewById(R.id.btStatus);
        mtstatus = (TextView) findViewById(R.id.motionstatus);
        hrstatus = (TextView) findViewById(R.id.hrstatus);
		heartraten = (TextView) findViewById(R.id.heartrateN);
        buttonScan = (Button) findViewById(R.id.buttonScan);
        RadioGroup pilihmode = (RadioGroup) findViewById(R.id.mode_select);
        gambarwarna = (ImageView) findViewById(R.id.imageView2);
        beepbeep = MediaPlayer.create(MainActivity.this, R.raw.alarm_beeps);
        sensorstatusv = (ImageView) findViewById(R.id.sensorstatusv);

        buttonScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				buttonScanOnClickProcess();										//Alert Dialog for selecting the BLE device
			}
		});

        pilihmode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case 2131165236:
                        serialSend("932");
                        break;
                    case 2131165238:
                        serialSend("931");
                        break;
                    default:
                        serialSend("930");
                        break;
                }
            }
        });
	}


    protected void onResume(){
		super.onResume();
		System.out.println("BlUNOActivity onResume");
		onResumeProcess();														//onResume Process by BlunoLibrary
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();														//onPause Process by BlunoLibrary
    }
	
	protected void onStop() {
		super.onStop();
		onStopProcess();														//onStop Process by BlunoLibrary
	}
    
	@Override
    protected void onDestroy() {
        super.onDestroy();	
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }

	@Override
	public void onConectionStateChange(connectionStateEnum theConnectionState) {//Once connection state changes, this function will be called
		switch (theConnectionState) {											//Four connection state
		case isConnected:
			buttonScan.setText("Disconnect");
			btstatus.setText(R.string.kalautersambung);
			break;
		case isConnecting:
			buttonScan.setText("Connecting");
			break;
		case isToScan:
			buttonScan.setText("Scan");
            btstatus.setText(R.string.kalaugatersambung);
			break;
		case isScanning:
			buttonScan.setText("Scanning");
			break;
		case isDisconnecting:
			buttonScan.setText("isDisconnecting");
			break;
		default:
			break;
		}
	}

	@Override
	public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
        char[] dataarray;
        double heartrate = 0;

        if(theString.length() > 15) {
            dataarray = theString.toCharArray();
            for (int i = dataarray.length - 1; i > 7; i--) {
                if (dataarray[i] == '1') {
                    heartrate = heartrate + Math.pow(2, 15 - i);
                }
            }

            heartraten.setText(String.valueOf((int) heartrate));

            //motion sensor status
            if (dataarray[3] == '1') {
                motionstatus = 1;
                mtstatus.setText(R.string.motionon);
            } else {
                motionstatus = 0;
                mtstatus.setText(R.string.motionoff);
            }

            //ppg sensor status
            if (dataarray[4] == '1') {
                if (dataarray[5] == '0') {
                    ppgstatus = 1;
                    hrstatus.setText(R.string.hrwarning);
                } else {
                    ppgstatus = 2;
                    hrstatus.setText(R.string.hron);
                    if(motionstatus == 1){
                        sensorstatusv.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                ppgstatus = 0;
                hrstatus.setText(R.string.hroff);
            }

            //ngantuk status
            if (dataarray[6] == '1') {
                if (dataarray[7] == '0') {
                    if(drowsycount == 0)  drowsydetected();
                }
                else if (dataarray[7] == '1'){
                    if(drowsycount == 1)  heavydrowsy();
                }
            }
        }
	}

    public void heavydrowsy() {
        gambarwarna.setImageResource(R.drawable.bggrayred);
        beepbeep.start();
        if(alertb != null && alertb.isShowing() ) return;
        AlertDialog.Builder b_builder = new AlertDialog.Builder(MainActivity.this);
        b_builder.setTitle("Heavy Drowsiness Detected")
                .setMessage(R.string.ngantukberat)
                .setCancelable(false)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        drowsycount = 2;
                        beepbeep.stop();
                    }
                });
        alertb = b_builder.create();
        if (!alertb.isShowing()) {
            alertb.show();
        }
    }

    public void drowsydetected(){
	    gambarwarna.setImageResource(R.drawable.bggrayyellow);
	    beepbeep.start();
        if(alert != null && alert.isShowing() ) return;
        AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.this);
        a_builder.setTitle("Drowsiness Detected")
                .setMessage(R.string.ngantukdikit)
                .setCancelable(false)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        drowsycount = 1;
                        beepbeep.stop();
                    }
                });
        alert = a_builder.create();
        if (!alert.isShowing()) {
            alert.show();
        }
    }
}