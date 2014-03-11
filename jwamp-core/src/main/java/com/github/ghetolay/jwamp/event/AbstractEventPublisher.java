/**
 * 
 */
package com.github.ghetolay.jwamp.event;

import java.net.URI;
import java.util.Collection;

import com.github.ghetolay.jwamp.message.WampPublishMessage;
import com.github.ghetolay.jwamp.utils.ObjectHolder;
import com.github.ghetolay.jwamp.utils.ObjectHolderFactory;

/**
 * @author Kevin
 *
 */
public abstract class AbstractEventPublisher implements EventPublisher {

	/**
	 * 
	 */
	public AbstractEventPublisher() {
		// TODO Auto-generated constructor stub
	}

	abstract protected void sendMessage(WampPublishMessage message);
	
	@Override
	public void publishEvent(URI topicURI, Object event) {
		ObjectHolder jsonEvent = ObjectHolderFactory.createForObject(event);
		WampPublishMessage message = WampPublishMessage.createSimple(topicURI, jsonEvent); 
		
		sendMessage(message);
		
	}

	@Override
	public void publishEvent(URI topicURI, Object event, boolean excludeMe){
		ObjectHolder jsonEvent = ObjectHolderFactory.createForObject(event);
		WampPublishMessage message = WampPublishMessage.createExcludeMe(topicURI, jsonEvent, excludeMe);
		
		sendMessage(message);
	}
	
	@Override
	public void publishEvent(URI topicURI, Object event, Collection<String> exclude){
		ObjectHolder jsonEvent = ObjectHolderFactory.createForObject(event);
		WampPublishMessage message = WampPublishMessage.createWithExclude(topicURI, jsonEvent, exclude);
		
		sendMessage(message);
	}
	
	@Override
	public void publishEvent(URI topicURI, Object event, Collection<String> exclude, Collection<String> eligible){
		ObjectHolder jsonEvent = ObjectHolderFactory.createForObject(event);
		WampPublishMessage message = WampPublishMessage.createWithExcludeAndEligible(topicURI, jsonEvent, exclude, eligible);
		
		sendMessage(message);
	}

}