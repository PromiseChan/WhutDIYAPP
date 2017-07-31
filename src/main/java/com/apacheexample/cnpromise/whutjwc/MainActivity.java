package com.apacheexample.cnpromise.whutjwc;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    public static String LOGIN_URL = "http://sso.jwc.whut.edu.cn/Certification//login.do";
    private int content_length;

    private String cerlogin;
    private String jsessionid;

    //获取用户名
    TextView tv_username;
    //获取密码
    TextView tv_pwd ;
    //获取身份
    TextView tv_type;
    //登录按钮
    Button btn_login;
    //教务处网页代码
    TextView tv_coures;
    //处理url连接和UI控件的handler
    Handler handler = null;
    //返回网页代码
    String html = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_username = (TextView) findViewById(R.id.et_username);
        //获取密码
        tv_pwd = (TextView) findViewById(R.id.ed_pwd);

        //获取身份
        tv_type = (TextView) findViewById(R.id.ed_type);

        //登录按钮
        btn_login = (Button) findViewById(R.id.btn_login);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable()
                {
                    public void run() {
                        send();
                        Message msg = handler.obtainMessage();
                        handler.sendMessage(msg);

                    }

                }).start();

            }

        });

        handler = new Handler(){
            public void handleMessage(Message msg){
                //Toast.makeText(MainActivity.this,html,Toast.LENGTH_SHORT).show();
                if (html!=null){
                    String text = showCourse();
                    setContentView(R.layout.courseshow);
                    tv_coures = (TextView) findViewById(R.id.tv_Course);


                    tv_coures.setText(text);

                }
                super.handleMessage(msg);
            }

        };
    }


    //发送HTML请求
    private void send() {
        URL url;
        String postContent = "systemId=&xmlmsg=&userName="+tv_username.getText()+"&password="+tv_pwd.getText()+"&type="+tv_type.getText()+"&imageField.x=0&imageField.y=0";
        content_length = postContent.length();  //请求消息长度
        List<String> cookie = null;
        try {
            url=new URL(LOGIN_URL);
            HttpURLConnection urlConn= (HttpURLConnection) url.openConnection();//注意:httpURLconnetion的创建是这样自的
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("POST");
            urlConn.setReadTimeout(6000);
            //设置头文件
            loginSetRequestProperty(urlConn);
            OutputStream postOS = urlConn.getOutputStream();
            postOS.write(postContent.getBytes());
            //获取返回的cookies
            Map<String, List<String>> headerFields = urlConn.getHeaderFields();
            cookie = headerFields.get("Set-Cookie");
            cerlogin = cookie.get(0);
            jsessionid = cookie.get(1);
            //如果成功获取返回的HTML，用result保存
            if (urlConn.getResponseCode()==HttpURLConnection.HTTP_OK){//连接成功,则相等,httpURLConnection.http_ok=200
                BufferedReader bufferedReader=new BufferedReader(new  //处理流,转换流,节点流
                        InputStreamReader(urlConn.getInputStream()));
                String line=null;
                while ((line=bufferedReader.readLine())!=null){
                    html+=line+"\n";                              //加了个"\n"相当于换到了下一行
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    public String getCerlogin() {
        return cerlogin;
    }

    public String getJsessionid() {
        return jsessionid;
    }

    //设置头文件方法
    private void loginSetRequestProperty(HttpURLConnection huc) {
        huc.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        huc.setRequestProperty("Accept-Encoding", "gzip, deflate");
        huc.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.6,en;q=0.4");
        huc.setRequestProperty("Cache-Control", "max-age=0");
        huc.setRequestProperty("Connection", "keep-alive");
        huc.setRequestProperty("Content-Length",content_length+"");
        huc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        huc.setRequestProperty("Host", "sso.jwc.whut.edu.cn");
        huc.setRequestProperty("Origin", "http://sso.jwc.whut.edu.cn");
        huc.setRequestProperty("Referer", "http://sso.jwc.whut.edu.cn/Certification//toLogin.do");
        huc.setRequestProperty("Upgrade-Insecure-Requests", "1");
        huc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
    }




    //将返回的HTML源码进行解析，获取课表
    private String showCourse(){
        String text="";
        if(!html.equals(null)) {
            Document document = Jsoup.parse(html);
            Elements tables =  document.getElementsByClass("mytable");
            Element table = tables.first();
            if(table!=null) {
                Elements trs =  table.getElementsByTag("tr");
                for (int i = 0;i<trs.size();i++) {
                    Elements tds = trs.get(i).select("td");
                    for(int j = 0;j<3;j++) {
                        text = text+tds.get(j).text()+"\t";

//                            text = new String(text.getBytes(),"UTF-8").replace('?', ' ').replace('　', ' ');
//                              text ="\t";
//                            text = String.format("%-15s", text);
                    }
                    text=text+"\n";
                }
            }
            else {
                Toast.makeText(MainActivity.this,"课表为空",Toast.LENGTH_LONG).show();
            }
        }
        return  text;
    }








}
