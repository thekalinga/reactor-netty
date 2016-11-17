/*
 * Copyright (c) 2011-2016 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.ipc.netty.channel;

import java.io.IOException;

import io.netty.channel.ChannelHandlerContext;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * @author Stephane Maldini
 */
final class CloseSubscriber implements Subscriber<Void> {

	final ChannelHandlerContext   ctx;
	final ChannelOperations<?, ?> parent;

	public CloseSubscriber(ChannelOperations<?, ?> ops, ChannelHandlerContext ctx) {
		this.ctx = ctx;
		this.parent = ops;
	}

	@Override
	public void onComplete() {
		if (ChannelOperations.log.isDebugEnabled()) {
			ChannelOperations.log.debug("Closing connection");
		}
		parent.cancel();
	}

	@Override
	public void onError(Throwable t) {
		if (t instanceof IOException && t.getMessage()
		                                 .contains("Broken pipe")) {
			if (ChannelOperations.log.isDebugEnabled()) {
				ChannelOperations.log.debug("Connection closed remotely", t);
			}
			parent.cancel();
			return;
		}

		ChannelOperations.log.error("Error processing connection. Closing the channel.", t);
		parent.cancel();
	}

	@Override
	public void onNext(Void aVoid) {
	}

	@Override
	public void onSubscribe(Subscription s) {
		s.request(Long.MAX_VALUE);
	}
}