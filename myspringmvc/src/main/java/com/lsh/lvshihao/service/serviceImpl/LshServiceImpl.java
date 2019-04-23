package com.lsh.lvshihao.service.serviceImpl;

import com.lsh.lvshihao.service.LshService;

@com.lsh.lvshihao.annoation.LshService("LshServiceImpl")
public class LshServiceImpl implements LshService{

	public String query(String name, String age) {
		// TODO Auto-generated method stub
		return "name==="+name+";age==="+age;
	}

}
