package com.androidcode.kraken.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androidcode.kraken.R
import com.androidcode.kraken.firebase.FirestoreClass
import com.androidcode.kraken.model.Board
import com.androidcode.kraken.utils.Constants
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.io.IOException
/////////////////////////////////////////////////////////////////////
/* tạo bảng */
class CreateBoardActivity : BaseActivity() {

    private var mSelectedImageFileUri : Uri? = null
    private lateinit var mUserName: String
    private var mBoardImageURL : String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)
        setUpActionBar()

        // Kiểm tra xem có nhận được dữ liệu từ MainActivity gửi qua không?
        if(intent.hasExtra(Constants.NAME)){
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }
/* truy cập bộ nhớ đt đê up hình*/
        iv_board_image.setOnClickListener {
            // xin quyền truy cập
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Show image chooser
                Constants.showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
// sự kiện cho nút tạo bảng
        btn_create.setOnClickListener {
            if(mSelectedImageFileUri != null){
                uploadBoardImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }

    } // HẾT HÀM onCreate()
// tạo bảng
    private fun createBoard(){
        val assignedUserArrayList: ArrayList<String> = ArrayList()
        assignedUserArrayList.add(getCurrentUserID())

        var board = Board(
            et_board_name.text.toString(),
            mBoardImageURL,
            mUserName, assignedUserArrayList
        )
        FirestoreClass().createBoard(this, board)
    }
// up load hình ảnh lên bảng
    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        //getting the storage reference
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            "BOARD_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                this, mSelectedImageFileUri
            )
        )

        //adding the file to firebase storage
        sRef.putFile(mSelectedImageFileUri!!)
            .addOnSuccessListener { taskSnapshot ->
                // The image upload is success
                Log.i(
                    "Board Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                // Get the downloadable url from the task snapshot
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.i("Downloadable Image URL", uri.toString())

                        // assign the image url to the variable.
                        mBoardImageURL = uri.toString()

                        // Call a function to create board in the database.
                        createBoard()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    exception.message,
                    Toast.LENGTH_LONG
                ).show()
                hideProgressDialog()
            }
    }

    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
// tạo mũi tên và tên sự kiện
    private fun setUpActionBar() {
        setSupportActionBar(toolbar_create_board_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.create_board_title)
        }
        toolbar_create_board_activity.setNavigationOnClickListener { onBackPressed() }
    }
// kiểm tra hình ảnh đc chọn từ bộ nhớ đt
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {
            // URI của hình đã được chọn từ bộ nhớ điện thoại
            mSelectedImageFileUri = data.data
            try {
                // Hiển thị hình ảnh người dùng lên ImageView
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(iv_board_image)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    // Hàm xác định kết quả của quyền thời gian chạy
    // sau khi người dùng cho phép hoặc từ chối quyền
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // TODO: Show image chooser
                Constants.showImageChooser(this)
            }
        } else {
            Toast.makeText(
                this,
                "Oops, you just denied the permission for storage. You can allow it from settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}