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
package com.github.ghetolay.jwamp.event;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.message.SerializationException;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.WampPublishMessage;
import com.github.ghetolay.jwamp.message.WampSubscribeMessage;
import com.github.ghetolay.jwamp.message.WampUnsubscribeMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampEventMessage;
import com.github.ghetolay.jwamp.utils.ActionMapping;

public abstract class AbstractEventManager implements WampMessageHandler, EventSender {
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	//TODO changer action mapping
	private ActionMapping<EventAction> eventMapping;
	
	//TODO ability to change eventMapping dynamically 
	public AbstractEventManager(ActionMapping<EventAction> eventMapping){
		if(eventMapping == null)
			throw new IllegalArgumentException("EventMapping can't be null");
		
		this.eventMapping = eventMapping;
		for(Iterator<EventAction> it = eventMapping.getActionsIterator(); it.hasNext();)
			it.next().setEventSender(this);
	}
	
	public boolean onMessage(String sessionId, WampMessage message){
	
		try{
			switch(message.getMessageType()){
				case WampMessage.SUBSCRIBE :
					onSubscribe(sessionId, (WampSubscribeMessage)message);
					return true;
				case WampMessage.UNSUBSCRIBE :
					onUnsubscribe(sessionId, (WampUnsubscribeMessage)message);
					return true;
				case WampMessage.PUBLISH :
					onPublish(sessionId, (WampPublishMessage)message);
					return true;
				default : 
					return false;
			}
		}catch(SerializationException e){
			//TODO
			log.trace("",e);
			return true;
		}
	}

	public void onSubscribe(String sessionId, WampSubscribeMessage wampSubscribeMessage) {
		EventAction e = eventMapping.getAction(wampSubscribeMessage.getTopicId());
		if(e != null)
			e.subscribe(sessionId);
		else if(log.isDebugEnabled())
			log.debug("unable to subscribe : action name doesn't not exist " + wampSubscribeMessage.getTopicId());
	}

	public void onUnsubscribe(String sessionId, WampUnsubscribeMessage wampUnsubscribeMessage) {
		EventAction e = eventMapping.getAction(wampUnsubscribeMessage.getTopicId());
		if(e != null)
			e.unsubscribe(sessionId);
		else if(log.isDebugEnabled())
			log.debug("unable to unsubscribe : action name doesn't not exist " + wampUnsubscribeMessage.getTopicId());
	}

	//TODO new publish
	public void onPublish(String sessionId, WampPublishMessage wampPublishMessage) throws SerializationException {
		EventAction e = eventMapping.getAction(wampPublishMessage.getTopicId());
		if(e != null){
			OutputWampEventMessage msg = new OutputWampEventMessage();
			msg.setTopicId(wampPublishMessage.getTopicId());
			msg.setEvent( wampPublishMessage.getEvent() );
			
			//e.publishTo think about it
			List<String> publishList = e.publishTo( getPublishList(e, sessionId, wampPublishMessage), wampPublishMessage, msg);
			if(publishList != null)
				for(String s : publishList)
						sendEvent(s, msg);
			
		}else if(log.isDebugEnabled())
			log.debug("unable to publish : action name doesn't not exist " + wampPublishMessage.getTopicId());
	}
	
	private List<String> getPublishList(EventAction e, String sessionId,WampPublishMessage wampPublishMessage){
		if(wampPublishMessage.getEligible() != null)
			return wampPublishMessage.getEligible();
		
		List<String> res;
		if(wampPublishMessage.getExclude() != null){
			res = new ArrayList<String>(e.getSubscriber());
			for(String s : wampPublishMessage.getExclude())
				res.remove(s);
		}
		else{ 
			if(wampPublishMessage.isExcludeMe()){
				res = new ArrayList<String>(e.getSubscriber());
				res.remove(sessionId);
			}else 
				res = new ArrayList<String>(e.getSubscriber());
		}
		
		return res;	
	}
	 
	public void sendEvent(String sessionId, String eventId, Object event) throws SerializationException{		
		OutputWampEventMessage msg = new OutputWampEventMessage();
		msg.setTopicId(eventId);
		msg.setEvent(event);
			
		sendEvent(sessionId, msg);
	}
	
	private void sendEvent(String sessionId, OutputWampEventMessage msg) throws SerializationException{
		WampConnection con = getConnection(sessionId);
		if(con != null)
			try {
				con.sendMessage(msg);
			} catch (IOException e) {
				if(log.isErrorEnabled())
					log.error("Unable to send event message : " + e.getMessage());
			}
		else if(log.isWarnEnabled())
			log.warn("Unable to find connection : " + sessionId);
	}
	
	public void onClose(String sessionId, int closeCode) {
		for(Iterator<EventAction> it = eventMapping.getActionsIterator(); it.hasNext();)
			it.next().unsubscribe(sessionId);
	}
	
	protected abstract WampConnection getConnection(String sessionId);
}
