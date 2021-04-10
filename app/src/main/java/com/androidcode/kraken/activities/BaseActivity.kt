package com.androidcode.kraken.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.androidcode.kraken.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.dialog_progress.*
///////////////////////////////////////////////////////////////////////
/*
       từ khóa open cho phep các Activity khác có thể kế thừa
       và sử dụng các phương thức trong lớp cha
* */

open class BaseActivity : AppCompatActivity() {


    private var doubleBackToExitPressedOnce = false

    /**
     * This is a progress dialog instance which we will initialize later on.
     */

    // biến ref Dialog
    private lateinit var mProgressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    /**
     * thực hiện chức năng load
     */
    fun showProgressDialog(text: String) {
        mProgressDialog = Dialog(this)

        /**/
        mProgressDialog.setContentView(R.layout.dialog_progress)

        mProgressDialog.tv_progress_text.text = text

        //Start the dialog and display it on screen.
        mProgressDialog.show()
    }
    /**
     * ẩn đi load
     */
    fun hideProgressDialog() {
        if(::mProgressDialog.isInitialized){
            mProgressDialog.dismiss()
        }else{
            Log.i("Error", "Error while creating mProgressDialog")
        }
    }
    /* lấy id nguoi dung*/
    fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }
    /* nhấn nút thoát ứng dụng*/
    fun doubleBackToExit() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(
            this,
            resources.getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_SHORT
        ).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }
    /* thông báo báo k nhập đầy đủ thông tin đk tk */
    fun showErrorSnackBar(message: String) {
        val snackBar =
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(
            ContextCompat.getColor(
                this@BaseActivity,
                R.color.snackbar_error_color
            )
        )
        snackBar.show()
    }
}