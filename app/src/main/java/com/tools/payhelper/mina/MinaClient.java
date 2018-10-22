package com.tools.payhelper.mina;

import android.content.Context;
import android.text.TextUtils;

import com.tools.payhelper.ConFigNet;
import com.tools.payhelper.CustomApplcation;
import com.tools.payhelper.eventbus.NetOffLine;
import com.tools.payhelper.utils.URLRequest;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.greenrobot.eventbus.EventBus;

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MinaClient {

	private static ExecutorService cachedThreadPool;
	public  ConnectFuture cf = null;
	public  NioSocketConnector connector;
	private  TimeClientHandler tm=null;
	private static MinaClient single;
	Context mcontext;
	private  static String ipaddress;
	private static int org_port;

	public static MinaClient getinstance(){
		if (null==single) {
			single=new MinaClient();
			cachedThreadPool = Executors.newCachedThreadPool(); //new 出一个新的线程池
		}
		return single;
	}
	/**
	 *
	 *
	 * @param address
	 * @param port
	 */

	public synchronized void getconnect(final String address, final int port, final Context context) {
		if (null==connector) {
			this.ipaddress =address;
			this.org_port =port;
			mcontext=context;
			connector = new NioSocketConnector();
			connector.getFilterChain().addLast("logger", new LoggingFilter());
			connector.getFilterChain().addLast("mycoder", new ProtocolCodecFilter(new MyCodecFcatory(new MyEncoder(), new MyDecoder())));
			ExecutorService executorService = Executors.newCachedThreadPool();
			connector.getFilterChain().addLast("threadPool", new ExecutorFilter(executorService));
			connector.getSessionConfig().setTcpNoDelay(true);
			connector.setConnectTimeoutMillis(6000);
			connector.getSessionConfig().setKeepAlive(true);
			tm = new TimeClientHandler(context,connector);
			connector.setHandler(tm);//
			connector.addListener(new IoListener());
			System.out.println("重新创建");
			cf = connector.connect(new InetSocketAddress(address, port));//
			connector.getFilterChain().addFirst("reconnection",new IoFilterAdapter(){
				@Override
				public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
					super.sessionIdle(nextFilter, session, status);
					if (status == IdleStatus.READER_IDLE) {
						System.out.println("40秒没有读取到数据,进入读的空闲状态");
						session.close(true);//
					} else if (status == IdleStatus.WRITER_IDLE) {
						if (null != MinaClient.getinstance().cf) {
							URLRequest.getInstance().send101(ConFigNet.socketip,mcontext);
						}
					}
//
				}

				@Override
				public void sessionClosed(NextFilter nextFilter, IoSession session) throws Exception {
					super.sessionClosed(nextFilter, session);
					System.out.println("......................掉线了............");
					EventBus.getDefault().post(new NetOffLine());
					if (CustomApplcation.getInstance().isDisConnect()){
						CustomApplcation.getInstance().setDisConnect(false);
						System.out.println("主动断开与服务器的连接");
						return;
					}
					tag: for(;;){
						try{
							Thread.sleep(3000);
							cf = connector.connect(new InetSocketAddress(ipaddress, org_port));
							cf.awaitUninterruptibly();// 等待连接创建成功
							session = cf.getSession();// 获取会话
							if(session.isConnected()){
								System.out.println(".............................重连成功....................");
								ConFigNet configNet = new ConFigNet();
								String uname=configNet.getuname(context.getApplicationContext(),"uname");
								String pasword=configNet.getuname(context.getApplicationContext(),"pasword");
								System.out.println("打印账号:"+uname+"密码:"+pasword);
								if (!TextUtils.isEmpty(uname)&&!TextUtils.isEmpty(pasword)){
									URLRequest.getInstance().send100(configNet.socketip,mcontext,uname,pasword);
								}else{
									System.out.println("账号或密码为空");
								}
								break tag;
							}
						}catch(Exception ex){
							cf.cancel();
							System.out.println("重连服务器登录失败,3秒再连接一次:" + ex.getMessage());
						}
					}
//					isSuccess=false;
//					tag: for(;;){
//						Thread.sleep(3000);
//						System.out.println("是否创建成功:"+isSuccess);
//						if (isSuccess)break tag;
//						reconnect(0);
//					}
//
//
				}
			});
//			cf.awaitUninterruptibly();// 等待连接创建成功
//			IoSession session = cf.getSession();// 获取会话
//			if(null!=session&&session.isConnected()) {
//				current_id = session.getId();
//				isSuccess=true;
//			}else{
//				System.out.println("创建失败");
//			}
		}
	}
	public  void reLease(){
		if (null!=connector){
			connector=null;
			if (null!=cf){
				cf.getSession().close(true);
				cf.awaitUninterruptibly();
			}
			cf=null;
			tm=null;
		}
	}

	public static  boolean isConnected(){
		try {
			if (null==single.connector)return false;
			if (null==single.cf)return false;
			if (null!=single.cf.getException())return false;
			if (single.cf.getSession().isConnected())return true;
			else return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	public  static boolean isDisPose(){
		if (null==single.cf)return true;
		try {
			if (null!=single.cf.getException())return true;
			if (single.cf.getSession().getService().isDisposed()||single.cf.getSession().getService().isDisposing())return true;
			else return false;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

	/**
	 *
	 *
	 * @param serviceAddress
	 * @param message
	 * @throws Exception
	 */
	public  void sendMessage(String serviceAddress, byte[] message, Context context, Object cmd) throws Exception {
		if (null==serviceAddress||"".equals(serviceAddress)){
//			reconnect(1);
			return;
		}
		String[] address = serviceAddress.split(":");
		mcontext=context;
		String ip="";
		int port = 0;
		try {
			ip=address[0];
			port = Integer.valueOf(address[1]);
			this.ipaddress =ip;
			this.org_port =port;
			if (null == cf) {
				System.out.println("socket重连了"+"ip"+ip+"prot"+port);
				getconnect(ip, port,context);
				cf.awaitUninterruptibly();////等待连接创建完成
			}
			if (message.length!=0&&null!=cf.getSession()) {
				if (null!=tm)tm.setcontext(context);
				try {
					cf.getSession().setAttribute(String.valueOf(cmd),cmd);
					int cmdtest=(int)cmd;
					if (cf.getSession().isConnected()){
                    WriteFuture writeFuture = cf.getSession().write(IoBuffer.wrap(message)).awaitUninterruptibly();// 等待发送完成
					}
				} catch (Exception e) {
					System.out.println("..............session获取失败................");
				}
			}else {
				System.out.println("发送的空的数据包");
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new Exception("数据异常");
		}
	}
}