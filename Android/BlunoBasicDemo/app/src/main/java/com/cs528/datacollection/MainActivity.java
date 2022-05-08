package com.cs528.datacollection;

import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends BlunoLibrary {
	private Button buttonScan;
	private Button buttonCollectDay;
	private Button buttonCollectNight;
	private Button buttonStop;
	private Button buttonCalibrate;
	private Button buttonVibrate;
	private TextView textCalibrate;

	int state = 0;

	boolean allow_writes = false;

	private StringBuilder data_buffer = new StringBuilder();
	private int data_counter = 0;

	final String SENSOR_FILE_LABEL = Long.toString(System.currentTimeMillis());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		request(1000, new OnPermissionsResult() {
			@Override
			public void OnSuccess() {
				Toast.makeText(MainActivity.this,"Permissions Granted",Toast.LENGTH_SHORT).show();
			}

			@Override
			public void OnFail(List<String> noPermissions) {
				Toast.makeText(MainActivity.this,"Permissions Failed",Toast.LENGTH_SHORT).show();
			}
		});

        onCreateProcess();														//onCreate Process by BlunoLibrary


        serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200

//        serialReceivedText=(TextView) findViewById(R.id.serialReveicedText);	//initial the EditText of the received data
//        serialSendText=(EditText) findViewById(R.id.serialSendText);			//initial the EditText of the sending data
//
//        buttonSerialSend = (Button) findViewById(R.id.buttonSerialSend);		//initial the button for sending the data
//        buttonSerialSend.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//
//				serialSend(serialSendText.getText().toString());				//send the data to the BLUNO
//			}
//		});

        buttonScan = (Button) findViewById(R.id.buttonScan);					//initial the button for scanning the BLE device
        buttonScan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				buttonScanOnClickProcess();										//Alert Dialog for selecting the BLE device
			}
		});

        /*
        * New Content
        */
		buttonCollectDay = (Button) findViewById(R.id.start_day);
		buttonCollectDay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				state = 1;
				String start_timestamp = System.currentTimeMillis() + ",0,0"  + "\n";
				writeData(start_timestamp, getApplicationContext());
				allow_writes = true;
				serialSend("1");
				buttonCollectDay.setVisibility(View.INVISIBLE);
				buttonCollectNight.setVisibility(View.INVISIBLE);
				buttonCalibrate.setVisibility(View.INVISIBLE);
				buttonStop.setVisibility(View.VISIBLE);
				buttonVibrate.setVisibility(View.INVISIBLE);
			}
		});
		buttonCollectNight = (Button) findViewById(R.id.start_night);
		buttonCollectNight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				state = 2;
				String start_timestamp = System.currentTimeMillis() + ",0,1"  + "\n";
				writeData(start_timestamp, getApplicationContext());
				allow_writes = true;
				serialSend("1");
				buttonCollectDay.setVisibility(View.INVISIBLE);
				buttonCollectNight.setVisibility(View.INVISIBLE);
				buttonCalibrate.setVisibility(View.INVISIBLE);
				buttonStop.setVisibility(View.VISIBLE);
				buttonVibrate.setVisibility(View.INVISIBLE);
			}
		});
		buttonStop = (Button) findViewById(R.id.stop);
		buttonStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				allow_writes = false;
				if(state < 3) {
					String stop_timestamp = System.currentTimeMillis() + ",1," + (state-1) + "\n";
					writeData(stop_timestamp, getApplicationContext());
				}
				state = 0;
				serialSend("0");
				buttonCollectDay.setVisibility(View.VISIBLE);
				buttonCollectNight.setVisibility(View.VISIBLE);
				buttonCalibrate.setVisibility(View.VISIBLE);
				buttonStop.setVisibility(View.INVISIBLE);
				textCalibrate.setVisibility(View.INVISIBLE);
				buttonVibrate.setVisibility(View.VISIBLE);
			}
		});

		buttonCalibrate = (Button) findViewById(R.id.calibrate);
		buttonCalibrate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				state = 3;
				serialSend("2");
				buttonCollectDay.setVisibility(View.INVISIBLE);
				buttonCollectNight.setVisibility(View.INVISIBLE);
				buttonCalibrate.setVisibility(View.INVISIBLE);
				buttonStop.setVisibility(View.VISIBLE);
				textCalibrate.setVisibility(View.VISIBLE);
				buttonVibrate.setVisibility(View.INVISIBLE);
			}
		});
		textCalibrate = (TextView) findViewById(R.id.calibrate_data);
		textCalibrate.setVisibility(View.INVISIBLE);

		buttonVibrate = (Button) findViewById(R.id.vibrate);
		buttonVibrate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				serialSend("3");
			}
		});

	}

	protected void onResume(){
		super.onResume();
		System.out.println("CS528 DataCollect onResume");
		onResumeProcess();												//onResume Process by BlunoLibrary

	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    @Override
    protected void onPause() {
        super.onPause();
//        onPauseProcess();														//onPause Process by BlunoLibrary
    }
	
	protected void onStop() {
		super.onStop();
//		onStopProcess();														//onStop Process by BlunoLibrary
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
			buttonScan.setText("Connected");
			break;
		case isConnecting:
			buttonScan.setText("Connecting");
			break;
		case isToScan:
			buttonScan.setText("Scan");
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


	private ArrayList<String> clean_bad_data(String input) {
		String[] value_tuples = input.split("\n");

		ArrayList<String> output = new ArrayList<>();

		for(String tuple : value_tuples) {
			tuple = tuple.trim();
			if(tuple.startsWith("[") && tuple.endsWith("]")) { // needs to be enclosed
//				System.out.println("First check");
				String tuple_tags_removed = tuple.substring(1, tuple.length()-1);
				if(!tuple_tags_removed.contains("[") && !tuple_tags_removed.contains("]")) { // no nested tags
//					System.out.println("Second check");
					String[] elements = tuple_tags_removed.split(",");
					if(elements.length == 4) {
						boolean fourth_check = true;
//						System.out.println("Third check");
						for(String element : elements) {
							try {
								Integer.parseInt(element);
							}
							catch(NumberFormatException nfe) {
								fourth_check = false;
								break;
							}
						}
						if(fourth_check) {
							output.add(tuple);
						}
					}
				}
			}
		}

		return output;
	}

	private String format_data(ArrayList<String> tuples, long timestamp) {
		StringBuilder output = new StringBuilder();

		int tag = state-1;
		timestamp -= (long) (tuples.size())*50;
		for(String tuple : tuples) {
			String tuple_cleaned = tuple.trim().substring(1, tuple.length()-1);
			output.append(timestamp);
			output.append(",");
			output.append(tuple_cleaned);
			output.append(",");
			output.append(tag);
			output.append("\n");
			timestamp += 50;
		}
		return output.toString();
	}

	@Override
	public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
		// TODO Auto-generated method stub
//		System.out.println(theString);

		if(state == 3) { // calibration
			textCalibrate.setText(theString.trim());
		}
		else if(allow_writes) { // normal data acquisition
			data_buffer.append(theString);
			if(theString.contains("]")) {
				data_counter++;
			}
			if(data_counter == 20) {
				long ts = System.currentTimeMillis();
				ArrayList<String> cleaned_data = clean_bad_data(data_buffer.toString());
				String formatted_data = format_data(cleaned_data, ts);
				writeData(formatted_data, getApplicationContext());
				data_buffer = new StringBuilder();
				data_counter = 0;
			}
		}
	}

	private void writeData(String data, Context context) {
		System.out.println("WRITING DATA");
		String base_dir = context.getExternalFilesDir(null).getAbsolutePath();
		String filename = SENSOR_FILE_LABEL + "_sensor_data.csv";
		File csv = new File(base_dir, filename);
		try {
			FileWriter fw = new FileWriter(csv, true);
			fw.write(data);
			fw.close();
		}
		catch (IOException e) {
			System.out.println("File write failed: " + e);
		}
	}

}