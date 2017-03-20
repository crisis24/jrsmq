package com.wedeploy.jrsmq.cmd;

import com.wedeploy.jrsmq.Fixtures;
import com.wedeploy.jrsmq.QueueAttributes;
import org.junit.Before;
import org.junit.Test;

import static com.wedeploy.jrsmq.Fixtures.TEST_QNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class QueueAttributesTest {

	@Before
	public void setUp() {
		Fixtures.cleanup();
	}

	@Test
	public void testGetQueueAttributes() {
		Fixtures.TestRedisSMQ rsmq = Fixtures.redisSMQ();

		rsmq.connect().createQueue().qname(TEST_QNAME).execute();

		rsmq.sendMessage().qname(TEST_QNAME).message("Hello1").execute();
		rsmq.receiveMessage().qname(TEST_QNAME).execute();
		rsmq.sendMessage().qname(TEST_QNAME).message("Hello2").execute();

		QueueAttributes qa = rsmq.getQueueAttributes().qname(TEST_QNAME).execute();

		assertNotNull(qa);
		assertEquals(0, qa.delay());
		assertEquals(2, qa.totalSent());
		assertEquals(1, qa.totalRecv());
		assertEquals(2, qa.msgs());
		assertEquals(2, qa.hiddenMsgs());

		// cleanup

		rsmq.deleteQueue().qname(TEST_QNAME).execute();
		rsmq.quit();
	}

	@Test
	public void testSetQueueAttributes_noChange() throws InterruptedException {
		Fixtures.TestRedisSMQ rsmq = Fixtures.redisSMQ();

		rsmq.connect().createQueue().qname(TEST_QNAME).execute();

		Thread.sleep(1000);

		rsmq.setQueueAttributes().qname(TEST_QNAME).execute();
		QueueAttributes qa = rsmq.getQueueAttributes().qname(TEST_QNAME).execute();

		assertEquals(0, qa.delay());
		assertTrue(qa.modified() > qa.created());

		// cleanup

		rsmq.deleteQueue().qname(TEST_QNAME).execute();
		rsmq.quit();
	}

	@Test
	public void testSetQueueAttributes() throws InterruptedException {
		Fixtures.TestRedisSMQ rsmq = Fixtures.redisSMQ();

		rsmq.connect().createQueue().qname(TEST_QNAME).execute();

		Thread.sleep(1000);

		QueueAttributes qa = rsmq.setQueueAttributes().qname(TEST_QNAME).delay(100).execute();

		assertEquals(100, qa.delay());
		assertTrue(qa.modified() > qa.created());

		// cleanup

		rsmq.deleteQueue().qname(TEST_QNAME).execute();
		rsmq.quit();
	}
}
