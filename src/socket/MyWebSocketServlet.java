package socket;

import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
@SuppressWarnings({ "serial", "deprecation" })
public class MyWebSocketServlet extends WebSocketServlet {

	public String getUser(HttpServletRequest request){
		String userName = (String) request.getSession().getAttribute("user");
		if(userName==null){
			return null;
		}
		return userName;  
		
	}  
	protected StreamInbound createWebSocketInbound(String arg0,
			HttpServletRequest request) {
		System.out.println("用户" + request.getSession().getAttribute("user") + "登录");
		return new MyMessageInbound(this.getUser(request));
	}
}