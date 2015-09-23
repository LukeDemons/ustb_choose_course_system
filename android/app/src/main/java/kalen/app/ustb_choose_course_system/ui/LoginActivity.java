package kalen.app.ustb_choose_course_system.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.CookieStore;
import java.util.HashMap;
import java.util.Map;
import kalen.app.ustb_choose_course_system.R;
import kalen.app.ustb_choose_course_system.model.ConstVal;
import kalen.app.ustb_choose_course_system.model.UserInfo;
import kalen.app.ustb_choose_course_system.utils.HttpUtils;


public class LoginActivity extends ActionBarActivity {

    EditText unameEdit;
    EditText passEdit;
    CheckBox savePassCkb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        initViews();
        initToolBar();

    }

    private void initToolBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.top_bar);
        TextView mToolBarTextView = (TextView) findViewById(R.id.top_bar_title);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.setNavigationIcon(null);
        //mToolbar.setNavigationIcon();
        mToolBarTextView.setText(R.string.app_name);
    }

    private void initViews() {
        unameEdit = (EditText) findViewById(R.id.login_username_edit);
        passEdit = (EditText) findViewById(R.id.login_pass_edit);
        savePassCkb = (CheckBox) findViewById(R.id.login_is_save_pass);

        SharedPreferences preferences = getSharedPreferences(ConstVal.USER_SHARE_PREFERENCE, MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");
        boolean isChecked = preferences.getBoolean("isChecked", true);
        unameEdit.setText(username);
        passEdit.setText(password);
        savePassCkb.setChecked(isChecked);

        //login button clicked
        findViewById(R.id.login_login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = unameEdit.getText().toString();
                final String password = passEdit.getText().toString();
                if (username.equals("")){
                    Toast.makeText(LoginActivity.this,
                            "请输入用户名", Toast.LENGTH_LONG).show();
                }else if (password.equals("")) {
                    Toast.makeText(LoginActivity.this,
                            "请输入密码", Toast.LENGTH_LONG).show();
                }else{
                    //start asynctask
                    LoginAsyncTask task = new LoginAsyncTask(username, password);
                    task.execute();
                }
            }
        });
    }

    /**
     * Login BackGround Operation
     */
    class LoginAsyncTask extends AsyncTask<Void, Void, String>{

        private String username;
        private String password;

        private ProgressDialog progressDialog;

        public LoginAsyncTask(String username, String password){
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(LoginActivity.this,
                    "请等待...", "正在登陆中...", true, false);
        }

        @Override
        protected String doInBackground(Void... params) {


            Map<String, String> map = new HashMap<>();
            map.put("j_username", username + ",undergraduate");
            map.put("j_password", password);

            try {
                final CookieStore cookieStore = HttpUtils.postWithCookies(
                        ConstVal.LOGIN_URL, map);

                final String result = HttpUtils.get(ConstVal.CHECK_LOGIN_SUCCESS_URL, cookieStore);
                System.out.println("result -->" + result);
                UserInfo userInfo = UserInfo.getInstance();
                userInfo.setCookieStore(cookieStore);
                userInfo.setUsername(username);
                userInfo.setPassword(password);

                return result;

            } catch (Exception e) {//network exception
                e.printStackTrace();
                return null;

            }

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressDialog.dismiss();

            String result = s;

            if (result == null){
                Toast.makeText(LoginActivity.this, "请检查网络配置",
                        Toast.LENGTH_SHORT).show();
            }else if(result.length() > 100){//登录失败
                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                builder.setTitle("登陆失败")
                        .setMessage("原因：学号或密码错误\n注：请输入选课系统密码")
                        .setNegativeButton("确定", null)
                        .create()
                        .show();

            }else{//登陆成功

                //保存用户名密码在 sharedpreference
                SharedPreferences.Editor editor = getSharedPreferences(
                        ConstVal.USER_SHARE_PREFERENCE, MODE_PRIVATE).edit();
                if(savePassCkb.isChecked()){
                    editor.putString("username", username);
                    editor.putString("password", password);
                }else{
                    //editor.putString("username", "");
                    editor.putString("password", "");
                }
                editor.putBoolean("isChecked",savePassCkb.isChecked());
                editor.commit();

                Toast.makeText(LoginActivity.this, "登陆成功",
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        }


    }



    //
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_class_table, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_about) {
//            startActivity(new Intent(LoginActivity.this,
//                    AboutActivity.class));
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
