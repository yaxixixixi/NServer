package com.personal.nserver;

import com.google.gson.Gson;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
	// write your code here


        ArrayList<String> o = new ArrayList<>();
        o.add("sss");
        o.add("hhh");
        o.add("ggg");
        o.add("fff");

//        o.forEach(i -> {if (i.equals("sss")){o.remove(i);}});
        o.removeIf(i ->i.equals("sss"));
        System.out.println(o.size());
        ChannelInfo channelInfo = new ChannelInfo();
        channelInfo.setPhoneModel("xinghaoxxxxxxxxxxxxx");
        channelInfo.setSerialNumber("chuanmaxxxxxxxxxxxxxx");
        channelInfo.setJobNember("gonghaoxxxxxxxxxxxxx");
        Gson gson = new Gson();
        String x = gson.toJson(channelInfo);
        System.out.println(x);


        ChannelInfo channelInfo1 = gson.fromJson(x, ChannelInfo.class);
        System.out.println("sss");

    }
}
