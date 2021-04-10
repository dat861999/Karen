package com.androidcode.kraken.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.GridLayout
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.androidcode.kraken.R
import com.androidcode.kraken.adapters.CardMemberListItemsAdapter
import com.androidcode.kraken.dialogs.LabelColorListDialog
import com.androidcode.kraken.dialogs.MembersListDialog
import com.androidcode.kraken.firebase.FirestoreClass
import com.androidcode.kraken.model.*
import com.androidcode.kraken.utils.Constants
import com.google.firebase.database.collection.LLRBNode
import kotlinx.android.synthetic.main.activity_card_details.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CardDetailsActivity : BaseActivity() {
    // A global variable for board details
    private lateinit var mBoardDetails: Board
    // A global variable for task item position
    private var mTaskListPosition = -1
    // A global variable for card item position
    private var mCardPosition = -1
    // A global variable for selected label color
    private var mSelectedColor = ""
    // A global variable for Assigned Members List.
    private lateinit var mMembersDetailList: ArrayList<User>
    private var mSelectedDueDateMilliSeconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)
        getIntentData()
        setUpActionBar()

        et_name_card_details.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        et_name_card_details.setSelection(et_name_card_details.text.toString().length)

        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor
        if(mSelectedColor.isNotEmpty()){
            setColor()
        }

        tv_select_label_color.setOnClickListener {
            labelColorsListDialog()
        }

        setUpSelectedMembersList()

        tv_select_members.setOnClickListener {
            membersListDialog()
        }

        mSelectedDueDateMilliSeconds = mBoardDetails
            .taskList[mTaskListPosition]
            .cards[mCardPosition].dueDate

        if(mSelectedDueDateMilliSeconds > 0){
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyy", Locale.ENGLISH)
            val selectedDate = simpleDateFormat.format(Date(mSelectedDueDateMilliSeconds))
            tv_select_due_date.text = selectedDate
        }
        // // Xử lý sự kiện chọn ngày tới hạn
        tv_select_due_date.setOnClickListener {
            showDataPicker()
        }
        // Xử lý sự kiện nút update Card
        btn_update_card_details.setOnClickListener {
            if(et_name_card_details.text.toString().isNotEmpty()){
                updateCardDetails()
            }else{
               Toast.makeText(this@CardDetailsActivity,
               "Enter a card name.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun addUpdateTaskListSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_card_details_activity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails
                .taskList[mTaskListPosition]
                .cards[mCardPosition].name

        }
        toolbar_card_details_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun colorsList(): ArrayList<String>{
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#FF0000")
        colorsList.add("#800080")
        colorsList.add("#FFB6C1")
        colorsList.add("#FFD700")
        colorsList.add("#FFA500")
        colorsList.add("#008080")
        colorsList.add("#8A2BE2")
        colorsList.add("#FFC3A0")
        colorsList.add("#00CED1")
        colorsList.add("#808080")
        return colorsList
    }
    private fun setColor(){
        tv_select_label_color.text = ""
        tv_select_label_color.setBackgroundColor(Color.parseColor(mSelectedColor))
    }
    private fun labelColorsListDialog(){
        val colorsList: ArrayList<String> =  colorsList()
        val listDialog = object: LabelColorListDialog(
            this, colorsList,
            resources.getString(R.string.str_select_label_color),mSelectedColor){
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }
        listDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate menu để sử dụng thanh Action Bar
        menuInflater.inflate(R.menu.menu_delete_card,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Xừ lý sự kiện khi nhấn vào items trên thanh Action Bar
        when(item.itemId){
            R.id.action_delete_card ->{
                alertDialogForDeleteCard(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getIntentData(){
        if(intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants
                .BOARD_DETAIL)!!

        }
        if(intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)){
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if(intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
        if(intent.hasExtra(Constants.BOARD_MEMBERS_LIST)){
            mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    private fun membersListDialog(){
        var cardAssignedMembersList = mBoardDetails.taskList[mTaskListPosition]
            .cards[mCardPosition].assignedTo

        if(cardAssignedMembersList.size > 0){
            for(i in mMembersDetailList.indices){
                for(j in cardAssignedMembersList){
                    if(mMembersDetailList[i].id == j){
                        mMembersDetailList[i].selected = true

                    }
                }
            }
        }else{
            for(i in mMembersDetailList.indices){
               mMembersDetailList[i].selected = false
            }
        }
        val listDialog = object : MembersListDialog(
            this,mMembersDetailList, resources.getString(R.string.str_select_member)

        ){
            override fun onItemSelected(user: User, action: String) {
                if(action == Constants.SELECT){
                    if(!mBoardDetails
                            .taskList[mTaskListPosition]
                            .cards[mCardPosition]
                            .assignedTo.contains(user.id)){
                        mBoardDetails
                            .taskList[mTaskListPosition]
                            .cards[mCardPosition]
                            .assignedTo.add(user.id)
                    }
                }
                else{
                    mBoardDetails
                        .taskList[mTaskListPosition]
                        .cards[mCardPosition]
                        .assignedTo.remove(user.id)

                    for(i in mMembersDetailList.indices){
                        if(mMembersDetailList[i].id == user.id){
                            mMembersDetailList[i].selected = false
                        }
                    }
                }
                setUpSelectedMembersList()
            }

        }
        listDialog.show()
    }

    private fun setUpSelectedMembersList(){
        val cardAssignedMemberList =
            mBoardDetails
                .taskList[mTaskListPosition]
                .cards[mCardPosition]
                .assignedTo
        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()
        for(i in mMembersDetailList.indices){
            for(j in cardAssignedMemberList){
                if(mMembersDetailList[i].id == j){
                    val selectedMember = SelectedMembers(
                        mMembersDetailList[i].id,
                        mMembersDetailList[i].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }
        if(selectedMembersList.size > 0){
            selectedMembersList.add(SelectedMembers("",""))
            tv_select_members.visibility = View.GONE
            rv_selected_members_list.visibility = View.VISIBLE

            rv_selected_members_list.layoutManager = GridLayoutManager(
                this,6
            )
            val adapter = CardMemberListItemsAdapter(this, selectedMembersList,true )
            rv_selected_members_list.adapter = adapter
            adapter.setOnClickListener(
                object : CardMemberListItemsAdapter.OnClickListener{
                    override fun onClick() {
                        membersListDialog()
                    }
                }
            )
        }else{
            tv_select_members.visibility = View.VISIBLE
            rv_selected_members_list.visibility =  View.GONE

        }
    }

    // START
    /**
     * The function to show the DatePicker Dialog and select the due date.
     */
    private fun showDataPicker() {
        /**
         * This Gets a calendar using the default time zone and locale.
         * The calender returned is based on the current time
         * in the default time zone with the default.
         */
        val c = Calendar.getInstance()
        val year =
            c.get(Calendar.YEAR) // Returns the value of the given calendar field. This indicates YEAR
        val month = c.get(Calendar.MONTH) // This indicates the Month
        val day = c.get(Calendar.DAY_OF_MONTH) // This indicates the Day

        /**
         * Creates a new date picker dialog for the specified date using the parent
         * context's default date picker dialog theme.
         */
        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                /*
                  The listener used to indicate the user has finished selecting a date.
                 Here the selected date is set into format i.e : day/Month/Year
                  And the month is counted in java is 0 to 11 so we need to add +1 so it can be as selected.

                 Here the selected date is set into format i.e : day/Month/Year
                  And the month is counted in java is 0 to 11 so we need to add +1 so it can be as selected.*/

                // Here we have appended 0 if the selected day is smaller than 10 to make it double digit value.
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"
                // Here we have appended 0 if the selected month is smaller than 10 to make it double digit value.
                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                // Selected date it set to the TextView to make it visible to user.
                tv_select_due_date.text = selectedDate

                /**
                 * Here we have taken an instance of Date Formatter as it will format our
                 * selected date in the format which we pass it as an parameter and Locale.
                 * Here I have passed the format as dd/MM/yyyy.
                 */
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                // The formatter will parse the selected date in to Date object
                // so we can simply get date in to milliseconds.
                val theDate = sdf.parse(selectedDate)

                /** Here we have get the time in milliSeconds from Date object
                 */
                mSelectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show() // It is used to show the datePicker Dialog.
    }
    // END

    private fun updateCardDetails(){
        val card = Card(
            et_name_card_details.text.toString(),
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            mSelectedColor,
            mSelectedDueDateMilliSeconds
        )
        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)
        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }

    private fun deleteCard(){
        val cardsList: ArrayList<Card> = mBoardDetails
            .taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardPosition)

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)
        taskList[mTaskListPosition].cards = cardsList
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this@CardDetailsActivity, mBoardDetails)
    }
    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle(resources.getString(R.string.alert))
        //set message for alert dialog
        builder.setMessage(resources.getString(R.string.confirmation_message_to_delete_card,
            cardName
            ))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
            deleteCard()
        }
        //performing negative action
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.dismiss() // Dialog will be dismissed
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
        alertDialog.show()  // show the dialog to UI
    }
}