package socket;

import util.MessageUtil;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint(value = "/websocket", configurator = GetHttpSessionConfigurator.class)
public class ChatServlet {

	private static final Map<HttpSession, ChatServlet> onlineUsers = new HashMap<>();

	private static int onlineCount = 0;

	private HttpSession httpSession;

	private Session wsSession;

	private String name;


	@OnOpen
	public void onOpen(Session wsSession, EndpointConfig config) {
		this.wsSession = wsSession;
		this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
		if (httpSession.getAttribute("user") != null) {
			onlineUsers.put(httpSession, this);
		}
		String names = getNames();
		String content = MessageUtil.sendContent(MessageUtil.USER, names);
		broadcastAll(content);
		addOnlineCount();           //在线数加1
		System.out.println("有新连接加入!当前在线人数为" + onlineUsers.size());
	}

	@OnError
	public void onError(Session wsSession, Throwable error) {
		System.out.println("发生错误");
		error.printStackTrace();
	}

	// TODO: 2018/6/12 0012
	private void offline(Session wsSession) {

	}

	@OnMessage
	public void onTextMessage(String msg, Session wsSession) {
		HashMap<String, String> messageMap = MessageUtil.getMessage(msg);
		String fromName = messageMap.get("fromName");    //消息来自人 的userId
		String toName = messageMap.get("toName");       //消息发往人的 userId
		String mapContent = messageMap.get("content");

		if ("all".equals(toName)) {
			String msgContentString = fromName + "对所有人说: " + mapContent;
			String content = MessageUtil.sendContent(MessageUtil.MESSAGE, msgContentString);
			broadcastAll(content);
		} else {
			try {
				singleChat(fromName, toName, mapContent);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@OnClose
	public void onClose() {
		if (onlineUsers.containsKey(this.httpSession)) {
			onlineUsers.remove(this.httpSession);
		}
		subOnlineCount();           //在线数减1
		System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
	}


	private void singleChat(String fromName, String toName, String mapContent) throws IOException {
		String msgContentString = fromName + "对" + toName + "说: " + mapContent;
		String contentTemp = MessageUtil.sendContent(MessageUtil.MESSAGE, msgContentString);
		boolean isExit = false;
		System.out.println(contentTemp);
		for (HttpSession key : onlineUsers.keySet()) {
			if (key.getAttribute("user").equals(toName)) {
				isExit = true;
			}
		}
		if (isExit) {
			for (HttpSession key : onlineUsers.keySet()) {
				if (key.getAttribute("user").equals(fromName) || key.getAttribute("user").equals(toName)) {
					onlineUsers.get(key).wsSession.getBasicRemote().sendText(contentTemp);
				}
			}
		} else {
			String content = MessageUtil.sendContent(MessageUtil.MESSAGE, "客服不在线请留言...");
			broadcastAll(content);
		}
	}


	private String getNames() {
		String names = "";
		for (HttpSession key : onlineUsers.keySet()) {
			String name = (String) key.getAttribute("user");
			names += name + ",";
		}
		String namesTemp = names.substring(0, names.length() - 1);
		return namesTemp;
	}


	/**
	 * 广播
	 * @param message
	 */
	private static void broadcastAll(String message) {
		for (HttpSession key : onlineUsers.keySet()) {
			try {
				onlineUsers.get(key).wsSession.getBasicRemote().sendText(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static synchronized int getOnlineCount() {
		return onlineCount;
	}

	public static synchronized void addOnlineCount() {
		ChatServlet.onlineCount++;
	}

	public static synchronized void subOnlineCount() {
		ChatServlet.onlineCount--;
	}


}