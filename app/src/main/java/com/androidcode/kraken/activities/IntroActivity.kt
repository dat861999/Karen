package com.androidcode.kraken.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.androidcode.kraken.R
import kotlinx.android.synthetic.main.activity_intro.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class IntroActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        // This is used to hide the status bar and make the splash screen as a full screen activity.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        btn_sign_in_intro.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        btn_sign_up_intro.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }


}