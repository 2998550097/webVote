package com.hcq.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.hcq.utils.LogUtil;


public class MyProperties extends Properties{

	private static final long serialVersionUID = 8677195326257805172L;

	private static MyProperties myproperties;
	
	private static String propertyFileName="db.properties";
	
	private MyProperties(){
		//�����������һ���࣬��������ڴ�����·���µ�һЩ����
		InputStream stream=MyProperties.class.getClassLoader().getResourceAsStream(propertyFileName);
		try{
			load(stream);
		}catch(IOException e){
			LogUtil.logger.error("error to read properties file",e);
			throw new RuntimeException(e);
		}
	}
	//ȷ������
	//synchronized : �����̷߳���ʱ����֤һ��ֻ����һ����������������
	public synchronized static MyProperties getInstance(){
		if(myproperties==null){
			myproperties=new MyProperties();
		}
		return myproperties;
	}
	
}