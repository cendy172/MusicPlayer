package com.liqing.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.liqing.R;

public class EqualizerActivity extends Activity {

	private RadioGroup sounsRadioGroup;
	private ArrayList<RadioButton> radioButtons = null;
	private int preset = 0;
	private ImageView back;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_sounds);
		
		back = (ImageView) this.findViewById(R.id.back);
		
		sounsRadioGroup = (RadioGroup) this.findViewById(R.id.sounds);
		radioButtons = new ArrayList<RadioButton>();
		initRadioButtons();
		
		preset = getIntent().getIntExtra("currentPreset", 0);
		radioButtons.get(preset).setChecked(true);
		for(RadioButton radioButton : radioButtons){
			sounsRadioGroup.addView(radioButton);
		}
		this.sounsRadioGroup.setOnCheckedChangeListener(soundsCheckedListener);
		this.back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("preset", preset);
				if(getParent() == null){
					setResult(RESULT_OK,intent);
				}else{
					getParent().setResult(RESULT_OK,intent);
				}
				finish();
			}
		});
	}

	private void initRadioButtons(){
		Equalizer equalizer = new Equalizer(0, new MediaPlayer().getAudioSessionId());
		ArrayList<String> presetNameStrings = new ArrayList<String>();
		int numberOfPresets = equalizer.getNumberOfPresets();
		for(int i=0;i<numberOfPresets;i++){
			presetNameStrings.add(equalizer.getPresetName((short) i));
			RadioButton radioButton = new RadioButton(this);
			radioButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			radioButton.setText(presetNameStrings.get(i));
			radioButton.setButtonDrawable(R.drawable.radiobutton_selector);
			radioButton.setId(i);
			radioButtons.add(radioButton);
		}
		
	}
	
	private OnCheckedChangeListener soundsCheckedListener = new RadioGroup.OnCheckedChangeListener() {
		
		/*
        Normal
        Classical
        Dance
        Flat
        Folk 民族
        Heavy Metal 重金属
        Hip Hop
        Jazz
        Pop
        Rock
        */

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			for(int i = 0;i<radioButtons.size();i++){
				if(checkedId == radioButtons.get(i).getId()){
					preset = i;
					break;
				}
			}
		}
	};
}
