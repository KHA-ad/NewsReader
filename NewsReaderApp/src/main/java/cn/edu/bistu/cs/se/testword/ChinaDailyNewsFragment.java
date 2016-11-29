package cn.edu.bistu.cs.se.testword;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChinaDailyNewsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChinaDailyNewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChinaDailyNewsFragment extends Fragment {
    public static final String DetailUrl = "detailUrl";//键
    public static final String DetailTitle = "title";//键
    public static final String NewsSummery = "summery"; //键

    private ListView mListView;
    private List<NewsItemModel> list;
    private NewsAdapter newsAdapter;
    //获取数据成功
    private final static int GET_DATA_SUCCEED = 1;
    private ProgressDialog dialog;

    private OnFragmentInteractionListener mListener;

    public ChinaDailyNewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CCTVNewsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChinaDailyNewsFragment newInstance(String title, String detailUrl) {
        ChinaDailyNewsFragment fragment = new ChinaDailyNewsFragment();
        Bundle args = new Bundle();
        //      args.putString(ARG_PARAM1, param1);
        //      args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list = new ArrayList<NewsItemModel>();
        //初始化数据
        //  initData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_china_daily_news, container, false);
        mListView = (ListView) view.findViewById(R.id.list_chinaDaily_news);
        switchOver();
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (OnFragmentInteractionListener) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void initData() {
        /*
        * ([a-zA-Z]+[']*)+  分词
        * */
        //开启一个线程执行耗时操作
        new Thread(new Runnable() {
            @Override
            public void run() {

                String result = CommonTool.getRequest("http://language.chinadaily.com.cn/news_bilingual.html", "utf-8");
                //获取网络数据
                for (int p = 2; p < 7; p++) {
                    result += CommonTool.getRequest("http://language.chinadaily.com.cn/news_bilingual_" + p + ".html", "utf-8");
                }
                if (result.equals("")) {
                    Looper.prepare();
                    Toast.makeText(getActivity(), "当前无网络连接！", Toast.LENGTH_LONG).show();
                    dialog.setCancelable(true);
                    Looper.loop();
                } else {
//                Log.d("结果------------->", result);
                    //              Log.d("Runnable: ", result);
                    //解析新闻数据
                    List<NewsItemModel> list = Function.parseChinaDailyData(result);
                    for (int i = 0; i < list.size(); i++) {//list.size()
                        NewsItemModel model = list.get(i);
                        //获取新闻图片
                        Bitmap bitmap = BitmapFactory.decodeStream(CommonTool.getImgInputStream("http://language.chinadaily.com.cn/"+list.get(i).getUrlImgAddress()));

                        model.setNewsBitmap(bitmap);
                    }
                    mHandler.sendMessage(mHandler.obtainMessage(GET_DATA_SUCCEED, list));
                }
            }


        }).start();

    }


    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_DATA_SUCCEED:
                    List<NewsItemModel> list = (List<NewsItemModel>) msg.obj;
                    //新闻列表适配器
                    newsAdapter = new NewsAdapter(getActivity(), list, R.layout.adapter_news_item);
                    mListView.setAdapter(newsAdapter);
                    //设置点击事件
                    mListView.setOnItemClickListener(new ItemClickListener());
                    Toast.makeText(getActivity(), String.valueOf(list.size()), Toast.LENGTH_LONG).show();
                    dialog.dismiss();  // 关闭加载窗口
                    break;
            }
        }
    };

    /**
     * 新闻列表点击事件
     */
    public class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            NewsItemModel temp = (NewsItemModel) newsAdapter.getItem(i);
            Toast.makeText(getActivity(), temp.getNewsTitle(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getActivity(), NewsDetailActivity.class);
            //        intent.setAction(Intent.ACTION_VIEW);
            //      intent.setData(Uri.parse(temp.getNewsDetailUrl()));
            intent.putExtra(DetailUrl, temp.getNewsDetailUrl());
            intent.putExtra(DetailTitle, temp.getNewsTitle());
            intent.putExtra(NewsDetailActivity.OPTION, "ChinaDaily");
            startActivity(intent);
        }
    }

    // 判断是否有可用的网络连接
    public boolean isNetworkAvailable(Activity activity) {
        Context context = activity.getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        else {   // 获取所有NetworkInfo对象
            NetworkInfo[] networkInfo = cm.getAllNetworkInfo();
            if (networkInfo != null && networkInfo.length > 0) {
                for (int i = 0; i < networkInfo.length; i++)
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;  // 存在可用的网络连接
            }
        }
        return false;
    }

    // 数据加载
    public void switchOver() {
        if (isNetworkAvailable(getActivity())) {
            // 显示“正在加载”窗口
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("正在读取新闻...");
            dialog.setCancelable(false);
            dialog.show();
            //初始化数据
            initData();
        } else {
            // 弹出提示框
            new AlertDialog.Builder(getActivity())
                    .setTitle("提示")
                    .setMessage("当前没有网络连接！")
                    .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switchOver();
                        }
                    }).setNegativeButton("退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    System.exit(0);  // 退出程序
                }
            }).show();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
