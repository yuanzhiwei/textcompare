package com.yuanzhiwei.textcompare;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.yuanzhiwei.textcompare.Diff_match_patch.Diff;
import com.yuanzhiwei.textcompare.Diff_match_patch.Operation;

/**
 * 字符串操作工具类
 * 
 * @author a
 *
 */
public class StringUtil {
	
	public static String textCompare(String source, String target){
		return textCompare(source, target, true);
	}
	/**
	 * 传入修改前后的文本 ， 返回修改记录富文本
	 * @param source
	 * @param target
	 * @return
	 */
	public static String textCompare1(String source, String target) {
		source = source.replaceAll("<.*?>", "")
				.replaceAll("&nbsp;", "");
		target = target.replaceAll("<.*?>", "")
				.replaceAll("&nbsp;", "");
		int slen = source.length();
		int tlen = target.length();
		int[][] h = new int[slen + 1][tlen + 1];
		for (int i = 0; i < 1; i++) {
			for (int j = 0; j < tlen + 1; j++) {
				h[i][j] = 0;
			}
		}
		for (int i = 1; i < slen + 1; i++) {
			for (int j = 1; j < tlen + 1; j++) {
				if (source.charAt(i - 1) == target.charAt(j - 1)) {
					h[i][j] = h[i - 1][j - 1] + 1;
				} else {
					h[i][j] = maximum(h[i - 1][j], h[i - 1][j - 1], h[i][j - 1]);
				}
			}
		}

		String result = getBack(source, target, h);
//		// 输出
//		System.out.println("score matrix:");
//		for (int i = 0; i < slen + 1; i++) {
//			for (int j = 0; j < tlen + 1; j++) {
//				System.out.printf("%4d", h[i][j]);
//				if (j != 0 && j % tlen == 0) {
//					System.out.println();
//				}
//			}
//		}

		return result.toString();
	}

	public static int maximum(int a, int b, int c) {
		int max = a;
		if (b > max) {
			max = b;
		}
		if (c > max) {
			max = c;
		}
		return max;
	}

	public static String getBack(String source, String target, int[][] d) {
		if(source.equals(target)){
			return source;
		}
//		System.out.println(source);
//		System.out.println(target);
		int i = source.length();
		int j = target.length();
		StringBuffer s = new StringBuffer();
		StringBuffer t = new StringBuffer();
		if (i == 0) {
			s.insert(0, StringUtils.repeat("-", j));
			t.insert(0, target);
		} else if (j == 0) {
			s.insert(0, source);
			t.insert(0, StringUtils.repeat("-", i));
		} else {
			while (i > 0 || j > 0) {
				if (i == 0) {
					s.insert(0, StringUtils.repeat("-", j));
					t.insert(0, target.substring(0, j));
					break;
				}
				if (j == 0) {
					s.insert(0, source.substring(0, i));
					t.insert(0, StringUtils.repeat("-", i));
					break;
				}
				if (source.charAt(i - 1) == target.charAt(j - 1) && d[i - 1][j - 1] == d[i - 1][j] && d[i - 1][j] == d[i][j - 1] && d[i][j] > d[i-1][j-1]) {
					i -= 1;
					j -= 1;
					s.insert(0, source.charAt(i));
					t.insert(0, target.charAt(j));
				} else {
					/*if(d[i][j] == d[i-1][j-1] && d[i - 1][j - 1] == d[i - 1][j] && d[i - 1][j] == d[i][j - 1]){
						i -= 1;
						j -= 1;
						s.insert(0, '-');
						t.insert(0, target.charAt(j));
						continue;
					}*/
					int[] temp = new int[] { d[i - 1][j - 1], d[i - 1][j], d[i][j - 1] }; // 优先级按照左上角、上边、左边
					int max = maximum(d[i - 1][j - 1], d[i - 1][j], d[i][j - 1]);
					int index = 0;
					for (int m = 0; m < temp.length; m++) {
						if (max == temp[m]) {
							index = m;
							break;
						}
					}
					switch (index) {
					case 0:
						i -= 1;
						j -= 1;
						s.insert(0, source.charAt(i));
						t.insert(0, target.charAt(j));
						break;
					case 1:
						i -= 1;
						s.insert(0, source.charAt(i));
						t.insert(0, '-');
						break;
					case 2:
						j -= 1;
						s.insert(0, '-');
						t.insert(0, target.charAt(j));
					default:
						break;
					}
				}
			}
		}
		String s1 = s.toString();
		String s2 = t.toString();
//		System.out.println(s1);
//		System.out.println(s2);
		StringBuffer sb = new StringBuffer();
		// 需要标记的序列
		int begin = -1;
		int end = -1;
		String type = "delete"; // other : insert、 modify
		for (int n = 0; n < s1.length(); n++) {
			if (s2.charAt(n) == '-') { // 删除
				// 判断是否有其他类型的操作未添加到结果集
				if (!type.equals("delete") && begin != -1) {
					if (type.equals("normal")) {
						sb.append(s1.substring(begin, end + 1));
					} else if (type.equals("edit")){
						if(begin >= 1 && s1.charAt(begin - 1) == '-'){
							sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
							sb.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(s1.substring(begin, end + 1)).append("</span>");
						}else{
							sb.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(s1.substring(begin, end + 1)).append("</span>");
							sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
						}
					} else {
						sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
					}
					begin = -1;
					end = -1;
				}
				if (begin == -1) { // 开始计算删除子串
					type = "delete";
					begin = n;
				}
				end = n;
			} else { // 添加 、 修改、 相同
				// 判断是否有其他类型的操作未添加到结果集
				if (type.equals("delete") && begin != -1) {
					sb.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(s1.substring(begin, end + 1)).append("</span>");
					begin = -1;
					end = -1;
				}
				if (s1.charAt(n) == '-' || s1.charAt(n) != s2.charAt(n)) { // 添加、编辑
					if (type.equals("normal") && begin != -1) { // 相同结束
						sb.append(s1.substring(begin, end + 1));
						begin = -1;
						end = -1;
					}
					if (s1.charAt(n) == '-') {
						if(begin == -1){
							type = "add";
							begin = n;
						}else{
							if(!type.equals("add")){
								if(begin >= 1 && s1.charAt(begin - 1) == '-'){
									sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
									sb.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(s1.substring(begin, end + 1)).append("</span>");
								}else{
									sb.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(s1.substring(begin, end + 1)).append("</span>");
									sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
								}
								type="add";
								begin = n;
							}
						}
						
					}else if (s1.charAt(n) != s2.charAt(n)){
						if(begin == -1){
							type = "edit";
							begin = n;
						}else{
							if(!type.equals("edit")){
								sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
								type="edit";
								begin = n;
							}
						}
						
					}
					end = n;
				}
				if (s1.charAt(n) == s2.charAt(n)) {
					if (type.equals("add") && begin != -1) { // 新增结束
						sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
						begin = -1;
						end = -1;
					} else if (type.equals("edit") && begin != -1){ // 编辑结束
						if(begin >= 1 && s1.charAt(begin - 1) == '-'){
							sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
							sb.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(s1.substring(begin, end + 1)).append("</span>");
						}else{
							sb.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(s1.substring(begin, end + 1)).append("</span>");
							sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
						}
						begin = -1;
						end = -1;
					}
					if (begin == -1) {
						type = "normal";
						begin = n;
					}
					end = n;
				}
			}
		}

		switch (type) {
		case "normal":
			sb.append(s1.substring(begin, end + 1));
			break;
		case "delete":
			sb.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(s1.substring(begin, end + 1)).append("</span>");
			break;
		case "edit":
			if(begin >= 1 && s1.charAt(begin - 1) == '-'){
				sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
				sb.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(s1.substring(begin, end + 1)).append("</span>");
			}else{
				sb.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(s1.substring(begin, end + 1)).append("</span>");
				sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
			}
			break;
		case "add":
			sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
			break;
		default:
			break;
		}
		return sb.toString();
	}
	
	public static String textCompare(String source, String target, boolean flag) {
		if(flag){
			List<String> text = Arrays.asList(source, target); 
			Diff_match_patch dmp = new Diff_match_patch(); 
		    LinkedList<Diff> linkedList = dmp.diff_main(source,target);
		    StringBuffer sb1 = new StringBuffer();
		    StringBuffer sb2 = new StringBuffer();
		    for (int a = 0; a < linkedList.size(); a++) {
		    	Diff diff = linkedList.get(a);
		    	if(StringUtils.isBlank(diff.text)){
		    		continue;
		    	}
		    	if(diff.operation == Operation.EQUAL){
		    		sb1.append(diff.text);
		    		sb2.append(diff.text);
		    	}else if(diff.operation == Operation.INSERT){
		    		if(diff.text.indexOf("∷") == -1){
		    			int point = appearNumber(sb2.toString(), diff.text);
		    			if(point == 0){
		    				text.set(1, text.get(1).replaceFirst(diff.text, "<span style='color:red'>"+diff.text+"</span>"));
		    			}else{
		    				text.set(1, replace(text.get(1), diff.text, "<span style='color:red'>"+diff.text+"</span>", point + 1));
		    			}
		    			sb2.append(diff.text);
		    		}else{
		    			String[] split = diff.text.split("∷");
		    			for (int i = 0; i < split.length; i++) {
		    				if(StringUtils.isBlank(split[i]))
		    					continue;
		    				int point = appearNumber(sb2.toString(), split[i]);
		    				if(point == 0){
			    				text.set(1, text.get(1).replaceFirst(split[i], "<span style='color:red'>"+split[i]+"</span>"));
			    			}else{
			    				text.set(1, replace(text.get(1), split[i], "<span style='color:red'>"+split[i]+"</span>", point + 1));
			    			}
		    				sb2.append(split[i]+"∷");
						}
		    		}
		    	}else if(diff.operation == Operation.DELETE){
		    		Diff last = null;
			    	Diff next = null;
		    		if(a>0){
		    			last = linkedList.get(a-1);
		    			if(last.text.equals("∷")){
		    				last = null;
		    			}
		    		}
		    		if(a<linkedList.size()-1){
		    			next = linkedList.get(a+1);
		    			if(next.text.equals("∷")){
		    				next = null;
		    			}
		    		}
		    		StringBuffer str = new StringBuffer();
	    			String prefix = "";
	    			String suffix = "";
	    			if(last != null){
	    				String[] split = last.text.split("∷");
	    				prefix  = split[split.length-1];
	    			}
	    			//str.append(diff.text);
	    			if(next != null){
	    				String[] split = next.text.split("∷");
	    				suffix = split[0];
	    			}
	    			String newStr = str.append(prefix).append(suffix).toString();
	    			str.setLength(0);
		    		//if(diff.text.indexOf("∷") == -1){
		    			if(StringUtils.isNotBlank(newStr)){
		    				int point = appearNumber(sb2.toString(), newStr);
		    				if(point == 0){
			    				text.set(1, text.get(1).replaceFirst(newStr, str.append(prefix).append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(diff.text.replace("∷", "")).append("</span>").append(suffix).toString()));
			    			}else{
			    				text.set(1, replace(text.get(1), newStr, str.append(prefix).append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(diff.text.replace("∷", "")).append("</span>").append(suffix).toString(), point));
			    			}
		    			}else{
		    				// 删除文本同标签内无其他有效内容，将删除内容直接加入到上一标签末尾或下一标签开头
		    				boolean[] isDown = new boolean[]{false};
		    				if(last != null){
		    					String[] split = reverse(last.text.split("∷"));
		    					Arrays.stream(split).forEach(s->{
		    						if(StringUtils.isNotBlank(s) && !isDown[0]){
		    							str.setLength(0);
		    							int point = appearNumber(sb2.toString(), s);
		    							if(point == 0){
		    			    				text.set(1, text.get(1).replaceFirst(s, str.append(s).append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(diff.text.replace("∷", "")).append("</span>").toString()));
		    			    			}else{
		    			    				text.set(1, replace(text.get(1), s, str.append(s).append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(diff.text.replace("∷", "")).append("</span>").toString(), point+1));
		    			    			}
		    							isDown[0] = true;
		    						}
		    					});
		    				}
		    				if(next != null && !isDown[0]){
		    					String nextStr = next.text;
		    					String[] split = nextStr.split("∷");
		    					Arrays.stream(split).forEach(s->{
		    						if(StringUtils.isNotBlank(s) && !isDown[0]){
		    							str.setLength(0);
		    							int point = appearNumber(sb2.toString()+nextStr, s);
		    							if(point == 0){
		    			    				text.set(1, text.get(1).replaceFirst(s, str.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(diff.text.replace("∷", "")).append("</span>").append(s).toString()));
		    			    			}else{
		    			    				text.set(1, replace(text.get(1), s, str.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(diff.text.replace("∷", "")).append("</span>").append(s).toString(), point));
		    			    			}
		    							isDown[0] = true;
		    						}
		    					});
		    				}
		    				if(!isDown[0]){
		    					text.set(1, str.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(diff.text.replace("∷", "")).append("</span>").toString());
		    				}
		    			}
		    			sb2.append("∷").append(diff.text.replace("∷", "")).append("∷");
		    			/*}else{
		    			if(last == null && next == null){
		    				text.set(1, str.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(diff.text).append("</span>").toString());
		    			}else if(last == null){
		    				text.set(1, str.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(diff.text).append("</span>").append(text.get(1)).toString());
		    			}else if(next == null){
		    				text.set(1, str.append(text.get(1)).append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(diff.text).append("</span>").toString());
		    			}else{
		    				String[] split = diff.text.split("∷");
		    				
		    				String[] array = reverse(last.text.split("∷"));
	    					Arrays.stream(split).forEach(s->{
	    						str.setLength(0);
	    						if(StringUtils.isNotBlank(s)){
	    							int point = appearNumber(sb2.toString(), s);
	    							if(point == 0){
	    			    				text.set(1, text.get(1).replaceFirst(s, str.append(s).append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(split[0]).append("</span>").toString()));
	    			    			}else{
	    			    				text.set(1, replace(text.get(1), s, str.append(s).append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(split[0]).append("</span>").toString(), point + 1));
	    			    			}
	    							return;
	    						}
	    					});
		    			}
		    		}*/
		    	}
		    }
		    	
		    return text.get(1);
		}else{
			return textCompare1(source, target);
		}
		
	}
	public String getMatchString(String source, String line, int index) {
	   List<String> strs = new ArrayList<String>();
	   Pattern p = Pattern.compile("<.*?>"+line+"</.*?>");
	   Matcher m = p.matcher(source);
	   while(m.find()) {
	     strs.add(m.group());
	   }
	   return strs.get(index);
	}
	
	private static String[] reverse(String[] Array) {
		String[] new_array = new String[Array.length];
		for (int i = 0; i < Array.length; i++) {
			new_array[i] = Array[Array.length - i - 1];
		}
		return new_array;
	}
	
	public static String replace(String text, String oldStr, String newStr, int index){
		Pattern pattern = Pattern.compile(oldStr);
        Matcher findMatcher = pattern.matcher(text);
        int number = 0;  
        while(findMatcher.find()) {  
            number++;  
           if(number == index){
              break;  
           }  
        }  
        int i = findMatcher.start();
        StringBuffer sb = new StringBuffer(text.substring(0, i));
        String suffix = text.substring(i);
        return sb.append(suffix.replaceFirst(oldStr, newStr)).toString();
	}

	/**
	 * 获取指定字符串出现的次数
	 * 
	 * @param srcText 源字符串
	 * @param findText 要查找的字符串
	 * @return
	 */
	public static int appearNumber(String srcText, String findText) {
	    int count = 0;
	    Pattern p = Pattern.compile(findText);
	    Matcher m = p.matcher(srcText);
	    while (m.find()) {
	        count++;
	    }
	    return count;
	}
	
	public static void main(String[] args) {
		String source = "<p><span style=\"font-size:20px;font-family:方正仿宋_GBK\">【<strong>概况</strong>】涪陵区地方税务局在职职工274名。设10个机关科室，1个稽查局，1个办税服务厅，9个基层税务所。2014年，全局上下紧紧围绕推进税收现代化和建设“三区一城、幸福涪陵”的总体目标，以深化税收征管改革为重点，以党的群众路线教育实践活动为依托，推动各项工作顺利开展。</span></p>";
		String target = "<p><span style=\"font-size:20px;font-family:方正仿宋_GBK\">涪陵区地方税务局在职职工274名。设10个机关科室，1个稽查局，1个办税服务厅，9个基层税务所。2014年，全局上下紧紧围绕推进税收现代化和建设“三区一城、幸福涪陵”的总体目标，以深化税收征管改革为重点，以党的群众路线教育实践活动为依托，推动各项工作顺利开展。</span></p><p><span style=\"font-family:方正仿宋_GBK\"><span style=\"font-size: 20px;\">税费收入完成情况</span></span></p>";
//		String source = "<p style=\"text-align:center;line-height:37px\"><span style=\"font-size:29px;font-family:方正小标宋_GBK\">涪陵区地方税务局</span></p><p style=\"line-height:37px\"><span style=\"font-size:20px;font-family:方正仿宋_GBK\">&nbsp;&nbsp;&nbsp; </span></p><p style=\"line-height:37px\"><span style=\"font-size:20px;font-family:方正仿宋_GBK\">&nbsp;&nbsp;&nbsp; </span><span style=\"font-size:20px;font-family:方正仿宋_GBK\">【<strong>概况</strong>】涪陵区地方税务局在职职工274名。设10个机关科室，1个稽查局，1个办税服务厅，9个基层税务所。2014年，全局上下紧紧围绕推进税收现代化和建设“三区一城、幸福涪陵”的总体目标，以深化税收征管改革为重点，以党的群众路线教育实践活动为依托，推动各项工作顺利开展。</span></p><p><br/></p>";
//		String target = "<p style=\"text-align:center;line-height:37px\"><span style=\"font-size:29px;font-family:方正小标宋_GBK\">涪陵区地方税务局</span></p><p style=\"line-height:37px\"><span style=\"font-size:20px;font-family:方正仿宋_GBK\">&nbsp;&nbsp;&nbsp; </span></p><p style=\"line-height:37px\"><span style=\"font-size:20px;font-family:方正仿宋_GBK\">&nbsp;&nbsp;&nbsp; </span><span style=\"font-size:20px;font-family:方正仿宋_GBK\">【<strong>概况</strong>】涪陵区地方税务局在职职工274名。设10个机关科室，1个稽查局，1个办税服务厅，9个基层税务所。2014年，全局上下紧紧围绕推进税收现代化和建设“三区一城、幸福涪陵”的总体目标，</span></p><p style=\"line-height:37px\"><span style=\"font-family:方正仿宋_GBK\"><span style=\"font-size: 20px;\">共建美好家园，建设社会主义新农村</span></span></p><p><br/></p>";
		System.out.println(textCompare(source, target,true));
//		System.out.println(textCompare(source, target));
		// sssssabcdedfgggssasaasasssss--sssaassassafddd-sssss
		// -----a------aaaaaaaaaaasssssdfsss--ssaaaaadddaaaaaa
		/*
		 * <s>sssss</s>a<s>bcdedf</s> <span class='text-red'>aaaaa</span>a<span
		 * class='text-red'>a</span>aa<span class='text-red'>a</span>asssss
		 * <span class='text-red'>df</span>sss<s>aa</s>ssa<span
		 * class='text-red'>aa</span>a<span class='text-red'>a</span>ddd<span
		 * class='text-red'>aaaaaa</span>
		 */
		
//		Diff_match_patch dmp = new Diff_match_patch(); 
//	    System.out.println(dmp.getHtmlDiffString(source,target));
		
//		System.out.println(replace("你在哪儿啊，你在干什么啊，你在我责任田吗","你在","hhhh",2));
		
//		int[] num=new int[]{1,2,3,4,5};
//        String str="";
//        count(0,str,num,3);
		
	}
	
	// 动态规划
	private static void count(int i, String str, int[] num,int n) {
        if(n==0){
            System.out.println(str);
            return;
        }
        if(i==num.length){
            return;
        }
        count(i+1,str+num[i]+",",num,n-1);
        count(i+1,str,num,n);
    }
}