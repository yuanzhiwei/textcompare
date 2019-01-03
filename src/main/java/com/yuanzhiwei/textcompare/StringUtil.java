package com.yuanzhiwei.textcompare;

import org.apache.commons.lang.StringUtils;

/**
 * 字符串操作工具类
 * 
 * @author yuanzhiwei
 *
 */
public class StringUtil {
	/**
	 * 传入修改前后的文本 ， 返回修改记录富文本
	 * @param source
	 * @param target
	 * @return
	 */
	public static String textCompare(String source, String target) {
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
		// 输出
		
		/*System.out.println("score matrix:");
		for (int i = 0; i < slen + 1; i++) {
			for (int j = 0; j < tlen + 1; j++) {
				System.out.printf("%4d", h[i][j]);
				if (j != 0 && j % tlen == 0) {
					System.out.println();
				}
			}
		}*/

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
				if (source.charAt(i - 1) == target.charAt(j - 1)) {
					i -= 1;
					j -= 1;
					s.insert(0, source.charAt(i));
					t.insert(0, target.charAt(j));
				} else {
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
						sb.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(s1.substring(begin, end + 1)).append("</span>");
						sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
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
								sb.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(s1.substring(begin, end + 1)).append("</span>");
								sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
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
						sb.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(s1.substring(begin, end + 1)).append("</span>");
						sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
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
			sb.append("<span style='text-decoration: line-through;background-color: rgb(216, 216, 216);'>").append(s1.substring(begin, end + 1)).append("</span>");
			sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
			break;
		case "add":
			sb.append("<span style='color:red'>").append(s2.substring(begin, end + 1)).append("</span>");
			break;
		default:
			break;
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		String source = "英文123";
		String target = "中英文145";
		System.out.println(textCompare(source, target));
		// sssssabcdedfgggssasaasasssss--sssaassassafddd-sssss
		// -----a------aaaaaaaaaaasssssdfsss--ssaaaaadddaaaaaa
		/*
		 * <s>sssss</s>a<s>bcdedf</s> <span class='text-red'>aaaaa</span>a<span
		 * class='text-red'>a</span>aa<span class='text-red'>a</span>asssss
		 * <span class='text-red'>df</span>sss<s>aa</s>ssa<span
		 * class='text-red'>aa</span>a<span class='text-red'>a</span>ddd<span
		 * class='text-red'>aaaaaa</span>
		 */
	}
}