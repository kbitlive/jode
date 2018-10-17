package com.tools.payhelper.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler;

/***
	 * @ClassName: KeepAliveRequestTimeoutHandlerImpl
	 * @Description: ��������ʱʱ�Ĵ���Ҳ������Ĭ�ϴ��� ����like
	 *               KeepAliveRequestTimeoutHandler.LOG�Ĵ���
	 * @author Minsc Wang ys2b7_hotmail_com
	 * @date 2011-3-7 ����04:15:39
	 * 
	 */
	class KeepAliveRequestTimeoutHandlerImpl implements
			KeepAliveRequestTimeoutHandler {

		/*
		 * (non-Javadoc)
		 * 
		 * @seeorg.apache.mina.filter.keepalive.KeepAliveRequestTimeoutHandler#
		 * keepAliveRequestTimedOut
		 * (org.apache.mina.filter.keepalive.KeepAliveFilter,
		 * org.apache.mina.core.session.IoSession)
		 */
		@Override
		public void keepAliveRequestTimedOut(KeepAliveFilter filter,
				IoSession session) throws Exception {
			System.out.println("������ʱ");
		}

	}