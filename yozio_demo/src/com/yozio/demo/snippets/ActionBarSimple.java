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
package com.yozio.demo.snippets;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.socialize.ActionBarUtils;
import com.socialize.entity.Entity;
import com.yozio.demo.R;

public class ActionBarSimple extends Activity {
	
	@SuppressWarnings("unused")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
//begin-snippet-0		
// Your entity key. 
String entityKey = "http://www.getsocialize.com";

// Create an entity object including a name
// The Entity object is Serializable, so you could also store the whole object in the Intent
Entity entity = Entity.newInstance(entityKey, "Socialize");

// The entity will be automatically created in the Socialize back end when this call completes.
View actionBarWrapped = ActionBarUtils.showActionBar(this, R.layout.actionbar, entity);
//end-snippet-0
		
	}
	

}



