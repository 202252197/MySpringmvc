package com.lsh.lvshihao.servlet;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.lsh.lvshihao.annoation.LshAutowired;
import com.lsh.lvshihao.annoation.LshController;
import com.lsh.lvshihao.annoation.LshRequestMapping;
import com.lsh.lvshihao.annoation.LshRequestParam;
import com.lsh.lvshihao.annoation.LshService;




public class DispatcherServlet extends HttpServlet{
	List<String> classNames=new ArrayList<String>();
	Map<String, Object> beans=new HashMap<String, Object>();
	Map<String, Method> handlerMap=new HashMap<String, Method>();
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		// �����е�beanɨ��-----ɨ�����е�class�ļ�
		scanPackage("com.lsh");//��ɨ�������·����ŵ�classNames��
		doInstance();//��classNames�е�����д���bean
		doIoc();//����bean��������ע��
		buildUrlMapping();
		
	}

	private void buildUrlMapping() {
		// TODO Auto-generated method stub
		Set<Entry<String, Object>> entrySet = beans.entrySet();
		if(entrySet.size()<=0) {
			System.out.println("û�����ʵ����.....");
			return;
		}
		for (Map.Entry<String,Object> entry : entrySet) {
			Object instance = entry.getValue();
			Class<? extends Object> clazz = instance.getClass();
			if(clazz.isAnnotationPresent(LshController.class)) {
				LshRequestMapping lshRequestMapping = clazz.getAnnotation(LshRequestMapping.class);
				String claspath = lshRequestMapping.value();
				Method[] methods = clazz.getMethods();
				for (Method method : methods) {
					if(method.isAnnotationPresent(LshRequestMapping.class)) {
						LshRequestMapping methodMapping = method.getAnnotation(LshRequestMapping.class);
						String methodPath = methodMapping.value();
						handlerMap.put(claspath+methodPath,method);
					}else {
						continue;
					}
				}
			}else {
				continue;
			}
		}
	}

	private void scanPackage(String basePackage) {
		// TODO Auto-generated method stub
		URL url=this.getClass().getClassLoader().getResource("/"+basePackage.replaceAll("\\.","/"));
		String fileStr=url.getFile(); //F:\sts\myspringmvc\src\main\java\com\lsh\
		File file=new File(fileStr); 
		String[] fiStrings=file.list();//F:\sts\myspringmvc\src\main\java\com\lsh\....
		for (String path : fiStrings) {
			File filePath=new File(fileStr+path);//F:\sts\myspringmvc\src\main\java\com\lsh\lvshihao
			if(filePath.isDirectory()) {    
				scanPackage(basePackage+"."+path); //F:\sts\myspringmvc\src\main\java\com\lsh\lvshihao
			}else {
				//���뵽list��
				classNames.add(basePackage+"."+filePath.getName());
			}
		}
	}
	@SuppressWarnings("deprecation")
	private void doInstance(){
		if(classNames.size()<=0) {
			System.out.println("��ɨ��ʧ��......");
			return;
		}
		//list������class��,����Щ�����ʵ��
		for (String cname : classNames) {
			String name = cname.replace(".class", "");
			Class<?> forName;
			try {
				forName = Class.forName(name);
				if(forName.isAnnotationPresent(LshController.class)) {
					Object instance = forName.newInstance();//����������
					LshRequestMapping lshRequestMapping = forName.getAnnotation(LshRequestMapping.class);
					String rmvalue = lshRequestMapping.value();//lvshihao
					beans.put(rmvalue, instance);
				}else if(forName.isAnnotationPresent(LshService.class)){
					LshService lshService = forName.getAnnotation(LshService.class);
					Object instance = forName.newInstance();
					beans.put(lshService.value(), instance);
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	//��service��ע�뵽controller��
	private void doIoc() {
		// TODO Auto-generated method stub
		if(beans.entrySet().size()<=0) {
			System.out.println("û��һ��ʵ������");
			return;
		}
		//��Map������ʵ������������
		for (Map.Entry<String, Object> entry:beans.entrySet()) {
			Object instance = entry.getValue();
			Class<? extends Object> clazz = instance.getClass();
			if(clazz.isAnnotationPresent(LshController.class)) {
				Field[] declaredFields = clazz.getDeclaredFields();
				for (Field field : declaredFields) {
					if(field.isAnnotationPresent(LshAutowired.class)) {
						LshAutowired lshAutowired = field.getAnnotation(LshAutowired.class);
						String key = lshAutowired.value();
						field.setAccessible(true);//����ֵ��Ȩ��
						try {
							field.set(instance,beans.get(key));
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//��ȡ����·�� /myspringmvc/lvshihao/query 
		String uri = req.getRequestURI();
		String contextPath = req.getContextPath();// myspringmvc
		String path = uri.replace(contextPath,"");//lvshihao/query 
		Method method=(Method)handlerMap.get(path);
		//����key=/lvshihao��mapȥ��
		com.lsh.lvshihao.controller.LshController instance = (com.lsh.lvshihao.controller.LshController) beans.get("/"+path.split("/")[1]);
		Object[] args = hand(req,resp,method);
		try {
			method.invoke(instance, args);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static Object[] hand(HttpServletRequest request,HttpServletResponse response,Method method) {
		//�õ���ǰִ�еķ�������Щ����
		Class<?>[] parameterTypes = method.getParameterTypes();
		//���ݲ����ĸ���,newһ������������,������������в�����ֵ��args��
		Object[] args=new Object[parameterTypes.length];
		int args_i=0;
		int index=0;
		for (Class<?> paramClazz : parameterTypes) {
			if(ServletRequest.class.isAssignableFrom(paramClazz)) {
				args[args_i++] =request;
			}
			if(ServletResponse.class.isAssignableFrom(paramClazz)) {
				args[args_i++] =response;
			}
			//��0-3�ж���û��RequestParamע��,������paramClazzΪ0��1ʱ,����
			//��2��3Ϊ@RequestParam,��Ҫ����
			Annotation[] paramAns = method.getParameterAnnotations()[index];
			if(paramAns.length>0) {
				for (Annotation paramAn : paramAns) {
					if(LshRequestParam.class.isAssignableFrom(paramAn.getClass())) {
						LshRequestParam rp=(LshRequestParam)paramAn;
						//�ҵ�ע�����name��age
						args[args_i++]=request.getParameter(rp.value());
					}
				}
			}
			index++;
		}
		return args;
	}

}
