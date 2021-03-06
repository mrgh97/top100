package com.person;
/**
 * @author:morigenhu
 * @date:2021-2-28
 */

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static String FileName = "E:\\coding\\dataset\\weibo\\weibo.txt";
    private static int fileCount = 1000;
    private static int topN = 100;
    // 存放结果数据
    private static ArrayList<Url> res = new ArrayList<>();


    // 初始化文件流对象集合
    private static ArrayList<FileWriter> WriterLists=new ArrayList<FileWriter>();
    private static ArrayList<BufferedWriter> BwLists=new ArrayList<BufferedWriter>();
//    private static ArrayList<FileReader> FrList=new ArrayList<FileReader>();
//    private static ArrayList<BufferedReader> BrList=new ArrayList<BufferedReader>();

    /**
     * @desc 自定义分区，根据子文件数量进行分区
     * @param obj 分区对象
     */
    private static void splitFile(SplitBuckets obj) {
        int partitionId = obj.getPartitionId();
        String childFileName= "data\\child_file_" + String.valueOf(partitionId);
        // 获取到对应输出文件的文件流
        try {
            BufferedWriter bw = BwLists.get(partitionId);
            bw.write(obj.getUid() + "\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param fileNums 文件切分数量
     */
    private static void createFiles(int fileNums) {
        FileWriter fs = null;
        BufferedWriter fw = null;
        try {
            for (int i = 0; i < fileNums; i++) {
                String childFileName= "data\\child_file_" + i;
                File file = new File(childFileName);

                if(!file.exists()) {
                    file.createNewFile();
                }
                // 开启文件写入流
                fs = new FileWriter(childFileName,false);
                fw = new BufferedWriter(fs);

                WriterLists.add(fs);
                BwLists.add(fw);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    /**
     * @desc 基于堆结构子文件局部排序，并取出前100大的数
     * @param br 读出流
     */
    private static void heapSortAndFindTop100(BufferedReader br) {
        HashMap<String, Url> map = new HashMap<>();
//        ArrayList<Url> tmpArr = new ArrayList<>();
        LinkedList<Url> urlHeap = new LinkedList<>();
        int count = 0;

        try (Stream<String> lines = br.lines()){
            lines.forEach(line -> {
                    if (map.containsKey(line)) {
                        Url url = map.get(line);
                        url.setNums(url.getNums() + 1);
                        // 如果新取到的url出现次数大于堆顶元素且该url不在堆内
                        if (url.getNums() > urlHeap.get(0).getNums() && !urlHeap.contains(url)) {
                            urlHeap.set(0, url);
                            sort(topN, urlHeap,0);
                        }
                    } else {
                        Url url =  new Url(line, 1);
                        map.put(line, url);
                        // 初始化堆结构
                        if (urlHeap.size() < topN) {
                            urlHeap.add(url);
                            // 构建小顶堆
                            if (urlHeap.size() == topN) {
                                for(int i = topN/2 - 1; i >= 0; i--) {
                                    sort(urlHeap.size(), urlHeap, i);
                                }
                            }
                        }
                    }
                }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(int i = 0; i < topN && i < urlHeap.size(); i++) {
            Url urlTmp = urlHeap.get(i);
            res.add(urlTmp);
        }
    }

    /**
     * @desc 小顶堆结构维持
     * @param end
     * @param smallTree
     * @param pos
     */
    private static void sort(int end, LinkedList<Url> smallTree, int pos) {
        int tmp = smallTree.get(pos).getNums();
        Url tmpObj = smallTree.get(pos);

        for(int j = 2 * pos + 1;j < end; j = 2*pos + 1){
            if( j + 1 < end && smallTree.get(j + 1).getNums() < smallTree.get(j).getNums()) {
                j++;
            }
            if(tmp > smallTree.get(j).getNums()){
                smallTree.set(pos, smallTree.get(j));
                pos = j;
            }else{
                break;
            }
        }
        smallTree.set(pos, tmpObj);
    }


    /**
     * @desc 子文件局部排序，并取出前100大的数
     * @param br 读出流
     */
    private static void sortAndFindTop100(BufferedReader br) {
        HashMap<String, Url> map = new HashMap<>();
        ArrayList<Url> tmpArr = new ArrayList<>();

        try (Stream<String> lines = br.lines()){
            lines.forEach(line -> {
                        if (map.containsKey(line)) {
                            map.get(line).setNums(map.get(line).getNums() + 1);
                        } else {
                            Url url =  new Url(line, 1);
                            map.put(line, url);
                            tmpArr.add(url);
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        tmpArr.sort((s1, s2) -> {
            // 根据url出现次数降序
            return s2.getNums().compareTo(s1.getNums());
        });

        for(int i = 0; i < topN && i < tmpArr.size(); i++) {
            Url urlTmp = tmpArr.get(i);
//            System.out.println("After sort:\n" + tmpArr.get(i).getUrl() + " " + tmpArr.get(i).getNums());
            res.add(urlTmp);
        }
    }


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        // 创建1000个子文件，并将子文件写入流准备就绪
        createFiles(fileCount);
        Path filePath = Paths.get(FileName);
        try (Stream<String> lines = Files.lines(filePath)) {
            lines.map(line -> line.split("\t"))
                    .map(line -> new SplitBuckets(line[1], Math.abs(line[1].hashCode() % fileCount)))
                    .forEach(Main::splitFile);

            // 关闭文件写入流
            for (int i = 0; i < BwLists.size(); i++) {
                BwLists.get(i).close();
                WriterLists.get(i).close();
            }

            // 子文件局部排序
            FileReader fr = null;
            BufferedReader br = null;
            for (int i = 0; i < fileCount; i++) {
                String childFileName= "data\\child_file_" + i;
                fr = new FileReader(childFileName);
                br = new BufferedReader(fr);
                heapSortAndFindTop100(br);
                br.close();
                fr.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 存放了1000个子文件的Top100数组(数组size为100000个Url对象的内存)进行内部排序
        res.sort((s1, s2) -> {
            // 根据url出现次数降序
            return s2.getNums().compareTo(s1.getNums());
        });

        // 最终结果打印
        System.out.println(res.size());
        for (int i = 0; i < topN; i++){
            System.out.println("Last result:" + res.get(i).getUrl() + " " + res.get(i).getNums());
        }

        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间：" + (endTime - startTime));
    }
}
