package com.clouway.push.client.channelapi;

import com.clouway.push.shared.PushEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Ivan Lazov <ivan.lazov@clouway.com>
 */
public interface PushChannelServiceAsync {

  void open(AsyncCallback<String> async);

  void subscribe(PushEvent.Type type, AsyncCallback<Void> async);

  void unsubscribe(PushEvent event, AsyncCallback<Void> async);

  void dummyMethod(AsyncCallback<PushEvent> async);
}
