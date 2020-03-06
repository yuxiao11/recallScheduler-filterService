package com.ifeng.recallScheduler.utils;

import com.beust.jcommander.internal.Lists;
import com.ifeng.recallScheduler.bean.LastDocBean;


import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * List处理的工具类
 * 
 * @author jiangmm
 *
 */
public class ListUtil {

	/**
	 * 将多个list合并到一个list中
	 * 
	 * @param lists
	 * @return
	 */
	public static <E> List<E> unionLists(List<E>... lists) {
		List<E> result = new ArrayList<E>();

		for (List<E> list : lists) {
			if (list != null) {
				result.addAll(list);
			}
		}
		return result;
	}

	
	/**
	 * list1 U list2 
	 * @param list1
	 * @param list2
	 * @return list1
	 */
	public static <T> List<T> getUnion(List<T> list1, List<T> list2) {
		if (list1 == null && list2 == null) {
			return null;
		} else if (list1 != null && list2 == null) {
			return list1;
		} else if (list2 != null && list1 == null) {
			return list2;
		} else {
			list1.removeAll(list2);
			list1.addAll(list2);
			return list1;
		}
	}

	/**
	 * list1-list2
	 * @param list1
	 * @param list2
	 * @return list1
	 */
	public static <T> List<T> getSubtract(List<T> list1, List<T> list2) {
		if (list1 == null) {
			return null;
		} else if (list2 == null) {
			return list1;
		}

		list1.removeAll(list2);

		return list1;
	}
	
	/**
	 * list1交List2
	 * @param list1
	 * @param list2
	 * @return list1
	 */
	public static <T> List<T> getIntersection(List<T> list1, List<T> list2) {
		if (list1 == null||list2==null) {
			return null;
		} 
		
		list1.retainAll(list2);
		
		return list1;
	}

	/**
	 * 获取所有list中的doc数量
	 * 
	 * @param lists
	 * @return
	 */
	public static <E> int getSizeOfLists(List<E>... lists) {
		int amount = 0;
		for (List<E> list : lists) {
			amount += getSizeOfList(list);
		}
		return amount;
	}

	public static int getSizeOfList(List<?> list) {
		if (list != null) {
			return list.size();
		}
		return 0;
	}
	public static boolean isEmpty(Collection coll) {
		return coll == null || coll.isEmpty();
	}
	/**
	 * 对list进行深拷贝
	 * 
	 * @param src
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> deepCopy(List<T> src) {
		if (src == null || src.isEmpty()) {
			return src;
		}

		ObjectOutputStream out = null;
		ObjectInputStream in = null;

		List<T> dest = null;
		try {
			// 将原list写入流中
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			out = new ObjectOutputStream(byteOut);
			out.writeObject(src);
			// 从流中读出来
			ByteArrayInputStream byteIn = new ByteArrayInputStream(
					byteOut.toByteArray());
			in = new ObjectInputStream(byteIn);
			dest = (List<T>) in.readObject();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return dest;
	}
	public static <T> HashSet<T> deepCopy(HashSet<T> src) {
		if (src == null || src.isEmpty()) {
			return src;
		}
		
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		
		HashSet<T> dest = null;
		try {
			// 将原list写入流中
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			out = new ObjectOutputStream(byteOut);
			out.writeObject(src);
			// 从流中读出来
			ByteArrayInputStream byteIn = new ByteArrayInputStream(
					byteOut.toByteArray());
			in = new ObjectInputStream(byteIn);
			dest = (HashSet<T>) in.readObject();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return  dest;
	}
	
	public static void main(String[] args) {
		List<LastDocBean> list1= Lists.newArrayList();

//		@SuppressWarnings("unchecked")
//		//无序
//		List<String> union=(List<String>) CollectionUtils.union(list1, list2);//[3, 3, 2, 1, 7, 6, 5, 4, 17, 9, 12]
//		System.out.println("union:"+union);
//		System.out.println("list1:"+list1);
//		System.out.println("list2:"+list2);
//
//
//		@SuppressWarnings("unchecked")
//		List<String> sub=(List<String>) CollectionUtils.subtract(list2, list1);//[7, 9, 3, 17, 12]
//		System.out.println("sub:"+sub);
//		System.out.println("list1:"+list1);
//		System.out.println("list2:"+list2);
//
//		List<String> list3=Arrays.asList("5","7","9","3","17","1","12");
//		ArrayList<String> list4=new ArrayList<String>(list3);
//		//直接使用LIst的Iterator,在iter.remove()时会报错;使用ArraList不会,why?
//		Iterator<String> iter=list4.iterator();
//		while(iter.hasNext()){
//			String s=iter.next();
//			if("5,3,1".indexOf(s)>=0){
//				iter.remove();
//				System.out.println(s);
//			}
//		}
//		System.out.println("list4:"+list4);
	}

	/**
	 * 将一个list拆分成多个
	 * @param targe
	 * @param size
	 * @return
	 */
	public static List<List<String>> createList(List<String> targe, int size) {
		List<List<String>> listArr = new ArrayList<List<String>>();

		int basesize = targe.size();
		int numOfEachList = basesize / size + 1;

		for (int i = 0; i < size; i++) {
			List<String> sub = new ArrayList<String>();
			//把指定索引数据放入到list中
			for (int j = i * numOfEachList; j <= numOfEachList * (i + 1) - 1; j++) {
				if (j <= targe.size() - 1) {
					sub.add(targe.get(j));
				}
			}
			listArr.add(sub);
		}
		return listArr;
	}
	
}
