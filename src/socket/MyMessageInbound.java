package socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.WsOutbound;

import util.MessageUtil;

@SuppressWarnings("deprecation")
public class MyMessageInbound extends MessageInbound {

	private String name;
	public MyMessageInbound() {
		super();
	}

	public MyMessageInbound(String name) {
		super();
		this.name = name;
	}

	@Override  
	protected void onBinaryMessage(ByteBuffer arg0) throws IOException {  

	}  

	@Override  
	protected void onTextMessage(CharBuffer msg) {

		
		HashMap<String,String> messageMap = MessageUtil.getMessage(msg);    //处理消息类
		String fromName = messageMap.get("fromName");    //消息来自人 的userId
		String toName = messageMap.get("toName");       //消息发往人的 userId
		String mapContent = messageMap.get("content");
		
		if("all".equals(toName)){
			String msgContentString = fromName + "说: " + mapContent;   //构造发送的消息
			String content = MessageUtil.sendContent(MessageUtil.MESSAGE,msgContentString);
			broadcastAll(content);
		}else{
			try {
				singleChat(fromName,toName,mapContent);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}  

	private void singleChat(String fromName, String toName, String mapContent) throws IOException {
		HashMap<String, MessageInbound> userMsgMap = InitServlet.getSocketList();
		MessageInbound messageInbound = userMsgMap.get(toName);    //在仓库中取出发往人的MessageInbound
		MessageInbound messageFromInbound = userMsgMap.get(fromName);
		if(messageInbound!=null && messageFromInbound!=null){     //如果发往人 存在进行操作
			WsOutbound outbound = messageInbound.getWsOutbound(); 
			WsOutbound outFromBound = messageFromInbound.getWsOutbound();
			
			String msgContentString = fromName + "说: " + mapContent;   //构造发送的消息
			String contentTemp = MessageUtil.sendContent(MessageUtil.MESSAGE,msgContentString);
			
			outFromBound.writeTextMessage(CharBuffer.wrap(contentTemp.toCharArray()));
			outbound.writeTextMessage(CharBuffer.wrap(contentTemp.toCharArray()));  //
			
			outFromBound.flush();
			outbound.flush();
		}else{
			String content = MessageUtil.sendContent(MessageUtil.MESSAGE,"客服不在线请留言...");
			broadcastAll(content);
		}
	}

	@Override  
	protected void onClose(int status) {  
		InitServlet.getSocketList().remove(this);
		String names = getNames();
		String content = MessageUtil.sendContent(MessageUtil.USER,names);
		broadcastAll(content);
		super.onClose(status);
	}  

	@Override
	protected void onOpen(WsOutbound outbound) { 
		super.onOpen(outbound);
		if(name!=null){
			InitServlet.getSocketList().put(name, this);//存放客服ID与用户
		}
		String names = getNames();
		String content = MessageUtil.sendContent(MessageUtil.USER,names);
		broadcastAll(content);
	}
	
	private String getNames() {
		Map<String,MessageInbound> exitUser = InitServlet.getSocketList();
		Iterator<String> it=exitUser.keySet().iterator();
		String names = "";
		while(it.hasNext()){
			String key=it.next();
			names += key + ",";
		}
		String namesTemp = names.substring(0,names.length()-1);
		return namesTemp;
	}

	
	
	public static void broadcastAll(String message){
		Set<Map.Entry<String,MessageInbound>> set = InitServlet.getSocketList().entrySet();
		WsOutbound outbound = null;
		for(Map.Entry<String,MessageInbound> messageInbound: set){
			try {
				outbound = messageInbound.getValue().getWsOutbound();
				outbound.writeTextMessage(CharBuffer.wrap(message));
				outbound.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int getReadTimeout() {
		return 0;
	}  


}