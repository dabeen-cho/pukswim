package com.example.app;


import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PopupActivity extends Activity{
//사용 안내 팝업

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_activity);
    }

    public void mOnClose(View v){
        Intent intent = new Intent();
        intent.putExtra("result","Close Popup");
        setResult(RESULT_OK, intent);
        finish();
    }
    @Override
    public boolean onTouchEvent (MotionEvent event){
        //바깥레이어 클릭시 안닫히게
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }


    @Override
    public void onBackPressed () {
        //안드로이드 백버튼 막기
        return;
    }



}
