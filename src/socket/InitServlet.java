package socket;

import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.catalina.websocket.MessageInbound;
@SuppressWarnings("deprecation")
public class InitServlet extends HttpServlet {

    private static final long serialVersionUID = -3163557381361759907L;  
    

	private static HashMap<String,MessageInbound> socketList;    
      
    public void init(ServletConfig config) throws ServletException {    
        InitServlet.socketList = new HashMap<String,MessageInbound>();    
        super.init(config);    
        System.out.println("聊天系统启动");    
    }    
        
    public static HashMap<String,MessageInbound> getSocketList() {    
        return InitServlet.socketList;    
    }    
}