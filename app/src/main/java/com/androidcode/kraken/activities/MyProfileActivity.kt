package com.androidcode.kraken.activities

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.androidcode.kraken.R
import com.androidcode.kraken.firebase.FirestoreClass
import com.androidcode.kraken.model.User
import com.androidcode.kraken.utils.Constants
import com.bumptech.glide.Glide
import com.google.common.io.Files.getFileExtension
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.activity_my_profile.iv_user_image_my_profile
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import java.io.IOException
import java.util.jar.Manifest
/* */
//////////////////////////////////////////////////////////////////////
class MyProfileActivity : BaseActivity() {

    // Biến global cho URI của một hình được chọn từ trong bộ nhớ điện thoại
    private var mSelectedImageFileUri: Uri? = null
    // Biến global cho UserDetaild và Profile Image URL
    private lateinit var mUserDetails: User
    // Biến global cho URl thông tin hình của người dùng
    private var mProfileImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setUpActionBar()

        FirestoreClass().loadUserData(this)
/* chọn hình ảnh từ trong máy*/
        iv_user_image_my_profile.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }
        // Cập nhật dữ liệu thông tin người dùng đến database
        btn_update.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }

    } // Hết Hàm onCreate()

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()
        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }

        if (et_name.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = et_name.text.toString()
        }

        if (et_mobile.text.toString() != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = et_mobile.text.toString().toLong()
        }
        // update the data in the database
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }
    /**
     *
     */
    private fun uploadUserImage() {

        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null) {

            //getting the storage reference
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                    this, mSelectedImageFileUri
                )
            )

            //adding the file to reference
            sRef.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // The image upload is success
                    Log.e(
                        "Firebase Image URL",
                        taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )

                    // Get the downloadable url from the task snapshot
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { uri ->
                            Log.e("Downloadable Image URL", uri.toString())

                            // assign the image url to the variable.
                            mProfileImageURL = uri.toString()

                            // Call a function to update user details in the database.
                            updateUserProfileData()
                        }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this@MyProfileActivity,
                        exception.message,
                        Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }
        }
    }
/* nếu  thành công tắt mn load*/
    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
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
/* kiểm tra nếu thành công */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null
        ) {
            // URI của hình đã được chọn từ bộ nhớ điện thoại
            mSelectedImageFileUri = data.data!!
            try {
                // Hiển thị hình ảnh người dùng lên ImageView
                Glide
                    .with(this)
                    .load(Uri.parse(mSelectedImageFileUri.toString()))
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(iv_user_image_my_profile)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
/* tạo mũi tên và tên tiêu đề*/
    private fun setUpActionBar() {
        setSupportActionBar(toolbar_my_profile_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile_tittle)
        }
        toolbar_my_profile_activity.setNavigationOnClickListener { onBackPressed() }
    }
/* truyền thông tin ng dung giao diện thồn tin ng dùng*/
    fun setUserDataInUI(user: User) {
        mUserDetails = user
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_user_image_my_profile)

        et_name.setText(user.name)
        et_email.setText(user.email)
        if (user.mobile != 0L) {
            et_mobile.setText(user.mobile.toString())
        }
    }
}
