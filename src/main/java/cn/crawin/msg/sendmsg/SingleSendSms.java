package cn.crawin.msg.sendmsg;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.crawin.msg.gateway.Constants;
import cn.crawin.msg.gateway.HttpSchema;
import cn.crawin.msg.gateway.Method;
import cn.crawin.msg.gateway.bean.Client;
import cn.crawin.msg.gateway.bean.Request;
import cn.crawin.msg.gateway.bean.Response;
import cn.crawin.msg.log.MsgLogger;


public class SingleSendSms {
	
	private MsgLogger log = MsgLogger.getLogger( this.getClass() );

	private final static String APP_KEY = ""; //AppKey从控制台获取
    private final static String APP_SECRET = ""; //AppSecret从控制台获取
    private final static String SIGN_NAME = ""; // 签名名称从控制台获取，必须是审核通过的
    //验证码模板
    private final static String TEMPLATE_CODE = ""; //模板CODE从控制台获取，必须是审核通过的
    
    private final static String HOST = "sms.market.alicloudapi.com"; //API域名从控制台获取

    private final static String ERRORKEY = "errorMessage";  //返回错误的key

    /**
     * 发送验证码
     * @param phoneNum 目标手机号，多个手机号可以逗号分隔;
     * @param params 短信模板中的变量，数字必须转换为字符串，如短信模板中变量为${no}",则参数params的值为{"no":"123456"}
     * @return
     */
    public boolean sendMsg(String phoneNum, String params){
    	return sendMsg1(phoneNum, params, TEMPLATE_CODE);
    }
    
    public boolean sendMsg1(String phoneNum, String params,String templateCode){
    	boolean result = false;
        String path = "/singleSendSms";

        Request request =  new Request(Method.GET, HttpSchema.HTTP + HOST, path, APP_KEY, APP_SECRET, Constants.DEFAULT_TIMEOUT);

        //请求的query
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("SignName", SIGN_NAME);
        querys.put("TemplateCode", templateCode);
        querys.put("RecNum", phoneNum);
        querys.put("ParamString", params);
        request.setQuerys(querys);

        try {
        	log.info("=======>>>  调用短信平台发送验证码信息"+HttpSchema.HTTP+path+"?ParamString="+params+"&RecNum="+
        phoneNum+"&SignName="+SIGN_NAME+"&TemplateCode="+templateCode);
            Map<String, String> bodymap = new HashMap<String, String>();
            Response response = Client.execute(request);
            //根据实际业务需要，调整对response的处理
            if (null == response) {
            	log.error("=======>>>  no response");
            } else if (200 != response.getStatusCode()) {
            	log.error("=======>>>  StatusCode:"+ response.getStatusCode()+"=======>>>  ErrorMessage:"+
            response.getErrorMessage()+"=======>>>  RequestId:"+response.getRequestId());
            	if( 403 == response.getStatusCode() ){
            		log.error("========>>>  短信账户余额不足，请尽快充值。");
            	}
            } else {
                bodymap = ReadResponseBodyContent(response.getBody());
                if (null != bodymap.get(ERRORKEY)) {
                    //当传入的参数不合法时，返回有错误说明
                	log.error(""+bodymap.get(ERRORKEY));
                } else {
                	result = true;
                    //成功返回map，对应的key分别为：message、success等
                	log.info("=======>>>  验证码发送成功"+JSON.toJSONString(bodymap));
                }
            }
        }catch (Exception e){
        	log.error("=======>>>  验证码发送失败，出现异常", e);
        }
        return result;
    }

    private Map<String, String> ReadResponseBodyContent(String body) {
        Map<String, String> map = new HashMap<String, String>();    
        try {
            JSONObject jsonObject = JSON.parseObject(body);
            if (null != jsonObject) {               
                for(Entry<String, Object> entry : jsonObject.entrySet()){
                    map.put(entry.getKey(), entry.getValue().toString());
                }               
            }
            if ("false".equals(map.get("success"))) {
                map.put(ERRORKEY, map.get("message"));
            }
        } catch (Exception e) {
            map.put(ERRORKEY, body);
        }
        return map;
    }

}
