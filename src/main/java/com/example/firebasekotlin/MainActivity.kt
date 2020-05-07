package com.example.firebasekotlin

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val PICK_IMAGE_REQUEST = 1
        private const val RC_SIGN_IN = 9001
        const val TAG = "MainActivity"
    }


    //Google authentication
    private lateinit var googleSignInClient: GoogleSignInClient
    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]



    private var mImageUri: Uri? = null
    private lateinit var mStorageRef: StorageReference
    private lateinit var mDataBaseRef: DatabaseReference
    private var mUploadTask: UploadTask? = null
    private lateinit var DownloadImageUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // [START config_signin]
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // [END config_signin]

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]

        sign_in.setOnClickListener (this)

        sign_out.setOnClickListener (this)



        mStorageRef = FirebaseStorage.getInstance().getReference("uploads")
        mDataBaseRef = FirebaseDatabase.getInstance().getReference("uploads")


        button_choose_image.setOnClickListener {

            openFileChooser()
        }

        button_upload.setOnClickListener {


            if (mUploadTask != null && mUploadTask!!.isInProgress) {

                showToast("Currently Upload in Progress")
            } else {
                upLoadFile()
            }
        }


        text_view_show_uploads.setOnClickListener {

            openImagesActivity()

        }

    }

    // [START on_start_check_user]
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    // [END on_start_check_user]


    private fun getFileExtension(uri: Uri): String? {

        val cr: ContentResolver = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cr.getType(uri))
    }

    private fun upLoadFile() {

        if (mImageUri != null) {

            val fileReference: StorageReference = mStorageRef.child(
                "" + System.currentTimeMillis()
                        + "." + getFileExtension(mImageUri!!)
            )


            mUploadTask = fileReference.putFile(mImageUri!!)


            mUploadTask!!.addOnSuccessListener { task ->

                val handler: Handler = Handler()
                handler.postDelayed(Runnable {

                    progress_bar.progress = 0

                }, 500)

                showToast("Upload Successful", Toast.LENGTH_LONG)

                val urlTask = mUploadTask!!.continueWithTask { task ->

                    if (!task.isSuccessful) {

                        task.exception.let {
                            throw it!!
                        }
                    }

                    DownloadImageUrl = fileReference.downloadUrl.toString()

                    fileReference.downloadUrl
                }.addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        DownloadImageUrl = task.result.toString()

                        showToast("got the product database Url")

                        SaveData_to_Database()

                    }

                }

            }
            mUploadTask!!.addOnFailureListener {

                Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()

            }
            mUploadTask!!.addOnProgressListener {
                val progress = (100.0 * it.bytesTransferred / it.totalByteCount)
                progress_bar.progress = progress.toInt()

            }

        } else {

            showToast("No File selected", Toast.LENGTH_LONG)
        }

    }

    private fun SaveData_to_Database() {

        val upload: Upload = Upload(edit_text_file_name.text.toString(), DownloadImageUrl)

        val uploadId = mDataBaseRef.push().key

        if (uploadId != null) {

            mDataBaseRef.child(uploadId).setValue(upload)
        }


    }


    private fun openImagesActivity() {

        intent = Intent(this, ImagesActivity::class.java)
        startActivity(intent)

    }


    private fun openFileChooser() {

        intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)

    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK
            && data != null && data.data != null
        ) {

            data.let {
                mImageUri = data.data!!

                Glide.with(this)
                    .load(mImageUri)
                    .into(image_view)
            }


        }

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                showToast("SignIn successful")
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // [START_EXCLUDE]
                showToast("SignIn Un-successful")
                updateUI(null)
                // [END_EXCLUDE]
            }
        }
    }
    // [END onactivityresult]


    private fun firebaseAuthWithGoogle(idToken: String) {
        // [START_EXCLUDE silent]
        // showProgressBar()
        // [END_EXCLUDE]
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    showToast("signInWithCredential:success")
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    // [START_EXCLUDE]
                    //val view = binding.mainLayout
                    // [END_EXCLUDE]
                    // Snackbar.make(view, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    showToast("signInWithCredential:failure")
                    updateUI(null)
                }

                // [START_EXCLUDE]
                //hideProgressBar()
                // [END_EXCLUDE]
            }
    }
    // [END auth_with_google]

    // [START signin]
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    // [END signin]


    private fun signOut() {
        // Firebase sign out
        auth.signOut()

        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(this) {
            updateUI(null)
        }
    }


    private fun updateUI(user: FirebaseUser?) {

        if(user!=null){
            sign_out.visibility = View.VISIBLE
            val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(applicationContext)
            account.let {
                val personName: String? = account?.displayName
                val personGivenName = account?.givenName
                val personFamilyName = account?.familyName
                val personEmail = account?.email
                val personId = account?.id
                val personPhoto = account?.photoUrl

                showToast("" + personName + personEmail)
            }
        }else{
            showToast("You Are Logged Out")
            sign_out.visibility = View.INVISIBLE

        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.sign_in -> signIn()
            R.id.sign_out -> signOut()

        }

    }





}
