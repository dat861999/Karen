package com.androidcode.kraken.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.androidcode.kraken.activities.*
import com.androidcode.kraken.model.Board
import com.androidcode.kraken.model.User
import com.androidcode.kraken.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
// LOADING ALL THE DATA FROM FIRESTORE ON WEBSITE
class FirestoreClass {
    // Create a instance of Firebase Firestore
    private val mFireStore = FirebaseFirestore.getInstance()

    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>){
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener {
                document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                val usersList: ArrayList<User> = ArrayList()
                for(i in document.documents){
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }
                if(activity is MembersActivity)
                activity.setupMembersList(usersList)
                else if(activity is TaskListActivity)
                    activity.boardMembersDetailsList(usersList)
            }.addOnFailureListener { e->
                if(activity is MembersActivity)
                    activity.hideProgressDialog()
                else if(activity is TaskListActivity)
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",e
                )
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                    document ->
                Log.e(activity.javaClass.simpleName, document.toString())
                // todo get board detail
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)
            }.addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating Board")
            }

    }

    fun getBoardsList(activity: MainActivity){
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get()
            .addOnSuccessListener {
                document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())
                val boardList: ArrayList<Board> = ArrayList()
                for(i in document.documents){
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardList.add(board)
                }
                activity.populateBoardListToUI(boardList)
            }.addOnFailureListener { e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating Board",e)
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board){
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName, "TaskList update successfully.")
                if(activity is TaskListActivity)
                    activity.addUpdateTaskListSuccess()
                else if(activity is CardDetailsActivity)
                    activity.addUpdateTaskListSuccess()


            }.addOnFailureListener {
                exception ->
                if(activity is TaskListActivity)
                activity.hideProgressDialog()
                else if(activity is CardDetailsActivity)
                    activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating a board.", exception)
            }
    }

    /**
     * A function allow us to pass in the user as a HashMap
     */
    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>){
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                Log.i(activity.javaClass.simpleName,
                    "Profile Data update Successfully!.")
                Toast.makeText(activity,"Profile updated successfully!.", Toast.LENGTH_SHORT).show()
                when(activity){
                    is MainActivity ->{
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity ->{
                        activity.profileUpdateSuccess()
                    }
                }

            }.addOnFailureListener {
                e ->
                when(activity){
                    is MainActivity ->{
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity ->{
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",e
                )
                Toast.makeText(activity,"Error when updating the profile!.", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * A function to make an entry of the registered user in the firestore database.
     */
    fun registerUser(activity: SignUpActivity, userInfo: User) {

        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(getCurrentUserID())
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                // Here call a function of base activity for transferring the result to it.
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board){
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName,
                    "Board created successfully.")
                Toast.makeText(activity,
                    "Board created successfully.",
                    Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener {
                exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,"Error while creating a board.",
                    exception)
            }
    }

    /**
     * A function for getting the user id of current logged user.
     */
    fun getCurrentUserID(): String {
        // An Instance of currentUser using FirebaseAuth
        val currentUser = FirebaseAuth.getInstance().currentUser
        // A variable to assign the currentUserId if it is not null or else it will be blank.
        var currentUserID = ""
        if (currentUser != null) {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }
    /**
     * lấy tên user hiện tại
     * kiểm tra.
     */
    fun loadUserData(activity: Activity, readBoardsList: Boolean = false) {
        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e(
                    activity.javaClass.simpleName, document.toString()
                )

                // Here we have received the document snapshot which is converted into the User Data model object.
                val loggedInUser = document.toObject(User::class.java)!!
                // Check
                when(activity){
                    is SignInActivity ->{
                        // Here call a function of base activity for transferring the result to it.
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity ->{
                        activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                    }
                    is MyProfileActivity ->{
                        activity.setUserDataInUI(loggedInUser)
                    }
                }
            }
            .addOnFailureListener {
                    e ->
                // Check
                when(activity){
                    is SignInActivity ->{
                        // Here call a function of base activity for transferring the result to it.
                        activity.hideProgressDialog()
                    }
                    is MainActivity ->{
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity ->{
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting loggedIn user details",
                    e
                )
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String){
        mFireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener {
                document ->
                if(document.documents.size > 0){
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                }else{
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found")
                }
            }
            .addOnFailureListener {
                e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while getting user details", e)
            }
    }
    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User){
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while creating a board.")
            }
    }



}