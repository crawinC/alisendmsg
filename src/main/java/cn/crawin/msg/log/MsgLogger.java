package cn.crawin.msg.log;

import org.apache.log4j.Logger;

public class MsgLogger {
	
	public static MsgLogger getLogger(Class c){
		return new MsgLogger(c);
	}
	
	private Logger log = null;
	
	private MsgLogger(Class c){
		log = Logger.getLogger(c);
	}
	
	public void info(Object message){
		log.info(message);
	}
	
	public void error(Object message){
		log.error(message);
	}
	
	public void error(Object message,Throwable t){
		log.error(message,t);
	}

}
