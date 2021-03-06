/**
*Copyright [2012] [Ghetolay]
*
*Licensed under the Apache License, Version 2.0 (the "License");
*you may not use this file except in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing, software
*distributed under the License is distributed on an "AS IS" BASIS,
*WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*See the License for the specific language governing permissions and
*limitations under the License.
*/
package com.github.ghetolay.jwamp.message.output;

import com.github.ghetolay.jwamp.message.WampArguments;
import com.github.ghetolay.jwamp.message.WampCallMessage;


/**
 * @author ghetolay
 *
 */
public class OutputWampCallMessage extends WampCallMessage{
	
	private Object arg;
	
	public void setCallId(String callId) {
		this.callId = callId;
	}
	
	public void setProcId(String procId) {
		this.procId = procId;
	}
	
	public void setArgument(Object arg){
		this.arg = arg;
	}
	
	public Object getArgument(){
		return arg;
	}
	
	@Override
	public WampArguments getArguments(){
		throw new IllegalStateException("Use getArgument()");
	}
}
