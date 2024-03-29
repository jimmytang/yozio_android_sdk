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
package com.yozio.demo.implementations.entity;

import android.os.Bundle;
import com.socialize.EntityUtils;
import com.socialize.entity.Entity;
import com.socialize.error.SocializeException;
import com.socialize.listener.entity.EntityGetListener;
import com.yozio.demo.SDKDemoActivity;


/**
 * @author Jason Polites
 *
 */
public class GetEntityByKeyActivity extends SDKDemoActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		entryText.setText(entity.getKey());
	}

	/* (non-Javadoc)
	 * @see com.yozio.demo.DemoActivity#executeDemo()
	 */
	@Override
	public void executeDemo(final String text) {
		
		EntityUtils.getEntity(this, text, new EntityGetListener() {
			@Override
			public void onGet(Entity entity) {
				handleBasicSocializeResult(entity);
			}
			
			@Override
			public void onError(SocializeException error) {
				if(isNotFoundError(error)) {
					handleResult("No entity found with key [" +
							text +
							"]");
				}
				else {
					handleError(GetEntityByKeyActivity.this, error);
				}
			}
		});
	}
	
	@Override
	public boolean isTextEntryRequired() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.yozio.demo.DemoActivity#getButtonText()
	 */
	@Override
	public String getButtonText() {
		return "Get Entity by Key";
	}
}
