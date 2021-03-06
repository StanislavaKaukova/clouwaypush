package com.clouway.push.server;

import com.google.appengine.api.channel.ChannelFailureException;
import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.common.collect.Lists;
import com.google.inject.util.Providers;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class PushServiceImplTest {

  @Rule
  public final JUnitRuleMockery context = new JUnitRuleMockery();

  @Mock
  private SubscriptionsRepository repository;

  @Mock
  private EventSerializer eventSerializer;

  @Mock
  private ChannelService channelService;

  private PushService pushService;

  @Before
  public void setUp() throws Exception {
    pushService = new PushServiceImpl(repository, eventSerializer, Providers.of(channelService));
  }

  @Test
  public void pushEventToClient() throws Exception {
    final DefaultEvent event = new DefaultEvent();
    final String eventMessage = "event message";

    final Subscription subscription = Subscription.aNewSubscription().subscriber("subscriber").build();

    final ArgumentCaptor<ChannelMessage> channelMessageCaptor = new ArgumentCaptor<ChannelMessage>();

    context.checking(new Expectations() {{
      oneOf(eventSerializer).serialize(new PushEventSource(event,""));
      will(returnValue(eventMessage));

      oneOf(repository).findSubscriptions(event.getKey());
      will(returnValue(Lists.newArrayList(subscription)));

      oneOf(channelService).sendMessage(with(channelMessageCaptor));
    }});

    pushService.pushEvent(event);

    assertThat(channelMessageCaptor.getValue().getClientId(), is("subscriber"));
    assertThat(channelMessageCaptor.getValue().getMessage(), is("event message"));
  }

  @Test(expected = UnableToPushEventException.class)
  public void pushEventIsFailed() throws Exception {

    final DefaultEvent event = new DefaultEvent();
    final String eventMessage = "event message";

    final Subscription subscription = Subscription.aNewSubscription().subscriber("subscriber").build();

    final ArgumentCaptor<ChannelMessage> channelMessageCaptor = new ArgumentCaptor<ChannelMessage>();

    context.checking(new Expectations() {{

      oneOf(eventSerializer).serialize(new PushEventSource(event, ""));
      will(returnValue(eventMessage));

      oneOf(repository).findSubscriptions(event.getKey());
      will(returnValue(Lists.newArrayList(subscription)));

      oneOf(channelService).sendMessage(with(channelMessageCaptor));
      will(throwException(new ChannelFailureException("message")));
    }});

    pushService.pushEvent(event);

  }

  @Test
  public void pushEventToClientWithCorrelationId() throws Exception {
    final DefaultEvent event = new DefaultEvent();
    final String eventMessage = "event message";

    final Subscription subscription = Subscription.aNewSubscription().subscriber("subscriber").build();

    final ArgumentCaptor<ChannelMessage> channelMessageCaptor = new ArgumentCaptor<ChannelMessage>();

    context.checking(new Expectations() {{
      oneOf(eventSerializer).serialize(new PushEventSource(event, "correlationId"));
      will(returnValue(eventMessage));

      oneOf(repository).findSubscriptions(event.getKey() + "correlationId");
      will(returnValue(Lists.newArrayList(subscription)));

      oneOf(channelService).sendMessage(with(channelMessageCaptor));
    }});

    pushService.pushEvent(event, "correlationId");

    assertThat(channelMessageCaptor.getValue().getClientId(), is("subscriber"));
    assertThat(channelMessageCaptor.getValue().getMessage(), is("event message"));
  }

  @Test(expected = UnableToPushEventException.class)
  public void pushingEventWithCorrelationIdFails() throws Exception {

    final DefaultEvent event = new DefaultEvent();
    final String eventMessage = "event message";

    final Subscription subscription = Subscription.aNewSubscription().subscriber("subscriber").build();

    final ArgumentCaptor<ChannelMessage> channelMessageCaptor = new ArgumentCaptor<ChannelMessage>();

    context.checking(new Expectations() {{

      oneOf(eventSerializer).serialize(new PushEventSource(event,"correlationId"));
      will(returnValue(eventMessage));

      oneOf(repository).findSubscriptions(event.getKey() + "correlationId");
      will(returnValue(Lists.newArrayList(subscription)));

      oneOf(channelService).sendMessage(with(channelMessageCaptor));
      will(throwException(new ChannelFailureException("message")));
    }});

    pushService.pushEvent(event, "correlationId");

  }

}