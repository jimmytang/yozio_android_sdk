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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import com.socialize.Socialize;
import com.socialize.entity.Entity;
import com.socialize.ui.dialog.DialogRegister;


/**
 * @author Jason Polites
 *
 */
public abstract class DemoActivity extends Activity implements DialogRegister {
	
	private List<Dialog> dialogs = new ArrayList<Dialog>();
	protected Entity entity;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		entity = Entity.newInstance("http://getsocialize.com", "Socialize");
		
		Socialize.onCreate(this, savedInstanceState);
	}
	
	/* (non-Javadoc)
	 * @see com.socialize.ui.dialog.DialogRegister#register(android.app.Dialog)
	 */
	@Override
	public void register(Dialog dialog) {
		dialogs.add(dialog);
	}

	/* (non-Javadoc)
	 * @see com.socialize.ui.dialog.DialogRegister#getDialogs()
	 */
	@Override
	public Collection<Dialog> getDialogs() {
		return dialogs;
	}
	
	protected void handleError(Activity context, Exception error) {
		error.printStackTrace();
		DemoUtils.showErrorDialog(context, error);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Socialize.onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Socialize.onResume(this);
	}

	@Override
	protected void onDestroy() {
		if(dialogs != null) {
			for (Dialog dialog : dialogs) {
				dialog.dismiss();
			}
			dialogs.clear();
		}		
		
		Socialize.onDestroy(this);
		
		super.onDestroy();
	}
}
