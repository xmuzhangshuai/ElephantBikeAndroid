package com.xxn.elephantbike.config;

public class Constants {

	// 包名
	public static final String PACKAGENAME = "com.xxn.elephantbike";

	// 域名或者是IP(旧)
	// public static String AppliactionServerIP = "http://120.24.183.74";

	// public static String AppliactionServerDomain =
	// "http://120.24.183.74/index.php/";

	public static String AppliactionServerIP_Share = "http://www.theballer.cn";
	// 域名或者是IP
	public static String AppliactionServerIP = "http://120.25.197.43/";
	// public static String AppliactionServerIP = "http://210.121.164.111/";
//	 public static String AppliactionServerIP = "http://192.168.0.101:8080/";
	// public static String AppliactionServerDomain =
	// "http://www.theballer.cn/index.php/";
	public static String AppliactionServerDomain = "http://120.25.197.43/ElephantBike/";
	// public static String AppliactionServerDomain =
	// "http://210.121.164.111/ElephantBike/";
//	public static String AppliactionServerDomain = "http://192.168.0.101:8080/ElephantBike/";
	public static String SignKey = "dfeb3d35bc3543rdc234";

	public static class Config {
		// 是否处于开发模式
		public static final boolean DEVELOPER_MODE = true;

		// 接受验证码时间为120s
		public static int AUTN_CODE_TIME = 120;

		// 照片缩小比例
		public static final int SCALE = 5;

		// 总共有多少页
		public static final int NUM_PAGE = 6;

		// 每页20个表情,还有最后一个删除button
		public static int NUM = 20;

		public static int PAGE_NUM = 20;

		// 聊天每次刷新纪录条数
		public static int LOAD_MESSAGE_COUNT = 20;
	}

	/**
	 * -1:AUTH_FROZEN冻结 0:AUTH_REG注册 1;AUTH_NEED_SUBMIT认证 2
	 * :AUTH_NEED_CONFIRM提交未审核
	 * 
	 * @ClassName: AUTH
	 * @Description: TODO
	 * @author: lzjing
	 * @date: 2016年4月4日 下午6:54:15
	 */
	public static class AUTH {
		// -1:冻结 0注册 1认证 2 提交未审核
		// 冻结
		public static String AUTH_FROZEN = "-1";
		// 注册
		public static String AUTH_REG = "0";
		// 认证
		public static String AUTH_NEED_SUBMIT = "1";
		// 提交未审核
		public static String AUTH_NEED_CONFIRM = "2";
	}

	public static class WeChatConfig {
		public final static String APP_ID = "wx4a480f3f5a6c4c6c";// 大象单车APPID=wx4a480f3f5a6c4c6c
		public final static String APP_SECRET = "6809df36797cf90e310833560d3d2c62";// 大象单车AppSecret
	}

	public static class WeiboConfig {
		public final static String API_KEY = "4169059323";
		public final static String SECRIT_KEY = "9825238066521372a2f776fa08b651e2";

		// public final static String API_KEY = "1143456865";
		// public final static String SECRIT_KEY =
		// "b28fbfc22b29b0e53424b8c2fb283c2e";
	}

	public static class QQConfig {
		public final static String API_KEY = "1104862406";
		public final static String SECRIT_KEY = "VHSnIwPNZJcnMqj7";
		// public final static String API_KEY = "1104913890";
		// public final static String SECRIT_KEY = "9qIl3e9wgCQSgNbL";
	}

	public static class Extra {
		public static final String IMAGES = "com.nostra13.example.universalimageloader.IMAGES";
		public static final String IMAGE_POSITION = "com.nostra13.example.universalimageloader.IMAGE_POSITION";
	}

}
