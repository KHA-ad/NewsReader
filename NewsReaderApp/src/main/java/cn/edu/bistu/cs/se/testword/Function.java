package cn.edu.bistu.cs.se.testword;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Function {
    private static final String TAG = "myTag";

    public static List<NewsItemModel> parseHtmlData(String result) {

        List<NewsItemModel> list = new ArrayList<>();

        Pattern pattern = Pattern
                .compile("<a style=\"\" href=\"(.*)\" target=\"_blank\" class=\"r bp\"><img src=\"(.*)\" width=\"80\" height=\"50\" title=\"(.*)\"/></a>(\\s*)<h2 class=\"t2 t3\"><a href=\"(.*)\" target=\"_blank\">(.*)</a></h2>(\\s*)<p>(.*)</p>");
        Matcher matcher = pattern.matcher(result);

        StringBuffer sb = new StringBuffer();
        int i = 0;
        while (matcher.find() && i < 100) {
            NewsItemModel model = new NewsItemModel();
            model.setNewsDetailUrl(matcher.group(1).trim());
            model.setUrlImgAddress(matcher.group(2).trim());
            model.setNewsTitle(matcher.group(3).trim());
            model.setNewsSummary(matcher.group(8).trim());

            sb.append("详情页地址：" + matcher.group(1).trim() + "\n");
            sb.append("图片地址：" + matcher.group(2).trim() + "\n");
            sb.append("标题：" + matcher.group(3).trim() + "\n");
            sb.append("概要：" + matcher.group(8).trim() + "\n\n");

            list.add(model);
            i++;
        }

   /*     while (matcher.find()) {
            NewsItemModel model = new NewsItemModel();
            model.setNewsDetailUrl(matcher.group(1).trim());
            model.setUrlImgAddress(matcher.group(2).trim());
            model.setNewsTitle(matcher.group(3).trim());
            model.setNewsSummary(matcher.group(8).trim());

            sb.append("详情页地址：" + matcher.group(1).trim() + "\n");
            sb.append("图片地址：" + matcher.group(2).trim() + "\n");
            sb.append("标题：" + matcher.group(3).trim() + "\n");
            sb.append("概要：" + matcher.group(8).trim() + "\n\n");

            list.add(model);
        }*/

        Log.e("----------------->", sb.toString());

        return list;
    }

    public static List<String> parseHtmlNewsDetailData(String result,String option) {

        List<String> list = new ArrayList<>();
        if(option.equals("CCTV")){
            Pattern pattern = Pattern
                    .compile("<!--repaste.body.begin-->((<p>(.*)</p>(\\s*))*)<!--repaste.body.end-->(\\s*)</div>");//<div class="text" id="zt">(\s*)
            Matcher matcher = pattern.matcher(result);

            StringBuffer sb = new StringBuffer();
            String br = "";
            if (matcher.find()) {
                sb.append(matcher.group(1).trim());
                Pattern patternDetail = Pattern.compile("<p>(.*)</p>(\\s*)");
                Matcher matcherDetail = patternDetail.matcher(sb.toString());
                while (matcherDetail.find()) {
                    br = matcherDetail.group(1).trim();
                    if (br.contains("<br />")) {
                        Pattern patternBr = Pattern.compile("(.*?)<br /><br />");
                        Matcher matcherBr = patternBr.matcher(sb.toString());
                        while (matcherBr.find()) {
                            list.add(matcherBr.group(1).trim() + "\n");
                            Log.e("matcherBr------->", matcherBr.group(1).trim());
                        }
                    } else {
                        list.add(matcherDetail.group(1).trim() + "\n");
                        Log.e("matcherDetail------->", matcherDetail.group(1).trim());
                    }

                }
            } else {
                list.add("-1");
            }
        }else if(option.equals("ChinaDaily")){
            Pattern pattern = Pattern
                    .compile("<p style=\"TEXT-INDENT: (.*)\">(.*)<br/>(\\s*)(.*)</p>");
            Matcher matcher = pattern.matcher(result);
            String sb = "";
            while (matcher.find()){
                sb = matcher.group(2).trim()+"\n"+matcher.group(4).trim()+"\n";
                list.add(sb);
            }
            if(list.size()==0){
                Pattern patternOther = Pattern
                        .compile("<p>(.*)<br/>(\\s*)(.*)</p>");
                Matcher matcherOther = patternOther.matcher(result);
                String str = "";
                while (matcherOther.find()){
                    str = matcherOther.group(1).trim()+"\n"+matcherOther.group(3).trim()+"\n";
                    list.add(str);
                }
            }
        }
        return list;
    }

    public static List<NewsItemModel> parseChinaDailyData(String result) {

        List<NewsItemModel> list = new ArrayList<>();

        Pattern pattern = Pattern
                .compile("<div class=\"mr10\"><a href=\"(.*)\" target=\"_blank\"><img src=\"(.*)\" border=\"0\" width=\"185\" height=\"120\"/></a></div>(\\s*)<div>(\\s*)<h3><a href=\"(.*)\" target=\"_blank\">(.*)</a></h3>(\\s*)<h2>(\\s*)<span>(.*)</span></h2>(\\s*)<p>(.*)</p>");
        Matcher matcher = pattern.matcher(result);

        StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
            NewsItemModel model = new NewsItemModel();
            model.setNewsDetailUrl("http://language.chinadaily.com.cn/"+matcher.group(1).trim());
            model.setUrlImgAddress(matcher.group(2).trim());
            model.setNewsTitle(matcher.group(6).trim());
            model.setNewsSummary(matcher.group(11).trim());

            sb.append("详情页地址：" + matcher.group(1).trim() + "\n");
            sb.append("图片地址：" + matcher.group(2).trim() + "\n");
            sb.append("标题：" + matcher.group(6).trim() + "\n");
            sb.append("概要：" + matcher.group(11).trim() + "\n\n");

            list.add(model);
        }

     //   Log.e("----------------->", sb.toString());

        return list;
    }
}
