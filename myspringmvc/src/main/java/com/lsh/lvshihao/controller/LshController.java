package com.lsh.lvshihao.controller;

import java.io.PrintWriter;
import java.net.http.HttpResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lsh.lvshihao.annoation.LshAutowired;
import com.lsh.lvshihao.annoation.LshRequestMapping;
import com.lsh.lvshihao.annoation.LshRequestParam;
import com.lsh.lvshihao.service.LshService;

@com.lsh.lvshihao.annoation.LshController
@LshRequestMapping("/lvshihao")
public class LshController {
	@LshAutowired("LshServiceImpl")
	private LshService lshService;
	
	@LshRequestMapping("/query")
	public void query(HttpServletRequest request,HttpServletResponse response,
			@LshRequestParam("name") String name,@LshRequestParam("age") String age) {
		try {
			PrintWriter writer = response.getWriter();
			String query = lshService.query(name, age);
			writer.println(query);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
