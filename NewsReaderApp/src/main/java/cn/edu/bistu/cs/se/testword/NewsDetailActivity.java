package cn.edu.bistu.cs.se.testword;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewsDetailActivity extends AppCompatActivity {

    public static final String DetailUrl = "detailUrl";//键
    private static String newsDetailUrl;//值
    public static final String DetailTitle = "title";//键
    private static String newsDetailTitle;//值
    public static final String OPTION = "option";//键
    private static String newsOption;//值
    public static final String NewsSummery = "summery"; //键
    private static String newsSummery; //值

    //获取数据成功
    private final static int GET_DATA_SUCCEED = 1;
    //获取数据失败
    private final static int GET_DATA_Fail = -1;

    public TextView newsDetail;
    public List<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //初始化视图
        initView();
        //初始化数据
        initData();

        Intent intent = getIntent();
        //新闻标题地址获取
        newsDetailTitle = intent.getStringExtra(DetailTitle);
        //新闻详细内容地址获取
        newsDetailUrl = intent.getStringExtra(DetailUrl);
        //新闻概要内容获取
        newsSummery = intent.getStringExtra(NewsSummery);

        newsOption = intent.getStringExtra(OPTION);
    }

    public void initView() {
        list = new ArrayList<String>();
        newsDetail = (TextView) findViewById(R.id.txt_news_detail);
    }

    public void initData() {
        //开启一个线程执行耗时操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                //获取网络数据
                String result = CommonTool.getRequest(newsDetailUrl, "utf-8");

   //             Log.d("结果------------->", result);
     //           Log.d("Runnable: ", result);
                //解析详细新闻数据
                List<String> list = Function.parseHtmlNewsDetailData(result,newsOption);
                if (list.get(0).equals("-1")|| list == null) {   //无正文信息
                    mHandler.sendMessage(mHandler.obtainMessage(GET_DATA_Fail,newsSummery));
                } else {
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < list.size(); i++) {//list.size()
                        sb.append(list.get(i));
                    }
                    StringBuffer str = new StringBuffer();
                    str.append(newsDetailTitle + "\n" + sb);
                    mHandler.sendMessage(mHandler.obtainMessage(GET_DATA_SUCCEED, str));
                }
            }
        }).start();

    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_DATA_SUCCEED:
                    StringBuffer detail = (StringBuffer) msg.obj;
                    //设置不同字体
                    final SpannableStringBuilder sp = new SpannableStringBuilder(detail);
                    sp.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, newsDetailTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //粗体
                    sp.setSpan(new AbsoluteSizeSpan(70), 0, newsDetailTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//字体大小为70像素
                    sp.setSpan(new AbsoluteSizeSpan(50), newsDetailTitle.length(), detail.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //字体大小为50像素

                    newsDetail.setText(sp, TextView.BufferType.SPANNABLE);//点击每个单词响应
                    getEachWord(newsDetail);
                    newsDetail.setMovementMethod(LinkMovementMethod.getInstance());
                    break;
                case GET_DATA_Fail:
                    String summery = (String) msg.obj;
                 //   String click = "\n 点击查看视频";
                    //设置不同字体
                    final SpannableStringBuilder sum = new SpannableStringBuilder(newsDetailTitle + summery);
                    sum.setSpan(new StyleSpan(android.graphics.Typeface.ITALIC), 0, newsDetailTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //粗体
                    sum.setSpan(new AbsoluteSizeSpan(70), 0, newsDetailTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//字体大小为70像素
                    sum.setSpan(new AbsoluteSizeSpan(50), newsDetailTitle.length(), sum.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); //字体大小为50像素
               //     sum.setSpan(new AbsoluteSizeSpan(50),newsDetailTitle.length(),(newsDetailTitle.length()+click.length()),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);//设置视频提示
                    newsDetail.setText(sum);
         /*           newsDetail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(newsDetailUrl));
                            startActivity(intent);
                        }
                    });*/
                    newsDetail.setText(sum, TextView.BufferType.SPANNABLE);//点击每个单词响应
                    getEachWord(newsDetail);
                    newsDetail.setMovementMethod(LinkMovementMethod.getInstance());
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onResume();
    }

    /**
     * 点击响应方法getEachWord（）
     */
    public void getEachWord(TextView textView) {
        Spannable spans = (Spannable) textView.getText();
        Integer[] indices = getIndices(textView.getText().toString().trim() + " ", ' ');
        int start = 0;
        int end = 0;
        // to cater last/only word loop will run equal to the length of indices.length
        for (int i = 0; i < indices.length; i++) {
            ClickableSpan clickSpan = getClickableSpan();
//          to cater last/only word
            end = (i < indices.length ? indices[i] : spans.length());
            spans.setSpan(clickSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = end + 1;
        }
//         改变选中文本的高亮颜色
//        textView.setHighlightColor(Color.BLUE);
    }

    private ClickableSpan getClickableSpan() {
        return new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent;
                TextView tv = (TextView) widget;
                Spannable spans = (Spannable) newsDetail.getText();
                String s = tv.getText().subSequence(tv.getSelectionStart(), tv.getSelectionEnd()).toString();
                if (s.contains(".") || s.contains(",")) {
                    String word = Word(s);
                    intent = new Intent(NewsDetailActivity.this, SearchActivity.class);
                    intent.putExtra(SearchActivity.Word, word);
                    startActivity(intent);
                    Log.e("word", word);
                }else if(s.contains("<em>")){
                    StringBuffer sb = new StringBuffer();
                    Pattern pattern = Pattern
                            .compile("<em>([a-zA-Z]+)+");//
                    Matcher matcher = pattern.matcher(s);
                    if (matcher.find()) {
                        sb.append(matcher.group(1).trim());
                    }
                    intent = new Intent(NewsDetailActivity.this, SearchActivity.class);
                    intent.putExtra(SearchActivity.Word, sb.toString());
                    startActivity(intent);
                } else {
                    intent = new Intent(NewsDetailActivity.this, SearchActivity.class);
                    intent.putExtra(SearchActivity.Word, s);
                    startActivity(intent);
                    Log.e("tapped on:s", s);
                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(Color.BLACK);
                ds.setUnderlineText(false);
            }
        };
    }

    public Integer[] getIndices(String ss, char c) {
        int pos = ss.indexOf(c, 0);
        List<Integer> integers = new ArrayList<>();
        while (pos != -1) {
            integers.add(pos);
            pos = ss.indexOf(c, pos + 1);
        }
        return integers.toArray(new Integer[0]);
    }

    public static String Word(String s) {
        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern
                .compile("([a-zA-Z]+)+");//([a-zA-Z]+[']?)+
        Matcher matcher = pattern.matcher(s);

        if (matcher.find()) {
            sb.append(matcher.group(1).trim());
        }

        Log.e("----------------->", sb.toString());
        return sb.toString();
    }
}
