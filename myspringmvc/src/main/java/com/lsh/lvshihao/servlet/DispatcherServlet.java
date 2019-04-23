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
		// 把所有的bean扫描-----扫描所有的class文件
		scanPackage("com.lsh");//将扫描的所有路径存放到classNames中
		doInstance();//将classNames中的类进行创建bean
		doIoc();//根据bean进行依赖注入
		buildUrlMapping();
		
	}

	private void buildUrlMapping() {
		// TODO Auto-generated method stub
		Set<Entry<String, Object>> entrySet = beans.entrySet();
		if(entrySet.size()<=0) {
			System.out.println("没有类的实例化.....");
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
				//加入到list中
				classNames.add(basePackage+"."+filePath.getName());
			}
		}
	}
	@SuppressWarnings("deprecation")
	private void doInstance(){
		if(classNames.size()<=0) {
			System.out.println("包扫描失败......");
			return;
		}
		//list的所有class类,对这些类进行实例
		for (String cname : classNames) {
			String name = cname.replace(".class", "");
			Class<?> forName;
			try {
				forName = Class.forName(name);
				if(forName.isAnnotationPresent(LshController.class)) {
					Object instance = forName.newInstance();//创建控制类
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
	//将service层注入到controller层
	private void doIoc() {
		// TODO Auto-generated method stub
		if(beans.entrySet().size()<=0) {
			System.out.println("没有一个实例化类");
			return;
		}
		//把Map里所有实例化遍历出来
		for (Map.Entry<String, Object> entry:beans.entrySet()) {
			Object instance = entry.getValue();
			Class<? extends Object> clazz = instance.getClass();
			if(clazz.isAnnotationPresent(LshController.class)) {
				Field[] declaredFields = clazz.getDeclaredFields();
				for (Field field : declaredFields) {
					if(field.isAnnotationPresent(LshAutowired.class)) {
						LshAutowired lshAutowired = field.getAnnotation(LshAutowired.class);
						String key = lshAutowired.value();
						field.setAccessible(true);//打开设值的权限
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
		//获取请求路径 /myspringmvc/lvshihao/query 
		String uri = req.getRequestURI();
		String contextPath = req.getContextPath();// myspringmvc
		String path = uri.replace(contextPath,"");//lvshihao/query 
		Method method=(Method)handlerMap.get(path);
		//根据key=/lvshihao到map去拿
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
		//拿到当前执行的方法有哪些参数
		Class<?>[] parameterTypes = method.getParameterTypes();
		//根据参数的个数,new一个参数的数组,将方法里的所有参数赋值到args来
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
			//从0-3判断有没有RequestParam注解,很明显paramClazz为0和1时,不是
			//当2和3为@RequestParam,需要解析
			Annotation[] paramAns = method.getParameterAnnotations()[index];
			if(paramAns.length>0) {
				for (Annotation paramAn : paramAns) {
					if(LshRequestParam.class.isAssignableFrom(paramAn.getClass())) {
						LshRequestParam rp=(LshRequestParam)paramAn;
						//找到注解里的name和age
						args[args_i++]=request.getParameter(rp.value());
					}
				}
			}
			index++;
		}
		return args;
	}

}
