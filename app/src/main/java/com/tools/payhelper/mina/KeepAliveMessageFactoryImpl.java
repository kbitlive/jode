package com.tools.payhelper.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;

/***
	 * @ClassName: KeepAliveMessageFactoryImpl
	 * @Description: �ڲ��࣬ʵ����������
	 * @author Minsc Wang ys2b7_hotmail_com
	 * @date 2011-3-7 ����04:09:02
	 * 
	 */

	class KeepAliveMessageFactoryImpl implements
	/** ���������� */
			KeepAliveMessageFactory {
		private static final String HEARTBEATREQUEST = "HEARTBEATREQUEST";
		private static final String HEARTBEATRESPONSE = "HEARTBEATRESPONSE";

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.mina.filter.keepalive.KeepAliveMessageFactory#getRequest
		 * (org.apache.mina.core.session.IoSession)
		 */
		@Override
		public Object getRequest(IoSession session) {
			/** ����Ԥ����� */
			System.out.println("心跳包发送时间到了");
			return HEARTBEATREQUEST;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.mina.filter.keepalive.KeepAliveMessageFactory#getResponse
		 * (org.apache.mina.core.session.IoSession, java.lang.Object)
		 */
		@Override
		public Object getResponse(IoSession session, Object request) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.mina.filter.keepalive.KeepAliveMessageFactory#isRequest
		 * (org.apache.mina.core.session.IoSession, java.lang.Object)
		 */
		@Override
		public boolean isRequest(IoSession session, Object message) {
			if(message.equals(HEARTBEATREQUEST))
				return true;
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.apache.mina.filter.keepalive.KeepAliveMessageFactory#isResponse
		 * (org.apache.mina.core.session.IoSession, java.lang.Object)
		 */
		@Override
		public boolean isResponse(IoSession session, Object message) {
			if(message.equals(HEARTBEATRESPONSE))
				return true;
			return false;
		}

	}