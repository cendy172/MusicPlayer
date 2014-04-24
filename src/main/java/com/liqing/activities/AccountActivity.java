package com.liqing.activities;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import javax.net.ssl.HttpsURLConnection;
import com.liqing.R;
import com.liqing.mediaplayer_music.MainActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AccountActivity extends Activity {

	private TextView accountShowTextView, uploadTextView, downloadTextView;
	private LinearLayout loginFormLayout;
	private EditText userNameEditText, passwordEditText;
	private Button registerButton, loginButton;
	private ImageView backImageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_account);
		initView();
	}

	private void initView() {
		this.accountShowTextView = (TextView) this
				.findViewById(R.id.accountShow);
		this.uploadTextView = (TextView) this.findViewById(R.id.upload);
		this.downloadTextView = (TextView) this.findViewById(R.id.download);
		this.loginFormLayout = (LinearLayout) this.findViewById(R.id.loginForm);
		this.userNameEditText = (EditText) this.findViewById(R.id.userNameEdt);
		this.passwordEditText = (EditText) this.findViewById(R.id.passwordEdt);
		this.registerButton = (Button) this.findViewById(R.id.registerBtn);
		this.loginButton = (Button) this.findViewById(R.id.loginBtn);
		this.backImageView = (ImageView) this.findViewById(R.id.back);

		if (MainActivity.userName == null) {
			loginFormLayout.setVisibility(View.GONE);
		} else {
			this.accountShowTextView.setText(MainActivity.userName);
			accountShowTextView.setEnabled(false);
		}

		View.OnClickListener onClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v.getId() == accountShowTextView.getId()) {
					loginFormLayout.setVisibility(View.VISIBLE);
					userNameEditText.setFocusable(true);
				} else if (v.getId() == backImageView.getId()) {
					finish();
				} else if (v.getId() == loginButton.getId()) {
					if (userNameEditText.getText().toString().equals("")
							|| passwordEditText.getText().toString().equals("")) {
						Toast.makeText(AccountActivity.this, "用户名和密码不能为空",
								Toast.LENGTH_SHORT).show();
					} else {
						login();
					}
				} else if (v.getId() == registerButton.getId()) {
					register();
				} else if (v.getId() == uploadTextView.getId()
						&& MainActivity.userName != null) {
					upload();
				} else if (v.getId() == downloadTextView.getId()
						&& MainActivity.userName != null) {
					download();
				}
			}
		};

		this.accountShowTextView.setOnClickListener(onClickListener);
		this.backImageView.setOnClickListener(onClickListener);
		this.loginButton.setOnClickListener(onClickListener);
		this.registerButton.setOnClickListener(onClickListener);
		this.uploadTextView.setOnClickListener(onClickListener);
		this.downloadTextView.setOnClickListener(onClickListener);
	}

	private void login() {
		URL url;
		try {
			String loginUrl = String.format(
					"....../login?username=%s&password=%s", userNameEditText
							.getText().toString(), URLEncoder.encode(
							passwordEditText.getText().toString(), "UTF-8"));
			url = new URL(loginUrl);
			URLConnection loginConnection = url.openConnection();
			HttpsURLConnection httpsURLConnection = (HttpsURLConnection) loginConnection;
			int responseCode = httpsURLConnection.getResponseCode();
			if (responseCode == HttpsURLConnection.HTTP_OK) {
				// 返回用户名
				accountShowTextView.setText("username");
				MainActivity.userName = "username";
				loginFormLayout.setVisibility(View.GONE);
			} else {
				this.passwordEditText.setText("");
				Toast.makeText(this, "登录失败！请输入正确的用户名和密码!", Toast.LENGTH_SHORT)
						.show();
			}
		} catch (Exception ex) {

		}
	}

	private void register() {
		Intent registIntent = new Intent();
		registIntent.setAction("android.intent.action.VIEW");
		Uri conneUri = Uri
				.parse("http://198.71.86.210:8080/Music/admin/jsp/reg/register.jsp");
		registIntent.setData(conneUri);
		startActivity(registIntent);
	}

	private void upload() {
		HttpsURLConnection httpsURLConnection;
		DataOutputStream dataOutputStream;
		DataInputStream dataInputStream;
		String uploadUrl = "#";

		try {
			URL url = new URL(uploadUrl);
			httpsURLConnection = (HttpsURLConnection) url.openConnection();
			
			httpsURLConnection.setDoInput(true);
			httpsURLConnection.setDoOutput(true);
			httpsURLConnection.setRequestMethod("POST");
			
			dataOutputStream = new DataOutputStream(httpsURLConnection.getOutputStream());
		} catch (Exception exception) {

		}

	}

	private void download() {
		initNet();

	}

	private void initNet() {

	}

}
