/*
 * Copyright (c) 2012 Socialize Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.yozio.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;

import com.socialize.Socialize;
import com.socialize.android.ioc.IOCContainer;
import com.socialize.error.SocializeException;
import com.socialize.listener.SocializeInitListener;
import com.yozio.android.Yozio;

public class Main extends Activity {



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Yozio.configure(this, "eecc0ab0-a768-012f-a275-12314000c03b", "eed0cb20-a768-012f-a276-12314000c03b");

		initView();
	}



	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Intent intent = new Intent(Main.this, DemoList.class);
			startActivity(intent);
			finish();
		}
	};


	private void initView() {

		setContentView(R.layout.main);
		final TextView version = (TextView) findViewById(R.id.txtVersion);
		final View viewContainer = findViewById(R.id.container);
		final Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator());
		fadeOut.setDuration(2500);
		fadeOut.setFillAfter(true);


		fadeOut.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				handler.sendEmptyMessageDelayed(0, 1000);
			}
		});


		// Initialize Socialize asynchronously
		Socialize.initAsync(this, new SocializeInitListener() {
			@Override
			public void onError(SocializeException error) {
				error.printStackTrace();
				DemoUtils.showErrorDialog(Main.this, error);
				version.setText("Yozio");
				viewContainer.startAnimation(fadeOut);
			}

			@Override
			public void onInit(Context context, IOCContainer container) {
				version.setText("Yozio");
				viewContainer.startAnimation(fadeOut);
			}
		});
	}
}