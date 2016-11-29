package cn.edu.bistu.cs.se.testword;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "myTag";
    private static TextView textViewSearch;
    private static Button buttonSearch;
    private static TextView tv;

    public static final String Word = "word";//键
    private static String searchWord;//值

    private String YouDaoBaseUrl = "http://fanyi.youdao.com/openapi.do";
    private String YouDaoKeyFrom = "haobaoshui";
    private String YouDaoKey = "1650542691";
    private String YouDaoType = "data";
    private String YouDaoDoctype = "json"; //xml或json或jsonp
    private String YouDaoVersion = "1.1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tv = (TextView) findViewById(R.id.tv);
        search();
    }

    @Override
    protected void onResume() {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onResume();
    }

    private void search() {
            Intent intent = getIntent();
            searchWord = intent.getStringExtra(Word);
            if (searchWord == null || "".equals(searchWord)) {
                Toast.makeText(getApplicationContext(), "请输入要翻译的内容", Toast.LENGTH_SHORT).show();
            }
            final String YouDaoUrl = YouDaoBaseUrl + "?keyfrom=" + YouDaoKeyFrom + "&key=" + YouDaoKey + "&type="
                    + YouDaoType + "&doctype=" + YouDaoDoctype + "&type=" + YouDaoType + "&version=" + YouDaoVersion
                    + "&q=" + searchWord;

            new AsyncTask<String, Float, String>() {
                @Override
                protected String doInBackground(String... params) {
                    URL url = null;
                    try {
                        url = new URL(YouDaoUrl);
                        URLConnection connection = url.openConnection();
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String str;
                        StringBuffer builder = new StringBuffer();
                        while ((str = br.readLine()) != null) {
                            builder.append(str);
                        }
                        br.close();
                        return builder.toString();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(String s) {
                    tv.setText("");
                    tv.append(JSONTransform(s));//JSONTransform(s)
                    super.onPostExecute(s);
                }
            }.execute(YouDaoUrl);
        }

        //有道传回的JSON数据解析
        private String JSONTransform(String data) {
            StringBuilder strMeaning = new StringBuilder();
            StringBuilder strSample = new StringBuilder();
            StringBuilder buff = new StringBuilder();
            try {
                JSONObject root = new JSONObject(data);
                JSONArray arr = root.getJSONArray("translation");
                buff.append("有道翻译：");
                for (int i = 0; i < arr.length(); i++) {
                    buff.append(arr.get(i).toString() + "  ");
                }
                buff.append("\n" + "基本词典:\n ");
                JSONObject basic = root.getJSONObject("basic");
                buff.append("发音:|" + basic.getString("phonetic") + "\n");
                if (basic.get("uk-phonetic") != null)
                    buff.append("英式发音:|" + basic.getString("phonetic") + "\n");
                if (basic.get("us-phonetic") != null)
                    buff.append("美式发音:|" + basic.getString("phonetic") + "\n");
                buff.append("基本词典解释：\n");
                JSONArray explains = basic.getJSONArray("explains");
                for (int i = 0; i < explains.length(); i++) {
                    buff.append(explains.get(i).toString() + "\n ");
                    strMeaning.append(explains.get(i).toString() + "\n ");
                }
                buff.append("\n" + "网络释义:\n ");
                JSONArray web = root.getJSONArray("web");
                for (int j = 0; j < web.length(); j++) {
                    if (web.getJSONObject(j).has("key")) {
                        String key = web.getJSONObject(j).getString("key");
                        buff.append(key + "\n");
                        strSample.append(key + "\n");
                    }
                    if (web.getJSONObject(j).has("value")) {
                        JSONArray value = ((JSONObject) web.get(j)).getJSONArray("value");
                        for (int i = 0; i < value.length(); i++) {
                            buff.append(value.get(i).toString() + "  ");
                            strSample.append(value.get(i).toString() + "  ");
                        }
                        buff.append("\n");
                        strSample.append("\n");
                    }
                }
             //   WordsDB wordsDB = WordsDB.getWordsDB();
             //   wordsDB.Insert(searchWord, strMeaning.toString(), strSample.toString());

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(SearchActivity.this, "网络未连接", Toast.LENGTH_SHORT).show();
            }
            return buff.toString();
        }

        //有道传回的XML数据解析
        private String XMLTransform(String data) {
            StringBuffer buffer = new StringBuffer();

            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                StringReader sr = new StringReader(data);
                InputSource in = new InputSource(sr);
                Document document = factory.newDocumentBuilder().parse(in);
                Element errorCode = (Element) document.getElementsByTagName("errorCode").item(0);
                Element translation = (Element) document.getElementsByTagName("translation").item(0);
                NodeList paragraphs = translation.getElementsByTagName("paragraph");
                for (int i = 0; i < paragraphs.getLength(); i++) {
                    Element paragraph = (Element) paragraphs.item(0);
                }
                buffer.append("\n基本释义:\n音标\n");
                Element basic = (Element) document.getElementsByTagName("basic").item(0);
                Element us_phonetic = (Element) basic.getElementsByTagName("us-phonetic").item(0);
                Element uk_phonetic = (Element) basic.getElementsByTagName("uk-phonetic").item(0);
                if (us_phonetic != null)
                    buffer.append("美式音标：" + us_phonetic.getTextContent() + "\n");
                if (uk_phonetic != null)
                    buffer.append("英式音标：" + uk_phonetic.getTextContent() + "\n");
                Element explains = (Element) basic.getElementsByTagName("explains").item(0);
                Element ex = (Element) explains.getElementsByTagName("ex").item(0);
                buffer.append(ex.getTextContent() + "\n");
                buffer.append("网络释义:\n");
                Element web = (Element) document.getElementsByTagName("web").item(0);
                NodeList arrExplain = web.getElementsByTagName("explain");
                for (int i = 0; i < arrExplain.getLength(); i++) {
                    Element explain = (Element) arrExplain.item(i);
                    Element key = (Element) explain.getElementsByTagName("key").item(0);
                    buffer.append(key.getTextContent() + "\n");
                    Element value = (Element) explain.getElementsByTagName("value").item(0);
                    NodeList exs = value.getElementsByTagName("ex");
                    for (int j = 0; j < exs.getLength(); j++) {
                        Element exTemp = (Element) exs.item(j);
                        buffer.append(exTemp.getTextContent() + " ");
                    }
                    buffer.append("\n");
                }

            } catch (ParserConfigurationException e) {
                Toast.makeText(SearchActivity.this, "Parse", Toast.LENGTH_SHORT).show();
            } catch (SAXException e) {
                Toast.makeText(SearchActivity.this, "SAX", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (IOException e) {
                Toast.makeText(SearchActivity.this, "IOE", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            return buffer.toString();
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.return_main:
                //返回详细新闻界面
                Intent intent=new Intent(SearchActivity.this,NewsDetailActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}




