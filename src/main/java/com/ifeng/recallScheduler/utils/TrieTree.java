package com.ifeng.recallScheduler.utils;

import com.google.common.collect.Maps;

import com.ifeng.recallScheduler.item.BanItem;
import com.ifeng.recallScheduler.item.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;


/**
 * 可以支持匹配简单伪正则的Trie树。简单伪正则模式：韩国{10}宣布{5}疫情{5}结束=韩国{0,10}宣布{0,5}疫情{0,5}结束
 * 也支持普通字符串匹配。示例见main函数
 *
 * @author zhouxiaocao
 * @create 2015-12-23
 **/
public class TrieTree implements Serializable {

    //距离子节点
    protected Set<TrieTree> rexDistanceNode = new HashSet<>();

    private final static Logger logger = LoggerFactory.getLogger(TrieTree.class);
    /**
     *
     */
    private static final long serialVersionUID = -7054366549011455074L;
    private boolean isEnd; // 是否到叶子节点
    private Map<String, TrieTree> children; // 子节点
    private String tag;

    //节点类型，0为普通文字节点，1为正则距离节点。
    protected int type = 0;
    //如果其父节点是距离节点，该字段是其父节点tag中定义的距离数值
    protected int rexDistance = 0;
    //当前节点所有距离节点的最大值。
    protected int maxRexDistance = 0;

    protected Map<String, Integer> pointsMap;

    public String toString() {
        String s = this.tag;
        if (!isEnd)
            for (String ttag : children.keySet()) {
                s += "[" + children.get(ttag) + "]";
            }
        return s;
    }

    /**
     * @param @param nodes
     * @return 返回类型
     * @throws Exception
     * @Title: TrieTree
     * @Description: TrieTree构造
     */
    public TrieTree(Collection<String> nodes) {
        init(nodes);
    }

    /**
     * 使用分来判断是否需要过滤
     *
     * @param nodes
     * @param pointsMap
     */
    public TrieTree(Collection<String> nodes, Map<String, Integer> pointsMap) {
        init(nodes);
        this.pointsMap = pointsMap;
    }

    private TrieTree(boolean isEnd, HashMap<String, TrieTree> children) {
        this.isEnd = isEnd;
        this.children = children;
    }

    private void init(Collection<String> nodes) {
        this.isEnd = false;
        this.children = new HashMap<String, TrieTree>();
        TrieTree root = this;  //表示获取当前实例本身
        for (String node : nodes) {
            if (node.trim().length() > 0) { //trim 去掉前后空格
                addWord(root, node, node);  //此处从根节点向下 构建了一颗前缀树
            }

        }
    }


    private void addWord(TrieTree t, String word, String origin) {
        if (word.length() == 0) {
            TrieTree leaf = new TrieTree(true, null); //这里如果word.length
            leaf.tag = origin;
            t.children.put("$end", leaf);  //设置一个重点
        } else {
            int index = 1;
            int rexDis = 0;
            int type = 0;
            if (word.startsWith("{")) {
                type = 1;
                while (word.charAt(index) != '}') {
                    index++;
                }
                rexDis = Integer.valueOf(word.substring(1, index));
                index += 1;
            }
            String addWord = word.substring(0, index); //substring 前面包括很后面不包括
            if (t.children.containsKey(addWord)) {
                addWord(t.children.get(addWord), word.substring(index), origin);
            } else {
                TrieTree subt = new TrieTree(false, new HashMap<String, TrieTree>());
                subt.tag = addWord;
                subt.type = type;
                t.children.put(subt.tag, subt);
                addWord(subt, word.substring(index), origin);
                if (type == 1) {
                    subt.rexDistance = rexDis;
                    t.rexDistanceNode.add(subt);
                    if (rexDis > t.maxRexDistance) {
                        t.maxRexDistance = rexDis;
                    }
                }
            }
        }
    }

    /**
     * @param str        待检测字符串
     * @param index      开始检测的偏移量
     * @param t          用于匹配的trie树
     * @param matchedRex 存放匹配到的正则。
     * @return 匹配的关键词的结束位置+1
     * @Description: 从字符串@param str中找到第一个匹配的关键词
     */
    private static int find(String str, int index, TrieTree t, List<String> matchedRex) {
        String matchNode = null;
        /*if(t.children.containsKey("$end")) {
            matchNode = t.children.get("$end").tag;
			matchedRex.add(matchNode);
			return index;
		}else{*/
        if (index == str.length()) { // 到了字符串末尾
            if (t.children.containsKey("$end")) {
                matchNode = t.children.get("$end").tag;
                matchedRex.add(matchNode);
                return index;
            }
            return -1;
        } else {
            String tag = str.substring(index, index + 1);
            String tagTmp = "";
            Set<TrieTree> rexts = t.rexDistanceNode;
            for (TrieTree rext : rexts) {
                int indexTmp = index;
                for (int dis = 0; dis <= t.maxRexDistance; dis++) {
                    if (rext.rexDistance >= dis && indexTmp < str.length()) {
                        tagTmp = str.substring(indexTmp, indexTmp + 1);
                        if (rext.children.containsKey(tagTmp)) {
                            int rtIndex = find(str, indexTmp + 1, rext.children.get(tagTmp), matchedRex);
                            if (rtIndex > -1) {
                                return rtIndex;
                            }
                        }
                        indexTmp++;
                    }
                }
            }
            if (t.children.containsKey(tag)) {
                if (t.children.containsKey("$end")) {
                    matchNode = t.children.get("$end").tag;
                    matchedRex.add(matchNode);

                }
                return find(str, index + 1, t.children.get(tag), matchedRex);
            } else {
                if (t.children.containsKey("$end")) {
                    matchNode = t.children.get("$end").tag;
                    matchedRex.add(matchNode);
                    return index;
                }
                return -1;
            }

        }
    }

    /**
     * @param str 输入字符串
     * @return Map<匹配到的正则,List<匹配到的字符串的起始位置>>
     * @Description:完全匹配输入的字符串
     */
    public Map<String, List<Integer>> detect(String str) {
        int i = 0;
        Map<String, List<Integer>> matchMap = new HashMap<>();

        while (i < str.length()) {
            List<String> matchedRex = new ArrayList<>();
            int index = find(str, i, this, matchedRex); //this 永远指向包含它的类的实体
            if (matchedRex.size() > 0) {
                //String matchedStr=str.substring(i,index);
                for (int j = 0; j < matchedRex.size(); j++) {
                    List<Integer> pos = matchMap.get(matchedRex.get(j));
                    if (pos == null) {
                        pos = new ArrayList<>();

                        matchMap.put(matchedRex.get(j), pos);
                    }
                    pos.add(i);
                }

                i++;
            } else {
                i++;
            }
        }
        return matchMap;
    }

    /**
     * 判断是否和词典中的数据匹配
     *
     * @param str
     * @return
     */
    public boolean matches(String str) {
        Map result = detect(str);
        if (result.size() == 0) {
            return false;
        } else {
            if(MathUtil.getNum(100)==1){
                logger.debug("matches filter, title:{} ,keywords:{}", str, result.keySet());
            }
            return true;
        }
    }

    /**
     * 判断过滤词典匹配分
     *
     * @param str
     * @return
     */
    public int matchesPoints(String str, int limit,Document document) {  //标题  //threshold //文章
        Map<String, List<Integer>> result = detect(str);

        int points = 0;
        for (Map.Entry<String, List<Integer>> item : result.entrySet()) {
            String keyWord = item.getKey();
            points += pointsMap.get(keyWord);
        }
        if (points >= limit) {
            logger.info("title:{} points filtered ,keywords:{}", str, result.keySet());
            ScoreFilterDoc2ShowUtil.deal2Show(new BanItem(document.getDocId(),str,result.keySet().toString()));
        }

        return points;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((children == null) ? 0 : children.hashCode());
        result = prime * result + (isEnd ? 1231 : 1237);
        result = prime * result + 1237;
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TrieTree)) {
            return false;
        }
        TrieTree other = (TrieTree) obj;
        if (children == null) {
            if (other.children != null) {
                return false;
            }
        } else if (!children.equals(other.children)) {
            return false;
        }
        if (isEnd != other.isEnd) {
            return false;
        }
        if (tag == null) {
            if (other.tag != null) {
                return false;
            }
        } else if (!tag.equals(other.tag)) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        Set<String> starNodes = new HashSet<String>();
        starNodes.add("小");
        starNodes.add("小王子");
        starNodes.add("王思聪");

        //Test without Points 使用没有分的方式，
        TrieTree starTree = new TrieTree(starNodes);
        Map<String, List<Integer>> map = starTree.detect("数量的分开就<小王>,王思聪");
        map = starTree.detect("这是一个测试，哈小王子哈哈哈");
        for (Map.Entry<String, List<Integer>> entry : map.entrySet()) {
            System.out.println(entry.getKey());
            for (Integer count : entry.getValue()) {
                System.out.println(count);
            }
        }
        Document document = new Document();
        Map<String, Integer> pointsMap = Maps.newHashMap();
        pointsMap.put("小", 3);
        pointsMap.put("小王子", 5);
        pointsMap.put("王思聪", 11);
        TrieTree tree = new TrieTree(starNodes, pointsMap);
        int points = tree.matchesPoints("数量的分开就<小王>,王思聪",3,document);
        System.out.println(points);

        //	Map<String, List<Integer>> map=starTree.detect("王思聪献出荧幕首秀 弄好发型的老公帅气呀10元厕所");
        System.out.println("zxc");

    }
}
